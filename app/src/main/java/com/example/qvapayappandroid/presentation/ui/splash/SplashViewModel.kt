package com.example.qvapayappandroid.presentation.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.usecase.CheckSessionUseCase
import com.example.qvapayappandroid.domain.usecase.InitializeSettingsUseCase
import com.example.qvapayappandroid.navigation.AppDestinations
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val checkSessionUseCase: CheckSessionUseCase,
    private val initializeSettingsUseCase: InitializeSettingsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        initializeApp()
    }
    
    private fun initializeApp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Initialize default settings
                initializeSettingsUseCase()
                
                // Keep the splash visible for at least 2.5s so the animation plays
                delay(2500)
                
                // Check whether the user is already signed in
                val isLoggedIn = checkSessionUseCase()
                
                val destination = when {
                    isLoggedIn -> AppDestinations.Main.route
                    else -> AppDestinations.Login.route
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    navigationDestination = destination
                )
                
            } catch (e: Exception) {
                // On error, fall back to the login screen
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    navigationDestination = AppDestinations.Login.route
                )
            }
        }
    }
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val navigationDestination: String? = null
)
