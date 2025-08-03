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