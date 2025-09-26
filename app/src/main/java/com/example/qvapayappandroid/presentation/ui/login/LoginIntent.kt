package com.example.qvapayappandroid.presentation.ui.login

sealed interface LoginIntent {
    data class UpdateEmail(val email: String) : LoginIntent
    data class UpdatePassword(val password: String) : LoginIntent
    data class UpdateCode(val code: String) : LoginIntent
    data object Login : LoginIntent
    data object ClearError : LoginIntent
}