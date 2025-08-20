package com.example.qvapayappandroid.presentation.ui.alerts.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qvapayappandroid.data.permissions.NotificationPermissionManager

@Composable
    fun NotificationPermissionCard(
    permissionStatus: NotificationPermissionManager.NotificationPermissionStatus,
    onPermissionChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Launcher para solicitar permisos de notificaciones (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionChanged()
    }
    
    // Launcher para abrir configuración de la app
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onPermissionChanged()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (permissionStatus.isFullyEnabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (permissionStatus.isFullyEnabled) {
                        Icons.Default.Notifications
                    } else {
                        Icons.Default.NotificationsOff
                    },
                    contentDescription = null,
                    tint = if (permissionStatus.isFullyEnabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (permissionStatus.isFullyEnabled) {
                            "✅ Notificaciones Activadas"
                        } else {
                            "⚠️ Notificaciones Desactivadas"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (permissionStatus.isFullyEnabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    
                    Text(
                        text = if (permissionStatus.isFullyEnabled) {
                            "Las alertas de ofertas funcionarán correctamente"
                        } else {
                            "Debes activar las notificaciones para recibir alertas"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (permissionStatus.isFullyEnabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        }
                    )
                }
            }

            // Estado detallado (solo mostrar si no está completamente habilitado)
            if (!permissionStatus.isFullyEnabled) {
                Divider(
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Estado de permisos:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    PermissionStatusRow(
                        label = "Permiso de notificaciones",
                        isEnabled = permissionStatus.isGranted,
                        showForAndroid13Plus = NotificationPermissionManager.shouldRequestPermission(context)
                    )
                    
                    PermissionStatusRow(
                        label = "Notificaciones de la app",
                        isEnabled = permissionStatus.canShowNotifications,
                        showForAndroid13Plus = false
                    )
                    
                    PermissionStatusRow(
                        label = "Canal de alertas",
                        isEnabled = permissionStatus.isChannelEnabled,
                        showForAndroid13Plus = false
                    )
                }

                // Botones de acción
                Divider(
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón para solicitar permiso (Android 13+)
                    if (!permissionStatus.isGranted && NotificationPermissionManager.shouldRequestPermission(context)) {
                        Button(
                            onClick = {
                                val permissionManager = NotificationPermissionManager(context)
                                val permission = permissionManager.getRequiredPermission()
                                permission?.let { permissionLauncher.launch(it) }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Permitir")
                        }
                    }
                    
                    // Botón para abrir configuración
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            settingsLauncher.launch(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configurar")
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    showForAndroid13Plus: Boolean = false
) {
    if (showForAndroid13Plus && !NotificationPermissionManager.shouldRequestPermission(LocalContext.current)) {
        return
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $label",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.Notifications else Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Text(
                text = if (isEnabled) "Activo" else "Inactivo",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}