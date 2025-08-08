package com.example.qvapayappandroid.presentation.ui.home.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ErrorCard(
    errorMessage: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    var seconds by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }
    
    val isNetworkError = errorMessage.contains("timeout", ignoreCase = true) ||
            errorMessage.contains("connection", ignoreCase = true) ||
            errorMessage.contains("network", ignoreCase = true) ||
            errorMessage.contains("unable", ignoreCase = true) ||
            errorMessage.contains("HttpRequestTimeoutException", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNetworkError) "Error de Conexión ($seconds s)" else "Error ($seconds s)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Text(
                text = if (isNetworkError) {
                    "No se pudo conectar al servidor. Verifica tu conexión a internet."
                } else {
                    errorMessage
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            if (isNetworkError && onRetry != null) {
                Button(onClick = onRetry) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reintentar"
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Reintentar")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            if (isNetworkError && onRetry != null) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}