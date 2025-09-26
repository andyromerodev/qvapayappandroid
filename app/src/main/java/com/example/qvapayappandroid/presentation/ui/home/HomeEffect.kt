package com.example.qvapayappandroid.presentation.ui.home

sealed interface HomeEffect {
    data class ShowSuccessMessage(val message: String) : HomeEffect
    data class ShowErrorMessage(val message: String) : HomeEffect
    data class NavigateToOfferDetail(val offerId: String) : HomeEffect
    data object NavigateToCreateOffer : HomeEffect
    data object RefreshOffers : HomeEffect
}