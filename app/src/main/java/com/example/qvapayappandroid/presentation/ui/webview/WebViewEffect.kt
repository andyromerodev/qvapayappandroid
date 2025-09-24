package com.example.qvapayappandroid.presentation.ui.webview

/**
 * WebView effects representing one-off events the UI must handle.
 */
sealed interface WebViewEffect {
    /** Navigation completed successfully. */
    data object NavigationCompleted : WebViewEffect
    
    /** Navigation failed with an error. */
    data class NavigationError(val error: String) : WebViewEffect
    
    /** WebView finished loading. */
    data object WebViewLoaded : WebViewEffect
    
    /** Request to show a message to the user. */
    data class ShowMessage(val message: String) : WebViewEffect
    
    /** Instruct the UI to close the WebView. */
    data object CloseWebView : WebViewEffect
    
    /** Page started loading. */
    data class PageStarted(val url: String) : WebViewEffect
    
    /** HTTP error response was received. */
    data class HttpError(val code: Int, val url: String) : WebViewEffect
    
    /** WebView is unavailable on the device. */
    data object WebViewUnavailable : WebViewEffect
}
