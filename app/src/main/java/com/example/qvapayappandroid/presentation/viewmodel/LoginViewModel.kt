package com.example.qvapayappandroid.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.LoginResponse
import com.example.qvapayappandroid.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    var uiState by mutableStateOf(LoginUiState())
        private set
    
    fun updateEmail(email: String) {
        uiState = uiState.copy(email = email, errorMessage = null)
    }
    
    fun updatePassword(password: String) {
        uiState = uiState.copy(password = password, errorMessage = null)
    }
    
    fun updateCode(code: String) {
        uiState = uiState.copy(code = code, errorMessage = null)
    }
    
    fun login() {
        if (uiState.isLoading) return
        
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val result = loginUseCase(
                email = uiState.email,
                password = uiState.password,
                code = uiState.code
            )
            
            result.fold(
                onSuccess = { response ->
                    uiState = uiState.copy(
                        isLoading = false,
                        loginResponse = response,
                        isLoggedIn = true // Si llegamos aquí, el login fue exitoso
                    )
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginResponse: LoginResponse? = null,
    val isLoggedIn: Boolean = false
)