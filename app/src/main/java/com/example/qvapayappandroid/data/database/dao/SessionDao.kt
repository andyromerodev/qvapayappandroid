package com.example.qvapayappandroid.data.database.dao

import androidx.room.*
import com.example.qvapayappandroid.data.database.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    
    @Query("SELECT * FROM sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?
    
    @Query("SELECT * FROM sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<SessionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)
    
    @Query("UPDATE sessions SET isActive = 0")
    suspend fun deactivateAllSessions()
    
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()
    
    @Transaction
    suspend fun saveNewSession(session: SessionEntity) {
        deactivateAllSessions()
        insertSession(session)
    }
}