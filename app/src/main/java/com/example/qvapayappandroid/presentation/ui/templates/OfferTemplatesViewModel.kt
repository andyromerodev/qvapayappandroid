package com.example.qvapayappandroid.presentation.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.domain.usecase.DeleteOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.GetOfferTemplatesUseCase
import com.example.qvapayappandroid.domain.usecase.CreateP2POfferUseCase
import com.example.qvapayappandroid.domain.usecase.LoadOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.SaveOfferTemplateUseCase
import com.example.qvapayappandroid.data.model.P2PCreateRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OfferTemplatesViewModel(
    private val getOfferTemplatesUseCase: GetOfferTemplatesUseCase,
    private val deleteOfferTemplateUseCase: DeleteOfferTemplateUseCase,
    private val createP2POfferUseCase: CreateP2POfferUseCase,
    private val loadOfferTemplateUseCase: LoadOfferTemplateUseCase,
    private val saveOfferTemplateUseCase: SaveOfferTemplateUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OfferTemplatesState())
    val uiState: StateFlow<OfferTemplatesState> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<OfferTemplatesEffect>()
    val effect = _effect.asSharedFlow()
    
    private var searchJob: Job? = null
    
    init {
        loadTemplates()
    }
    
    fun handleIntent(intent: OfferTemplatesIntent) {
        when (intent) {
            is OfferTemplatesIntent.LoadTemplates -> loadTemplates()
            is OfferTemplatesIntent.RefreshTemplates -> refreshTemplates()
            is OfferTemplatesIntent.SearchTemplates -> searchTemplates(intent.query)
            is OfferTemplatesIntent.FilterByType -> filterByType(intent.type)
            is OfferTemplatesIntent.DeleteTemplate -> deleteTemplate(intent.template)
            is OfferTemplatesIntent.EditTemplate -> editTemplate(intent.template)
            is OfferTemplatesIntent.UseTemplate -> useTemplate(intent.template)
            is OfferTemplatesIntent.DuplicateTemplate -> duplicateTemplate(intent.template)
            is OfferTemplatesIntent.CreateNewTemplate -> createNewTemplate()
            is OfferTemplatesIntent.ClearSearch -> clearSearch()
            is OfferTemplatesIntent.DismissError -> dismissError()
        }
    }
    
    private fun loadTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                getOfferTemplatesUseCase()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar plantillas: ${exception.message}"
                        )
                    }
                    .collect { templates ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            templates = templates,
                            isEmpty = templates.isEmpty()
                        )
                        applyCurrentFilters()
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar plantillas: ${e.message}"
                )
            }
        }
    }
    
    private fun refreshTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            
            try {
                getOfferTemplatesUseCase()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            errorMessage = "Error al actualizar plantillas: ${exception.message}"
                        )
                    }
                    .collect { templates ->
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            templates = templates,
                            isEmpty = templates.isEmpty()
                        )
                        applyCurrentFilters()
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    errorMessage = "Error al actualizar plantillas: ${e.message}"
                )
            }
        }
    }
    
    private fun searchTemplates(query: String) {
        searchJob?.cancel()
        
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotEmpty()
        )
        
        if (query.isEmpty()) {
            applyCurrentFilters()
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            
            try {
                getOfferTemplatesUseCase.search(query)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al buscar plantillas: ${exception.message}"
                        )
                    }
                    .collect { searchResults ->
                        val filtered = if (_uiState.value.selectedType != null) {
                            searchResults.filter { it.type == _uiState.value.selectedType }
                        } else {
                            searchResults
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            filteredTemplates = filtered
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al buscar plantillas: ${e.message}"
                )
            }
        }
    }
    
    private fun filterByType(type: String?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        applyCurrentFilters()
    }
    
    private fun applyCurrentFilters() {
        val currentState = _uiState.value
        val baseTemplates = currentState.templates
        
        val filtered = when {
            currentState.searchQuery.isNotEmpty() -> {
                // Si hay búsqueda, usar los resultados de búsqueda y aplicar filtro de tipo
                val searchResults = baseTemplates.filter { template ->
                    template.name.contains(currentState.searchQuery, ignoreCase = true) ||
                    template.description?.contains(currentState.searchQuery, ignoreCase = true) == true
                }
                
                if (currentState.selectedType != null) {
                    searchResults.filter { it.type == currentState.selectedType }
                } else {
                    searchResults
                }
            }
            currentState.selectedType != null -> {
                // Solo filtro de tipo
                baseTemplates.filter { it.type == currentState.selectedType }
            }
            else -> {
                // Sin filtros
                baseTemplates
            }
        }
        
        _uiState.value = _uiState.value.copy(filteredTemplates = filtered)
    }
    
    private fun deleteTemplate(template: OfferTemplate) {
        viewModelScope.launch {
            try {
                val result = deleteOfferTemplateUseCase(template)
                if (result.isSuccess) {
                    _effect.emit(OfferTemplatesEffect.ShowSuccessMessage("Plantilla eliminada correctamente"))
                } else {
                    _effect.emit(OfferTemplatesEffect.ShowErrorMessage("Error al eliminar plantilla: ${result.exceptionOrNull()?.message}"))
                }
            } catch (e: Exception) {
                _effect.emit(OfferTemplatesEffect.ShowErrorMessage("Error al eliminar plantilla: ${e.message}"))
            }
        }
    }
    
    private fun editTemplate(template: OfferTemplate) {
        viewModelScope.launch {
            _effect.emit(OfferTemplatesEffect.NavigateToEditTemplate(template.id))
        }
    }
    
    private fun useTemplate(template: OfferTemplate) {
        viewModelScope.launch {
            // Marcar que estamos creando una oferta desde esta plantilla
            _uiState.value = _uiState.value.copy(creatingOfferFromTemplateId = template.id)
            
            try {
                // Crear request P2P usando los datos de la plantilla
                val request = P2PCreateRequest(
                    type = template.type,
                    coin = template.coinId.toIntOrNull() ?: 0,
                    amount = template.amount.toDoubleOrNull() ?: 0.0,
                    receive = template.receive.toDoubleOrNull() ?: 0.0,
                    details = Json.encodeToString(template.details),
                    onlyKyc = if (template.onlyKyc) 1 else 0,
                    private = if (template.private) 1 else 0,
                    promoteOffer = if (template.promoteOffer) 1 else 0,
                    onlyVip = if (template.onlyVip) 1 else 0,
                    message = template.message,
                    webhook = template.webhook.ifEmpty { null }
                )
                
                // Crear la oferta P2P
                val result = createP2POfferUseCase(request)
                
                // Limpiar estado de creación
                _uiState.value = _uiState.value.copy(creatingOfferFromTemplateId = null)
                
                if (result.isSuccess) {
                    _effect.emit(OfferTemplatesEffect.ShowSuccessMessage("Oferta P2P creada exitosamente"))
                } else {
                    _effect.emit(OfferTemplatesEffect.ShowErrorMessage("Error al crear oferta P2P: ${result.exceptionOrNull()?.message}"))
                }
                
            } catch (e: Exception) {
                // Limpiar estado de creación en caso de error
                _uiState.value = _uiState.value.copy(creatingOfferFromTemplateId = null)
                _effect.emit(OfferTemplatesEffect.ShowErrorMessage("Error al crear oferta P2P: ${e.message}"))
            }
        }
    }
    
    private fun duplicateTemplate(template: OfferTemplate) {
        viewModelScope.launch {
            try {
                val result = loadOfferTemplateUseCase(template.id)
                if (result.isSuccess) {
                    val originalTemplate = result.getOrNull()
                    if (originalTemplate != null) {
                        val duplicatedTemplate = originalTemplate.copy(
                            id = 0, // Nuevo ID será asignado por la base de datos
                            name = "Copy - ${originalTemplate.name}",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        
                        val saveResult = saveOfferTemplateUseCase(duplicatedTemplate)
                        if (saveResult.isSuccess) {
                            _effect.emit(OfferTemplatesEffect.ShowSuccessMessage("Plantilla duplicada exitosamente"))
                        } else {
                            _effect.emit(OfferTemplatesEffect.ShowErrorMessage("Error al duplicar plantilla: ${saveResult.exceptionOrNull()?.message}"))
                        }
                    } else {
                        _effect.emit(OfferTemplatesEffect.ShowErrorMessage("No se pudo cargar la plantilla a duplicar"))
                    }
                } else {
                    _effect.emit(OfferTemplatesEffect.ShowErrorMessage("Error al cargar plantilla: ${result.exceptionOrNull()?.message}"))
                }
            } catch (e: Exception) {
                _effect.emit(OfferTemplatesEffect.ShowErrorMessage("Error al duplicar plantilla: ${e.message}"))
            }
        }
    }
    
    private fun createNewTemplate() {
        viewModelScope.launch {
            _effect.emit(OfferTemplatesEffect.NavigateToCreateTemplate)
        }
    }
    
    private fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearching = false
        )
        applyCurrentFilters()
    }
    
    private fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}