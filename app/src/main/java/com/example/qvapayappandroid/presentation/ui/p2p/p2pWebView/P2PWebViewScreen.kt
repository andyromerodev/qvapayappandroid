package com.example.qvapayappandroid.presentation.ui.p2p.p2pWebView

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
    
    val customUrl = "${P2PWebViewState.QVAPAY_P2P_BASE_URL}/$offerId"

    // Inicializar WebView con URL
    LaunchedEffect(offerId) {
        viewModel.handleIntent(P2PWebViewIntent.ShowP2POfferWebView(offerId))
    }

    // Manejo de effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is P2PWebViewEffect.P2POfferNavigationCompleted -> {
                    // Navegación completada - podrías mostrar indicador de éxito
                }
                is P2PWebViewEffect.P2POfferNavigationError -> {
                    // Error de navegación - manejado por state.error
                }
                is P2PWebViewEffect.P2PWebViewLoaded -> {
                    // P2P WebView cargado completamente
                }
                is P2PWebViewEffect.ShowP2PMessage -> {
                    // Mostrar mensaje al usuario - podrías implementar snackbar
                }
                is P2PWebViewEffect.CloseP2PWebView -> {
                    onClose()
                }
                is P2PWebViewEffect.P2POfferPageStarted -> {
                    // Página de oferta P2P comenzó a cargar
                    Log.d("P2PWebView", "Cargando oferta P2P: ${effect.offerId}")
                }
                is P2PWebViewEffect.P2POfferHttpError -> {
                    // Error HTTP en oferta P2P
                    Log.e("P2PWebView", "Error HTTP ${effect.code} en oferta ${effect.offerId}")
                }
                is P2PWebViewEffect.P2PWebViewUnavailable -> {
                    // WebView no disponible - manejado por state.error
                }
                is P2PWebViewEffect.P2POfferProcessed -> {
                    // Oferta P2P procesada - para futuras integraciones
                }
                is P2PWebViewEffect.P2POfferInteraction -> {
                    // Interacción del usuario con la oferta P2P
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.offerId != null) {
                            "Oferta P2P - ${state.offerId}"
                        } else {
                            "Oferta P2P"
                        },
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
                SideEffect { viewModel.handleIntent(P2PWebViewIntent.OnWebViewUnavailable) }
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