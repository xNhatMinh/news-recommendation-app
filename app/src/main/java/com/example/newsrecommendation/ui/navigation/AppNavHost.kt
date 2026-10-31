package com.example.newsrecommendation.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.*
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.DeviceTokenRequest
import com.example.newsrecommendation.storage.TokenManager
import com.example.newsrecommendation.ui.screens.article.ArticleDetailScreen
import com.example.newsrecommendation.ui.screens.auth.LoginScreen
import com.example.newsrecommendation.ui.screens.auth.RegisterScreen
import com.example.newsrecommendation.ui.screens.home.HomeScreen
import com.example.newsrecommendation.ui.screens.notifications.NotificationsScreen
import com.example.newsrecommendation.ui.screens.preferences.PreferencesScreen
import com.example.newsrecommendation.ui.screens.saved.SavedArticlesScreen
import com.example.newsrecommendation.ui.screens.search.SearchScreen
import com.example.newsrecommendation.ui.screens.summary.SummaryScreen
import com.example.newsrecommendation.ui.viewmodel.SessionViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val FOR_YOU = "for_you"

@Composable
fun AppNavHost(
    openNotifications: Boolean = false,
    onNotificationHandled: () -> Unit = {},
    session: SessionViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var pendingDestination by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingBookmarkUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var saveAfterLoginUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var openForYouAfterLogin by rememberSaveable { mutableStateOf(false) }
    var showLoginPrompt by rememberSaveable { mutableStateOf(false) }
    var handlingExpiry by remember { mutableStateOf(false) }

    fun articleRoute(url: String) = "${Routes.ARTICLE_DETAIL}?url=${Uri.encode(url)}"

    fun openArticle(url: String) {
        navController.navigate(articleRoute(url)) { launchSingleTop = true }
    }

    fun clearPendingLogin() {
        pendingDestination = null
        pendingBookmarkUrl = null
        showLoginPrompt = false
    }

    fun requestLogin(destination: String? = null, bookmarkUrl: String? = null, prompt: Boolean = true) {
        pendingDestination = destination
        pendingBookmarkUrl = bookmarkUrl
        if (prompt) showLoginPrompt = true
        else navController.navigate(Routes.LOGIN) { launchSingleTop = true }
    }

    fun openProtected(destination: String) {
        scope.launch {
            session.awaitRestored()
            if (session.isLoggedIn) {
                navController.navigate(destination) { launchSingleTop = true }
            } else {
                requestLogin(destination)
            }
        }
    }

    fun expireSession(destination: String? = null, bookmarkUrl: String? = null) {
        if (handlingExpiry) return
        handlingExpiry = true
        scope.launch {
            try {
                session.clearSession()
                if (destination != null && bookmarkUrl == null) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                if (destination != null) requestLogin(destination, bookmarkUrl)
            } finally {
                handlingExpiry = false
            }
        }
    }

    fun returnFromLogin(target: String? = null) {
        val previous = navController.previousBackStackEntry
        val previousRoute = previous?.destination?.route
        val fallback = when {
            previousRoute?.startsWith(Routes.ARTICLE_DETAIL) == true ->
                articleRoute(previous?.arguments?.getString("url").orEmpty())
            previousRoute == Routes.SEARCH -> Routes.SEARCH
            else -> Routes.HOME
        }
        // Chuyển màn hình và bỏ màn đăng nhập trong cùng một lần điều hướng.
        navController.navigate(target ?: fallback) {
            popUpTo(Routes.LOGIN) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun finishLogin() {
        if (navController.currentDestination?.route != Routes.LOGIN) return
        val destination = pendingDestination
        val bookmarkUrl = pendingBookmarkUrl
        session.onLoggedIn()
        clearPendingLogin()
        val target = when {
            bookmarkUrl != null -> {
                saveAfterLoginUrl = bookmarkUrl
                articleRoute(bookmarkUrl)
            }
            destination == FOR_YOU -> {
                openForYouAfterLogin = true
                Routes.HOME
            }
            else -> destination
        }
        returnFromLogin(target)
    }

    val currentEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(session.isLoggedIn, currentEntry) {
        if (session.isLoggedIn && currentEntry?.destination?.route == Routes.LOGIN) finishLogin()
    }
    LaunchedEffect(openNotifications) {
        if (openNotifications) {
            onNotificationHandled()
            openProtected(Routes.NOTIFICATIONS)
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = { finishLogin() },
                onCancelClick = {
                    clearPendingLogin()
                    returnFromLogin()
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onLoginClick = { navController.popBackStack() })
        }
        composable(Routes.HOME) {
            HomeScreen(
                isLoggedIn = session.isLoggedIn,
                onLoginClick = { requestLogin(prompt = false) },
                onForYouLoginClick = {
                    scope.launch {
                        session.awaitRestored()
                        if (session.isLoggedIn) openForYouAfterLogin = true
                        else requestLogin(FOR_YOU)
                    }
                },
                openForYouAfterLogin = openForYouAfterLogin,
                onForYouHandled = { openForYouAfterLogin = false },
                onSessionExpired = { fromPersonalized -> expireSession(if (fromPersonalized) FOR_YOU else null) },
                onLogoutClick = {
                    scope.launch {
                        removeCurrentDevice(tokenManager)
                        session.clearSession()
                        clearPendingLogin()
                        saveAfterLoginUrl = null
                        openForYouAfterLogin = false
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onPreferencesClick = { openProtected(Routes.PREFERENCES) },
                onSummaryClick = { openProtected(Routes.SUMMARY) },
                onSavedArticlesClick = { openProtected(Routes.SAVED_ARTICLES) },
                onSearchClick = { navController.navigate(Routes.SEARCH) { launchSingleTop = true } },
                onNotificationsClick = { openProtected(Routes.NOTIFICATIONS) },
                onArticleClick = { openArticle(it) }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onArticleClick = { openArticle(it) }
            )
        }
        composable(Routes.SAVED_ARTICLES) {
            RequireSession(session, { expireSession(Routes.SAVED_ARTICLES) }) {
                SavedArticlesScreen(
                    onBackClick = { navController.popBackStack() },
                    onArticleClick = { openArticle(it) },
                    onLoginClick = { expireSession(Routes.SAVED_ARTICLES) }
                )
            }
        }
        composable(Routes.NOTIFICATIONS) {
            RequireSession(session, { expireSession(Routes.NOTIFICATIONS) }) {
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() },
                    onArticleClick = { openArticle(it) },
                    onLoginClick = { expireSession(Routes.NOTIFICATIONS) }
                )
            }
        }
        composable(Routes.PREFERENCES) {
            RequireSession(session, { expireSession(Routes.PREFERENCES) }) {
                PreferencesScreen(
                    onBackClick = { navController.popBackStack() },
                    onLoginClick = { expireSession(Routes.PREFERENCES) }
                )
            }
        }
        composable(Routes.SUMMARY) {
            RequireSession(session, { expireSession(Routes.SUMMARY) }) {
                SummaryScreen(
                    onBackClick = { navController.popBackStack() },
                    onArticleClick = { openArticle(it) },
                    onLoginClick = { expireSession(Routes.SUMMARY) }
                )
            }
        }
        composable(
            route = "${Routes.ARTICLE_DETAIL}?url={url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { entry ->
            val url = entry.arguments?.getString("url").orEmpty()
            ArticleDetailScreen(
                url = url,
                isLoggedIn = session.isLoggedIn,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { requestLogin(prompt = false) },
                onSessionExpired = { resumeSave ->
                    if (resumeSave) expireSession(articleRoute(url), bookmarkUrl = url)
                    else expireSession()
                },
                saveAfterLogin = saveAfterLoginUrl == url,
                onSaveHandled = { saveAfterLoginUrl = null }
            )
        }
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { clearPendingLogin() },
            title = { Text("Đăng nhập để tiếp tục") },
            text = { Text("Tính năng này cần tài khoản để lưu dữ liệu và cá nhân hóa tin cho bạn.") },
            confirmButton = {
                TextButton(onClick = {
                    showLoginPrompt = false
                    navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                }) { Text("Đăng nhập") }
            },
            dismissButton = { TextButton(onClick = { clearPendingLogin() }) { Text("Để sau") } }
        )
    }
}

@Composable
private fun RequireSession(
    session: SessionViewModel,
    onLoginRequired: () -> Unit,
    content: @Composable () -> Unit
) {
    if (session.isLoggedIn) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            CircularProgressIndicator()
            Text("Đang kiểm tra đăng nhập...")
        }
        LaunchedEffect(Unit) {
            session.awaitRestored()
            if (!session.isLoggedIn) onLoginRequired()
        }
    }
}

private suspend fun removeCurrentDevice(tokenManager: TokenManager) {
    try {
        val accessToken = withContext(Dispatchers.IO) {
            tokenManager.getToken()
        }

        if (accessToken.isNullOrBlank()) return

        val fcmToken = withContext(Dispatchers.IO) {
            Tasks.await(
                FirebaseMessaging.getInstance().token,
                10,
                TimeUnit.SECONDS
            )
        }

        val response = RetrofitClient.apiService.removeDeviceToken(
            authorization = "Bearer $accessToken",
            request = DeviceTokenRequest(fcm_token = fcmToken)
        )

        if (response.isSuccessful) {
            Log.d("FCM_DEVICE", "Đã gỡ liên kết thiết bị")
        } else {
            Log.w("FCM_DEVICE", "Gỡ thiết bị thất bại: ${response.code()}")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("FCM_DEVICE", "Không gỡ được thiết bị", e)
    }
}