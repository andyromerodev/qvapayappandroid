package com.example.qvapayappandroid.data.datasource

import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POfferResponse
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PCreateResponse
import com.example.qvapayappandroid.data.model.P2PApplyResponse
import com.example.qvapayappandroid.data.model.P2PCancelResponse

interface P2PDataSource {
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
    
    suspend fun getMyP2POffers(
        accessToken: String,
        page: Int? = null
    ): Result<P2POfferResponse>

    suspend fun cancelP2POffer(
        offerId: String,
        accessToken: String? = null
    ): Result<P2PCancelResponse>
}