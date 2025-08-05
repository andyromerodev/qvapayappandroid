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
                // Inicializar configuraciones por defecto
                initializeSettingsUseCase()
                
                // Mostrar splash por al menos 2.5 segundos para la animación
                delay(2500)
                
                // Verificar si el usuario está logueado
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
                // En caso de error, ir a login
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