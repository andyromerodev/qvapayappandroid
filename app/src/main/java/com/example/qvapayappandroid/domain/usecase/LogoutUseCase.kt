package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.AuthRepository

/**
 * LogoutUseCase optimized for DataStore.
 * Uses AuthRepository for consistent authentication flow management.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}