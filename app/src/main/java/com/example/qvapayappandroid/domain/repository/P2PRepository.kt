package com.example.qvapayappandroid.domain.repository

import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse

interface P2PRepository {
    suspend fun getP2POffers(
        filters: P2PFilterRequest = P2PFilterRequest(),
        accessToken: String? = null
    ): Result<P2POfferResponse>
}