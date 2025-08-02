package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse

interface LoginDataSource {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
}