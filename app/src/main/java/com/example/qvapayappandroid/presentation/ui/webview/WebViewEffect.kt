package com.example.qvapayappandroid.presentation.ui.webview

/**
 * Effects del WebView que representan efectos secundarios o eventos únicos
 * que deben ser manejados por la UI
 */
sealed interface WebViewEffect {
    /**
     * Navegación completada exitosamente
     */
    data object NavigationCompleted : WebViewEffect
    
    /**
     * Error de navegación ocurrido
     */
    data class NavigationError(val error: String) : WebViewEffect
    
    /**
     * WebView se ha cargado completamente
     */
    data object WebViewLoaded : WebViewEffect
    
    /**
     * Se requiere mostrar un mensaje al usuario
     */
    data class ShowMessage(val message: String) : WebViewEffect
    
    /**
     * Se debe cerrar el WebView
     */
    data object CloseWebView : WebViewEffect
    
    /**
     * Página comenzó a cargar
     */
    data class PageStarted(val url: String) : WebViewEffect
    
    /**
     * Error HTTP recibido
     */
    data class HttpError(val code: Int, val url: String) : WebViewEffect
    
    /**
     * WebView no está disponible en el dispositivo
     */
    data object WebViewUnavailable : WebViewEffect
}