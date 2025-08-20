package com.example.qvapayappandroid.presentation.ui.alerts

import com.example.qvapayappandroid.domain.model.OfferAlert

data class OfferAlertsState(
    val alerts: List<OfferAlert> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val editingAlert: OfferAlert? = null,
    val deletingAlertId: Long? = null,
    val showDeleteConfirmation: Boolean = false,
    val isProcessing: Boolean = false
) {
    val isEmpty: Boolean = alerts.isEmpty() && !isLoading
    val hasActiveAlerts: Boolean = alerts.any { it.isActive }
    val activeAlertsCount: Int = alerts.count { it.isActive }
    val totalAlertsCount: Int = alerts.size
}