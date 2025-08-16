package com.example.qvapayappandroid.domain.model

import com.example.qvapayappandroid.data.model.P2PDetail
import com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer.AvailableCoin

data class OfferTemplate(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val type: String, // "sell" o "buy"
    val coinId: String,
    val coinName: String,
    val coinTick: String,
    val amount: String,
    val receive: String,
    val details: List<P2PDetail>,
    val onlyKyc: Boolean,
    val private: Boolean,
    val promoteOffer: Boolean,
    val onlyVip: Boolean,
    val message: String,
    val webhook: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toAvailableCoin(): AvailableCoin {
        return AvailableCoin(
            id = coinId.toIntOrNull() ?: 0,
            name = coinName,
            tick = coinTick
        )
    }
}