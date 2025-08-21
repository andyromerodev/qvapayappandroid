package com.example.qvapayappandroid.data.repository

import android.util.Log
import com.example.qvapayappandroid.data.datasource.P2PDataSource
import com.example.qvapayappandroid.data.database.dao.P2POfferDao
import com.example.qvapayappandroid.data.database.entities.toEntity
import com.example.qvapayappandroid.data.database.entities.toDomainModelList
import com.example.qvapayappandroid.data.database.entities.toDomainModel
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.model.P2PApplyResponse
import com.example.qvapayappandroid.data.model.P2PCancelResponse
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PCreateResponse
import com.example.qvapayappandroid.domain.repository.P2PRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class P2PRepositoryImpl(
    private val p2pDataSource: P2PDataSource,
    private val p2pOfferDao: P2POfferDao
) : P2PRepository {
    
    // ========================================
    // CACHE-FIRST REACTIVE METHODS (Single Source of Truth)
    // ========================================
    
    override fun getMyP2POffersFlow(): Flow<List<P2POffer>> {
        Log.d("P2PRepository", "Getting my P2P offers from cache as Flow")
        return p2pOfferDao.getMyOffersFlow().map { entities ->
            entities.toDomainModelList()
        }
    }
    
    override fun getMyP2POffersByStatusFlow(statuses: List<String>): Flow<List<P2POffer>> {
        Log.d("P2PRepository", "Getting my P2P offers by status from cache as Flow: $statuses")
        return p2pOfferDao.getMyOffersByStatusFlow(statuses).map { entities ->
            entities.toDomainModelList()
        }
    }
    
    override fun getP2POfferByIdFlow(offerId: String): Flow<P2POffer?> {
        Log.d("P2PRepository", "Getting P2P offer by ID from cache as Flow: $offerId")
        return p2pOfferDao.getOfferByUuidFlow(offerId).map { entity ->
            entity?.toDomainModel()
        }
    }
    
    override suspend fun syncMyP2POffers(accessToken: String, page: Int?): Result<Unit> {
        return try {
            Log.d("P2PRepository", "Syncing my P2P offers with API - page: $page")
            
            // Fetch from API
            p2pDataSource.getMyP2POffers(accessToken, page).fold(
                onSuccess = { response ->
                    Log.d("P2PRepository", "API sync successful - Total: ${response.total}, Current page: ${response.currentPage}")
                    
                    // Convert and save to local cache
                    val entities = response.data.map { it.toEntity(isMyOffer = true) }
                    
                    if (page == null || page == 1) {
                        // First page or refresh - replace all my offers
                        p2pOfferDao.insertMyOffers(entities)
                        Log.d("P2PRepository", "Replaced all my offers in cache with ${entities.size} items")
                    } else {
                        // Additional page - append to existing offers
                        p2pOfferDao.insertOffers(entities)
                        Log.d("P2PRepository", "Added ${entities.size} more offers to cache")
                    }
                    
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e("P2PRepository", "Failed to sync my P2P offers: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P sync error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun refreshMyP2POffers(accessToken: String): Result<Unit> {
        return try {
            Log.d("P2PRepository", "Refreshing all my P2P offers (clear cache + reload)")
            
            // Clear existing cache first
            p2pOfferDao.clearMyOffers()
            Log.d("P2PRepository", "Cleared my offers cache")
            
            // Fetch fresh data from API
            syncMyP2POffers(accessToken, page = 1)
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P refresh error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // TRADITIONAL API METHODS (for non-cached operations)
    // ========================================
    
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
    
    override suspend fun getMyP2POffers(
        accessToken: String,
        page: Int?
    ): Result<P2POfferResponse> {
        return try {
            Log.d("P2PRepository", "Getting my P2P offers with page: $page")
            Log.d("P2PRepository", "Access token provided: ${accessToken.isNotEmpty()}")
            
            // Call data source with provided token
            p2pDataSource.getMyP2POffers(accessToken, page).fold(
                onSuccess = { response ->
                    Log.d("P2PRepository", "My P2P offers retrieved successfully - Total: ${response.total}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("P2PRepository", "Failed to get my P2P offers: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P repository error for my offers: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelP2POffer(
        offerId: String,
        accessToken: String?
    ): Result<P2PCancelResponse> {
        return try {
            Log.d("P2PRepository", "Cancelling P2P offer ID: $offerId")
            Log.d("P2PRepository", "Access token provided: ${accessToken != null}")
            
            // First update local cache with "cancelling" status
            p2pOfferDao.updateLocalStatus(offerId, "cancelling")
            Log.d("P2PRepository", "Updated local status to 'cancelling' for offer: $offerId")
            
            // Then call API
            p2pDataSource.cancelP2POffer(offerId, accessToken).fold(
                onSuccess = { response ->
                    Log.d("P2PRepository", "P2P offer cancelled successfully - Message: ${response.msg}")
                    
                    // Update local cache with final cancelled status
                    p2pOfferDao.updateOfferStatus(offerId, "cancelled")
                    p2pOfferDao.updateLocalStatus(offerId, null) // Clear local status
                    Log.d("P2PRepository", "Updated cache with cancelled status for offer: $offerId")
                    
                    Result.success(response)
                },
                onFailure = { error ->
                    Log.e("P2PRepository", "Failed to cancel P2P offer: ${error.message}")
                    
                    // Revert local status on API failure
                    p2pOfferDao.updateLocalStatus(offerId, null)
                    Log.d("P2PRepository", "Reverted local status for offer: $offerId due to API failure")
                    
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e("P2PRepository", "P2P repository error for offer cancellation: ${e.message}", e)
            
            // Revert local status on exception
            try {
                p2pOfferDao.updateLocalStatus(offerId, null)
            } catch (dbException: Exception) {
                Log.e("P2PRepository", "Failed to revert local status: ${dbException.message}")
            }
            
            Result.failure(e)
        }
    }
}