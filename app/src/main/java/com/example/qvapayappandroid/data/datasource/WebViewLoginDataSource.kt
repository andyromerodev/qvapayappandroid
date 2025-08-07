package com.example.qvapayappandroid.data.datasource

import android.webkit.WebView

interface WebViewLoginDataSource {
    fun setOnWebViewLoginCompleted(callback: () -> Unit)
    fun setWebView(webView: WebView)
}