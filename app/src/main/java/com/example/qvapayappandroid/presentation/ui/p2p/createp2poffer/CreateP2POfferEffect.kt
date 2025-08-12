package com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer

/**
 * Effects específicos para la creación de ofertas P2P que representan efectos secundarios 
 * o eventos únicos que deben ser manejados por la UI durante el proceso de creación
 */
sealed interface CreateP2POfferEffect {
    /**
     * Navegar hacia atrás (cerrar pantalla de creación)
     */
    data object NavigateBack : CreateP2POfferEffect
    
    /**
     * Mostrar mensaje de error al usuario
     */
    data class ShowError(val message: String) : CreateP2POfferEffect
    
    /**
     * Mostrar mensaje de éxito al usuario
     */
    data class ShowSuccess(val message: String) : CreateP2POfferEffect
    
    /**
     * Oferta creada exitosamente con detalles
     */
    data class OfferCreatedSuccessfully(val uuid: String, val message: String) : CreateP2POfferEffect
    
    /**
     * Error de validación específico
     */
    data class ValidationError(val field: String, val message: String) : CreateP2POfferEffect
    
    /**
     * Mostrar loading indicator
     */
    data object ShowLoading : CreateP2POfferEffect
    
    /**
     * Ocultar loading indicator  
     */
    data object HideLoading : CreateP2POfferEffect
    
    /**
     * Limpiar formulario después de creación exitosa
     */
    data object ClearForm : CreateP2POfferEffect
}