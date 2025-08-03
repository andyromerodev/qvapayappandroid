package com.example.qvapayappandroid.presentation.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qvapayappandroid.navigation.AppDestinations
import com.example.qvapayappandroid.presentation.ui.components.BottomNavigationBar
import com.example.qvapayappandroid.presentation.ui.home.HomeScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2PScreen
import com.example.qvapayappandroid.presentation.ui.settings.SettingsScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.Home.route) {
                HomeScreen(
                    onLogout = onLogout
                )
            }
            
            composable(AppDestinations.P2P.route) {
                P2PScreen()
            }
            
            composable(AppDestinations.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}