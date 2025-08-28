package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.domain.repository.AuthRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

/**
 * AuthRepository implementation using SessionRepository for proper data persistence.
 * Uses SessionRepository to handle both DataStore and Room data consistently.
 */
class AuthRepositoryImpl(
    private val loginDataSource: LoginDataSource,
    private val sessionRepository: SessionRepository
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d(TAG, "🔐 Starting login process for user: ${request.email}")
            
            loginDataSource.login(request).fold(
                onSuccess = { loginResponse ->
                    Log.d(TAG, "✅ Login successful, saving session via SessionRepository...")
                    
                    // Save session using SessionRepository (saves to both DataStore and Room)
                    sessionRepository.saveLoginSession(loginResponse).fold(
                        onSuccess = {
                            Log.d(TAG, "✅ Session saved successfully")
                        },
                        onFailure = { sessionError ->
                            Log.e(TAG, "❌ Error saving session: ${sessionError.message}")
                            throw sessionError
                        }
                    )
                    
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
     * Performs logout using SessionRepository for consistent data clearing.
     */
    override suspend fun logout(): Result<Unit> {
        return try {
            Log.d(TAG, "🚪 Starting logout process via SessionRepository...")
            
            sessionRepository.logout()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during logout: ${e.message}", e)
            Result.failure(e)
        }
    }
}