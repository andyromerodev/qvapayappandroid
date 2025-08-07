package com.example.qvapayappandroid.presentation.ui.login

data class WebViewLoginState(
    val isVisible: Boolean = false,
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        const val QVAPAY_LOGIN_URL = "https://qvapay.com/login"
        
        fun showLogin() = WebViewLoginState(
            isVisible = true,
            url = QVAPAY_LOGIN_URL,
            isLoading = false
        )
        
        fun hide() = WebViewLoginState(
            isVisible = false,
            url = "",
            isLoading = false,
            error = null
        )
        
        fun error(message: String) = WebViewLoginState(
            isVisible = true,
            url = QVAPAY_LOGIN_URL,
            isLoading = false,
            error = message
        )
    }
}