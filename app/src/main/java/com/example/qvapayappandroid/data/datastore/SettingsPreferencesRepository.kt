package com.example.qvapayappandroid.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Repository for managing app settings preferences using DataStore.
 * This handles theme, notifications, biometric settings, and other app configurations.
 */
class SettingsPreferencesRepository(private val context: Context) {

    /**
     * Data class representing settings preferences
     */
    data class SettingsPreferences(
        val theme: String = "system", // "light", "dark", "system"
        val language: String = "es", // "es", "en"
        val notificationsEnabled: Boolean = true,
        val biometricEnabled: Boolean = false,
        val settingsCreatedAt: Long = 0L,
        val settingsUpdatedAt: Long = 0L
    )

    /**
     * Flow that emits settings preferences whenever they change
     */
    val settingsPreferencesFlow: Flow<SettingsPreferences> = context.dataStore.data
        .catch { exception ->
            // If there's an error reading preferences, emit empty preferences
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SettingsPreferences(
                theme = preferences[PreferencesKeys.THEME] ?: "system",
                language = preferences[PreferencesKeys.LANGUAGE] ?: "es",
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                biometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
                settingsCreatedAt = preferences[PreferencesKeys.SETTINGS_CREATED_AT] ?: 0L,
                settingsUpdatedAt = preferences[PreferencesKeys.SETTINGS_UPDATED_AT] ?: 0L
            )
        }

    /**
     * Initialize default settings if they don't exist
     */
    suspend fun initializeDefaultSettings() {
        context.dataStore.edit { preferences ->
            val currentTime = System.currentTimeMillis()
            
            // Only set defaults if values don't exist
            if (!preferences.contains(PreferencesKeys.THEME)) {
                preferences[PreferencesKeys.THEME] = "system"
            }
            if (!preferences.contains(PreferencesKeys.LANGUAGE)) {
                preferences[PreferencesKeys.LANGUAGE] = "es"
            }
            if (!preferences.contains(PreferencesKeys.NOTIFICATIONS_ENABLED)) {
                preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = true
            }
            if (!preferences.contains(PreferencesKeys.BIOMETRIC_ENABLED)) {
                preferences[PreferencesKeys.BIOMETRIC_ENABLED] = false
            }
            if (!preferences.contains(PreferencesKeys.SETTINGS_CREATED_AT)) {
                preferences[PreferencesKeys.SETTINGS_CREATED_AT] = currentTime
            }
            
            preferences[PreferencesKeys.SETTINGS_UPDATED_AT] = currentTime
        }
    }

    /**
     * Update theme setting
     */
    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
            preferences[PreferencesKeys.SETTINGS_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Update language setting
     */
    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
            preferences[PreferencesKeys.SETTINGS_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Update notifications setting
     */
    suspend fun updateNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
            preferences[PreferencesKeys.SETTINGS_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Update biometric setting
     */
    suspend fun updateBiometric(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
            preferences[PreferencesKeys.SETTINGS_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Get current theme
     */
    fun getTheme(): Flow<String> {
        return settingsPreferencesFlow.map { it.theme }
    }

    /**
     * Get current language
     */
    fun getLanguage(): Flow<String> {
        return settingsPreferencesFlow.map { it.language }
    }

    /**
     * Get notifications enabled status
     */
    fun getNotificationsEnabled(): Flow<Boolean> {
        return settingsPreferencesFlow.map { it.notificationsEnabled }
    }

    /**
     * Get biometric enabled status
     */
    fun getBiometricEnabled(): Flow<Boolean> {
        return settingsPreferencesFlow.map { it.biometricEnabled }
    }

    /**
     * Clear all settings (reset to defaults)
     */
    suspend fun clearAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.THEME)
            preferences.remove(PreferencesKeys.LANGUAGE)
            preferences.remove(PreferencesKeys.NOTIFICATIONS_ENABLED)
            preferences.remove(PreferencesKeys.BIOMETRIC_ENABLED)
            preferences.remove(PreferencesKeys.SETTINGS_CREATED_AT)
            preferences.remove(PreferencesKeys.SETTINGS_UPDATED_AT)
        }
        
        // Re-initialize with defaults
        initializeDefaultSettings()
    }
}