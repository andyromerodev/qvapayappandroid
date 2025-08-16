package com.example.qvapayappandroid.domain.throttling

/**
 * Resultado de verificación de throttling
 * 
 * @param canExecute Si la operación puede ejecutarse ahora
 * @param remainingTimeMs Tiempo restante en milisegundos hasta que pueda ejecutarse
 * @param reason Razón por la cual no puede ejecutarse (si aplica)
 */
data class ThrottlingResult(
    val canExecute: Boolean,
    val remainingTimeMs: Long = 0L,
    val reason: String? = null
) {
    companion object {
        fun allowed() = ThrottlingResult(canExecute = true)
        
        fun blocked(remainingTimeMs: Long, reason: String? = null) = ThrottlingResult(
            canExecute = false,
            remainingTimeMs = remainingTimeMs,
            reason = reason
        )
    }
    
    /**
     * Tiempo restante en segundos (redondeado hacia arriba)
     */
    val remainingTimeSeconds: Int
        get() = if (remainingTimeMs <= 0) 0 else ((remainingTimeMs + 999) / 1000).toInt()
}