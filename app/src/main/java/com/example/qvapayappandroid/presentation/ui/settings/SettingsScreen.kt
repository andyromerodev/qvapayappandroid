package com.example.qvapayappandroid.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.qvapayappandroid.BuildConfig
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qvapayappandroid.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Observe navigation-related effects
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.NavigateToLogin -> onLogout()
                is SettingsEffect.ShowMessage -> {
                    // Handle showing messages/toasts
                }
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.qvapay_surface_light),
                    scrolledContainerColor = colorResource(id = R.color.qvapay_surface_light)
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.qvapay_surface_light))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando configuración...")
                    }
                }
            }
            
            else -> {
                SettingsContent(
                    modifier = Modifier.padding(16.dp),
                    userSettings = uiState.userSettings,
                    onNotificationsToggle = { viewModel.toggleNotifications(it) },
                    onBiometricToggle = { viewModel.toggleBiometric(it) },
                    onThemeChange = { viewModel.changeTheme(it) },
                    onLanguageChange = { viewModel.changeLanguage(it) },
                    onProfileClick = onProfileClick,
                    onAlertsClick = onAlertsClick,
                    onChangePassword = { viewModel.changePassword() },
                    onPrivacySettings = { viewModel.privacySettings() },
                    onAbout = { viewModel.showAbout() },
                    onLogout = { viewModel.logout() },
                    isLoggingOut = uiState.isLoggingOut
                )
            }
        }
        
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
        }

        // About dialog
        if (uiState.showAboutDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissAbout() },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissAbout() }) {
                        Text("Cerrar")
                    }
                },
                title = { Text("Acerca de") },
                text = {
                    Column {
                        Text("QvaPay Android (no oficial)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Versión: v${BuildConfig.VERSION_NAME}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Desarrollado por: Andy Luis Hernandez Romero")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Licencia: Apache-2.0")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Canal de Telegram: https://t.me/qvapayandroid")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sitio del proyecto: https://andyromerodev.github.io/qvapayappandroid")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Este proyecto no está afiliado a QvaPay. ‘QvaPay’ es marca de sus respectivos titulares.")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    userSettings: UserSettings,
    onNotificationsToggle: (Boolean) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onChangePassword: () -> Unit,
    onPrivacySettings: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit,
    isLoggingOut: Boolean
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Account Section
        SettingsSection(title = "Cuenta") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Mi Perfil",
                subtitle = "Ver y editar información personal",
                onClick = onProfileClick
            )
            
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Cambiar Contraseña",
                subtitle = "Actualiza tu contraseña de acceso",
                onClick = onChangePassword
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Preferences Section
        SettingsSection(title = "Preferencias") {
            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                subtitle = "Recibir notificaciones push",
                checked = userSettings.notificationsEnabled,
                onCheckedChange = onNotificationsToggle
            )
            
            SettingsSwitchItem(
                icon = Icons.Default.CheckCircle,
                title = "Autenticación Biométrica",
                subtitle = "Usar huella dactilar o Face ID",
                checked = userSettings.biometricEnabled,
                onCheckedChange = onBiometricToggle
            )
            
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Tema",
                subtitle = "Claro, Oscuro o Sistema",
                trailing = {
                    Text(
                        text = userSettings.theme,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { showThemeDialog = true }
            )
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Idioma",
                subtitle = "Seleccionar idioma de la app",
                trailing = {
                    Text(
                        text = userSettings.language,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { onLanguageChange("Español") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // P2P Section
        SettingsSection(title = "P2P") {
            SettingsItem(
                icon = Icons.Default.NotificationImportant,
                title = "Alertas de Ofertas",
                subtitle = "Configurar notificaciones para ofertas P2P",
                onClick = onAlertsClick
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Privacy & Security Section
        SettingsSection(title = "Privacidad y Seguridad") {
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Configuración de Privacidad",
                subtitle = "Gestiona tu privacidad y datos",
                onClick = onPrivacySettings
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Support Section
        SettingsSection(title = "Soporte") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Acerca de",
                subtitle = "Versión de la app e información",
                onClick = onAbout
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logout Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            onClick = onLogout
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = if (isLoggingOut) "Cerrando sesión..." else "Cerrar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
    
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = userSettings.theme,
            onThemeSelected = { theme ->
                onThemeChange(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.qvapay_surface_light)),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.qvapay_purple_primary)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.qvapay_surface_medium)
        ),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorResource(id = R.color.qvapay_purple_primary)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            trailing?.invoke()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.qvapay_surface_medium)
        ),
        border = BorderStroke(1.dp, colorResource(id = R.color.qvapay_purple_light))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorResource(id = R.color.qvapay_purple_primary)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
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

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themeOptions = listOf("Claro", "Oscuro", "Sistema")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Tema",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                themeOptions.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(id = R.color.qvapay_purple_primary),
                                unselectedColor = colorResource(id = R.color.qvapay_purple_text)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = theme,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.qvapay_purple_primary)
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}
