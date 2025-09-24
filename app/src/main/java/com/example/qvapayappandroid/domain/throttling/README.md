# Sistema de Throttling - Clean Architecture

Este sistema de throttling sigue los principios SOLID y Clean Architecture para proporcionar un control centralizado y reutilizable de límites de peticiones en toda la aplicación.

## Arquitectura

### Domain Layer
- **ThrottlingManager**: Interfaz principal (Dependency Inversion Principle)
- **ThrottlingConfig**: Configuración de throttling
- **ThrottlingResult**: Resultado de verificación
- **ThrottlingOperations**: Constantes para operaciones
- **ThrottlingExtensions**: Functions de extensión para facilitar uso

### Data Layer  
- **ThrottlingManagerImpl**: Implementación concreta thread-safe

## Principios SOLID Aplicados

1. **Single Responsibility**: Cada clase tiene una única responsabilidad
2. **Open/Closed**: Extensible para nuevos tipos de throttling
3. **Liskov Substitution**: Cualquier implementación del manager es intercambiable
4. **Interface Segregation**: Interfaces específicas y cohesivas
5. **Dependency Inversion**: Dependencias en abstracciones, no concreciones

## Uso Básico

### En DataSources
```kotlin
class MyDataSource(
    private val httpClient: HttpClient,
    private val throttlingManager: ThrottlingManager
) {
    
    suspend fun getData(): Result<Data> {
        return try {
            val throttlingResult = throttlingManager.canExecute("my_operation")
            
            if (!throttlingResult.canExecute) {
                delay(throttlingResult.remainingTimeMs)
            }
            
            throttlingManager.recordExecution("my_operation")
            
            // Perform the HTTP request
            val response = httpClient.get("url")
            Result.success(response.body())
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### En ViewModels (con extension functions)
```kotlin
class MyViewModel(
    private val throttlingManager: ThrottlingManager
) : ViewModel() {
    
    fun performOperation() {
        executeWithThrottling(
            throttlingManager = throttlingManager,
            operationKey = ThrottlingOperations.MY_OPERATION,
            onThrottled = { result ->
                // Surface a wait message to the user
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Espera ${result.remainingTimeSeconds} segundos"
                )
            }
        ) {
            // Execute the operation
            doSomething()
        }
    }
}
```

## Configuraciones Predefinidas

```kotlin
// Simple configuration (interval based)
ThrottlingConfig(intervalMs = 5000L) // 5 segundos

// Configuration using rate limiting
ThrottlingConfig(
    intervalMs = 1000L,
    maxExecutionsPerWindow = 10,
    windowSizeMs = 60000L // 10 requests per minute
)

// Configuraciones predefinidas
ThrottlingConfig.DEFAULT_API_CONFIG     // 1 segundo
ThrottlingConfig.HEAVY_API_CONFIG       // 5 segundos  
ThrottlingConfig.CREATE_OPERATIONS_CONFIG // 10 segundos
ThrottlingConfig.DISABLED_CONFIG        // Sin throttling
```

## Configuración en DI

```kotlin
val dataModule = module {
    single<ThrottlingManager> { ThrottlingManagerImpl() }
    
    single<MyDataSource> { MyDataSourceImpl(get(), get()) }
}
```

## Operaciones Predefinidas

Usa las constantes en `ThrottlingOperations` para mantener consistencia:

- `P2P_CREATE_OFFER`: Crear ofertas P2P
- `P2P_GET_OFFERS`: Obtener listado de ofertas
- `P2P_APPLY_TO_OFFER`: Aplicar a ofertas
- `AUTH_LOGIN`: Login de usuario
- Y muchas más...

## Ventajas del Sistema

1. **Centralizado**: Una sola fuente de verdad para throttling
2. **Configurable**: Diferentes reglas por operación
3. **Thread-safe**: Uso seguro en corrutinas concurrentes
4. **Extensible**: Fácil agregar nuevos tipos de throttling
5. **Testeable**: Interfaces permiten mocking fácil
6. **Reutilizable**: Usar en cualquier parte de la app
7. **Clean Architecture**: Separación clara de responsabilidades

## Ejemplo Completo: P2P Create Offer

El sistema ya está integrado en `P2PDataSourceImpl` con configuración automática de:
- 10 segundos para crear ofertas
- 5 segundos para obtener listados
- 2 segundos para obtener detalles
- 3 segundos para mis ofertas

Esto garantiza que el backend no se sobrecargue y proporciona una experiencia de usuario consistente.
