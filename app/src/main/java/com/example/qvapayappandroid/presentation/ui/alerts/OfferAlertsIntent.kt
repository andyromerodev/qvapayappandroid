package com.example.qvapayappandroid.presentation.ui.alerts

import com.example.qvapayappandroid.domain.model.OfferAlert

sealed interface OfferAlertsIntent {
    data object LoadAlerts : OfferAlertsIntent
    data object RefreshAlerts : OfferAlertsIntent
    data object ShowCreateAlert : OfferAlertsIntent
    data class EditAlert(val alert: OfferAlert) : OfferAlertsIntent
    data class DeleteAlert(val alertId: Long) : OfferAlertsIntent
    data class ToggleAlert(val alertId: Long, val isActive: Boolean) : OfferAlertsIntent
    data class CreateAlert(val alert: OfferAlert) : OfferAlertsIntent
    data class UpdateAlert(val alert: OfferAlert) : OfferAlertsIntent
    data object DismissDialog : OfferAlertsIntent
    data object DismissError : OfferAlertsIntent
    data class ConfirmDelete(val alertId: Long) : OfferAlertsIntent
    data object CancelDelete : OfferAlertsIntent
}