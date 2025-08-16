package com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.data.model.P2PCreateRequest
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

    private val _uiState = MutableStateFlow(CreateP2POfferState())
    val uiState: StateFlow<CreateP2POfferState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CreateP2POfferEffect>()
    val effect: SharedFlow<CreateP2POfferEffect> = _effect.asSharedFlow()
    
    companion object {
        private var lastCreateOfferRequestTime = 0L
        private const val MIN_CREATE_OFFER_INTERVAL = 10000L // 10 seconds between create requests
    }

    /**
     * Maneja todos los intents de creación de ofertas P2P
     */
    fun handleIntent(intent: CreateP2POfferIntent) {
        when (intent) {
            is CreateP2POfferIntent.ChangeType -> changeType(intent.type)
            is CreateP2POfferIntent.ChangeCoinId -> changeCoinId(intent.coinId)
            is CreateP2POfferIntent.SelectCoin -> selectCoin(intent.coin)
            is CreateP2POfferIntent.ChangeAmount -> changeAmount(intent.amount)
            is CreateP2POfferIntent.ChangeReceive -> changeReceive(intent.receive)
            is CreateP2POfferIntent.ChangeDetail -> changeDetail(intent.index, intent.value)
            is CreateP2POfferIntent.ChangeOnlyKyc -> changeOnlyKyc(intent.onlyKyc)
            is CreateP2POfferIntent.ChangePrivate -> changePrivate(intent.private)
            is CreateP2POfferIntent.ChangePromoteOffer -> changePromoteOffer(intent.promoteOffer)
            is CreateP2POfferIntent.ChangeOnlyVip -> changeOnlyVip(intent.onlyVip)
            is CreateP2POfferIntent.ChangeMessage -> changeMessage(intent.message)
            is CreateP2POfferIntent.ChangeWebhook -> changeWebhook(intent.webhook)
            is CreateP2POfferIntent.CreateOffer -> createOffer()
            is CreateP2POfferIntent.NavigateBack -> navigateBack()
            is CreateP2POfferIntent.DismissError -> dismissError()
            is CreateP2POfferIntent.DismissSuccessMessage -> dismissSuccessMessage()
            is CreateP2POfferIntent.LoadFromTemplate -> loadFromTemplate(intent.template)
            is CreateP2POfferIntent.RequestCurrentStateForTemplate -> requestCurrentStateForTemplate()
        }
    }

    private fun changeType(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    private fun changeCoinId(coinId: String) {
        _uiState.value = _uiState.value.copy(coinId = coinId)
    }
    
    private fun selectCoin(coin: AvailableCoin) {
        _uiState.value = _uiState.value.copy(
            selectedCoin = coin,
            coinId = coin.id.toString()
        )
    }

    private fun changeAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    private fun changeReceive(receive: String) {
        _uiState.value = _uiState.value.copy(receive = receive)
    }

    private fun changeDetail(index: Int, value: String) {
        val currentDetails = _uiState.value.details.toMutableList()
        if (index in currentDetails.indices) {
            currentDetails[index] = currentDetails[index].copy(value = value)
            _uiState.value = _uiState.value.copy(details = currentDetails)
        }
    }

    private fun changeOnlyKyc(onlyKyc: Boolean) {
        _uiState.value = _uiState.value.copy(onlyKyc = onlyKyc)
    }

    private fun changePrivate(private: Boolean) {
        _uiState.value = _uiState.value.copy(private = private)
    }

    private fun changePromoteOffer(promoteOffer: Boolean) {
        _uiState.value = _uiState.value.copy(promoteOffer = promoteOffer)
    }

    private fun changeOnlyVip(onlyVip: Boolean) {
        _uiState.value = _uiState.value.copy(onlyVip = onlyVip)
    }

    private fun changeMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    private fun changeWebhook(webhook: String) {
        _uiState.value = _uiState.value.copy(webhook = webhook)
    }

    private fun navigateBack() {
        emitEffect(CreateP2POfferEffect.NavigateBack)
    }

    private fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun createOffer() {
        val currentState = _uiState.value
        
        // Verificar throttling
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRequest = currentTime - lastCreateOfferRequestTime
        
        if (timeSinceLastRequest < MIN_CREATE_OFFER_INTERVAL) {
            val remainingTime = MIN_CREATE_OFFER_INTERVAL - timeSinceLastRequest
            val remainingSeconds = (remainingTime / 1000).toInt() + 1
            
            val errorMessage = "Debes esperar $remainingSeconds segundos antes de crear otra oferta"
            _uiState.value = _uiState.value.copy(
                errorMessage = errorMessage,
                isThrottled = true,
                throttleRemainingSeconds = remainingSeconds
            )
            emitEffect(CreateP2POfferEffect.ShowError(errorMessage))
            
            // Iniciar countdown
            startThrottleCountdown(remainingSeconds)
            return
        }
        
        // Validaciones básicas
        if (currentState.amount.isBlank()) {
            val errorMessage = "El monto es requerido"
            _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
            emitEffect(CreateP2POfferEffect.ValidationError("amount", errorMessage))
            emitEffect(CreateP2POfferEffect.ShowError(errorMessage))
            return
        }
        
        if (currentState.receive.isBlank()) {
            val errorMessage = "El monto a recibir es requerido"
            _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
            emitEffect(CreateP2POfferEffect.ValidationError("receive", errorMessage))
            emitEffect(CreateP2POfferEffect.ShowError(errorMessage))
            return
        }
        
        if (currentState.message.isBlank()) {
            val errorMessage = "El mensaje es requerido"
            _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
            emitEffect(CreateP2POfferEffect.ValidationError("message", errorMessage))
            emitEffect(CreateP2POfferEffect.ShowError(errorMessage))
            return
        }
        
        // Validar que los detalles no estén vacíos
        val emptyDetail = currentState.details.find { it.value.isBlank() }
        if (emptyDetail != null) {
            val errorMessage = "Todos los detalles son requeridos: ${emptyDetail.name}"
            _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
            emitEffect(CreateP2POfferEffect.ValidationError("details", errorMessage))
            emitEffect(CreateP2POfferEffect.ShowError(errorMessage))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null,
                isThrottled = false,
                throttleRemainingSeconds = 0
            )
            emitEffect(CreateP2POfferEffect.ShowLoading)
            
            // Actualizar el timestamp de la última petición
            lastCreateOfferRequestTime = System.currentTimeMillis()
            
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
                        emitEffect(CreateP2POfferEffect.HideLoading)
                        emitEffect(CreateP2POfferEffect.OfferCreatedSuccessfully(response.p2p.uuid, response.msg))
                    },
                    onFailure = { error ->
                        val errorMessage = error.message ?: "Error desconocido al crear la oferta"
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = errorMessage
                        )
                        emitEffect(CreateP2POfferEffect.HideLoading)
                        emitEffect(CreateP2POfferEffect.ShowError(errorMessage))
                    }
                )
            } catch (e: NumberFormatException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                emitEffect(CreateP2POfferEffect.HideLoading)
                emitEffect(CreateP2POfferEffect.ShowError("Formato de número inválido"))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                emitEffect(CreateP2POfferEffect.HideLoading)
                emitEffect(CreateP2POfferEffect.ShowError("Error inesperado: ${e.message}"))
            }
        }
    }

    /**
     * Carga datos desde una plantilla
     */
    private fun loadFromTemplate(template: com.example.qvapayappandroid.domain.model.OfferTemplate) {
        val availableCoin = template.toAvailableCoin()
        
        _uiState.value = _uiState.value.copy(
            type = template.type,
            selectedCoin = availableCoin,
            coinId = template.coinId,
            amount = template.amount,
            receive = template.receive,
            details = template.details,
            onlyKyc = template.onlyKyc,
            private = template.private,
            promoteOffer = template.promoteOffer,
            onlyVip = template.onlyVip,
            message = template.message,
            webhook = template.webhook,
            successMessage = "Plantilla '${template.name}' cargada exitosamente"
        )
    }
    
    /**
     * Proporciona el estado actual para crear una plantilla
     */
    private fun requestCurrentStateForTemplate() {
        emitEffect(CreateP2POfferEffect.CurrentStateForTemplate(_uiState.value))
    }
    
    /**
     * Emite un efecto
     */
    private fun emitEffect(effect: CreateP2POfferEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
    
    /**
     * Inicia un countdown para mostrar el tiempo restante del throttling
     */
    private fun startThrottleCountdown(initialSeconds: Int) {
        viewModelScope.launch {
            var remainingSeconds = initialSeconds
            
            while (remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000L) // Esperar 1 segundo
                remainingSeconds--
                
                _uiState.value = _uiState.value.copy(
                    throttleRemainingSeconds = remainingSeconds,
                    errorMessage = if (remainingSeconds > 0) {
                        "Debes esperar $remainingSeconds segundos antes de crear otra oferta"
                    } else null,
                    isThrottled = remainingSeconds > 0
                )
            }
        }
    }
}