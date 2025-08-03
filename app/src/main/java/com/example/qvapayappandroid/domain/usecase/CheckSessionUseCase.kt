package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.domain.repository.SessionRepository

class CheckSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Boolean {
        return sessionRepository.isUserLoggedIn()
    }
}