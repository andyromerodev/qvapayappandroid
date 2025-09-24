package com.example.qvapayappandroid.domain.throttling

/**
 * Result returned by a throttling check.
 *
 * @param canExecute Whether the operation can run right now
 * @param remainingTimeMs Remaining time in milliseconds until it can run
 * @param reason Optional reason stating why it cannot run yet
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
     * Remaining time expressed in seconds (rounded up).
     */
    val remainingTimeSeconds: Int
        get() = if (remainingTimeMs <= 0) 0 else ((remainingTimeMs + 999) / 1000).toInt()
}
