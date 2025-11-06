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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.qvapayappandroid.R
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
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colorResource(id = R.color.qvapay_purple_light)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            // First row: "My Offer" icon and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Owner profile photo or a default icon
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
                            .background(colorResource(id = R.color.qvapay_purple_primary)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Mi oferta",
                            tint = colorResource(id = R.color.white),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.width(7.dp))

                Row(
                    modifier = Modifier.weight(1f),
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
                    // Peer profile photo
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
                                .background(colorResource(id = R.color.qvapay_surface_medium)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                tint = colorResource(id = R.color.qvapay_purple_text),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = offer.peer?.name ?: "Usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Chip
                MyOfferStatusChip(status = offer.status)
            }

            // Show the message when present
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

            // Compact grid of minicards (first row amount/receive, second row type/ratio)
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // First row: primary transaction information
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
                // Second row: supporting details
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

            // Mini chip row
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
