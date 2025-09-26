package com.example.qvapayappandroid.presentation.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import androidx.compose.foundation.layout.WindowInsets
import coil.compose.AsyncImage
import com.example.qvapayappandroid.R
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.presentation.ui.home.components.MyOfferStatusChip
import com.example.qvapayappandroid.presentation.ui.p2p.components.MiniCardM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.KycChipMiniM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.OfferChipMiniM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.getRatio
import com.example.qvapayappandroid.presentation.ui.p2p.components.toTwoDecimals
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOfferDetailScreen(
    offer: P2POffer,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEditOffer: (P2POffer) -> Unit = {},
    onShareOffer: (P2POffer) -> Unit = {},
    isCancellingOffer: String? = null,
    cancelOfferError: String? = null,
    onCancelOffer: (String, () -> Unit) -> Unit = { _, _ -> },
    navController: NavController? = null
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Detalles de mi Oferta") },
                navigationIcon = {
                    IconButton(onClick = { 
                        navController?.navigateUp() ?: onBackClick()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onShareOffer(offer) }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.qvapay_surface_light),
                    scrolledContainerColor = colorResource(id = R.color.qvapay_surface_light)
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditOffer(offer) },
                containerColor = colorResource(id = R.color.qvapay_purple_dark),
                contentColor = colorResource(id = R.color.white)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar oferta"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.qvapay_surface_light))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status and participants
            ParticipantsCard(offer = offer)

            // Transaction details
            TransactionInfoCard(offer = offer)

            // Additional details
            AdditionalDetailsCard(offer = offer)

            // Message when present
            offer.message?.takeIf { it.isNotBlank() }?.let { message ->
                MessageCard(message = message)
            }

            // Timing information
            DateInfoCard(offer = offer)

            // Show the cancel button only for processing or open offers
            if (offer.status?.lowercase() == "processing" || offer.status?.lowercase() == "open") {
                CancelOfferButton(
                    offerId = offer.uuid ?: "",
                    onCancelOffer = { offerId ->
                        onCancelOffer(offerId) {
                            navController?.navigateUp() ?: onBackClick()
                        }
                    },
                    isCancellingOffer = isCancellingOffer,
                    cancelOfferError = cancelOfferError
                )
            }
        }
    }
}

@Composable
private fun ParticipantsCard(
    offer: P2POffer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header showing the current status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado de la Oferta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                MyOfferStatusChip(status = offer.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Participants
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Owner (me)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!offer.owner?.profilePhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = offer.owner.profilePhotoUrl,
                            contentDescription = "Mi perfil",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.qvapay_purple_primary)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Mi perfil",
                                tint = colorResource(id = R.color.white),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = offer.owner?.name ?: "Yo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mi Oferta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "hacia",
                    tint = colorResource(id = R.color.qvapay_purple_primary),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Peer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!offer.peer?.profilePhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = offer.peer.profilePhotoUrl,
                            contentDescription = offer.peer.name ?: "Usuario",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.qvapay_purple_light)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = colorResource(id = R.color.white),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = offer.peer?.name ?: "Usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Contraparte",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniCardM3(
                        label = "Monto",
                        value = offer.amount.toTwoDecimals(),
                        color = colorResource(id = R.color.qvapay_purple_primary)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MiniCardM3(
                        label = "Recibes",
                        value = offer.receive.toTwoDecimals(),
                        color = colorResource(id = R.color.qvapay_purple_primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        color = colorResource(id = R.color.qvapay_purple_dark),
                        isTag = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MiniCardM3(
                        label = "Ratio",
                        value = offer.getRatio() ?: "-",
                        color = colorResource(id = R.color.qvapay_purple_light)
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
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
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

            // Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OfferChipMiniM3(type = offer.type)
                if (offer.onlyKyc == 1) {
                    KycChipMiniM3()
                }
                if (offer.onlyVip == 1) {
                    VipChipMiniM3()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extra information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (offer.uuid != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "ID de Oferta",
                            value = offer.uuid.take(8) + "...",
                            color = colorResource(id = R.color.qvapay_purple_text)
                        )
                    }
                }
                if (offer.valid != null) {
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "Válida",
                            value = if (offer.valid == 1) "Sí" else "No",
                            color = if (offer.valid == 1) colorResource(id = R.color.qvapay_purple_primary) else MaterialTheme.colorScheme.error
                        )
                    }
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
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Mensaje",
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
private fun DateInfoCard(
    offer: P2POffer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Información Temporal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                offer.createdAt?.let { createdAt ->
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "Creada",
                            value = formatDate(createdAt),
                            color = colorResource(id = R.color.qvapay_purple_text)
                        )
                    }
                }
                offer.updatedAt?.let { updatedAt ->
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "Actualizada",
                            value = formatDate(updatedAt),
                            color = colorResource(id = R.color.qvapay_purple_text)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelOfferButton(
    offerId: String,
    onCancelOffer: (String) -> Unit,
    isCancellingOffer: String?,
    cancelOfferError: String?,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cancelar Oferta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Esta oferta está activa o en proceso. Puedes cancelarla si es necesario.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isCancellingOffer == offerId) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Cancelando...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { onCancelOffer(offerId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancelar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Oferta")
                }
            }

            cancelOfferError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun VipChipMiniM3() {
    Surface(
        color = colorResource(id = R.color.qvapay_purple_light),
        shape = CircleShape
    ) {
        Text(
            text = "VIP",
            color = colorResource(id = R.color.white),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (_: Exception) {
        dateString.take(10) // Fallback: show only the date portion
    }
}
