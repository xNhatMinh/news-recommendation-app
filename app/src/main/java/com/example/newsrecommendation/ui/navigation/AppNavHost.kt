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
            )
        }
        composable(Routes.PREFERENCES) {
            PreferencesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    coroutineScope.launch {
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