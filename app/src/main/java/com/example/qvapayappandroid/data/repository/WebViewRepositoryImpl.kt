package com.example.qvapayappandroid.data.repository

import android.webkit.WebView
import com.example.qvapayappandroid.data.datasource.WebViewLoginDataSource
import com.example.qvapayappandroid.domain.repository.WebViewRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WebViewRepositoryImpl(
    private val webViewDataSource: WebViewLoginDataSource
) : WebViewRepository {
    
    override suspend fun applyToP2POffer(offerId: String, webView: WebView): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            // Wire the WebView into the data source
            webViewDataSource.setWebView(webView)
            
            // Run the offer application flow
            webViewDataSource.applyToOffer(
                offerId = offerId,
                onSuccess = { result ->
                    continuation.resume(Result.success(result))
                },
                onError = { error ->
                    continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }
}
