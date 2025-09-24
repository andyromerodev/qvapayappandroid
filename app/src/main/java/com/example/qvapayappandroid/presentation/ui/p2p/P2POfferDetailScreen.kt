package com.example.qvapayappandroid.presentation.ui.p2p

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.presentation.ui.p2p.components.KycChipMiniM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.MiniCardM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.OfferChipMiniM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.getRatio
import com.example.qvapayappandroid.presentation.ui.p2p.components.toTwoDecimals
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2POfferDetailScreen(
    modifier: Modifier = Modifier,
    offer: P2POffer,
    onBackClick: () -> Unit,
    onContactUser: () -> Unit = {},
    onAcceptOffer: () -> Unit = {},
    viewModel: P2POfferDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is P2POfferDetailViewModel.Effect.NavigateBack -> onBackClick()
                is P2POfferDetailViewModel.Effect.ShowError -> {
                    // Error handling can be done here if needed
                }
                is P2POfferDetailViewModel.Effect.ShowApplicationSuccess -> {
                    // Success handling can be done here if needed
                }
            }
        }
    }

    P2POfferDetailContent(
        offer = offer,
        uiState = uiState,
        onBackClick = onBackClick,
        onContactUser = onContactUser,
        onAcceptOffer = onAcceptOffer,
        modifier = modifier
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun P2POfferDetailContent(
    modifier: Modifier = Modifier,
    offer: P2POffer,
    uiState: P2POfferDetailViewModel.UiState,
    onBackClick: () -> Unit,
    onContactUser: () -> Unit,
    onAcceptOffer: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Oferta P2P") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offer status and owner
            OfferOwnerCard(offer = offer)

            // Transaction information
            TransactionInfoCard(offer = offer)

            // Additional details
            AdditionalDetailsCard(offer = offer)

            // Message, if provided
            offer.message?.takeIf { it.isNotBlank() }?.let { message ->
                MessageCard(message = message)
            }

            // Success banner when the application completed
            uiState.applicationSuccessMessage?.let { successMessage ->
                SuccessMessageCard(message = successMessage)
            }

            // Error banner when something went wrong
            uiState.errorMessage?.let { errorMessage ->
                ErrorMessageCard(message = errorMessage)
            }

            // Action buttons
            ActionButtonsRow(
                onContactUser = onContactUser,
                onAcceptOffer = onAcceptOffer,
                isApplying = uiState.isApplying,
                isOfferApplied = false
            )
        }
    }
}

@Composable
private fun OfferOwnerCard(
    offer: P2POffer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with the offer type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OfferChipMiniM3(type = offer.type)
                if (offer.onlyKyc == 1) {
                    KycChipMiniM3()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Owner information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Owner avatar
                val username = offer.owner?.username?.trim().orEmpty()
                val initial = username.firstOrNull()?.uppercase() ?: "?"

                if (!offer.owner?.profilePhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = offer.owner.profilePhotoUrl,
                        contentDescription = username,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User information
                Column {
                    Text(
                        text = username.ifEmpty { "Usuario Desconocido" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    offer.owner?.let { owner ->
                        if (!owner.name.isNullOrBlank()) {
                            Text(
                                text = "${owner.name} ${owner.lastname ?: ""}".trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Star-based rating
                        owner.averageRating?.toDoubleOrNull()?.let { rating ->
                            if (rating > 0.0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Rating",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format(Locale.US, "%.1f", rating),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
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
private fun TransactionInfoCard(
    offer: P2POffer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Información de la Transacción",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // First row: amount and receive
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniCardM3(
                        label = "Monto",
                        value = offer.amount.toTwoDecimals(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MiniCardM3(
                        label = "Recibes",
                        value = offer.receive.toTwoDecimals(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Second row: coin type and ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniCardM3(
                        label = "Tipo de Moneda",
                        value = offer.coinData?.tick
                            ?: offer.coinData?.name
                            ?: offer.coin ?: "N/A",
                        color = MaterialTheme.colorScheme.secondary,
                        isTag = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MiniCardM3(
                        label = "Ratio",
                        value = offer.getRatio() ?: "-",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun AdditionalDetailsCard(
    offer: P2POffer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Detalles Adicionales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User verification badges
            offer.owner?.let { owner ->
                if (owner.kyc == 1 || owner.goldenCheck == 1 || owner.vip == 1) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (owner.kyc == 1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✅",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Usuario verificado por KYC",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        if (owner.goldenCheck == 1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏆",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Usuario verificado Gold",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        if (owner.vip == 1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⭐",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Usuario VIP",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Coin information when available
            offer.coinData?.let { coinData ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Moneda: ${coinData.name ?: offer.coin}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Offer ID when available
            offer.uuid?.let { uuid ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔗",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ID: ${uuid.take(8)}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Mensaje del Usuario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuccessMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Éxito",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¡Aplicación Exitosa!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    modifier: Modifier = Modifier,
    onContactUser: () -> Unit,
    onAcceptOffer: () -> Unit,
    isApplying: Boolean,
    isOfferApplied: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onContactUser,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contactar")
        }

        Button(
            onClick = onAcceptOffer,
            modifier = Modifier.weight(1f),
            enabled = !isApplying && !isOfferApplied
        ) {
            when {
                isOfferApplied -> {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Oferta Aplicada")
                }
                isApplying -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aplicando...")
                }
                else -> {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aceptar Oferta")
                }
            }
        }
    }
}
