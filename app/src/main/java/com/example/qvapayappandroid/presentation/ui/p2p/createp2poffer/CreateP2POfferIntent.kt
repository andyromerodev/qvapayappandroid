package com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer

/**
 * Intents used throughout P2P offer creation, covering every user action.
 */
sealed interface CreateP2POfferIntent {
    /** Change the offer type (buy/sell). */
    data class ChangeType(val type: String) : CreateP2POfferIntent
    
    /** Change the selected coin ID. */
    data class ChangeCoinId(val coinId: String) : CreateP2POfferIntent
    
    /** Select a specific coin. */
    data class SelectCoin(val coin: AvailableCoin) : CreateP2POfferIntent
    
    /** Update the offer amount. */
    data class ChangeAmount(val amount: String) : CreateP2POfferIntent
    
    /** Update the amount to receive. */
    data class ChangeReceive(val receive: String) : CreateP2POfferIntent
    
    /** Change the value of a specific detail entry. */
    data class ChangeDetail(val index: Int, val value: String) : CreateP2POfferIntent
    
    /** Toggle whether the offer is KYC-only. */
    data class ChangeOnlyKyc(val onlyKyc: Boolean) : CreateP2POfferIntent
    
    /** Toggle whether the offer is private. */
    data class ChangePrivate(val private: Boolean) : CreateP2POfferIntent
    
    /** Toggle whether the offer should be promoted. */
    data class ChangePromoteOffer(val promoteOffer: Boolean) : CreateP2POfferIntent
    
    /** Toggle whether the offer is VIP-only. */
    data class ChangeOnlyVip(val onlyVip: Boolean) : CreateP2POfferIntent
    
    /** Update the message associated with the offer. */
    data class ChangeMessage(val message: String) : CreateP2POfferIntent
    
    /** Update the webhook URL. */
    data class ChangeWebhook(val webhook: String) : CreateP2POfferIntent
    
    /** Start the offer creation flow. */
    data object CreateOffer : CreateP2POfferIntent
    
    /** Navigate back. */
    data object NavigateBack : CreateP2POfferIntent
    
    /** Dismiss the current error message. */
    data object DismissError : CreateP2POfferIntent
    
    /** Dismiss the current success message. */
    data object DismissSuccessMessage : CreateP2POfferIntent
    
    /** Load data from a saved template. */
    data class LoadFromTemplate(val template: com.example.qvapayappandroid.domain.model.OfferTemplate) : CreateP2POfferIntent
    
    /** Capture the current state to be saved as a template. */
    data object RequestCurrentStateForTemplate : CreateP2POfferIntent
}
