package com.example.qvapayappandroid.presentation.ui.p2p.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qvapayappandroid.R
import com.example.qvapayappandroid.data.model.P2POffer
import java.util.Locale

@Composable
fun MiniCardM3(
    label: String,
    value: String,
    color: Color,
    isTag: Boolean = false
) {
    Column(
        modifier = Modifier.Companion
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Companion.Medium,
            fontSize = 10.sp,
        )
        if (isTag) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(5.dp)
            ) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Companion.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.Companion.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        } else {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun OfferChipMiniM3(type: String?) {
    Surface(
        color = if (type == "buy") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = when (type) {
                "buy" -> "COMPRA"
                "sell" -> "VENTA"
                else -> "N/A"
            }.uppercase(),
            color = if (type == "buy") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Companion.Bold,
            fontSize = 10.sp,
            modifier = Modifier.Companion.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun KycChipMiniM3() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
    ) {
        Text(
            text = "KYC",
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Companion.Bold,
            fontSize = 10.sp,
            modifier = Modifier.Companion.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun GoldenCheckChipMiniM3() {
    Surface(
        color = Color(0xFFFFF8DC),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD700)),
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.award_star_24px),
            contentDescription = "Golden Check",
            tint = Color(0xFFFFD700),
            modifier = Modifier
                .size(28.dp) // Larger icon size without padding reduction
                .padding(4.dp) // Internal padding within the Surface
        )
    }
}



fun String?.toTwoDecimals(): String =
    this?.toDoubleOrNull()?.let { String.Companion.format(Locale.US, "%.2f", it) } ?: "N/A"

fun P2POffer.getRatio(): String? {
    val receiveVal = receive?.toDoubleOrNull()
    val amountVal = amount?.toDoubleOrNull()
    return if (receiveVal != null && amountVal != null && amountVal > 0) {
        String.Companion.format(Locale.US, "%.2f", receiveVal / amountVal)
    } else null
}