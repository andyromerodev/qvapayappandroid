package com.example.qvapayappandroid.presentation.ui.login

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect
    data class ShowSuccessMessage(val message: String) : LoginEffect
    data class ShowErrorMessage(val message: String) : LoginEffect
}