package com.example.qvapayappandroid.data.database.dao

import androidx.room.*
import com.example.qvapayappandroid.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?
    
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: SettingsEntity)
    
    @Query("UPDATE settings SET theme = :theme, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateTheme(theme: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE settings SET language = :language, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateLanguage(language: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE settings SET notificationsEnabled = :enabled, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateNotifications(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE settings SET biometricEnabled = :enabled, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateBiometric(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()
}