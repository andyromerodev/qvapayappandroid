package com.example.qvapayappandroid.presentation.ui.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewFullScreen(
    viewModel: WebViewFullScreenViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    initialUrl: String = WebViewFullScreenState.QVAPAY_LOGIN_URL
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialUrl) {
        viewModel.handleIntent(WebViewIntent.ShowWebView(initialUrl))
    }

    // Manejo de effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WebViewEffect.NavigationCompleted -> {
                    // Navigation completed - could show success indicator
                }
                is WebViewEffect.NavigationError -> {
                    // Navigation error - handled by state.error
                }
                is WebViewEffect.WebViewLoaded -> {
                    // WebView loaded completely
                }
                is WebViewEffect.ShowMessage -> {
                    // Show message to user - could implement snackbar
                }
                is WebViewEffect.CloseWebView -> {
                    onBackClick()
                }
                is WebViewEffect.PageStarted -> {
                    // Page started loading
                }
                is WebViewEffect.HttpError -> {
                    // HTTP error occurred
                }
                is WebViewEffect.WebViewUnavailable -> {
                    // WebView not available - handled by state.error
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QvaPay Web") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.handleIntent(WebViewIntent.Reload) }) {
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
    ) { padding ->
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val webView = remember(key1 = "webview_instance_fullscreen") {
                viewModel.getOrCreateWebView(context, initialUrl)
            }

            // Ciclo de vida (resume/pause)
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, e ->
                    when (e) {
                        Lifecycle.Event.ON_RESUME -> webView.onResume()
                        Lifecycle.Event.ON_PAUSE -> webView.onPause()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // Guardar estado al salir de la pantalla
            DisposableEffect(Unit) {
                onDispose { viewModel.saveWebViewState() }
            }

            key("stable_androidview_fullscreen") {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    factory = { webView },
                    update = { /* instancia estable */ }
                )
            }

            // Puedes pintar overlays con state.isLoading / state.error si quieres
        }
    }
}
