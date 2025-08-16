package com.example.qvapayappandroid.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offer_templates")
data class OfferTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val type: String, // "sell" o "buy"
    val coinId: String,
    val coinName: String,
    val coinTick: String,
    val amount: String,
    val receive: String,
    val detailsJson: String, // JSON serializado de List<P2PDetail>
    val onlyKyc: Boolean,
    @ColumnInfo(name = "private")
    val isPrivate: Boolean,
    val promoteOffer: Boolean,
    val onlyVip: Boolean,
    val message: String,
    val webhook: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)