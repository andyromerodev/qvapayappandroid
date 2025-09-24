package com.example.qvapayappandroid.presentation.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qvapayappandroid.domain.model.OfferTemplate
import com.example.qvapayappandroid.presentation.ui.templates.components.TemplateCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferTemplatesScreen(
    onNavigateToEditTemplate: (Long) -> Unit = {},
    onNavigateToCreateTemplate: () -> Unit = {},
    onNavigateToCreateOffer: (com.example.qvapayappandroid.domain.model.OfferTemplate) -> Unit = {},
    viewModel: OfferTemplatesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<com.example.qvapayappandroid.domain.model.OfferTemplate?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle view effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OfferTemplatesEffect.NavigateToEditTemplate -> {
                    onNavigateToEditTemplate(effect.templateId)
                }
                is OfferTemplatesEffect.NavigateToCreateTemplate -> {
                    onNavigateToCreateTemplate()
                }
                is OfferTemplatesEffect.NavigateToCreateOffer -> {
                    onNavigateToCreateOffer(effect.template)
                }
                is OfferTemplatesEffect.ShowDeleteConfirmation -> {
                    showDeleteDialog = effect.template
                }
                is OfferTemplatesEffect.ShowSuccessMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is OfferTemplatesEffect.ShowErrorMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plantillas de Ofertas") },
                actions = {
                    IconButton(onClick = { viewModel.handleIntent(OfferTemplatesIntent.CreateNewTemplate) }) {
                        Icon(Icons.Default.Add, contentDescription = "Crear plantilla")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar and filters
            SearchAndFilters(
                searchQuery = uiState.searchQuery,
                selectedType = uiState.selectedType,
                typeFilters = uiState.typeFilters,
                onSearchQueryChange = { viewModel.handleIntent(OfferTemplatesIntent.SearchTemplates(it)) },
                onTypeFilterChange = { viewModel.handleIntent(OfferTemplatesIntent.FilterByType(it)) },
                onClearSearch = { viewModel.handleIntent(OfferTemplatesIntent.ClearSearch) }
            )
            
            // Main content area
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.errorMessage != null -> {
                        ErrorMessage(
                            message = uiState.errorMessage!!,
                            onDismiss = { viewModel.handleIntent(OfferTemplatesIntent.DismissError) },
                            onRetry = { viewModel.handleIntent(OfferTemplatesIntent.LoadTemplates) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.showEmptyState -> {
                        EmptyStateMessage(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        TemplatesList(
                            templates = uiState.displayTemplates,
                            isRefreshing = uiState.isRefreshing,
                            creatingOfferFromTemplateId = uiState.creatingOfferFromTemplateId,
                            onRefresh = { viewModel.handleIntent(OfferTemplatesIntent.RefreshTemplates) },
                            onEditTemplate = { viewModel.handleIntent(OfferTemplatesIntent.EditTemplate(it)) },
                            onDeleteTemplate = { showDeleteDialog = it },
                            onUseTemplate = { viewModel.handleIntent(OfferTemplatesIntent.UseTemplate(it)) },
                            onDuplicateTemplate = { viewModel.handleIntent(OfferTemplatesIntent.DuplicateTemplate(it)) }
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { template ->
        DeleteConfirmationDialog(
            templateName = template.name,
            onConfirm = {
                viewModel.handleIntent(OfferTemplatesIntent.DeleteTemplate(template))
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAndFilters(
    searchQuery: String,
    selectedType: String?,
    typeFilters: List<TypeFilter>,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (String?) -> Unit,
    onClearSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Buscar plantillas") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Type filters
            Text(
                text = "Tipo de oferta",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                typeFilters.forEach { filter ->
                    FilterChip(
                        onClick = { onTypeFilterChange(filter.value) },
                        label = { Text(filter.name) },
                        selected = selectedType == filter.value,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatesList(
    templates: List<OfferTemplate>,
    isRefreshing: Boolean,
    creatingOfferFromTemplateId: Long?,
    onRefresh: () -> Unit,
    onEditTemplate: (OfferTemplate) -> Unit,
    onDeleteTemplate: (OfferTemplate) -> Unit,
    onUseTemplate: (OfferTemplate) -> Unit,
    onDuplicateTemplate: (OfferTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(templates) { template ->
            TemplateCard(
                template = template,
                onEdit = { onEditTemplate(template) },
                onDelete = { onDeleteTemplate(template) },
                onUse = { onUseTemplate(template) },
                onDuplicate = { onDuplicateTemplate(template) },
                isCreatingOffer = creatingOfferFromTemplateId == template.id
            )
        }
    }
}

@Composable
private fun EmptyStateMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No tienes plantillas guardadas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crea tu primera plantilla para agilizar la creación de ofertas",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onRetry) {
                    Text("Reintentar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    templateName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar plantilla") },
        text = { Text("¿Estás seguro de que quieres eliminar la plantilla \"$templateName\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
