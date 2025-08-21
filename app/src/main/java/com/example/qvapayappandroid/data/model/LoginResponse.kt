package com.example.qvapayappandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("accessToken")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    val me: User
)

@Serializable
data class User(
    val uuid: String,
    val username: String,
    val name: String,
    val lastname: String,
    @SerialName("two_factor_secret")
    val twoFactorSecret: String? = null,
    @SerialName("two_factor_reset_code")
    val twoFactorResetCode: String? = null,
    val bio: String? = null,
    val country: String? = null,
    val balance: Double,
    @SerialName("pending_balance")
    val pendingBalance: Double,
    val satoshis: Int,
    val phone: String? = null,
    @SerialName("phone_request_id")
    val phoneRequestId: String? = null,
    @SerialName("phone_verified")
    val phoneVerified: Int,
    val twitter: String? = null,
    val kyc: Int,
    val vip: Int,
    @SerialName("golden_check")
    val goldenCheck: Int,
    @SerialName("golden_expire")
    val goldenExpire: String? = null,
    @SerialName("p2p_enabled")
    val p2pEnabled: Int,
    @SerialName("telegram_id")
    val telegramId: Long? = null,
    val role: String,
    @SerialName("can_withdraw")
    val canWithdraw: Int? = null,
    @SerialName("can_deposit")
    val canDeposit: Int? = null,
    @SerialName("can_transfer")
    val canTransfer: Int? = null,
    @SerialName("can_buy")
    val canBuy: Int? = null,
    @SerialName("can_sell")
    val canSell: Int? = null,
    @SerialName("name_verified")
    val nameVerified: String,
    @SerialName("cover_photo_url")
    val coverPhotoUrl: String,
    @SerialName("profile_photo_url")
    val profilePhotoUrl: String,
    @SerialName("average_rating")
    val averageRating: String
)