package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.model.OfferAlert
import com.example.qvapayappandroid.domain.repository.OfferAlertRepository
import kotlinx.coroutines.flow.Flow

class GetOfferAlertsUseCase(
    private val repository: OfferAlertRepository
) {
    operator fun invoke(): Flow<List<OfferAlert>> {
        return repository.getAllAlerts()
    }
}