package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    suspend fun getSettings(): SettingsEntity?
    fun getSettingsFlow(): Flow<SettingsEntity?>
    suspend fun saveSettings(settings: SettingsEntity)
    suspend fun updateTheme(theme: String)
    suspend fun updateLanguage(language: String)
    suspend fun updateNotifications(enabled: Boolean)
    suspend fun updateBiometric(enabled: Boolean)
    suspend fun initializeDefaultSettings()
}