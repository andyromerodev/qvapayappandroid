package com.example.qvapayappandroid.presentation.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.qvapayappandroid.navigation.AppDestinations
import com.example.qvapayappandroid.presentation.ui.components.BottomNavigationBar
import com.example.qvapayappandroid.presentation.ui.home.HomeScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2PScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2POfferDetailScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2PFiltersScreen
import com.example.qvapayappandroid.presentation.ui.p2p.P2PViewModel
import com.example.qvapayappandroid.presentation.ui.settings.SettingsScreen
import com.example.qvapayappandroid.data.model.P2POffer
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    var selectedOffer by remember { mutableStateOf<P2POffer?>(null) }
    
    // Instancia compartida del ViewModel P2P
    val sharedP2PViewModel: P2PViewModel = koinViewModel()
    
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
                    onLogout = onLogout
                )
            }
            
            composable(AppDestinations.P2P.route) {
                P2PScreen(
                    viewModel = sharedP2PViewModel,
                    onOfferClick = { offer ->
                        selectedOffer = offer
                        navController.navigate(AppDestinations.P2POfferDetail.route)
                    },
                    onShowFilters = {
                        navController.navigate(AppDestinations.P2PFilters.route)
                    }
                )
            }
            
            composable(AppDestinations.P2POfferDetail.route) {
                selectedOffer?.let { offer ->
                    P2POfferDetailScreen(
                        offer = offer,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onContactUser = {
                            // TODO: Implementar contacto con usuario
                        },
                        onAcceptOffer = {
                            // TODO: Implementar aceptación de oferta
                        }
                    )
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
            
            composable(AppDestinations.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}