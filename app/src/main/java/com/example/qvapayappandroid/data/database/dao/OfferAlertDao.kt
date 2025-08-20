package com.example.qvapayappandroid.data.database.dao

import androidx.room.*
import com.example.qvapayappandroid.data.database.entities.OfferAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferAlertDao {
    
    @Query("SELECT * FROM offer_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<OfferAlertEntity>>
    
    @Query("SELECT * FROM offer_alerts WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAlerts(): Flow<List<OfferAlertEntity>>
    
    @Query("SELECT * FROM offer_alerts WHERE id = :id")
    fun getAlertById(id: Long): Flow<OfferAlertEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: OfferAlertEntity): Long
    
    @Update
    suspend fun updateAlert(alert: OfferAlertEntity)
    
    @Query("DELETE FROM offer_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)
    
    @Query("UPDATE offer_alerts SET lastCheckedAt = :timestamp WHERE id = :alertId")
    suspend fun updateLastCheckedAt(alertId: Long, timestamp: Long)
    
    @Query("UPDATE offer_alerts SET lastTriggeredAt = :timestamp WHERE id = :alertId")
    suspend fun updateLastTriggeredAt(alertId: Long, timestamp: Long)
    
    @Query("UPDATE offer_alerts SET isActive = :isActive WHERE id = :alertId")
    suspend fun toggleAlertActive(alertId: Long, isActive: Boolean)
    
    @Query("SELECT COUNT(*) FROM offer_alerts WHERE isActive = 1")
    suspend fun getActiveAlertsCount(): Int
}