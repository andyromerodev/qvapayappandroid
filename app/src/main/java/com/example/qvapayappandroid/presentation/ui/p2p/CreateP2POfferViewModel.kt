package com.example.qvapayappandroid.presentation.ui.p2p

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import com.example.qvapayappandroid.data.model.P2PDetail
import com.example.qvapayappandroid.domain.usecase.CreateP2POfferUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CreateP2POfferViewModel(
    private val createP2POfferUseCase: CreateP2POfferUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CreateP2PEffect>()
    val effect: SharedFlow<CreateP2PEffect> = _effect.asSharedFlow()

    fun onTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun onCoinIdChanged(coinId: String) {
        _uiState.value = _uiState.value.copy(coinId = coinId)
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onReceiveChanged(receive: String) {
        _uiState.value = _uiState.value.copy(receive = receive)
    }

    fun onDetailChanged(index: Int, value: String) {
        val currentDetails = _uiState.value.details.toMutableList()
        if (index in currentDetails.indices) {
            currentDetails[index] = currentDetails[index].copy(value = value)
            _uiState.value = _uiState.value.copy(details = currentDetails)
        }
    }

    fun onOnlyKycChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(onlyKyc = value)
    }

    fun onPrivateChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(private = value)
    }

    fun onPromoteOfferChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(promoteOffer = value)
    }

    fun onOnlyVipChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(onlyVip = value)
    }

    fun onMessageChanged(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    fun onWebhookChanged(webhook: String) {
        _uiState.value = _uiState.value.copy(webhook = webhook)
    }

    fun onBackClick() {
        viewModelScope.launch {
            _effect.emit(CreateP2PEffect.NavigateBack)
        }
    }

    fun createOffer() {
        val currentState = _uiState.value
        
        // Validaciones básicas
        if (currentState.amount.isBlank()) {
            viewModelScope.launch {
                _effect.emit(CreateP2PEffect.ShowError("El monto es requerido"))
            }
            return
        }
        
        if (currentState.receive.isBlank()) {
            viewModelScope.launch {
                _effect.emit(CreateP2PEffect.ShowError("El monto a recibir es requerido"))
            }
            return
        }
        
        if (currentState.message.isBlank()) {
            viewModelScope.launch {
                _effect.emit(CreateP2PEffect.ShowError("El mensaje es requerido"))
            }
            return
        }
        
        // Validar que los detalles no estén vacíos
        val emptyDetail = currentState.details.find { it.value.isBlank() }
        if (emptyDetail != null) {
            viewModelScope.launch {
                _effect.emit(CreateP2PEffect.ShowError("Todos los detalles son requeridos: ${emptyDetail.name}"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Convertir los detalles a JSON string
                val detailsJson = Json.encodeToString(currentState.details)
                
                val request = P2PCreateRequest(
                    type = currentState.type,
                    coin = currentState.coinId.toInt(),
                    amount = currentState.amount.toDouble(),
                    receive = currentState.receive.toDouble(),
                    details = detailsJson,
                    onlyKyc = if (currentState.onlyKyc) 1 else 0,
                    private = if (currentState.private) 1 else 0,
                    promoteOffer = if (currentState.promoteOffer) 1 else 0,
                    onlyVip = if (currentState.onlyVip) 1 else 0,
                    message = currentState.message,
                    webhook = currentState.webhook.ifBlank { null }
                )
                
                createP2POfferUseCase(request).fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = response.msg
                        )
                        _effect.emit(CreateP2PEffect.ShowSuccess("${response.msg} - UUID: ${response.p2p.uuid}"))
                    },
                    onFailure = { error ->
                        val errorMessage = error.message ?: "Error desconocido al crear la oferta"
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = errorMessage
                        )
                        _effect.emit(CreateP2PEffect.ShowError(errorMessage))
                    }
                )
            } catch (e: NumberFormatException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.emit(CreateP2PEffect.ShowError("Formato de número inválido"))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.emit(CreateP2PEffect.ShowError("Error inesperado: ${e.message}"))
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class UiState(
    val isLoading: Boolean = false,
    val type: String = "sell",
    val coinId: String = "108",
    val amount: String = "",
    val receive: String = "",
    val details: List<P2PDetail> = listOf(
        P2PDetail("Nombre y Apellidos", ""),
        P2PDetail("Nro de tarjeta", ""),
        P2PDetail("Nro de celular", "")
    ),
    val onlyKyc: Boolean = true,
    val private: Boolean = false,
    val promoteOffer: Boolean = false,
    val onlyVip: Boolean = true,
    val message: String = "",
    val webhook: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed class CreateP2PEffect {
    object NavigateBack : CreateP2PEffect()
    data class ShowError(val message: String) : CreateP2PEffect()
    data class ShowSuccess(val message: String) : CreateP2PEffect()
}