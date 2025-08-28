package com.example.qvapayappandroid.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Repository for managing session-related preferences using DataStore.
 * This handles authentication tokens, user credentials, and session state.
 */
class SessionPreferencesRepository(private val context: Context) {

    /**
     * Data class representing session preferences
     */
    data class SessionPreferences(
        val userId: String = "",
        val userUuid: String = "",
        val username: String = "",
        val accessToken: String = "",
        val refreshToken: String = "",
        val isLoggedIn: Boolean = false,
        val sessionCreatedAt: Long = 0L,
        val sessionUpdatedAt: Long = 0L
    )

    /**
     * Flow that emits session preferences whenever they change
     */
    val sessionPreferencesFlow: Flow<SessionPreferences> = context.dataStore.data
        .catch { exception ->
            // If there's an error reading preferences, emit empty preferences
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SessionPreferences(
                userId = preferences[PreferencesKeys.USER_ID] ?: "",
                userUuid = preferences[PreferencesKeys.USER_UUID] ?: "",
                username = preferences[PreferencesKeys.USERNAME] ?: "",
                accessToken = preferences[PreferencesKeys.ACCESS_TOKEN] ?: "",
                refreshToken = preferences[PreferencesKeys.REFRESH_TOKEN] ?: "",
                isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false,
                sessionCreatedAt = preferences[PreferencesKeys.SESSION_CREATED_AT] ?: 0L,
                sessionUpdatedAt = preferences[PreferencesKeys.SESSION_UPDATED_AT] ?: 0L
            )
        }

    /**
     * Save complete session information
     */
    suspend fun saveSession(
        userId: String,
        userUuid: String,
        username: String,
        accessToken: String,
        refreshToken: String
    ) {
        val currentTime = System.currentTimeMillis()
        
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_UUID] = userUuid
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.ACCESS_TOKEN] = accessToken
            preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.SESSION_CREATED_AT] = currentTime
            preferences[PreferencesKeys.SESSION_UPDATED_AT] = currentTime
        }
    }

    /**
     * Update access token only
     */
    suspend fun updateAccessToken(accessToken: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = accessToken
            preferences[PreferencesKeys.SESSION_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Update refresh token only
     */
    suspend fun updateRefreshToken(refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
            preferences[PreferencesKeys.SESSION_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Update login status
     */
    suspend fun updateLoginStatus(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferencesKeys.SESSION_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Clear all session data (logout)
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USER_UUID)
            preferences.remove(PreferencesKeys.USERNAME)
            preferences.remove(PreferencesKeys.ACCESS_TOKEN)
            preferences.remove(PreferencesKeys.REFRESH_TOKEN)
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
            preferences.remove(PreferencesKeys.SESSION_CREATED_AT)
            preferences[PreferencesKeys.SESSION_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /**
     * Check if user is currently logged in
     */
    fun isLoggedIn(): Flow<Boolean> {
        return sessionPreferencesFlow.map { it.isLoggedIn }.catch { emit(false) }
    }

    /**
     * Get current access token
     */
    fun getAccessToken(): Flow<String> {
        return sessionPreferencesFlow.map { it.accessToken }
    }

    /**
     * Get current user UUID
     */
    fun getUserUuid(): Flow<String> {
        return sessionPreferencesFlow.map { it.userUuid }
    }
}