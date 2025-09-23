package com.example.qvapayappandroid.presentation.ui.p2p

import P2POfferCard
import android.util.Log
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.presentation.ui.home.components.LoadingMoreIndicator
import com.example.qvapayappandroid.presentation.ui.p2p.components.ErrorRetryState
import com.example.qvapayappandroid.presentation.ui.p2p.components.LoadMoreRetryIndicator
import com.example.qvapayappandroid.presentation.ui.p2p.components.P2PShimmerEffect
import com.example.qvapayappandroid.presentation.ui.p2p.components.getRatio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PScreen(
    viewModel: P2PViewModel,
    onOfferClick: (P2POffer) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar datos cuando se abre P2PScreen
    LaunchedEffect(Unit) {
        Log.d("P2PScreen", "P2PScreen opened - loading P2P data")
        viewModel.loadP2PData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ofertas P2P") },
                actions = {
                    IconButton(onClick = onFiltersClick) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        val showBlockingLoader =
            uiState.isLoading ||
                    uiState.isRefreshing ||
                    (uiState.isFiltering && uiState.currentPage == 1)

        when {
            // Mostrar shimmer durante carga inicial, refresh o filtrado
            showBlockingLoader -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    P2PShimmerEffect()
                }
            }

            else -> {
                P2PContent(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onNextPage = { viewModel.onNextPage() },
                    onRefresh = { viewModel.refresh() },
                    onOfferClick = onOfferClick,
                    onSortByChanged = { viewModel.onSortByChanged(it) },
                    onSortOrderToggled = { viewModel.onSortOrderToggled() },
                    onRetryLoadMore = { viewModel.retryLoadMore() },
                    onRetryFirstLoad = { viewModel.retryFirstLoad() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun P2PContent(
    uiState: P2PUiState,
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit,
    onRefresh: () -> Unit,
    onOfferClick: (P2POffer) -> Unit,
    onSortByChanged: (String) -> Unit,
    onSortOrderToggled: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onRetryFirstLoad: () -> Unit,
) {
    val listState = rememberLazyListState()

    var userHasScrolled by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { inProgress ->
            if (inProgress) userHasScrolled = true
        }
    }

    val shouldLoadMore by remember(listState, uiState, userHasScrolled) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            val hasContent = uiState.offers.isNotEmpty()
            val atEnd = totalItems > 0 && lastVisibleIndex == totalItems - 1 // <- fin real de la lista
            val notLastPage = uiState.totalPages > 0 && uiState.currentPage < uiState.totalPages
            val idle = !uiState.isLoading && !uiState.isLoadingMore && !uiState.isFiltering
            val noError = uiState.errorMessage == null

            // Requiere:
            // - que el usuario haya scrolleado alguna vez (evita auto-carga si todo cabe en pantalla)
            // - estar exactamente en el último elemento
            val result = userHasScrolled &&
                    hasContent &&
                    atEnd &&
                    notLastPage &&
                    idle &&
                    noError

            if (totalItems > 0) {
                Log.d(
                    "P2PScreen",
                    "SCROLL DEBUG - total: $totalItems, lastVisible: $lastVisibleIndex, atEnd: $atEnd, userHasScrolled: $userHasScrolled"
                )
                Log.d(
                    "P2PScreen",
                    "shouldLoadMore - notLastPage: $notLastPage, idle: $idle, noError: $noError, RESULT: $result"
                )
            }

            result
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onNextPage()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Header con contador de ofertas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.totalOffers} ofertas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                        val list =
                            uiState.offers.sortedBy { it.getRatio()?.toDoubleOrNull() ?: 0.0 }
                        if (uiState.sortAsc) list else list.reversed()
                    }

                    "nombre" -> {
                        val list = uiState.offers.sortedBy { it.owner?.username?.lowercase() ?: "" }
                        if (uiState.sortAsc) list else list.reversed()
                    }

                    else -> uiState.offers
                }
            }
            
            // Ofertas, error o mensaje de vacío
            // Nota: Los estados de carga (isFiltering, isRefreshing, isLoading) ya se manejan en P2PScreen
            // por lo que aquí solo necesitamos manejar: errores, lista vacía, o lista con contenido
            when {
                // Mostrar errores
                uiState.errorMessage != null || uiState.isRetryingFirstLoad -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorRetryState(
                            errorMessage = uiState.errorMessage ?: "",
                            onRetry = onRetryFirstLoad,
                            isRetrying = uiState.isRetryingFirstLoad
                        )
                    }
                }

                // Mostrar mensaje de vacío SOLO cuando el resultado del filtro/carga es definitivamente vacío
                // No cuando la lista está vacía temporalmente durante el filtrado
                sortedOffers.isEmpty() && uiState.totalOffers == 0 && !uiState.isFiltering -> {
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
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(sortedOffers, key = { it.uuid ?: "" }) { offer ->
                            AnimatedListItem(
                                offer = offer,
                                onOfferClick = onOfferClick,
                                phoneNumber = offer.uuid?.let { uiState.phoneNumbersMap[it] }
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                LoadingMoreIndicator()
                            }
                        }

                        if (uiState.loadMoreError != null || uiState.isRetrying) {
                            item {
                                LoadMoreRetryIndicator(
                                    errorMessage = uiState.loadMoreError ?: "Reintentando...",
                                    onRetry = onRetryLoadMore,
                                    isRetrying = uiState.isRetrying
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedListItem(
    offer: P2POffer,
    onOfferClick: (P2POffer) -> Unit,
    phoneNumber: String?
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(offer.uuid) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 50
            )
        ) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it / 3 }
        )
    ) {
        P2POfferCard(
            offer = offer,
            onClick = onOfferClick,
            phoneNumber = phoneNumber
        )
    }
}

