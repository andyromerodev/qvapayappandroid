package com.example.qvapayappandroid.data.throttling

import com.example.qvapayappandroid.domain.throttling.ThrottlingConfig
import com.example.qvapayappandroid.domain.throttling.ThrottlingManager
import com.example.qvapayappandroid.domain.throttling.ThrottlingResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementación del ThrottlingManager que maneja throttling en memoria
 * 
 * Características:
 * - Thread-safe usando Mutex y ConcurrentHashMap
 * - Soporte para throttling simple por intervalo
 * - Soporte para rate limiting por ventana de tiempo
 * - Configuración dinámica por operación
 */
class ThrottlingManagerImpl : ThrottlingManager {
    
    private val mutex = Mutex()
    private val operationConfigs = ConcurrentHashMap<String, ThrottlingConfig>()
    private val lastExecutionTimes = ConcurrentHashMap<String, Long>()
    private val executionHistory = ConcurrentHashMap<String, MutableList<Long>>()
    
    override suspend fun canExecute(operationKey: String): ThrottlingResult = mutex.withLock {
        val config = operationConfigs[operationKey] ?: ThrottlingConfig.DEFAULT_API_CONFIG
        
        if (!config.enabled) {
            return ThrottlingResult.allowed()
        }
        
        val currentTime = System.currentTimeMillis()
        
        // Verificar throttling por intervalo simple
        val intervalResult = checkIntervalThrottling(operationKey, currentTime, config)
        if (!intervalResult.canExecute) {
            return intervalResult
        }
        
        // Verificar rate limiting por ventana de tiempo (si está configurado)
        config.maxExecutionsPerWindow?.let { maxExecutions ->
            val windowResult = checkWindowThrottling(operationKey, currentTime, config, maxExecutions)
            if (!windowResult.canExecute) {
                return windowResult
            }
        }
        
        return ThrottlingResult.allowed()
    }
    
    private fun checkIntervalThrottling(
        operationKey: String, 
        currentTime: Long, 
        config: ThrottlingConfig
    ): ThrottlingResult {
        val lastExecution = lastExecutionTimes[operationKey] ?: 0L
        val timeSinceLastExecution = currentTime - lastExecution
        
        return if (timeSinceLastExecution < config.intervalMs) {
            val remainingTime = config.intervalMs - timeSinceLastExecution
            ThrottlingResult.blocked(
                remainingTimeMs = remainingTime,
                reason = "Interval throttling: ${config.intervalMs}ms required between executions"
            )
        } else {
            ThrottlingResult.allowed()
        }
    }
    
    private fun checkWindowThrottling(
        operationKey: String,
        currentTime: Long,
        config: ThrottlingConfig,
        maxExecutions: Int
    ): ThrottlingResult {
        val windowSize = config.windowSizeMs ?: return ThrottlingResult.allowed()
        val history = executionHistory.getOrPut(operationKey) { mutableListOf() }
        
        // Limpiar ejecuciones fuera de la ventana
        history.removeAll { it < currentTime - windowSize }
        
        return if (history.size >= maxExecutions) {
            val oldestInWindow = history.minOrNull() ?: currentTime
            val remainingTime = windowSize - (currentTime - oldestInWindow)
            ThrottlingResult.blocked(
                remainingTimeMs = remainingTime,
                reason = "Rate limit: max $maxExecutions executions per ${windowSize}ms window"
            )
        } else {
            ThrottlingResult.allowed()
        }
    }
    
    override suspend fun recordExecution(operationKey: String) = mutex.withLock {
        val currentTime = System.currentTimeMillis()
        
        // Registrar tiempo de última ejecución
        lastExecutionTimes[operationKey] = currentTime
        
        // Agregar a historial si hay configuración de ventana
        val config = operationConfigs[operationKey]
        if (config?.maxExecutionsPerWindow != null) {
            val history = executionHistory.getOrPut(operationKey) { mutableListOf() }
            history.add(currentTime)
            
            // Mantener solo las ejecuciones dentro de la ventana
            config.windowSizeMs?.let { windowSize ->
                history.removeAll { it < currentTime - windowSize }
            }
        }
    }
    
    override suspend fun configureOperation(operationKey: String, config: ThrottlingConfig) {
        operationConfigs[operationKey] = config
    }
    
    override suspend fun getRemainingTime(operationKey: String): Long = mutex.withLock {
        val config = operationConfigs[operationKey] ?: ThrottlingConfig.DEFAULT_API_CONFIG
        
        if (!config.enabled) return 0L
        
        val currentTime = System.currentTimeMillis()
        val lastExecution = lastExecutionTimes[operationKey] ?: 0L
        val timeSinceLastExecution = currentTime - lastExecution
        
        return maxOf(0L, config.intervalMs - timeSinceLastExecution)
    }
    
    override suspend fun clearThrottling(operationKey: String) {
        mutex.withLock {
            lastExecutionTimes.remove(operationKey)
            executionHistory.remove(operationKey)
        }
    }
    
    override suspend fun clearAllThrottling() {
        mutex.withLock {
            lastExecutionTimes.clear()
            executionHistory.clear()
        }
    }
}