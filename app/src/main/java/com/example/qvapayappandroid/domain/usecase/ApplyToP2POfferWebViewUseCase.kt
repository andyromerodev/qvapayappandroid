package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import android.webkit.WebView
import com.example.qvapayappandroid.domain.repository.WebViewRepository
import kotlinx.coroutines.withTimeout

class ApplyToP2POfferWebViewUseCase(
    private val webViewRepository: WebViewRepository
) {
    suspend operator fun invoke(offerId: String, webView: WebView): Result<String> {
        return try {
            Log.d("ApplyToP2POfferWebViewUseCase", "Aplicando a oferta P2P: $offerId")
            
            // Timeout de 30 segundos para la operación completa
            withTimeout(30_000) {
                webViewRepository.applyToP2POffer(offerId, webView)
            }
        } catch (e: Exception) {
            Log.e("ApplyToP2POfferWebViewUseCase", "Error aplicando a oferta P2P: $offerId", e)
            Result.failure(e)
        }
    }
}