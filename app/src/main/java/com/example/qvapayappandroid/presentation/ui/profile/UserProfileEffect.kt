package com.example.qvapayappandroid.presentation.ui.profile

/**
 * UserProfileEffect sealed interface following MVI pattern.
 * Defines all side effects that can occur in the User Profile screen.
 */
sealed interface UserProfileEffect {
    /**
     * Navigate to login screen after successful logout
     */
    data object NavigateToLogin : UserProfileEffect
    
    /**
     * Show success message when profile is refreshed
     */
    data class ShowSuccessMessage(val message: String) : UserProfileEffect
    
    /**
     * Show error message as snackbar or toast
     */
    data class ShowErrorMessage(val message: String) : UserProfileEffect
    
    /**
     * Show logout confirmation dialog
     */
    data object ShowLogoutConfirmation : UserProfileEffect
}