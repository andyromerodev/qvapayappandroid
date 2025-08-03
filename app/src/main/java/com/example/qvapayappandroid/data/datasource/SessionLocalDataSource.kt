package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.database.entities.SessionEntity
import com.example.qvapayappandroid.data.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

interface SessionLocalDataSource {
    suspend fun saveSession(session: SessionEntity, user: UserEntity)
    suspend fun getActiveSession(): SessionEntity?
    fun getActiveSessionFlow(): Flow<SessionEntity?>
    suspend fun getCurrentUser(): UserEntity?
    fun getCurrentUserFlow(): Flow<UserEntity?>
    suspend fun clearSession()
    suspend fun updateUser(user: UserEntity)
}