package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.domain.repository.AuthRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class AuthRepositoryImpl(
    private val loginDataSource: LoginDataSource,
    private val sessionRepository: SessionRepository
) : AuthRepository {
    
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return loginDataSource.login(request).fold(
            onSuccess = { loginResponse ->
                Log.d("AuthRepository", "Login successful, saving session...")
                
                // Guardar automáticamente la sesión en BD
                sessionRepository.saveLoginSession(loginResponse).fold(
                    onSuccess = {
                        Log.d("AuthRepository", "Session saved successfully")
                        Result.success(loginResponse)
                    },
                    onFailure = { sessionError ->
                        Log.e("AuthRepository", "Failed to save session: ${sessionError.message}")
                        // Aunque falle guardar la sesión, el login fue exitoso
                        Result.success(loginResponse)
                    }
                )
            },
            onFailure = { loginError ->
                Log.e("AuthRepository", "Login failed: ${loginError.message}")
                Result.failure(loginError)
            }
        )
    }
}