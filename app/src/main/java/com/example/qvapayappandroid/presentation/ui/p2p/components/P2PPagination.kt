package com.example.qvapayappandroid.presentation.ui.p2p.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun P2PPagination(
    currentPage: Int,
    totalPages: Int,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp), // Menos padding vertical
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousPage,
            enabled = currentPage > 1 && totalPages > 1,
            modifier = Modifier.size(32.dp) // Botón más pequeño
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                contentDescription = "Anterior",
                modifier = Modifier.size(18.dp) // Icono más pequeño
            )
        }

        Spacer(modifier = Modifier.width(4.dp)) // Menor separación

        Text(
            text = if (totalPages > 0) "Página $currentPage de $totalPages" else "Sin páginas",
            style = MaterialTheme.typography.bodySmall, // Texto más pequeño
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.width(4.dp)) // Menor separación

        IconButton(
            onClick = onNextPage,
            enabled = currentPage < totalPages && totalPages > 1,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = "Siguiente",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

