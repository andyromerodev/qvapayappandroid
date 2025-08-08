package com.example.qvapayappandroid.presentation.ui.webview

import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.usecase.ApplyToP2POfferWebViewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WebViewViewModel(
    private val applyToP2POfferWebViewUseCase: ApplyToP2POfferWebViewUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(WebViewScreenState())
    val state: StateFlow<WebViewScreenState> = _state.asStateFlow()
    
    private var webViewInstance: WebView? = null
    
    fun showWebView(url: String = WebViewScreenState.QVAPAY_LOGIN_URL) {
        Log.d("WebViewViewModel", "Mostrando WebView con URL: $url")
        _state.value = _state.value.copy(
            isVisible = true,
            url = url,
            isLoading = true,
            error = null
        )
    }
    
    fun hideWebView() {
        Log.d("WebViewViewModel", "Ocultando WebView")
        _state.value = WebViewScreenState.hide()
    }
    
    fun setLoading(isLoading: Boolean) {
        _state.value = _state.value.copy(isLoading = isLoading)
    }
    
    fun setError(error: String?) {
        Log.e("WebViewViewModel", "Error en WebView: $error")
        _state.value = _state.value.copy(
            error = error,
            isLoading = false
        )
    }
    
    fun onWebViewReady(webView: WebView) {
        Log.d("WebViewViewModel", "WebView listo para usar")
        webViewInstance = webView
        setLoading(false)
    }
    
    fun onWebViewUnavailable() {
        Log.w("WebViewViewModel", "WebView no disponible")
        setError("WebView no está disponible en este dispositivo")
    }
    
    fun loadUrl(url: String) {
        webViewInstance?.let { webView ->
            Log.d("WebViewViewModel", "Cargando URL: $url")
            _state.value = _state.value.copy(url = url, isLoading = true, error = null)
            webView.loadUrl(url)
        } ?: run {
            Log.e("WebViewViewModel", "Intentando cargar URL sin WebView inicializado")
            setError("WebView no está inicializado")
        }
    }
    
    fun reload() {
        webViewInstance?.let { webView ->
            Log.d("WebViewViewModel", "Recargando WebView")
            setLoading(true)
            webView.reload()
        }
    }
    
    fun goBack(): Boolean {
        return webViewInstance?.let { webView ->
            if (webView.canGoBack()) {
                Log.d("WebViewViewModel", "Navegando hacia atrás")
                webView.goBack()
                true
            } else {
                false
            }
        } ?: false
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    fun applyToP2POffer(offerId: String) {
        Log.d("WebViewViewModel", "🎯 applyToP2POffer llamado con offerId: $offerId")
        
        val currentWebView = webViewInstance
        if (currentWebView == null) {
            Log.e("WebViewViewModel", "❌ WebView no está disponible")
            setError("WebView no está disponible")
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d("WebViewViewModel", "🚀 Iniciando aplicación a oferta P2P: $offerId")
                setLoading(true)
                
                val result = applyToP2POfferWebViewUseCase(offerId, currentWebView)
                
                result.fold(
                    onSuccess = { message ->
                        Log.d("WebViewViewModel", "✅ Oferta aplicada exitosamente: $message")
                        setLoading(false)
                        // Aquí podrías actualizar el estado para mostrar éxito
                    },
                    onFailure = { error ->
                        Log.e("WebViewViewModel", "❌ Error aplicando a oferta: ${error.message}")
                        setError("Error aplicando a oferta: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("WebViewViewModel", "💥 Error inesperado aplicando a oferta", e)
                setError("Error inesperado: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d("WebViewViewModel", "ViewModel limpiado")
        webViewInstance = null
    }
}