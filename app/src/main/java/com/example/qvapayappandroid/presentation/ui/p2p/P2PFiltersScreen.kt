package com.example.qvapayappandroid.presentation.ui.p2p

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.foundation.background
import com.example.qvapayappandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PFiltersScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onApplyFilters: (String, List<String>) -> Unit,
    selectedOfferType: String,
    selectedCoins: List<String>,
    availableCoins: List<String>
) {
    var localSelectedOfferType by remember { mutableStateOf(selectedOfferType) }
    var localSelectedCoins by remember { mutableStateOf(selectedCoins.toSet()) }

    LaunchedEffect(selectedOfferType, selectedCoins) {
        localSelectedOfferType = selectedOfferType
        localSelectedCoins = selectedCoins.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filtros P2P", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.qvapay_surface_light),
                    scrolledContainerColor = colorResource(id = R.color.qvapay_surface_light)
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },

        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                color = colorResource(id = R.color.qvapay_surface_light)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            localSelectedOfferType = "all"
                            localSelectedCoins = setOf()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorResource(id = R.color.qvapay_purple_primary)
                        ),
                        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_primary))
                    ) { Text("Limpiar", fontSize = 13.sp) }

                    Button(
                        onClick = {
                            onApplyFilters(localSelectedOfferType, localSelectedCoins.toList())
                            onBackClick()
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.qvapay_purple_primary),
                            contentColor = colorResource(id = R.color.white)
                        )
                    ) { Text("Filtrar", fontSize = 13.sp) }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.qvapay_surface_light))
                .padding(
                    start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                    top   = paddingValues.calculateTopPadding(),
                    end   = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
                )
                .padding(8.dp)
        ) {
            // Offer type
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("all" to "Todas", "buy" to "Compra", "sell" to "Venta").forEach { (type, label) ->
                            FilterChip(
                                onClick = { localSelectedOfferType = type },
                                label = { Text(label, fontSize = 13.sp) },
                                selected = localSelectedOfferType == type,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colorResource(id = R.color.qvapay_surface_medium),
                                    labelColor = colorResource(id = R.color.qvapay_purple_text),
                                    selectedContainerColor = colorResource(id = R.color.qvapay_purple_primary),
                                    selectedLabelColor = colorResource(id = R.color.white)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Coins
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),                 // ⬅️ ocupa todo el espacio libre
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()           // ⬅️ la Card realmente se estira
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Monedas",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Toggle for "All coins"—mutually exclusive
                    CoinToggleRow(
                        text = "Todas las monedas",
                        isChecked = localSelectedCoins.isEmpty(),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                localSelectedCoins = setOf()
                            }
                        }
                    )

                    if (availableCoins.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 3.dp))

                        // Grid of toggles for specific coins—mutually exclusive

                        Box(Modifier.weight(1f, fill = true)) {
                            CoinSelectionGrid(
                                availableCoins = availableCoins,
                                selectedCoins = localSelectedCoins,
                                onCoinSelected = { selectedCoin ->
                                    localSelectedCoins = if (selectedCoin.isEmpty()) {
                                        // If a coin is deselected, fall back to "All coins"
                                        setOf()
                                    } else {
                                        // Only allow one selected coin at a time
                                        setOf(selectedCoin)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(64.dp))
        }
    }
}

// SOLID - Single Responsibility: Component for individual coin toggle
@Composable
private fun CoinToggleRow(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val rowHeight = if (compact) 24.dp else 32.dp
    val switchScale = if (compact) 0.58f else 0.7f
    val textStyle = if (compact) MaterialTheme.typography.labelSmall
    else MaterialTheme.typography.bodySmall

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = rowHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = textStyle, maxLines = 1)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .scale(switchScale)
                .minimumInteractiveComponentSize(), // Uses the locally provided minimum (24.dp or 0.dp)
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.white),
                checkedTrackColor = colorResource(id = R.color.qvapay_purple_primary),
                uncheckedThumbColor = colorResource(id = R.color.qvapay_purple_text),
                uncheckedTrackColor = colorResource(id = R.color.qvapay_surface_medium)
            )
        )
    }
}

// SOLID - Single Responsibility: Component for coin selection grid with mutual exclusion
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoinSelectionGrid(
    availableCoins: List<String>,
    selectedCoins: Set<String>,
    onCoinSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1) Allow compact components
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 24.dp) {
        // or 0.dp if you want to disable the minimum entirely
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(availableCoins) { coin ->
                CoinToggleRow(
                    text = coin,
                    isChecked = selectedCoins.contains(coin),
                    onCheckedChange = { checked ->
                        onCoinSelected(if (checked) coin else "")
                    },
                    compact = true
                )
            }
        }
    }
}
