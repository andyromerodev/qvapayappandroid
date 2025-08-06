package com.example.qvapayappandroid.presentation.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class StatusFilter(
    val key: String,
    val label: String,
    val isSelected: Boolean = false
)

@Composable
fun StatusFilterChips(
    selectedStatuses: Set<String>,
    onStatusToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusFilters = listOf(
        StatusFilter("todas", "TODAS", selectedStatuses.isEmpty()),
        StatusFilter("activa", "ACTIVA", selectedStatuses.contains("activa")),
        StatusFilter("completada", "COMPLETADA", selectedStatuses.contains("completada")),
        StatusFilter("cancelada", "CANCELADA", selectedStatuses.contains("cancelada")),
        StatusFilter("pausada", "PAUSADA", selectedStatuses.contains("pausada")),
        StatusFilter("pendiente", "PENDIENTE", selectedStatuses.contains("pendiente"))
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statusFilters) { filter ->
            FilterChip(
                selected = filter.isSelected,
                onClick = { 
                    if (filter.key == "todas") {
                        // Si se selecciona "TODAS", limpiar todos los filtros
                        onStatusToggle("")
                    } else {
                        onStatusToggle(filter.key)
                    }
                },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}