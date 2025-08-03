package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.datasource.P2PDataSource
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class P2PRepositoryImpl(
    private val p2pDataSource: P2PDataSource,
    private val sessionRepository: SessionRepository
) : P2PRepository {
    
    override suspend fun getP2POffers(filters: P2PFilterRequest): Result<P2POfferResponse> {
        return try {
            Log.d("P2PRepository", "Getting P2P offers with filters: $filters")
            
            // Get access token from session repository
            val accessToken = sessionRepository.getAccessToken()
            Log.d("P2PRepository", "Access token retrieved: ${accessToken != null}")
            
            // Call data source with token
            p2pDataSource.getP2POffers(filters, accessToken).fold(
                onSuccess = { response ->
                    Log.d("P2PRepository", "P2P offers retrieved successfully - Total: ${response.total}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("P2PRepository", "Failed to get P2P offers: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P repository error: ${e.message}", e)
            Result.failure(e)
        }
    }
}