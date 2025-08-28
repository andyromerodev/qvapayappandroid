package com.example.qvapayappandroid.data.repository

import com.example.qvapayappandroid.data.datastore.SettingsPreferencesRepository
import com.example.qvapayappandroid.domain.repository.SettingsRepository
import com.example.qvapayappandroid.presentation.ui.settings.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * SettingsRepository implementation using DataStore instead of Room.
 * This migrates settings storage from Room database to DataStore preferences
 * for better performance and modern Android best practices.
 */
class SettingsRepositoryDataStoreImpl(
    private val settingsPreferencesRepository: SettingsPreferencesRepository
) : SettingsRepository {

    override suspend fun getSettings(): UserSettings {
        return try {
            val settingsPrefs = settingsPreferencesRepository.settingsPreferencesFlow.first()
            UserSettings(
                notificationsEnabled = settingsPrefs.notificationsEnabled,
                biometricEnabled = settingsPrefs.biometricEnabled,
                theme = mapThemeFromDataStore(settingsPrefs.theme),
                language = mapLanguageFromDataStore(settingsPrefs.language)
            )
        } catch (e: Exception) {
            // If there's an error, initialize default settings and return them
            initializeDefaultSettings()
            UserSettings()
        }
    }

    override fun getSettingsFlow(): Flow<UserSettings> {
        return settingsPreferencesRepository.settingsPreferencesFlow.map { settingsPrefs ->
            UserSettings(
                notificationsEnabled = settingsPrefs.notificationsEnabled,
                biometricEnabled = settingsPrefs.biometricEnabled,
                theme = mapThemeFromDataStore(settingsPrefs.theme),
                language = mapLanguageFromDataStore(settingsPrefs.language)
            )
        }
    }

    override suspend fun updateTheme(theme: String) {
        val dataStoreTheme = mapThemeToDataStore(theme)
        settingsPreferencesRepository.updateTheme(dataStoreTheme)
    }

    override suspend fun updateLanguage(language: String) {
        val dataStoreLanguage = mapLanguageToDataStore(language)
        settingsPreferencesRepository.updateLanguage(dataStoreLanguage)
    }

    override suspend fun updateNotifications(enabled: Boolean) {
        settingsPreferencesRepository.updateNotifications(enabled)
    }

    override suspend fun updateBiometric(enabled: Boolean) {
        settingsPreferencesRepository.updateBiometric(enabled)
    }

    override suspend fun initializeDefaultSettings() {
        settingsPreferencesRepository.initializeDefaultSettings()
    }

    /**
     * Additional DataStore-specific methods
     */

    /**
     * Get theme setting as Flow
     */
    fun getThemeFlow(): Flow<String> {
        return settingsPreferencesRepository.getTheme().map { mapThemeFromDataStore(it) }
    }

    /**
     * Get language setting as Flow
     */
    fun getLanguageFlow(): Flow<String> {
        return settingsPreferencesRepository.getLanguage().map { mapLanguageFromDataStore(it) }
    }

    /**
     * Get notifications enabled as Flow
     */
    fun getNotificationsEnabledFlow(): Flow<Boolean> {
        return settingsPreferencesRepository.getNotificationsEnabled()
    }

    /**
     * Get biometric enabled as Flow
     */
    fun getBiometricEnabledFlow(): Flow<Boolean> {
        return settingsPreferencesRepository.getBiometricEnabled()
    }

    /**
     * Get raw settings preferences (useful for debugging)
     */
    fun getSettingsPreferencesFlow(): Flow<SettingsPreferencesRepository.SettingsPreferences> {
        return settingsPreferencesRepository.settingsPreferencesFlow
    }

    /**
     * Clear all settings (reset to defaults)
     */
    suspend fun clearAllSettings() {
        settingsPreferencesRepository.clearAllSettings()
    }

    // Helper methods to map between UI strings and DataStore values

    /**
     * Maps DataStore theme values to UI display strings
     */
    private fun mapThemeFromDataStore(dataStoreTheme: String): String {
        return when (dataStoreTheme) {
            "light" -> "Claro"
            "dark" -> "Oscuro"
            "system" -> "Sistema"
            else -> "Sistema" // Default fallback
        }
    }

    /**
     * Maps UI theme strings to DataStore values
     */
    private fun mapThemeToDataStore(uiTheme: String): String {
        return when (uiTheme) {
            "Claro" -> "light"
            "Oscuro" -> "dark"
            "Sistema" -> "system"
            else -> "system" // Default fallback
        }
    }

    /**
     * Maps DataStore language values to UI display strings
     */
    private fun mapLanguageFromDataStore(dataStoreLanguage: String): String {
        return when (dataStoreLanguage) {
            "es" -> "Español"
            "en" -> "English"
            else -> "Español" // Default fallback
        }
    }

    /**
     * Maps UI language strings to DataStore values
     */
    private fun mapLanguageToDataStore(uiLanguage: String): String {
        return when (uiLanguage) {
            "Español" -> "es"
            "English" -> "en"
            else -> "es" // Default fallback
        }
    }
}