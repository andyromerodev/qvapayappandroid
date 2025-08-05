package com.example.qvapayappandroid.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qvapayappandroid.presentation.ui.login.LoginScreen
import com.example.qvapayappandroid.presentation.ui.main.MainScreen
import com.example.qvapayappandroid.presentation.ui.splash.SplashScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.START_DESTINATION,
        enterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 400)
            ) + slideInHorizontally(
                initialOffsetX = { it / 4 },
                animationSpec = tween(durationMillis = 400)
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 400)
            ) + slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(durationMillis = 400)
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 400)
            ) + slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(durationMillis = 400)
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 400)
            ) + slideOutHorizontally(
                targetOffsetX = { it / 4 },
                animationSpec = tween(durationMillis = 400)
            )
        }
    ) {
        composable(
            route = AppDestinations.Splash.route,
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 300)) + 
                scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) {
            SplashScreen(
                onNavigationReady = { destination ->
                    navController.navigate(destination) {
                        popUpTo(AppDestinations.Splash.route) { 
                            inclusive = true 
                        }
                    }
                }
            )
        }
        
        composable(
            route = AppDestinations.Login.route,
            enterTransition = {
                when (initialState.destination.route) {
                    AppDestinations.Splash.route -> {
                        fadeIn(animationSpec = tween(durationMillis = 500)) +
                        scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(durationMillis = 500)
                        )
                    }
                    else -> {
                        fadeIn(animationSpec = tween(durationMillis = 400)) +
                        slideInHorizontally(
                            initialOffsetX = { it / 4 },
                            animationSpec = tween(durationMillis = 400)
                        )
                    }
                }
            }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.Main.route) {
                        popUpTo(AppDestinations.Login.route) { 
                            inclusive = true 
                        }
                    }
                }
            )
        }
        
        composable(
            route = AppDestinations.Main.route,
            enterTransition = {
                when (initialState.destination.route) {
                    AppDestinations.Splash.route -> {
                        fadeIn(animationSpec = tween(durationMillis = 500)) +
                        scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(durationMillis = 500)
                        )
                    }
                    else -> {
                        fadeIn(animationSpec = tween(durationMillis = 400)) +
                        slideInHorizontally(
                            initialOffsetX = { it / 4 },
                            animationSpec = tween(durationMillis = 400)
                        )
                    }
                }
            }
        ) {
            MainScreen(
                onLogout = {
                    navController.navigate(AppDestinations.Login.route) {
                        popUpTo(AppDestinations.Main.route) { 
                            inclusive = true 
                        }
                    }
                }
            )
        }
    }
}