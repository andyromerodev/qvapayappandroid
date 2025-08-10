package com.example.qvapayappandroid.presentation.ui.webview

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qvapayappandroid.domain.usecase.ApplyToP2POfferWebViewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class WebViewViewModel(
    private val applyToP2POfferWebViewUseCase: ApplyToP2POfferWebViewUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WebViewScreenState())
    val state: StateFlow<WebViewScreenState> = _state.asStateFlow()

    // Instancia única + estado persistente
    private var webViewRef: WeakReference<WebView>? = null
    private var savedStateBundle: Bundle? = null
    private var hasCommittedFrame: Boolean = false

    fun showWebView(url: String = WebViewScreenState.QVAPAY_LOGIN_URL) {
        Log.d("WebViewViewModel", "Mostrando WebView con URL: $url")
        _state.value = _state.value.copy(
            isVisible = true,
            url = url,
            isLoading = true,
            error = null
        )
        // NOTA: el load real se hace al crear/recuperar el WebView
    }

    fun hideWebView() {
        Log.d("WebViewViewModel", "Ocultando WebView")
        _state.value = WebViewScreenState.hide()
    }

    fun setLoading(isLoading: Boolean) {
        _state.value = _state.value.copy(isLoading = isLoading)
    }

    fun setError(error: String?) {
        Log.e("WebViewViewModel", "Error en WebView: $error")
        _state.value = _state.value.copy(
            error = error,
            isLoading = false
        )
    }

    /**
     * Devuelve siempre la misma instancia de WebView mientras viva el ViewModel.
     * Restaura estado si lo hubiera. Carga initialUrl solo en el primer uso real.
     */
    fun getOrCreateWebView(ctx: Context, initialUrl: String): WebView {
        val existing = webViewRef?.get()
        if (existing != null) return existing

        val appCtx = ctx.applicationContext
        val wv = WebView(appCtx).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(android.graphics.Color.TRANSPARENT)

            webChromeClient = object : WebChromeClient() {}

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (!hasCommittedFrame) setLoading(true)
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    hasCommittedFrame = true
                    setLoading(false)
                }

                // Errores de red (main & subresources)
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    val desc = error?.description?.toString()
                    if (shouldIgnoreWebError(desc, request)) return

                    setLoading(false)
                    setError(desc ?: "Error de carga")
                }

                // Errores HTTP (4xx/5xx) con respuesta
                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    // Solo tratamos errores del frame principal ANTES del primer frame
                    if (true &&
                        request?.isForMainFrame != true
                    ) return
                    if (hasCommittedFrame) return

                    val code = errorResponse?.statusCode ?: return
                    // Opcional: ignora 204/304 si aparecen
                    if (code == 204 || code == 304) return

                    setLoading(false)
                    setError("HTTP $code")
                }
            }

        }

        // Restaurar estado si existe; si no, carga initialUrl
        if (savedStateBundle != null) {
            wv.restoreState(savedStateBundle!!)
            // No activar shimmer al volver de pestañas
            hasCommittedFrame = true
            setLoading(false)
        } else {
            // Primera navegación real → shimmer hasta commit visible
            hasCommittedFrame = false
            setLoading(true)
            val urlToLoad = state.value.url.takeIf { it.isNotBlank() } ?: initialUrl
            wv.loadUrl(urlToLoad)
        }

        webViewRef = java.lang.ref.WeakReference(wv)
        return wv
    }

    /** Guardar estado de la página (llamar al salir de la pantalla/pestaña) */
    fun saveWebViewState() {
        val wv = webViewRef?.get() ?: return
        val out = Bundle()
        wv.saveState(out)
        savedStateBundle = out
    }

    /** Llamar cuando tú forces una nueva navegación (antes de loadUrl) */
    fun markNavigatingToNewUrl() {
        hasCommittedFrame = false
        setLoading(true)
    }

    fun onWebViewUnavailable() {
        Log.w("WebViewViewModel", "WebView no disponible")
        setError("WebView no está disponible en este dispositivo")
    }

    fun reload() {
        webViewRef?.get()?.let { webView ->
            Log.d("WebViewViewModel", "Recargando WebView")
            markNavigatingToNewUrl()
            webView.reload()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    // === Tu lógica de JS se mantiene igual (solo usa webViewRef en vez de webViewInstance) ===
    fun executeButtonClick(offerId: String, onResult: (Boolean, String?) -> Unit) {
        Log.d("WebViewViewModel", "🖱️ executeButtonClick llamado con offerId: $offerId")

        val currentWebView = webViewRef?.get()
        if (currentWebView == null) {
            Log.e("WebViewViewModel", "❌ WebView no está disponible para ejecutar botón")
            setError("WebView no está disponible")
            onResult(false, "WebView no está disponible")
            return
        }

        setLoading(true)

        val javascript = """
            (function() {
                try {
                    function clickApplyButton() {
                        try {
                            let button = null;
                            const buttons = document.querySelectorAll('button');
                            for (let btn of buttons) {
                                const text = (btn.textContent || '').toLowerCase();
                                if (text.includes('aplicar a esta oferta') || text.includes('aplicar')) {
                                    button = btn; break;
                                }
                            }
                            if (!button) button = document.querySelector('.d-grid .btn-primary');
                            if (!button) {
                                const allButtons = document.querySelectorAll('button');
                                for (let btn of allButtons) {
                                    if (btn.getAttribute && btn.getAttribute('wire:click') === 'apply') { button = btn; break; }
                                }
                            }
                            if (!button) button = document.querySelector('button.btn.btn-primary.waves-effect.waves-float.waves-light');
                            if (button && !button.disabled) {
                                const rect = button.getBoundingClientRect();
                                if (rect.width > 0 && rect.height > 0) { button.click(); return 'success'; }
                                return 'not_clickable';
                            } else {
                                return button ? 'disabled' : 'not_found';
                            }
                        } catch (e) { return 'error'; }
                    }
                    if (window.location.href.includes('$offerId')) {
                        return clickApplyButton();
                    } else {
                        window.location.href = 'https://qvapay.com/p2p/$offerId';
                        setTimeout(function() { return clickApplyButton(); }, 3000);
                        return 'navigating';
                    }
                } catch (e) { return 'general_error'; }
            })();
        """.trimIndent()

        try {
            currentWebView.evaluateJavascript(javascript) { result ->
                Log.d("WebViewViewModel", "🔄 Resultado ejecución JavaScript: $result")
                setLoading(false)
                val cleanResult = result?.replace("\"", "") ?: "null"
                when (cleanResult) {
                    "success" -> onResult(true, "Oferta aplicada exitosamente")
                    "not_found" -> { setError("No se encontró el botón de aplicar"); onResult(false, "No se encontró el botón de aplicar en la página") }
                    "disabled" -> { setError("El botón de aplicar está deshabilitado"); onResult(false, "El botón de aplicar está deshabilitado") }
                    "not_clickable" -> { setError("El botón no es clickeable"); onResult(false, "El botón no es clickeable") }
                    "navigating" -> {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(4000)
                            currentWebView.evaluateJavascript("document.location.href") { url ->
                                if (url?.contains(offerId) == true) onResult(true, "Navegación completada")
                                else onResult(false, "Error en la navegación")
                            }
                        }
                    }
                    else -> { setError("Error ejecutando la acción: $cleanResult"); onResult(false, "Error inesperado: $cleanResult") }
                }
            }
        } catch (e: Exception) {
            Log.e("WebViewViewModel", "💥 Error ejecutando JavaScript", e)
            setError("Error ejecutando acción: ${e.message}")
            setLoading(false)
            onResult(false, "Error ejecutando acción: ${e.message}")
        }
    }

    // ✅ Shim para mantener compatibilidad con WebViewScreen
    fun onWebViewReady(webView: WebView, initialUrl: String? = null) {
        // guarda la instancia pasada por el composable
        webViewRef = java.lang.ref.WeakReference(webView)

        // settings y fondo
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        // chrome
        webView.webChromeClient = object : WebChromeClient() {}

        // clientes con control de shimmer (commit visible)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (!hasCommittedFrame) setLoading(true)
            }
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                hasCommittedFrame = true
                setLoading(false)
            }
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val description = error?.description?.toString() ?: ""
                Log.w("WebViewViewModel", "⚠️ Error en subrecurso: $description")

                // Ignorar errores que no afectan la carga principal
                if (description.contains("net::ERR_BLOCKED_BY_ORB") ||
                    description.contains("net::ERR_FAILED")
                ) {
                    return // No hacer nada
                }

                setLoading(false)
                setError(description.ifBlank { "Error de carga" })
            }
        }

        // Restaurar estado si existía; si no, cargar URL inicial una única vez
        if (savedStateBundle != null) {
            webView.restoreState(savedStateBundle!!)
            hasCommittedFrame = true
            setLoading(false)
        } else if (webView.url.isNullOrBlank()) {
            // primera navegación real
            hasCommittedFrame = false
            setLoading(true)
            val urlToLoad =
                state.value.url.takeIf { it.isNotBlank() }
                    ?: initialUrl
                    ?: WebViewScreenState.QVAPAY_LOGIN_URL
            webView.loadUrl(urlToLoad)
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d("WebViewViewModel", "ViewModel limpiado")
        // Destruir definitivamente si el ViewModel muere (saliste del flujo)
        webViewRef?.get()?.let { wv ->
            try {
                wv.stopLoading()
                wv.onPause()
                wv.setWebViewClient(WebViewClient())
                wv.webChromeClient = null
                wv.setDownloadListener(null)
                wv.loadUrl("about:blank")
                wv.clearHistory()
                wv.clearCache(true)
                wv.removeAllViews()
                wv.destroy()
            } catch (_: Throwable) { /* ignore */ }
        }
        webViewRef = null
    }

    // 1) Helper para filtrar "falsos" errores
    private fun shouldIgnoreWebError(
        description: String?,
        request: WebResourceRequest?
    ): Boolean {
        val d = description?.lowercase() ?: ""
        // Ignorar los dos que te molestan
        if (d.contains("net::err_blocked_by_orb") || d.contains("net::err_failed")) return true

        // Si NO es el frame principal (subrecurso), ignora
        if (request?.isForMainFrame == false) return true

        // Si ya tenemos un frame comprometido visible, ignora (no es fatal)
        if (hasCommittedFrame) return true

        // Evitar falsos positivos de about:blank
        val url = request?.url?.toString().orEmpty()
        if (url == "about:blank") return true

        return false
    }

}
