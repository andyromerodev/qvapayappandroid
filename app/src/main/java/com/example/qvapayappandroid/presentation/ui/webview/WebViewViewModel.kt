package com.example.qvapayappandroid.presentation.ui.webview

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WebViewViewModel(
    private val applyToP2POfferWebViewUseCase: ApplyToP2POfferWebViewUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WebViewScreenState())
    val state: StateFlow<WebViewScreenState> = _state.asStateFlow()

    private var webViewRef: WeakReference<WebView>? = null
    private var savedStateBundle: Bundle? = null
    private var hasCommittedFrame: Boolean = false
    private var isShowingShimmer: Boolean = false

    fun showWebView(url: String = WebViewScreenState.QVAPAY_LOGIN_URL) {
        _state.value = _state.value.copy(
            isVisible = true,
            url = url,
            isLoading = true,
            error = null
        )
    }

    fun hideWebView() {
        _state.value = WebViewScreenState.hide()
    }

    fun setLoading(isLoading: Boolean) {
        _state.value = _state.value.copy(isLoading = isLoading)
    }

    fun setError(error: String?) {
        _state.value = _state.value.copy(error = error, isLoading = false)
    }

    fun getOrCreateWebView(ctx: Context, initialUrl: String): WebView {
        val existing = webViewRef?.get()
        if (existing != null) return existing

        val wv = WebView(ctx).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            webChromeClient = object : WebChromeClient() {}

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    val u = url.orEmpty()
                    val isData = u.startsWith("data:", true)
                    val isBlank = u == "about:blank"
                    if (!hasCommittedFrame && !isData && !isBlank && !isShowingShimmer) {
                        setLoading(true)
                        showShimmerHTML(view)
                    }
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    val u = url.orEmpty()
                    if (u.startsWith("data:", ignoreCase = true) || u == "about:blank") return
                    hasCommittedFrame = true
                    isShowingShimmer = false
                    setLoading(false)
                }


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

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    if (request?.isForMainFrame != true) return
                    if (hasCommittedFrame) return
                    val code = errorResponse?.statusCode ?: return
                    if (code == 204 || code == 304) return
                    setLoading(false)
                    setError("HTTP $code")
                }
            }
        }

        if (savedStateBundle != null) {
            wv.restoreState(savedStateBundle!!)
            hasCommittedFrame = true
            setLoading(false)
        } else {
            hasCommittedFrame = false
            isShowingShimmer = false
            setLoading(true)
            wv.post { showShimmerHTML(wv) }

            val urlToLoad = state.value.url.takeIf { it.isNotBlank() } ?: initialUrl
            viewModelScope.launch {
                delay(500)
                wv.post { wv.loadUrl(urlToLoad) }
            }
        }

        webViewRef = WeakReference(wv)
        return wv
    }

    fun saveWebViewState() {
        val wv = webViewRef?.get() ?: return
        val out = Bundle()
        wv.saveState(out)
        savedStateBundle = out
    }

    fun markNavigatingToNewUrl() {
        hasCommittedFrame = false
        setLoading(true)
    }

    fun onWebViewUnavailable() {
        setError("WebView no está disponible en este dispositivo")
    }

    fun reload() {
        webViewRef?.get()?.let {
            markNavigatingToNewUrl()
            it.reload()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    // Helpers
    private suspend fun WebView.evalJs(code: String): String? =
        suspendCoroutine { cont ->
            try {
                this.evaluateJavascript(code) { res -> cont.resume(res) }
            } catch (t: Throwable) {
                cont.resumeWithException(t)
            }
        }

    private fun String?.jsValue(): String? {
        if (this == null) return null
        if (this == "null") return null
        return if (length >= 2 && startsWith("\"") && endsWith("\"")) substring(1, length - 1) else this
    }

    fun executeButtonClick(offerId: String, onResult: (Boolean, String?) -> Unit) {
        Log.d("WebViewViewModel", "executeButtonClick() ▶️ inicio | offerId=$offerId")
        val wv = webViewRef?.get() ?: run {
            setError("WebView no está disponible"); onResult(false, "WebView no está disponible"); return
        }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                setLoading(true)

                val runnerSeed = """
                (function(){
                  try{
                    window.__QP_STATUS='pending'; window.__QP_DETAIL='';
                    window.__QP_RUN=function(){
                      try{
                        const MAX_TRIES=20, INTERVAL=500; let tries=0;
                        function findButton(){
                          const buttons=[...document.querySelectorAll('button')];
                          const byText = buttons.find(b => ((b.textContent||'').toLowerCase().includes('aplicar a esta oferta') || (b.textContent||'').toLowerCase().includes('aplicar')));
                          if (byText) return byText;
                          const dgrid=document.querySelector('.d-grid .btn-primary'); if (dgrid) return dgrid;
                          const wire = buttons.find(b => b.getAttribute && b.getAttribute('wire:click')==='apply'); if (wire) return wire;
                          const waves=document.querySelector('button.btn.btn-primary.waves-effect.waves-float.waves-light'); if (waves) return waves;
                          return null;
                        }
                        (function tick(){
                          try{
                            tries++;
                            if (document.readyState!=='complete'){ if(tries>=MAX_TRIES){window.__QP_STATUS='timeout_dom';return;} return setTimeout(tick,INTERVAL); }
                            const btn=findButton();
                            if(!btn){ if(tries>=MAX_TRIES){window.__QP_STATUS='not_found';return;} return setTimeout(tick,INTERVAL); }
                            const r=btn.getBoundingClientRect();
                            if(btn.disabled){ window.__QP_STATUS='disabled'; return; }
                            if(r.width<=0||r.height<=0){ try{btn.click(); window.__QP_STATUS='clicked_maybe';}catch(e){window.__QP_STATUS='not_clickable'; window.__QP_DETAIL=String(e&&e.message||e);} return; }
                            btn.click(); window.__QP_STATUS='success';
                          }catch(e){ window.__QP_STATUS='error'; window.__QP_DETAIL=String(e&&e.message||e); }
                        })();
                      }catch(e){ window.__QP_STATUS='general_error'; window.__QP_DETAIL=String(e&&e.message||e); }
                    };
                    'ready';
                  }catch(e){ 'seed_error'; }
                })();
            """.trimIndent()

                fun ensureSeedJs() = """
                (function(){
                  try { return (typeof window.__QP_RUN==='function') ? 'ok' : 'need_seed'; }
                  catch(e){ return 'need_seed'; }
                })();
            """.trimIndent()

                // 1) navega si hace falta
                val currentUrl = wv.url
                if (currentUrl?.contains(offerId) != true) {
                    val navRes = wv.evalJs("(function(){try{var t='https://qvapay.com/p2p/$offerId'; if(location.href.includes('$offerId')) return 'already'; location.href=t; 'nav';}catch(e){'err';}})();")?.jsValue()
                    Log.d("WebViewViewModel", "executeButtonClick() [navigate]=$navRes")
                } else {
                    Log.d("WebViewViewModel", "executeButtonClick() 🟢 ya en la oferta")
                }

                // 2) espera URL objetivo + readyState
                val t1 = System.currentTimeMillis() + 15_000
                while (System.currentTimeMillis() < t1) {
                    val href = wv.evalJs("document.location && document.location.href")?.jsValue()
                    val ready = wv.evalJs("document && document.readyState")?.jsValue()
                    Log.d("WebViewViewModel", "executeButtonClick() 🔎 poll href=$href | ready=$ready")
                    if ((href?.contains(offerId) == true) && (ready == "interactive" || ready == "complete")) break
                    delay(400)
                }

                // 3) *** re-seed en la PÁGINA REAL *** (clave para evitar RUN=run_error)
                val check = wv.evalJs(ensureSeedJs())?.jsValue()
                Log.d("WebViewViewModel", "executeButtonClick() ensureSeed before run = $check")
                if (check != "ok") {
                    val seed2 = wv.evalJs(runnerSeed)?.jsValue()
                    Log.d("WebViewViewModel", "executeButtonClick() [seed2]=$seed2")
                    // micro-pausa por si el engine tarda un frame en adjuntar globals
                    delay(150)
                }

                // 4) run + esperar estado
                val runRes = wv.evalJs("try{ window.__QP_RUN(); 'started' }catch(e){ 'run_error' }")?.jsValue()
                Log.d("WebViewViewModel", "executeButtonClick() ▶️ RUN=$runRes")

                val t2 = System.currentTimeMillis() + 12_000
                var status: String? = null
                var detail: String? = null
                while (System.currentTimeMillis() < t2) {
                    status = wv.evalJs("window.__QP_STATUS")?.jsValue()
                    detail = wv.evalJs("window.__QP_DETAIL")?.jsValue()
                    Log.d("WebViewViewModel", "executeButtonClick() 📥 status=$status detail=$detail")
                    if (!status.isNullOrBlank() && status != "pending") break
                    delay(400)
                }

                setLoading(false)
                when (status) {
                    "success", "clicked_maybe" -> onResult(true, "Oferta aplicada exitosamente")
                    "not_found" -> { setError("No se encontró el botón de aplicar"); onResult(false, "No se encontró el botón") }
                    "disabled" -> { setError("El botón de aplicar está deshabilitado"); onResult(false, "Botón deshabilitado") }
                    "not_clickable" -> { setError("El botón no es clickeable"); onResult(false, "Botón no clickeable") }
                    "timeout_dom" -> { setError("La página tardó demasiado en cargar"); onResult(false, "Timeout de carga") }
                    "error","general_error","run_error", null -> { setError("Error ejecutando la acción"); onResult(false, detail ?: "Error ejecutando la acción") }
                    else -> { setError("Error ejecutando la acción: $status"); onResult(false, "Error: $status") }
                }
            } catch (e: Exception) {
                Log.e("WebViewViewModel", "executeButtonClick() 💥 excepción", e)
                setLoading(false)
                setError("Error ejecutando acción: ${e.message}")
                onResult(false, "Error ejecutando acción: ${e.message}")
            }
        }
    }



    fun onWebViewReady(webView: WebView, initialUrl: String? = null) {
        webViewRef = WeakReference(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: android.webkit.ConsoleMessage): Boolean {
                Log.d(
                    "WebViewConsole",
                    "[${message.messageLevel()}] ${message.message()} @${message.sourceId()}:${message.lineNumber()}"
                )
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val u = url.orEmpty()
                val isData = u.startsWith("data:", true)
                val isBlank = u == "about:blank"
                if (!hasCommittedFrame && !isData && !isBlank && !isShowingShimmer) {
                    setLoading(true)
                    showShimmerHTML(view)
                }
            }
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                hasCommittedFrame = true
                isShowingShimmer = false
                setLoading(false)
            }
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val description = error?.description?.toString() ?: ""
                if (description.contains("net::ERR_BLOCKED_BY_ORB") ||
                    description.contains("net::ERR_FAILED")) return
                setLoading(false)
                setError(description.ifBlank { "Error de carga" })
            }
        }

        if (savedStateBundle != null) {
            webView.restoreState(savedStateBundle!!)
            hasCommittedFrame = true
            setLoading(false)
        } else if (webView.url.isNullOrBlank()) {
            hasCommittedFrame = false
            isShowingShimmer = false
            setLoading(true)
            webView.post { showShimmerHTML(webView) }

            val urlToLoad =
                state.value.url.takeIf { it.isNotBlank() }
                    ?: initialUrl
                    ?: WebViewScreenState.QVAPAY_LOGIN_URL
            viewModelScope.launch {
                delay(500)
                webView.post { webView.loadUrl(urlToLoad) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webViewRef?.get()?.let { wv ->
            try {
                wv.stopLoading()
                wv.onPause()
                wv.webViewClient = WebViewClient()
                wv.webChromeClient = null
                wv.setDownloadListener(null)
                wv.loadUrl("about:blank")
                wv.clearHistory()
                wv.clearCache(true)
                wv.removeAllViews()
                wv.destroy()
            } catch (_: Throwable) { }
        }
        webViewRef = null
    }

    private fun showShimmerHTML(webView: WebView?) {
        val wv = webView ?: return
        if (isShowingShimmer) return
        isShowingShimmer = true

        val html = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                html, body { height: 100%; width: 100%; }
                body {
                    background: #f8f9fa;
                    color: #32373d;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Helvetica Neue", Arial, sans-serif;
                    -webkit-font-smoothing: antialiased;
                    -moz-osx-font-smoothing: grayscale;
                    -webkit-text-size-adjust: 100%;
                    overflow: hidden;
                }
                .center {
                    position: fixed;
                    inset: 0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    padding: env(safe-area-inset-top) env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left);
                    min-height: 100vh;
                }
                .loading-text {
                    font-size: clamp(20px, 6vw, 36px);
                    font-weight: 800;
                    letter-spacing: 0.5px;
                    text-align: center;
                    line-height: 1.2;
                    user-select: none;
                    -webkit-user-select: none;
                    animation: pulseFade 1.4s ease-in-out infinite alternate;
                }
                @keyframes pulseFade {
                    0%   { opacity: 1; transform: translateY(0); }
                    100% { opacity: 0.35; transform: translateY(2px); }
                }
                /* Tema oscuro */
                @media (prefers-color-scheme: dark) {
                    body { background: #0f1214; color: #eef2f6; }
                }
            </style>
        </head>
        <body>
            <div class="center">
                <div class="loading-text">LOADING QVAPAY</div>
            </div>
        </body>
        </html>
    """.trimIndent()

        val encoded = android.util.Base64.encodeToString(
            html.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )
        wv.post {
            wv.loadUrl("data:text/html;charset=utf-8;base64,$encoded")
        }
    }


    private fun shouldIgnoreWebError(
        description: String?,
        request: WebResourceRequest?
    ): Boolean {
        val d = description?.lowercase() ?: ""
        if (d.contains("net::err_blocked_by_orb") || d.contains("net::err_failed")) return true
        if (request?.isForMainFrame == false) return true
        if (hasCommittedFrame) return true
        val url = request?.url?.toString().orEmpty()
        if (url == "about:blank") return true
        return false
    }
}
