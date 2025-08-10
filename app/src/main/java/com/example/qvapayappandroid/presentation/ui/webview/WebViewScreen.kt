package com.example.qvapayappandroid.presentation.ui.webview

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.example.qvapayappandroid.data.model.P2POffer
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    viewModel: WebViewViewModel = koinViewModel(),
    onClose: () -> Unit = {},
    customUrl: String? = null,
    showConfirmDialog: Boolean = false,
    offer: P2POffer? = null,
    onAcceptOffer: () -> Unit = {},
    onCancelOffer: () -> Unit = {},
    onOfferApplied: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    // Estado para el snackbar
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarIsError by remember { mutableStateOf(false) }
    
    // Estado para el dialog de error de WebView
    var showWebViewErrorDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Inicializar el WebView cuando se monta el composable
    LaunchedEffect(customUrl) {
        val url = customUrl ?: WebViewScreenState.QVAPAY_LOGIN_URL
        viewModel.showWebView(url)
    }
    
    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            kotlinx.coroutines.delay(4000)
            showSnackbar = false
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        WebViewContent(
            state = state,
            onClose = {
                viewModel.hideWebView()
                onClose()
            },
            onWebViewReady = viewModel::onWebViewReady,
            onWebViewUnavailable = {
                viewModel.onWebViewUnavailable()
                showWebViewErrorDialog = true
            },
            customUrl = customUrl,
            showConfirmDialog = showConfirmDialog,
            offer = offer,
            onAcceptOffer = {
                // Extraer el UUID de la oferta de la URL personalizada
                val offerId = offer?.uuid ?: customUrl?.substringAfterLast("/") ?: ""
                Log.d("WebViewScreen", "🎯 onAcceptOffer - offerId extraído: '$offerId'")
                Log.d("WebViewScreen", "🔍 offer?.uuid: '${offer?.uuid}', customUrl: '$customUrl'")
                if (offerId.isNotEmpty()) {
                    Log.d("WebViewScreen", "✅ Llamando a viewModel.executeButtonClick($offerId)")
                    viewModel.executeButtonClick(offerId) { success, message ->
                        if (success) {
                            // Éxito: cerrar WebView y notificar
                            snackbarMessage = message ?: "Oferta aplicada exitosamente"
                            snackbarIsError = false
                            showSnackbar = true
                            
                            // Cerrar WebView
                            viewModel.hideWebView()
                            onAcceptOffer()
                            onOfferApplied()
                            onClose()
                        } else {
                            // Error: mostrar snackbar pero no cerrar dialog
                            snackbarMessage = message ?: "Error aplicando a la oferta"
                            snackbarIsError = true
                            showSnackbar = true
                        }
                    }
                } else {
                    Log.w("WebViewScreen", "⚠️ offerId está vacío, no se puede aplicar a la oferta")
                    snackbarMessage = "Error: ID de oferta no válido"
                    snackbarIsError = true
                    showSnackbar = true
                }
            },
            onCancelOffer = onCancelOffer
        )
        
        // Snackbar flotante
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
                    Text(
                        text = if (snackbarIsError) "❌" else "✅",
                        fontSize = 20.sp
                    )
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
        
        // Dialog de error de WebView
        if (showWebViewErrorDialog) {
            WebViewErrorDialog(
                onDismiss = {
                    showWebViewErrorDialog = false
                    onClose()
                },
                onRestartApp = {
                    // Reiniciar la aplicación
                    val packageManager = context.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    if (context is android.app.Activity) {
                        context.finish()
                    }
                    Runtime.getRuntime().exit(0)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewContent(
    state: WebViewScreenState,
    onClose: () -> Unit,
    onWebViewReady: (WebView) -> Unit,
    onWebViewUnavailable: () -> Unit = {},
    customUrl: String? = null,
    showConfirmDialog: Boolean = false,
    offer: P2POffer? = null,
    onAcceptOffer: () -> Unit = {},
    onCancelOffer: () -> Unit = {}
) {
    val context = LocalContext.current

    // Evitar crear nuevas instancias de WebView que causan el error de inicialización múltiple
    var webView by remember { mutableStateOf<WebView?>(null) }
    var creationError by remember { mutableStateOf<String?>(null) }

    // Intentar crear WebView solo como último recurso y con manejo seguro
    LaunchedEffect(state.isVisible) {
        if (state.isVisible && webView == null) {
            Log.d("WebViewLoginComponent", "Intentando crear WebView de forma segura...")
            try {
                webView = createWebViewSafely(context)
                if (webView != null) {
                    Log.d("WebViewLoginComponent", "✅ WebView creado exitosamente")
                    creationError = null
                } else {
                    creationError = "No se pudo crear WebView - usa el sistema integrado existente"
                    onWebViewUnavailable()
                }
            } catch (e: Exception) {
                Log.e("WebViewLoginComponent", "Error crítico creando WebView", e)
                creationError = "Error crítico: ${e.message}"
                onWebViewUnavailable()
            }
        }
    }

    // Configurar WebView cuando se crea exitosamente
    LaunchedEffect(webView) {
        webView?.let { wv ->
            try {
                Log.d("WebViewLoginComponent", "Configurando WebView creado...")
                onWebViewReady(wv)
            } catch (e: Exception) {
                Log.e("WebViewLoginComponent", "Error configurando WebView", e)
                creationError = "Error configurando WebView: ${e.message}"
            }
        }
    }

    // Cargar URL cuando se hace visible
    LaunchedEffect(state.isVisible, webView, customUrl) {
        val currentWebView = webView
        if (state.isVisible && currentWebView != null) {
            val url = customUrl ?: "https://qvapay.com/login"
            Log.d("WebViewLoginComponent", "Cargando URL: $url")
            currentWebView.loadUrl(url)
        }
    }

    // Crear WebView invisible para mantener en segundo plano
    if (!state.isVisible && webView != null) {
        // WebView en segundo plano - no mostrar UI pero mantener instancia
        AndroidView(
            factory = { webView!! },
            modifier = Modifier
                .size(1.dp) // Mínimo visible para que Android lo procese
                .alpha(0f) // Invisible visualmente
        )
    }

    if (state.isVisible) {
        // WebView visible para el usuario
        Box(modifier = Modifier.fillMaxSize()) {
            // WebView en el fondo
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // WebView para login con Cloudflare
                val currentWebView = webView
                if (currentWebView != null) {
                    AndroidView(
                        factory = { currentWebView },
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
                                text = creationError
                                    ?: "WebView no disponible. El sistema ya tiene un WebView inicializado. Usa el login integrado desde la pantalla principal.",
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

                // Diálogo de confirmación superpuesto
                if (showConfirmDialog && offer != null) {
                    WebViewAcceptDialog(
                        offer = offer,
                        onAccept = onAcceptOffer,
                        onCancel = onCancelOffer
                    )
                }
            }
        }
        // No renderizar nada cuando no es visible
    }
}

// Intentar crear WebView con manejo robusto de errores
fun createWebViewSafely(context: Context): WebView? {
    return try {
        Log.d("WebViewLoginComponent", "Intentando crear WebView...")

        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Configurar WebViewClient básico
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return if (url?.startsWith("https://qvapay.com") == true ||
                        url?.startsWith("https://www.qvapay.com") == true
                    ) {
                        view?.loadUrl(url)
                        true
                    } else {
                        false
                    }
                }
            }

            // Configuración mínima de settings
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }
        }
    } catch (e: Exception) {
        Log.e("WebViewLoginComponent", "Error creando WebView: ${e.message}", e)
        null
    }
}