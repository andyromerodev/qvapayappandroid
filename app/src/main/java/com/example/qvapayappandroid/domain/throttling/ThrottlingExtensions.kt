package com.example.qvapayappandroid.domain.throttling

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Extension functions para facilitar el uso del ThrottlingManager en ViewModels
 * siguiendo principios de Clean Architecture y SOLID
 */

private const val TAG = "ThrottlingExt"

/**
 * Extension function para verificar throttling antes de ejecutar una operación
 * 
 * @param throttlingManager El manager de throttling inyectado
 * @param operationKey Clave única que identifica la operación
 * @param onThrottled Callback ejecutado si la operación está throttled
 * @param onAllowed Callback ejecutado si la operación puede proceder
 */
suspend fun ThrottlingManager.executeWithThrottling(
    operationKey: String,
    onThrottled: (ThrottlingResult) -> Unit = {},
    onAllowed: suspend () -> Unit
) {
    Log.d(TAG, "🚀 executeWithThrottling() - operationKey: '$operationKey'")
    
    val result = canExecute(operationKey)
    
    if (result.canExecute) {
        Log.d(TAG, "✅ Operation ALLOWED - executing and recording")
        recordExecution(operationKey)
        try {
            onAllowed()
            Log.d(TAG, "✅ Operation completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Operation failed with exception: ${e.message}", e)
            throw e
        }
    } else {
        Log.d(TAG, "❌ Operation THROTTLED - calling onThrottled callback")
        Log.d(TAG, "   • Remaining time: ${result.remainingTimeMs}ms")
        Log.d(TAG, "   • Reason: ${result.reason}")
        onThrottled(result)
    }
}

/**
 * Extension function para ViewModels que facilita el manejo de throttling
 * con manejo automático de corrutinas
 * 
 * @param throttlingManager El manager de throttling inyectado
 * @param operationKey Clave única que identifica la operación
 * @param onThrottled Callback ejecutado si la operación está throttled
 * @param operation Operación a ejecutar si no está throttled
 */
fun ViewModel.executeWithThrottling(
    throttlingManager: ThrottlingManager,
    operationKey: String,
    onThrottled: (ThrottlingResult) -> Unit = {},
    operation: suspend () -> Unit
) {
    Log.d(TAG, "🎯 ViewModel.executeWithThrottling() - operationKey: '$operationKey'")
    Log.d(TAG, "   • Launching in viewModelScope")
    
    viewModelScope.launch {
        try {
            throttlingManager.executeWithThrottling(
                operationKey = operationKey,
                onThrottled = { result ->
                    Log.d(TAG, "🔄 ViewModel throttled callback - remaining: ${result.remainingTimeMs}ms")
                    onThrottled(result)
                },
                onAllowed = {
                    Log.d(TAG, "🔄 ViewModel executing operation")
                    operation()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ ViewModel operation failed: ${e.message}", e)
        }
    }
}

/**
 * Extension function para obtener información de throttling de una operación
 * de forma más conveniente
 * 
 * @param operationKey Clave única que identifica la operación
 * @return ThrottlingInfo con información detallada
 */
suspend fun ThrottlingManager.getThrottlingInfo(operationKey: String): ThrottlingInfo {
    Log.d(TAG, "ℹ️ getThrottlingInfo() - operationKey: '$operationKey'")
    
    val result = canExecute(operationKey)
    val remainingTime = getRemainingTime(operationKey)
    
    val info = ThrottlingInfo(
        operationKey = operationKey,
        canExecute = result.canExecute,
        remainingTimeMs = remainingTime,
        remainingTimeSeconds = result.remainingTimeSeconds,
        reason = result.reason
    )
    
    Log.d(TAG, "   • Can execute: ${info.canExecute}")
    Log.d(TAG, "   • Remaining time: ${info.remainingTimeMs}ms (${info.remainingTimeSeconds}s)")
    Log.d(TAG, "   • Reason: ${info.reason ?: "N/A"}")
    
    return info
}

/**
 * Data class con información detallada de throttling para una operación
 */
data class ThrottlingInfo(
    val operationKey: String,
    val canExecute: Boolean,
    val remainingTimeMs: Long,
    val remainingTimeSeconds: Int,
    val reason: String?
)

/**
 * Extension function para configurar múltiples operaciones de throttling de una vez
 * 
 * @param configurations Map de operationKey a ThrottlingConfig
 */
suspend fun ThrottlingManager.configureOperations(
    configurations: Map<String, ThrottlingConfig>
) {
    Log.d(TAG, "⚙️ configureOperations() - configuring ${configurations.size} operations")
    
    configurations.forEach { (operationKey, config) ->
        Log.d(TAG, "   • Configuring '$operationKey': interval=${config.intervalMs}ms, enabled=${config.enabled}")
        configureOperation(operationKey, config)
    }
    
    Log.d(TAG, "✅ All operations configured successfully")
}

/**
 * Extension function para obtener configuraciones predefinidas para P2P
 */
fun ThrottlingManager.getP2PConfigurations(): Map<String, ThrottlingConfig> {
    Log.d(TAG, "📋 getP2PConfigurations() - generating P2P throttling configs")
    
    val configs = mapOf(
        ThrottlingOperations.P2P_GET_OFFERS to ThrottlingConfig(intervalMs = 5000L),
        ThrottlingOperations.P2P_GET_OFFER_BY_ID to ThrottlingConfig(intervalMs = 2000L),
        ThrottlingOperations.P2P_CREATE_OFFER to ThrottlingConfig.CREATE_OPERATIONS_CONFIG,
        ThrottlingOperations.P2P_APPLY_TO_OFFER to ThrottlingConfig.CREATE_OPERATIONS_CONFIG,
        ThrottlingOperations.P2P_CANCEL_OFFER to ThrottlingConfig(intervalMs = 5000L),
        ThrottlingOperations.P2P_GET_MY_OFFERS to ThrottlingConfig(intervalMs = 3000L)
    )
    
    Log.d(TAG, "   • Generated ${configs.size} P2P configurations")
    configs.forEach { (key, config) ->
        Log.d(TAG, "     - $key: ${config.intervalMs}ms")
    }
    
    return configs
}