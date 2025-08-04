package com.example.qvapayappandroid.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Solo tendremos un registro de configuraciones
    val theme: String = "Sistema",
    val language: String = "Español",
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)