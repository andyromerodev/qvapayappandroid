package com.example.qvapayappandroid.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uuid: String,
    val username: String,
    val name: String,
    val lastname: String,
    val bio: String?,
    val country: String?,
    val balance: Double,
    val pendingBalance: Double,
    val satoshis: Int,
    val phone: String?,
    val phoneVerified: Int,
    val twitter: String?,
    val kyc: Int,
    val vip: Int,
    val goldenCheck: Int,
    val goldenExpire: String?,
    val p2pEnabled: Int,
    val telegramId: Long?,
    val role: String,
    val nameVerified: String,
    val coverPhotoUrl: String,
    val profilePhotoUrl: String,
    val averageRating: String,
    val twoFactorSecret: String?,
    val twoFactorResetCode: String?,
    val phoneRequestId: String?,
    val canWithdraw: Int?,
    val canDeposit: Int?,
    val canTransfer: Int?,
    val canBuy: Int?,
    val canSell: Int?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)