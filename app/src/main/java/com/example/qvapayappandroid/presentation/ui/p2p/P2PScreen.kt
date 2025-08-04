package com.example.qvapayappandroid.presentation.ui.p2p

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.presentation.ui.p2p.components.P2PActionButton
import com.example.qvapayappandroid.presentation.ui.p2p.components.P2PFiltersCard
import com.example.qvapayappandroid.presentation.ui.p2p.components.P2POfferCard
import com.example.qvapayappandroid.presentation.ui.p2p.components.P2PStatsCard
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
            .verticalScroll(rememberScrollState())
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
                    modifier = Modifier.padding(16.dp),
                    onSendMoney = { viewModel.onSendMoney() },
                    onReceiveMoney = { viewModel.onReceiveMoney() },
                    onViewHistory = { viewModel.onViewHistory() },
                    onOfferTypeChanged = { viewModel.onOfferTypeChanged(it) },
                    onCoinChanged = { viewModel.onCoinChanged(it) },
                    onNextPage = { viewModel.onNextPage() },
                    onPreviousPage = { viewModel.onPreviousPage() },
                    onRefresh = { viewModel.refreshData() },
                    onOfferClick = onOfferClick
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
    onOfferClick: (P2POffer) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.offers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
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
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.offers) { offer ->
                    P2POfferCard(
                        offer = offer,
                        onClick = onOfferClick
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Pagination - Siempre visible
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousPage,
                enabled = uiState.currentPage > 1 && uiState.totalPages > 1
            ) {
                Icon(Icons.Default.NavigateBefore, contentDescription = "Anterior")
            }
            
            Text(
                text = if (uiState.totalPages > 0) "Página ${uiState.currentPage} de ${uiState.totalPages}" else "Sin páginas",
                style = MaterialTheme.typography.bodyMedium
            )
            
            IconButton(
                onClick = onNextPage,
                enabled = uiState.currentPage < uiState.totalPages && uiState.totalPages > 1
            ) {
                Icon(Icons.Default.NavigateNext, contentDescription = "Siguiente")
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
    }
}

