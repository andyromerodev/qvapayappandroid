package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        code: String
    ): Result<LoginResponse> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email is required"))
        }
        
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password is required"))
        }
        
//        if (code.isBlank()) {
//            return Result.failure(IllegalArgumentException("Code is required"))
//        }
        
        val request = LoginRequest(
            email = email.trim(),
            password = password,
            code = code.trim()
        )
        
        return authRepository.login(request)
    }
}