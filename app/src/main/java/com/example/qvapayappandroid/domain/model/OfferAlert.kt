package com.example.qvapayappandroid.domain.model

data class OfferAlert(
    val id: Long = 0,
    val name: String,
    val coinType: String,
    val offerType: String, // "buy", "sell", "both"
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val targetRate: Double,
    val rateComparison: String, // "greater", "less", "equal"
    val onlyKyc: Boolean = false,
    val onlyVip: Boolean = false,
    val isActive: Boolean = true,
    val checkIntervalMinutes: Int = 30,
    val createdAt: Long = System.currentTimeMillis(),
    val lastCheckedAt: Long? = null,
    val lastTriggeredAt: Long? = null
)