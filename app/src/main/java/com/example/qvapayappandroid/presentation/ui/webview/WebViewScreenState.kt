package com.example.qvapayappandroid.presentation.ui.webview

/**
 * Estado para el WebView VISIBLE usado en LoginScreen
 * (El WebView invisible para aplicar ofertas no necesita estado UI)
 */
data class WebViewScreenState(
    val isVisible: Boolean = false,
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        const val QVAPAY_LOGIN_URL = "https://qvapay.com/login"

        fun showLogin() = WebViewScreenState(
            isVisible = true,
            url = QVAPAY_LOGIN_URL,
            isLoading = false
        )

        fun hide() = WebViewScreenState(
            isVisible = false,
            url = "",
            isLoading = false,
            error = null
        )

        fun error(message: String) = WebViewScreenState(
            isVisible = true,
            url = QVAPAY_LOGIN_URL,
            isLoading = false,
            error = message
        )
    }
}