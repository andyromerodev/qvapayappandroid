package com.example.qvapayappandroid.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.ui.res.colorResource
import com.example.qvapayappandroid.R
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.navigation.NavController
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.navigation.AppDestinations
import com.example.qvapayappandroid.presentation.ui.home.components.EmptyOffersState
import com.example.qvapayappandroid.presentation.ui.home.components.ErrorCard
import com.example.qvapayappandroid.presentation.ui.home.components.LoadingMoreIndicator
import com.example.qvapayappandroid.presentation.ui.home.components.MyOfferCard
import com.example.qvapayappandroid.presentation.ui.home.components.MyOfferShimmerEffect
import com.example.qvapayappandroid.presentation.ui.home.components.StatusFilterChips
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateOffer: () -> Unit = {},
    viewModel: HomeViewModel,
    navController: NavController? = null
) {
    val uiState by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSuccessMessage -> {
                    // Success messages can be handled here with snackbar or similar
                    Log.d("HomeScreen", "Success: ${effect.message}")
                }
                is HomeEffect.ShowErrorMessage -> {
                    // Error messages can be handled here with snackbar or similar
                    Log.e("HomeScreen", "Error: ${effect.message}")
                }
                is HomeEffect.NavigateToOfferDetail -> {
                    navController?.navigate(AppDestinations.MyOfferDetail.createRoute(effect.offerId))
                }
                is HomeEffect.NavigateToCreateOffer -> {
                    onCreateOffer()
                }
                is HomeEffect.RefreshOffers -> {
                    // Could trigger UI refresh animation or other visual feedback
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(color = colorResource(id = R.color.qvapay_surface_light)),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Ofertas",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.qvapay_surface_light),
                    scrolledContainerColor = colorResource(id = R.color.qvapay_surface_light)
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateOffer,
                containerColor = colorResource(id = R.color.qvapay_purple_dark),
                contentColor = colorResource(id = R.color.white)
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
            onRefresh = { viewModel.handleIntent(HomeIntent.RefreshOffers) },
            onLoadMore = { viewModel.handleIntent(HomeIntent.LoadMoreOffers) },
            onClearError = { viewModel.handleIntent(HomeIntent.ClearOffersError) },
            onStatusToggle = { viewModel.handleIntent(HomeIntent.ToggleStatusFilter(it)) },
            onOfferClick = { offer ->
                offer.uuid?.let { offerId ->
                    navController?.navigate(AppDestinations.MyOfferDetail.createRoute(offerId))
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues).background(color = colorResource(id = R.color.qvapay_surface_light))
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyP2POffersSection(
    uiState: HomeState,
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

            val result = totalItemsNumber > 0 && 
                    lastVisibleItemIndex >= totalItemsNumber - 3 && 
                    uiState.canLoadMore
            
            if (totalItemsNumber > 0) {
                Log.d("HomeScreen", "shouldLoadMore check - total: $totalItemsNumber, lastVisible: $lastVisibleItemIndex, canLoadMore: ${uiState.canLoadMore}, RESULT: $result")
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
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(color = colorResource(id = R.color.qvapay_surface_light))
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
                
                uiState.shouldShowLoading -> {
                    MyOfferShimmerEffect()
                }
                
                uiState.shouldShowEmpty -> {
                    EmptyOffersState(
                        onRetry = onRefresh
                    )
                }
                
                else -> {
                    val offersToShow = uiState.displayOffers
                    
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(offersToShow, key = { it.uuid ?: "" }) { offer ->
                            AnimatedOfferItem(
                                offer = offer,
                                onOfferClick = onOfferClick
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

@Composable
private fun AnimatedOfferItem(
    offer: P2POffer,
    onOfferClick: (P2POffer) -> Unit
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
        MyOfferCard(
            offer = offer,
            onClick = onOfferClick
        )
    }
}
