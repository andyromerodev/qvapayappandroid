package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.database.dao.SessionDao
import com.example.qvapayappandroid.data.database.dao.UserDao
import com.example.qvapayappandroid.data.database.entities.SessionEntity
import com.example.qvapayappandroid.data.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionLocalDataSourceImpl(
    private val sessionDao: SessionDao,
    private val userDao: UserDao
) : SessionLocalDataSource {
    
    override suspend fun saveSession(session: SessionEntity, user: UserEntity) {
        userDao.insertUser(user)
        sessionDao.saveNewSession(session)
    }
    
    override suspend fun getActiveSession(): SessionEntity? {
        return sessionDao.getActiveSession()
    }
    
    override fun getActiveSessionFlow(): Flow<SessionEntity?> {
        return sessionDao.getActiveSessionFlow()
    }
    
    override suspend fun getCurrentUser(): UserEntity? {
        val session = getActiveSession()
        return session?.let { userDao.getUserByUuid(it.userUuid) }
    }
    
    override fun getCurrentUserFlow(): Flow<UserEntity?> {
        return getActiveSessionFlow().map { session ->
            session?.let { userDao.getUserByUuid(it.userUuid) }
        }
    }
    
    override suspend fun clearSession() {
        sessionDao.deleteAllSessions()
        userDao.deleteAllUsers()
    }
    
    override suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user.copy(updatedAt = System.currentTimeMillis()))
    }
}