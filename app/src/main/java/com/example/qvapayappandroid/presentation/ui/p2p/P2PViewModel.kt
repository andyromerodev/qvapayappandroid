package com.example.qvapayappandroid.presentation.ui.p2p

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.usecase.GetP2POffersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class P2PViewModel(
    private val getP2POffersUseCase: GetP2POffersUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(P2PUiState())
    val uiState: StateFlow<P2PUiState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<P2PEffect>()
    val effect: SharedFlow<P2PEffect> = _effect.asSharedFlow()
    
    private var loadDataJob: Job? = null
    
    // Removed automatic loading from init - now loads only when P2PScreen is opened
    
    private fun loadP2PDataDebounced() {
        // Cancel previous request if still running
        loadDataJob?.cancel()
        
        loadDataJob = viewModelScope.launch {
            // Add small delay to prevent rapid successive calls
            delay(300)
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentState = _uiState.value
                val filters = P2PFilterRequest(
                    type = if (currentState.selectedOfferType == "all") null else currentState.selectedOfferType,
                    coin = if (currentState.selectedCoin == "all") null else currentState.selectedCoin,
                    page = currentState.currentPage
                )
                
                Log.d("P2PViewModel", "Loading P2P offers with filters: $filters")
                
                getP2POffersUseCase(filters).fold(
                    onSuccess = { response ->
                        val newOffers = if (currentState.currentPage == 1) {
                            response.data
                        } else {
                            _uiState.value.offers + response.data
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = null,
                            loadMoreError = null, // Clear on success
                            offers = newOffers,
                            currentPage = response.currentPage,
                            totalPages = response.lastPage,
                            totalOffers = response.total
                        )
                        Log.d("P2PViewModel", "P2P offers loaded: ${response.data.size} offers, page ${response.currentPage}/${response.lastPage}")
                    },
                    onFailure = { error ->
                        val currentStateForError = _uiState.value
                        if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                            // Error en paginación - mantener ofertas existentes
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                loadMoreError = "Error loading more offers: ${error.message}"
                            )
                        } else {
                            // Error en primera carga - limpiar ofertas
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                errorMessage = "Error loading P2P offers: ${error.message}",
                                offers = emptyList()
                            )
                        }
                        Log.e("P2PViewModel", "Error loading P2P offers", error)
                    }
                )
            } catch (e: Exception) {
                val currentStateForError = _uiState.value
                if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                    // Error en paginación - mantener ofertas existentes
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        loadMoreError = "Unexpected error: ${e.message}"
                    )
                } else {
                    // Error en primera carga - limpiar ofertas
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = "Unexpected error: ${e.message}",
                        offers = emptyList()
                    )
                }
                Log.e("P2PViewModel", "Unexpected error loading P2P data", e)
            }
        }
    }
    
    /**
     * Carga las ofertas P2P. Se debe llamar manualmente cuando se abra P2PScreen
     */
    fun loadP2PData() {
        loadP2PDataDebounced()
    }
    
    /**
     * Refresca las ofertas P2P manteniendo los filtros aplicados
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                val currentState = _uiState.value
                val filters = P2PFilterRequest(
                    type = if (currentState.selectedOfferType == "all") null else currentState.selectedOfferType,
                    coin = if (currentState.selectedCoins.isEmpty()) null else currentState.selectedCoins.joinToString(","),
                    page = 1 // Resetear a la primera página en refresh
                )
                
                Log.d("P2PViewModel", "Refreshing P2P offers with filters: $filters")
                
                getP2POffersUseCase(filters).fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            offers = response.data,
                            currentPage = response.currentPage,
                            totalPages = response.lastPage,
                            totalOffers = response.total,
                            errorMessage = null
                        )
                        Log.d("P2PViewModel", "Refresh successful - ${response.data.size} offers loaded")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            errorMessage = error.message
                        )
                        Log.e("P2PViewModel", "Refresh failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = "Error inesperado al refrescar: ${e.message}"
                )
                Log.e("P2PViewModel", "Refresh error: ${e.message}", e)
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
    
    fun clearLoadMoreError() {
        _uiState.value = _uiState.value.copy(loadMoreError = null)
    }
    
    fun retryLoadMore() {
        val currentError = _uiState.value.loadMoreError
        val is429Error = currentError?.contains("429") == true || currentError?.contains("Too Many Attempts") == true
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRetrying = true,
                loadMoreError = if (is429Error) "Esperando 15 segundos antes de reintentar..." else null
            )
            
            if (is429Error) {
                Log.d("P2PViewModel", "HTTP 429 detected, waiting 15 seconds before retry")
                delay(15000) // 15 segundos de espera
            }
            
            _uiState.value = _uiState.value.copy(
                isRetrying = false,
                loadMoreError = null
            )
            
            onNextPage()
        }
    }
    
    fun retryFirstLoad() {
        val currentError = _uiState.value.errorMessage
        val is429Error = currentError?.contains("429") == true || currentError?.contains("Too Many Attempts") == true
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRetryingFirstLoad = true,
                errorMessage = if (is429Error) currentError else null
            )
            
            if (is429Error) {
                Log.d("P2PViewModel", "HTTP 429 detected on first load, waiting 15 seconds before retry")
                delay(15000) // 15 segundos de espera
            }
            
            _uiState.value = _uiState.value.copy(
                isRetryingFirstLoad = false,
                errorMessage = null,
                currentPage = 1,
                offers = emptyList()
            )
            
            loadP2PData()
        }
    }
    
    fun onOfferTypeChanged(offerType: String) {
        _uiState.value = _uiState.value.copy(
            selectedOfferType = offerType,
            currentPage = 1,
            offers = emptyList() // Clear offers when changing filters
        )
        loadP2PData()
    }
    
    fun onCoinChanged(coin: String) {
        _uiState.value = _uiState.value.copy(
            selectedCoin = coin,
            currentPage = 1,
            offers = emptyList() // Clear offers when changing filters
        )
        loadP2PData()
    }
    
    fun applyFilters(offerType: String, coins: List<String>) {
        _uiState.value = _uiState.value.copy(
            selectedOfferType = offerType,
            selectedCoins = coins,
            selectedCoin = if (coins.isEmpty()) "all" else coins.firstOrNull() ?: "all",
            currentPage = 1,
            offers = emptyList() // Clear offers when applying filters
        )
        loadP2PDataImmediate()
    }
    
    private fun loadP2PDataImmediate() {
        // Cancel previous request if still running
        loadDataJob?.cancel()
        
        loadDataJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentState = _uiState.value
                val coinsToQuery = currentState.selectedCoins.ifEmpty {
                    listOf("all") // Si no hay monedas seleccionadas, buscar todas
                }
                
                Log.d("P2PViewModel", "Loading P2P offers for coins: $coinsToQuery")
                
                // Hacer peticiones paralelas para cada moneda
                val deferredResults = coinsToQuery.map { coin ->
                    async {
                        val filters = P2PFilterRequest(
                            type = if (currentState.selectedOfferType == "all") null else currentState.selectedOfferType,
                            coin = if (coin == "all") null else coin,
                            page = currentState.currentPage
                        )
                        
                        Log.d("P2PViewModel", "Loading P2P offers with filters: $filters")
                        getP2POffersUseCase(filters)
                    }
                }
                
                // Esperar a que todas las peticiones se completen
                val results = deferredResults.awaitAll()
                
                // Combinar resultados exitosos
                val allOffers = mutableListOf<P2POffer>()
                var totalOffersCount = 0
                var maxPages = 1
                var hasError = false
                var errorMessage = ""
                
                results.forEach { result ->
                    result.fold(
                        onSuccess = { response ->
                            allOffers.addAll(response.data)
                            totalOffersCount += response.total
                            maxPages = maxOf(maxPages, response.lastPage)
                        },
                        onFailure = { error ->
                            hasError = true
                            errorMessage = "Error loading P2P offers: ${error.message}"
                            Log.e("P2PViewModel", "Error loading P2P offers", error)
                        }
                    )
                }
                
                if (hasError) {
                    val currentStateForError = _uiState.value
                    if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                        // Error en paginación - mantener ofertas existentes
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            loadMoreError = errorMessage
                        )
                    } else {
                        // Error en primera carga - limpiar ofertas
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = errorMessage,
                            offers = emptyList()
                        )
                    }
                } else {
                    // Deduplicar ofertas por UUID
                    val uniqueOffers = allOffers.distinctBy { it.uuid }
                    
                    val finalOffers = if (currentState.currentPage == 1) {
                        uniqueOffers
                    } else {
                        (_uiState.value.offers + uniqueOffers).distinctBy { it.uuid }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = null,
                        loadMoreError = null, // Clear on success
                        offers = finalOffers,
                        currentPage = currentState.currentPage,
                        totalPages = maxPages,
                        totalOffers = finalOffers.size
                    )
                    Log.d("P2PViewModel", "P2P offers loaded: ${uniqueOffers.size} unique offers from ${coinsToQuery.size} coin(s)")
                }
                
            } catch (e: Exception) {
                val currentStateForError = _uiState.value
                if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                    // Error en paginación - mantener ofertas existentes
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        loadMoreError = "Error loading P2P offers: ${e.message}"
                    )
                } else {
                    // Error en primera carga - limpiar ofertas
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = "Error loading P2P offers: ${e.message}",
                        offers = emptyList()
                    )
                }
                Log.e("P2PViewModel", "Error loading P2P offers", e)
            }
        }
    }
    
    fun onPageChanged(page: Int) {
        if (page in 1.._uiState.value.totalPages) {
            _uiState.value = _uiState.value.copy(currentPage = page)
            loadP2PDataImmediate()
        }
    }
    
    fun onNextPage() {
        // Prevent navigation if already loading
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore) {
            Log.d("P2PViewModel", "Next page blocked - already loading")
            return
        }
        
        val currentPage = _uiState.value.currentPage
        val totalPages = _uiState.value.totalPages
        if (currentPage < totalPages) {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            onPageChanged(currentPage + 1)
        }
    }
    
    fun onPreviousPage() {
        // Prevent navigation if already loading
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore) {
            Log.d("P2PViewModel", "Previous page blocked - already loading")
            return
        }
        
        val currentPage = _uiState.value.currentPage
        if (currentPage > 1) {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            onPageChanged(currentPage - 1)
        }
    }

    fun onSortByChanged(sortBy: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
    }

    fun onSortOrderToggled() {
        _uiState.value = _uiState.value.copy(sortAsc = !_uiState.value.sortAsc)
    }

}

data class P2PUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val loadMoreError: String? = null,
    val isRetrying: Boolean = false,
    val isRetryingFirstLoad: Boolean = false,
    val offers: List<P2POffer> = emptyList(),
    val selectedOfferType: String = "all", // "all", "buy", "sell"
    val selectedCoin: String = "all", // "all" or specific coin
    val selectedCoins: List<String> = emptyList(), // Lista de monedas seleccionadas
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalOffers: Int = 0,
    val availableCoins: List<String> = listOf(
        "SOL", "SBERBANK", "BANK_CUP", "ZELLE", "TROPIPAY", "ETECSA", 
        "USDCASH", "CLASICA", "BANK_MLC", "NEOMOON", "USDT", "BANK_EUR", 
        "QVAPAY", "BANDECPREPAGO", "CUPCASH", "WISE", "EURCASH", "USDTBSC", "BOLSATM"
    ),
    val sortBy: String = "ratio",    // "ratio" o "nombre"
    val sortAsc: Boolean = false
)

sealed class P2PEffect {
    object NavigateToSendMoney : P2PEffect()
    object NavigateToReceiveMoney : P2PEffect()
    object NavigateToHistory : P2PEffect()
}