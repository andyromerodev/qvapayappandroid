package com.example.qvapayappandroid.presentation.ui.home

import com.example.qvapayappandroid.data.model.P2POffer

sealed interface HomeIntent {
    data object LoadOffers : HomeIntent
    data object RefreshOffers : HomeIntent
    data object LoadMoreOffers : HomeIntent
    data object ClearOffersError : HomeIntent
    data object ClearCancelOfferError : HomeIntent
    data class ToggleStatusFilter(val status: String) : HomeIntent
    data class CancelOffer(val offerId: String, val onSuccess: (() -> Unit)? = null) : HomeIntent
    data class GetOfferById(val offerId: String) : HomeIntent
}