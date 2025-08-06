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
    private var lastRefreshTime = 0L
    private val minLoadInterval = 1000L // Reducido a 1 segundo para paginación más fluida
    private val minRefreshInterval = 3000L // Reducido a 3 segundos para refresh
    
    init {
        loadMyOffers()
    }
    
    private fun loadMyOffers(page: Int = 1, isRefresh: Boolean = false, retryCount: Int = 0) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "loadMyOffers called - page: $page, isRefresh: $isRefresh, retryCount: $retryCount")
            Log.d("HomeViewModel", "Current state before load - isLoadingOffers: ${_uiState.value.isLoadingOffers}, isLoadingMore: ${_uiState.value.isLoadingMore}")
            
            // Prevenir solicitudes muy frecuentes según el tipo
            val currentTime = System.currentTimeMillis()
            
            if (isRefresh) {
                // Para refresh, verificar intervalo más largo
                if (currentTime - lastRefreshTime < minRefreshInterval) {
                    Log.d("HomeViewModel", "Refresh throttled, too soon since last refresh")
                    return@launch
                }
                lastRefreshTime = currentTime
            } else if (page > 1) {
                // Para paginación, verificar intervalo más corto solo si ya se hizo una carga reciente
                if (lastLoadTime > 0 && currentTime - lastLoadTime < minLoadInterval) {
                    Log.d("HomeViewModel", "Pagination throttled, too soon since last load")
                    return@launch
                }
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
                    
                    Log.d("HomeViewModel", "SUCCESS - Setting isLoadingOffers = false, isLoadingMore = false")
                    _uiState.value = _uiState.value.copy(
                        isLoadingOffers = false,
                        isLoadingMore = false,
                        myOffers = newOffers,
                        filteredOffers = filterOffers(newOffers, _uiState.value.selectedStatusFilters),
                        offersError = null,
                        currentPage = response.currentPage,
                        hasNextPage = response.currentPage < response.lastPage
                    )
                    Log.d("HomeViewModel", "My offers loaded: ${response.data.size} offers, page: $page, total pages: ${response.lastPage}")
                    Log.d("HomeViewModel", "Final state - isLoadingOffers: ${_uiState.value.isLoadingOffers}, isLoadingMore: ${_uiState.value.isLoadingMore}")
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
            
            Log.d("HomeViewModel", "ERROR - Setting isLoadingOffers = false, isLoadingMore = false")
            _uiState.value = _uiState.value.copy(
                isLoadingOffers = false,
                isLoadingMore = false,
                offersError = "Error cargando ofertas: $errorMessage"
            )
            Log.e("HomeViewModel", "Error loading my offers: $errorMessage")
            Log.d("HomeViewModel", "Error state - isLoadingOffers: ${_uiState.value.isLoadingOffers}, isLoadingMore: ${_uiState.value.isLoadingMore}")
        }
    }
    
    fun refreshOffers() {
        loadMyOffers(page = 1, isRefresh = true)
    }
    
    fun loadMoreOffers() {
        val currentState = _uiState.value
        Log.d("HomeViewModel", "loadMoreOffers called - isLoadingMore: ${currentState.isLoadingMore}, hasNextPage: ${currentState.hasNextPage}, currentPage: ${currentState.currentPage}")
        
        if (!currentState.isLoadingMore && currentState.hasNextPage) {
            val nextPage = currentState.currentPage + 1
            Log.d("HomeViewModel", "Loading page $nextPage")
            loadMyOffers(nextPage)
        } else {
            Log.d("HomeViewModel", "loadMoreOffers blocked - isLoadingMore: ${currentState.isLoadingMore}, hasNextPage: ${currentState.hasNextPage}")
        }
    }
    
    fun clearOffersError() {
        _uiState.value = _uiState.value.copy(offersError = null)
    }
    
    fun toggleStatusFilter(status: String) {
        val currentFilters = _uiState.value.selectedStatusFilters.toMutableSet()
        
        if (status.isEmpty()) {
            // Si se selecciona "TODAS", limpiar todos los filtros
            currentFilters.clear()
        } else {
            if (currentFilters.contains(status)) {
                currentFilters.remove(status)
            } else {
                currentFilters.add(status)
            }
        }
        
        val filteredOffers = filterOffers(_uiState.value.myOffers, currentFilters)
        
        _uiState.value = _uiState.value.copy(
            selectedStatusFilters = currentFilters,
            filteredOffers = filteredOffers
        )
    }
    
    private fun filterOffers(offers: List<P2POffer>, statusFilters: Set<String>): List<P2POffer> {
        if (statusFilters.isEmpty()) {
            return offers
        }
        
        return offers.filter { offer ->
            val offerStatus = mapOfferStatusToFilterKey(offer.status)
            statusFilters.contains(offerStatus)
        }
    }
    
    private fun mapOfferStatusToFilterKey(status: String?): String {
        return when (status?.lowercase()) {
            "open", "abierta", "activa" -> "activa"
            "completed", "completada", "finalizada" -> "completada"
            "cancelled", "cancelada" -> "cancelada"
            "paused", "pausada" -> "pausada"
            "pending", "pendiente" -> "pendiente"
            else -> "pendiente" // default para estados desconocidos
        }
    }
    
    fun getOfferById(offerId: String): P2POffer? {
        return _uiState.value.myOffers.find { it.uuid == offerId }
    }
}

data class HomeUiState(
    val isLoadingOffers: Boolean = false,
    val myOffers: List<P2POffer> = emptyList(),
    val filteredOffers: List<P2POffer> = emptyList(),
    val selectedStatusFilters: Set<String> = emptySet(),
    val offersError: String? = null,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = true,
    val isLoadingMore: Boolean = false
)