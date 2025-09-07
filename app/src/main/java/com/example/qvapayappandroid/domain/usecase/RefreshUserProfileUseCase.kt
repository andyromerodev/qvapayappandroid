package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.User
import com.example.qvapayappandroid.domain.repository.SessionRepository

/**
 * RefreshUserProfileUseCase following Clean Architecture principles.
 * Handles the business logic for refreshing user profile data from server.
 */
class RefreshUserProfileUseCase(
    private val sessionRepository: SessionRepository
) {
    companion object {
        private const val TAG = "RefreshUserProfileUC"
    }
    
    /**
     * Refresh user profile by fetching fresh data from the server
     * and updating local storage (both DataStore and Room).
     */
    suspend operator fun invoke(): Result<User> {
        return try {
            Log.d(TAG, "🔄 Executing user profile refresh...")
            
            sessionRepository.refreshUserProfile().fold(
                onSuccess = { refreshedUser ->
                    Log.d(TAG, "✅ User profile refreshed successfully: ${refreshedUser.username}")
                    Result.success(refreshedUser)
                },
                onFailure = { error ->
                    Log.e(TAG, "❌ User profile refresh failed: ${error.message}", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during profile refresh: ${e.message}", e)
            Result.failure(e)
        }
    }
}