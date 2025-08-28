package com.example.qvapayappandroid.presentation.ui.profile

import com.example.qvapayappandroid.data.model.User

/**
 * UserProfileState data class following MVI pattern.
 * Represents the complete UI state for the User Profile screen.
 */
data class UserProfileState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoggingOut: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val isInitialized: Boolean = false
) {
    /**
     * Computed property to check if we should show loading indicator
     */
    val shouldShowLoading: Boolean
        get() = isLoading && !isRefreshing && user == null
    
    /**
     * Computed property to check if we should show content
     */
    val shouldShowContent: Boolean
        get() = user != null && errorMessage == null
    
    /**
     * Computed property to check if we should show error
     */
    val shouldShowError: Boolean
        get() = errorMessage != null && user == null
    
    /**
     * Computed property to check if refresh action is available
     */
    val canRefresh: Boolean
        get() = !isLoading && !isRefreshing && !isLoggingOut
    
    /**
     * Computed property to check if logout action is available
     */
    val canLogout: Boolean
        get() = !isLoggingOut && !isLoading
    
    /**
     * Get display name for the user
     */
    val userDisplayName: String
        get() = user?.let { "${it.name} ${it.lastname}".trim() } ?: ""
    
    /**
     * Get username with @ prefix
     */
    val userUsername: String
        get() = user?.username?.let { "@$it" } ?: ""
    
    /**
     * Check if user has profile picture
     */
    val hasProfilePicture: Boolean
        get() = !user?.profilePhotoUrl.isNullOrBlank()
    
    /**
     * Check if user has bio
     */
    val hasBio: Boolean
        get() = !user?.bio.isNullOrBlank()
    
    /**
     * Get formatted balance
     */
    val formattedBalance: String
        get() = user?.balance?.let { "$${it}" } ?: "$0.00"
    
    /**
     * Get formatted pending balance
     */
    val formattedPendingBalance: String
        get() = user?.pendingBalance?.let { "$${it}" } ?: "$0.00"
    
    /**
     * Get formatted satoshis
     */
    val formattedSatoshis: String
        get() = user?.satoshis?.toString() ?: "0"
    
    /**
     * Get KYC status text
     */
    val kycStatusText: String
        get() = if (user?.kyc == 1) "Verificado" else "No verificado"
    
    /**
     * Get VIP status text
     */
    val vipStatusText: String
        get() = if (user?.vip == 1) "Sí" else "No"
    
    /**
     * Get P2P status text
     */
    val p2pStatusText: String
        get() = if (user?.p2pEnabled == 1) "Habilitado" else "Deshabilitado"
    
    /**
     * Get country display text
     */
    val countryDisplayText: String
        get() = user?.country?.ifBlank { "No especificado" } ?: "No especificado"
    
    /**
     * Get email display text (using phone field as email)
     */
    val emailDisplayText: String
        get() = user?.phone ?: "No disponible"
    
    /**
     * Get rating display text
     */
    val ratingDisplayText: String
        get() = user?.averageRating ?: "N/A"
}