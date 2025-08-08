
package com.example.qvapayappandroid.presentation.ui.p2p

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.domain.usecase.GetP2POfferByIdUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class P2POfferDetailViewModel(
    private val getP2POfferByIdUseCase: GetP2POfferByIdUseCase,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val offer: P2POffer? = null,
        val errorMessage: String? = null,
        val isApplying: Boolean = false,
        val applicationSuccessMessage: String? = null
    )

    sealed class Effect {
        object NavigateBack : Effect()
        data class ShowError(val message: String) : Effect()
        data class ShowApplicationSuccess(val message: String) : Effect()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    fun loadOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            getP2POfferByIdUseCase(offerId).fold(
                onSuccess = { offer ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        offer = offer,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Error desconocido al cargar la oferta"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                    _effect.emit(Effect.ShowError(errorMessage))
                }
            )
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _effect.emit(Effect.NavigateBack)
        }
    }

    fun onContactUser() {
        // TODO: Implement contact user functionality
    }
}