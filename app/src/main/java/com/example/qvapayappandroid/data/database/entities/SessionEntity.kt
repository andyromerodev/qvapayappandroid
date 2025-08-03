package com.example.qvapayappandroid.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: Int = 1, // Only one active session
    val accessToken: String,
    val tokenType: String,
    val userUuid: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)