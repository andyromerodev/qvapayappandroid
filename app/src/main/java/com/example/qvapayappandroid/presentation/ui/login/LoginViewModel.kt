package com.example.qvapayappandroid.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()
    
    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> updateEmail(intent.email)
            is LoginIntent.UpdatePassword -> updatePassword(intent.password)
            is LoginIntent.UpdateCode -> updateCode(intent.code)
            is LoginIntent.Login -> login()
            is LoginIntent.ClearError -> clearError()
        }
    }
    
    private fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, errorMessage = null)
    }
    
    private fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, errorMessage = null)
    }
    
    private fun updateCode(code: String) {
        _state.value = _state.value.copy(code = code, errorMessage = null)
    }
    
    private fun login() {
        if (_state.value.isLoading) return
        
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val result = loginUseCase(
                email = _state.value.email,
                password = _state.value.password,
                code = _state.value.code
            )
            
            result.fold(
                onSuccess = { response ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        loginResponse = response,
                        errorMessage = null
                    )
                    _effect.emit(LoginEffect.ShowSuccessMessage("¡Inicio de sesión exitoso!"))
                    _effect.emit(LoginEffect.NavigateToHome)
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Error desconocido al iniciar sesión"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                    _effect.emit(LoginEffect.ShowErrorMessage(errorMessage))
                }
            )
        }
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
