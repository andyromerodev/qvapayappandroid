package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class GetMyP2POffersUseCase(
    private val p2pRepository: P2PRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(page: Int? = null): Result<P2POfferResponse> {
        return try {
            Log.d("GetMyP2POffersUseCase", "Getting my P2P offers with page: $page")
            
            // Get access token from session
            val accessToken = sessionRepository.getAccessToken()
                ?: return Result.failure(Exception("Access token not available"))
            
            Log.d("GetMyP2POffersUseCase", "Access token retrieved successfully")
            
            // Call repository with access token
            p2pRepository.getMyP2POffers(accessToken, page).fold(
                onSuccess = { response ->
                    Log.d("GetMyP2POffersUseCase", "My P2P offers retrieved successfully - Total: ${response.total}, Page: ${response.currentPage}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("GetMyP2POffersUseCase", "Failed to get my P2P offers: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("GetMyP2POffersUseCase", "Use case error: ${e.message}", e)
            Result.failure(e)
        }
    }
}