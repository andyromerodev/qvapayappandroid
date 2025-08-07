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
import androidx.compose.material.icons.filled.Star
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
import java.util.Locale

@Composable
fun P2POfferCard(
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
            // Primera fila: avatar, user, rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val username = offer.owner?.name?.trim().orEmpty()
                val initial = username.firstOrNull()?.uppercase() ?: "?"

                if (!offer.owner?.profilePhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = offer.owner.profilePhotoUrl,
                        contentDescription = username,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.width(7.dp))

                Text(
                    text = username.ifEmpty { "Desconocido" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                // Rating
                offer.owner?.averageRating?.toDoubleOrNull()?.let { rating ->
                    if (rating > 0.0) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f", rating),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

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
            }
        }
    }
}


