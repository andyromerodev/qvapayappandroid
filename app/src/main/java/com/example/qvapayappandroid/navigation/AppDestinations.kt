package com.example.qvapayappandroid.navigation

sealed class AppDestinations(val route: String) {
    object Login : AppDestinations("login")
    object Main : AppDestinations("main")
    object Home : AppDestinations("home")
    object P2P : AppDestinations("p2p")
    object Settings : AppDestinations("settings")
    
    companion object {
        const val START_DESTINATION = "login"
    }
}