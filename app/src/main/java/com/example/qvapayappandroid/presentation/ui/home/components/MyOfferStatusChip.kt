package com.example.qvapayappandroid.presentation.ui.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qvapayappandroid.R

@Composable
fun MyOfferStatusChip(
    status: String?,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, text) = when (status?.lowercase()) {
        "open", "abierta", "activa" -> Triple(
            colorResource(id = R.color.qvapay_purple_primary),
            colorResource(id = R.color.white),
            "ACTIVA"
        )
        "completed", "completada", "finalizada" -> Triple(
            colorResource(id = R.color.qvapay_purple_light),
            colorResource(id = R.color.qvapay_purple_text),
            "COMPLETADA"
        )
        "cancelled", "cancelada" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "CANCELADA"
        )
        "paused", "pausada" -> Triple(
            colorResource(id = R.color.qvapay_surface_medium),
            colorResource(id = R.color.qvapay_purple_text),
            "PAUSADA"
        )
        "pending", "pendiente" -> Triple(
            colorResource(id = R.color.qvapay_surface_medium),
            colorResource(id = R.color.qvapay_purple_text),
            "PENDIENTE"
        )
        else -> Triple(
            colorResource(id = R.color.qvapay_surface_medium),
            colorResource(id = R.color.qvapay_purple_text),
            status?.uppercase() ?: "N/A"
        )
    }

    Surface(
        color = containerColor,
        shape = CircleShape,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}