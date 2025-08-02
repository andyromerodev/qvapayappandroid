package com.example.qvapayappandroid.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qvapayappandroid.presentation.ui.HomeScreen
import com.example.qvapayappandroid.presentation.ui.LoginScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.START_DESTINATION
    ) {
        composable(AppDestinations.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.Home.route) {
                        popUpTo(AppDestinations.Login.route) { 
                            inclusive = true 
                        }
                    }
                }
            )
        }
        
        composable(AppDestinations.Home.route) {
            HomeScreen()
        }
    }
}