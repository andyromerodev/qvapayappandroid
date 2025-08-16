package com.example.qvapayappandroid.presentation.ui.templates

sealed interface SaveOfferTemplateEffect {
    object NavigateBack : SaveOfferTemplateEffect
    data class ShowSuccess(val message: String) : SaveOfferTemplateEffect
    data class ShowError(val message: String) : SaveOfferTemplateEffect
    object TemplateSavedSuccessfully : SaveOfferTemplateEffect
}