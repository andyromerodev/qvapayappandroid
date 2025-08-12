package com.example.qvapayappandroid.domain.throttling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Extension functions para facilitar el uso del ThrottlingManager en ViewModels
 * siguiendo principios de Clean Architecture y SOLID
 */

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
    val result = canExecute(operationKey)
    
    if (result.canExecute) {
        recordExecution(operationKey)
        onAllowed()
    } else {
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
    viewModelScope.launch {
        throttlingManager.executeWithThrottling(
            operationKey = operationKey,
            onThrottled = onThrottled,
            onAllowed = operation
        )
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
    val result = canExecute(operationKey)
    val remainingTime = getRemainingTime(operationKey)
    
    return ThrottlingInfo(
        operationKey = operationKey,
        canExecute = result.canExecute,
        remainingTimeMs = remainingTime,
        remainingTimeSeconds = result.remainingTimeSeconds,
        reason = result.reason
    )
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
    configurations.forEach { (operationKey, config) ->
        configureOperation(operationKey, config)
    }
}

/**
 * Extension function para obtener configuraciones predefinidas para P2P
 */
fun ThrottlingManager.getP2PConfigurations(): Map<String, ThrottlingConfig> {
    return mapOf(
        ThrottlingOperations.P2P_GET_OFFERS to ThrottlingConfig(intervalMs = 5000L),
        ThrottlingOperations.P2P_GET_OFFER_BY_ID to ThrottlingConfig(intervalMs = 2000L),
        ThrottlingOperations.P2P_CREATE_OFFER to ThrottlingConfig.CREATE_OPERATIONS_CONFIG,
        ThrottlingOperations.P2P_APPLY_TO_OFFER to ThrottlingConfig.CREATE_OPERATIONS_CONFIG,
        ThrottlingOperations.P2P_CANCEL_OFFER to ThrottlingConfig(intervalMs = 5000L),
        ThrottlingOperations.P2P_GET_MY_OFFERS to ThrottlingConfig(intervalMs = 3000L)
    )
}