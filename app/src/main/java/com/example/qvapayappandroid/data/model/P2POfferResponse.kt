package com.example.qvapayappandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class P2POfferResponse(
    @SerialName("current_page")
    val currentPage: Int,
    val data: List<P2POffer>,
    @SerialName("first_page_url")
    val firstPageUrl: String,
    val from: Int,
    @SerialName("last_page")
    val lastPage: Int,
    @SerialName("last_page_url")
    val lastPageUrl: String,
    val links: List<P2PPageLink>,
    @SerialName("next_page_url")
    val nextPageUrl: String?,
    val path: String,
    @SerialName("per_page")
    val perPage: Int,
    @SerialName("prev_page_url")
    val prevPageUrl: String?,
    val to: Int,
    val total: Int
)

@Serializable
data class P2POffer(
    val uuid: String? = null,
    @SerialName("user_id")
    val userId: Int? = null,
    val type: String? = null, // "buy" or "sell"
    val coin: String? = null,
    @SerialName("peer_id")
    val peerId: Int? = null,
    val amount: String? = null,
    val receive: String? = null,
    @SerialName("only_kyc")
    val onlyKyc: Int? = null,
    val private: Int? = null,
    val status: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class P2PPageLink(
    val url: String?,
    val label: String,
    val active: Boolean
)

@Serializable
data class P2PFilterRequest(
    val type: String? = null, // "buy" or "sell"
    val min: Double? = null,
    val max: Double? = null,
    val coin: String? = null,
    val my: Boolean? = null,
    val vip: Boolean? = null,
    val page: Int? = null
)