package com.example.qvapayappandroid.domain.model

data class AlertNotification(
    val id: Long = 0,
    val alertId: Long,
    val offerId: String,
    val alertName: String,
    val coinType: String,
    val offerType: String,
    val amount: Double,
    val rate: Double,
    val ownerUsername: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)