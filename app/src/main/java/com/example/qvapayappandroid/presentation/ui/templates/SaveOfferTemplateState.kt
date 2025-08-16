package com.example.qvapayappandroid.presentation.ui.templates

import com.example.qvapayappandroid.data.model.P2PDetail
import com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer.AvailableCoin
import com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer.CreateP2POfferState

data class SaveOfferTemplateState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val templateId: Long? = null,
    val name: String = "",
    val description: String = "",
    val type: String = "sell",
    val selectedCoin: AvailableCoin = CreateP2POfferState.availableCoins.first { it.id == 108 },
    val availableCoins: List<AvailableCoin> = CreateP2POfferState.availableCoins,
    val amount: String = "",
    val receive: String = "",
    val details: List<P2PDetail> = listOf(
        P2PDetail("Nombre y Apellidos", ""),
        P2PDetail("Nro de tarjeta", ""),
        P2PDetail("Nro de celular", "")
    ),
    val onlyKyc: Boolean = true,
    val private: Boolean = false,
    val promoteOffer: Boolean = false,
    val onlyVip: Boolean = true,
    val message: String = "",
    val webhook: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank()
    
    val coinId: String
        get() = selectedCoin.id.toString()
}