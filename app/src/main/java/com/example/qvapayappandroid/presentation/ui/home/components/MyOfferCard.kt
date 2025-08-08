package com.example.qvapayappandroid.presentation.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun MyOfferCard(
    offer: P2POffer,
    onClick: (P2POffer) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(offer) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            // Primera fila: icono "Mi Oferta", estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil del owner o icono por defecto
                if (!offer.owner?.profilePhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = offer.owner.profilePhotoUrl,
                        contentDescription = offer.owner.name ?: "Mi perfil",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Mi oferta",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.width(7.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = offer.owner?.name ?: "Yo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "hacia",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    // Foto de perfil del peer
                    if (!offer.peer?.profilePhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = offer.peer.profilePhotoUrl,
                            contentDescription = offer.peer.name ?: "Usuario",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = offer.peer?.name ?: "Usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Status Chip
                MyOfferStatusChip(status = offer.status)
            }

            // Mensaje si existe
            offer.message?.takeIf { it.isNotBlank() }?.let { msg ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Minicards en grid optimizado (primera fila: monto y recibe, segunda fila: tipo y ratio)
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Primera fila: Información principal (transacción)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "MONTO",
                            value = offer.amount.toTwoDecimals(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "RECIBE",
                            value = offer.receive.toTwoDecimals(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                // Segunda fila: Información secundaria (detalles)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "TIPO",
                            value = offer.coinData?.tick
                                ?: offer.coinData?.name
                                ?: offer.coin ?: "N/A",
                            color = MaterialTheme.colorScheme.secondary,
                            isTag = true
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MiniCardM3(
                            label = "RATIO",
                            value = offer.getRatio() ?: "-",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // CHIPS en fila mini
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OfferChipMiniM3(type = offer.type)
                if (offer.onlyKyc == 1) {
                    KycChipMiniM3()
                }
                if (offer.onlyVip == 1) {
                    VipChipMiniM3()
                }
            }
        }
    }
}

@Composable
private fun VipChipMiniM3() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape
    ) {
        Text(
            text = "VIP",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}