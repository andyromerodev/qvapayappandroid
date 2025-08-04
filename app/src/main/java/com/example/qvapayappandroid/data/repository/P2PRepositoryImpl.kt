package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.datasource.P2PDataSource
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.repository.P2PRepository
class P2PRepositoryImpl(
    private val p2pDataSource: P2PDataSource
) : P2PRepository {
    
    override suspend fun getP2POffers(
        filters: P2PFilterRequest,
        accessToken: String?
    ): Result<P2POfferResponse> {
        return try {
            Log.d("P2PRepository", "Getting P2P offers with filters: $filters")
            Log.d("P2PRepository", "Access token provided: ${accessToken != null}")
            
            // Call data source with provided token
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
    
    override suspend fun getP2POfferById(
        offerId: String,
        accessToken: String?
    ): Result<P2POffer> {
        return try {
            Log.d("P2PRepository", "Getting P2P offer by ID: $offerId")
            Log.d("P2PRepository", "Access token provided: ${accessToken != null}")
            
            // Call data source with provided token
            p2pDataSource.getP2POfferById(offerId, accessToken).fold(
                onSuccess = { offer ->
                    Log.d("P2PRepository", "P2P offer retrieved successfully - UUID: ${offer.uuid}")
                    Result.success(offer)
                },
                onFailure = { error ->
                    Log.e("P2PRepository", "Failed to get P2P offer by ID: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P repository error for offer ID: ${e.message}", e)
            Result.failure(e)
        }
    }
}