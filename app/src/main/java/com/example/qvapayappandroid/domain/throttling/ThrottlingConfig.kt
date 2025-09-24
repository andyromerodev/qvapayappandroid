package com.example.qvapayappandroid.domain.throttling

/**
 * Configuration object for throttling operations.
 *
 * @param intervalMs Minimum interval between executions in milliseconds
 * @param maxExecutionsPerWindow Maximum executions per window (optional)
 * @param windowSizeMs Window size in milliseconds (used with maxExecutionsPerWindow)
 * @param enabled Whether throttling is enabled for this operation
 */
data class ThrottlingConfig(
    val intervalMs: Long,
    val maxExecutionsPerWindow: Int? = null,
    val windowSizeMs: Long? = null,
    val enabled: Boolean = true
) {
    companion object {
        /**
         * Predefined configurations for common scenarios
         */
        val DEFAULT_API_CONFIG = ThrottlingConfig(intervalMs = 15000L) // 15 seconds
        val HEAVY_API_CONFIG = ThrottlingConfig(intervalMs = 5000L) // 5 seconds
        val CREATE_OPERATIONS_CONFIG = ThrottlingConfig(intervalMs = 15000L) // 15 seconds
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
