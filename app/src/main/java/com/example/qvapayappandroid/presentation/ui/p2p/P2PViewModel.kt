package com.example.qvapayappandroid.presentation.ui.p2p

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class P2PViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(P2PUiState())
    val uiState: StateFlow<P2PUiState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<P2PEffect>()
    val effect: SharedFlow<P2PEffect> = _effect.asSharedFlow()
    
    init {
        loadP2PData()
    }
    
    private fun loadP2PData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Simular carga de datos P2P
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
                Log.d("P2PViewModel", "P2P data loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading P2P data: ${e.message}"
                )
                Log.e("P2PViewModel", "Error loading P2P data", e)
            }
        }
    }
    
    fun onSendMoney() {
        viewModelScope.launch {
            Log.d("P2PViewModel", "Send money action triggered")
            _effect.emit(P2PEffect.NavigateToSendMoney)
        }
    }
    
    fun onReceiveMoney() {
        viewModelScope.launch {
            Log.d("P2PViewModel", "Receive money action triggered")
            _effect.emit(P2PEffect.NavigateToReceiveMoney)
        }
    }
    
    fun onViewHistory() {
        viewModelScope.launch {
            Log.d("P2PViewModel", "View history action triggered")
            _effect.emit(P2PEffect.NavigateToHistory)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun refreshData() {
        loadP2PData()
    }
}

data class P2PUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed class P2PEffect {
    object NavigateToSendMoney : P2PEffect()
    object NavigateToReceiveMoney : P2PEffect()
    object NavigateToHistory : P2PEffect()
}