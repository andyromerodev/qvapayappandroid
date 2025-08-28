package com.example.qvapayappandroid.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.model.Owner
import com.example.qvapayappandroid.data.model.Peer
import com.example.qvapayappandroid.data.model.CoinData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Room entity for P2P offers with Single Source of Truth pattern.
 * Stores P2P offers locally for offline-first approach.
 */
@Entity(
    tableName = "p2p_offers"
)
data class P2POfferEntity(
    @PrimaryKey
    val uuid: String,
    
    // Basic offer information
    val type: String? = null, // "buy" or "sell"
    val coin: String? = null,
    
    @ColumnInfo(name = "peer_id")
    val peerId: Int? = null,
    
    val amount: String? = null,
    val receive: String? = null,
    val details: String? = null,
    val message: String? = null,
    
    // Offer configuration
    @ColumnInfo(name = "only_kyc")
    val onlyKyc: Int? = null,
    
    @ColumnInfo(name = "private")
    val isPrivate: Int? = null,
    
    @ColumnInfo(name = "only_vip")
    val onlyVip: Int? = null,
    
    // Status and tracking
    val status: String? = null,
    
    @ColumnInfo(name = "tx_id")
    val txId: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null,
    
    val valid: Int? = null,
    
    // Complex objects serialized as JSON strings
    @ColumnInfo(name = "coin_data_json")
    val coinDataJson: String? = null,
    
    @ColumnInfo(name = "owner_json")
    val ownerJson: String? = null,
    
    @ColumnInfo(name = "peer_json")
    val peerJson: String? = null,
    
    // Local metadata for cache management
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long = 0L,
    
    @ColumnInfo(name = "is_my_offer")
    val isMyOffer: Boolean = false,
    
    @ColumnInfo(name = "local_status")
    val localStatus: String? = null // For offline actions like "cancelling"
)

// Extension functions to convert between domain model and entity
fun P2POffer.toEntity(isMyOffer: Boolean = false): P2POfferEntity {
    val json = Json { ignoreUnknownKeys = true }
    
    return P2POfferEntity(
        uuid = uuid ?: throw IllegalArgumentException("P2POffer must have a UUID"),
        type = type,
        coin = coin,
        peerId = peerId,
        amount = amount,
        receive = receive,
        details = details,
        message = message,
        onlyKyc = onlyKyc,
        isPrivate = private,
        onlyVip = onlyVip,
        status = status,
        txId = txId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        valid = valid,
        coinDataJson = coinData?.let { json.encodeToString(it) },
        ownerJson = owner?.let { json.encodeToString(it) },
        peerJson = peer?.let { json.encodeToString(it) },
        lastSyncAt = System.currentTimeMillis(),
        isMyOffer = isMyOffer,
        localStatus = null
    )
}

fun P2POfferEntity.toDomainModel(): P2POffer {
    val json = Json { ignoreUnknownKeys = true }
    
    return P2POffer(
        uuid = uuid,
        type = type,
        coin = coin,
        peerId = peerId,
        amount = amount,
        receive = receive,
        details = details,
        message = message,
        onlyKyc = onlyKyc,
        private = isPrivate,
        onlyVip = onlyVip,
        status = status,
        txId = txId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        valid = valid,
        coinData = coinDataJson?.let { json.decodeFromString<CoinData>(it) },
        owner = ownerJson?.let { json.decodeFromString<Owner>(it) },
        peer = peerJson?.let { json.decodeFromString<Peer>(it) }
    )
}

fun List<P2POffer>.toEntityList(isMyOffer: Boolean = false): List<P2POfferEntity> {
    return map { it.toEntity(isMyOffer) }
}

fun List<P2POfferEntity>.toDomainModelList(): List<P2POffer> {
    return map { it.toDomainModel() }
}