package com.example.qvapayappandroid.presentation.ui.p2p

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            title = { Text("Filtros P2P", fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Tipo de oferta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tipo de Oferta",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("all" to "Todas", "buy" to "Compra", "sell" to "Venta").forEach { (type, label) ->
                            FilterChip(
                                onClick = { selectedOfferType = type },
                                label = { Text(label, fontSize = 13.sp) },
                                selected = selectedOfferType == type,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Monedas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "Monedas",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Selecciona las monedas que quieres ver:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle para "Todas las monedas"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Todas las monedas",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal
                        )
                        Switch(
                            checked = selectedCoins.isEmpty(),
                            onCheckedChange = {
                                if (it) selectedCoins = setOf()
                            },
                            modifier = Modifier.scale(0.85f)
                        )
                    }

                    if (selectedCoins.isNotEmpty() || uiState.availableCoins.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        // Toggles para monedas específicas
                        uiState.availableCoins.forEach { coin ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = coin,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Switch(
                                    checked = selectedCoins.contains(coin),
                                    onCheckedChange = { isChecked ->
                                        selectedCoins = if (isChecked) {
                                            selectedCoins + coin
                                        } else {
                                            selectedCoins - coin
                                        }
                                    },
                                    modifier = Modifier.scale(0.85f)
                                )
                            }
                            if (coin != uiState.availableCoins.last()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedOfferType = "all"
                        selectedCoins = setOf()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 7.dp)
                ) {
                    Text("Limpiar", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        onApplyFilters(selectedOfferType, selectedCoins.toList())
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 7.dp)
                ) {
                    Text("Filtrar", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "ℹ️ Información",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "• Si no seleccionas ninguna moneda específica, se mostrarán todas las ofertas disponibles.\n" +
                                "• Los filtros se aplican al pulsar 'Filtrar'.\n" +
                                "• Puedes combinar diferentes tipos de filtros.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
