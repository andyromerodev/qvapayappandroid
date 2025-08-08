package com.example.qvapayappandroid.data.datasource

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView

class WebViewLoginDataSourceImpl : WebViewLoginDataSource {

    private var webView: WebView? = null
    private var onWebViewLoginCompleted: (() -> Unit)? = null

    override fun setOnWebViewLoginCompleted(callback: () -> Unit) {
        onWebViewLoginCompleted = callback
    }

    override fun setWebView(webView: WebView) {
        Log.d("WebViewLoginDataSource", "🔧 Setting up WebView and JavaScript interface...")
        this.webView = webView
        setupJavaScriptInterface(webView)
        Log.d("WebViewLoginDataSource", "✅ WebView and JavaScript interface configured")
    }

    override fun isWebViewReady(): Boolean = webView != null

    override fun applyToOffer(offerId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        applyToOfferWithRetryOld(offerId, onSuccess, onError, 0)
    }
    
    private fun applyToOfferWithRetry(offerId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, retryCount: Int) {
        val webView = this.webView
        if (webView == null) {
            onError("WebView no está disponible")
            return
        }

        try {
            Log.d("WebViewLoginDataSource", "🧪 TEST: Probando carga básica de URL para oferta: $offerId")
            
            val targetUrl = "https://qvapay.com/p2p/$offerId"
            
            // JavaScript básico para probar si la página carga correctamente
            val basicTestJs = """
                (function() {
                    try {
                        console.log('🚀 TEST: JavaScript iniciado - URL actual: ' + window.location.href);
                        
                        if (typeof window.AndroidInterface === 'undefined') {
                            console.error('❌ AndroidInterface no disponible');
                            return;
                        }
                        
                        // Reportar estado inicial (solo para debugging - no es error)
                        console.log('🔍 TEST - Estado inicial URL: ' + window.location.href);
                        
                        // Función para verificar si estamos en la página correcta
                        function checkPageLoad() {
                            const currentUrl = window.location.href;
                            const expectedOfferId = '$offerId';
                            
                            console.log('📍 Verificando página - URL actual:', currentUrl);
                            console.log('🎯 OfferID esperado:', expectedOfferId);
                            
                            if (currentUrl.includes(expectedOfferId)) {
                                window.AndroidInterface.onApplySuccess('✅ TEST EXITOSO: Página cargada correctamente - URL: ' + currentUrl);
                            } else {
                                // Intentar navegar a la URL correcta
                                console.log('🌐 Navegando a URL correcta...');
                                window.AndroidInterface.onApplyError('🌐 Navegando a: ' + '$targetUrl');
                                
                                window.location.href = '$targetUrl';
                                
                                // Verificar después de navegar
                                setTimeout(function() {
                                    const newUrl = window.location.href;
                                    if (newUrl.includes(expectedOfferId)) {
                                        window.AndroidInterface.onApplySuccess('✅ TEST EXITOSO: Navegación completada - URL: ' + newUrl);
                                    } else {
                                        window.AndroidInterface.onApplyError('❌ TEST FALLIDO: No se pudo cargar la página correcta. URL final: ' + newUrl);
                                    }
                                }, 3000);
                            }
                        }
                        
                        // Iniciar verificación
                        checkPageLoad();
                        
                    } catch (error) {
                        console.error('❌ Error en test JavaScript:', error);
                        window.AndroidInterface.onApplyError('❌ TEST ERROR: ' + error.message);
                    }
                })();
            """.trimIndent()
            
            // Configurar callbacks
            currentApplySuccessCallback = onSuccess
            currentApplyErrorCallback = onError
            
            // Timeout más corto para la prueba
            webView.postDelayed({
                if (currentApplySuccessCallback != null || currentApplyErrorCallback != null) {
                    Log.w("WebViewLoginDataSource", "⏰ TEST: Timeout después de 10 segundos")
                    currentApplyErrorCallback?.invoke("❌ TEST TIMEOUT: La prueba tardó más de 10 segundos")
                    clearApplyCallbacks()
                }
            }, 10000) // 10 segundos para la prueba
            
            Log.d("WebViewLoginDataSource", "🔍 TEST: WebView URL actual antes del test: ${webView.url}")
            
            // Verificar estado del WebView antes de ejecutar JavaScript
            Log.d("WebViewLoginDataSource", "🔍 TEST: WebView attached to window: ${webView.isAttachedToWindow}")
            Log.d("WebViewLoginDataSource", "🔍 TEST: WebView visibility: ${webView.visibility}")
            
            // Ejecutar JavaScript directamente en el hilo principal
            try {
                Log.d("WebViewLoginDataSource", "🚀 TEST: Ejecutando JavaScript de prueba...")
                
                // Verificar que AndroidInterface esté disponible primero
                webView.evaluateJavascript("typeof window.AndroidInterface") { interfaceCheck ->
                    Log.d("WebViewLoginDataSource", "🔍 TEST: AndroidInterface check: $interfaceCheck")
                    
                    if (interfaceCheck == "\"undefined\"" || interfaceCheck == "undefined") {
                        Log.e("WebViewLoginDataSource", "❌ TEST: AndroidInterface not available!")
                        currentApplyErrorCallback?.invoke("❌ TEST ERROR: AndroidInterface no disponible")
                        clearApplyCallbacks()
                        return@evaluateJavascript
                    }
                    
                    // Ejecutar el JavaScript principal
                    webView.evaluateJavascript(basicTestJs) { result ->
                        Log.d("WebViewLoginDataSource", "📄 TEST: JavaScript ejecutado, resultado: $result")
                        
                        if (result != null && result.contains("error", ignoreCase = true)) {
                            Log.e("WebViewLoginDataSource", "❌ TEST: Error inmediato en JavaScript: $result")
                            currentApplyErrorCallback?.invoke("❌ TEST ERROR: $result")
                            clearApplyCallbacks()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebViewLoginDataSource", "❌ TEST: Error ejecutando JavaScript", e)
                currentApplyErrorCallback?.invoke("❌ TEST ERROR: Error ejecutando JavaScript - ${e.message}")
                clearApplyCallbacks()
            }
            
        } catch (e: Exception) {
            Log.e("WebViewLoginDataSource", "❌ TEST: Error en prueba básica", e)
            onError("❌ TEST ERROR: ${e.message}")
        }
    }
    
    private fun applyToOfferWithRetryOld(offerId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, retryCount: Int) {
        if (retryCount >= 3) {
            onError("Máximo número de reintentos alcanzado (3) para aplicar a la oferta")
            return
        }
        
        val webView = this.webView
        if (webView == null) {
            onError("WebView no está disponible")
            return
        }

        try {
            Log.d("WebViewLoginDataSource", "Aplicando a oferta en segundo plano: $offerId (intento ${retryCount + 1}/3)")

            // JavaScript optimizado para trabajo en segundo plano con debugging mejorado
            val javascript = """
                (function() {
                    try {
                        console.log('🚀 Iniciando aplicación a oferta: $offerId');
                        
                        // Verificar AndroidInterface inmediatamente
                        if (typeof window.AndroidInterface === 'undefined') {
                            console.error('❌ AndroidInterface no está disponible');
                            return;
                        }
                        console.log('✅ AndroidInterface disponible');
                        
                        // Reportar estado inicial
                        window.AndroidInterface.onApplyInfo('🔍 JavaScript iniciado correctamente - URL actual: ' + window.location.href);
                        
                        // Función para aplicar a la oferta
                        function applyToOffer() {
                            console.log('🔍 applyToOffer() - URL actual:', window.location.href);
                            
                            // Si ya estamos en la página correcta, buscar el botón directamente
                            if (window.location.href.includes('$offerId')) {
                                console.log('✅ Ya estamos en la página correcta, buscando botón...');
                                clickApplyButton();
                            } else {
                                // Navegar a la URL de la oferta
                                console.log('🌐 Navegando a oferta: $offerId');
                                window.AndroidInterface.onApplyInfo('🌐 Navegando a: https://qvapay.com/p2p/$offerId');
                                window.location.href = 'https://qvapay.com/p2p/$offerId';
                                
                                // Esperar a que la página cargue - tiempo extendido para background
                                setTimeout(function() {
                                    console.log('⏳ Página debería haber cargado, verificando URL...');
                                    console.log('📍 URL después de navegación:', window.location.href);
                                    window.AndroidInterface.onApplyInfo('📍 URL después de 6s: ' + window.location.href);
                                    clickApplyButton();
                                }, 6000); // Tiempo extendido para WebView en segundo plano
                            }
                        }
                        
                        function clickApplyButton() {
                            try {
                                console.log('🔍 Buscando botón de aplicar...');
                                console.log('📍 URL actual:', window.location.href);
                                window.AndroidInterface.onApplyInfo('🔍 Buscando botón en URL: ' + window.location.href);
                                
                                let button = null;
                                let searchAttempts = 0;
                                const maxAttempts = 3;
                                
                                function findButton() {
                                    searchAttempts++;
                                    console.log('Intento de búsqueda #' + searchAttempts);
                                    
                                    // Método 1: Por texto del botón (más confiable basado en tu HTML)
                                    const buttons = document.querySelectorAll('button');
                                    console.log('Método 1 - Buscando entre ' + buttons.length + ' botones por texto');
                                    for (let btn of buttons) {
                                        const text = btn.textContent ? btn.textContent.toLowerCase() : '';
                                        console.log('Botón texto:', text);
                                        if (text.includes('aplicar a esta oferta') || text.includes('aplicar')) {
                                            button = btn;
                                            console.log('✅ Botón encontrado por texto completo:', text);
                                            break;
                                        }
                                    }
                                    
                                    // Método 2: Por clase btn-primary dentro de d-grid
                                    if (!button) {
                                        button = document.querySelector('.d-grid .btn-primary');
                                        console.log('Método 2 - .d-grid .btn-primary:', button ? 'Encontrado' : 'No encontrado');
                                        if (button) {
                                            console.log('Texto del botón encontrado:', button.textContent);
                                        }
                                    }
                                    
                                    // Método 3: Por atributo wire:click usando getAttribute
                                    if (!button) {
                                        const allButtons = document.querySelectorAll('button');
                                        for (let btn of allButtons) {
                                            if (btn.getAttribute && btn.getAttribute('wire:click') === 'apply') {
                                                button = btn;
                                                console.log('Método 3 - wire:click por getAttribute:', 'Encontrado');
                                                break;
                                            }
                                        }
                                        console.log('Método 3 - wire:click getAttribute:', button ? 'Encontrado' : 'No encontrado');
                                    }
                                    
                                    // Método 4: Por clase específica y contenido
                                    if (!button) {
                                        button = document.querySelector('button.btn.btn-primary.waves-effect.waves-float.waves-light');
                                        console.log('Método 4 - clases específicas:', button ? 'Encontrado' : 'No encontrado');
                                        if (button) {
                                            console.log('Texto del botón con clases específicas:', button.textContent);
                                        }
                                    }
                                    
                                    return button;
                                }
                                
                                button = findButton();
                                
                                // Si no se encuentra el botón, reintentar después de un momento
                                if (!button && searchAttempts < maxAttempts) {
                                    console.log('⚠️ Botón no encontrado, reintentando en 2 segundos...');
                                    window.AndroidInterface.onApplyInfo('⚠️ Botón no encontrado, intento ' + searchAttempts + '/' + maxAttempts + ' - reintentando...');
                                    setTimeout(function() {
                                        clickApplyButton();
                                    }, 2000);
                                    return;
                                }
                                
                                if (button && !button.disabled) {
                                    console.log('✅ Botón encontrado, aplicando...');
                                    window.AndroidInterface.onApplyInfo('✅ Botón encontrado! Haciendo click...');
                                    
                                    // Verificar que el botón sea visible y clickeable
                                    const rect = button.getBoundingClientRect();
                                    if (rect.width > 0 && rect.height > 0) {
                                        button.click();
                                        window.AndroidInterface.onApplyInfo('🖱️ Click realizado, esperando resultado...');
                                        
                                        // Verificar resultado después de un momento - tiempo extendido
                                        setTimeout(function() {
                                            checkApplicationResult();
                                        }, 5000); // Más tiempo para procesar la aplicación
                                    } else {
                                        console.log('❌ El botón no es clickeable');
                                        window.AndroidInterface.onApplyError('❌ El botón no es clickeable (rect: ' + rect.width + 'x' + rect.height + ')');
                                    }
                                } else {
                                    const reason = button ? '❌ El botón está deshabilitado' : '❌ No se encontró el botón de aplicar después de ' + maxAttempts + ' intentos';
                                    console.log('Error: ' + reason);
                                    window.AndroidInterface.onApplyError(reason);
                                }
                            } catch (error) {
                                console.error('Error al hacer click:', error);
                                window.AndroidInterface.onApplyError('Error al hacer click: ' + error.message);
                            }
                        }
                        
                        function checkApplicationResult() {
                            try {
                                console.log('Verificando resultado de aplicación...');
                                
                                // Buscar indicadores de éxito
                                const successSelectors = [
                                    '.alert-success',
                                    '.toast-success', 
                                    '.notification-success',
                                    '[class*="success"]',
                                    '.swal2-success' // SweetAlert success
                                ];
                                
                                // Buscar indicadores de error
                                const errorSelectors = [
                                    '.alert-danger',
                                    '.alert-error',
                                    '.toast-error',
                                    '.notification-error', 
                                    '[class*="error"]',
                                    '.swal2-error' // SweetAlert error
                                ];
                                
                                let result = null;
                                let isSuccess = false;
                                
                                // Verificar éxito
                                for (let selector of successSelectors) {
                                    const element = document.querySelector(selector);
                                    if (element && element.textContent && element.textContent.trim()) {
                                        result = element.textContent.trim();
                                        isSuccess = true;
                                        console.log('Éxito detectado:', result);
                                        break;
                                    }
                                }
                                
                                // Verificar error si no hubo éxito
                                if (!isSuccess) {
                                    for (let selector of errorSelectors) {
                                        const element = document.querySelector(selector);
                                        if (element && element.textContent && element.textContent.trim()) {
                                            result = element.textContent.trim();
                                            console.log('Error detectado:', result);
                                            window.AndroidInterface.onApplyError(result);
                                            return;
                                        }
                                    }
                                }
                                
                                if (isSuccess) {
                                    // Deshabilitar el botón después del éxito
                                    let successButton = document.querySelector('.d-grid .btn-primary');
                                    if (!successButton) {
                                        // Fallback: buscar por getAttribute
                                        const allButtons = document.querySelectorAll('button');
                                        for (let btn of allButtons) {
                                            if (btn.getAttribute && btn.getAttribute('wire:click') === 'apply') {
                                                successButton = btn;
                                                break;
                                            }
                                        }
                                    }
                                    if (successButton) {
                                        successButton.disabled = true;
                                        successButton.innerHTML = successButton.innerHTML.replace('Aplicar', 'Aplicado ✓');
                                        console.log('✅ Botón deshabilitado después del éxito');
                                    }
                                    window.AndroidInterface.onApplySuccess(result || 'Aplicación exitosa');
                                } else {
                                    // Verificar cambios en el botón como indicador de éxito
                                    let buttonAfter = document.querySelector('.d-grid .btn-primary');
                                    if (!buttonAfter) {
                                        // Fallback: buscar por getAttribute
                                        const allButtons = document.querySelectorAll('button');
                                        for (let btn of allButtons) {
                                            if (btn.getAttribute && btn.getAttribute('wire:click') === 'apply') {
                                                buttonAfter = btn;
                                                break;
                                            }
                                        }
                                    }
                                    if (buttonAfter && buttonAfter.disabled) {
                                        console.log('Botón deshabilitado - asumiendo éxito');
                                        window.AndroidInterface.onApplySuccess('Aplicación completada exitosamente');
                                    } else {
                                        // Verificar si hay algún cambio en la página que indique éxito
                                        const pageTitle = document.title.toLowerCase();
                                        if (pageTitle.includes('éxito') || pageTitle.includes('success')) {
                                            // También deshabilitar botón en este caso
                                            if (buttonAfter) {
                                                buttonAfter.disabled = true;
                                                buttonAfter.innerHTML = buttonAfter.innerHTML.replace('Aplicar', 'Aplicado ✓');
                                            }
                                            window.AndroidInterface.onApplySuccess('Aplicación completada');
                                        } else {
                                            window.AndroidInterface.onApplyError('No se pudo determinar el resultado de la aplicación');
                                        }
                                    }
                                }
                            } catch (error) {
                                console.error('Error verificando resultado:', error);
                                window.AndroidInterface.onApplyError('Error verificando resultado: ' + error.message);
                            }
                        }
                        
                        // Iniciar el proceso
                        applyToOffer();
                        
                    } catch (error) {
                        console.error('Error general:', error);
                        window.AndroidInterface.onApplyError('Error general: ' + error.message);
                    }
                })();
            """.trimIndent()

            // Configurar callbacks para esta operación específica
            currentApplySuccessCallback = onSuccess
            currentApplyErrorCallback = onError

            // Timeout de seguridad en caso de que el JavaScript no responda
            webView.postDelayed({
                if (currentApplySuccessCallback != null || currentApplyErrorCallback != null) {
                    Log.w("WebViewLoginDataSource", "⏰ JavaScript timeout - no response after 25 seconds")
                    currentApplyErrorCallback?.invoke("Timeout: La aplicación tardó demasiado en responder")
                    clearApplyCallbacks()
                }
            }, 25000) // 25 segundos timeout

            // Verificar que el WebView esté en un estado válido
            val currentUrl = try {
                webView.url ?: "null"
            } catch (e: Exception) {
                Log.e("WebViewLoginDataSource", "Error getting WebView URL: ${e.message}")
                "error"
            }
            Log.d("WebViewLoginDataSource", "🔍 WebView URL before JavaScript: $currentUrl")
            
            // Validar estado del WebView
            if (currentUrl == "null" || currentUrl == "error" || currentUrl.startsWith("about:") || currentUrl == "") {
                Log.w("WebViewLoginDataSource", "⚠️ WebView no tiene URL válida ($currentUrl), navegando primero a QvaPay...")
                webView.post {
                    webView.loadUrl("https://qvapay.com")
                    webView.postDelayed({
                        // Reintentar después de cargar la página
                        Log.d("WebViewLoginDataSource", "🔄 Reintentando después de cargar QvaPay...")
                        applyToOfferWithRetryOld(offerId, onSuccess, onError, retryCount + 1)
                    }, 3000)
                }
                return
            }
            
            // Ejecutar JavaScript si el WebView tiene URL válida
            executeJavaScriptWithValidation(webView, javascript, offerId, onSuccess, onError)

        } catch (e: Exception) {
            Log.e("WebViewLoginDataSource", "Error al aplicar a oferta en segundo plano", e)
            onError("Error interno: ${e.message}")
        }
    }
    
    private fun executeJavaScriptWithValidation(
        webView: WebView, 
        javascript: String, 
        offerId: String,
        onSuccess: (String) -> Unit, 
        onError: (String) -> Unit
    ) {
        try {
            Log.d("WebViewLoginDataSource", "🚀 Ejecutando JavaScript para oferta: $offerId")
            Log.d("WebViewLoginDataSource", "📍 WebView URL at execution time: ${webView.url}")
            
            // Ejecutar JavaScript directamente (sin webView.post que no funciona en WebView invisible)
            try {
                // Primero verificar que AndroidInterface esté disponible
                webView.evaluateJavascript("typeof window.AndroidInterface") { interfaceCheck ->
                    Log.d("WebViewLoginDataSource", "🔍 AndroidInterface check: $interfaceCheck")
                    
                    if (interfaceCheck == "\"undefined\"" || interfaceCheck == "undefined") {
                        Log.e("WebViewLoginDataSource", "❌ AndroidInterface not available!")
                        currentApplyErrorCallback?.invoke("AndroidInterface no está disponible en el WebView")
                        clearApplyCallbacks()
                        return@evaluateJavascript
                    }
                    
                    // Ejecutar el JavaScript principal
                    webView.evaluateJavascript(javascript) { result ->
                        Log.d("WebViewLoginDataSource", "📄 Background JavaScript executed, result: $result")
                        
                        // Verificar si hubo error inmediato en el JavaScript
                        if (result != null && result.contains("error", ignoreCase = true)) {
                            Log.e("WebViewLoginDataSource", "❌ Immediate JavaScript error: $result")
                            currentApplyErrorCallback?.invoke("Error en JavaScript: $result")
                            clearApplyCallbacks()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebViewLoginDataSource", "❌ Error ejecutando JavaScript directamente", e)
                currentApplyErrorCallback?.invoke("❌ Error ejecutando JavaScript: ${e.message}")
                clearApplyCallbacks()
            }

        } catch (e: Exception) {
            Log.e("WebViewLoginDataSource", "❌ Error ejecutando JavaScript", e)
            onError("Error interno: ${e.message}")
        }
    }

    private var currentApplySuccessCallback: ((String) -> Unit)? = null
    private var currentApplyErrorCallback: ((String) -> Unit)? = null

    private fun setupJavaScriptInterface(webView: WebView) {
        Log.d("WebViewLoginDataSource", "🔗 Adding JavaScript interface 'AndroidInterface'...")
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onApplySuccess(message: String) {
                Log.d("WebViewLoginDataSource", "✅ JavaScript success callback: $message")
                currentApplySuccessCallback?.invoke(message)
                clearApplyCallbacks()
            }

            @JavascriptInterface
            fun onApplyError(message: String) {
                Log.e("WebViewLoginDataSource", "❌ JavaScript error callback: $message")
                currentApplyErrorCallback?.invoke(message)
                clearApplyCallbacks()
            }
            
            @JavascriptInterface
            fun onApplyInfo(message: String) {
                Log.d("WebViewLoginDataSource", "ℹ️ JavaScript info: $message")
                // Info messages don't trigger callbacks - just log for debugging
            }
        }, "AndroidInterface")
        Log.d("WebViewLoginDataSource", "✅ JavaScript interface 'AndroidInterface' added successfully")
    }

    private fun clearApplyCallbacks() {
        currentApplySuccessCallback = null
        currentApplyErrorCallback = null
    }
}