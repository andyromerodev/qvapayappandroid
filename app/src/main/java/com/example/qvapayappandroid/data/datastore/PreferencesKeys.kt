package com.example.qvapayappandroid.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * PreferencesKeys object contains all the keys used for DataStore Preferences.
 * This provides type safety and centralizes all preference keys in one place.
 */
object PreferencesKeys {
    
    // Session Data Keys
    val USER_ID = stringPreferencesKey("user_id")
    val USER_UUID = stringPreferencesKey("user_uuid")
    val USERNAME = stringPreferencesKey("username")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val SESSION_CREATED_AT = longPreferencesKey("session_created_at")
    val SESSION_UPDATED_AT = longPreferencesKey("session_updated_at")
    
    // Settings Data Keys
    val THEME = stringPreferencesKey("theme")
    val LANGUAGE = stringPreferencesKey("language")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    val SETTINGS_CREATED_AT = longPreferencesKey("settings_created_at")
    val SETTINGS_UPDATED_AT = longPreferencesKey("settings_updated_at")
    
    // App Preferences Keys
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    val APP_VERSION = stringPreferencesKey("app_version")
    
    // Filter Preferences Keys (for P2P filters persistence)
    val SELECTED_OFFER_TYPE = stringPreferencesKey("selected_offer_type")
    val SELECTED_COINS = stringPreferencesKey("selected_coins") // JSON array as string
    val FILTERS_LAST_USED = longPreferencesKey("filters_last_used")
}