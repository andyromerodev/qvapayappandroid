package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class GetP2POffersUseCase(
    private val p2pRepository: P2PRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(filters: P2PFilterRequest = P2PFilterRequest()): Result<P2POfferResponse> {
        return try {
            Log.d("GetP2POffersUseCase", "Getting P2P offers with filters: $filters")
            
            // Get access token from session repository
            val accessToken = sessionRepository.getAccessToken()
            Log.d("GetP2POffersUseCase", "Access token retrieved: ${accessToken != null}")
            
            // Call P2P repository with token
            p2pRepository.getP2POffers(filters, accessToken).fold(
                onSuccess = { response ->
                    Log.d("GetP2POffersUseCase", "P2P offers retrieved successfully - Total: ${response.total}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("GetP2POffersUseCase", "Failed to get P2P offers: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("GetP2POffersUseCase", "Use case error: ${e.message}", e)
            Result.failure(e)
        }
    }
}