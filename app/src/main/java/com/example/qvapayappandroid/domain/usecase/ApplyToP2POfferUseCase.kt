package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PApplyResponse
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class ApplyToP2POfferUseCase(
    private val p2pRepository: P2PRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(offerId: String): Result<P2PApplyResponse> {
        return try {
            Log.d("ApplyToP2POfferUseCase", "Applying to P2P offer ID: $offerId")
            
            // Get access token from session repository
            val accessToken = sessionRepository.getAccessToken()
            Log.d("ApplyToP2POfferUseCase", "Access token retrieved: ${accessToken != null}")
            
            // Call P2P repository with token
            p2pRepository.applyToP2POffer(offerId, accessToken).fold(
                onSuccess = { response ->
                    Log.d("ApplyToP2POfferUseCase", "P2P offer application successful - Message: ${response.msg}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("ApplyToP2POfferUseCase", "Failed to apply to P2P offer: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("ApplyToP2POfferUseCase", "Use case error: ${e.message}", e)
            Result.failure(e)
        }
    }
}