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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class WebViewFullScreenViewModel : ViewModel() {

    private val _state = MutableStateFlow(WebViewFullScreenState())
    val state: StateFlow<WebViewFullScreenState> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<WebViewEffect>()
    val effect: SharedFlow<WebViewEffect> = _effect.asSharedFlow()

    private var webViewRef: WeakReference<WebView>? = null
    private var savedStateBundle: Bundle? = null
    private var hasCommittedFrame: Boolean = false
    private var isShowingShimmer: Boolean = false

    /**
     * Maneja todos los intents del WebView
     */
    fun handleIntent(intent: WebViewIntent) {
        when (intent) {
            is WebViewIntent.ShowWebView -> showWebView(intent.url)
            is WebViewIntent.HideWebView -> hideWebView()
            is WebViewIntent.Reload -> reload()
            is WebViewIntent.ClearError -> clearError()
            is WebViewIntent.MarkNavigatingToNewUrl -> markNavigatingToNewUrl()
            is WebViewIntent.OnWebViewUnavailable -> onWebViewUnavailable()
            is WebViewIntent.SetLoading -> setLoading(intent.isLoading)
            is WebViewIntent.SetError -> setError(intent.error)
        }
    }

    private fun showWebView(url: String = WebViewFullScreenState.QVAPAY_LOGIN_URL) {
        _state.value = _state.value.copy(
            isVisible = true,
            url = url,
            isLoading = true,
            error = null
        )
        emitEffect(WebViewEffect.PageStarted(url))
    }

    private fun hideWebView() {
        _state.value = WebViewFullScreenState(
            isVisible = false,
            url = "",
            isLoading = false,
            error = null
        )
        emitEffect(WebViewEffect.CloseWebView)
    }

    private fun setLoading(isLoading: Boolean) {
        _state.value = _state.value.copy(isLoading = isLoading)
    }

    private fun setError(error: String?) {
        _state.value = _state.value.copy(error = error, isLoading = false)
        if (error != null) {
            emitEffect(WebViewEffect.NavigationError(error))
        }
    }

    /**
     * Emite un efecto
     */
    private fun emitEffect(effect: WebViewEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
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
                    emitEffect(WebViewEffect.NavigationCompleted)
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
                    emitEffect(WebViewEffect.HttpError(code, request?.url?.toString() ?: ""))
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

    private fun markNavigatingToNewUrl() {
        hasCommittedFrame = false
        setLoading(true)
    }

    private fun onWebViewUnavailable() {
        setError("WebView no está disponible en este dispositivo")
        emitEffect(WebViewEffect.WebViewUnavailable)
    }

    private fun reload() {
        webViewRef?.get()?.let {
            markNavigatingToNewUrl()
            it.reload()
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
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