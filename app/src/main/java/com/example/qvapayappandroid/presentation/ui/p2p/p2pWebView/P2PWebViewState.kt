package com.example.qvapayappandroid.presentation.ui.p2p.p2pWebView

/**
 * Estado específico para P2P WebView
 */
data class P2PWebViewState(
    val isVisible: Boolean = false,
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val offerId: String? = null
) {
    companion object {
        const val QVAPAY_P2P_BASE_URL = "https://qvapay.com/p2p"
    }
}