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

class P2PViewModel(
    private val getP2POffersUseCase: GetP2POffersUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(P2PUiState())
    val uiState: StateFlow<P2PUiState> = _uiState.asStateFlow()
    
    private var loadDataJob: Job? = null
    private var hasInitialLoadStarted = false
    
    private fun logP2POffersDetails(offers: List<P2POffer>, context: String = "") {
        Log.d("P2POffers", "===== P2P OFFERS LOG ${if (context.isNotEmpty()) "($context)" else ""} =====")
        Log.d("P2POffers", "Total offers: ${offers.size}")
        Log.d("P2POffers", "========================================")
        
        offers.forEachIndexed { index, offer ->
            Log.d("P2POffers", "--- OFFER ${index + 1} ---")
            Log.d("P2POffers", "UUID: ${offer.uuid}")
            Log.d("P2POffers", "Type: ${offer.type}")
            Log.d("P2POffers", "Coin: ${offer.coin}")
            Log.d("P2POffers", "Peer ID: ${offer.peerId}")
            Log.d("P2POffers", "Amount: ${offer.amount}")
            Log.d("P2POffers", "Receive: ${offer.receive}")
            Log.d("P2POffers", "Details: ${offer.details}")
            Log.d("P2POffers", "Message: ${offer.message}")
            Log.d("P2POffers", "Only KYC: ${offer.onlyKyc}")
            Log.d("P2POffers", "Private: ${offer.private}")
            Log.d("P2POffers", "Only VIP: ${offer.onlyVip}")
            Log.d("P2POffers", "Status: ${offer.status}")
            Log.d("P2POffers", "TX ID: ${offer.txId}")
            Log.d("P2POffers", "Created At: ${offer.createdAt}")
            Log.d("P2POffers", "Updated At: ${offer.updatedAt}")
            Log.d("P2POffers", "Valid: ${offer.valid}")
            Log.d("P2POffers", "Coin Data: ${offer.coinData}")
            Log.d("P2POffers", "Owner: ${offer.owner}")
            Log.d("P2POffers", "Peer: ${offer.peer}")
            Log.d("P2POffers", "")
        }
        Log.d("P2POffers", "===== END OF P2P OFFERS LOG =====")
    }
    
    private fun loadP2PDataDebounced() {
        // Cancel previous request if still running
        loadDataJob?.cancel()
        
        loadDataJob = viewModelScope.launch {
            // Increased delay to prevent rapid successive calls when filtering
            delay(1000)
            
            // Don't change isFiltering if it's currently true (filtering in progress)
            val isCurrentlyFiltering = _uiState.value.isFiltering
            Log.d("P2PViewModel", "loadP2PDataDebounced - isCurrentlyFiltering = $isCurrentlyFiltering")
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                allowAutoPagination = _uiState.value.allowAutoPagination
            )
            
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
                        
                        // Procesar números de teléfono
                        val newPhoneNumbers = processOffersWithPhoneNumbers(response.data)
                        val updatedPhoneNumbers = _uiState.value.phoneNumbersMap + newPhoneNumbers
                        
                        Log.d("P2PViewModel", "loadP2PDataDebounced - SUCCESS: clearing isFiltering, offers = ${newOffers.size}")
                        val currentAllowAutoPagination = _uiState.value.allowAutoPagination
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            isFiltering = false, // Clear filtering state
                            errorMessage = null,
                            loadMoreError = null, // Clear on success
                            offers = newOffers,
                            currentPage = response.currentPage,
                            totalPages = response.lastPage,
                            totalOffers = response.total,
                            phoneNumbersMap = updatedPhoneNumbers,
                            allowAutoPagination = currentAllowAutoPagination // Preserve current value
                        )
                        
                        // Enable auto-pagination after initial load completes successfully
                        if (currentState.currentPage == 1) {
                            viewModelScope.launch {
                                delay(3000) // Wait 3 seconds before allowing auto-pagination
                                _uiState.value = _uiState.value.copy(allowAutoPagination = true)
                                Log.d("P2PViewModel", "Auto-pagination enabled after initial load")
                            }
                        }
                        Log.d("P2PViewModel", "P2P offers loaded: ${response.data.size} offers, page ${response.currentPage}/${response.lastPage}")
                        Log.d("P2PViewModel", "loadP2PDataDebounced - Final state: isFiltering = ${_uiState.value.isFiltering}")
                        
                        // Log detailed offer information
                        logP2POffersDetails(response.data, "loadP2PDataDebounced - New offers")
                    },
                    onFailure = { error ->
                        val currentStateForError = _uiState.value
                        if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                            // Error en paginación - mantener ofertas existentes
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                loadMoreError = "Error loading more offers: ${error.message}",
                                allowAutoPagination = _uiState.value.allowAutoPagination
                            )
                        } else {
                            // Error en primera carga - limpiar ofertas
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                isFiltering = false, // Clear filtering state on error
                                errorMessage = "Error loading P2P offers: ${error.message}",
                                allowAutoPagination = _uiState.value.allowAutoPagination
                                //offers = emptyList()
                            )
                        }
                        Log.e("P2PViewModel", "Error loading P2P offers", error)
                    }
                )
            } catch (e: Exception) {
                // Ignore CancellationException as it's expected when cancelling previous requests
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d("P2PViewModel", "Coroutine cancelled in loadP2PDataDebounced - ignoring")
                    return@launch
                }
                
                val currentStateForError = _uiState.value
                if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                    // Error en paginación - mantener ofertas existentes
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        loadMoreError = "Unexpected error: ${e.message}",
                        allowAutoPagination = _uiState.value.allowAutoPagination
                    )
                } else {
                    // Error en primera carga - limpiar ofertas
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isFiltering = false, // Clear filtering state on error
                        errorMessage = "Unexpected error: ${e.message}",
                        allowAutoPagination = _uiState.value.allowAutoPagination
                        //offers = emptyList()
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
        if (!hasInitialLoadStarted) {
            hasInitialLoadStarted = true
            Log.d("P2PViewModel", "loadP2PData - First time loading, proceeding...")
            loadP2PDataDebounced()
        } else {
            Log.d("P2PViewModel", "loadP2PData - Already started initial load, skipping...")
        }
    }
    
    /**
     * Refresca las ofertas P2P manteniendo los filtros aplicados
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                allowAutoPagination = _uiState.value.allowAutoPagination
            )
            
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
                        // Procesar números de teléfono
                        val phoneNumbers = processOffersWithPhoneNumbers(response.data)
                        
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            offers = response.data,
                            currentPage = response.currentPage,
                            totalPages = response.lastPage,
                            totalOffers = response.total,
                            errorMessage = null,
                            phoneNumbersMap = phoneNumbers,
                            allowAutoPagination = _uiState.value.allowAutoPagination
                        )
                        Log.d("P2PViewModel", "Refresh successful - ${response.data.size} offers loaded")
                        
                        // Log detailed offer information
                        logP2POffersDetails(response.data, "refresh")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            errorMessage = error.message,
                            allowAutoPagination = _uiState.value.allowAutoPagination
                        )
                        Log.e("P2PViewModel", "Refresh failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = "Error inesperado al refrescar: ${e.message}",
                    allowAutoPagination = _uiState.value.allowAutoPagination
                )
                Log.e("P2PViewModel", "Refresh error: ${e.message}", e)
            }
        }
    }

    fun retryLoadMore() {
        val currentError = _uiState.value.loadMoreError
        val is429Error = currentError?.contains("429") == true || currentError?.contains("Too Many Attempts") == true
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRetrying = true,
                loadMoreError = if (is429Error) "Esperando 15 segundos antes de reintentar..." else null,
                allowAutoPagination = _uiState.value.allowAutoPagination
            )
            
            if (is429Error) {
                Log.d("P2PViewModel", "HTTP 429 detected, waiting 15 seconds before retry")
                delay(15000) // 15 segundos de espera
            }
            
            _uiState.value = _uiState.value.copy(
                isRetrying = false,
                loadMoreError = null,
                allowAutoPagination = _uiState.value.allowAutoPagination
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
                errorMessage = if (is429Error) currentError else null,
                allowAutoPagination = _uiState.value.allowAutoPagination
            )
            
            if (is429Error) {
                Log.d("P2PViewModel", "HTTP 429 detected on first load, waiting 15 seconds before retry")
                delay(15000) // 15 segundos de espera
            }
            
            _uiState.value = _uiState.value.copy(
                isRetryingFirstLoad = false,
                errorMessage = null,
                currentPage = 1,
                allowAutoPagination = _uiState.value.allowAutoPagination
                //offers = emptyList()
            )
            
            loadP2PData()
        }
    }

    fun applyFilters(offerType: String, coins: List<String>) {
        Log.d("P2PViewModel", "applyFilters called - setting isFiltering = true")
        _uiState.value = _uiState.value.copy(
            selectedOfferType = offerType,
            selectedCoins = coins,
            selectedCoin = if (coins.isEmpty()) "all" else coins.firstOrNull() ?: "all",
            currentPage = 1,
            //offers = emptyList(), // Clear offers when applying filters
            errorMessage = null, // Clear any previous error messages
            loadMoreError = null, // Clear any pagination errors
            isFiltering = true, // Set filtering state immediately
            allowAutoPagination = _uiState.value.allowAutoPagination
        )
        Log.d("P2PViewModel", "applyFilters - isFiltering set to: ${_uiState.value.isFiltering}")
        loadP2PDataImmediate()
    }
    
    private fun loadP2PDataImmediate() {
        // Cancel previous request if still running
        loadDataJob?.cancel()
        
        loadDataJob = viewModelScope.launch {
            Log.d("P2PViewModel", "loadP2PDataImmediate started - isFiltering = ${_uiState.value.isFiltering}")
            try {
                val currentState = _uiState.value
                val coinsToQuery = currentState.selectedCoins.ifEmpty {
                    listOf("all") // Si no hay monedas seleccionadas, buscar todas
                }
                
                Log.d("P2PViewModel", "Loading P2P offers for coins: $coinsToQuery")
                
                // Hacer peticiones SECUENCIALES para respetar el throttling
                val allOffers = mutableListOf<P2POffer>()
                var totalOffersCount = 0
                var maxPages = 1
                var hasError = false
                var errorMessage = ""
                var allCoinsCancelled = true // Track if all requests were cancelled
                
                for (coin in coinsToQuery) {
                    try {
                        val filters = P2PFilterRequest(
                            type = if (currentState.selectedOfferType == "all") null else currentState.selectedOfferType,
                            coin = if (coin == "all") null else coin,
                            page = currentState.currentPage
                        )
                        
                        Log.d("P2PViewModel", "Loading P2P offers sequentially with filters: $filters")
                        
                        getP2POffersUseCase(filters).fold(
                            onSuccess = { response ->
                                allCoinsCancelled = false // At least one request succeeded
                                allOffers.addAll(response.data)
                                totalOffersCount += response.total
                                maxPages = maxOf(maxPages, response.lastPage)
                                Log.d("P2PViewModel", "Successfully loaded ${response.data.size} offers for coin: $coin")
                            },
                            onFailure = { error ->
                                // Ignore CancellationException as it's expected when cancelling previous requests
                                if (error is kotlinx.coroutines.CancellationException) {
                                    Log.d("P2PViewModel", "Request cancelled for coin $coin - ignoring as expected")
                                    return@fold // Skip this coin and continue with others
                                }
                                
                                allCoinsCancelled = false // At least one request completed (even with error)
                                
                                hasError = true
                                val isRateLimitError = error.message?.contains("429") == true || 
                                                     error.message?.contains("Too Many") == true
                                errorMessage = if (isRateLimitError) {
                                    "API rate limit reached. Please wait before filtering again."
                                } else {
                                    "Error loading P2P offers for coin $coin: ${error.message}"
                                }
                                Log.e("P2PViewModel", "Error loading P2P offers for coin: $coin (Rate limit: $isRateLimitError)", error)
                                
                                // If rate limited, break the loop to prevent further requests
                                if (isRateLimitError) {
                                    Log.w("P2PViewModel", "Rate limit detected - stopping further coin requests")
                                    return@fold // Exit the fold, but continue in the loop
                                }
                            }
                        )
                        
                        // Add small delay between sequential requests to be extra safe
                        if (coin != coinsToQuery.last()) {
                            Log.d("P2PViewModel", "Waiting 1s before next coin request...")
                            delay(1000)
                        }
                        
                    } catch (e: Exception) {
                        // Ignore CancellationException as it's expected when cancelling previous requests
                        if (e is kotlinx.coroutines.CancellationException) {
                            Log.d("P2PViewModel", "Request cancelled for coin $coin in catch block - ignoring as expected")
                            continue // Skip this coin and continue with the next one
                        }
                        
                        allCoinsCancelled = false // At least one request completed (even with error)
                        
                        hasError = true
                        errorMessage = "Unexpected error for coin $coin: ${e.message}"
                        Log.e("P2PViewModel", "Unexpected error for coin: $coin", e)
                        // Continue with other coins
                    }
                }
                
                // If all requests were cancelled, don't update the state - keep isFiltering = true
                if (allCoinsCancelled) {
                    Log.d("P2PViewModel", "All coin requests were cancelled - keeping current state")
                    return@launch
                }
                
                if (hasError) {
                    val currentStateForError = _uiState.value
                    if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                        // Error en paginación - mantener ofertas existentes
                        _uiState.value = _uiState.value.copy(
                            isFiltering = false,
                            isLoadingMore = false,
                            loadMoreError = errorMessage,
                            allowAutoPagination = _uiState.value.allowAutoPagination
                        )
                    } else {
                        // Error en primera carga - limpiar ofertas
                        Log.d("P2PViewModel", "loadP2PDataImmediate - ERROR (hasError): setting isFiltering = false")
                        _uiState.value = _uiState.value.copy(
                            isFiltering = false,
                            isLoadingMore = false,
                            errorMessage = errorMessage,
                            totalOffers = 0, // Reset totalOffers on error
                            allowAutoPagination = _uiState.value.allowAutoPagination
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
                    
                    // Procesar números de teléfono para todas las ofertas finales
                    val phoneNumbers = processOffersWithPhoneNumbers(finalOffers)
                    
                    Log.d("P2PViewModel", "loadP2PDataImmediate - SUCCESS: setting isFiltering = false, offers count = ${finalOffers.size}")
                    _uiState.value = _uiState.value.copy(
                        isFiltering = false,
                        isLoadingMore = false,
                        errorMessage = null,
                        loadMoreError = null, // Clear on success
                        offers = finalOffers,
                        currentPage = currentState.currentPage,
                        totalPages = maxPages,
                        totalOffers = finalOffers.size,
                        phoneNumbersMap = phoneNumbers,
                        allowAutoPagination = _uiState.value.allowAutoPagination
                    )
                    Log.d("P2PViewModel", "loadP2PDataImmediate - Final state: isFiltering = ${_uiState.value.isFiltering}")
                    Log.d("P2PViewModel", "P2P offers loaded: ${uniqueOffers.size} unique offers from ${coinsToQuery.size} coin(s)")
                    
                    // Log detailed offer information
                    logP2POffersDetails(uniqueOffers, "loadP2PDataImmediate - Unique offers")
                    logP2POffersDetails(finalOffers, "loadP2PDataImmediate - Final offers")
                }
                
            } catch (e: Exception) {
                // Ignore CancellationException as it's expected when cancelling previous requests
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d("P2PViewModel", "Coroutine cancelled - ignoring as this is expected during filtering")
                    return@launch
                }
                
                val currentStateForError = _uiState.value
                if (currentStateForError.currentPage > 1 && currentStateForError.offers.isNotEmpty()) {
                    // Error en paginación - mantener ofertas existentes
                    _uiState.value = _uiState.value.copy(
                        isFiltering = false,
                        isLoadingMore = false,
                        loadMoreError = "Error loading P2P offers: ${e.message}",
                        allowAutoPagination = _uiState.value.allowAutoPagination
                    )
                } else {
                    // Error en primera carga - limpiar ofertas
                    Log.d("P2PViewModel", "loadP2PDataImmediate - ERROR (catch): setting isFiltering = false")
                    _uiState.value = _uiState.value.copy(
                        isFiltering = false,
                        isLoadingMore = false,
                        errorMessage = "Error loading P2P offers: ${e.message}",
                        totalOffers = 0, // Reset totalOffers on error
                        allowAutoPagination = _uiState.value.allowAutoPagination
                    )
                }
                Log.e("P2PViewModel", "Error loading P2P offers", e)
            }
        }
    }
    
    fun onPageChanged(page: Int) {
        if (page in 1.._uiState.value.totalPages) {
            _uiState.value = _uiState.value.copy(
                currentPage = page, 
                //isFiltering = true,
                errorMessage = null, // Clear any previous errors
                loadMoreError = null,
                allowAutoPagination = _uiState.value.allowAutoPagination
            )
            loadP2PDataImmediate()
        }
    }
    
    fun onNextPage() {
        // Prevent navigation if already loading
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore) {
            Log.d("P2PViewModel", "Next page blocked - already loading")
            return
        }
        
        // Prevent automatic pagination during initial load or filtering
        if (_uiState.value.isFiltering) {
            Log.d("P2PViewModel", "Next page blocked - currently filtering")
            return
        }
        
        val currentPage = _uiState.value.currentPage
        val totalPages = _uiState.value.totalPages
        Log.d("P2PViewModel", "onNextPage called - currentPage: $currentPage, totalPages: $totalPages")
        if (currentPage < totalPages) {
            _uiState.value = _uiState.value.copy(
                isLoadingMore = true,
                allowAutoPagination = _uiState.value.allowAutoPagination
            )
            onPageChanged(currentPage + 1)
        } else {
            Log.d("P2PViewModel", "Next page blocked - already at last page")
        }
    }

    fun onSortByChanged(sortBy: String) {
        _uiState.value = _uiState.value.copy(
            sortBy = sortBy,
            allowAutoPagination = _uiState.value.allowAutoPagination
        )
    }

    fun onSortOrderToggled() {
        _uiState.value = _uiState.value.copy(
            sortAsc = !_uiState.value.sortAsc,
            allowAutoPagination = _uiState.value.allowAutoPagination
        )
    }

    /**
     * Procesa una lista de ofertas y extrae los números de teléfono
     * Actualiza el mapa de números de teléfono en el estado
     */
    private fun processOffersWithPhoneNumbers(offers: List<P2POffer>): Map<String, String> {
        val phoneMap = mutableMapOf<String, String>()
        offers.forEach { offer ->
            offer.uuid?.let { uuid ->
                extractPhoneFromMessage(offer.message)?.let { phone ->
                    phoneMap[uuid] = phone
                }
            }
        }
        return phoneMap
    }

    /**
     * Extrae número de teléfono del mensaje usando múltiples expresiones regulares
     * Busca números de diferentes países y formatos
     */
    fun extractPhoneFromMessage(message: String?): String? {
        if (message.isNullOrBlank()) return null
        
        // Lista de expresiones regulares para diferentes países
        val phonePatterns = listOf(
            // Cuba: +53XXXXXXXX, 53XXXXXXXX, 5XXXXXXXX (móviles cubanos)
            Regex("""\+?53[\s\-]?[5-9]\d{7}"""),
            // Estados Unidos/Canadá: +1XXXXXXXXXX, 1XXXXXXXXXX
            Regex("""\+?1[\s\-]?\d{10}"""),
            // México: +52XXXXXXXXXX
            Regex("""\+?52[\s\-]?1?\d{10}"""),
            // España: +34XXXXXXXXX
            Regex("""\+?34[\s\-]?\d{9}"""),
            // Argentina: +54XXXXXXXXXX
            Regex("""\+?54[\s\-]?\d{10}"""),
            // Colombia: +57XXXXXXXXXX
            Regex("""\+?57[\s\-]?\d{10}"""),
            // Venezuela: +58XXXXXXXXXX
            Regex("""\+?58[\s\-]?\d{10}"""),
            // Brasil: +55XXXXXXXXXXX
            Regex("""\+?55[\s\-]?\d{11}"""),
            // Chile: +56XXXXXXXXX
            Regex("""\+?56[\s\-]?\d{9}"""),
            // Perú: +51XXXXXXXXX
            Regex("""\+?51[\s\-]?\d{9}"""),
            // Ecuador: +593XXXXXXXXX
            Regex("""\+?593[\s\-]?\d{9}"""),
            // Patrón genérico para números internacionales (8-15 dígitos)
            Regex("""\+\d{1,4}[\s\-]?\d{8,12}"""),
            // Números locales de 8-11 dígitos (sin código de país)
            Regex("""\b\d{8,11}\b""")
        )
        
        // Buscar coincidencias con cada patrón
        for (pattern in phonePatterns) {
            val matches = pattern.findAll(message)
            val match = matches.firstOrNull()
            if (match != null) {
                // Limpiar espacios, guiones y caracteres especiales
                return match.value.replace(Regex("[\\s\\-()]"), "")
            }
        }
        
        return null
    }

}

data class P2PUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isFiltering: Boolean = false,
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
    val sortAsc: Boolean = false,
    // Mapa para almacenar números de teléfono extraídos por UUID de oferta
    val phoneNumbersMap: Map<String, String> = emptyMap(),
    val allowAutoPagination: Boolean = false
)