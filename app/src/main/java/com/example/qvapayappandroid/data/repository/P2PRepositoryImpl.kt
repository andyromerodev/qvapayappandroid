package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.datasource.P2PDataSource
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.model.P2PApplyResponse
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PCreateResponse
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
    
    override suspend fun applyToP2POffer(
        offerId: String,
        accessToken: String?
    ): Result<P2PApplyResponse> {
        return try {
            Log.d("P2PRepository", "Applying to P2P offer ID: $offerId")
            Log.d("P2PRepository", "Access token provided: ${accessToken != null}")
            
            // Call data source with provided token
            p2pDataSource.applyToP2POffer(offerId, accessToken).fold(
                onSuccess = { response ->
                    Log.d("P2PRepository", "P2P offer application successful - Message: ${response.msg}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("P2PRepository", "Failed to apply to P2P offer: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P repository error for offer application: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun createP2POffer(
        request: P2PCreateRequest,
        accessToken: String?
    ): Result<P2PCreateResponse> {
        return try {
            Log.d("P2PRepository", "Creating P2P offer: $request")
            Log.d("P2PRepository", "Access token provided: ${accessToken != null}")
            
            // Call data source with provided token
            p2pDataSource.createP2POffer(request, accessToken).fold(
                onSuccess = { response ->
                    Log.d("P2PRepository", "P2P offer created successfully - Message: ${response.msg}, UUID: ${response.p2p.uuid}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("P2PRepository", "Failed to create P2P offer: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P repository error for offer creation: ${e.message}", e)
            Result.failure(e)
        }
    }
}