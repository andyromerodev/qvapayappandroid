package com.example.qvapayappandroid.data.database.dao

import androidx.room.*
import com.example.qvapayappandroid.data.database.entities.P2POfferEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for P2P offers with Single Source of Truth pattern.
 * Provides reactive flows for real-time UI updates.
 */
@Dao
interface P2POfferDao {
    
    // ========================================
    // REACTIVE QUERIES (Flow-based)
    // ========================================
    
    /**
     * Get all my P2P offers as reactive Flow
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 1 ORDER BY created_at DESC")
    fun getMyOffersFlow(): Flow<List<P2POfferEntity>>
    
    /**
     * Get my offers filtered by status as reactive Flow
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 1 AND status IN (:statuses) ORDER BY created_at DESC")
    fun getMyOffersByStatusFlow(statuses: List<String>): Flow<List<P2POfferEntity>>
    
    /**
     * Get all P2P offers (marketplace) as reactive Flow
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 0 ORDER BY created_at DESC")
    fun getMarketplaceOffersFlow(): Flow<List<P2POfferEntity>>
    
    /**
     * Get offer by UUID as reactive Flow
     */
    @Query("SELECT * FROM p2p_offers WHERE uuid = :uuid")
    fun getOfferByUuidFlow(uuid: String): Flow<P2POfferEntity?>
    
    // ========================================
    // SUSPENDING QUERIES (One-time fetch)
    // ========================================
    
    /**
     * Get all my P2P offers (one-time fetch)
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 1 ORDER BY created_at DESC")
    suspend fun getMyOffers(): List<P2POfferEntity>
    
    /**
     * Get my offers filtered by status (one-time fetch)
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 1 AND status IN (:statuses) ORDER BY created_at DESC")
    suspend fun getMyOffersByStatus(statuses: List<String>): List<P2POfferEntity>
    
    /**
     * Get all marketplace offers (one-time fetch)
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 0 ORDER BY created_at DESC")
    suspend fun getMarketplaceOffers(): List<P2POfferEntity>
    
    /**
     * Get offer by UUID (one-time fetch)
     */
    @Query("SELECT * FROM p2p_offers WHERE uuid = :uuid")
    suspend fun getOfferByUuid(uuid: String): P2POfferEntity?
    
    /**
     * Check if offer exists
     */
    @Query("SELECT COUNT(*) > 0 FROM p2p_offers WHERE uuid = :uuid")
    suspend fun existsOffer(uuid: String): Boolean
    
    /**
     * Get offers that need sync (older than threshold)
     */
    @Query("SELECT * FROM p2p_offers WHERE last_sync_at < :threshold")
    suspend fun getOffersNeedingSync(threshold: Long): List<P2POfferEntity>
    
    /**
     * Get count of my offers
     */
    @Query("SELECT COUNT(*) FROM p2p_offers WHERE is_my_offer = 1")
    suspend fun getMyOffersCount(): Int
    
    // ========================================
    // INSERT OPERATIONS
    // ========================================
    
    /**
     * Insert single offer (replace if exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: P2POfferEntity)
    
    /**
     * Insert multiple offers (replace if exist)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<P2POfferEntity>)
    
    /**
     * Insert or update my offers (replaces existing data)
     */
    @Transaction
    suspend fun insertMyOffers(offers: List<P2POfferEntity>) {
        // Clear existing my offers and insert new ones
        clearMyOffers()
        insertOffers(offers.map { it.copy(isMyOffer = true) })
    }
    
    /**
     * Insert or update marketplace offers (replaces existing data)
     */
    @Transaction
    suspend fun insertMarketplaceOffers(offers: List<P2POfferEntity>) {
        // Clear existing marketplace offers and insert new ones
        clearMarketplaceOffers()
        insertOffers(offers.map { it.copy(isMyOffer = false) })
    }
    
    // ========================================
    // UPDATE OPERATIONS
    // ========================================
    
    /**
     * Update offer status
     */
    @Query("UPDATE p2p_offers SET status = :status, last_sync_at = :syncTime WHERE uuid = :uuid")
    suspend fun updateOfferStatus(uuid: String, status: String, syncTime: Long = System.currentTimeMillis())
    
    /**
     * Update local status (for offline actions)
     */
    @Query("UPDATE p2p_offers SET local_status = :localStatus WHERE uuid = :uuid")
    suspend fun updateLocalStatus(uuid: String, localStatus: String?)
    
    /**
     * Update last sync time
     */
    @Query("UPDATE p2p_offers SET last_sync_at = :syncTime WHERE uuid = :uuid")
    suspend fun updateLastSyncTime(uuid: String, syncTime: Long = System.currentTimeMillis())
    
    /**
     * Update full offer
     */
    @Update
    suspend fun updateOffer(offer: P2POfferEntity)
    
    // ========================================
    // DELETE OPERATIONS
    // ========================================
    
    /**
     * Delete offer by UUID
     */
    @Query("DELETE FROM p2p_offers WHERE uuid = :uuid")
    suspend fun deleteOffer(uuid: String)
    
    /**
     * Clear all my offers
     */
    @Query("DELETE FROM p2p_offers WHERE is_my_offer = 1")
    suspend fun clearMyOffers()
    
    /**
     * Clear all marketplace offers
     */
    @Query("DELETE FROM p2p_offers WHERE is_my_offer = 0")
    suspend fun clearMarketplaceOffers()
    
    /**
     * Clear all offers
     */
    @Query("DELETE FROM p2p_offers")
    suspend fun clearAllOffers()
    
    /**
     * Delete old offers (cache cleanup)
     */
    @Query("DELETE FROM p2p_offers WHERE last_sync_at < :threshold")
    suspend fun deleteOldOffers(threshold: Long)
    
    // ========================================
    // ADVANCED QUERIES
    // ========================================
    
    /**
     * Search my offers by text
     */
    @Query("""
        SELECT * FROM p2p_offers 
        WHERE is_my_offer = 1 
        AND (
            coin LIKE '%' || :query || '%' OR
            details LIKE '%' || :query || '%' OR
            message LIKE '%' || :query || '%' OR
            status LIKE '%' || :query || '%'
        )
        ORDER BY created_at DESC
    """)
    fun searchMyOffers(query: String): Flow<List<P2POfferEntity>>
    
    /**
     * Get offers by type (buy/sell)
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 1 AND type = :type ORDER BY created_at DESC")
    fun getMyOffersByType(type: String): Flow<List<P2POfferEntity>>
    
    /**
     * Get offers by coin
     */
    @Query("SELECT * FROM p2p_offers WHERE is_my_offer = 1 AND coin = :coin ORDER BY created_at DESC")
    fun getMyOffersByCoin(coin: String): Flow<List<P2POfferEntity>>
}