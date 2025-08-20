package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferAlert
import com.example.qvapayappandroid.domain.repository.OfferAlertRepository

class UpdateOfferAlertUseCase(
    private val repository: OfferAlertRepository
) {
    suspend operator fun invoke(alert: OfferAlert): Result<Unit> {
        return try {
            repository.updateAlert(alert)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}