package com.example.qvapayappandroid.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.usecase.GetMyP2POffersUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getMyP2POffersUseCase: GetMyP2POffersUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var lastLoadTime = 0L
    private val minLoadInterval = 4000L // Mínimo 2 segundos entre cargas
    
    init {
        loadMyOffers()
    }
    
    private fun loadMyOffers(page: Int = 1, isRefresh: Boolean = false, retryCount: Int = 0) {
        viewModelScope.launch {
            // Prevenir solicitudes muy frecuentes
            val currentTime = System.currentTimeMillis()
            if (!isRefresh && currentTime - lastLoadTime < minLoadInterval) {
                Log.d("HomeViewModel", "Request throttled, too soon since last request")
                return@launch
            }
            lastLoadTime = currentTime
            
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(
                    isLoadingOffers = true, 
                    offersError = null,
                    currentPage = 1,
                    hasNextPage = true
                )
            } else if (page > 1) {
                _uiState.value = _uiState.value.copy(isLoadingMore = true, offersError = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoadingOffers = true, offersError = null)
            }
            
            getMyP2POffersUseCase(page).fold(
                onSuccess = { response ->
                    val newOffers = if (isRefresh || page == 1) {
                        response.data
                    } else {
                        _uiState.value.myOffers + response.data
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoadingOffers = false,
                        isLoadingMore = false,
                        myOffers = newOffers,
                        offersError = null,
                        currentPage = response.currentPage,
                        hasNextPage = response.currentPage < response.lastPage
                    )
                    Log.d("HomeViewModel", "My offers loaded: ${response.data.size} offers, page: $page, total pages: ${response.lastPage}")
                },
                onFailure = { error ->
                    handleLoadError(error, page, isRefresh, retryCount)
                }
            )
        }
    }
    
    private fun handleLoadError(error: Throwable, page: Int, isRefresh: Boolean, retryCount: Int) {
        viewModelScope.launch {
            val errorMessage = error.message ?: "Error desconocido"
            
            // Manejar error 429 (Too Many Requests) con retry automático
            if (errorMessage.contains("Too Many Attempts") || errorMessage.contains("429")) {
                if (retryCount < 3) {
                    val delayTime = when (retryCount) {
                        0 -> 5000L  // 5 segundos
                        1 -> 10000L // 10 segundos  
                        else -> 15000L // 15 segundos
                    }
                    
                    Log.d("HomeViewModel", "Rate limited, retrying in ${delayTime/1000} seconds (attempt ${retryCount + 1})")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoadingOffers = false,
                        isLoadingMore = false,
                        offersError = "Demasiadas solicitudes, reintentando en ${delayTime/1000} segundos..."
                    )
                    
                    delay(delayTime)
                    loadMyOffers(page, isRefresh, retryCount + 1)
                    return@launch
                }
            }
            
            _uiState.value = _uiState.value.copy(
                isLoadingOffers = false,
                isLoadingMore = false,
                offersError = "Error cargando ofertas: $errorMessage"
            )
            Log.e("HomeViewModel", "Error loading my offers: $errorMessage")
        }
    }
    
    fun refreshOffers() {
        loadMyOffers(page = 1, isRefresh = true)
    }
    
    fun loadMoreOffers() {
        if (!_uiState.value.isLoadingMore && _uiState.value.hasNextPage) {
            loadMyOffers(_uiState.value.currentPage + 1)
        }
    }
    
    fun clearOffersError() {
        _uiState.value = _uiState.value.copy(offersError = null)
    }
}

data class HomeUiState(
    val isLoadingOffers: Boolean = false,
    val myOffers: List<P2POffer> = emptyList(),
    val offersError: String? = null,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = true,
    val isLoadingMore: Boolean = false
)