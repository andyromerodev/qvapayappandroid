package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class GetP2POfferByIdUseCase(
    private val p2pRepository: P2PRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(offerId: String): Result<P2POffer> {
        return try {
            Log.d("GetP2POfferByIdUseCase", "Getting P2P offer by ID: $offerId")
            
            // Get access token from session repository
            val accessToken = sessionRepository.getAccessToken()
            Log.d("GetP2POfferByIdUseCase", "Access token retrieved: ${accessToken != null}")
            
            // Call P2P repository with token
            p2pRepository.getP2POfferById(offerId, accessToken).fold(
                onSuccess = { offer ->
                    Log.d("GetP2POfferByIdUseCase", "P2P offer retrieved successfully - UUID: ${offer.uuid}")
                    Result.success(offer)
                },
                onFailure = { error ->
                    Log.e("GetP2POfferByIdUseCase", "Failed to get P2P offer by ID: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("GetP2POfferByIdUseCase", "Use case error: ${e.message}", e)
            Result.failure(e)
        }
    }
}