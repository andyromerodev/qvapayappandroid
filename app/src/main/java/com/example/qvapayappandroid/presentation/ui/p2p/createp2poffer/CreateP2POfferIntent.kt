package com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer

/**
 * Intents específicos para la creación de ofertas P2P que representan todas las acciones 
 * que el usuario puede realizar durante el proceso de creación de ofertas
 */
sealed interface CreateP2POfferIntent {
    /**
     * Cambiar el tipo de oferta (buy/sell)
     */
    data class ChangeType(val type: String) : CreateP2POfferIntent
    
    /**
     * Cambiar el ID de la moneda
     */
    data class ChangeCoinId(val coinId: String) : CreateP2POfferIntent
    
    /**
     * Seleccionar una moneda específica
     */
    data class SelectCoin(val coin: AvailableCoin) : CreateP2POfferIntent
    
    /**
     * Cambiar el monto de la oferta
     */
    data class ChangeAmount(val amount: String) : CreateP2POfferIntent
    
    /**
     * Cambiar el monto a recibir
     */
    data class ChangeReceive(val receive: String) : CreateP2POfferIntent
    
    /**
     * Cambiar el valor de un detalle específico
     */
    data class ChangeDetail(val index: Int, val value: String) : CreateP2POfferIntent
    
    /**
     * Cambiar si la oferta es solo para usuarios KYC
     */
    data class ChangeOnlyKyc(val onlyKyc: Boolean) : CreateP2POfferIntent
    
    /**
     * Cambiar si la oferta es privada
     */
    data class ChangePrivate(val private: Boolean) : CreateP2POfferIntent
    
    /**
     * Cambiar si se quiere promover la oferta
     */
    data class ChangePromoteOffer(val promoteOffer: Boolean) : CreateP2POfferIntent
    
    /**
     * Cambiar si la oferta es solo para usuarios VIP
     */
    data class ChangeOnlyVip(val onlyVip: Boolean) : CreateP2POfferIntent
    
    /**
     * Cambiar el mensaje de la oferta
     */
    data class ChangeMessage(val message: String) : CreateP2POfferIntent
    
    /**
     * Cambiar la URL del webhook
     */
    data class ChangeWebhook(val webhook: String) : CreateP2POfferIntent
    
    /**
     * Iniciar el proceso de creación de la oferta
     */
    data object CreateOffer : CreateP2POfferIntent
    
    /**
     * Navegar hacia atrás
     */
    data object NavigateBack : CreateP2POfferIntent
    
    /**
     * Descartar mensaje de error
     */
    data object DismissError : CreateP2POfferIntent
    
    /**
     * Descartar mensaje de éxito
     */
    data object DismissSuccessMessage : CreateP2POfferIntent
    
    /**
     * Cargar datos desde una plantilla
     */
    data class LoadFromTemplate(val template: com.example.qvapayappandroid.domain.model.OfferTemplate) : CreateP2POfferIntent
    
    /**
     * Obtener el estado actual para guardar como plantilla
     */
    data object RequestCurrentStateForTemplate : CreateP2POfferIntent
}