package com.example.newsrecommendation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.newsrecommendation.ui.screens.notifications.NotificationsScreen
import com.example.newsrecommendation.ui.screens.search.SearchScreen
import com.example.newsrecommendation.ui.screens.saved.SavedArticlesScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsrecommendation.ui.screens.auth.*
import com.example.newsrecommendation.ui.screens.home.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.newsrecommendation.storage.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.newsrecommendation.ui.screens.preferences.PreferencesScreen
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.example.newsrecommendation.api.RetrofitClient
import com.example.newsrecommendation.model.DeviceTokenRequest
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit
import com.example.newsrecommendation.ui.screens.summary.SummaryScreen
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.newsrecommendation.ui.screens.article.ArticleDetailScreen

@Composable
fun AppNavHost(openNotifications: Boolean = false, onNotificationHandled: () -> Unit = {}){
    val navController = rememberNavController()
    val context = LocalContext.current

    val tokenManager = remember {
        TokenManager(context.applicationContext)
    }

    val coroutineScope = rememberCoroutineScope()

    fun openArticle(url: String) {
        navController.navigate(
            "${Routes.ARTICLE_DETAIL}?url=${Uri.encode(url)}"
        ) {
            launchSingleTop = true
        }
    }

    val currentEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(openNotifications, currentEntry) {
        val route = currentEntry?.destination?.route
        if (openNotifications && route != null && route != Routes.LOGIN && route != Routes.REGISTER) {
            navController.navigate(Routes.NOTIFICATIONS) { launchSingleTop = true }
            onNotificationHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME){
                        popUpTo(Routes.LOGIN){
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onLoginClick ={
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.HOME){
            HomeScreen(
                onLogoutClick = {
                    coroutineScope.launch{
                        removeCurrentDevice(tokenManager)

                        withContext(Dispatchers.IO) {
                            tokenManager.clearToken()
                        }

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onPreferencesClick = {
                    navController.navigate(Routes.PREFERENCES)
                },
                onSummaryClick = {
                    navController.navigate(Routes.SUMMARY) {
                        launchSingleTop = true
                    }
                },
                onSavedArticlesClick = {
                    navController.navigate(Routes.SAVED_ARTICLES) { launchSingleTop = true }
                },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH) { launchSingleTop = true }
                },
                onNotificationsClick = {
                    navController.navigate(Routes.NOTIFICATIONS) { launchSingleTop = true }
                },
                onArticleClick = { url ->
                    openArticle(url)
                },
            )
        }
        composable(Routes.SAVED_ARTICLES) {
            SavedArticlesScreen(
                onBackClick = { navController.popBackStack() },
                onArticleClick = { url -> openArticle(url) },
                onLoginClick = {
                    coroutineScope.launch {
                        removeCurrentDevice(tokenManager)
                        withContext(Dispatchers.IO) { tokenManager.clearToken() }
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onArticleClick = { url -> openArticle(url) },
                onLoginClick = {
                    coroutineScope.launch {
                        removeCurrentDevice(tokenManager)
                        withContext(Dispatchers.IO) { tokenManager.clearToken() }
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                onArticleClick = { url -> openArticle(url) },
                onLoginClick = {
                    coroutineScope.launch {
                        removeCurrentDevice(tokenManager)
                        withContext(Dispatchers.IO) { tokenManager.clearToken() }
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Routes.PREFERENCES) {
            PreferencesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    coroutineScope.launch {
                        removeCurrentDevice(tokenManager)

                        withContext(Dispatchers.IO) {
                            tokenManager.clearToken()
                        }

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Routes.SUMMARY) {
            SummaryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    coroutineScope.launch {
                        removeCurrentDevice(tokenManager)

                        withContext(Dispatchers.IO) {
                            tokenManager.clearToken()
                        }

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onArticleClick = { url ->
                    openArticle(url)
                },
            )
        }
        composable(
            route = "${Routes.ARTICLE_DETAIL}?url={url}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments
                ?.getString("url")
                .orEmpty()

            ArticleDetailScreen(
                url = url,
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    coroutineScope.launch {
                        removeCurrentDevice(tokenManager)

                        withContext(Dispatchers.IO) {
                            tokenManager.clearToken()
                        }

                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
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