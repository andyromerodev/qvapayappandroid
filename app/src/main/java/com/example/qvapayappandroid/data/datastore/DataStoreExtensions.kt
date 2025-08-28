package com.example.qvapayappandroid.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Extension property to create DataStore instance.
 * This creates a singleton DataStore instance per app process.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qvapay_preferences")

/**
 * DataStore file names constants
 */
object DataStoreNames {
    const val QVAPAY_PREFERENCES = "qvapay_preferences"
}