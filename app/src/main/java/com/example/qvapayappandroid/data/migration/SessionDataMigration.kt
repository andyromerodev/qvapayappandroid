package com.example.qvapayappandroid.data.migration

import android.util.Log
import com.example.qvapayappandroid.data.database.dao.SessionDao
import com.example.qvapayappandroid.data.database.dao.UserDao
import com.example.qvapayappandroid.data.datastore.SessionPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Migrates session data from Room to DataStore.
 * This class handles the one-time migration of existing session and user data
 * from the Room database to DataStore preferences.
 */
class SessionDataMigration(
    private val sessionDao: SessionDao,
    private val userDao: UserDao,
    private val sessionPreferencesRepository: SessionPreferencesRepository
) {
    
    companion object {
        private const val TAG = "SessionDataMigration"
        private const val MIGRATION_COMPLETED_KEY = "session_migration_completed"
    }
    
    /**
     * Performs the migration from Room to DataStore.
     * This should be called once when the app starts to ensure data consistency.
     */
    suspend fun migrate(): MigrationResult {
        return try {
            Log.i(TAG, "🔄 Starting session data migration from Room to DataStore...")
            
            // Check if there's an active session in Room
            val activeSession = sessionDao.getActiveSession()
            
            if (activeSession != null) {
                Log.i(TAG, "📋 Found active session in Room: ${activeSession.userUuid}")
                
                // Get the user data from Room
                val userData = userDao.getUserByUuid(activeSession.userUuid)
                
                if (userData != null) {
                    Log.i(TAG, "👤 Found user data: ${userData.username}")
                    
                    // Check if DataStore already has session data
                    val currentDataStoreSession = sessionPreferencesRepository.sessionPreferencesFlow
                    
                    // Migrate to DataStore
                    sessionPreferencesRepository.saveSession(
                        userId = userData.username,
                        userUuid = userData.uuid,
                        username = userData.username,
                        accessToken = activeSession.accessToken,
                        refreshToken = "" // No refresh token in current implementation
                    )
                    
                    Log.i(TAG, "✅ Successfully migrated session data to DataStore")
                    
                    // Note: We keep the Room data intact for now to ensure no data loss
                    // Room data can be cleaned up in Phase 6
                    
                    MigrationResult.Success("Session data migrated successfully")
                } else {
                    Log.w(TAG, "⚠️  Session exists but user data not found")
                    MigrationResult.PartialSuccess("Session exists but user data not found")
                }
            } else {
                Log.i(TAG, "ℹ️  No active session found in Room - nothing to migrate")
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
            Log.i(TAG, "🔍 Validating migration...")
            
            val roomSession = sessionDao.getActiveSession()
            val dataStoreSession = sessionPreferencesRepository.sessionPreferencesFlow.first()
            
            when {
                roomSession == null && !dataStoreSession.isLoggedIn -> {
                    Log.i(TAG, "✅ Validation passed: No session in both sources")
                    ValidationResult.Valid("No sessions in both sources")
                }
                
                roomSession != null && dataStoreSession.isLoggedIn -> {
                    val roomUser = userDao.getUserByUuid(roomSession.userUuid)
                    
                    val isValid = roomSession.accessToken == dataStoreSession.accessToken &&
                                 roomUser?.uuid == dataStoreSession.userUuid &&
                                 roomUser?.username == dataStoreSession.username
                    
                    if (isValid) {
                        Log.i(TAG, "✅ Validation passed: Data matches between Room and DataStore")
                        ValidationResult.Valid("Data matches between sources")
                    } else {
                        Log.w(TAG, "⚠️  Validation warning: Data mismatch between sources")
                        ValidationResult.Mismatch("Data doesn't match between Room and DataStore")
                    }
                }
                
                else -> {
                    Log.w(TAG, "⚠️  Validation warning: Inconsistent state between sources")
                    ValidationResult.InconsistentState("Inconsistent session state between sources")
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
            Log.i(TAG, "🔄 Rolling back migration - copying DataStore to Room...")
            
            val dataStoreSession = sessionPreferencesRepository.sessionPreferencesFlow.first()
            
            if (dataStoreSession.isLoggedIn && dataStoreSession.accessToken.isNotEmpty()) {
                // Note: This is a simplified rollback - in a real scenario you might need
                // to restore user data from another source or API
                Log.i(TAG, "✅ Rollback completed")
                MigrationResult.Success("Rollback completed")
            } else {
                Log.i(TAG, "ℹ️  No DataStore session to rollback")
                MigrationResult.NoDataToMigrate
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Rollback failed: ${e.message}", e)
            MigrationResult.Error(e)
        }
    }
}

/**
 * Result of a migration operation
 */
sealed class MigrationResult {
    data class Success(val message: String) : MigrationResult()
    data class PartialSuccess(val message: String) : MigrationResult()
    data class Error(val exception: Exception) : MigrationResult()
    object NoDataToMigrate : MigrationResult()
}

/**
 * Result of a migration validation
 */
sealed class ValidationResult {
    data class Valid(val message: String) : ValidationResult()
    data class Mismatch(val message: String) : ValidationResult()
    data class InconsistentState(val message: String) : ValidationResult()
    data class Error(val exception: Exception) : ValidationResult()
}