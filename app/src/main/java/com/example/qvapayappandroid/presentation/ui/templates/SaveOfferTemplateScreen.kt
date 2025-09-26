package com.example.qvapayappandroid.presentation.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.colorResource
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import com.example.qvapayappandroid.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveOfferTemplateScreen(
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: SaveOfferTemplateViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle one-off view effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SaveOfferTemplateEffect.NavigateBack -> onBackClick()
                is SaveOfferTemplateEffect.ShowSuccess -> {
                    // Success message is handled via state
                }
                is SaveOfferTemplateEffect.ShowError -> {
                    // Error details come from state
                }
                is SaveOfferTemplateEffect.TemplateSavedSuccessfully -> {
                    onSuccess()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (uiState.isEditing) "Editar Plantilla" else "Nueva Plantilla") 
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.qvapay_surface_light),
                    scrolledContainerColor = colorResource(id = R.color.qvapay_surface_light)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(id = R.color.qvapay_background_primary))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Core template information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información de la Plantilla",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeName(it)) },
                        label = { Text("Nombre de la plantilla *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.name.isBlank() && uiState.errorMessage != null
                    )
                    
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeDescription(it)) },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }

            // Offer type
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tipo de Oferta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeType("sell")) },
                            label = { Text("Vender") },
                            selected = uiState.type == "sell",
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colorResource(id = R.color.qvapay_surface_medium),
                                labelColor = colorResource(id = R.color.qvapay_purple_text),
                                selectedContainerColor = colorResource(id = R.color.qvapay_purple_primary),
                                selectedLabelColor = colorResource(id = R.color.white)
                            )
                        )
                        FilterChip(
                            onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeType("buy")) },
                            label = { Text("Comprar") },
                            selected = uiState.type == "buy",
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

            // Primary offer data
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Datos de la Oferta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Coin selector
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${uiState.selectedCoin.name} (${uiState.selectedCoin.tick})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Moneda") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            uiState.availableCoins.forEach { coin ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(
                                                text = coin.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.handleIntent(SaveOfferTemplateIntent.SelectCoin(coin))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeAmount(it)) },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    OutlinedTextField(
                        value = uiState.receive,
                        onValueChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeReceive(it)) },
                        label = { Text("Monto a Recibir") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            // Contact details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Detalles de Contacto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    uiState.details.forEachIndexed { index, detail ->
                        OutlinedTextField(
                            value = detail.value,
                            onValueChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeDetail(index, it)) },
                            label = { Text(detail.name) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Flags and switches
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Configuraciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Solo usuarios KYC")
                        Switch(
                            checked = uiState.onlyKyc,
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeOnlyKyc(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorResource(id = R.color.white),
                                checkedTrackColor = colorResource(id = R.color.qvapay_purple_primary),
                                uncheckedThumbColor = colorResource(id = R.color.qvapay_purple_text),
                                uncheckedTrackColor = colorResource(id = R.color.qvapay_surface_medium)
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Oferta privada")
                        Switch(
                            checked = uiState.private,
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangePrivate(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorResource(id = R.color.white),
                                checkedTrackColor = colorResource(id = R.color.qvapay_purple_primary),
                                uncheckedThumbColor = colorResource(id = R.color.qvapay_purple_text),
                                uncheckedTrackColor = colorResource(id = R.color.qvapay_surface_medium)
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Promocionar oferta")
                        Switch(
                            checked = uiState.promoteOffer,
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangePromoteOffer(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorResource(id = R.color.white),
                                checkedTrackColor = colorResource(id = R.color.qvapay_purple_primary),
                                uncheckedThumbColor = colorResource(id = R.color.qvapay_purple_text),
                                uncheckedTrackColor = colorResource(id = R.color.qvapay_surface_medium)
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Solo usuarios VIP")
                        Switch(
                            checked = uiState.onlyVip,
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeOnlyVip(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorResource(id = R.color.white),
                                checkedTrackColor = colorResource(id = R.color.qvapay_purple_primary),
                                uncheckedThumbColor = colorResource(id = R.color.qvapay_purple_text),
                                uncheckedTrackColor = colorResource(id = R.color.qvapay_surface_medium)
                            )
                        )
                    }
                }
            }

            // Message and webhook
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
                border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información Adicional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = uiState.message,
                        onValueChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeMessage(it)) },
                        label = { Text("Mensaje") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    OutlinedTextField(
                        value = uiState.webhook,
                        onValueChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeWebhook(it)) },
                        label = { Text("Webhook (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Error message
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.DismissError) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorResource(id = R.color.qvapay_purple_primary)
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }

            // Success message
            if (uiState.successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.qvapay_purple_light)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "¡Éxito!",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorResource(id = R.color.qvapay_purple_text),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.successMessage!!,
                            color = colorResource(id = R.color.qvapay_purple_text)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.DismissSuccessMessage) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorResource(id = R.color.qvapay_purple_primary)
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }

            // Save button
            Button(
                onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.SaveTemplate) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.qvapay_purple_primary),
                    contentColor = colorResource(id = R.color.white)
                )
            ) {
                if (uiState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colorResource(id = R.color.white)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.isEditing) "Actualizando..." else "Guardando...")
                    }
                } else {
                    Text(if (uiState.isEditing) "Actualizar Plantilla" else "Guardar Plantilla")
                }
            }

            // Bottom spacer
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
