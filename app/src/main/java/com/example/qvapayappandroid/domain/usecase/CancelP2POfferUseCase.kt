package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PCancelResponse
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class CancelP2POfferUseCase(
    private val p2pRepository: P2PRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(offerId: String): Result<P2PCancelResponse> {
        return try {
            Log.d("CancelP2POfferUseCase", "Cancelling P2P offer ID: $offerId")

            val accessToken = sessionRepository.getAccessToken()
            Log.d("CancelP2POfferUseCase", "Access token retrieved: ${accessToken != null}")

            p2pRepository.cancelP2POffer(offerId, accessToken).fold(
                onSuccess = { response ->
                    Log.d("CancelP2POfferUseCase", "P2P offer cancelled successfully - Message: ${response.msg}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("CancelP2POfferUseCase", "Failed to cancel P2P offer: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e("CancelP2POfferUseCase", "Use case error: ${e.message}", e)
            Result.failure(e)
        }
    }
}



