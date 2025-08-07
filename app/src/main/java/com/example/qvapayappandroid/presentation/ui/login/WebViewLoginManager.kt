package com.example.qvapayappandroid.presentation.ui.login

import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.qvapayappandroid.data.datasource.WebViewLoginDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WebViewLoginManager(
    private val webViewDataSource: WebViewLoginDataSource
) {
    private val _state = MutableStateFlow(WebViewLoginState())
    val state: StateFlow<WebViewLoginState> = _state.asStateFlow()
    
    private var webView: WebView? = null
    private var onLoginCompleted: (() -> Unit)? = null
    
    fun initialize(webView: WebView?, onLoginCompleted: () -> Unit) {
        this.webView = webView
        this.onLoginCompleted = onLoginCompleted
        
        webView?.let { wv ->
            configureWebView(wv)
            configureDataSource()
        }
    }
    
    fun showWebViewLogin() {
        Log.d("WebViewLoginManager", "Mostrando WebView login")
        _state.value = WebViewLoginState.showLogin()
        // La URL se cargará cuando el WebView esté configurado
    }
    
    
    
    fun hideWebView() {
        _state.value = WebViewLoginState.hide()
    }
    
    fun onWebViewLoginCompleted() {
        hideWebView()
        onLoginCompleted?.invoke()
    }
    
    private fun configureWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }
        
        // Configurar cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        
        // WebViewClient para manejar navegación y estado de carga
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false // Permitir navegación normal
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("WebViewLoginManager", "Página iniciada: $url")
                _state.value = _state.value.copy(isLoading = true)
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("WebViewLoginManager", "Página terminada: $url")
                _state.value = _state.value.copy(isLoading = false)
                
                // Detectar si llegó al dashboard (login exitoso)
                if (url?.contains("/dashboard") == true || url?.contains("/home") == true) {
                    Log.d("WebViewLoginManager", "Login detectado exitoso")
                    onWebViewLoginCompleted()
                }
            }
        }
    }
    
    private fun configureDataSource() {
        webView?.let { wv ->
            webViewDataSource.setWebView(wv)
            webViewDataSource.setOnWebViewLoginCompleted {
                onWebViewLoginCompleted()
            }
        }
    }
}