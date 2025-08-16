package com.example.qvapayappandroid.presentation.ui.templates

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.usecase.LoadOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.SaveOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateOfferTemplateUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SaveOfferTemplateViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val saveOfferTemplateUseCase: SaveOfferTemplateUseCase,
    private val updateOfferTemplateUseCase: UpdateOfferTemplateUseCase,
    private val loadOfferTemplateUseCase: LoadOfferTemplateUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SaveOfferTemplateState())
    val uiState: StateFlow<SaveOfferTemplateState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<SaveOfferTemplateEffect>()
    val effect = _effect.asSharedFlow()
    
    private val templateId: Long? = savedStateHandle.get<String>("templateId")?.toLongOrNull()
    
    init {
        if (templateId != null) {
            _uiState.value = _uiState.value.copy(
                isEditing = true,
                templateId = templateId
            )
            loadTemplate(templateId)
        }
    }
    
    fun handleIntent(intent: SaveOfferTemplateIntent) {
        when (intent) {
            is SaveOfferTemplateIntent.LoadTemplate -> {
                templateId?.let { loadTemplate(it) }
            }
            is SaveOfferTemplateIntent.ChangeName -> {
                _uiState.value = _uiState.value.copy(name = intent.name)
            }
            is SaveOfferTemplateIntent.ChangeDescription -> {
                _uiState.value = _uiState.value.copy(description = intent.description)
            }
            is SaveOfferTemplateIntent.ChangeType -> {
                _uiState.value = _uiState.value.copy(type = intent.type)
            }
            is SaveOfferTemplateIntent.SelectCoin -> {
                _uiState.value = _uiState.value.copy(selectedCoin = intent.coin)
            }
            is SaveOfferTemplateIntent.ChangeAmount -> {
                _uiState.value = _uiState.value.copy(amount = intent.amount)
            }
            is SaveOfferTemplateIntent.ChangeReceive -> {
                _uiState.value = _uiState.value.copy(receive = intent.receive)
            }
            is SaveOfferTemplateIntent.ChangeDetail -> {
                val updatedDetails = _uiState.value.details.toMutableList()
                if (intent.index in updatedDetails.indices) {
                    updatedDetails[intent.index] = updatedDetails[intent.index].copy(value = intent.value)
                    _uiState.value = _uiState.value.copy(details = updatedDetails)
                }
            }
            is SaveOfferTemplateIntent.ChangeOnlyKyc -> {
                _uiState.value = _uiState.value.copy(onlyKyc = intent.onlyKyc)
            }
            is SaveOfferTemplateIntent.ChangePrivate -> {
                _uiState.value = _uiState.value.copy(private = intent.private)
            }
            is SaveOfferTemplateIntent.ChangePromoteOffer -> {
                _uiState.value = _uiState.value.copy(promoteOffer = intent.promoteOffer)
            }
            is SaveOfferTemplateIntent.ChangeOnlyVip -> {
                _uiState.value = _uiState.value.copy(onlyVip = intent.onlyVip)
            }
            is SaveOfferTemplateIntent.ChangeMessage -> {
                _uiState.value = _uiState.value.copy(message = intent.message)
            }
            is SaveOfferTemplateIntent.ChangeWebhook -> {
                _uiState.value = _uiState.value.copy(webhook = intent.webhook)
            }
            is SaveOfferTemplateIntent.SaveTemplate -> {
                saveTemplate()
            }
            is SaveOfferTemplateIntent.NavigateBack -> {
                viewModelScope.launch {
                    _effect.emit(SaveOfferTemplateEffect.NavigateBack)
                }
            }
            is SaveOfferTemplateIntent.DismissError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
            is SaveOfferTemplateIntent.DismissSuccessMessage -> {
                _uiState.value = _uiState.value.copy(successMessage = null)
            }
        }
    }
    
    private fun loadTemplate(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = loadOfferTemplateUseCase(id)
                if (result.isSuccess) {
                    val template = result.getOrNull()
                    if (template != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            name = template.name,
                            description = template.description ?: "",
                            type = template.type,
                            selectedCoin = template.toAvailableCoin(),
                            amount = template.amount,
                            receive = template.receive,
                            details = template.details,
                            onlyKyc = template.onlyKyc,
                            private = template.private,
                            promoteOffer = template.promoteOffer,
                            onlyVip = template.onlyVip,
                            message = template.message,
                            webhook = template.webhook
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Plantilla no encontrada"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar la plantilla: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar la plantilla: ${e.message}"
                )
            }
        }
    }
    
    private fun saveTemplate() {
        if (!_uiState.value.isValid) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "El nombre de la plantilla es obligatorio"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val currentState = _uiState.value
                val template = OfferTemplate(
                    id = currentState.templateId ?: 0,
                    name = currentState.name,
                    description = currentState.description.ifEmpty { null },
                    type = currentState.type,
                    coinId = currentState.coinId,
                    coinName = currentState.selectedCoin.name,
                    coinTick = currentState.selectedCoin.tick,
                    amount = currentState.amount,
                    receive = currentState.receive,
                    details = currentState.details,
                    onlyKyc = currentState.onlyKyc,
                    private = currentState.private,
                    promoteOffer = currentState.promoteOffer,
                    onlyVip = currentState.onlyVip,
                    message = currentState.message,
                    webhook = currentState.webhook
                )
                
                val result = if (currentState.isEditing && currentState.templateId != null) {
                    updateOfferTemplateUseCase(template)
                } else {
                    saveOfferTemplateUseCase(template).map { Unit }
                }
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = if (currentState.isEditing) "Plantilla actualizada correctamente" else "Plantilla guardada correctamente"
                    )
                    _effect.emit(SaveOfferTemplateEffect.TemplateSavedSuccessfully)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al guardar la plantilla: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar la plantilla: ${e.message}"
                )
            }
        }
    }
}