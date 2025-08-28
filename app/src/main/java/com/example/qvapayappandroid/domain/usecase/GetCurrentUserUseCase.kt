package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.User
import com.example.qvapayappandroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * GetCurrentUserUseCase optimized for DataStore.
 * Provides robust user data retrieval with error handling and reactive flows.
 */
class GetCurrentUserUseCase(
    private val sessionRepository: SessionRepository
) {
    companion object {
        private const val TAG = "GetCurrentUserUseCase"
    }
    
    /**
     * Get current user (one-time fetch)
     */
    suspend fun getCurrentUser(): User? {
        return try {
            val user = sessionRepository.getCurrentUser()
            Log.d(TAG, "👤 Current user retrieved: ${user?.username ?: "null"}")
            user
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting current user: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get current user as reactive Flow (continuous updates)
     */
    fun getCurrentUserFlow(): Flow<User?> {
        return sessionRepository.getCurrentUserFlow()
            .catch { e ->
                Log.e(TAG, "❌ Error in user flow: ${e.message}", e)
                emit(null)
            }
    }
    
    /**
     * Get user with session data combined (DataStore-specific enhancement)
     */
    fun getUserWithSessionFlow(): Flow<Pair<User?, Boolean>> {
        return try {
            // Cast to DataStore implementation to access enhanced flows
            if (sessionRepository is com.example.qvapayappandroid.data.repository.SessionRepositoryDataStoreImpl) {
                sessionRepository.getUserWithSessionFlow()
                    .map { (user, sessionPrefs) ->
                        user to sessionPrefs.isLoggedIn
                    }
                    .catch { e ->
                        Log.e(TAG, "❌ Error in user+session flow: ${e.message}", e)
                        emit(null to false)
                    }
            } else {
                // Fallback for other implementations
                combine(
                    getCurrentUserFlow(),
                    flow { emit(sessionRepository.isUserLoggedIn()) }
                ) { user, isLoggedIn ->
                    user to isLoggedIn
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating user+session flow: ${e.message}", e)
            flowOf(null to false)
        }
    }
}