package com.example.qvapayappandroid.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.usecase.GetMyP2POffersUseCase
import com.example.qvapayappandroid.domain.usecase.CancelP2POfferUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * HomeViewModel with Single Source of Truth pattern using reactive flows.
 * Data flows from local cache (Room) automatically, API calls only sync in background.
 */
class HomeViewModel(
    private val getMyP2POffersUseCase: GetMyP2POffersUseCase,
    private val cancelP2POfferUseCase: CancelP2POfferUseCase
) : ViewModel() {
    
    // UI State for loading states and errors
    private val _loadingState = MutableStateFlow(LoadingState())
    private val _selectedStatusFilters = MutableStateFlow<Set<String>>(emptySet())
    
    // Reactive UI State that combines cache data with UI state
    val uiState: StateFlow<HomeUiState> = combine(
        getMyP2POffersUseCase.getMyOffersFlow(), // Reactive data from cache
        _selectedStatusFilters,
        _loadingState
    ) { cachedOffers, statusFilters, loadingState ->
        val filteredOffers = if (statusFilters.isEmpty()) {
            cachedOffers
        } else {
            filterOffers(cachedOffers, statusFilters)
        }
        
        HomeUiState(
            isLoadingOffers = loadingState.isLoadingOffers,
            isRefreshing = loadingState.isRefreshing,
            myOffers = cachedOffers,
            filteredOffers = filteredOffers,
            selectedStatusFilters = statusFilters,
            offersError = loadingState.offersError,
            isLoadingMore = loadingState.isLoadingMore,
            hasNextPage = loadingState.hasNextPage,
            isCancellingOffer = loadingState.isCancellingOffer,
            cancelOfferError = loadingState.cancelOfferError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
    
    private var lastSyncTime = 0L
    private var lastRefreshTime = 0L
    private val minSyncInterval = 5000L // 5 seconds between syncs
    private val minRefreshInterval = 3000L // 3 seconds between refreshes
    private var currentPage = 1
    
    init {
        // Initial sync with API to populate cache
        initialSync()
    }
    
    // ========================================
    // REACTIVE CACHE-FIRST METHODS
    // ========================================
    
    /**
     * Initial sync with API to populate cache.
     * Called once when ViewModel is created.
     */
    private fun initialSync() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Initial sync with API to populate cache")
            
            _loadingState.value = _loadingState.value.copy(
                isLoadingOffers = true,
                offersError = null
            )
            
            getMyP2POffersUseCase.syncOffers().fold(
                onSuccess = {
                    Log.d("HomeViewModel", "Initial sync successful")
                    _loadingState.value = _loadingState.value.copy(
                        isLoadingOffers = false,
                        hasNextPage = true // Assume there might be more pages initially
                    )
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Initial sync failed: ${error.message}")
                    _loadingState.value = _loadingState.value.copy(
                        isLoadingOffers = false,
                        offersError = "Error cargando ofertas: ${error.message}"
                    )
                }
            )
        }
    }
    
    /**
     * Refresh offers from API and update cache.
     * This clears existing cache and reloads from server.
     */
    fun refreshOffers() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            // Throttle refresh requests
            if (currentTime - lastRefreshTime < minRefreshInterval) {
                Log.d("HomeViewModel", "Refresh throttled, too soon since last refresh")
                return@launch
            }
            lastRefreshTime = currentTime
            
            Log.d("HomeViewModel", "Refreshing offers from API")
            
            _loadingState.value = _loadingState.value.copy(
                isRefreshing = true,
                offersError = null
            )
            
            currentPage = 1 // Reset pagination
            
            getMyP2POffersUseCase.refreshOffers().fold(
                onSuccess = {
                    Log.d("HomeViewModel", "Refresh successful")
                    _loadingState.value = _loadingState.value.copy(
                        isRefreshing = false,
                        hasNextPage = true // Reset pagination state
                    )
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Refresh failed: ${error.message}")
                    handleSyncError(error, isRefresh = true)
                }
            )
        }
    }
    
    /**
     * Load more offers (pagination) from API and append to cache.
     */
    fun loadMoreOffers() {
        viewModelScope.launch {
            val currentState = _loadingState.value
            
            if (currentState.isLoadingMore || !currentState.hasNextPage) {
                Log.d("HomeViewModel", "loadMoreOffers blocked - isLoadingMore: ${currentState.isLoadingMore}, hasNextPage: ${currentState.hasNextPage}")
                return@launch
            }
            
            val currentTime = System.currentTimeMillis()
            
            // Throttle pagination requests
            if (currentTime - lastSyncTime < minSyncInterval) {
                Log.d("HomeViewModel", "Pagination throttled, too soon since last sync")
                return@launch
            }
            lastSyncTime = currentTime
            
            val nextPage = currentPage + 1
            Log.d("HomeViewModel", "Loading more offers - page $nextPage")
            
            _loadingState.value = _loadingState.value.copy(
                isLoadingMore = true,
                offersError = null
            )
            
            getMyP2POffersUseCase.syncOffers(nextPage).fold(
                onSuccess = {
                    Log.d("HomeViewModel", "Load more successful - page $nextPage")
                    currentPage = nextPage
                    _loadingState.value = _loadingState.value.copy(
                        isLoadingMore = false
                        // hasNextPage will be determined by the actual API response
                        // For now, assume there might be more pages
                    )
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Load more failed: ${error.message}")
                    _loadingState.value = _loadingState.value.copy(
                        isLoadingMore = false,
                        offersError = "Error cargando más ofertas: ${error.message}"
                    )
                }
            )
        }
    }
    
    /**
     * Handle sync errors with automatic retry for rate limiting.
     */
    private suspend fun handleSyncError(error: Throwable, isRefresh: Boolean = false, retryCount: Int = 0) {
        val errorMessage = error.message ?: "Error desconocido"
        
        // Handle rate limiting with automatic retry
        if (errorMessage.contains("Too Many Attempts") || errorMessage.contains("429")) {
            if (retryCount < 3) {
                val delayTime = when (retryCount) {
                    0 -> 5000L  // 5 seconds
                    1 -> 10000L // 10 seconds  
                    else -> 15000L // 15 seconds
                }
                
                Log.d("HomeViewModel", "Rate limited, retrying in ${delayTime/1000} seconds (attempt ${retryCount + 1})")
                
                _loadingState.value = _loadingState.value.copy(
                    isLoadingOffers = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    offersError = "Demasiadas solicitudes, reintentando en ${delayTime/1000} segundos..."
                )
                
                delay(delayTime)
                
                if (isRefresh) {
                    refreshOffers()
                } else {
                    initialSync()
                }
                return
            }
        }
        
        _loadingState.value = _loadingState.value.copy(
            isLoadingOffers = false,
            isRefreshing = false,
            isLoadingMore = false,
            offersError = "Error: $errorMessage"
        )
    }
    
    /**
     * Clear error messages.
     */
    fun clearOffersError() {
        _loadingState.value = _loadingState.value.copy(offersError = null)
    }
    
    /**
     * Toggle status filter for offers.
     */
    fun toggleStatusFilter(status: String) {
        val currentFilters = _selectedStatusFilters.value.toMutableSet()
        
        if (status.isEmpty()) {
            // If "ALL" is selected, clear all filters
            currentFilters.clear()
        } else {
            if (currentFilters.contains(status)) {
                currentFilters.remove(status)
            } else {
                currentFilters.add(status)
            }
        }
        
        _selectedStatusFilters.value = currentFilters
    }
    
    /**
     * Filter offers by status.
     */
    private fun filterOffers(offers: List<P2POffer>, statusFilters: Set<String>): List<P2POffer> {
        if (statusFilters.isEmpty()) {
            return offers
        }
        
        return offers.filter { offer ->
            val offerStatus = mapOfferStatusToFilterKey(offer.status)
            statusFilters.contains(offerStatus)
        }
    }
    
    /**
     * Map offer status to filter key.
     */
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
    
    /**
     * Get offer by ID from current cache.
     * Note: This will automatically update when cache changes due to reactive flows.
     */
    fun getOfferById(offerId: String): P2POffer? {
        return uiState.value.myOffers.find { it.uuid == offerId }
    }
    
    /**
     * Cancel an offer.
     * This automatically updates the local cache and the UI will react to changes.
     */
    fun cancelOffer(offerId: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Cancelling offer: $offerId")
            
            _loadingState.value = _loadingState.value.copy(
                isCancellingOffer = offerId,
                cancelOfferError = null
            )
            
            cancelP2POfferUseCase(offerId).fold(
                onSuccess = { response ->
                    Log.d("HomeViewModel", "Offer cancelled successfully: ${response.msg}")
                    _loadingState.value = _loadingState.value.copy(
                        isCancellingOffer = null,
                        cancelOfferError = null
                    )
                    // No need to refresh - cache is automatically updated by repository
                    // Execute navigation callback if provided
                    onSuccess?.invoke()
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Failed to cancel offer: ${error.message}")
                    _loadingState.value = _loadingState.value.copy(
                        isCancellingOffer = null,
                        cancelOfferError = error.message ?: "Error cancelando oferta"
                    )
                }
            )
        }
    }
    
    /**
     * Clear cancel offer error.
     */
    fun clearCancelOfferError() {
        _loadingState.value = _loadingState.value.copy(cancelOfferError = null)
    }
}

/**
 * Internal loading state for the ViewModel.
 */
private data class LoadingState(
    val isLoadingOffers: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasNextPage: Boolean = true,
    val offersError: String? = null,
    val isCancellingOffer: String? = null,
    val cancelOfferError: String? = null
)

/**
 * UI State for the Home Screen with Single Source of Truth pattern.
 * Data comes from reactive flows, loading states are separate.
 */
data class HomeUiState(
    val isLoadingOffers: Boolean = false,
    val isRefreshing: Boolean = false,
    val myOffers: List<P2POffer> = emptyList(),
    val filteredOffers: List<P2POffer> = emptyList(),
    val selectedStatusFilters: Set<String> = emptySet(),
    val offersError: String? = null,
    val hasNextPage: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isCancellingOffer: String? = null,
    val cancelOfferError: String? = null
)