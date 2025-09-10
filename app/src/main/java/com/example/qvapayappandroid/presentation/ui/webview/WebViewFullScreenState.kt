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
        const val QVAPAY_DASHBOARD_URL = "https://qvapay.com/dashboard"
    }
}