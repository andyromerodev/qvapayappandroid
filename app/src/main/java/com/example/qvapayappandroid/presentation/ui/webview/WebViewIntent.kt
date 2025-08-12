package com.example.qvapayappandroid.presentation.ui.webview

/**
 * Intents del WebView que representan todas las acciones que el usuario puede realizar
 */
sealed interface WebViewIntent {
    /**
     * Mostrar el WebView con una URL específica
     */
    data class ShowWebView(val url: String = WebViewFullScreenState.QVAPAY_LOGIN_URL) : WebViewIntent
    
    /**
     * Ocultar el WebView
     */
    data object HideWebView : WebViewIntent
    
    /**
     * Recargar la página actual
     */
    data object Reload : WebViewIntent
    
    /**
     * Limpiar error y reintentar
     */
    data object ClearError : WebViewIntent
    
    /**
     * Marcar que se está navegando a una nueva URL
     */
    data object MarkNavigatingToNewUrl : WebViewIntent
    
    /**
     * Indicar que el WebView no está disponible
     */
    data object OnWebViewUnavailable : WebViewIntent
    
    /**
     * Actualizar estado de carga
     */
    data class SetLoading(val isLoading: Boolean) : WebViewIntent
    
    /**
     * Establecer un error
     */
    data class SetError(val error: String?) : WebViewIntent
}