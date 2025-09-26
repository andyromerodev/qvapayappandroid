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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * HomeViewModel following MVI pattern.
 * Handles all user intents and maintains state reactively.
 */
class HomeViewModel(
    private val getMyP2POffersUseCase: GetMyP2POffersUseCase,
    private val cancelP2POfferUseCase: CancelP2POfferUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = combine(
        getMyP2POffersUseCase.getMyOffersFlow(),
        _state
    ) { cachedOffers, currentState ->
        val filteredOffers = if (currentState.selectedStatusFilters.isEmpty()) {
            cachedOffers
        } else {
            filterOffers(cachedOffers, currentState.selectedStatusFilters)
        }
        
        currentState.copy(
            myOffers = cachedOffers,
            filteredOffers = filteredOffers
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState()
    )
    
    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()
    
    private var lastSyncTime = 0L
    private var lastRefreshTime = 0L
    private val minSyncInterval = 5000L
    private val minRefreshInterval = 3000L
    
    init {
        handleIntent(HomeIntent.LoadOffers)
    }
    
    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadOffers -> initialSync()
            is HomeIntent.RefreshOffers -> refreshOffers()
            is HomeIntent.LoadMoreOffers -> loadMoreOffers()
            is HomeIntent.ClearOffersError -> clearOffersError()
            is HomeIntent.ClearCancelOfferError -> clearCancelOfferError()
            is HomeIntent.ToggleStatusFilter -> toggleStatusFilter(intent.status)
            is HomeIntent.CancelOffer -> cancelOffer(intent.offerId, intent.onSuccess)
            is HomeIntent.GetOfferById -> {} // This is handled by the state directly
        }
    }
    
    // ========================================
    // INTENT HANDLERS
    // ========================================
    
    private fun initialSync() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Initial sync with API to populate cache")
            
            _state.value = _state.value.copy(
                isLoading = true,
                offersError = null
            )
            
            getMyP2POffersUseCase.syncOffers().fold(
                onSuccess = {
                    Log.d("HomeViewModel", "Initial sync successful")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        hasNextPage = true,
                        currentPage = 1
                    )
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Initial sync failed: ${error.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        offersError = "Error cargando ofertas: ${error.message}"
                    )
                }
            )
        }
    }
    
    private fun refreshOffers() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastRefreshTime < minRefreshInterval) {
                Log.d("HomeViewModel", "Refresh throttled, too soon since last refresh")
                return@launch
            }
            lastRefreshTime = currentTime
            
            Log.d("HomeViewModel", "Refreshing offers from API")
            
            _state.value = _state.value.copy(
                isRefreshing = true,
                offersError = null
            )
            
            getMyP2POffersUseCase.refreshOffers().fold(
                onSuccess = {
                    Log.d("HomeViewModel", "Refresh successful")
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        hasNextPage = true,
                        currentPage = 1
                    )
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Refresh failed: ${error.message}")
                    handleSyncError(error, isRefresh = true)
                }
            )
        }
    }
    
    private fun loadMoreOffers() {
        viewModelScope.launch {
            val currentState = _state.value
            
            if (currentState.isLoadingMore || !currentState.hasNextPage) {
                Log.d("HomeViewModel", "loadMoreOffers blocked - isLoadingMore: ${currentState.isLoadingMore}, hasNextPage: ${currentState.hasNextPage}")
                return@launch
            }
            
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastSyncTime < minSyncInterval) {
                Log.d("HomeViewModel", "Pagination throttled, too soon since last sync")
                return@launch
            }
            lastSyncTime = currentTime
            
            val nextPage = currentState.currentPage + 1
            Log.d("HomeViewModel", "Loading more offers - page $nextPage")
            
            _state.value = _state.value.copy(
                isLoadingMore = true,
                offersError = null
            )
            
            getMyP2POffersUseCase.syncOffers(nextPage).fold(
                onSuccess = {
                    Log.d("HomeViewModel", "Load more successful - page $nextPage")
                    _state.value = _state.value.copy(
                        isLoadingMore = false,
                        currentPage = nextPage
                    )
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Load more failed: ${error.message}")
                    _state.value = _state.value.copy(
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
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    offersError = "Demasiadas solicitudes, reintentando en ${delayTime/1000} segundos..."
                )
                
                delay(delayTime)
                
                if (isRefresh) {
                    handleIntent(HomeIntent.RefreshOffers)
                } else {
                    handleIntent(HomeIntent.LoadOffers)
                }
                return
            }
        }
        
        _state.value = _state.value.copy(
            isLoading = false,
            isRefreshing = false,
            isLoadingMore = false,
            offersError = "Error: $errorMessage"
        )
    }
    
    private fun clearOffersError() {
        _state.value = _state.value.copy(offersError = null)
    }
    
    private fun toggleStatusFilter(status: String) {
        val currentFilters = _state.value.selectedStatusFilters.toMutableSet()
        
        if (status.isEmpty()) {
            currentFilters.clear()
        } else {
            if (currentFilters.contains(status)) {
                currentFilters.remove(status)
            } else {
                currentFilters.add(status)
            }
        }
        
        _state.value = _state.value.copy(selectedStatusFilters = currentFilters)
    }
    
    fun getOfferById(offerId: String): P2POffer? {
        return state.value.myOffers.find { it.uuid == offerId }
    }
    
    private fun cancelOffer(offerId: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Cancelling offer: $offerId")
            
            _state.value = _state.value.copy(
                isCancellingOffer = offerId,
                cancelOfferError = null
            )
            
            cancelP2POfferUseCase(offerId).fold(
                onSuccess = { response ->
                    Log.d("HomeViewModel", "Offer cancelled successfully: ${response.msg}")
                    _state.value = _state.value.copy(
                        isCancellingOffer = null,
                        cancelOfferError = null
                    )
                    _effect.tryEmit(HomeEffect.ShowSuccessMessage("Oferta cancelada exitosamente"))
                    onSuccess?.invoke()
                },
                onFailure = { error ->
                    Log.e("HomeViewModel", "Failed to cancel offer: ${error.message}")
                    val errorMessage = error.message ?: "Error cancelando oferta"
                    _state.value = _state.value.copy(
                        isCancellingOffer = null,
                        cancelOfferError = errorMessage
                    )
                    _effect.tryEmit(HomeEffect.ShowErrorMessage(errorMessage))
                }
            )
        }
    }
    
    private fun clearCancelOfferError() {
        _state.value = _state.value.copy(cancelOfferError = null)
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
            else -> "pendiente" // default for unknown statuses
        }
    }
}