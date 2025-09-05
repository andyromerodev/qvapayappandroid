import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qvapayappandroid.R
import com.example.qvapayappandroid.data.model.CoinData
import com.example.qvapayappandroid.data.model.Owner
import com.example.qvapayappandroid.data.model.P2POffer
import com.example.qvapayappandroid.presentation.ui.p2p.components.KycChipMiniM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.GoldenCheckChipMiniM3
import com.example.qvapayappandroid.presentation.ui.p2p.components.getRatio
import com.example.qvapayappandroid.presentation.ui.p2p.components.toTwoDecimals
import java.util.Locale

@Composable
fun P2POfferCard(
    offer: P2POffer,
    onClick: (P2POffer) -> Unit,
    modifier: Modifier = Modifier,
    phoneNumber: String? = null
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
            // First row: avatar, user, rating
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                // Chips y Rating juntos a la derecha
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (offer.owner?.goldenCheck == 1) {
                        GoldenCheckChipMiniM3()
                    }

                    if (offer.onlyKyc == 1) {
                        KycChipMiniM3()
                    }

                    // Rating
                    offer.owner?.averageRating?.toDoubleOrNull()?.let { rating ->
                        if (rating > 0.0) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(4.dp),
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
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.1f", rating),
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 10.sp,
                                        modifier = Modifier.Companion.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
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
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 6.dp)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))



            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                SentenceRow(
                    prefix = if (offer.type == "buy") "Compra" else "Vende",
                    amountOrRatio = "$${offer.amount.toTwoDecimals()}",
                    suffix = "en saldo ${"Qvapay"}",
                )
                ThinDivider()

                SentenceRow(
                    prefix = if (offer.type == "buy") "Envía" else "Recibe",
                    amountOrRatio = "$${offer.receive.toTwoDecimals()}",
                    suffix = "en ${offer.coinData?.tick ?: offer.coin ?: "N/A"}",
                )
                ThinDivider()

                SentenceRowWithPhone(
                    prefix = "Ratio",
                    amountOrRatio = offer.getRatio() ?: "-",
                    suffix = null,
                    phoneNumber = phoneNumber
                )
            }


            Spacer(modifier = Modifier.height(7.dp))
        }
    }
}



@Composable
private fun SentenceRow(
    prefix: String,
    amountOrRatio: String,
    suffix: String? = null,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant


    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        text = buildAnnotatedString {
            append("$prefix ")
            withStyle(SpanStyle(color = onSurface, fontWeight = FontWeight.ExtraBold)) {
                append(amountOrRatio)
            }
            if (!suffix.isNullOrBlank()) {
                append(" ")
                withStyle(SpanStyle(color = onSurfaceVariant, fontWeight = FontWeight.Medium)) {
                    append(suffix)
                }
            }
        },
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        color = onSurface,
        maxLines = 1
    )
}

@Composable
private fun SentenceRowWithPhone(
    prefix: String,
    amountOrRatio: String,
    suffix: String? = null,
    phoneNumber: String? = null
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = buildAnnotatedString {
                append("$prefix ")
                withStyle(SpanStyle(color = onSurface, fontWeight = FontWeight.ExtraBold)) {
                    append(amountOrRatio)
                }
                if (!suffix.isNullOrBlank()) {
                    append(" ")
                    withStyle(SpanStyle(color = onSurfaceVariant, fontWeight = FontWeight.Medium)) {
                        append(suffix)
                    }
                }
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            color = onSurface,
            maxLines = 1
        )
        
        // WhatsApp and Telegram icons if phone number detected
        phoneNumber?.let { phone ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp button
                Surface(
                    onClick = { openWhatsApp(context, phone) },
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF25D366)),
                    color = Color(0xFFE8F5E8) // Verde muy claro
                ) {
                    Icon(
                        painter = painterResource(R.drawable.whatsapp_svgrepo_com),
                        contentDescription = "Abrir WhatsApp",
                        tint = Color(0xFF25D366), // WhatsApp green color
                        modifier = Modifier
                            .size(16.dp)
                            .padding(4.dp)
                    )
                }
                
                // Telegram button
                Surface(
                    onClick = { openTelegram(context, phone) },
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF0088CC)),
                    color = Color(0xFFE3F2FD) // Azul muy claro
                ) {
                    Icon(
                        painter = painterResource(R.drawable.telegram_icon),
                        contentDescription = "Abrir Telegram",
                        tint = Color(0xFF0088CC), // Telegram blue color
                        modifier = Modifier
                            .size(16.dp)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )
}


private fun openWhatsApp(context: Context, phoneNumber: String) {
    try {
        val formattedNumber = if (phoneNumber.startsWith("+")) {
            phoneNumber.replace("+", "")
        } else {
            phoneNumber
        }
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$formattedNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to regular phone call if WhatsApp is not available
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}

private fun openTelegram(context: Context, phoneNumber: String) {
    try {
        val formattedNumber = if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            "+$phoneNumber"
        }
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://t.me/$formattedNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to regular phone call if Telegram is not available
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}


@Preview(showBackground = true)
@Composable
fun P2POfferCardPreview() {
    MaterialTheme {
        P2POfferCard(
            offer = P2POffer(
                uuid = "sample-uuid",
                type = "buy",
                amount = "1000.00",
                receive = "1150.00",
                coin = "CLASICA",
                coinData = CoinData(
                    name = "CLASICA",
                    tick = "CLASICA"
                ),
                owner = Owner(
                    name = "JuanCarlos",
                    username = "juancarlos_dev",
                    bio = "Trader profesional",
                    kyc = 1,
                    goldenCheck = 1,
                    averageRating = "4.8"
                ),
                message = "🫵🏻53768488 SI NO ESTOY EN LÍNEA ME ESCRIBES GRACIAS🫵🏻",
                onlyKyc = 1,
                createdAt = "2025-01-15T10:30:00Z"
            ),
            onClick = { },
            phoneNumber = "53768488"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun P2POfferCardNoRatingPreview() {
    MaterialTheme {
        P2POfferCard(
            offer = P2POffer(
                uuid = "sample-uuid-2",
                type = "Compra",
                amount = "500.00",
                receive = "575.00",
                coin = "CUP",
                coinData = CoinData(
                    name = "Peso Cubano",
                    tick = "CUP"
                ),
                owner = Owner(
                    name = "Maria",
                    username = "maria_trader",
                    bio = null,
                    kyc = 0,
                    averageRating = null
                ),
                message = null,
                onlyKyc = 0,
                createdAt = "2025-01-15T09:15:00Z"
            ),
            onClick = { }
        )
    }
}




