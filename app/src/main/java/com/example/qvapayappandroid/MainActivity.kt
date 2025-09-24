package com.example.qvapayappandroid

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.qvapayappandroid.di.allModules
import com.example.qvapayappandroid.navigation.AppNavigation
import com.example.qvapayappandroid.presentation.ui.theme.AppTheme
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Force light mode to avoid MIUI's forced dark mode quirks
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        super.onCreate(savedInstanceState)

        // Add a short delay so the splash screen stays visible
        Handler(Looper.getMainLooper()).postDelayed({
            setTheme(R.style.Theme_Qvapayoffers)

            // Warm up the WebView ahead of time
            initializeWebView()

            // Spin up Koin if it has not been started yet
            if (org.koin.core.context.GlobalContext.getOrNull() == null) {
                startKoin {
                    androidLogger()
                    androidContext(this@MainActivity)
                    modules(allModules)
                }
            }

            setContent {
                AppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }, 1500) // 1.5s splash delay
    }
    
    private fun initializeWebView() {
        try {
            Log.d("MainActivity", "🚀 Inicializando WebView proactivamente...")
            
            // Check whether WebView is actually available
            try {
                val webViewPackageInfo = WebView.getCurrentWebViewPackage()
                Log.d("MainActivity", "📦 WebView package: ${webViewPackageInfo?.packageName}")
            } catch (e: Exception) {
                Log.w("MainActivity", "⚠️ No se pudo obtener info del WebView package: ${e.message}")
            }
            
            // Create a throwaway WebView to trigger initialization
            val tempWebView = WebView(this)
            
            // Apply a more thorough settings configuration
            tempWebView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportMultipleWindows(false)
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            }
            
            // Use a lightweight WebViewClient just to log failures
            tempWebView.webViewClient = object : android.webkit.WebViewClient() {
                override fun onReceivedError(
                    view: WebView?, 
                    errorCode: Int, 
                    description: String?, 
                    failingUrl: String?
                ) {
                    Log.w("MainActivity", "🌐 WebView error durante inicialización: $description")
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d("MainActivity", "✅ WebView página inicial cargada: $url")
                }
            }
            
            // Load a blank page to complete initialization
            tempWebView.loadUrl("about:blank")
            
            Log.d("MainActivity", "✅ WebView inicializado exitosamente")
            
            // Tear down the temporary WebView after a longer pause
            tempWebView.postDelayed({
                try {
                    tempWebView.clearHistory()
                    tempWebView.clearCache(true)
                    tempWebView.destroy()
                    Log.d("MainActivity", "🗑️ WebView temporal destruido correctamente")
                } catch (e: Exception) {
                    Log.w("MainActivity", "Error destruyendo WebView temporal: ${e.message}")
                }
            }, 2000) // Give it extra time to finish warming up
            
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Error crítico inicializando WebView: ${e.message}", e)
            
            // Attempt a bit more diagnostics
            try {
                val webViewPackage = WebView.getCurrentWebViewPackage()
                if (webViewPackage == null) {
                    Log.e("MainActivity", "🚫 WebView no está disponible en este dispositivo")
                } else {
                    Log.e("MainActivity", "📱 WebView disponible pero falló inicialización: ${webViewPackage.packageName}")
                }
            } catch (diagE: Exception) {
                Log.e("MainActivity", "💥 Error en diagnóstico de WebView: ${diagE.message}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    MaterialTheme {
        AppNavigation()
    }
}
