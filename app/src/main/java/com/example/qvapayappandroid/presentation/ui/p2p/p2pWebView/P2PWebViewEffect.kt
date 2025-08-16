package com.example.qvapayappandroid.presentation.ui.p2p.p2pWebView

/**
 * Effects específicos del P2P WebView que representan efectos secundarios 
 * o eventos únicos que deben ser manejados por la UI en el contexto de ofertas P2P
 */
sealed interface P2PWebViewEffect {
    /**
     * Navegación a oferta P2P completada exitosamente
     */
    data object P2POfferNavigationCompleted : P2PWebViewEffect
    
    /**
     * Error de navegación ocurrido en oferta P2P
     */
    data class P2POfferNavigationError(val error: String) : P2PWebViewEffect
    
    /**
     * P2P WebView se ha cargado completamente
     */
    data object P2PWebViewLoaded : P2PWebViewEffect
    
    /**
     * Se requiere mostrar un mensaje relacionado con la oferta P2P
     */
    data class ShowP2PMessage(val message: String) : P2PWebViewEffect
    
    /**
     * Se debe cerrar el P2P WebView
     */
    data object CloseP2PWebView : P2PWebViewEffect
    
    /**
     * Página de oferta P2P comenzó a cargar
     */
    data class P2POfferPageStarted(val url: String, val offerId: String) : P2PWebViewEffect
    
    /**
     * Error HTTP recibido en oferta P2P
     */
    data class P2POfferHttpError(val code: Int, val url: String, val offerId: String) : P2PWebViewEffect
    
    /**
     * WebView no está disponible para mostrar ofertas P2P
     */
    data object P2PWebViewUnavailable : P2PWebViewEffect
    
    /**
     * Oferta P2P procesada correctamente (para futuras integraciones)
     */
    data class P2POfferProcessed(val offerId: String, val result: String) : P2PWebViewEffect
    
    /**
     * Usuario interactuó con la oferta P2P (click, scroll, etc.)
     */
    data class P2POfferInteraction(val offerId: String, val interaction: String) : P2PWebViewEffect
}