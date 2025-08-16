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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveOfferTemplateScreen(
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: SaveOfferTemplateViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SaveOfferTemplateEffect.NavigateBack -> onBackClick()
                is SaveOfferTemplateEffect.ShowSuccess -> {
                    // Mensaje de éxito manejado por el estado
                }
                is SaveOfferTemplateEffect.ShowError -> {
                    // Error manejado por el estado
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información básica de la plantilla
            Card(
                modifier = Modifier.fillMaxWidth()
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

            // Tipo de oferta
            Card(
                modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeType("buy")) },
                            label = { Text("Comprar") },
                            selected = uiState.type == "buy",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Datos básicos
            Card(
                modifier = Modifier.fillMaxWidth()
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
                    
                    // Selector de Moneda
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

            // Detalles
            Card(
                modifier = Modifier.fillMaxWidth()
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

            // Configuraciones
            Card(
                modifier = Modifier.fillMaxWidth()
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
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeOnlyKyc(it)) }
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
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangePrivate(it)) }
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
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangePromoteOffer(it)) }
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
                            onCheckedChange = { viewModel.handleIntent(SaveOfferTemplateIntent.ChangeOnlyVip(it)) }
                        )
                    }
                }
            }

            // Mensaje y webhook
            Card(
                modifier = Modifier.fillMaxWidth()
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
                            onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.DismissError) }
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "¡Éxito!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.successMessage!!,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.DismissSuccessMessage) }
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }

            // Botón guardar
            Button(
                onClick = { viewModel.handleIntent(SaveOfferTemplateIntent.SaveTemplate) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.isValid
            ) {
                if (uiState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.isEditing) "Actualizando..." else "Guardando...")
                    }
                } else {
                    Text(if (uiState.isEditing) "Actualizar Plantilla" else "Guardar Plantilla")
                }
            }

            // Espaciado al final
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}