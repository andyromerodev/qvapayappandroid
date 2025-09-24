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

    // Initialize the WebView with the incoming URL
    LaunchedEffect(offerId) {
        viewModel.handleIntent(P2PWebViewIntent.ShowP2POfferWebView(offerId))
    }

    // Handle one-off effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is P2PWebViewEffect.P2POfferNavigationCompleted -> {
                    // Navigation finished—could trigger a success indicator
                }
                is P2PWebViewEffect.P2POfferNavigationError -> {
                    // Navigation error—state.error already reflects it
                }
                is P2PWebViewEffect.P2PWebViewLoaded -> {
                    // P2P WebView fully loaded
                }
                is P2PWebViewEffect.ShowP2PMessage -> {
                    // Show a user message—snackbar fits nicely here
                }
                is P2PWebViewEffect.CloseP2PWebView -> {
                    onClose()
                }
                is P2PWebViewEffect.P2POfferPageStarted -> {
                    // P2P offer page started loading
                    Log.d("P2PWebView", "Cargando oferta P2P: ${effect.offerId}")
                }
                is P2PWebViewEffect.P2POfferHttpError -> {
                    // HTTP error while loading the offer
                    Log.e("P2PWebView", "Error HTTP ${effect.code} en oferta ${effect.offerId}")
                }
                is P2PWebViewEffect.P2PWebViewUnavailable -> {
                    // WebView unavailable—already handled via state.error
                }
                is P2PWebViewEffect.P2POfferProcessed -> {
                    // Offer processed—reserved for integrations down the road
                }
                is P2PWebViewEffect.P2POfferInteraction -> {
                    // User interaction within the P2P offer
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
                // Handle WebView lifecycle hooks
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

                // Save state when the view is torn down
                DisposableEffect(Unit) {
                    onDispose { viewModel.saveWebViewState() }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    factory = { webView },
                    update = { /* keep the instance stable */ }
                )
            }
        }
    }
}
