package com.example.qvapayappandroid.domain.usecase

import android.util.Log
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case for managing my P2P offers with Single Source of Truth pattern.
 * Provides both reactive (Flow) and traditional (suspend) access methods.
 */
class GetMyP2POffersUseCase(
    private val p2pRepository: P2PRepository,
    private val sessionRepository: SessionRepository
) {
    
    // ========================================
    // REACTIVE METHODS (Single Source of Truth)
    // ========================================
    
    /**
     * Get my P2P offers as reactive Flow from local cache.
     * This method returns data immediately and updates automatically when cache changes.
     */
    fun getMyOffersFlow(): Flow<List<P2POffer>> {
        Log.d("GetMyP2POffersUseCase", "Getting my P2P offers as reactive Flow")
        return p2pRepository.getMyP2POffersFlow()
    }
    
    /**
     * Get my P2P offers filtered by status as reactive Flow from local cache.
     */
    fun getMyOffersByStatusFlow(statuses: List<String>): Flow<List<P2POffer>> {
        Log.d("GetMyP2POffersUseCase", "Getting my P2P offers by status as reactive Flow: $statuses")
        return p2pRepository.getMyP2POffersByStatusFlow(statuses)
    }
    
    /**
     * Sync my P2P offers with API and update local cache.
     * Call this method to refresh data from server.
     */
    suspend fun syncOffers(page: Int? = null): Result<Unit> {
        return try {
            Log.d("GetMyP2POffersUseCase", "Syncing my P2P offers with page: $page")
            
            // Get access token from session
            val accessToken = sessionRepository.getAccessToken()
                ?: return Result.failure(Exception("Access token not available"))
            
            Log.d("GetMyP2POffersUseCase", "Access token retrieved successfully")
            
            // Sync with API
            p2pRepository.syncMyP2POffers(accessToken, page).fold(
                onSuccess = {
                    Log.d("GetMyP2POffersUseCase", "My P2P offers synced successfully")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e("GetMyP2POffersUseCase", "Failed to sync my P2P offers: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("GetMyP2POffersUseCase", "Sync error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Refresh all my P2P offers (clear cache and reload from API).
     */
    suspend fun refreshOffers(): Result<Unit> {
        return try {
            Log.d("GetMyP2POffersUseCase", "Refreshing all my P2P offers")
            
            // Get access token from session
            val accessToken = sessionRepository.getAccessToken()
                ?: return Result.failure(Exception("Access token not available"))
            
            Log.d("GetMyP2POffersUseCase", "Access token retrieved successfully")
            
            // Refresh with API
            p2pRepository.refreshMyP2POffers(accessToken).fold(
                onSuccess = {
                    Log.d("GetMyP2POffersUseCase", "My P2P offers refreshed successfully")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e("GetMyP2POffersUseCase", "Failed to refresh my P2P offers: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("GetMyP2POffersUseCase", "Refresh error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // LEGACY METHOD (for backward compatibility)
    // ========================================
    
    /**
     * @deprecated Use getMyOffersFlow() for reactive access or syncOffers() for manual sync
     */
    @Deprecated("Use reactive methods instead")
    suspend operator fun invoke(page: Int? = null): Result<P2POfferResponse> {
        return try {
            Log.d("GetMyP2POffersUseCase", "Getting my P2P offers with page: $page (DEPRECATED)")
            
            // Get access token from session
            val accessToken = sessionRepository.getAccessToken()
                ?: return Result.failure(Exception("Access token not available"))
            
            Log.d("GetMyP2POffersUseCase", "Access token retrieved successfully")
            
            // Call repository with access token
            @Suppress("DEPRECATION")
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