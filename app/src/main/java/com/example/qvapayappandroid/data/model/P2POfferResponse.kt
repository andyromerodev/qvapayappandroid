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
    val type: String? = null, // "buy" or "sell"
    val coin: String? = null,
    @SerialName("peer_id")
    val peerId: Int? = null,
    val amount: String? = null,
    val receive: String? = null,
    val message: String? = null,
    @SerialName("only_kyc")
    val onlyKyc: Int? = null,
    val private: Int? = null,
    @SerialName("only_vip")
    val onlyVip: Int? = null,
    val status: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    val valid: Int? = null,
    @SerialName("coin_data")
    val coinData: CoinData? = null,
    val owner: Owner? = null
)

@Serializable
data class P2PPageLink(
    val url: String?,
    val label: String,
    val active: Boolean
)

@Serializable
data class Owner(
    val uuid: String? = null,
    val username: String? = null,
    val name: String? = null,
    val lastname: String? = null,
    @SerialName("two_factor_reset_code")
    val twoFactorResetCode: String? = null,
    val bio: String? = null,
    val country: String? = null,
    @SerialName("phone_request_id")
    val phoneRequestId: String? = null,
    val twitter: String? = null,
    val kyc: Int? = null,
    val vip: Int? = null,
    @SerialName("golden_check")
    val goldenCheck: Int? = null,
    val role: String? = null,
    @SerialName("can_withdraw")
    val canWithdraw: Int? = null,
    @SerialName("can_deposit")
    val canDeposit: Int? = null,
    @SerialName("can_transfer")
    val canTransfer: Int? = null,
    @SerialName("can_buy")
    val canBuy: Int? = null,
    @SerialName("can_sell")
    val canSell: Int? = null,
    @SerialName("name_verified")
    val nameVerified: String? = null,
    @SerialName("cover_photo_url")
    val coverPhotoUrl: String? = null,
    @SerialName("profile_photo_url")
    val profilePhotoUrl: String? = null,
    @SerialName("average_rating")
    val averageRating: String? = null
)

@Serializable
data class CoinData(
    val id: Int? = null,
    @SerialName("coins_categories_id")
    val coinsCategoriesId: Int? = null,
    val network: String? = null,
    val name: String? = null,
    val logo: String? = null,
    val tick: String? = null,
    val stable: Int? = null,
    @SerialName("fee_in")
    val feeIn: String? = null,
    @SerialName("fee_out")
    val feeOut: String? = null,
    @SerialName("fee_out_fixed")
    val feeOutFixed: String? = null,
    @SerialName("min_in")
    val minIn: String? = null,
    @SerialName("min_out")
    val minOut: String? = null,
    @SerialName("max_in")
    val maxIn: Int? = null,
    @SerialName("max_out")
    val maxOut: Int? = null,
    @SerialName("working_data")
    val workingData: String? = null,
    @SerialName("enabled_in")
    val enabledIn: Int? = null,
    @SerialName("enabled_out")
    val enabledOut: Int? = null,
    @SerialName("enabled_p2p")
    val enabledP2p: Int? = null,
    val price: String? = null
)

@Serializable
data class P2PFilterRequest(
    val type: String? = null, // "buy" or "sell"
    val min: Double? = null,
    val max: Double? = null,
    val coin: String? = null,
    val my: Boolean? = null,
    val vip: Boolean? = null,
    val page: Int? = null,
    val perPage: Int? = 15 // Default 15 items per page to avoid rate limiting
)