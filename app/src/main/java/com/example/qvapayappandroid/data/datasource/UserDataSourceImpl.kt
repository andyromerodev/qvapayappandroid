package com.example.qvapayappandroid.data.datasource

import android.util.Log
import com.example.qvapayappandroid.data.model.User
import com.example.qvapayappandroid.data.network.ApiConfig
import com.example.qvapayappandroid.data.network.HttpClientFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class UserDataSourceImpl(
    private val httpClient: HttpClient = HttpClientFactory.create()
) : UserDataSource {
    
    companion object {
        private const val TAG = "UserDataSource"
    }
    
    override suspend fun getCurrentUserProfile(accessToken: String): Result<User> {
        return try {
            Log.d(TAG, "Fetching current user profile from server")
            Log.d(TAG, "Base URL: ${ApiConfig.BASE_URL}")
            Log.d(TAG, "Endpoint: ${ApiConfig.Endpoints.USER_PROFILE}")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.USER_PROFILE}"
            Log.d(TAG, "Full URL: $fullUrl")
            
            val response = httpClient.get(fullUrl) {
                header("Authorization", "Bearer $accessToken")
                contentType(ContentType.Application.Json)
            }
            
            Log.d(TAG, "Response status: ${response.status}")
            
            // Get raw response body first
            val rawResponseBody = response.body<String>()
            Log.d(TAG, "Raw response body: $rawResponseBody")
            
            // Handle non-success HTTP status codes
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            // Parse response as User
            val json = Json { ignoreUnknownKeys = true }
            val user = json.decodeFromString<User>(rawResponseBody)
            Log.d(TAG, "Parsed user profile: ${user.username}")
            
            Result.success(user)
            
        } catch (e: Exception) {
            Log.e(TAG, "User profile fetch error: ${e.message}", e)
            Result.failure(e)
        }
    }
}