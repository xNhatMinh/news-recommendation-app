package com.example.newsrecommendation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsrecommendation.ui.screens.auth.LoginScreen
import com.example.newsrecommendation.ui.screens.auth.RegisterScreen

@Composable
fun AppNavHost(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
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
    }
}