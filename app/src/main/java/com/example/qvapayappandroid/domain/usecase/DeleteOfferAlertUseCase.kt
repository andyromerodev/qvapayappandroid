package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.OfferAlertRepository

class DeleteOfferAlertUseCase(
    private val repository: OfferAlertRepository
) {
    suspend operator fun invoke(alertId: Long): Result<Unit> {
        return try {
            repository.deleteAlert(alertId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}