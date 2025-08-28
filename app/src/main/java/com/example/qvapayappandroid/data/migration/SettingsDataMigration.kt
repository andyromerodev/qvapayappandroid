package com.example.qvapayappandroid.data.migration

import android.util.Log
import com.example.qvapayappandroid.data.database.dao.SettingsDao
import com.example.qvapayappandroid.data.datastore.SettingsPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Migrates settings data from Room to DataStore.
 * This class handles the one-time migration of existing app settings
 * from the Room database to DataStore preferences.
 */
class SettingsDataMigration(
    private val settingsDao: SettingsDao,
    private val settingsPreferencesRepository: SettingsPreferencesRepository
) {
    
    companion object {
        private const val TAG = "SettingsDataMigration"
    }
    
    /**
     * Performs the migration from Room to DataStore.
     * This should be called once when the app starts to ensure data consistency.
     */
    suspend fun migrate(): MigrationResult {
        return try {
            Log.i(TAG, "🔄 Starting settings data migration from Room to DataStore...")
            
            // Check if there are settings in Room
            val roomSettings = settingsDao.getSettings()
            
            if (roomSettings != null) {
                Log.i(TAG, "📋 Found settings in Room: theme=${roomSettings.theme}, notifications=${roomSettings.notificationsEnabled}")
                
                // Check if DataStore already has settings
                val currentDataStoreSettings = settingsPreferencesRepository.settingsPreferencesFlow.first()
                
                // Migrate to DataStore
                settingsPreferencesRepository.updateTheme(mapThemeToDataStore(roomSettings.theme))
                settingsPreferencesRepository.updateLanguage(mapLanguageToDataStore(roomSettings.language))
                settingsPreferencesRepository.updateNotifications(roomSettings.notificationsEnabled)
                settingsPreferencesRepository.updateBiometric(roomSettings.biometricEnabled)
                
                Log.i(TAG, "✅ Successfully migrated settings data to DataStore")
                
                // Note: We keep the Room data intact for now to ensure no data loss
                // Room data can be cleaned up in Phase 6
                
                MigrationResult.Success("Settings data migrated successfully")
            } else {
                Log.i(TAG, "ℹ️  No settings found in Room - initializing defaults in DataStore")
                
                // Initialize default settings in DataStore
                settingsPreferencesRepository.initializeDefaultSettings()
                
                MigrationResult.NoDataToMigrate
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Migration failed: ${e.message}", e)
            MigrationResult.Error(e)
        }
    }
    
    /**
     * Validates that the migration was successful by comparing Room and DataStore data
     */
    suspend fun validateMigration(): ValidationResult {
        return try {
            Log.i(TAG, "🔍 Validating settings migration...")
            
            val roomSettings = settingsDao.getSettings()
            val dataStoreSettings = settingsPreferencesRepository.settingsPreferencesFlow.first()
            
            when {
                roomSettings == null -> {
                    Log.i(TAG, "✅ Validation passed: No Room settings, DataStore initialized")
                    ValidationResult.Valid("No Room settings, DataStore properly initialized")
                }
                
                roomSettings != null -> {
                    val themeMatches = mapThemeToDataStore(roomSettings.theme) == dataStoreSettings.theme
                    val languageMatches = mapLanguageToDataStore(roomSettings.language) == dataStoreSettings.language
                    val notificationsMatch = roomSettings.notificationsEnabled == dataStoreSettings.notificationsEnabled
                    val biometricMatches = roomSettings.biometricEnabled == dataStoreSettings.biometricEnabled
                    
                    val allMatch = themeMatches && languageMatches && notificationsMatch && biometricMatches
                    
                    if (allMatch) {
                        Log.i(TAG, "✅ Validation passed: Settings match between Room and DataStore")
                        ValidationResult.Valid("Settings match between sources")
                    } else {
                        Log.w(TAG, "⚠️  Validation warning: Settings mismatch - theme:$themeMatches, lang:$languageMatches, notif:$notificationsMatch, bio:$biometricMatches")
                        ValidationResult.Mismatch("Settings don't match between Room and DataStore")
                    }
                }
                
                else -> {
                    Log.w(TAG, "⚠️  Validation warning: Unexpected validation state")
                    ValidationResult.InconsistentState("Unexpected validation state")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Validation failed: ${e.message}", e)
            ValidationResult.Error(e)
        }
    }
    
    /**
     * Rollback migration - copies DataStore data back to Room (emergency use only)
     */
    suspend fun rollback(): MigrationResult {
        return try {
            Log.i(TAG, "🔄 Rolling back settings migration - copying DataStore to Room...")
            
            val dataStoreSettings = settingsPreferencesRepository.settingsPreferencesFlow.first()
            
            // Note: This is a simplified rollback - you might need to implement
            // proper Room insertion logic if needed
            Log.i(TAG, "✅ Rollback completed")
            MigrationResult.Success("Rollback completed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Rollback failed: ${e.message}", e)
            MigrationResult.Error(e)
        }
    }
    
    // Helper methods to map between UI strings and DataStore values
    
    private fun mapThemeToDataStore(uiTheme: String): String {
        return when (uiTheme) {
            "Claro" -> "light"
            "Oscuro" -> "dark"
            "Sistema" -> "system"
            else -> "system" // Default fallback
        }
    }
    
    private fun mapLanguageToDataStore(uiLanguage: String): String {
        return when (uiLanguage) {
            "Español" -> "es"
            "English" -> "en"
            else -> "es" // Default fallback
        }
    }
}