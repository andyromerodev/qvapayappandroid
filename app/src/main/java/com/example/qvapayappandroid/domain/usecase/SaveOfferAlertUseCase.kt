package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferAlert
import com.example.qvapayappandroid.domain.repository.OfferAlertRepository

class SaveOfferAlertUseCase(
    private val repository: OfferAlertRepository
) {
    suspend operator fun invoke(alert: OfferAlert): Result<Long> {
        return try {
            val id = repository.saveAlert(alert)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}