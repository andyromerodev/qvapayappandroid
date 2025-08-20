package com.example.qvapayappandroid.presentation.ui.alerts

sealed interface OfferAlertsEffect {
    data class ShowSuccessMessage(val message: String) : OfferAlertsEffect
    data class ShowErrorMessage(val message: String) : OfferAlertsEffect
    data object NavigateToCreateAlert : OfferAlertsEffect
    data class NavigateToEditAlert(val alertId: Long) : OfferAlertsEffect
}