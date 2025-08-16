package com.example.qvapayappandroid.presentation.ui.templates

import com.example.qvapayappandroid.domain.model.OfferTemplate

sealed interface OfferTemplatesIntent {
    object LoadTemplates : OfferTemplatesIntent
    object RefreshTemplates : OfferTemplatesIntent
    data class SearchTemplates(val query: String) : OfferTemplatesIntent
    data class FilterByType(val type: String?) : OfferTemplatesIntent
    data class DeleteTemplate(val template: OfferTemplate) : OfferTemplatesIntent
    data class EditTemplate(val template: OfferTemplate) : OfferTemplatesIntent
    data class UseTemplate(val template: OfferTemplate) : OfferTemplatesIntent
    data class DuplicateTemplate(val template: OfferTemplate) : OfferTemplatesIntent
    object CreateNewTemplate : OfferTemplatesIntent
    object ClearSearch : OfferTemplatesIntent
    object DismissError : OfferTemplatesIntent
}