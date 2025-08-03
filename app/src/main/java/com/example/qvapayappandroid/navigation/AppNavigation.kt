package com.example.qvapayappandroid.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qvapayappandroid.presentation.ui.login.LoginScreen
import com.example.qvapayappandroid.presentation.ui.main.MainScreen
import com.example.qvapayappandroid.presentation.ui.main.MainViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val uiState by mainViewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> {
            // Loading screen mientras se verifica la sesión
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verificando sesión...")
                }
            }
        }
        
        uiState.isInitialized -> {
            val startDestination = if (uiState.isLoggedIn) {
                AppDestinations.Main.route
            } else {
                AppDestinations.Login.route
            }
            
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(AppDestinations.Login.route) {
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
                
                composable(AppDestinations.Main.route) {
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
    }
}