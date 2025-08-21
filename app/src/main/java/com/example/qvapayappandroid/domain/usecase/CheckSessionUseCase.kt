package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * CheckSessionUseCase optimized for DataStore.
 * Provides both suspend and Flow-based session checking for different use cases.
 */
class CheckSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    companion object {
        private const val TAG = "CheckSessionUseCase"
    }
    
    /**
     * Check current session status (one-time check)
     */
    suspend operator fun invoke(): Boolean {
        return try {
            val isLoggedIn = sessionRepository.isUserLoggedIn()
            Log.d(TAG, "🔍 Session check result: $isLoggedIn")
            isLoggedIn
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking session: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get reactive session status flow (continuous monitoring)
     */
    fun getSessionStatusFlow(): Flow<Boolean> {
        return try {
            // Cast to DataStore implementation to access reactive flows
            if (sessionRepository is com.example.qvapayappandroid.data.repository.SessionRepositoryDataStoreImpl) {
                sessionRepository.getLoginStatusFlow()
            } else {
                // Fallback for other implementations
                flow {
                    emit(sessionRepository.isUserLoggedIn())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating session status flow: ${e.message}", e)
            flowOf(false)
        }
    }
}