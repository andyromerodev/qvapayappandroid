package com.example.qvapayappandroid.presentation.ui.p2p

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PWebViewScreen(
    offerId: String,
    onClose: () -> Unit,
    viewModel: P2PWebViewViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()
    
    val customUrl = "https://qvapay.com/p2p/$offerId"

    // Inicializar WebView con URL
    LaunchedEffect(offerId) {
        viewModel.showP2POfferWebView(offerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.url.takeIf { it.isNotBlank() } ?: "Oferta P2P",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val initialUrl = customUrl
            val webView = remember(key1 = "webview_instance_p2p") {
                try {
                    viewModel.getOrCreateWebView(context, initialUrl)
                } catch (e: Exception) {
                    Log.e("P2PWebViewScreen", "Error creando WebView: ${e.message}", e)
                    null
                }
            }

            if (webView == null) {
                SideEffect { viewModel.onWebViewUnavailable() }
            } else {
                // Ciclo de vida del WebView
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> webView.onResume()
                            Lifecycle.Event.ON_PAUSE -> webView.onPause()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Guardar estado al desmontar
                DisposableEffect(Unit) {
                    onDispose { viewModel.saveWebViewState() }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    factory = { webView },
                    update = { /* instancia estable */ }
                )
            }
        }
    }
}