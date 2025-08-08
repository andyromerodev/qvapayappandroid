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
import com.example.qvapayappandroid.presentation.ui.home.HomeViewModel
import com.example.qvapayappandroid.presentation.ui.home.MyOfferDetailScreen
import com.example.qvapayappandroid.presentation.ui.p2p.CreateP2POfferScreen
import com.example.qvapayappandroid.presentation.ui.p2p.CreateP2POfferViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.P2PScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2POfferDetailScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2PFiltersScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2PViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.P2POfferDetailViewModel
import com.example.qvapayappandroid.presentation.ui.settings.SettingsScreen
import com.example.qvapayappandroid.presentation.ui.profile.UserProfileScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    
    // Instancia compartida del ViewModel P2P
    val sharedP2PViewModel: P2PViewModel = koinViewModel()
    
    // Instancia compartida del HomeViewModel
    val sharedHomeViewModel: HomeViewModel = koinViewModel()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
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
                    onOfferClick = { offer ->
                        offer.uuid?.let { uuid ->
                            navController.navigate(AppDestinations.MyOfferDetail.createRoute(uuid))
                        }
                    },
                    viewModel = sharedHomeViewModel
                )
            }
            
            composable(AppDestinations.P2P.route) {
                P2PScreen(
                    viewModel = sharedP2PViewModel,
                    onOfferClick = { offer ->
                        offer.uuid?.let { uuid ->
                            navController.navigate(AppDestinations.P2POfferDetail.createRoute(uuid))
                        }
                    },
                    onShowFilters = {
                        navController.navigate(AppDestinations.P2PFilters.route)
                    }
                )
            }
            
            composable(AppDestinations.P2POfferDetail.route) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId")
                
                if (offerId != null) {
                    val offerDetailViewModel: P2POfferDetailViewModel = koinViewModel()
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
//                                onAcceptOffer = offerDetailViewModel::onAcceptOffer,
//                                isApplying = uiState.isApplying,
//                                applicationSuccessMessage = uiState.applicationSuccessMessage
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
            
            composable(AppDestinations.MyOfferDetail.route) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getString("offerId")
                
                if (offerId != null) {
                    val offer = sharedHomeViewModel.getOfferById(offerId)
                    
                    offer?.let { myOffer ->
                        MyOfferDetailScreen(
                            offer = myOffer,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onEditOffer = { offer ->
                                // TODO: Implementar navegación a editar oferta
                            },
                            onShareOffer = { offer ->
                                // TODO: Implementar compartir oferta
                            }
                        )
                    } ?: run {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Oferta no encontrada",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "La oferta que buscas no está disponible",
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
                } else {
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
            
            composable(AppDestinations.P2PFilters.route) {
                val uiState by sharedP2PViewModel.uiState.collectAsState()
                
                P2PFiltersScreen(
                    uiState = uiState,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onApplyFilters = { offerType, coins ->
                        sharedP2PViewModel.applyFilters(offerType, coins)
                        navController.popBackStack()
                    }
                )
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
                    viewModel = createOfferViewModel
                )
            }
            
            composable(AppDestinations.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout,
                    onProfileClick = {
                        navController.navigate(AppDestinations.UserProfile.route)
                    }
                )
            }
            
            composable(AppDestinations.UserProfile.route) {
                UserProfileScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}