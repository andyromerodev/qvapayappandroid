package com.example.qvapayappandroid.domain.repository

import android.webkit.WebView

interface WebViewRepository {
    suspend fun applyToP2POffer(offerId: String, webView: WebView): Result<String>
}