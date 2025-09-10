package com.example.qvapayappandroid.presentation.ui.webview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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

            // Floating Action Button expandible
            var isFabExpanded by remember { mutableStateOf(false) }
            
            val fabRotation by animateFloatAsState(
                targetValue = if (isFabExpanded) 45f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "fab_rotation"
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // FAB secundarios (Login y Dashboard)
                if (isFabExpanded) {
                    SmallFloatingActionButton(
                        onClick = {
                            viewModel.handleIntent(WebViewIntent.ShowWebView(WebViewFullScreenState.QVAPAY_DASHBOARD_URL))
                            isFabExpanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.offset(y = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    }

                    SmallFloatingActionButton(
                        onClick = {
                            viewModel.handleIntent(WebViewIntent.ShowWebView(WebViewFullScreenState.QVAPAY_LOGIN_URL))
                            isFabExpanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Login,
                            contentDescription = "Login"
                        )
                    }
                }

                // FAB principal
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Opciones",
                        modifier = Modifier.rotate(fabRotation)
                    )
                }
            }
        }
    }
}
