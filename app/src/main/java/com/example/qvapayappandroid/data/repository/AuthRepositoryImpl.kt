package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.datastore.SessionPreferencesRepository
import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.domain.repository.AuthRepository

/**
 * AuthRepository implementation optimized for DataStore.
 * Directly uses SessionPreferencesRepository for faster and more efficient session management.
 */
class AuthRepositoryImpl(
    private val loginDataSource: LoginDataSource,
    private val sessionPreferencesRepository: SessionPreferencesRepository
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d(TAG, "🔐 Starting login process for user: ${request.email}")
            
            loginDataSource.login(request).fold(
                onSuccess = { loginResponse ->
                    Log.d(TAG, "✅ Login successful, saving session to DataStore...")
                    
                    // Save session directly to DataStore for optimal performance
                    saveSessionToDataStore(loginResponse)
                    
                    Log.d(TAG, "✅ Session saved successfully to DataStore")
                    Result.success(loginResponse)
                },
                onFailure = { loginError ->
                    Log.e(TAG, "❌ Login failed: ${loginError.message}")
                    Result.failure(loginError)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during login: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Saves login session directly to DataStore preferences.
     * This is more efficient than going through the hybrid SessionRepository.
     */
    private suspend fun saveSessionToDataStore(loginResponse: LoginResponse) {
        try {
            val user = loginResponse.me
            
            Log.d(TAG, "💾 Saving session data to DataStore for user: ${user.username}")
            
            // Save basic session info - using available fields from LoginResponse and User
            sessionPreferencesRepository.saveSession(
                userId = user.uuid, // Using UUID as ID since no direct ID field
                userUuid = user.uuid,
                username = user.username,
                accessToken = loginResponse.accessToken,
                refreshToken = "" // LoginResponse doesn't have refresh token
            )
            
            Log.d(TAG, "✅ Basic session data saved to DataStore")
            
            // Note: SessionPreferencesRepository doesn't have saveUserInfo and saveUserPermissions methods
            // These would need to be added if we want to store additional user data in DataStore
            // For now, we'll keep it simple with just the basic session data
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving session to DataStore: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Performs logout by clearing session data from DataStore.
     * More efficient than hybrid approach.
     */
    override suspend fun logout(): Result<Unit> {
        return try {
            Log.d(TAG, "🚪 Starting logout process...")
            
            sessionPreferencesRepository.clearSession()
            
            Log.d(TAG, "✅ Logout completed - session cleared from DataStore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during logout: ${e.message}", e)
            Result.failure(e)
        }
    }
}