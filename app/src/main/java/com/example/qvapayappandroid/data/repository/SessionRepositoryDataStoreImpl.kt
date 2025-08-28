package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.database.dao.UserDao
import com.example.qvapayappandroid.data.database.entities.UserEntity
import com.example.qvapayappandroid.data.datastore.SessionPreferencesRepository
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.data.model.User
import com.example.qvapayappandroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Optimized SessionRepository implementation with DataStore as primary source.
 * Uses DataStore for session/tokens and Room for complex user profile data.
 * This implementation has been refined to minimize hybrid complexity while maintaining data integrity.
 */
class SessionRepositoryDataStoreImpl(
    private val sessionPreferencesRepository: SessionPreferencesRepository,
    private val userDao: UserDao
) : SessionRepository {

    companion object {
        private const val TAG = "SessionRepositoryDS"
    }

    override suspend fun saveLoginSession(loginResponse: LoginResponse): Result<Unit> {
        return try {
            Log.d(TAG, "💾 Saving login session for user: ${loginResponse.me.username}")
            
            // Save session tokens in DataStore (primary source)
            sessionPreferencesRepository.saveSession(
                userId = loginResponse.me.uuid, // Use UUID as primary ID
                userUuid = loginResponse.me.uuid,
                username = loginResponse.me.username,
                accessToken = loginResponse.accessToken,
                refreshToken = "" // No refresh token in current LoginResponse
            )
            
            // Save user profile in Room (complex data backup)
            val userEntity = loginResponse.me.toUserEntity()
            userDao.insertUser(userEntity)
            
            Log.d(TAG, "✅ Login session saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving login session: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            // Get user UUID from DataStore
            val userUuid = sessionPreferencesRepository.getUserUuid().first()
            if (userUuid.isEmpty()) {
                null
            } else {
                // Get user data from Room
                userDao.getUserByUuid(userUuid)?.toUser()
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> {
        return sessionPreferencesRepository.getUserUuid()
            .map { userUuid ->
                if (userUuid.isEmpty()) {
                    null
                } else {
                    userDao.getUserByUuid(userUuid)?.toUser()
                }
            }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return try {
            val sessionPrefs = sessionPreferencesRepository.sessionPreferencesFlow.first()
            val isLoggedIn = sessionPrefs.isLoggedIn && sessionPrefs.accessToken.isNotEmpty()
            Log.d(TAG, "🔍 User logged in status: $isLoggedIn")
            isLoggedIn
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking login status: ${e.message}", e)
            false
        }
    }

    override suspend fun getAccessToken(): String? {
        return try {
            val accessToken = sessionPreferencesRepository.getAccessToken().first()
            val token = if (accessToken.isEmpty()) null else accessToken
            Log.d(TAG, "🔑 Access token retrieved: ${if (token != null) "exists" else "null"}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting access token: ${e.message}", e)
            null
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            Log.d(TAG, "🚪 Starting logout process...")
            
            // Clear session data from DataStore (primary)
            sessionPreferencesRepository.clearSession()
            
            // Clear user data from Room (backup)
            userDao.deleteAllUsers()
            
            Log.d(TAG, "✅ Logout completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during logout: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserData(user: User): Result<Unit> {
        return try {
            // Update user profile in Room
            val userEntity = user.toUserEntity()
            userDao.updateUser(userEntity.copy(updatedAt = System.currentTimeMillis()))
            
            // Update username in DataStore if changed
            sessionPreferencesRepository.sessionPreferencesFlow.first().let { sessionPrefs ->
                if (sessionPrefs.username != user.username) {
                    sessionPreferencesRepository.saveSession(
                        userId = sessionPrefs.userId,
                        userUuid = user.uuid,
                        username = user.username,
                        accessToken = sessionPrefs.accessToken,
                        refreshToken = sessionPrefs.refreshToken
                    )
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * DataStore-specific methods for session management
     */
    
    /**
     * Update only access token (useful for token refresh)
     */
    suspend fun updateAccessToken(accessToken: String): Result<Unit> {
        return try {
            sessionPreferencesRepository.updateAccessToken(accessToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get session preferences flow
     */
    fun getSessionPreferencesFlow(): Flow<SessionPreferencesRepository.SessionPreferences> {
        return sessionPreferencesRepository.sessionPreferencesFlow
    }

    /**
     * Get login status flow
     */
    fun getLoginStatusFlow(): Flow<Boolean> {
        return sessionPreferencesRepository.isLoggedIn()
    }

    /**
     * Get combined user and session data flow
     */
    fun getUserWithSessionFlow(): Flow<Pair<User?, SessionPreferencesRepository.SessionPreferences>> {
        return combine(
            getCurrentUserFlow(),
            sessionPreferencesRepository.sessionPreferencesFlow
        ) { user, sessionPrefs ->
            user to sessionPrefs
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