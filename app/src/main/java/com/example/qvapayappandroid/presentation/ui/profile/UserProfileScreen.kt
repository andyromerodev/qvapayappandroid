package com.example.qvapayappandroid.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBarDefaults
import coil.compose.SubcomposeAsyncImage
import com.example.qvapayappandroid.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: UserProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val (snackbarController, snackbarHostState) = rememberSnackbarController()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserProfileEffect.NavigateToLogin -> onLogout()
                is UserProfileEffect.ShowSuccessMessage -> {
                    snackbarController.showSuccess(effect.message)
                }
                is UserProfileEffect.ShowErrorMessage -> {
                    snackbarController.showError(effect.message)
                }
                is UserProfileEffect.ShowLogoutConfirmation -> {
                    // Could implement logout confirmation dialog here
                }
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleIntent(UserProfileIntent.RefreshUserProfile) },
                        enabled = uiState.canRefresh
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualizar perfil"
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { viewModel.handleIntent(UserProfileIntent.Logout) },
                        enabled = uiState.canLogout
                    ) {
                        if (uiState.isLoggingOut) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.qvapay_surface_light),
                    scrolledContainerColor = colorResource(id = R.color.qvapay_surface_light)
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.qvapay_surface_light))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                uiState.shouldShowLoading -> {
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
                            Text("Cargando perfil de usuario...")
                        }
                    }
                }
                
                uiState.shouldShowContent -> {
                    UserProfileContent(
                        state = uiState,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                uiState.shouldShowError -> {
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
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = { viewModel.handleIntent(UserProfileIntent.RetryLoadProfile) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = colorResource(id = R.color.qvapay_purple_primary)
                                    )
                                ) {
                                    Text("Reintentar")
                                }
                                TextButton(
                                    onClick = { viewModel.handleIntent(UserProfileIntent.ClearError) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = colorResource(id = R.color.qvapay_purple_text)
                                    )
                                ) {
                                    Text("Cerrar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileContent(
    state: UserProfileState,
    modifier: Modifier = Modifier
) {
    val user = state.user!!
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.qvapay_purple_primary)
            ),
            border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                SubcomposeAsyncImage(
                    model = user.profilePhotoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = colorResource(id = R.color.white)
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = state.userDisplayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
                
                Text(
                    text = state.userUsername,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(id = R.color.white)
                )
                
                if (state.hasBio) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.bio!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(id = R.color.white)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.qvapay_purple_light)
            ),
            border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_primary))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Balance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.white)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(id = R.color.white)
                        )
                        Text(
                            text = state.formattedBalance,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.white)
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Pendiente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(id = R.color.white)
                        )
                        Text(
                            text = state.formattedPendingBalance,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(id = R.color.white)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Satoshis: ${state.formattedSatoshis}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = R.color.white)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
            border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Información de la cuenta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.qvapay_purple_primary)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoRow("UUID", user.uuid)
                ProfileInfoRow("Email", state.emailDisplayText)
                ProfileInfoRow("País", state.countryDisplayText)
                ProfileInfoRow("Rol", user.role)
                ProfileInfoRow("Rating", state.ratingDisplayText)
                ProfileInfoRow("KYC", state.kycStatusText)
                ProfileInfoRow("VIP", state.vipStatusText)
                ProfileInfoRow("P2P", state.p2pStatusText)
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(id = R.color.qvapay_purple_text),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.qvapay_purple_primary)
        )
    }
}