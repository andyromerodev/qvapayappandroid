package com.example.qvapayappandroid.domain.repository

import com.example.qvapayappandroid.domain.model.OfferAlert
import kotlinx.coroutines.flow.Flow

interface OfferAlertRepository {
    fun getAllAlerts(): Flow<List<OfferAlert>>
    fun getActiveAlerts(): Flow<List<OfferAlert>>
    fun getAlertById(id: Long): Flow<OfferAlert?>
    suspend fun saveAlert(alert: OfferAlert): Long
    suspend fun updateAlert(alert: OfferAlert)
    suspend fun deleteAlert(id: Long)
    suspend fun updateLastCheckedAt(alertId: Long, timestamp: Long)
    suspend fun updateLastTriggeredAt(alertId: Long, timestamp: Long)
    suspend fun toggleAlertActive(alertId: Long, isActive: Boolean)
}