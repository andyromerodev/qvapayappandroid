package com.example.qvapayappandroid.data.datasource

import android.webkit.WebView

class WebViewLoginDataSourceImpl : WebViewLoginDataSource {

    private var webView: WebView? = null
    private var onWebViewLoginCompleted: (() -> Unit)? = null
    
    override fun setOnWebViewLoginCompleted(callback: () -> Unit) {
        onWebViewLoginCompleted = callback
    }
    
    override fun setWebView(webView: WebView) {
        this.webView = webView
    }
}