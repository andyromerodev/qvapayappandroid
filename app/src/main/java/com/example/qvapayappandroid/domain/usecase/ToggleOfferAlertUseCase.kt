package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.OfferAlertRepository

class ToggleOfferAlertUseCase(
    private val repository: OfferAlertRepository
) {
    suspend operator fun invoke(alertId: Long, isActive: Boolean): Result<Unit> {
        return try {
            repository.toggleAlertActive(alertId, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}