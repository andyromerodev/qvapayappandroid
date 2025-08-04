package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer

interface P2PDataSource {
    suspend fun getP2POffers(
        filters: P2PFilterRequest = P2PFilterRequest(),
        accessToken: String? = null
    ): Result<P2POfferResponse>
    
    suspend fun getP2POfferById(
        offerId: String,
        accessToken: String? = null
    ): Result<P2POffer>
}