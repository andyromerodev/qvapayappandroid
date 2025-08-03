# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Building the project
```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK
./gradlew build               # Full build with tests
```

### Running tests
```bash
./gradlew test                # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests
./gradlew testDebugUnitTest   # Run specific variant unit tests
```

### Code quality and linting
```bash
./gradlew lint                # Run Android lint
./gradlew ktlintCheck         # Check Kotlin code style (if configured)
```

### Cleaning
```bash
./gradlew clean               # Clean build artifacts
```

## Project Architecture

This is an Android Kotlin application following Clean Architecture principles with MVVM pattern.

### Tech Stack
- **UI**: Jetpack Compose with Material3
- **Navigation**: Navigation Compose
- **DI**: Koin for dependency injection
- **Networking**: Ktor client with Kotlinx Serialization
- **Database**: Room with coroutines support
- **Image Loading**: Coil
- **Target SDK**: 35, Min SDK: 24

### Architecture Layers

#### Domain Layer (`domain/`)
- **Use Cases**: Business logic implementation (LoginUseCase, CheckSessionUseCase, etc.)
- **Repositories**: Abstract interfaces for data access
- Pure Kotlin, no Android dependencies

#### Data Layer (`data/`)
- **Repository Implementations**: AuthRepositoryImpl, SessionRepositoryImpl
- **Data Sources**: Local (Room) and Remote (Ktor) data sources
- **Database**: Room entities and DAOs

#### Presentation Layer (`presentation/`)
- **UI**: Jetpack Compose screens (HomeScreen, LoginScreen, P2PScreen, SettingsScreen)
- **ViewModels**: MVVM pattern with state management
- **Navigation**: Single-activity architecture with Navigation Compose

### Dependency Injection Structure

The DI is organized into modules in `di/`:
- `AppModule.kt` - Main module aggregator
- `NetworkModule.kt` - Ktor client configuration
- `DatabaseModule.kt` - Room database setup
- `DataModule.kt` - Repository implementations
- `DomainModule.kt` - Use cases
- `PresentationModule.kt` - ViewModels

All modules are registered in `MainActivity.kt` via Koin's `startKoin()`.

### Key Components

#### Authentication Flow
- Session-based authentication with local storage
- `AuthRepository` handles login/logout operations
- `SessionRepository` manages session persistence
- Use cases: `LoginUseCase`, `CheckSessionUseCase`, `LogoutUseCase`, `GetCurrentUserUseCase`

#### Navigation
- Bottom navigation with main sections: Home, P2P, Settings
- Navigation routes defined with type-safe arguments
- Session-aware routing (login required for protected routes)

#### WebView Integration
- Specialized for QvaPay website interaction
- P2P offers scraping functionality
- JavaScript injection for data extraction

## Development Notes

### Package Structure
```
com.example.qvapayappandroid/
├── data/
│   ├── database/           # Room entities, DAOs
│   ├── datasource/         # Local and remote data sources
│   └── repository/         # Repository implementations
├── di/                     # Dependency injection modules
├── domain/
│   ├── repository/         # Repository interfaces
│   └── usecase/           # Business logic use cases
├── navigation/             # Navigation setup
└── presentation/
    └── ui/                # Compose screens and ViewModels
```

### Recent Refactoring
According to `REFACTOR_SUMMARY.md`, the project recently underwent significant refactoring:
- Separated business logic into dedicated Use Cases
- Improved dependency injection organization
- Enhanced WebView data extraction with validation
- Applied SOLID principles throughout the codebase

### Version Catalog
Dependencies are managed via `gradle/libs.versions.toml` with version catalog approach for consistency.

### Testing Strategy
- Unit tests for Use Cases and ViewModels
- Instrumented tests for UI components
- Test runner: AndroidJUnitRunner with Espresso for UI tests