package com.example.qvapayappandroid.presentation.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.User
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

class UserProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<UserProfileEffect>()
    val effect: SharedFlow<UserProfileEffect> = _effect.asSharedFlow()
    
    init {
        loadUserData()
        observeUserChanges()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val user = getCurrentUserUseCase.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    errorMessage = null
                )
                Log.d("UserProfileViewModel", "User loaded: ${user?.name}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading user data: ${e.message}"
                )
                Log.e("UserProfileViewModel", "Error loading user", e)
            }
        }
    }
    
    private fun observeUserChanges() {
        viewModelScope.launch {
            getCurrentUserUseCase.getCurrentUserFlow()
                .catch { e ->
                    Log.e("UserProfileViewModel", "Error observing user changes", e)
                }
                .collect { user ->
                    if (!_uiState.value.isLoading) {
                        _uiState.value = _uiState.value.copy(user = user)
                    }
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            
            logoutUseCase().fold(
                onSuccess = {
                    Log.d("UserProfileViewModel", "Logout successful")
                    _uiState.value = _uiState.value.copy(isLoggingOut = false)
                    _effect.emit(UserProfileEffect.NavigateToLogin)
                },
                onFailure = { error ->
                    Log.e("UserProfileViewModel", "Logout failed: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoggingOut = false,
                        errorMessage = "Error logging out: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun refreshProfile() {
        loadUserData()
    }
}

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

sealed class UserProfileEffect {
    object NavigateToLogin : UserProfileEffect()
}