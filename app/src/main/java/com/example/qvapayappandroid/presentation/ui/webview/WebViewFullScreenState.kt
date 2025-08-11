package com.example.qvapayappandroid.presentation.ui.webview

/**
 * Estado para el WebView de pantalla completa
 */
data class WebViewFullScreenState(
    val isVisible: Boolean = false,
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        const val QVAPAY_LOGIN_URL = "https://qvapay.com/login"

        fun showLogin() = WebViewFullScreenState(
            isVisible = true,
            url = QVAPAY_LOGIN_URL,
            isLoading = false
        )

        fun hide() = WebViewFullScreenState(
            isVisible = false,
            url = "",
            isLoading = false,
            error = null
        )

        fun error(message: String) = WebViewFullScreenState(
            isVisible = true,
            url = QVAPAY_LOGIN_URL,
            isLoading = false,
            error = message
        )
    }
}