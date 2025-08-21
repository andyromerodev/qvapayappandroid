package com.example.qvapayappandroid.data.repository

import com.example.qvapayappandroid.data.database.entities.UserEntity
import com.example.qvapayappandroid.data.model.User

// Extension functions para mapear entre User y UserEntity
private fun User.toUserEntity(): UserEntity {
    return UserEntity(
        uuid = uuid,
        username = username,
        name = name,
        lastname = lastname,
        bio = bio,
        country = country,
        balance = balance,
        pendingBalance = pendingBalance,
        satoshis = satoshis,
        phone = phone,
        phoneVerified = phoneVerified,
        twitter = twitter,
        kyc = kyc,
        vip = vip,
        goldenCheck = goldenCheck,
        goldenExpire = goldenExpire,
        p2pEnabled = p2pEnabled,
        telegramId = telegramId,
        role = role,
        nameVerified = nameVerified,
        coverPhotoUrl = coverPhotoUrl,
        profilePhotoUrl = profilePhotoUrl,
        averageRating = averageRating,
        twoFactorSecret = twoFactorSecret,
        twoFactorResetCode = twoFactorResetCode,
        phoneRequestId = phoneRequestId,
        canWithdraw = canWithdraw,
        canDeposit = canDeposit,
        canTransfer = canTransfer,
        canBuy = canBuy,
        canSell = canSell
    )
}

private fun UserEntity.toUser(): User {
    return User(
        uuid = uuid,
        username = username,
        name = name,
        lastname = lastname,
        bio = bio,
        country = country,
        balance = balance,
        pendingBalance = pendingBalance,
        satoshis = satoshis,
        phone = phone,
        phoneVerified = phoneVerified,
        twitter = twitter,
        kyc = kyc,
        vip = vip,
        goldenCheck = goldenCheck,
        goldenExpire = goldenExpire,
        p2pEnabled = p2pEnabled,
        telegramId = telegramId,
        role = role,
        nameVerified = nameVerified,
        coverPhotoUrl = coverPhotoUrl,
        profilePhotoUrl = profilePhotoUrl,
        averageRating = averageRating,
        twoFactorSecret = twoFactorSecret,
        twoFactorResetCode = twoFactorResetCode,
        phoneRequestId = phoneRequestId,
        canWithdraw = canWithdraw,
        canDeposit = canDeposit,
        canTransfer = canTransfer,
        canBuy = canBuy,
        canSell = canSell
    )
}