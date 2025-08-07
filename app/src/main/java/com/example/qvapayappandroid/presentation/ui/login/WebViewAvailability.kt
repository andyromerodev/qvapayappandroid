package com.example.qvapayappandroid.presentation.ui.login

import android.content.Context
import android.content.pm.PackageManager

object WebViewAvailability {
    fun isWebViewAvailable(context: Context): Boolean {
        return try {
            // Verificar si el paquete WebView está disponible
            val packageManager = context.packageManager
            packageManager.getPackageInfo("com.google.android.webview", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                // Fallback: verificar Android System WebView
                val packageManager = context.packageManager
                packageManager.getPackageInfo("com.android.webview", 0)
                true
            } catch (e2: Exception) {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}