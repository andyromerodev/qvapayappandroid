package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.data.work.OfferAlertWorkManager
import com.example.qvapayappandroid.domain.repository.OfferAlertRepository
import kotlinx.coroutines.flow.first

class ManageAlertWorkManagerUseCase(
    private val workManager: OfferAlertWorkManager,
    private val repository: OfferAlertRepository
) {
    suspend operator fun invoke() {
        val activeAlerts = repository.getAllAlerts().first()
        val hasActiveAlerts = activeAlerts.any { it.isActive }
        
        if (hasActiveAlerts) {
            workManager.startPeriodicChecks()
        } else {
            workManager.stopPeriodicChecks()
        }
    }
    
    fun scheduleImmediateCheck() {
        workManager.scheduleOneTimeCheck()
    }
}