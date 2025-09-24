package com.example.qvapayappandroid.domain.throttling

/**
 * Interface that encapsulates request throttling, following SOLID principles.
 *
 * Applied principles:
 * - Single Responsibility: only manages throttling concerns
 * - Open/Closed: open to different throttling strategies
 * - Liskov Substitution: any implementation can be swapped in
 * - Interface Segregation: dedicated contract just for throttling
 * - Dependency Inversion: callers depend on abstractions, not concrete types
 */
interface ThrottlingManager {
    
    /**
     * Checks whether an operation is allowed to run according to throttling rules.
     *
     * @param operationKey Unique key that identifies the operation type
     * @return ThrottlingResult containing the decision and remaining wait time (if any)
     */
    suspend fun canExecute(operationKey: String): ThrottlingResult
    
    /**
     * Records that an operation has been executed.
     *
     * @param operationKey Unique key that identifies the operation type
     */
    suspend fun recordExecution(operationKey: String)
    
    /**
     * Configures throttling rules for a specific operation.
     *
     * @param operationKey Unique key that identifies the operation type
     * @param config Throttling settings for that operation
     */
    suspend fun configureOperation(operationKey: String, config: ThrottlingConfig)
    
    /**
     * Returns the remaining time until the operation can be executed again.
     *
     * @param operationKey Unique key that identifies the operation type
     * @return Remaining time in milliseconds, or 0 when it may run immediately
     */
    suspend fun getRemainingTime(operationKey: String): Long
    
    /**
     * Clears throttling history for a specific operation.
     *
     * @param operationKey Unique key that identifies the operation type
     */
    suspend fun clearThrottling(operationKey: String)
    
    /** Clears all throttling history. */
    suspend fun clearAllThrottling()
    
    /**
     * Checks global API throttling to see if any operation may run.
     * Useful when the server enforces global limits independent of individual operations.
     *
     * @return ThrottlingResult containing the decision and remaining wait time (if any)
     */
    suspend fun canExecuteGlobalApi(): ThrottlingResult
    
    /** Records that any API operation was executed (for global throttling). */
    suspend fun recordGlobalApiExecution()

    /**
     * Configures throttling rules applied globally to all API calls.
     *
     * @param config Global throttling configuration
     */
    suspend fun configureGlobalApi(config: ThrottlingConfig)
}
