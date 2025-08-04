package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.database.dao.SettingsDao
import com.example.qvapayappandroid.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

class SettingsLocalDataSourceImpl(
    private val settingsDao: SettingsDao
) : SettingsLocalDataSource {
    
    override suspend fun getSettings(): SettingsEntity? {
        return settingsDao.getSettings()
    }
    
    override fun getSettingsFlow(): Flow<SettingsEntity?> {
        return settingsDao.getSettingsFlow()
    }
    
    override suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insertSettings(settings)
    }
    
    override suspend fun updateTheme(theme: String) {
        val settings = getSettings()
        if (settings != null) {
            settingsDao.updateTheme(theme)
        } else {
            initializeDefaultSettings()
            settingsDao.updateTheme(theme)
        }
    }
    
    override suspend fun updateLanguage(language: String) {
        val settings = getSettings()
        if (settings != null) {
            settingsDao.updateLanguage(language)
        } else {
            initializeDefaultSettings()
            settingsDao.updateLanguage(language)
        }
    }
    
    override suspend fun updateNotifications(enabled: Boolean) {
        val settings = getSettings()
        if (settings != null) {
            settingsDao.updateNotifications(enabled)
        } else {
            initializeDefaultSettings()
            settingsDao.updateNotifications(enabled)
        }
    }
    
    override suspend fun updateBiometric(enabled: Boolean) {
        val settings = getSettings()
        if (settings != null) {
            settingsDao.updateBiometric(enabled)
        } else {
            initializeDefaultSettings()
            settingsDao.updateBiometric(enabled)
        }
    }
    
    override suspend fun initializeDefaultSettings() {
        val existingSettings = getSettings()
        if (existingSettings == null) {
            val defaultSettings = SettingsEntity()
            saveSettings(defaultSettings)
        }
    }
}