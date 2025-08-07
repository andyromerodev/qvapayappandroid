package com.example.qvapayappandroid.presentation.ui.login

import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewLoginComponent(
    state: WebViewLoginState,
    onClose: () -> Unit,
    onWebViewReady: (WebView) -> Unit,
    onWebViewUnavailable: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Crear WebView solo una vez
    val webView = remember {
        try {
            Log.d("WebViewLoginComponent", "Creando WebView...")
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        } catch (e: Exception) {
            Log.e("WebViewLoginComponent", "Error creando WebView: ${e.message}")
            onWebViewUnavailable()
            null
        }
    }
    
    // Configurar WebView cuando se crea
    LaunchedEffect(webView) {
        webView?.let { wv ->
            Log.d("WebViewLoginComponent", "WebView creado, configurando...")
            onWebViewReady(wv)
        }
    }
    
    // Cargar URL cuando se hace visible
    LaunchedEffect(state.isVisible, webView) {
        if (state.isVisible && webView != null) {
            val url = "https://qvapay.com/login"
            Log.d("WebViewLoginComponent", "Cargando URL: $url")
            webView.loadUrl(url)
        }
    }
    
    if (state.isVisible) {
        // WebView visible para el usuario (Cloudflare)
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con botón de cerrar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Completar verificación",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (state.isLoading) {
                            Text(
                                text = "Cargando...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        state.error?.let { error ->
                            Text(
                                text = "Error: $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Loading indicator
                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // WebView para login con Cloudflare
            if (webView != null) {
                AndroidView(
                    factory = { webView },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                // WebView no disponible - mostrar mensaje de error
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "WebView no disponible",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "El navegador web no está disponible en este dispositivo. Usa el login normal con API.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
    // No renderizar nada cuando no es visible
}