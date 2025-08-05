package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PCreateResponse
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository

class CreateP2POfferUseCase(
    private val p2pRepository: P2PRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(request: P2PCreateRequest): Result<P2PCreateResponse> {
        return try {
            Log.d("CreateP2POfferUseCase", "Creating P2P offer: $request")
            
            // Get access token from session repository
            val accessToken = sessionRepository.getAccessToken()
            Log.d("CreateP2POfferUseCase", "Access token retrieved: ${accessToken != null}")
            
            // Call P2P repository with token
            p2pRepository.createP2POffer(request, accessToken).fold(
                onSuccess = { response ->
                    Log.d("CreateP2POfferUseCase", "P2P offer creation successful - Message: ${response.msg}, UUID: ${response.p2p.uuid}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("CreateP2POfferUseCase", "Failed to create P2P offer: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("CreateP2POfferUseCase", "Use case error: ${e.message}", e)
            Result.failure(e)
        }
    }
}