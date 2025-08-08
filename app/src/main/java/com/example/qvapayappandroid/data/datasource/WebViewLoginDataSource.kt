package com.example.qvapayappandroid.data.datasource

import android.webkit.WebView

interface WebViewLoginDataSource {
    fun setOnWebViewLoginCompleted(callback: () -> Unit)
    fun setWebView(webView: WebView)
    fun applyToOffer(offerId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun isWebViewReady(): Boolean
}