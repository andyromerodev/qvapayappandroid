package com.example.qvapayappandroid.data.database.dao

import androidx.room.*
import com.example.qvapayappandroid.data.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE uuid = :uuid")
    suspend fun getUserByUuid(uuid: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE uuid = :uuid")
    fun getUserByUuidFlow(uuid: String): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE uuid = :uuid")
    suspend fun deleteUserByUuid(uuid: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}