package com.example.qvapayappandroid.domain.repository

import com.example.qvapayappandroid.presentation.ui.settings.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSettings(): UserSettings
    fun getSettingsFlow(): Flow<UserSettings>
    suspend fun updateTheme(theme: String)
    suspend fun updateLanguage(language: String)
    suspend fun updateNotifications(enabled: Boolean)
    suspend fun updateBiometric(enabled: Boolean)
    suspend fun initializeDefaultSettings()
}