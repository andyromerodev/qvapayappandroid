package com.example.qvapayappandroid.navigation

sealed class AppDestinations(val route: String) {
    object Login : AppDestinations("login")
    object Home : AppDestinations("home")
    object Profile : AppDestinations("profile")
    
    companion object {
        const val START_DESTINATION = "login"
    }
}