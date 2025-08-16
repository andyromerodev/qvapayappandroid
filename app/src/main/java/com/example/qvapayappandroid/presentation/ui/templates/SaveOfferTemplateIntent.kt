package com.example.qvapayappandroid.presentation.ui.templates

import com.example.qvapayappandroid.data.model.P2PDetail
import com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer.AvailableCoin

sealed interface SaveOfferTemplateIntent {
    object LoadTemplate : SaveOfferTemplateIntent
    data class ChangeName(val name: String) : SaveOfferTemplateIntent
    data class ChangeDescription(val description: String) : SaveOfferTemplateIntent
    data class ChangeType(val type: String) : SaveOfferTemplateIntent
    data class SelectCoin(val coin: AvailableCoin) : SaveOfferTemplateIntent
    data class ChangeAmount(val amount: String) : SaveOfferTemplateIntent
    data class ChangeReceive(val receive: String) : SaveOfferTemplateIntent
    data class ChangeDetail(val index: Int, val value: String) : SaveOfferTemplateIntent
    data class ChangeOnlyKyc(val onlyKyc: Boolean) : SaveOfferTemplateIntent
    data class ChangePrivate(val private: Boolean) : SaveOfferTemplateIntent
    data class ChangePromoteOffer(val promoteOffer: Boolean) : SaveOfferTemplateIntent
    data class ChangeOnlyVip(val onlyVip: Boolean) : SaveOfferTemplateIntent
    data class ChangeMessage(val message: String) : SaveOfferTemplateIntent
    data class ChangeWebhook(val webhook: String) : SaveOfferTemplateIntent
    object SaveTemplate : SaveOfferTemplateIntent
    object NavigateBack : SaveOfferTemplateIntent
    object DismissError : SaveOfferTemplateIntent
    object DismissSuccessMessage : SaveOfferTemplateIntent
}