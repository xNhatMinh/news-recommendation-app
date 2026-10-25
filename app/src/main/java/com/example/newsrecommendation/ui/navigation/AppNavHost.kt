package com.example.newsrecommendation.ui.navigation

import androidx.compose.runtime.Composable
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
import com.example.newsrecommendation.ui.screens.history.HistoryScreen


@Composable
fun AppNavHost(){
    val navController = rememberNavController()
    val context = LocalContext.current

    val tokenManager = remember {
        TokenManager(context.applicationContext)
    }

    val coroutineScope = rememberCoroutineScope()

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
                onHistoryClick = {
                    navController.navigate(Routes.HISTORY) {
                        launchSingleTop = true
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
                }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
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