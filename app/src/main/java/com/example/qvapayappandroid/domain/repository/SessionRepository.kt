package com.example.qvapayappandroid.domain.repository

import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.data.model.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun saveLoginSession(loginResponse: LoginResponse): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun isUserLoggedIn(): Boolean
    suspend fun getAccessToken(): String?
    suspend fun logout(): Result<Unit>
    suspend fun updateUserData(user: User): Result<Unit>
    suspend fun refreshUserProfile(): Result<User>
}