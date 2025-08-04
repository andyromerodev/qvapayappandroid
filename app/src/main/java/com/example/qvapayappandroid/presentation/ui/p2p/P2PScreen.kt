package com.example.qvapayappandroid.presentation.ui.p2p

import P2POfferCard
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.presentation.ui.p2p.components.P2PPagination
import com.example.qvapayappandroid.presentation.ui.p2p.components.getRatio
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PScreen(
    viewModel: P2PViewModel = koinViewModel(),
    onOfferClick: (P2POffer) -> Unit = {},
    onShowFilters: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
        // .verticalScroll(rememberScrollState()) // QUÍTALO
    ) {
        // TopAppBar
        TopAppBar(
            title = { Text("P2P Transactions") },
            actions = {
                IconButton(onClick = onShowFilters) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros"
                    )
                }
            }
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando datos P2P...")
                    }
                }
            }

            else -> {
                P2PContent(
                    uiState = uiState,
                    modifier = Modifier.weight(1f), // <- Toma el espacio disponible
                    onSendMoney = { viewModel.onSendMoney() },
                    onReceiveMoney = { viewModel.onReceiveMoney() },
                    onViewHistory = { viewModel.onViewHistory() },
                    onOfferTypeChanged = { viewModel.onOfferTypeChanged(it) },
                    onCoinChanged = { viewModel.onCoinChanged(it) },
                    onNextPage = { viewModel.onNextPage() },
                    onPreviousPage = { viewModel.onPreviousPage() },
                    onRefresh = { viewModel.refreshData() },
                    onOfferClick = onOfferClick,
                    onSortByChanged = { viewModel.onSortByChanged(it) },
                    onSortOrderToggled = { viewModel.onSortOrderToggled() },
                )
            }
        }

        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun P2PContent(
    uiState: P2PUiState,
    modifier: Modifier = Modifier,
    onSendMoney: () -> Unit,
    onReceiveMoney: () -> Unit,
    onViewHistory: () -> Unit,
    onOfferTypeChanged: (String) -> Unit,
    onCoinChanged: (String) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onRefresh: () -> Unit,
    onOfferClick: (P2POffer) -> Unit,
    onSortByChanged: (String) -> Unit,
    onSortOrderToggled: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header con contador de ofertas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ofertas P2P",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.totalOffers} ofertas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // CHIPS DE ORDENACIÓN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = uiState.sortBy == "ratio",
                onClick = { onSortByChanged("ratio") },
                label = { Text("Ratio", fontSize = 13.sp) }
            )
            FilterChip(
                selected = uiState.sortBy == "nombre",
                onClick = { onSortByChanged("nombre") },
                label = { Text("Nombre", fontSize = 13.sp) }
            )
            AssistChip(
                onClick = { onSortOrderToggled() },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.sortAsc) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = if (uiState.sortAsc) "Ascendente" else "Descendente",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            if (uiState.sortAsc) "Ascendente" else "Descendente",
                            fontSize = 13.sp
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ordenar lista localmente antes de mostrar
        val sortedOffers = remember(uiState.offers, uiState.sortBy, uiState.sortAsc) {
            when (uiState.sortBy) {
                "ratio" -> {
                    val list = uiState.offers.sortedBy { it.getRatio()?.toDoubleOrNull() ?: 0.0 }
                    if (uiState.sortAsc) list else list.reversed()
                }
                "nombre" -> {
                    val list = uiState.offers.sortedBy { it.owner?.username?.lowercase() ?: "" }
                    if (uiState.sortAsc) list else list.reversed()
                }
                else -> uiState.offers
            }
        }

        // Ofertas o mensaje de vacío
        if (sortedOffers.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay ofertas disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(sortedOffers) { offer ->
                    P2POfferCard(
                        offer = offer,
                        onClick = onOfferClick
                    )
                }
            }
        }

        // Paginación SIEMPRE al fondo
        P2PPagination(
            currentPage = uiState.currentPage,
            totalPages = uiState.totalPages,
            onNextPage = onNextPage,
            onPreviousPage = onPreviousPage
        )

    }
}

