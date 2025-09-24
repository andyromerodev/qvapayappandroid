package com.example.qvapayappandroid.presentation.ui.templates

import com.example.qvapayappandroid.domain.model.OfferTemplate

data class OfferTemplatesState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val templates: List<OfferTemplate> = emptyList(),
    val filteredTemplates: List<OfferTemplate> = emptyList(),
    val searchQuery: String = "",
    val selectedType: String? = null, // null = all, otherwise "sell" or "buy"
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val isSearching: Boolean = false,
    val creatingOfferFromTemplateId: Long? = null // Template ID currently used to spin up an offer
) {
    val displayTemplates: List<OfferTemplate>
        get() = if (searchQuery.isNotEmpty() || selectedType != null) filteredTemplates else templates
    
    val showEmptyState: Boolean
        get() = !isLoading && !isRefreshing && displayTemplates.isEmpty() && errorMessage == null
    
    val typeFilters = listOf(
        TypeFilter("Todas", null),
        TypeFilter("Vender", "sell"),
        TypeFilter("Comprar", "buy")
    )
}

data class TypeFilter(
    val name: String,
    val value: String?
)
