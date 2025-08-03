package com.example.qvapayappandroid.presentation.ui.p2p

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.P2PFilterRequest
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.usecase.GetP2POffersUseCase
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
    
    private val _effect = MutableSharedFlow<P2PEffect>()
    val effect: SharedFlow<P2PEffect> = _effect.asSharedFlow()
    
    init {
        loadP2PData()
    }
    
    private fun loadP2PData() {
        viewModelScope.launch {
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
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            offers = response.data,
                            currentPage = response.currentPage,
                            totalPages = response.lastPage,
                            totalOffers = response.total
                        )
                        Log.d("P2PViewModel", "P2P offers loaded: ${response.data.size} offers, page ${response.currentPage}/${response.lastPage}")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error loading P2P offers: ${error.message}",
                            offers = emptyList()
                        )
                        Log.e("P2PViewModel", "Error loading P2P offers", error)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}",
                    offers = emptyList()
                )
                Log.e("P2PViewModel", "Unexpected error loading P2P data", e)
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
    
    fun onOfferTypeChanged(offerType: String) {
        _uiState.value = _uiState.value.copy(
            selectedOfferType = offerType,
            currentPage = 1
        )
        loadP2PData()
    }
    
    fun onCoinChanged(coin: String) {
        _uiState.value = _uiState.value.copy(
            selectedCoin = coin,
            currentPage = 1
        )
        loadP2PData()
    }
    
    fun onPageChanged(page: Int) {
        if (page in 1.._uiState.value.totalPages) {
            _uiState.value = _uiState.value.copy(currentPage = page)
            loadP2PData()
        }
    }
    
    fun onNextPage() {
        val currentPage = _uiState.value.currentPage
        val totalPages = _uiState.value.totalPages
        if (currentPage < totalPages) {
            onPageChanged(currentPage + 1)
        }
    }
    
    fun onPreviousPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage > 1) {
            onPageChanged(currentPage - 1)
        }
    }
}

data class P2PUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val offers: List<P2POffer> = emptyList(),
    val selectedOfferType: String = "all", // "all", "buy", "sell"
    val selectedCoin: String = "all", // "all" or specific coin
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalOffers: Int = 0,
    val availableCoins: List<String> = listOf(
        "SOL", "SBERBANK", "BANK_CUP", "ZELLE", "TROPIPAY", "ETECSA", 
        "USDCASH", "CLASICA", "BANK_MLC", "NEOMOON", "USDT", "BANK_EUR", 
        "QVAPAY", "BANDECPREPAGO", "CUPCASH", "WISE", "EURCASH", "USDTBSC", "BOLSATM"
    )
)

sealed class P2PEffect {
    object NavigateToSendMoney : P2PEffect()
    object NavigateToReceiveMoney : P2PEffect()
    object NavigateToHistory : P2PEffect()
}