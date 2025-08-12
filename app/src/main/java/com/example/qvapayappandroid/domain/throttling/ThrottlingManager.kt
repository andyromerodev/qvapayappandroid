package com.example.qvapayappandroid.domain.throttling

/**
 * Interfaz para manejar throttling de peticiones según principios SOLID
 * 
 * Principios aplicados:
 * - Single Responsibility: Solo se encarga del throttling
 * - Open/Closed: Extensible para diferentes tipos de throttling
 * - Liskov Substitution: Cualquier implementación puede ser intercambiada
 * - Interface Segregation: Interface específica para throttling
 * - Dependency Inversion: Dependencias dependen de abstracciones
 */
interface ThrottlingManager {
    
    /**
     * Verifica si una operación puede ejecutarse según las reglas de throttling
     * 
     * @param operationKey Clave única que identifica el tipo de operación
     * @return ThrottlingResult con información sobre si puede ejecutarse y tiempo restante
     */
    suspend fun canExecute(operationKey: String): ThrottlingResult
    
    /**
     * Registra que una operación se ha ejecutado
     * 
     * @param operationKey Clave única que identifica el tipo de operación
     */
    suspend fun recordExecution(operationKey: String): Int
    
    /**
     * Configura las reglas de throttling para una operación específica
     * 
     * @param operationKey Clave única que identifica el tipo de operación
     * @param config Configuración de throttling para esta operación
     */
    suspend fun configureOperation(operationKey: String, config: ThrottlingConfig)
    
    /**
     * Obtiene el tiempo restante hasta que una operación pueda ejecutarse nuevamente
     * 
     * @param operationKey Clave única que identifica el tipo de operación
     * @return Tiempo restante en milisegundos, 0 si puede ejecutarse inmediatamente
     */
    suspend fun getRemainingTime(operationKey: String): Long
    
    /**
     * Limpia el historial de throttling para una operación específica
     * 
     * @param operationKey Clave única que identifica el tipo de operación
     */
    suspend fun clearThrottling(operationKey: String)
    
    /**
     * Limpia todo el historial de throttling
     */
    suspend fun clearAllThrottling()
}