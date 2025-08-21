package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.data.datastore.AppPreferencesRepository
import com.example.qvapayappandroid.data.datastore.SessionPreferencesRepository
import com.example.qvapayappandroid.data.datastore.SettingsPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for DataStore dependencies.
 * This module provides DataStore-based repositories for managing app preferences.
 */
val dataStoreModule = module {
    
    /**
     * SessionPreferencesRepository - Manages user session and authentication data
     * Handles: access tokens, refresh tokens, user credentials, login status
     */
    single<SessionPreferencesRepository> { 
        SessionPreferencesRepository(androidContext()) 
    }
    
    /**
     * SettingsPreferencesRepository - Manages app settings and user preferences
     * Handles: theme, language, notifications, biometric settings
     */
    single<SettingsPreferencesRepository> { 
        SettingsPreferencesRepository(androidContext()) 
    }
    
    /**
     * AppPreferencesRepository - Manages general app state and feature preferences
     * Handles: first launch, sync timestamps, P2P filters, app version
     */
    single<AppPreferencesRepository> { 
        AppPreferencesRepository(androidContext()) 
    }
}