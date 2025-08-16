package com.example.qvapayappandroid.data.repository

import com.example.qvapayappandroid.data.datasource.SessionLocalDataSource
import com.example.qvapayappandroid.data.database.entities.SessionEntity
import com.example.qvapayappandroid.data.database.entities.UserEntity
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.data.model.User
import com.example.qvapayappandroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(
    private val sessionLocalDataSource: SessionLocalDataSource
) : SessionRepository {
    
    override suspend fun saveLoginSession(loginResponse: LoginResponse): Result<Unit> {
        return try {
            val sessionEntity = SessionEntity(
                accessToken = loginResponse.accessToken,
                tokenType = loginResponse.tokenType,
                userUuid = loginResponse.me.uuid
            )
            
            val userEntity = loginResponse.me.toUserEntity()
            
            sessionLocalDataSource.saveSession(sessionEntity, userEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        return sessionLocalDataSource.getCurrentUser()?.toUser()
    }
    
    override fun getCurrentUserFlow(): Flow<User?> {
        return sessionLocalDataSource.getCurrentUserFlow().map { it?.toUser() }
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        return sessionLocalDataSource.getActiveSession() != null
    }
    
    override suspend fun getAccessToken(): String? {
        return sessionLocalDataSource.getActiveSession()?.accessToken
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            sessionLocalDataSource.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserData(user: User): Result<Unit> {
        return try {
            val userEntity = user.toUserEntity()
            sessionLocalDataSource.updateUser(userEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

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