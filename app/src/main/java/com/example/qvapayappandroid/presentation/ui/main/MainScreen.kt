package com.example.qvapayappandroid.presentation.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qvapayappandroid.navigation.AppDestinations
import com.example.qvapayappandroid.presentation.ui.components.BottomNavigationBar
import com.example.qvapayappandroid.presentation.ui.home.HomeScreen
import com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer.CreateP2POfferScreen
import com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer.CreateP2POfferViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.P2PScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2PFiltersScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2POfferDetailScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2POfferDetailViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.p2pWebView.P2PWebViewScreen
import com.example.qvapayappandroid.presentation.ui.settings.SettingsScreen
import com.example.qvapayappandroid.presentation.ui.profile.UserProfileScreen
import com.example.qvapayappandroid.presentation.ui.home.HomeViewModel
import com.example.qvapayappandroid.presentation.ui.home.MyOfferDetailScreen
import com.example.qvapayappandroid.presentation.ui.webview.WebViewFullScreen
import androidx.compose.runtime.LaunchedEffect
import com.example.qvapayappandroid.presentation.ui.p2p.P2PViewModel
import com.example.qvapayappandroid.presentation.ui.templates.OfferTemplatesScreen
import com.example.qvapayappandroid.presentation.ui.templates.SaveOfferTemplateScreen
import com.example.qvapayappandroid.presentation.ui.alerts.OfferAlertsScreen
import com.example.qvapayappandroid.presentation.ui.home.HomeIntent
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val homeViewModel: HomeViewModel = koinViewModel()
    val p2pViewModel: P2PViewModel = koinViewModel()
    val offerDetailViewModel: P2POfferDetailViewModel = koinViewModel()

    // Routes that should not show the bottom navigation bar
    val routesWithoutBottomNav = setOf(
        AppDestinations.UserProfile.route,
        AppDestinations.MyOfferDetail.route,
        AppDestinations.P2POfferDetail.route,
        AppDestinations.CreateP2POffer.route,
        AppDestinations.P2PFilters.route,
//        AppDestinations.WebView.route,
        AppDestinations.P2PWebView.route,
        AppDestinations.OfferAlerts.route,
//        AppDestinations.Templates.route,
        AppDestinations.SaveOfferTemplate.route,
        AppDestinations.EditOfferTemplate.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute !in routesWithoutBottomNav) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.Home.route) {
                HomeScreen(
                    onCreateOffer = {
                        navController.navigate(AppDestinations.CreateP2POffer.route)
                    },
                    navController = navController,
                    viewModel = homeViewModel
                )
            }

            composable(AppDestinations.MyOfferDetail.route) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId")

                if (offerId != null) {
                    val uiState by homeViewModel.state.collectAsState()

                    homeViewModel.getOfferById(offerId)?.let { offer ->
                        MyOfferDetailScreen(
                            offer = offer,
                            onBackClick = {
                                navController.navigateUp()
                            },
                            isCancellingOffer = uiState.isCancellingOffer,
                            cancelOfferError = uiState.cancelOfferError,
                            onCancelOffer = { id, onSuccess ->
                                homeViewModel.handleIntent(HomeIntent.CancelOffer(id, onSuccess))
                            },
                            onEditOffer = { offer ->
                                // TODO: Hook up navigation to the edit screen
                            },
                            onShareOffer = { offer ->
                                // TODO: Wire up the share flow
                            },
                            navController = navController
                        )
                    } ?: run {
                        // If the offer is missing, navigate back
                        LaunchedEffect(Unit) {
                            navController.navigateUp()
                        }
                    }
                } else {
                    // If no offerId is provided, navigate back
                    LaunchedEffect(Unit) {
                        navController.navigateUp()
                    }
                }
            }
            
            composable(AppDestinations.P2P.route) {
                P2PScreen(
                    onOfferClick = { offer ->
                        offer.uuid?.let { uuid ->
                            navController.navigate(AppDestinations.P2POfferDetail.createRoute(uuid))
                        }
                    },
                    onFiltersClick = {
                        navController.navigate(AppDestinations.P2PFilters.route)
                    },
                    navController = navController,
                    viewModel = p2pViewModel
                )
            }
            
            composable(AppDestinations.P2PFilters.route) {
                val uiState by p2pViewModel.uiState.collectAsState()
                
                P2PFiltersScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onApplyFilters = { offerType, coins ->
                        p2pViewModel.applyFilters(offerType, coins)
                    },
                    selectedOfferType = uiState.selectedOfferType,
                    selectedCoins = uiState.selectedCoins,
                    availableCoins = uiState.availableCoins
                )
            }
            
            composable(AppDestinations.P2POfferDetail.route) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId")
                
                if (offerId != null) {

                    val uiState by offerDetailViewModel.uiState.collectAsState()
                    
                    LaunchedEffect(offerId) {
                        offerDetailViewModel.loadOffer(offerId)
                    }
                    
                    LaunchedEffect(Unit) {
                        offerDetailViewModel.effect.collect { effect ->
                            when (effect) {
                                is P2POfferDetailViewModel.Effect.NavigateBack -> {
                                    navController.popBackStack()
                                }
                                is P2POfferDetailViewModel.Effect.ShowError -> {
                                    // Error handling is done in the UI state
                                }
                                is P2POfferDetailViewModel.Effect.ShowApplicationSuccess -> {
                                    // Success handling is done in the UI state
                                }
                            }
                        }
                    }
                    
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.offer != null -> {
                            P2POfferDetailScreen(
                                offer = uiState.offer!!,
                                onBackClick = offerDetailViewModel::onBackClick,
                                onContactUser = offerDetailViewModel::onContactUser,
                                onAcceptOffer = {
                                    uiState.offer?.uuid?.let { uuid ->
                                        navController.navigate(AppDestinations.P2PWebView.createRoute(uuid))
                                    }
                                }
                            )
                        }
                        uiState.errorMessage != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Error al cargar la oferta",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.errorMessage!!,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { navController.popBackStack() }
                                    ) {
                                        Text("Volver")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Handle missing offerId parameter
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ID de oferta no válido",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Volver")
                            }
                        }
                    }
                }
            }
            
            
            composable(AppDestinations.CreateP2POffer.route) {
                val createOfferViewModel: CreateP2POfferViewModel = koinViewModel()
                
                CreateP2POfferScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.popBackStack()
                    },
                    onLoadTemplates = {
                        navController.navigate(AppDestinations.Templates.route)
                    },
                    onSaveAsTemplate = { state ->
                        navController.navigate(AppDestinations.SaveOfferTemplate.route)
                    },
                    viewModel = createOfferViewModel
                )
            }
            
            composable(AppDestinations.WebView.route) {
                WebViewFullScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    initialUrl = "https://qvapay.com"
                )
            }
            
            composable(AppDestinations.P2PWebView.route) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId")
                
                if (offerId != null) {
                    P2PWebViewScreen(
                        offerId = offerId,
                        onClose = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
            
            composable(AppDestinations.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout,
                    onProfileClick = {
                        navController.navigate(AppDestinations.UserProfile.route)
                    },
                    onAlertsClick = {
                        navController.navigate(AppDestinations.OfferAlerts.route)
                    }
                )
            }
            
            composable(AppDestinations.UserProfile.route) {
                UserProfileScreen(
                    onLogout = onLogout,
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
            
            composable(AppDestinations.OfferAlerts.route) {
                OfferAlertsScreen(
                    navController = navController
                )
            }
            
            composable(AppDestinations.Templates.route) {
                OfferTemplatesScreen(
                    onNavigateToEditTemplate = { templateId ->
                        navController.navigate(AppDestinations.EditOfferTemplate.createRoute(templateId))
                    },
                    onNavigateToCreateTemplate = {
                        navController.navigate(AppDestinations.SaveOfferTemplate.route)
                    },
                    onNavigateToCreateOffer = { template ->
                        navController.navigate(AppDestinations.CreateP2POffer.route)
                    }
                )
            }
            
            composable(AppDestinations.SaveOfferTemplate.route) {
                SaveOfferTemplateScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(AppDestinations.EditOfferTemplate.route) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId")?.toLongOrNull()
                
                SaveOfferTemplateScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
