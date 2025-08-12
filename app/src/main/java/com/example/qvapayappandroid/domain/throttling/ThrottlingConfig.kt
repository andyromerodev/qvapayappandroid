package com.example.qvapayappandroid.domain.throttling

/**
 * Configuración para throttling de operaciones
 * 
 * @param intervalMs Intervalo mínimo entre ejecuciones en milisegundos
 * @param maxExecutionsPerWindow Número máximo de ejecuciones por ventana de tiempo (opcional)
 * @param windowSizeMs Tamaño de la ventana de tiempo en milisegundos (usado con maxExecutionsPerWindow)
 * @param enabled Si el throttling está habilitado para esta operación
 */
data class ThrottlingConfig(
    val intervalMs: Long,
    val maxExecutionsPerWindow: Int? = null,
    val windowSizeMs: Long? = null,
    val enabled: Boolean = true
) {
    companion object {
        /**
         * Configuraciones predefinidas para casos comunes
         */
        val DEFAULT_API_CONFIG = ThrottlingConfig(intervalMs = 1000L) // 1 segundo
        val HEAVY_API_CONFIG = ThrottlingConfig(intervalMs = 5000L) // 5 segundos
        val CREATE_OPERATIONS_CONFIG = ThrottlingConfig(intervalMs = 10000L) // 10 segundos
        val RATE_LIMITED_CONFIG = ThrottlingConfig(
            intervalMs = 1000L,
            maxExecutionsPerWindow = 10,
            windowSizeMs = 60000L // 10 requests per minute
        )
        val DISABLED_CONFIG = ThrottlingConfig(intervalMs = 0L, enabled = false)
    }
    
    init {
        require(intervalMs >= 0) { "Interval must be non-negative" }
        require(maxExecutionsPerWindow == null || maxExecutionsPerWindow > 0) { 
            "Max executions per window must be positive" 
        }
        require(windowSizeMs == null || windowSizeMs > 0) { 
            "Window size must be positive" 
        }
        require((maxExecutionsPerWindow == null) == (windowSizeMs == null)) {
            "Both maxExecutionsPerWindow and windowSizeMs must be specified together or both null"
        }
    }
}