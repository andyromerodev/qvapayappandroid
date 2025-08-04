package com.example.qvapayappandroid.data.repository

import com.example.qvapayappandroid.data.datasource.SettingsLocalDataSource
import com.example.qvapayappandroid.domain.repository.SettingsRepository
import com.example.qvapayappandroid.presentation.ui.settings.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settingsLocalDataSource: SettingsLocalDataSource
) : SettingsRepository {
    
    override suspend fun getSettings(): UserSettings {
        val settingsEntity = settingsLocalDataSource.getSettings()
        return if (settingsEntity != null) {
            UserSettings(
                theme = settingsEntity.theme,
                language = settingsEntity.language,
                notificationsEnabled = settingsEntity.notificationsEnabled,
                biometricEnabled = settingsEntity.biometricEnabled
            )
        } else {
            initializeDefaultSettings()
            UserSettings()
        }
    }
    
    override fun getSettingsFlow(): Flow<UserSettings> {
        return settingsLocalDataSource.getSettingsFlow().map { settingsEntity ->
            if (settingsEntity != null) {
                UserSettings(
                    theme = settingsEntity.theme,
                    language = settingsEntity.language,
                    notificationsEnabled = settingsEntity.notificationsEnabled,
                    biometricEnabled = settingsEntity.biometricEnabled
                )
            } else {
                // Si no hay configuraciones, inicializar con valores por defecto
                // y devolver los valores por defecto inmediatamente
                UserSettings()
            }
        }
    }
    
    override suspend fun updateTheme(theme: String) {
        settingsLocalDataSource.updateTheme(theme)
    }
    
    override suspend fun updateLanguage(language: String) {
        settingsLocalDataSource.updateLanguage(language)
    }
    
    override suspend fun updateNotifications(enabled: Boolean) {
        settingsLocalDataSource.updateNotifications(enabled)
    }
    
    override suspend fun updateBiometric(enabled: Boolean) {
        settingsLocalDataSource.updateBiometric(enabled)
    }
    
    override suspend fun initializeDefaultSettings() {
        settingsLocalDataSource.initializeDefaultSettings()
    }
}