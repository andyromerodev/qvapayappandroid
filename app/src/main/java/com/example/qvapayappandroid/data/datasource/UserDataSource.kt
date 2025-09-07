package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.model.User

interface UserDataSource {
    suspend fun getCurrentUserProfile(accessToken: String): Result<User>
}