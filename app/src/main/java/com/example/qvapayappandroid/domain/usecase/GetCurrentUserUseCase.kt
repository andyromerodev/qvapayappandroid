package com.example.qvapayappandroid.domain.usecase

import com.example.qvapayappandroid.data.model.User
import com.example.qvapayappandroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend fun getCurrentUser(): User? {
        return sessionRepository.getCurrentUser()
    }
    
    fun getCurrentUserFlow(): Flow<User?> {
        return sessionRepository.getCurrentUserFlow()
    }
}