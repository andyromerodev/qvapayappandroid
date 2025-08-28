package com.example.qvapayappandroid.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Repository for managing general app preferences using DataStore.
 * This handles first launch state, sync timestamps, app version, and filter preferences.
 */
class AppPreferencesRepository(private val context: Context) {

    /**
     * Data class representing app preferences
     */
    data class AppPreferences(
        val isFirstLaunch: Boolean = true,
        val lastSyncTimestamp: Long = 0L,
        val appVersion: String = "",
        val selectedOfferType: String = "all", // "all", "buy", "sell"
        val selectedCoins: List<String> = emptyList(),
        val filtersLastUsed: Long = 0L
    )

    /**
     * Flow that emits app preferences whenever they change
     */
    val appPreferencesFlow: Flow<AppPreferences> = context.dataStore.data
        .catch { exception ->
            // If there's an error reading preferences, emit empty preferences
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val selectedCoinsJson = preferences[PreferencesKeys.SELECTED_COINS] ?: "[]"
            val selectedCoins = try {
                Json.decodeFromString<List<String>>(selectedCoinsJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            AppPreferences(
                isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true,
                lastSyncTimestamp = preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] ?: 0L,
                appVersion = preferences[PreferencesKeys.APP_VERSION] ?: "",
                selectedOfferType = preferences[PreferencesKeys.SELECTED_OFFER_TYPE] ?: "all",
                selectedCoins = selectedCoins,
                filtersLastUsed = preferences[PreferencesKeys.FILTERS_LAST_USED] ?: 0L
            )
        }

    /**
     * Mark first launch as completed
     */
    suspend fun markFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    /**
     * Update last sync timestamp
     */
    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    /**
     * Update app version
     */
    suspend fun updateAppVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_VERSION] = version
        }
    }

    /**
     * Update P2P filter preferences
     */
    suspend fun updateP2PFilters(offerType: String, selectedCoins: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_OFFER_TYPE] = offerType
            preferences[PreferencesKeys.SELECTED_COINS] = Json.encodeToString(selectedCoins)
            preferences[PreferencesKeys.FILTERS_LAST_USED] = System.currentTimeMillis()
        }
    }

    /**
     * Update selected offer type only
     */
    suspend fun updateSelectedOfferType(offerType: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_OFFER_TYPE] = offerType
            preferences[PreferencesKeys.FILTERS_LAST_USED] = System.currentTimeMillis()
        }
    }

    /**
     * Update selected coins only
     */
    suspend fun updateSelectedCoins(selectedCoins: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_COINS] = Json.encodeToString(selectedCoins)
            preferences[PreferencesKeys.FILTERS_LAST_USED] = System.currentTimeMillis()
        }
    }

    /**
     * Clear P2P filter preferences
     */
    suspend fun clearP2PFilters() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_OFFER_TYPE] = "all"
            preferences[PreferencesKeys.SELECTED_COINS] = Json.encodeToString(emptyList<String>())
            preferences[PreferencesKeys.FILTERS_LAST_USED] = System.currentTimeMillis()
        }
    }

    /**
     * Get first launch status
     */
    fun isFirstLaunch(): Flow<Boolean> {
        return appPreferencesFlow.map { it.isFirstLaunch }
    }

    /**
     * Get last sync timestamp
     */
    fun getLastSyncTimestamp(): Flow<Long> {
        return appPreferencesFlow.map { it.lastSyncTimestamp }
    }

    /**
     * Get current P2P filters
     */
    fun getP2PFilters(): Flow<Pair<String, List<String>>> {
        return appPreferencesFlow.map { prefs ->
            prefs.selectedOfferType to prefs.selectedCoins
        }
    }

    /**
     * Clear all app preferences
     */
    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.IS_FIRST_LAUNCH)
            preferences.remove(PreferencesKeys.LAST_SYNC_TIMESTAMP)
            preferences.remove(PreferencesKeys.APP_VERSION)
            preferences.remove(PreferencesKeys.SELECTED_OFFER_TYPE)
            preferences.remove(PreferencesKeys.SELECTED_COINS)
            preferences.remove(PreferencesKeys.FILTERS_LAST_USED)
        }
    }
}