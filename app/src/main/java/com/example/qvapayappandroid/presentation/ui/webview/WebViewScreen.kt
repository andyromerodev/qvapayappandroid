package com.example.qvapayappandroid.presentation.ui.webview

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.qvapayappandroid.data.model.P2POffer
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    viewModel: WebViewViewModel = koinViewModel(),
    onClose: () -> Unit = {},
    customUrl: String? = null,
    showConfirmDialog: Boolean = false, // se ignora: gestionamos el diálogo internamente
    offer: P2POffer? = null,
    onAcceptOffer: () -> Unit = {},
    onCancelOffer: () -> Unit = {},
    onOfferApplied: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Snackbar simple
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarIsError by remember { mutableStateOf(false) }

    // Diálogo de confirmación: aparece al terminar de cargar
    var showOfferDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Pide al VM la URL objetivo
    LaunchedEffect(customUrl) {
        val url = customUrl ?: WebViewScreenState.QVAPAY_LOGIN_URL
        viewModel.showWebView(url)
    }

    // Cuando termina de cargar (isLoading pasa a false), mostramos el diálogo
    LaunchedEffect(state.isLoading, state.error) {
        if (!state.isLoading && state.error == null) {
            showOfferDialog = true
        } else {
            showOfferDialog = false
        }
    }

    // Autocierre del snackbar
    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            kotlinx.coroutines.delay(4000)
            showSnackbar = false
        }
    }

    // Acción de ACEPTAR (reutiliza tu helper existente)
    val acceptAction = rememberAcceptAction(
        viewModel = viewModel,
        offer = offer,
        customUrl = customUrl,
        onAcceptOffer = onAcceptOffer,
        onOfferApplied = onOfferApplied,
        onClose = onClose,
        setShow = { showSnackbar = it },
        setMsg = { snackbarMessage = it },
        setErr = { snackbarIsError = it }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val initialUrl = customUrl ?: WebViewScreenState.QVAPAY_LOGIN_URL
        val webView = remember(key1 = "webview_instance_screen") {
            try {
                viewModel.getOrCreateWebView(context, initialUrl)
            } catch (e: Exception) {
                Log.e("WebViewScreen", "Error creando WebView: ${e.message}", e)
                null
            }
        }

        if (webView == null) {
            // WebView no disponible
            SideEffect { viewModel.onWebViewUnavailable() }
        } else {
            // Ciclo de vida
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
            // Guardar estado al desmontar
            DisposableEffect(Unit) {
                onDispose { viewModel.saveWebViewState() }
            }
            // Render WebView
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                factory = { webView },
                update = { /* instancia estable */ }
            )
        }

        // === Overlay de "CARGANDO OFERTA" mientras el VM está cargando ===
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CARGANDO OFERTA",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // === Diálogo de Confirmación (aparece SOLO cuando la página real terminó de cargar) ===
        if (showOfferDialog) {
            AlertDialog(
                onDismissRequest = {
                    showOfferDialog = false
                    onCancelOffer()
                },
                title = { Text("¿Aplicar a esta oferta?") },
                text = {
                    Text(
                        text = "Serás redirigido y se intentará aplicar a la oferta en la página cargada.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        showOfferDialog = false
                        acceptAction() // Ejecuta el click de "Aplicar" vía JS
                    }) {
                        Text("ACEPTAR")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showOfferDialog = false
                        onCancelOffer()
                    }) {
                        Text("CANCELAR")
                    }
                }
            )
        }

        // Overlay de error (opcional)
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
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
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

        // Snackbar flotante (opcional)
        if (showSnackbar) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (snackbarIsError)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (snackbarIsError) "❌" else "✅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = snackbarMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (snackbarIsError)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberAcceptAction(
    viewModel: WebViewViewModel,
    offer: P2POffer?,
    customUrl: String?,
    onAcceptOffer: () -> Unit,
    onOfferApplied: () -> Unit,
    onClose: () -> Unit,
    setShow: (Boolean) -> Unit,
    setMsg: (String) -> Unit,
    setErr: (Boolean) -> Unit
): () -> Unit = remember(viewModel, offer, customUrl) {
    {
        val offerId = offer?.uuid ?: customUrl?.substringAfterLast("/") ?: ""
        Log.d("WebViewScreen", "🎯 onAcceptOffer - offerId: '$offerId'")
        if (offerId.isNotEmpty()) {
            viewModel.executeButtonClick(offerId) { success, message ->
                if (success) {
                    setMsg(message ?: "Oferta aplicada exitosamente")
                    setErr(false)
                    setShow(true)
                    viewModel.hideWebView()
                    onAcceptOffer()
                    onOfferApplied()
                    onClose()
                } else {
                    setMsg(message ?: "Error aplicando a la oferta")
                    setErr(true)
                    setShow(true)
                }
            }
        } else {
            Log.w("WebViewScreen", "⚠️ offerId vacío; no se puede aplicar a la oferta")
            setMsg("Error: ID de oferta no válido")
            setErr(true)
            setShow(true)
        }
    }
}
