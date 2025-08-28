package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.domain.repository.AuthRepository

/**
 * LoginUseCase optimized for DataStore.
 * Handles login validation and delegates to AuthRepository for session management.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "LoginUseCase"
    }
    
    suspend operator fun invoke(
        email: String,
        password: String,
        code: String
    ): Result<LoginResponse> {
        Log.d(TAG, "🔐 Starting login process for email: ${email.take(3)}***")
        
        // Input validation
        if (email.isBlank()) {
            Log.w(TAG, "❌ Login failed: Email is required")
            return Result.failure(IllegalArgumentException("Email is required"))
        }
        
        if (password.isBlank()) {
            Log.w(TAG, "❌ Login failed: Password is required")
            return Result.failure(IllegalArgumentException("Password is required"))
        }
        
        // Code validation is commented out - seems to be optional
//        if (code.isBlank()) {
//            return Result.failure(IllegalArgumentException("Code is required"))
//        }
        
        val request = LoginRequest(
            email = email.trim(),
            password = password,
            code = code.trim()
        )
        
        return try {
            val result = authRepository.login(request)
            if (result.isSuccess) {
                Log.d(TAG, "✅ Login successful")
            } else {
                Log.w(TAG, "❌ Login failed: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during login: ${e.message}", e)
            Result.failure(e)
        }
    }
}