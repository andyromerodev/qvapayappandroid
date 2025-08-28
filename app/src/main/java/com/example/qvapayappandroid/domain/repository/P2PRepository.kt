package com.example.qvapayappandroid.domain.repository

import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.model.P2PApplyResponse
import com.example.qvapayappandroid.data.model.P2PCancelResponse
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PCreateResponse
import kotlinx.coroutines.flow.Flow

interface P2PRepository {
    // ========================================
    // CACHE-FIRST REACTIVE METHODS (Single Source of Truth)
    // ========================================
    
    /**
     * Get my P2P offers as reactive Flow from local cache
     * Cache-first: Returns local data immediately, syncs with API in background
     */
    fun getMyP2POffersFlow(): Flow<List<P2POffer>>
    
    /**
     * Get my P2P offers filtered by status as reactive Flow from local cache
     */
    fun getMyP2POffersByStatusFlow(statuses: List<String>): Flow<List<P2POffer>>
    
    /**
     * Get specific offer by UUID as reactive Flow from local cache
     */
    fun getP2POfferByIdFlow(offerId: String): Flow<P2POffer?>
    
    /**
     * Sync my P2P offers with API and update local cache
     * This method triggers background sync and updates the Flow subscribers
     */
    suspend fun syncMyP2POffers(accessToken: String, page: Int? = null): Result<Unit>
    
    /**
     * Refresh all my P2P offers (clear cache and reload from API)
     */
    suspend fun refreshMyP2POffers(accessToken: String): Result<Unit>
    
    // ========================================
    // TRADITIONAL API METHODS (for non-cached operations)
    // ========================================
    
    suspend fun getP2POffers(
        filters: P2PFilterRequest = P2PFilterRequest(),
        accessToken: String? = null
    ): Result<P2POfferResponse>
    
    suspend fun getP2POfferById(
        offerId: String,
        accessToken: String? = null
    ): Result<P2POffer>
    
    suspend fun applyToP2POffer(
        offerId: String,
        accessToken: String? = null
    ): Result<P2PApplyResponse>
    
    suspend fun createP2POffer(
        request: P2PCreateRequest,
        accessToken: String? = null
    ): Result<P2PCreateResponse>
    
    /**
     * @deprecated Use getMyP2POffersFlow() for reactive cached access or syncMyP2POffers() for manual sync
     */
    @Deprecated("Use cache-first methods instead")
    suspend fun getMyP2POffers(
        accessToken: String,
        page: Int? = null
    ): Result<P2POfferResponse>

    /**
     * Cancel P2P offer and update local cache
     */
    suspend fun cancelP2POffer(
        offerId: String,
        accessToken: String? = null
    ): Result<P2PCancelResponse>
}