package com.example.qvapayappandroid.data.repository

import com.example.qvapayappandroid.data.database.dao.OfferAlertDao
import com.example.qvapayappandroid.data.database.entities.OfferAlertEntity
import com.example.qvapayappandroid.domain.model.OfferAlert
import com.example.qvapayappandroid.domain.repository.OfferAlertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfferAlertRepositoryImpl(
    private val offerAlertDao: OfferAlertDao
) : OfferAlertRepository {

    override fun getAllAlerts(): Flow<List<OfferAlert>> {
        return offerAlertDao.getAllAlerts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getActiveAlerts(): Flow<List<OfferAlert>> {
        return offerAlertDao.getActiveAlerts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAlertById(id: Long): Flow<OfferAlert?> {
        return offerAlertDao.getAlertById(id).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun saveAlert(alert: OfferAlert): Long {
        return offerAlertDao.insertAlert(alert.toEntity())
    }

    override suspend fun updateAlert(alert: OfferAlert) {
        offerAlertDao.updateAlert(alert.toEntity())
    }

    override suspend fun deleteAlert(id: Long) {
        offerAlertDao.deleteAlert(id)
    }

    override suspend fun updateLastCheckedAt(alertId: Long, timestamp: Long) {
        offerAlertDao.updateLastCheckedAt(alertId, timestamp)
    }

    override suspend fun updateLastTriggeredAt(alertId: Long, timestamp: Long) {
        offerAlertDao.updateLastTriggeredAt(alertId, timestamp)
    }

    override suspend fun toggleAlertActive(alertId: Long, isActive: Boolean) {
        offerAlertDao.toggleAlertActive(alertId, isActive)
    }

    private fun OfferAlertEntity.toDomainModel(): OfferAlert {
        return OfferAlert(
            id = id,
            name = name,
            coinType = coinType,
            offerType = offerType,
            minAmount = minAmount,
            maxAmount = maxAmount,
            targetRate = targetRate,
            rateComparison = rateComparison,
            onlyKyc = onlyKyc,
            onlyVip = onlyVip,
            isActive = isActive,
            checkIntervalMinutes = checkIntervalMinutes,
            createdAt = createdAt,
            lastCheckedAt = lastCheckedAt,
            lastTriggeredAt = lastTriggeredAt
        )
    }

    private fun OfferAlert.toEntity(): OfferAlertEntity {
        return OfferAlertEntity(
            id = id,
            name = name,
            coinType = coinType,
            offerType = offerType,
            minAmount = minAmount,
            maxAmount = maxAmount,
            targetRate = targetRate,
            rateComparison = rateComparison,
            onlyKyc = onlyKyc,
            onlyVip = onlyVip,
            isActive = isActive,
            checkIntervalMinutes = checkIntervalMinutes,
            createdAt = createdAt,
            lastCheckedAt = lastCheckedAt,
            lastTriggeredAt = lastTriggeredAt
        )
    }
}