package com.example.qvapayappandroid.presentation.ui.p2p.p2pWebView

/**
 * Intents específicos del P2P WebView que representan todas las acciones 
 * que el usuario puede realizar en el contexto de ofertas P2P
 */
sealed interface P2PWebViewIntent {
    /**
     * Mostrar el WebView con una oferta P2P específica
     */
    data class ShowP2POfferWebView(val offerId: String) : P2PWebViewIntent
    
    /**
     * Ocultar el WebView
     */
    data object HideWebView : P2PWebViewIntent
    
    /**
     * Recargar la página actual de la oferta P2P
     */
    data object Reload : P2PWebViewIntent
    
    /**
     * Limpiar error y reintentar
     */
    data object ClearError : P2PWebViewIntent
    
    /**
     * Indicar que el WebView no está disponible
     */
    data object OnWebViewUnavailable : P2PWebViewIntent
    
    /**
     * Actualizar estado de carga
     */
    data class SetLoading(val isLoading: Boolean) : P2PWebViewIntent
    
    /**
     * Establecer un error específico
     */
    data class SetError(val error: String?) : P2PWebViewIntent
    
    /**
     * Marcar que se está navegando a una nueva URL
     */
    data object MarkNavigatingToNewUrl : P2PWebViewIntent
}