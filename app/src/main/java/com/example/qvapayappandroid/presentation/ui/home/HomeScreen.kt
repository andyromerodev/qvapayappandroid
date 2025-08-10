package com.example.qvapayappandroid.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.navigation.NavController
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.navigation.AppDestinations
import com.example.qvapayappandroid.presentation.ui.home.components.EmptyOffersState
import com.example.qvapayappandroid.presentation.ui.home.components.ErrorCard
import com.example.qvapayappandroid.presentation.ui.home.components.LoadingMoreIndicator
import com.example.qvapayappandroid.presentation.ui.home.components.MyOfferCard
import com.example.qvapayappandroid.presentation.ui.home.components.StatusFilterChips
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateOffer: () -> Unit = {},
    viewModel: HomeViewModel,
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Ofertas P2P") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateOffer
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Crear oferta P2P"
                )
            }
        }
    ) { paddingValues ->
        MyP2POffersSection(
            uiState = uiState,
            onRefresh = { viewModel.refreshOffers() },
            onLoadMore = { viewModel.loadMoreOffers() },
            onClearError = { viewModel.clearOffersError() },
            onStatusToggle = { viewModel.toggleStatusFilter(it) },
            onOfferClick = { offer ->
                offer.uuid?.let { offerId ->
                    navController?.navigate(AppDestinations.MyOfferDetail.createRoute(offerId))
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyP2POffersSection(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onClearError: () -> Unit,
    onStatusToggle: (String) -> Unit,
    onOfferClick: (P2POffer) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1)

            val condition1 = totalItemsNumber > 0
            val condition2 = lastVisibleItemIndex >= totalItemsNumber - 3
            val condition3 = uiState.hasNextPage
            val condition4 = !uiState.isLoadingMore
            // Ignorar loadingOffers si hay páginas disponibles - solo verificar que no esté ya paginando
            val condition5 = true // Removemos la condición de loadingOffers
            val condition6 = uiState.offersError == null

            val result = condition1 && condition2 && condition3 && condition4 && condition5 && condition6
            
            // Solo log cuando las condiciones cambian
            if (totalItemsNumber > 0) {
                Log.d("HomeScreen", "shouldLoadMore check - total: $totalItemsNumber, lastVisible: $lastVisibleItemIndex, hasNext: ${uiState.hasNextPage}, loadingMore: ${uiState.isLoadingMore}, loadingOffers: ${uiState.isLoadingOffers}, error: ${uiState.offersError}")
                Log.d("HomeScreen", "Conditions - 1: $condition1, 2: $condition2, 3: $condition3, 4: $condition4, 5: $condition5, 6: $condition6, RESULT: $result")
            }

            result
        }
    }

    LaunchedEffect(shouldLoadMore) {
        Log.d("HomeScreen", "LaunchedEffect triggered - shouldLoadMore = $shouldLoadMore")
        if (shouldLoadMore) {
            Log.d("HomeScreen", "Triggering loadMore - shouldLoadMore = true")
            onLoadMore()
        }
    }
    
    PullToRefreshBox(
        isRefreshing = uiState.isLoadingOffers,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StatusFilterChips(
                selectedStatuses = uiState.selectedStatusFilters,
                onStatusToggle = onStatusToggle
            )
            
            when {
                uiState.offersError != null -> {
                    ErrorCard(
                        errorMessage = uiState.offersError,
                        onDismiss = onClearError,
                        onRetry = onRefresh
                    )
                }
                
                uiState.myOffers.isEmpty() && !uiState.isLoadingOffers -> {
                    EmptyOffersState(
                        onRetry = onRefresh
                    )
                }
                
                else -> {
                    val offersToShow = if (uiState.selectedStatusFilters.isEmpty()) {
                        uiState.myOffers
                    } else {
                        uiState.filteredOffers
                    }
                    
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(offersToShow) { offer ->
                            MyOfferCard(
                                offer = offer,
                                onClick = onOfferClick
                            )
                        }
                        
                        if (uiState.isLoadingMore) {
                            item {
                                LoadingMoreIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

