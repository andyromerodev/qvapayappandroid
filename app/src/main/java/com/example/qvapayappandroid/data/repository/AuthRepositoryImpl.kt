package com.example.qvapayappandroid.data.repository

import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val loginDataSource: LoginDataSource
) : AuthRepository {
    
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return loginDataSource.login(request)
    }
}