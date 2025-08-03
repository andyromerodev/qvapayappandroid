package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.SessionRepository

class LogoutUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return sessionRepository.logout()
    }
}