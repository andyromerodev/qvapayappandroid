package com.example.qvapayappandroid.presentation.ui.p2p.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qvapayappandroid.presentation.ui.p2p.P2PUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PFiltersCard(
    uiState: P2PUiState,
    onOfferTypeChanged: (String) -> Unit,
    onCoinChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all" to "Todas", "buy" to "Compra", "sell" to "Venta").forEach { (type, label) ->
                    FilterChip(
                        onClick = { onOfferTypeChanged(type) },
                        label = { Text(label) },
                        selected = uiState.selectedOfferType == type,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    value = if (uiState.selectedCoin == "all") "Todas las monedas" else uiState.selectedCoin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Moneda") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las monedas") },
                        onClick = {
                            onCoinChanged("all")
                            expanded = false
                        }
                    )
                    uiState.availableCoins.forEach { coin ->
                        DropdownMenuItem(
                            text = { Text(coin) },
                            onClick = {
                                onCoinChanged(coin)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}