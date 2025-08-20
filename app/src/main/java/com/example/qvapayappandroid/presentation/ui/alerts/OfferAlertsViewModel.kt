package com.example.qvapayappandroid.presentation.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.usecase.DeleteOfferAlertUseCase
import com.example.qvapayappandroid.domain.usecase.GetNotificationPermissionStatusUseCase
import com.example.qvapayappandroid.domain.usecase.GetOfferAlertsUseCase
import com.example.qvapayappandroid.domain.usecase.ManageAlertWorkManagerUseCase
import com.example.qvapayappandroid.domain.usecase.SaveOfferAlertUseCase
import com.example.qvapayappandroid.domain.usecase.ToggleOfferAlertUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateOfferAlertUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OfferAlertsViewModel(
    private val getOfferAlertsUseCase: GetOfferAlertsUseCase,
    private val saveOfferAlertUseCase: SaveOfferAlertUseCase,
    private val updateOfferAlertUseCase: UpdateOfferAlertUseCase,
    private val deleteOfferAlertUseCase: DeleteOfferAlertUseCase,
    private val toggleOfferAlertUseCase: ToggleOfferAlertUseCase,
    private val manageAlertWorkManagerUseCase: ManageAlertWorkManagerUseCase,
    private val getNotificationPermissionStatusUseCase: GetNotificationPermissionStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OfferAlertsState())
    val state: StateFlow<OfferAlertsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<OfferAlertsEffect>()
    val effects: SharedFlow<OfferAlertsEffect> = _effects.asSharedFlow()

    // Flow reactivo para permisos de notificaciones
    val notificationPermissionStatus = getNotificationPermissionStatusUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.qvapayappandroid.data.permissions.NotificationPermissionManager.NotificationPermissionStatus(
                isGranted = false,
                isChannelEnabled = false,
                canShowNotifications = false
            )
        )

    init {
        handleIntent(OfferAlertsIntent.LoadAlerts)
    }

    fun refreshPermissionStatus() {
        getNotificationPermissionStatusUseCase.refreshStatus()
    }

    fun handleIntent(intent: OfferAlertsIntent) {
        when (intent) {
            is OfferAlertsIntent.LoadAlerts -> loadAlerts()
            is OfferAlertsIntent.RefreshAlerts -> refreshAlerts()
            is OfferAlertsIntent.ShowCreateAlert -> showCreateDialog()
            is OfferAlertsIntent.EditAlert -> editAlert(intent.alert)
            is OfferAlertsIntent.DeleteAlert -> requestDeleteAlert(intent.alertId)
            is OfferAlertsIntent.ToggleAlert -> toggleAlert(intent.alertId, intent.isActive)
            is OfferAlertsIntent.CreateAlert -> createAlert(intent.alert)
            is OfferAlertsIntent.UpdateAlert -> updateAlert(intent.alert)
            is OfferAlertsIntent.DismissDialog -> dismissDialog()
            is OfferAlertsIntent.DismissError -> dismissError()
            is OfferAlertsIntent.ConfirmDelete -> confirmDeleteAlert(intent.alertId)
            is OfferAlertsIntent.CancelDelete -> cancelDelete()
        }
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            getOfferAlertsUseCase()
                .catch { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Error al cargar las alertas: ${error.message}"
                        ) 
                    }
                }
                .collectLatest { alerts ->
                    _state.update { 
                        it.copy(
                            alerts = alerts, 
                            isLoading = false, 
                            error = null
                        ) 
                    }
                }
        }
    }

    private fun refreshAlerts() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            
            getOfferAlertsUseCase()
                .catch { error ->
                    _state.update { 
                        it.copy(
                            isRefreshing = false, 
                            error = "Error al actualizar las alertas: ${error.message}"
                        ) 
                    }
                }
                .collectLatest { alerts ->
                    _state.update { 
                        it.copy(
                            alerts = alerts, 
                            isRefreshing = false, 
                            error = null
                        ) 
                    }
                }
        }
    }

    private fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true) }
    }

    private fun editAlert(alert: com.example.qvapayappandroid.domain.model.OfferAlert) {
        _state.update { it.copy(editingAlert = alert) }
    }

    private fun requestDeleteAlert(alertId: Long) {
        _state.update { 
            it.copy(
                deletingAlertId = alertId, 
                showDeleteConfirmation = true
            ) 
        }
    }

    private fun confirmDeleteAlert(alertId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, showDeleteConfirmation = false) }
            
            deleteOfferAlertUseCase(alertId)
                .onSuccess {
                    _state.update { 
                        it.copy(
                            isProcessing = false,
                            deletingAlertId = null
                        ) 
                    }
                    _effects.emit(OfferAlertsEffect.ShowSuccessMessage("Alerta eliminada correctamente"))
                    // Gestionar WorkManager después de eliminar una alerta
                    manageAlertWorkManagerUseCase()
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isProcessing = false,
                            deletingAlertId = null,
                            error = "Error al eliminar la alerta: ${error.message}"
                        ) 
                    }
                }
        }
    }

    private fun cancelDelete() {
        _state.update { 
            it.copy(
                deletingAlertId = null, 
                showDeleteConfirmation = false
            ) 
        }
    }

    private fun toggleAlert(alertId: Long, isActive: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            
            toggleOfferAlertUseCase(alertId, isActive)
                .onSuccess {
                    _state.update { it.copy(isProcessing = false) }
                    val message = if (isActive) "Alerta activada" else "Alerta desactivada"
                    _effects.emit(OfferAlertsEffect.ShowSuccessMessage(message))
                    // Gestionar WorkManager después de cambiar el estado de una alerta
                    manageAlertWorkManagerUseCase()
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isProcessing = false,
                            error = "Error al cambiar el estado de la alerta: ${error.message}"
                        ) 
                    }
                }
        }
    }

    private fun createAlert(alert: com.example.qvapayappandroid.domain.model.OfferAlert) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, showCreateDialog = false) }
            
            saveOfferAlertUseCase(alert)
                .onSuccess {
                    _state.update { it.copy(isProcessing = false) }
                    _effects.emit(OfferAlertsEffect.ShowSuccessMessage("Alerta creada correctamente"))
                    // Gestionar WorkManager después de crear una alerta
                    manageAlertWorkManagerUseCase()
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isProcessing = false,
                            error = "Error al crear la alerta: ${error.message}"
                        ) 
                    }
                }
        }
    }

    private fun updateAlert(alert: com.example.qvapayappandroid.domain.model.OfferAlert) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, editingAlert = null) }
            
            updateOfferAlertUseCase(alert)
                .onSuccess {
                    _state.update { it.copy(isProcessing = false) }
                    _effects.emit(OfferAlertsEffect.ShowSuccessMessage("Alerta actualizada correctamente"))
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isProcessing = false,
                            error = "Error al actualizar la alerta: ${error.message}"
                        ) 
                    }
                }
        }
    }

    private fun dismissDialog() {
        _state.update { 
            it.copy(
                showCreateDialog = false,
                editingAlert = null
            ) 
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}