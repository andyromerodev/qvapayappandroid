# Changelog - QvaPay Android App

## ✅ v2.0.0 - Sistema P2P Completo (2024-01-XX)

### 🎯 Funcionalidades P2P Implementadas

#### 📊 Arquitectura P2P con Clean Architecture
- **Data Layer**:
  - `P2PDataSource` interface y `P2PDataSourceImpl` con Ktor HTTP client
  - `P2PRepository` interface y `P2PRepositoryImpl` para gestión de ofertas
  - Modelos de datos: `P2POfferResponse`, `P2POffer`, `P2PFilterRequest`
  - Rate limiting automático (2 segundos mínimo entre peticiones)
  
- **Domain Layer**:
  - `GetP2POffersUseCase` para coordinar repositorios
  - Separación correcta: SessionRepository + P2PRepository
  - Arquitectura sin violaciones de dependencias

- **Presentation Layer**:
  - `P2PViewModel` con StateFlow/SharedFlow reactivo
  - `P2PScreen` completamente rediseñada con ofertas reales

#### 🔥 Funcionalidades de Usuario
- **Sistema de filtros avanzado**:
  - FilterChips para tipo de oferta (Todas/Compra/Venta)
  - Dropdown con 19 monedas soportadas: SOL, SBERBANK, BANK_CUP, ZELLE, TROPIPAY, ETECSA, USDCASH, CLASICA, BANK_MLC, NEOMOON, USDT, BANK_EUR, QVAPAY, BANDECPREPAGO, CUPCASH, WISE, EURCASH, USDTBSC, BOLSATM
  
- **Lista de ofertas P2P real**:
  - Cards con información detallada: tipo, moneda, monto, usuario
  - Badges para COMPRA/VENTA con colores distintivos
  - Indicadores KYC cuando aplica
  - LazyColumn para rendimiento optimizado

- **Paginación completa**:
  - Navegación anterior/siguiente con IconButtons
  - Indicador "Página X de Y"
  - 15 elementos por página para evitar rate limiting
  - Loading states que previenen múltiples peticiones

- **Gestión de errores robusta**:
  - Manejo de HTTP 429 "Too Many Attempts"
  - Campos opcionales para API responses incompletas
  - Estados de error con mensajes descriptivos y botón "Dismiss"

#### 🛡️ Optimizaciones de Rendimiento y Estabilidad
- **Rate Limiting multi-capa**:
  - Data Source: 2 segundos mínimo entre peticiones API
  - ViewModel: Debouncing de 300ms para prevenir calls rápidos
  - UI: Botones bloqueados durante loading states
  
- **Request management avanzado**:
  - Cancelación de peticiones pendientes con Job cancellation
  - Logging detallado para debugging de API calls
  - Manejo robusto de respuestas malformadas

- **Arquitectura resiliente**:
  - Todos los campos P2POffer opcionales con fallbacks "N/A"
  - JSON parsing tolerante a campos faltantes
  - Clean Architecture sin violaciones de dependencias

### 🔧 Dependencias y Configuración
- **Base de datos completa**:
  - Room entities: `UserEntity`, `SessionEntity`
  - DAOs: `UserDao`, `SessionDao` con operaciones CRUD
  - `AppDatabase` con singleton pattern

- **Session Management**:
  - `SessionRepository` para persistencia local
  - `SessionLocalDataSource` con Flow support
  - Use cases: `CheckSessionUseCase`, `GetCurrentUserUseCase`, `LogoutUseCase`

- **Dependency Injection actualizada**:
  - `DatabaseModule` para Room setup
  - Todos los módulos integrados: Network, Database, Data, Domain, Presentation

### 📁 Estructura de Archivos Agregados
```
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── SessionDao.kt
│   │   │   └── UserDao.kt
│   │   └── entities/
│   │       ├── SessionEntity.kt
│   │       └── UserEntity.kt
│   ├── datasource/
│   │   ├── P2PDataSource.kt
│   │   ├── P2PDataSourceImpl.kt (con rate limiting)
│   │   ├── SessionLocalDataSource.kt
│   │   └── SessionLocalDataSourceImpl.kt
│   ├── model/
│   │   └── P2POfferResponse.kt (modelos P2P)
│   └── repository/
│       ├── P2PRepositoryImpl.kt
│       └── SessionRepositoryImpl.kt
├── domain/
│   ├── repository/
│   │   ├── P2PRepository.kt
│   │   └── SessionRepository.kt
│   └── usecase/
│       ├── CheckSessionUseCase.kt
│       ├── GetCurrentUserUseCase.kt
│       ├── GetP2POffersUseCase.kt
│       └── LogoutUseCase.kt
├── di/
│   └── DatabaseModule.kt
└── CLAUDE.md (documentación del proyecto)
```

### 🐛 Bugs Corregidos
- **HTTP 429 "Too Many Attempts"**: Solucionado con rate limiting multi-capa
- **JsonConvertException**: Campos opcionales en modelos P2P
- **Multiple simultaneous requests**: Debouncing y job cancellation
- **UI crashes con datos incompletos**: Null safety y fallbacks

## 🚀 v2.1.0 - Sistema de Filtros P2P Avanzado (2025-01-XX)

### ✨ Refactorización Completa del Sistema P2P

#### 🔧 Separación de Componentes (SOLID Principles)
- **P2PScreen refactorizado** con separación de responsabilidades:
  - `P2PActionButton.kt` - Botón reutilizable para acciones P2P
  - `P2PFiltersCard.kt` - Card de filtros con toggles y dropdown
  - `P2POfferCard.kt` - Card clickeable para mostrar ofertas individuales
  - `P2PStatsCard.kt` - Card de estadísticas P2P
  - `P2POfferDetailScreen.kt` - Pantalla completa de detalles de oferta

#### 🎯 Sistema de Filtros Avanzado
- **Pantalla de filtros independiente** (`P2PFiltersScreen`):
  - Acceso mediante ícono de filtro en TopAppBar
  - **Toggles para múltiples monedas** en lugar de dropdown
  - **Filtrado manual** con botón "Filtrar Ofertas"
  - **Persistencia de selección** al navegar entre pantallas
  - Botón "Limpiar Filtros" para reset rápido
  
- **Navegación mejorada**:
  - Nueva ruta `P2PFilters` en `AppDestinations`
  - Navegación fluida entre P2P → Filtros → P2P
  - **Instancia compartida del ViewModel** para evitar cancelaciones

#### 🚀 Manejo de Múltiples Monedas
- **Peticiones paralelas** usando `async` y `awaitAll()`
- **Una petición por moneda seleccionada**:
  - ETECSA + BANK_CUP = 2 peticiones simultáneas
  - 3 monedas = 3 peticiones paralelas
- **Combinación y deduplicación** de resultados por UUID
- **Mejor rendimiento** con peticiones concurrentes

#### 📱 UI/UX Mejoradas
- **Cards de ofertas individuales**:
  - Elevación de 4dp con sombras sutiles
  - Bordes redondeados automáticos (Material3)
  - Color `surface` para mejor contraste
  - **Clickeables** para navegar a pantalla de detalles
  
- **Pantalla de detalles de oferta**:
  - Información completa del usuario (username, nombre, rating)
  - **Badges de verificación** (KYC, Gold, VIP)
  - **Mensaje del usuario** si está disponible
  - Botones de acción: "Contactar" y "Aceptar Oferta"
  
- **Paginación optimizada**:
  - **Siempre visible** en la parte inferior
  - **Menos separación** (8dp en lugar de 16dp)
  - Mejor lógica para casos edge (sin páginas, página única)

#### 🛠️ Modelos de Datos Actualizados
- **P2POffer completo** con todos los campos del JSON real:
  - `message`, `onlyVip`, `valid`, `coinData`, `owner`
  - **Modelo Owner** con información completa del usuario
  - **Modelo CoinData** con detalles técnicos de la moneda
  
- **P2PUiState extendido**:
  - `selectedCoins: List<String>` para múltiples monedas
  - Persistencia de filtros entre navegaciones

#### 🔄 ViewModel Mejorado
- **Método `applyFilters()`** para filtros múltiples
- **`loadP2PDataImmediate()`** sin debouncing para filtros
- **Manejo de instancia compartida** entre pantallas
- **Logging detallado** para debugging de múltiples peticiones

### 🐛 Bugs Corregidos
- **Job cancelation al navegar**: Instancia compartida del ViewModel evita cancelaciones
- **Filtros no persistentes**: LaunchedEffect sincroniza estado entre pantallas  
- **Una sola moneda en múltiples selecciones**: Peticiones paralelas por cada moneda
- **UI inconsistente**: Cards individuales con diseño unificado

### 📁 Archivos Nuevos
```
presentation/ui/p2p/
├── P2PFiltersScreen.kt (pantalla completa de filtros)
├── P2POfferDetailScreen.kt (detalles de oferta)
└── components/
    ├── P2PActionButton.kt
    ├── P2PFiltersCard.kt
    ├── P2POfferCard.kt
    └── P2PStatsCard.kt
```

### 📁 Archivos Modificados
```
├── navigation/AppDestinations.kt (nueva ruta P2PFilters)
├── presentation/ui/main/MainScreen.kt (instancia compartida ViewModel)
├── presentation/ui/p2p/P2PScreen.kt (refactorizado con componentes)
├── presentation/ui/p2p/P2PViewModel.kt (filtros múltiples)
└── data/model/P2POfferResponse.kt (modelos completos)
```

## 🚀 v2.2.0 - Sistema de Temas Dinámico Completo (2025-08-04)

### ✨ Sistema de Configuraciones Persistentes

#### 🎨 Implementación de Temas Dinámicos
- **Diálogo de selección de tema** en SettingsScreen:
  - RadioButtons para "Claro", "Oscuro", "Sistema"
  - Material 3 AlertDialog con diseño consistente
  - Selección persistente entre sesiones de la app
  - Cambio instantáneo de tema al seleccionar

- **AppTheme dinámico**:
  - Observa cambios de configuración en tiempo real usando Flow
  - Soporte para Dynamic Colors en Android 12+ (Material You)
  - Tema "Sistema" respeta configuración del dispositivo
  - Aplicación automática de temas claro/oscuro

#### 🏗️ Arquitectura Completa de Settings

##### **Data Layer**
- **SettingsEntity**: Entity de Room para persistir configuraciones
- **SettingsDao**: DAO con operaciones CRUD y queries específicas
- **SettingsLocalDataSource**: Interface y implementación para gestión local
- **SettingsRepositoryImpl**: Repository con mapeo entity ↔ domain model

##### **Domain Layer**  
- **SettingsRepository**: Interface del repositorio de configuraciones
- **Use Cases especializados**:
  - `GetSettingsUseCase` - Obtener configuraciones con Flow reactivo
  - `InitializeSettingsUseCase` - Inicializar valores por defecto
  - `UpdateThemeUseCase` - Persistir selección de tema
  - `UpdateNotificationsUseCase` - Configurar notificaciones
  - `UpdateBiometricUseCase` - Configurar autenticación biométrica

##### **Presentation Layer**
- **SettingsViewModel actualizado**: Integración completa con use cases reales
- **Manejo de estado reactivo**: Cambios se reflejan inmediatamente en UI
- **Gestión de errores**: Try-catch con mensajes descriptivos al usuario

#### 🛠️ Base de Datos Actualizada
- **AppDatabase v2**: Migración automática de versión 1 → 2
- **Migración SQL**: Creación de tabla `settings` con campos completos
- **Persistencia robusta**: Configuraciones sobreviven reinstalaciones

#### 🔧 Dependency Injection Completo
- **DatabaseModule**: SettingsDao agregado al grafo de dependencias
- **DataModule**: SettingsLocalDataSource y Repository configurados
- **DomainModule**: Todos los use cases de settings registrados  
- **PresentationModule**: ViewModel actualizado con 5 dependencias

### 🐛 Fixes de UI
- **P2PScreen optimización**: Reducción de espaciado excesivo con bottom navigation
- **SettingsScreen diálogo**: Corrección de scope de variables y ubicación correcta

### 📁 Archivos Nuevos
```
├── data/
│   ├── database/
│   │   ├── dao/SettingsDao.kt
│   │   └── entities/SettingsEntity.kt
│   ├── datasource/
│   │   ├── SettingsLocalDataSource.kt
│   │   └── SettingsLocalDataSourceImpl.kt
│   └── repository/
│       └── SettingsRepositoryImpl.kt
├── domain/
│   ├── repository/SettingsRepository.kt
│   └── usecase/
│       ├── GetSettingsUseCase.kt
│       ├── InitializeSettingsUseCase.kt
│       ├── UpdateBiometricUseCase.kt
│       ├── UpdateNotificationsUseCase.kt
│       └── UpdateThemeUseCase.kt
└── presentation/ui/theme/
    └── AppTheme.kt
```

### 📁 Archivos Modificados
```
├── MainActivity.kt (AppTheme integrado)
├── data/database/AppDatabase.kt (v2 + migración + SettingsDao)
├── presentation/ui/settings/
│   ├── SettingsScreen.kt (diálogo de tema + ThemeSelectionDialog)
│   └── SettingsViewModel.kt (use cases reales integrados)
├── presentation/ui/p2p/P2PScreen.kt (espaciado optimizado)
└── di/ (todos los módulos actualizados con settings)
    ├── DatabaseModule.kt
    ├── DataModule.kt  
    ├── DomainModule.kt
    └── PresentationModule.kt
```

### 🎯 Funcionalidad Completa
1. **Usuario selecciona tema** → Diálogo se abre
2. **Selecciona opción** → UpdateThemeUseCase persiste en BD
3. **AppTheme observa cambio** → Flow emite nueva configuración  
4. **UI se actualiza** → Material 3 aplica tema inmediatamente
5. **Persistencia garantizada** → Configuración sobrevive entre sesiones

## 🚀 Cambios Pendientes de Commit

### ✨ Nuevas Funcionalidades

#### 🏗️ Refactorización de Arquitectura
- **Reorganización del directorio `presentation/ui/`**:
  ```
  presentation/ui/
  ├── components//
  │   └── BottomNavigationBar.kt
  ├── home/
  │   ├── HomeScreen.kt
  │   └── HomeViewModel.kt
  ├── login/
  │   ├── LoginScreen.kt
  │   └── LoginViewModel.kt
  ├── main/
  │   ├── MainScreen.kt
  │   └── MainViewModel.kt
  ├── p2p/
  │   ├── P2PScreen.kt
  │   └── P2PViewModel.kt
  └── settings/
      ├── SettingsScreen.kt
      └── SettingsViewModel.kt
  ```

#### 🎨 UI/UX Mejoradas
- **LoginScreen completamente rediseñado**:
  - Gradiente de fondo con colores del tema
  - Logo circular con iniciales "QP"
  - Campos con iconos (Email, Lock, Security)
  - **Toggle de visibilidad de contraseña** con iconos Visibility/VisibilityOff
  - Navegación mejorada con teclado (Next/Done actions)
  - Validación de campos en tiempo real
  - Mensajes de error y éxito mejorados
  - Esquinas redondeadas y diseño moderno
  - Textos completamente en español

#### 🧭 Sistema de Navegación
- **BottomNavigationBar** implementada con 3 tabs:
  - 🏠 **Inicio** (HomeScreen)
  - 🔄 **P2P** (P2PScreen)
  - ⚙️ **Ajustes** (SettingsScreen)

- **MainScreen** como contenedor principal con Scaffold
- **Navegación centralizada** usando `AppDestinations` sealed class
- **Flujo**: Login → MainScreen (con BottomNav entre Home/P2P/Settings)

#### 📱 Nuevas Pantallas
- **P2PScreen**: Transacciones peer-to-peer con estadísticas y acciones rápidas
- **SettingsScreen**: Configuraciones completas con toggle switches y opciones

#### 🔄 Patrones de Estado
- **Migración completa a StateFlow/SharedFlow**:
  - `StateFlow<UiState>` para estado de UI
  - `SharedFlow<Effect>` para efectos de navegación
  - `collectAsState()` en Compose
  - Eliminación de `mutableStateOf` en ViewModels

### 🔧 Dependencias Agregadas
- **Material Icons Extended**: `androidx.compose.material:material-icons-extended`
  - Acceso a iconos como Visibility, VisibilityOff, Security, Error, CheckCircle
  - Soluciona limitaciones del pack básico de iconos

### 📁 Archivos Modificados
```
├── app/build.gradle.kts (nueva dependencia)
├── gradle/libs.versions.toml (nueva dependencia)
├── navigation/
│   ├── AppDestinations.kt (nuevas rutas: Main, P2P, Settings)
│   └── AppNavigation.kt (refactorizada con AppDestinations)
├── di/PresentationModule.kt (nuevos ViewModels)
└── presentation/ui/ (completamente reorganizada)
```

---

## 📋 Reglas de Desarrollo a Futuro

### ✅ **LO QUE SE DEBE HACER**

#### 🏗️ Arquitectura
- **SIEMPRE** usar Clean Architecture con capas separadas
- **SIEMPRE** aplicar principios SOLID
- **SIEMPRE** usar Dependency Injection con Koin
- **SIEMPRE** seguir el patrón de directorios establecido:
  ```
  presentation/ui/{feature}/
  ├── {Feature}Screen.kt
  └── {Feature}ViewModel.kt
  ```

#### 📱 ViewModels y Estado
- **OBLIGATORIO** usar StateFlow/SharedFlow en ViewModels:
  ```kotlin
  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()
  
  private val _effect = MutableSharedFlow<Effect>()
  val effect: SharedFlow<Effect> = _effect.asSharedFlow()
  ```
- **OBLIGATORIO** usar `collectAsState()` en Compose
- **OBLIGATORIO** separar estado (StateFlow) de efectos (SharedFlow)

#### 🧭 Navegación
- **SIEMPRE** usar `AppDestinations` sealed class para rutas
- **NUNCA** hardcodear strings de rutas
- **SIEMPRE** mantener navegación centralizada

#### 🎨 UI/UX
- **SIEMPRE** usar Material 3 Design System
- **SIEMPRE** implementar textos en español
- **SIEMPRE** agregar iconos apropiados en campos de entrada
- **SIEMPRE** usar esquinas redondeadas (12.dp)
- **SIEMPRE** implementar estados de carga y error
- **OBLIGATORIO** añadir accessibility (contentDescription)

#### 🔒 Seguridad
- **NUNCA** exponer o loggear secrets/keys
- **NUNCA** commitear secrets al repositorio
- **SIEMPRE** seguir mejores prácticas de seguridad

#### 📝 Código
- **NUNCA** agregar comentarios a menos que sea explícitamente solicitado
- **SIEMPRE** seguir convenciones de código existentes
- **SIEMPRE** usar librerías ya presentes en el proyecto
- **SIEMPRE** verificar que librerías estén disponibles antes de usarlas

---

### ❌ **LO QUE NO SE DEBE HACER**

#### 🚫 Antipatrones de Arquitectura
- **NO** usar `mutableStateOf` en ViewModels (usar StateFlow)
- **NO** mezclar lógica de negocio en Composables
- **NO** crear archivos en ubicaciones incorrectas
- **NO** romper la separación de capas

#### 🚫 Imports y Dependencias
- **NO** usar `import androidx.lifecycle.compose.collectAsState` 
  - Ya viene en `androidx.compose.runtime.*`
- **NO** asumir que librerías conocidas están disponibles
- **NO** usar iconos no disponibles en el pack de iconos actual

#### 🚫 Navegación
- **NO** hardcodear rutas como strings
- **NO** crear múltiples sistemas de navegación
- **NO** romper el flujo Login → MainScreen → BottomNav

#### 🚫 Gestión de Estado
- **NO** usar estados locales para lógica compleja
- **NO** mezclar estado de UI con efectos de navegación
- **NO** usar `mutableStateOf` para estado compartido

#### 🚫 UI/UX
- **NO** crear archivos de documentación (.md) proactivamente
- **NO** usar emojis a menos que sea explícitamente solicitado
- **NO** romper la consistencia visual establecida

---

## 🎯 Próximos Pasos Recomendados

1. **Hacer commit** de todos los cambios actuales
2. **Probar** la navegación completa: Login → Home → P2P → Settings
3. **Validar** que todos los iconos se muestren correctamente
4. **Implementar** funcionalidades específicas de P2P y Settings según necesidades
5. **Agregar** tests unitarios para los nuevos ViewModels
6. **Optimizar** rendimiento si es necesario

---

## 📚 Tecnologías Utilizadas

- **UI**: Jetpack Compose + Material 3
- **Navegación**: Navigation Compose + BottomNavigation
- **Estado**: StateFlow + SharedFlow
- **DI**: Koin
- **Arquitectura**: Clean Architecture + SOLID
- **HTTP**: Ktor Client
- **Base de Datos**: Room
- **Imágenes**: Coil
- **Iconos**: Material Icons Extended

---

*Documento generado automáticamente - Mantener actualizado con cada cambio significativo*