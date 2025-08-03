package com.example.qvapayappandroid.data.datasource

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.network.ApiConfig
import com.example.qvapayappandroid.domain.repository.SessionRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class P2PDataSourceImpl(
    private val httpClient: HttpClient,
    private val sessionRepository: SessionRepository
) : P2PDataSource {
    
    override suspend fun getP2POffers(filters: P2PFilterRequest): Result<P2POfferResponse> {
        return try {
            Log.d("P2PDataSource", "Getting P2P offers with filters: $filters")
            
            // Get access token from session
            val accessToken = sessionRepository.getAccessToken()
            Log.d("P2PDataSource", "Access token available: ${accessToken != null}")
            
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
            }
            
            Log.d("P2PDataSource", "Response status: ${response.status}")
            
            // Get raw response body first
            val rawResponseBody = response.body<String>()
            Log.d("P2PDataSource", "Raw response body (first 500 chars): ${rawResponseBody.take(500)}")
            
            // Handle non-success HTTP status codes
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            // Parse as P2POfferResponse
            val responseBody = response.body<P2POfferResponse>()
            Log.d("P2PDataSource", "Parsed response - Total offers: ${responseBody.total}, Current page: ${responseBody.currentPage}")
            
            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e("P2PDataSource", "P2P offers error: ${e.message}", e)
            Result.failure(e)
        }
    }
}