package com.example.qvapayappandroid.data.datasource

import android.util.Log
import com.example.qvapayappandroid.data.model.LoginRequest
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.data.network.ApiConfig
import com.example.qvapayappandroid.data.network.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LoginDataSourceImpl(
    private val httpClient: HttpClient = HttpClientFactory.create()
) : LoginDataSource {
    
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            Log.d("LoginDataSource", "Sending login request: $request")
            Log.d("LoginDataSource", "Base URL: ${ApiConfig.BASE_URL}")
            Log.d("LoginDataSource", "Endpoint: ${ApiConfig.Endpoints.AUTH_LOGIN}")
            
            // Test manual serialization first
            val json = Json { ignoreUnknownKeys = true }
            val jsonString = json.encodeToString(request)
            Log.d("LoginDataSource", "Manual JSON serialization: $jsonString")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUTH_LOGIN}"
            Log.d("LoginDataSource", "Full URL: $fullUrl")
            
            val response = httpClient.post(fullUrl) {
                contentType(ContentType.Application.Json)
                setBody(jsonString)
            }
            
            Log.d("LoginDataSource", "Response status: ${response.status}")
            
            // Get raw response body first
            val rawResponseBody = response.body<String>()
            Log.d("LoginDataSource", "Raw response body: $rawResponseBody")
            
            // Handle non-success HTTP status codes
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            // Try to parse as LoginResponse
            val responseBody = json.decodeFromString<LoginResponse>(rawResponseBody)
            Log.d("LoginDataSource", "Parsed response: $responseBody")
            
            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e("LoginDataSource", "Login error: ${e.message}", e)
            Result.failure(e)
        }
    }
}