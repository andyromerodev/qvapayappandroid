package com.example.qvapayappandroid.presentation.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.usecase.GetCurrentUserUseCase
import com.example.qvapayappandroid.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UserProfileViewModel following MVI pattern.
 * Handles all business logic for the User Profile screen using Intent/State/Effect pattern.
 */
class UserProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val refreshUserProfileUseCase: com.example.qvapayappandroid.domain.usecase.RefreshUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "UserProfileViewModel"
    }
    
    private val _uiState = MutableStateFlow(UserProfileState())
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<UserProfileEffect>()
    val effect: SharedFlow<UserProfileEffect> = _effect.asSharedFlow()
    
    init {
        handleIntent(UserProfileIntent.LoadUserProfile)
        observeUserChanges()
    }
    
    /**
     * Handle all user intents following MVI pattern
     */
    fun handleIntent(intent: UserProfileIntent) {
        when (intent) {
            is UserProfileIntent.LoadUserProfile -> loadUserData()
            is UserProfileIntent.RefreshUserProfile -> refreshUserData()
            is UserProfileIntent.Logout -> performLogout()
            is UserProfileIntent.ClearError -> clearError()
            is UserProfileIntent.RetryLoadProfile -> retryLoadProfile()
        }
    }
    
    private fun loadUserData() {
        if (_uiState.value.isInitialized) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val user = getCurrentUserUseCase.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    errorMessage = null,
                    isInitialized = true
                )
                Log.d(TAG, "User loaded: ${user?.name}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading user data: ${e.message}",
                    isInitialized = true
                )
                Log.e(TAG, "Error loading user", e)
            }
        }
    }
    
    private fun refreshUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                errorMessage = null
            )
            
            refreshUserProfileUseCase().fold(
                onSuccess = { refreshedUser ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        user = refreshedUser,
                        errorMessage = null
                    )
                    Log.d(TAG, "User profile refreshed from server: ${refreshedUser.name}")
                    _effect.emit(UserProfileEffect.ShowSuccessMessage("Perfil actualizado desde el servidor"))
                },
                onFailure = { error ->
                    val errorMessage = "Error refreshing user profile: ${error.message}"
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = errorMessage
                    )
                    Log.e(TAG, "Error refreshing user profile", error)
                    _effect.emit(UserProfileEffect.ShowErrorMessage(errorMessage))
                }
            )
        }
    }
    
    private fun retryLoadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val user = getCurrentUserUseCase.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    errorMessage = null
                )
                Log.d(TAG, "User retry loaded: ${user?.name}")
            } catch (e: Exception) {
                val errorMessage = "Error loading user data: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
                Log.e(TAG, "Error retry loading user", e)
            }
        }
    }
    
    private fun observeUserChanges() {
        viewModelScope.launch {
            getCurrentUserUseCase.getCurrentUserFlow()
                .catch { e ->
                    Log.e(TAG, "Error observing user changes", e)
                }
                .collect { user ->
                    if (!_uiState.value.isLoading && !_uiState.value.isRefreshing) {
                        _uiState.value = _uiState.value.copy(user = user)
                        Log.d(TAG, "User updated from flow: ${user?.name}")
                    }
                }
        }
    }
    
    private fun performLogout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            
            logoutUseCase().fold(
                onSuccess = {
                    Log.d(TAG, "Logout successful")
                    _uiState.value = _uiState.value.copy(isLoggingOut = false)
                    _effect.emit(UserProfileEffect.NavigateToLogin)
                },
                onFailure = { error ->
                    val errorMessage = "Error logging out: ${error.message}"
                    Log.e(TAG, "Logout failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        errorMessage = errorMessage
                    )
                    _effect.emit(UserProfileEffect.ShowErrorMessage(errorMessage))
                }
            )
        }
    }
    
    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}