package com.example.qvapayappandroid.presentation.ui.alerts.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.qvapayappandroid.domain.model.OfferAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertFormDialog(
    alert: OfferAlert? = null,
    onDismiss: () -> Unit,
    onSave: (OfferAlert) -> Unit
) {
    var name by remember { mutableStateOf(alert?.name ?: "") }
    var coinType by remember { mutableStateOf(alert?.coinType ?: "CUP") }
    var offerType by remember { mutableStateOf(alert?.offerType ?: "both") }
    var minAmount by remember { mutableStateOf(alert?.minAmount?.toString() ?: "") }
    var maxAmount by remember { mutableStateOf(alert?.maxAmount?.toString() ?: "") }
    var targetRate by remember { mutableStateOf(alert?.targetRate?.toString() ?: "") }
    var rateComparison by remember { mutableStateOf(alert?.rateComparison ?: "greater") }
    var onlyKyc by remember { mutableStateOf(alert?.onlyKyc ?: false) }
    var onlyVip by remember { mutableStateOf(alert?.onlyVip ?: false) }
    var checkInterval by remember { mutableStateOf(alert?.checkIntervalMinutes?.toString() ?: "30") }

    var coinDropdownExpanded by remember { mutableStateOf(false) }
    var offerTypeDropdownExpanded by remember { mutableStateOf(false) }
    var comparisonDropdownExpanded by remember { mutableStateOf(false) }

    val coinTypes = listOf(
        "CUP", "USDT", "USDCASH", "CUPCASH", "USDTBSC", "SOL", "ZELLE", 
        "TROPIPAY", "ETECSA", "CLASICA", "BANK_MLC", "NEOMOON", "QVAPAY",
        "BANK_EUR", "EURCASH", "BANDECPREPAGO", "WISE", "BOLSATM", "SBERBANK"
    )

    val offerTypes = listOf(
        "both" to "Todas",
        "buy" to "Comprar",
        "sell" to "Vender"
    )

    val comparisons = listOf(
        "greater" to "Mayor que",
        "less" to "Menor que", 
        "equal" to "Igual a"
    )

    val isEditing = alert != null
    val title = if (isEditing) "Editar Alerta" else "Nueva Alerta"

    val isValid = name.isNotBlank() && 
                  targetRate.isNotBlank() && 
                  checkInterval.isNotBlank() &&
                  targetRate.toDoubleOrNull() != null &&
                  checkInterval.toIntOrNull() != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la alerta") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Coin Type dropdown
                ExposedDropdownMenuBox(
                    expanded = coinDropdownExpanded,
                    onExpandedChange = { coinDropdownExpanded = !coinDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = coinType,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Moneda") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = coinDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = coinDropdownExpanded,
                        onDismissRequest = { coinDropdownExpanded = false }
                    ) {
                        coinTypes.forEach { coin ->
                            DropdownMenuItem(
                                text = { Text(coin) },
                                onClick = {
                                    coinType = coin
                                    coinDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Offer Type dropdown
                ExposedDropdownMenuBox(
                    expanded = offerTypeDropdownExpanded,
                    onExpandedChange = { offerTypeDropdownExpanded = !offerTypeDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = offerTypes.find { it.first == offerType }?.second ?: "Todas",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipo de oferta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = offerTypeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = offerTypeDropdownExpanded,
                        onDismissRequest = { offerTypeDropdownExpanded = false }
                    ) {
                        offerTypes.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    offerType = key
                                    offerTypeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Range
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = minAmount,
                        onValueChange = { minAmount = it },
                        label = { Text("Monto mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxAmount,
                        onValueChange = { maxAmount = it },
                        label = { Text("Monto máximo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Rate and Comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    ExposedDropdownMenuBox(
                        expanded = comparisonDropdownExpanded,
                        onExpandedChange = { comparisonDropdownExpanded = !comparisonDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = comparisons.find { it.first == rateComparison }?.second ?: "Mayor que",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Comparación") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = comparisonDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = comparisonDropdownExpanded,
                            onDismissRequest = { comparisonDropdownExpanded = false }
                        ) {
                            comparisons.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        rateComparison = key
                                        comparisonDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = targetRate,
                        onValueChange = { targetRate = it },
                        label = { Text("Ratio objetivo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Check Interval
                OutlinedTextField(
                    value = checkInterval,
                    onValueChange = { checkInterval = it },
                    label = { Text("Intervalo de verificación (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Checkboxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = onlyKyc,
                        onCheckedChange = { onlyKyc = it }
                    )
                    Text(
                        text = "Solo usuarios KYC",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = onlyVip,
                        onCheckedChange = { onlyVip = it }
                    )
                    Text(
                        text = "Solo usuarios VIP",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val newAlert = OfferAlert(
                                id = alert?.id ?: 0,
                                name = name,
                                coinType = coinType,
                                offerType = offerType,
                                minAmount = minAmount.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
                                maxAmount = maxAmount.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
                                targetRate = targetRate.toDoubleOrNull() ?: 0.0,
                                rateComparison = rateComparison,
                                onlyKyc = onlyKyc,
                                onlyVip = onlyVip,
                                checkIntervalMinutes = checkInterval.toIntOrNull() ?: 30,
                                isActive = alert?.isActive ?: true,
                                createdAt = alert?.createdAt ?: System.currentTimeMillis(),
                                lastCheckedAt = alert?.lastCheckedAt,
                                lastTriggeredAt = alert?.lastTriggeredAt
                            )
                            onSave(newAlert)
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isEditing) "Actualizar" else "Crear")
                    }
                }
            }
        }
    }
}