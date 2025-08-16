package com.example.qvapayappandroid.presentation.ui.templates

import com.example.qvapayappandroid.domain.model.OfferTemplate

sealed interface OfferTemplatesEffect {
    data class NavigateToEditTemplate(val templateId: Long) : OfferTemplatesEffect
    object NavigateToCreateTemplate : OfferTemplatesEffect
    data class NavigateToCreateOffer(val template: OfferTemplate) : OfferTemplatesEffect
    data class ShowDeleteConfirmation(val template: OfferTemplate) : OfferTemplatesEffect
    data class ShowSuccessMessage(val message: String) : OfferTemplatesEffect
    data class ShowErrorMessage(val message: String) : OfferTemplatesEffect
}