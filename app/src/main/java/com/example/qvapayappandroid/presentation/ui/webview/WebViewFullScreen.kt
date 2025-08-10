package com.example.qvapayappandroid.presentation.ui.webview

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewFullScreen(
    viewModel: WebViewViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    initialUrl: String = "https://qvapay.com"
) {
    val state by viewModel.state.collectAsState()

    // Inicializa el intent de mostrar la WebView (no fuerza recreación)
    LaunchedEffect(Unit) {
        android.util.Log.d("WebViewFullScreen", "🚀 Inicializando con URL: $initialUrl")
        viewModel.showWebView(initialUrl)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QvaPay Web",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Instancia ÚNICA desde el ViewModel (no se destruye al cambiar de pestaña)
            val webView = remember { viewModel.getOrCreateWebView(context, initialUrl) }

            // Atar al ciclo de vida para pausar/reanudar (sin destruir)
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

            // Guardar estado al desmontar esta pantalla (p. ej., navegar fuera del flujo)
            DisposableEffect(Unit) {
                onDispose {
                    // No destruir: solo guardar el estado para restaurar al volver
                    viewModel.saveWebViewState()
                }
            }

            // === Render: WebView SIEMPRE montado ===
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { _ -> webView }
            )

            // === Shimmer overlay: fade-out cuando el primer frame es visible ===
//            val targetAlpha = if (state.isLoading) 1f else 0f
//            val alpha by animateFloatAsState(targetValue = targetAlpha, label = "shimmerAlpha")
//
//            if (alpha > 0f) {
//                Box(
//                    modifier = Modifier
//                        .matchParentSize()
//                        .alpha(alpha)
//                ) {
//                    WebViewShimmer(
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//            }

            // === Overlay de error (si existe) ===
            state.error?.let { message ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = {
                                viewModel.clearError()
                                viewModel.reload()
                            }) { Text("Reintentar") }
                        }
                    }
                }
            }
        }
    }
}
