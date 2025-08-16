package com.example.qvapayappandroid.data.datasource

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.model.P2PApplyResponse
import com.example.qvapayappandroid.data.model.P2PCancelResponse
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PCreateResponse
import com.example.qvapayappandroid.data.network.ApiConfig
import com.example.qvapayappandroid.domain.throttling.ThrottlingManager
import com.example.qvapayappandroid.domain.throttling.ThrottlingConfig
import com.example.qvapayappandroid.domain.throttling.ThrottlingOperations
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class P2PDataSourceImpl(
    private val httpClient: HttpClient,
    private val throttlingManager: ThrottlingManager
) : P2PDataSource {
    
    companion object {
        private const val TAG = "P2PDataSource"
    }
    
    init {
        Log.d(TAG, "🔧 P2PDataSourceImpl initialized - configuring throttling")
        // Configurar throttling para las operaciones P2P
        configureThrottling()
    }
    
    private fun configureThrottling() {
        Log.d(TAG, "⚙️ configureThrottling() - setting up P2P operation throttling")
        
        kotlinx.coroutines.runBlocking {
            // Configurar throttling específico para cada operación P2P
            Log.d(TAG, "   • Configuring P2P_GET_OFFERS: 15000ms interval")
            throttlingManager.configureOperation(
                ThrottlingOperations.P2P_GET_OFFERS,
                ThrottlingConfig.DEFAULT_API_CONFIG // 15 segundos para listados (aumentado por filtros múltiples)
            )
            
            Log.d(TAG, "   • Configuring P2P_GET_OFFER_BY_ID: 5000ms interval")
            throttlingManager.configureOperation(
                ThrottlingOperations.P2P_GET_OFFER_BY_ID,
                ThrottlingConfig.DEFAULT_API_CONFIG // 5 segundos para detalles
            )
            
            Log.d(TAG, "   • Configuring P2P_CREATE_OFFER: CREATE_OPERATIONS_CONFIG")
            throttlingManager.configureOperation(
                ThrottlingOperations.P2P_CREATE_OFFER, 
                ThrottlingConfig.DEFAULT_API_CONFIG // 10 segundos para creación
            )
            
            Log.d(TAG, "   • Configuring P2P_APPLY_TO_OFFER: CREATE_OPERATIONS_CONFIG")
            throttlingManager.configureOperation(
                ThrottlingOperations.P2P_APPLY_TO_OFFER, 
                ThrottlingConfig.DEFAULT_API_CONFIG // 10 segundos para aplicar
            )
            
            Log.d(TAG, "   • Configuring P2P_CANCEL_OFFER: 5000ms interval")
            throttlingManager.configureOperation(
                ThrottlingOperations.P2P_CANCEL_OFFER, 
                ThrottlingConfig.DEFAULT_API_CONFIG // 5 segundos para cancelar
            )
            
            Log.d(TAG, "   • Configuring P2P_GET_MY_OFFERS: 3000ms interval")
            throttlingManager.configureOperation(
                ThrottlingOperations.P2P_GET_MY_OFFERS,
                ThrottlingConfig.DEFAULT_API_CONFIG // 3 segundos para mis ofertas
            )
            
            // Configure global API throttling to prevent 429 errors
            Log.d(TAG, "   • Configuring Global API throttling: 15000ms interval")
            throttlingManager.configureGlobalApi(
                ThrottlingConfig.DEFAULT_API_CONFIG // 15 segundos entre cualquier llamada a la API
            )
            
            Log.d(TAG, "✅ All P2P throttling configurations completed")
        }
    }
    
    override suspend fun getP2POffers(
        filters: P2PFilterRequest,
        accessToken: String?
    ): Result<P2POfferResponse> {
        return try {
            Log.d(TAG, "📋 getP2POffers() called with filters: $filters")
            
            // Verificar throttling usando el manager
            Log.d(TAG, "🔍 Checking throttling for P2P_GET_OFFERS operation")
            val throttlingResult = throttlingManager.canExecute(ThrottlingOperations.P2P_GET_OFFERS)
            
            if (!throttlingResult.canExecute) {
                Log.d(TAG, "⏸️ THROTTLED - waiting ${throttlingResult.remainingTimeMs}ms before request")
                Log.d(TAG, "   • Reason: ${throttlingResult.reason}")
                kotlinx.coroutines.delay(throttlingResult.remainingTimeMs)
                Log.d(TAG, "✅ Wait completed - proceeding with request")
            } else {
                Log.d(TAG, "✅ Not throttled - proceeding immediately")
            }
            
            // Registrar la ejecución
            Log.d(TAG, "📝 Recording execution for P2P_GET_OFFERS")
            throttlingManager.recordExecution(ThrottlingOperations.P2P_GET_OFFERS)
            
            Log.d(TAG, "🌐 Preparing HTTP request")
            Log.d(TAG, "   • Access token provided: ${accessToken != null}")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.P2P_INDEX}"
            Log.d(TAG, "   • Full URL: $fullUrl")
            
            val startTime = System.currentTimeMillis()
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
            
            val endTime = System.currentTimeMillis()
            val requestDuration = endTime - startTime
            
            Log.d(TAG, "✅ HTTP request completed")
            Log.d(TAG, "   • Response status: ${response.status}")
            Log.d(TAG, "   • Request duration: ${requestDuration}ms")
            
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
            
            Log.d(TAG, "✅ Response parsing successful")
            Log.d(TAG, "   • Total offers: ${responseBody.total}")
            Log.d(TAG, "   • Current page: ${responseBody.currentPage}")
            Log.d(TAG, "   • Offers in response: ${responseBody.data.size}")
            
            // Crear un conjunto único de monedas para evitar duplicados
            val uniqueCoins = responseBody.data
                .mapNotNull { it.coinData }
                .distinctBy { it.coinsCategoriesId }
                .sortedBy { it.coinsCategoriesId }
            
            Log.d(TAG, "   • Unique coins found: ${uniqueCoins.size}")

            Result.success(responseBody)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ getP2POffers() failed with exception")
            Log.e(TAG, "   • Exception type: ${e::class.simpleName}")
            Log.e(TAG, "   • Exception message: ${e.message}")
            Log.e(TAG, "   • Full stack trace:", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getP2POfferById(
        offerId: String,
        accessToken: String?
    ): Result<P2POffer> {
        return try {
            // Verificar throttling usando el manager
            val throttlingResult = throttlingManager.canExecute(ThrottlingOperations.P2P_GET_OFFER_BY_ID)
            
            if (!throttlingResult.canExecute) {
                Log.d("P2PDataSource", "Get offer by ID throttling: waiting ${throttlingResult.remainingTimeMs}ms before request")
                kotlinx.coroutines.delay(throttlingResult.remainingTimeMs)
            }
            
            // Registrar la ejecución
            throttlingManager.recordExecution(ThrottlingOperations.P2P_GET_OFFER_BY_ID)
            
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
            // Verificar throttling usando el manager
            val throttlingResult = throttlingManager.canExecute(ThrottlingOperations.P2P_APPLY_TO_OFFER)
            
            if (!throttlingResult.canExecute) {
                Log.d("P2PDataSource", "Apply to offer throttling: waiting ${throttlingResult.remainingTimeMs}ms before request")
                kotlinx.coroutines.delay(throttlingResult.remainingTimeMs)
            }
            
            // Registrar la ejecución
            throttlingManager.recordExecution(ThrottlingOperations.P2P_APPLY_TO_OFFER)
            
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
            // Verificar throttling usando el manager
            val throttlingResult = throttlingManager.canExecute(ThrottlingOperations.P2P_CREATE_OFFER)
            
            if (!throttlingResult.canExecute) {
                Log.d("P2PDataSource", "Create offer throttling: waiting ${throttlingResult.remainingTimeMs}ms before request")
                kotlinx.coroutines.delay(throttlingResult.remainingTimeMs)
            }
            
            // Registrar la ejecución
            throttlingManager.recordExecution(ThrottlingOperations.P2P_CREATE_OFFER)
            
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
            // Verificar throttling usando el manager
            val throttlingResult = throttlingManager.canExecute(ThrottlingOperations.P2P_GET_MY_OFFERS)
            
            if (!throttlingResult.canExecute) {
                Log.d("P2PDataSource", "Get my offers throttling: waiting ${throttlingResult.remainingTimeMs}ms before request")
                kotlinx.coroutines.delay(throttlingResult.remainingTimeMs)
            }
            
            // Registrar la ejecución
            throttlingManager.recordExecution(ThrottlingOperations.P2P_GET_MY_OFFERS)
            
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

    override suspend fun cancelP2POffer(
        offerId: String,
        accessToken: String?
    ): Result<P2PCancelResponse> {
        return try {
            // Verificar throttling usando el manager
            val throttlingResult = throttlingManager.canExecute(ThrottlingOperations.P2P_CANCEL_OFFER)
            
            if (!throttlingResult.canExecute) {
                Log.d("P2PDataSource", "Cancel offer throttling: waiting ${throttlingResult.remainingTimeMs}ms before request")
                kotlinx.coroutines.delay(throttlingResult.remainingTimeMs)
            }
            
            // Registrar la ejecución
            throttlingManager.recordExecution(ThrottlingOperations.P2P_CANCEL_OFFER)
            
            Log.d("P2PDataSource", "Cancelling P2P offer ID: $offerId")
            Log.d("P2PDataSource", "Access token provided: ${accessToken != null}")
            
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.P2P_CANCEL}/$offerId/cancel"
            Log.d("P2PDataSource", "Full URL: $fullUrl")
            
            val response = httpClient.post(fullUrl) {
                accessToken?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                        append("Accept", "application/json")
                        append("User-Agent", "QvaPay-Android-App")
                        append("X-Requested-With", "XMLHttpRequest")
                    }
                }
                contentType(ContentType.Application.Json)
                setBody("{}")
            }
            
            Log.d("P2PDataSource", "Response status: ${response.status}")
            val rawResponseBody = response.body<String>()
            Log.d("P2PDataSource", "Raw response body: $rawResponseBody")
            
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("HTTP ${response.status.value}: $rawResponseBody"))
            }
            
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            val responseBody = json.decodeFromString<P2PCancelResponse>(rawResponseBody)
            Log.d("P2PDataSource", "Offer cancelled successfully - Message: ${responseBody.msg}")
            
            Result.success(responseBody)
        } catch (e: Exception) {
            Log.e("P2PDataSource", "P2P cancel error: ${e.message}", e)
            Result.failure(e)
        }
    }
}