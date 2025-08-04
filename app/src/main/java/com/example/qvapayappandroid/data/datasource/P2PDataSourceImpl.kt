package com.example.qvapayappandroid.data.datasource

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class P2PDataSourceImpl(
    private val httpClient: HttpClient
) : P2PDataSource {
    
    companion object {
        private var lastRequestTime = 0L
        private const val MIN_REQUEST_INTERVAL = 2000L // 2 seconds between requests
    }
    
    override suspend fun getP2POffers(
        filters: P2PFilterRequest,
        accessToken: String?
    ): Result<P2POfferResponse> {
        return try {
            // Rate limiting: ensure minimum interval between requests
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastRequestTime
            
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                val waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest
                Log.d("P2PDataSource", "Rate limiting: waiting ${waitTime}ms before request")
                kotlinx.coroutines.delay(waitTime)
            }
            
            lastRequestTime = System.currentTimeMillis()
            
            Log.d("P2PDataSource", "Getting P2P offers with filters: $filters")
            Log.d("P2PDataSource", "Access token provided: ${accessToken != null}")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.P2P_INDEX}"
            Log.d("P2PDataSource", "Full URL: $fullUrl")
            
            val response = httpClient.get(fullUrl) {
                // Add authorization header if token is available
                accessToken?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
                
                // Add query parameters based on filters
                filters.type?.let { parameter("type", it) }
                filters.min?.let { parameter("min", it.toString()) }
                filters.max?.let { parameter("max", it.toString()) }
                filters.coin?.let { parameter("coin", it) }
                filters.my?.let { if (it) parameter("my", "1") }
                filters.vip?.let { if (it) parameter("vip", "1") }
                filters.page?.let { parameter("page", it.toString()) }
                filters.perPage?.let { parameter("per_page", it.toString()) }
            }
            
            Log.d("P2PDataSource", "Response status: ${response.status}")
            
            // Get raw response body first
            val rawResponseBody = response.body<String>()
            Log.d("P2PDataSource", "Raw response body: $rawResponseBody")
            
            // Handle non-success HTTP status codes
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            // Try to parse as P2POfferResponse with better error handling
            val json = kotlinx.serialization.json.Json { 
                ignoreUnknownKeys = true 
                isLenient = true 
            }
            val responseBody = json.decodeFromString<P2POfferResponse>(rawResponseBody)
            Log.d("P2PDataSource", "Parsed response - Total offers: ${responseBody.total}, Current page: ${responseBody.currentPage}")
            
            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e("P2PDataSource", "P2P offers error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getP2POfferById(
        offerId: String,
        accessToken: String?
    ): Result<P2POffer> {
        return try {
            // Rate limiting: ensure minimum interval between requests
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastRequestTime
            
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                val waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest
                Log.d("P2PDataSource", "Rate limiting: waiting ${waitTime}ms before request")
                kotlinx.coroutines.delay(waitTime)
            }
            
            lastRequestTime = System.currentTimeMillis()
            
            Log.d("P2PDataSource", "Getting P2P offer by ID: $offerId")
            Log.d("P2PDataSource", "Access token provided: ${accessToken != null}")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.P2P_OFFER}/$offerId"
            Log.d("P2PDataSource", "Full URL: $fullUrl")
            
            val response = httpClient.get(fullUrl) {
                // Add authorization header if token is available
                accessToken?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
            }
            
            Log.d("P2PDataSource", "Response status: ${response.status}")
            
            // Get raw response body first
            val rawResponseBody = response.body<String>()
            Log.d("P2PDataSource", "Raw response body: $rawResponseBody")
            
            // Handle non-success HTTP status codes
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            // Try to parse as P2POffer with better error handling
            val json = kotlinx.serialization.json.Json { 
                ignoreUnknownKeys = true 
                isLenient = true 
            }
            val responseBody = json.decodeFromString<P2POffer>(rawResponseBody)
            Log.d("P2PDataSource", "Parsed offer - UUID: ${responseBody.uuid}, Type: ${responseBody.type}")
            
            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e("P2PDataSource", "P2P offer by ID error: ${e.message}", e)
            Result.failure(e)
        }
    }
}