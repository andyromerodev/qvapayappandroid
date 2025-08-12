package com.example.qvapayappandroid.data.throttling

import android.util.Log
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

    companion object {
        private const val TAG = "ThrottlingManager"
    }

    private val mutex = Mutex()
    private val operationConfigs = ConcurrentHashMap<String, ThrottlingConfig>()
    private val lastExecutionTimes = ConcurrentHashMap<String, Long>()
    private val executionHistory = ConcurrentHashMap<String, MutableList<Long>>()

    override suspend fun canExecute(operationKey: String): ThrottlingResult = mutex.withLock {
        Log.d(TAG, "🔍 canExecute() - operationKey: '$operationKey'")

        val config = operationConfigs[operationKey] ?: ThrottlingConfig.DEFAULT_API_CONFIG
        Log.d(TAG, "⚙️ Config for '$operationKey': intervalMs=${config.intervalMs}, enabled=${config.enabled}, maxExecutions=${config.maxExecutionsPerWindow}")

        if (!config.enabled) {
            Log.d(TAG, "✅ Throttling DISABLED for '$operationKey' - ALLOWED immediately")
            return ThrottlingResult.allowed()
        }

        val currentTime = System.currentTimeMillis()
        val lastExecution = lastExecutionTimes[operationKey] ?: 0L
        val timeSinceLastExecution = if (lastExecution > 0) currentTime - lastExecution else -1L

        Log.d(TAG, "⏱️ Time analysis for '$operationKey':")
        Log.d(TAG, "   • Current time: $currentTime")
        Log.d(TAG, "   • Last execution: $lastExecution")
        Log.d(TAG, "   • Time since last execution: ${if (timeSinceLastExecution >= 0) "${timeSinceLastExecution}ms" else "NEVER"}")

        // Verificar throttling por intervalo simple
        val intervalResult = checkIntervalThrottling(operationKey, currentTime, config)
        if (!intervalResult.canExecute) {
            Log.d(TAG, "❌ BLOCKED by interval throttling - ${intervalResult.reason}")
            Log.d(TAG, "⏳ Remaining time: ${intervalResult.remainingTimeMs}ms (${intervalResult.remainingTimeMs/1000.0}s)")
            return intervalResult
        }

        // Verificar rate limiting por ventana de tiempo (si está configurado)
        config.maxExecutionsPerWindow?.let { maxExecutions ->
            Log.d(TAG, "🪟 Checking window throttling - max $maxExecutions executions per ${config.windowSizeMs}ms")
            val windowResult = checkWindowThrottling(operationKey, currentTime, config, maxExecutions)
            if (!windowResult.canExecute) {
                Log.d(TAG, "❌ BLOCKED by window throttling - ${windowResult.reason}")
                Log.d(TAG, "⏳ Remaining time: ${windowResult.remainingTimeMs}ms (${windowResult.remainingTimeMs/1000.0}s)")
                return windowResult
            }
        }

        Log.d(TAG, "✅ '$operationKey' - ALLOWED to execute")
        return ThrottlingResult.allowed()
    }

    private fun checkIntervalThrottling(
        operationKey: String,
        currentTime: Long,
        config: ThrottlingConfig
    ): ThrottlingResult {
        val lastExecution = lastExecutionTimes[operationKey] ?: 0L
        val timeSinceLastExecution = currentTime - lastExecution

        Log.d(TAG, "📊 Interval throttling check for '$operationKey':")
        Log.d(TAG, "   • Required interval: ${config.intervalMs}ms")
        Log.d(TAG, "   • Time since last execution: ${timeSinceLastExecution}ms")

        return if (timeSinceLastExecution < config.intervalMs) {
            val remainingTime = config.intervalMs - timeSinceLastExecution
            Log.d(TAG, "   • Status: BLOCKED - need to wait ${remainingTime}ms more")
            ThrottlingResult.blocked(
                remainingTimeMs = remainingTime,
                reason = "Interval throttling: ${config.intervalMs}ms required between executions"
            )
        } else {
            Log.d(TAG, "   • Status: ALLOWED - sufficient time has passed")
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

        Log.d(TAG, "🪟 Window throttling check for '$operationKey':")
        Log.d(TAG, "   • Window size: ${windowSize}ms")
        Log.d(TAG, "   • Max executions per window: $maxExecutions")
        Log.d(TAG, "   • History before cleanup: ${history.size} executions")

        // Limpiar ejecuciones fuera de la ventana
        val sizeBefore = history.size
        history.removeAll { it < currentTime - windowSize }
        val sizeAfter = history.size

        Log.d(TAG, "   • History after cleanup: $sizeAfter executions (removed ${sizeBefore - sizeAfter} old)")
        Log.d(TAG, "   • Execution times in window: ${history.map { currentTime - it }.joinToString(", ") { "${it}ms ago" }}")

        return if (history.size >= maxExecutions) {
            val oldestInWindow = history.minOrNull() ?: currentTime
            val remainingTime = windowSize - (currentTime - oldestInWindow)
            Log.d(TAG, "   • Status: BLOCKED - reached max $maxExecutions executions")
            Log.d(TAG, "   • Oldest execution: ${currentTime - oldestInWindow}ms ago")
            Log.d(TAG, "   • Need to wait: ${remainingTime}ms more")
            ThrottlingResult.blocked(
                remainingTimeMs = remainingTime,
                reason = "Rate limit: max $maxExecutions executions per ${windowSize}ms window"
            )
        } else {
            Log.d(TAG, "   • Status: ALLOWED - ${history.size}/$maxExecutions executions used")
            ThrottlingResult.allowed()
        }
    }

    override suspend fun recordExecution(operationKey: String) = mutex.withLock {
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "📝 recordExecution() - operationKey: '$operationKey'")
        Log.d(TAG, "   • Execution time: $currentTime")

        // Registrar tiempo de última ejecución
        val previousTime = lastExecutionTimes[operationKey]
        lastExecutionTimes[operationKey] = currentTime

        Log.d(TAG, "   • Previous execution: ${previousTime ?: "NONE"}")
        if (previousTime != null) {
            Log.d(TAG, "   • Time between executions: ${currentTime - previousTime}ms")
        }

        // Agregar a historial si hay configuración de ventana
        val config = operationConfigs[operationKey]
        if (config?.maxExecutionsPerWindow != null) {
            val history = executionHistory.getOrPut(operationKey) { mutableListOf() }
            val sizeBefore = history.size
            history.add(currentTime)

            Log.d(TAG, "   • Added to execution history (size: ${sizeBefore} -> ${history.size})")

            // Mantener solo las ejecuciones dentro de la ventana
            config.windowSizeMs?.let { windowSize ->
                val sizeBeforeCleanup = history.size
                history.removeAll { it < currentTime - windowSize }
                Log.d(TAG, "   • Cleaned old executions: ${sizeBeforeCleanup} -> ${history.size} (window: ${windowSize}ms)")
            }
        } else {
            Log.d(TAG, "   • No window config - execution not added to history")
        }

        Log.d(TAG, "✅ Execution recorded successfully for '$operationKey'")
    }

    override suspend fun configureOperation(operationKey: String, config: ThrottlingConfig) {
        Log.d(TAG, "🔧 configureOperation() - operationKey: '$operationKey'")
        Log.d(TAG, "   • Config: intervalMs=${config.intervalMs}, enabled=${config.enabled}")
        Log.d(TAG, "   • Window: maxExecutions=${config.maxExecutionsPerWindow}, windowSize=${config.windowSizeMs}ms")

        operationConfigs[operationKey] = config
        Log.d(TAG, "✅ Configuration saved for '$operationKey'")
    }

    override suspend fun getRemainingTime(operationKey: String): Long = mutex.withLock {
        Log.d(TAG, "⏳ getRemainingTime() - operationKey: '$operationKey'")

        val config = operationConfigs[operationKey] ?: ThrottlingConfig.DEFAULT_API_CONFIG

        if (!config.enabled) {
            Log.d(TAG, "   • Throttling disabled - remaining time: 0ms")
            return 0L
        }

        val currentTime = System.currentTimeMillis()
        val lastExecution = lastExecutionTimes[operationKey] ?: 0L
        val timeSinceLastExecution = currentTime - lastExecution
        val remainingTime = maxOf(0L, config.intervalMs - timeSinceLastExecution)

        Log.d(TAG, "   • Last execution: $lastExecution")
        Log.d(TAG, "   • Time since last: ${timeSinceLastExecution}ms")
        Log.d(TAG, "   • Required interval: ${config.intervalMs}ms")
        Log.d(TAG, "   • Remaining time: ${remainingTime}ms")

        return remainingTime
    }

    override suspend fun clearThrottling(operationKey: String) {
        mutex.withLock {
            Log.d(TAG, "🧹 clearThrottling() - operationKey: '$operationKey'")

            val hadLastExecution = lastExecutionTimes.containsKey(operationKey)
            val hadHistory = executionHistory.containsKey(operationKey)

            lastExecutionTimes.remove(operationKey)
            executionHistory.remove(operationKey)

            Log.d(TAG, "   • Cleared last execution time: $hadLastExecution")
            Log.d(TAG, "   • Cleared execution history: $hadHistory")
            Log.d(TAG, "✅ Throttling cleared for '$operationKey'")
        }
    }

    override suspend fun clearAllThrottling() {
        mutex.withLock {
            Log.d(TAG, "🧹 clearAllThrottling()")

            val executionCount = lastExecutionTimes.size
            val historyCount = executionHistory.size

            lastExecutionTimes.clear()
            executionHistory.clear()

            Log.d(TAG, "   • Cleared $executionCount execution times")
            Log.d(TAG, "   • Cleared $historyCount execution histories")
            Log.d(TAG, "✅ All throttling cleared")
        }
    }
}