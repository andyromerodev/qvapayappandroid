package com.example.qvapayappandroid.presentation.ui.profile

/**
 * UserProfileIntent sealed interface following MVI pattern.
 * Defines all possible user actions in the User Profile screen.
 */
sealed interface UserProfileIntent {
    /**
     * Load user profile data initially
     */
    data object LoadUserProfile : UserProfileIntent
    
    /**
     * Refresh user profile data
     */
    data object RefreshUserProfile : UserProfileIntent
    
    /**
     * Perform user logout
     */
    data object Logout : UserProfileIntent
    
    /**
     * Clear error message from UI state
     */
    data object ClearError : UserProfileIntent
    
    /**
     * Retry loading user profile after error
     */
    data object RetryLoadProfile : UserProfileIntent
}