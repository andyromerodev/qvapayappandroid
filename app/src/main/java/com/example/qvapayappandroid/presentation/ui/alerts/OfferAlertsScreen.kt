package com.example.qvapayappandroid.presentation.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.qvapayappandroid.presentation.ui.alerts.components.AlertCard
import com.example.qvapayappandroid.presentation.ui.alerts.components.AlertFormDialog
import com.example.qvapayappandroid.presentation.ui.alerts.components.NotificationPermissionCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferAlertsScreen(
    navController: NavController,
    viewModel: OfferAlertsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val notificationPermissionStatus by viewModel.notificationPermissionStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OfferAlertsEffect.ShowSuccessMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is OfferAlertsEffect.ShowErrorMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is OfferAlertsEffect.NavigateToCreateAlert -> {
                    // Handle navigation if needed
                }
                is OfferAlertsEffect.NavigateToEditAlert -> {
                    // Handle navigation if needed
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Alertas P2P",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (!state.isLoading) {
                        IconButton(
                            onClick = { viewModel.handleIntent(OfferAlertsIntent.RefreshAlerts) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.handleIntent(OfferAlertsIntent.ShowCreateAlert) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva alerta"
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Nueva Alerta")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.handleIntent(OfferAlertsIntent.RefreshAlerts) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Notification Permission Card - Always shown if not fully enabled
                if (!notificationPermissionStatus.isFullyEnabled) {
                    NotificationPermissionCard(
                        permissionStatus = notificationPermissionStatus,
                        onPermissionChanged = { viewModel.refreshPermissionStatus() },
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Main content
                when {
                    state.isLoading -> {
                        LoadingState()
                    }
                    state.isEmpty -> {
                        EmptyAlertsState(
                            onCreateClick = { viewModel.handleIntent(OfferAlertsIntent.ShowCreateAlert) },
                            showNotificationCard = !notificationPermissionStatus.isFullyEnabled
                        )
                    }
                    else -> {
                        AlertsList(
                            state = state,
                            onEditAlert = { alert -> 
                                viewModel.handleIntent(OfferAlertsIntent.EditAlert(alert))
                            },
                            onDeleteAlert = { alertId -> 
                                viewModel.handleIntent(OfferAlertsIntent.DeleteAlert(alertId))
                            },
                            onToggleAlert = { alertId, isActive -> 
                                viewModel.handleIntent(OfferAlertsIntent.ToggleAlert(alertId, isActive))
                            },
                            showNotificationCard = !notificationPermissionStatus.isFullyEnabled
                        )
                    }
                }
            }
        }

        // Show error if any
        state.error?.let { error ->
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(error)
                viewModel.handleIntent(OfferAlertsIntent.DismissError)
            }
        }

        // Create/Edit Alert Dialog
        if (state.showCreateDialog || state.editingAlert != null) {
            AlertFormDialog(
                alert = state.editingAlert,
                onDismiss = { viewModel.handleIntent(OfferAlertsIntent.DismissDialog) },
                onSave = { alert ->
                    if (state.editingAlert != null) {
                        viewModel.handleIntent(OfferAlertsIntent.UpdateAlert(alert))
                    } else {
                        viewModel.handleIntent(OfferAlertsIntent.CreateAlert(alert))
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (state.showDeleteConfirmation && state.deletingAlertId != null) {
            AlertDialog(
                onDismissRequest = { viewModel.handleIntent(OfferAlertsIntent.CancelDelete) },
                title = { Text("Eliminar alerta") },
                text = { Text("¿Estás seguro de que quieres eliminar esta alerta? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            viewModel.handleIntent(OfferAlertsIntent.ConfirmDelete(state.deletingAlertId!!))
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.handleIntent(OfferAlertsIntent.CancelDelete) }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando alertas...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyAlertsState(
    onCreateClick: () -> Unit,
    showNotificationCard: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationImportant,
                    contentDescription = "Sin alertas",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No tienes alertas configuradas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crea alertas para recibir notificaciones cuando aparezcan ofertas P2P que coincidan con tus criterios.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onCreateClick
                ) {
                    Text("Crear mi primera alerta")
                }
            }
        }
    }
}

@Composable
private fun AlertsList(
    state: OfferAlertsState,
    onEditAlert: (com.example.qvapayappandroid.domain.model.OfferAlert) -> Unit,
    onDeleteAlert: (Long) -> Unit,
    onToggleAlert: (Long, Boolean) -> Unit,
    showNotificationCard: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Stats header
        if (state.totalAlertsCount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Total",
                        value = state.totalAlertsCount.toString()
                    )
                    StatItem(
                        label = "Activas",
                        value = state.activeAlertsCount.toString()
                    )
                    StatItem(
                        label = "Inactivas",
                        value = (state.totalAlertsCount - state.activeAlertsCount).toString()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Alerts list
        LazyColumn(
            contentPadding = PaddingValues(bottom = 88.dp), // Space for FAB
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.alerts) { alert ->
                AlertCard(
                    alert = alert,
                    onEdit = { onEditAlert(alert) },
                    onDelete = { onDeleteAlert(alert.id) },
                    onToggle = { isActive -> onToggleAlert(alert.id, isActive) }
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}