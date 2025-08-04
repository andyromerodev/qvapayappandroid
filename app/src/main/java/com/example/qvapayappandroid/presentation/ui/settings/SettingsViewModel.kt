package com.example.qvapayappandroid.presentation.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.usecase.GetSettingsUseCase
import com.example.qvapayappandroid.domain.usecase.LogoutUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateBiometricUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateNotificationsUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateThemeUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateNotificationsUseCase: UpdateNotificationsUseCase,
    private val updateBiometricUseCase: UpdateBiometricUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()
    
    init {
        loadUserSettings()
    }
    
    private fun loadUserSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val userSettings = getSettingsUseCase()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userSettings = userSettings,
                    errorMessage = null
                )
                Log.d("SettingsViewModel", "User settings loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading settings: ${e.message}"
                )
                Log.e("SettingsViewModel", "Error loading settings", e)
            }
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updateNotificationsUseCase(enabled)
                val currentSettings = _uiState.value.userSettings
                _uiState.value = _uiState.value.copy(
                    userSettings = currentSettings.copy(notificationsEnabled = enabled)
                )
                Log.d("SettingsViewModel", "Notifications toggled: $enabled")
                _effect.emit(SettingsEffect.ShowMessage("Notificaciones ${if (enabled) "activadas" else "desactivadas"}"))
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating notifications", e)
                _effect.emit(SettingsEffect.ShowMessage("Error al actualizar notificaciones"))
            }
        }
    }
    
    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updateBiometricUseCase(enabled)
                val currentSettings = _uiState.value.userSettings
                _uiState.value = _uiState.value.copy(
                    userSettings = currentSettings.copy(biometricEnabled = enabled)
                )
                Log.d("SettingsViewModel", "Biometric authentication toggled: $enabled")
                _effect.emit(SettingsEffect.ShowMessage("Autenticación biométrica ${if (enabled) "activada" else "desactivada"}"))
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating biometric", e)
                _effect.emit(SettingsEffect.ShowMessage("Error al actualizar autenticación biométrica"))
            }
        }
    }
    
    fun changeTheme(theme: String) {
        viewModelScope.launch {
            try {
                updateThemeUseCase(theme)
                val currentSettings = _uiState.value.userSettings
                _uiState.value = _uiState.value.copy(
                    userSettings = currentSettings.copy(theme = theme)
                )
                Log.d("SettingsViewModel", "Theme changed to: $theme")
                _effect.emit(SettingsEffect.ShowMessage("Tema cambiado a $theme"))
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating theme", e)
                _effect.emit(SettingsEffect.ShowMessage("Error al actualizar tema"))
            }
        }
    }
    
    fun changeLanguage(language: String) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.userSettings
            _uiState.value = _uiState.value.copy(
                userSettings = currentSettings.copy(language = language)
            )
            Log.d("SettingsViewModel", "Language changed to: $language")
            _effect.emit(SettingsEffect.ShowMessage("Idioma cambiado a $language"))
        }
    }
    
    fun changePassword() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Change password action triggered")
            _effect.emit(SettingsEffect.ShowMessage("Funcionalidad de cambio de contraseña próximamente"))
        }
    }
    
    fun manageDevices() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Manage devices action triggered")
            _effect.emit(SettingsEffect.ShowMessage("Gestión de dispositivos próximamente"))
        }
    }
    
    fun privacySettings() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Privacy settings action triggered")
            _effect.emit(SettingsEffect.ShowMessage("Configuración de privacidad próximamente"))
        }
    }
    
    fun showAbout() {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Show about action triggered")
            _effect.emit(SettingsEffect.ShowMessage("QvaPay App v1.0.0"))
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            
            logoutUseCase().fold(
                onSuccess = {
                    Log.d("SettingsViewModel", "Logout successful")
                    _uiState.value = _uiState.value.copy(isLoggingOut = false)
                    _effect.emit(SettingsEffect.NavigateToLogin)
                },
                onFailure = { error ->
                    Log.e("SettingsViewModel", "Logout failed: ${error.message}")
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
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val userSettings: UserSettings = UserSettings(),
    val errorMessage: String? = null
)

data class UserSettings(
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val theme: String = "Sistema",
    val language: String = "Español"
)

sealed class SettingsEffect {
    object NavigateToLogin : SettingsEffect()
    data class ShowMessage(val message: String) : SettingsEffect()
}