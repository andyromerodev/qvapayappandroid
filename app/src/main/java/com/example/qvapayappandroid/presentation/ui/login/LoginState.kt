package com.example.qvapayappandroid.presentation.ui.login

import com.example.qvapayappandroid.data.model.LoginResponse

data class LoginState(
    val email: String = "",
    val password: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginResponse: LoginResponse? = null
) {
    val isLoginEnabled: Boolean
        get() = !isLoading && email.isNotBlank() && password.isNotBlank()
    
    val hasError: Boolean
        get() = errorMessage != null
    
    val isLoginSuccessful: Boolean
        get() = loginResponse != null
    
    val userDisplayName: String
        get() = loginResponse?.let { "${it.me.name} ${it.me.lastname}" } ?: ""
    
    val userBalance: String
        get() = (loginResponse?.me?.balance ?: "0.00").toString()
}