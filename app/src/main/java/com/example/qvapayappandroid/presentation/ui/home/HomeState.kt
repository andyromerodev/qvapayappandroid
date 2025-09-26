package com.example.qvapayappandroid.presentation.ui.home

import com.example.qvapayappandroid.data.model.P2POffer

data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val myOffers: List<P2POffer> = emptyList(),
    val filteredOffers: List<P2POffer> = emptyList(),
    val selectedStatusFilters: Set<String> = emptySet(),
    val hasNextPage: Boolean = true,
    val currentPage: Int = 1,
    val offersError: String? = null,
    val isCancellingOffer: String? = null,
    val cancelOfferError: String? = null
) {
    val shouldShowLoading: Boolean
        get() = isLoading && myOffers.isEmpty()
    
    val shouldShowContent: Boolean
        get() = myOffers.isNotEmpty() && offersError == null
    
    val shouldShowError: Boolean
        get() = offersError != null && myOffers.isEmpty()
    
    val shouldShowEmpty: Boolean
        get() = myOffers.isEmpty() && !isLoading && offersError == null
    
    val canRefresh: Boolean
        get() = !isLoading && !isRefreshing
    
    val canLoadMore: Boolean
        get() = hasNextPage && !isLoadingMore && !isLoading && offersError == null
    
    val displayOffers: List<P2POffer>
        get() = if (selectedStatusFilters.isEmpty()) myOffers else filteredOffers
    
    val isCancellingAnyOffer: Boolean
        get() = isCancellingOffer != null
}