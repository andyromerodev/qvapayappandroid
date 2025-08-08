package com.example.qvapayappandroid.data.datasource

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.model.P2PApplyResponse
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PCreateResponse
import com.example.qvapayappandroid.data.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class P2PDataSourceImpl(
    private val httpClient: HttpClient
) : P2PDataSource {
    
    companion object {
        // Separate throttling per endpoint instead of global
        private var lastP2POffersRequestTime = 0L
        private var lastOfferByIdRequestTime = 0L
        private var lastApplyRequestTime = 0L
        private var lastCreateRequestTime = 0L
        private const val MIN_REQUEST_INTERVAL = 2000L // 2 seconds between requests
    }
    
    override suspend fun getP2POffers(
        filters: P2PFilterRequest,
        accessToken: String?
    ): Result<P2POfferResponse> {
        return try {
            // Rate limiting: ensure minimum interval between requests
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastP2POffersRequestTime
            
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                val waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest
                Log.d("P2PDataSource", "Rate limiting: waiting ${waitTime}ms before request")
                kotlinx.coroutines.delay(waitTime)
            }
            
            lastP2POffersRequestTime = System.currentTimeMillis()
            
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
            val timeSinceLastRequest = currentTime - lastOfferByIdRequestTime
            
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                val waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest
                Log.d("P2PDataSource", "Rate limiting: waiting ${waitTime}ms before request")
                kotlinx.coroutines.delay(waitTime)
            }
            
            lastOfferByIdRequestTime = System.currentTimeMillis()
            
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
    
    override suspend fun applyToP2POffer(
        offerId: String,
        accessToken: String?
    ): Result<P2PApplyResponse> {
        return try {
            // Rate limiting: ensure minimum interval between requests
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastApplyRequestTime
            
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                val waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest
                Log.d("P2PDataSource", "Rate limiting: waiting ${waitTime}ms before request")
                kotlinx.coroutines.delay(waitTime)
            }
            
            lastApplyRequestTime = System.currentTimeMillis()
            
            Log.d("P2PDataSource", "Applying to P2P offer ID: $offerId")
            Log.d("P2PDataSource", "Access token provided: ${accessToken != null}")
            Log.d("P2PDataSource", "Access token value: $accessToken")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.P2P_APPLY}/$offerId"
            Log.d("P2PDataSource", "Full URL: $fullUrl")
            
            // Try different HTTP methods and body configurations
            Log.d("P2PDataSource", "Attempting POST request...")
            
            val response = httpClient.post(fullUrl) {
                // Add authorization header if token is available
                accessToken?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                        append("Accept", "application/json")
                        append("User-Agent", "QvaPay-Android-App")
                        append("X-Requested-With", "XMLHttpRequest")
                    }
                    Log.d("P2PDataSource", "Authorization header set with token: Bearer $token")
                } ?: run {
                    Log.w("P2PDataSource", "No access token provided!")
                }
                
                // Log all request headers
                headers.entries().forEach { (key, values) ->
                    Log.d("P2PDataSource", "Request header: $key = ${values.joinToString(", ")}")
                }
                
                // Set content type and empty JSON body
                contentType(ContentType.Application.Json)
                setBody("{}")
                Log.d("P2PDataSource", "Sending POST with JSON body: {}")
            }
            
            Log.d("P2PDataSource", "Response status: ${response.status}")
            Log.d("P2PDataSource", "Response headers: ${response.headers}")
            Log.d("P2PDataSource", "Content-Type: ${response.headers["Content-Type"]}")
            
            // Get raw response body first
            val rawResponseBody = response.body<String>()
            Log.d("P2PDataSource", "Raw response body: $rawResponseBody")
            Log.d("P2PDataSource", "Response body length: ${rawResponseBody.length}")
            
            // Handle non-success HTTP status codes
            if (response.status.value !in 200..299) {
                Log.e("P2PDataSource", "HTTP Error ${response.status.value}: $rawResponseBody")
                Log.e("P2PDataSource", "Response headers on error: ${response.headers}")
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            // Try to parse as P2PApplyResponse with better error handling
            val json = kotlinx.serialization.json.Json { 
                ignoreUnknownKeys = true 
                isLenient = true 
            }
            val responseBody = json.decodeFromString<P2PApplyResponse>(rawResponseBody)
            Log.d("P2PDataSource", "Applied successfully - Message: ${responseBody.msg}, Offer UUID: ${responseBody.p2p.uuid}")
            
            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e("P2PDataSource", "P2P apply error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun createP2POffer(
        request: P2PCreateRequest,
        accessToken: String?
    ): Result<P2PCreateResponse> {
        return try {
            // Rate limiting: ensure minimum interval between requests
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastCreateRequestTime
            
            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
                val waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest
                Log.d("P2PDataSource", "Rate limiting: waiting ${waitTime}ms before request")
                kotlinx.coroutines.delay(waitTime)
            }
            
            lastCreateRequestTime = System.currentTimeMillis()
            
            Log.d("P2PDataSource", "Creating P2P offer: $request")
            Log.d("P2PDataSource", "Access token provided: ${accessToken != null}")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.P2P_CREATE}"
            Log.d("P2PDataSource", "Full URL: $fullUrl")
            
            val response = httpClient.post(fullUrl) {
                // Add authorization header if token is available
                accessToken?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
                
                // Set content type for JSON
                contentType(ContentType.Application.Json)
                
                // Set request body
                setBody(request)
            }
            
            Log.d("P2PDataSource", "Response status: ${response.status}")
            
            // Get raw response body first
            val rawResponseBody = response.body<String>()
            Log.d("P2PDataSource", "Raw response body: $rawResponseBody")
            
            // Handle non-success HTTP status codes
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            // Try to parse as P2PCreateResponse with better error handling
            val json = kotlinx.serialization.json.Json { 
                ignoreUnknownKeys = true 
                isLenient = true 
            }
            val responseBody = json.decodeFromString<P2PCreateResponse>(rawResponseBody)
            Log.d("P2PDataSource", "Offer created successfully - Message: ${responseBody.msg}, Offer UUID: ${responseBody.p2p.uuid}")
            
            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e("P2PDataSource", "P2P create error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getMyP2POffers(
        accessToken: String,
        page: Int?
    ): Result<P2POfferResponse> {
        return try {
            // No throttling for getMyP2POffers - ViewModel handles pagination throttling
            
            Log.d("P2PDataSource", "Getting my P2P offers with page: $page")
            Log.d("P2PDataSource", "Access token provided: ${accessToken.isNotEmpty()}")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.P2P_MY}"
            Log.d("P2PDataSource", "Full URL: $fullUrl")
            
            val response = httpClient.get(fullUrl) {
                // Add authorization header
                headers {
                    append("Authorization", "Bearer $accessToken")
                }
                
                // Add page parameter if provided
                page?.let { parameter("page", it.toString()) }
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
            Log.d("P2PDataSource", "Parsed my offers - Total offers: ${responseBody.total}, Current page: ${responseBody.currentPage}")
            
            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e("P2PDataSource", "My P2P offers error: ${e.message}", e)
            Result.failure(e)
        }
    }
}