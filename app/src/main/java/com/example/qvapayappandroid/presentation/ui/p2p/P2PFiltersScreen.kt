package com.example.qvapayappandroid.presentation.ui.p2p

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PFiltersScreen(
    uiState: P2PUiState,
    onBackClick: () -> Unit,
    onApplyFilters: (String, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOfferType by remember { mutableStateOf(uiState.selectedOfferType) }
    var selectedCoins by remember { mutableStateOf(uiState.selectedCoins.toSet()) }
    
    // Actualizar el estado si cambia desde fuera
    LaunchedEffect(uiState.selectedOfferType, uiState.selectedCoins) {
        selectedOfferType = uiState.selectedOfferType
        selectedCoins = uiState.selectedCoins.toSet()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Filtros P2P") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Tipo de oferta
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tipo de Oferta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("all" to "Todas", "buy" to "Compra", "sell" to "Venta").forEach { (type, label) ->
                            FilterChip(
                                onClick = { selectedOfferType = type },
                                label = { Text(label) },
                                selected = selectedOfferType == type,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Monedas
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Monedas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Selecciona las monedas que quieres ver:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Toggle para "Todas las monedas"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Todas las monedas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = selectedCoins.isEmpty(),
                            onCheckedChange = { 
                                if (it) {
                                    selectedCoins = setOf()
                                }
                            }
                        )
                    }
                    
                    if (selectedCoins.isNotEmpty() || uiState.availableCoins.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Toggles para monedas específicas
                        uiState.availableCoins.forEach { coin ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = coin,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = selectedCoins.contains(coin),
                                    onCheckedChange = { isChecked ->
                                        selectedCoins = if (isChecked) {
                                            selectedCoins + coin
                                        } else {
                                            selectedCoins - coin
                                        }
                                    }
                                )
                            }
                            
                            if (coin != uiState.availableCoins.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedOfferType = "all"
                        selectedCoins = setOf()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar Filtros")
                }
                
                Button(
                    onClick = {
                        onApplyFilters(selectedOfferType, selectedCoins.toList())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Filtrar Ofertas")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Información",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Si no seleccionas ninguna moneda específica, se mostrarán todas las ofertas disponibles.\n" +
                               "• Los filtros se aplicarán cuando presiones 'Filtrar Ofertas'.\n" +
                               "• Puedes combinar diferentes tipos de filtros.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}