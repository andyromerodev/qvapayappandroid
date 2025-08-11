package com.example.qvapayappandroid.presentation.ui.p2p

/**
 * Estado específico para P2P WebView
 */
data class P2PWebViewState(
    val isVisible: Boolean = false,
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        const val QVAPAY_P2P_BASE_URL = "https://qvapay.com/p2p"

        fun showP2POffer(offerId: String) = P2PWebViewState(
            isVisible = true,
            url = "$QVAPAY_P2P_BASE_URL/$offerId",
            isLoading = false
        )

        fun hide() = P2PWebViewState(
            isVisible = false,
            url = "",
            isLoading = false,
            error = null
        )

        fun error(message: String, url: String = "") = P2PWebViewState(
            isVisible = true,
            url = url,
            isLoading = false,
            error = message
        )
    }
}