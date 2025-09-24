package com.example.qvapayappandroid.presentation.ui.webview

/**
 * WebView intents covering every action the user can trigger.
 */
sealed interface WebViewIntent {
    /**
     * Show the WebView with a specific URL.
     */
    data class ShowWebView(val url: String = WebViewFullScreenState.QVAPAY_LOGIN_URL) : WebViewIntent
    
    /** Hide the WebView. */
    data object HideWebView : WebViewIntent
    
    /** Reload the current page. */
    data object Reload : WebViewIntent
    
    /** Clear the error state so the UI can retry. */
    data object ClearError : WebViewIntent
    
    /** Mark that navigation to a new URL is underway. */
    data object MarkNavigatingToNewUrl : WebViewIntent
    
    /** Indicate that the WebView is unavailable. */
    data object OnWebViewUnavailable : WebViewIntent
    
    /** Update the loading state. */
    data class SetLoading(val isLoading: Boolean) : WebViewIntent
    
    /** Set the current error message. */
    data class SetError(val error: String?) : WebViewIntent
}
