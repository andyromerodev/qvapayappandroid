# QvaPay Android: P2P potente con Compose, MVI y DataStore

Un cliente Android moderno para el ecosistema QvaPay, centrado en una experiencia P2P rápida, confiable y segura. Construido con Jetpack Compose, arquitectura limpia (MVI + casos de uso), persistencia con DataStore/Room y red con Ktor. Incluye alertas de ofertas, plantillas reutilizables, WebViews integradas y un enfoque fuerte en rendimiento y UX.

## ⚠️ Estado del Proyecto
- Fase: en desarrollo (work‑in‑progress / pre‑release).
- API pública: hay endpoints no implementados aún y otros inestables.
- Caso concreto: el endpoint de “aceptar oferta” (apply) no funciona actualmente o depende de cambios del servicio externo.
- Nota: el backend es un servicio de terceros (QvaPay); cambios en su API pueden afectar la funcionalidad. Se irá actualizando progresivamente.

---

## ✨ Highlights
- UI 100% Jetpack Compose (Material 3) con animaciones y navegación declarativa.
- Mercado P2P completo: listar/filtrar/ordenar, ver detalle, crear, aplicar y cancelar ofertas.
- Plantillas de ofertas reutilizables y sistema de alertas P2P con notificaciones.
- WebView optimizada (login y dashboard QvaPay + vista P2P por oferta).
- Arquitectura limpia: MVI por pantalla, casos de uso, repositorios y DI con Koin.
- Ktor + Kotlinx Serialization, DataStore para sesión/ajustes y Room como caché.
- Throttling centralizado para evitar 429, paginación inteligente y manejo de errores robusto.

---

## 🔧 Arquitectura
- Presentación (Compose + MVI)
  - Intents, State y Effects por feature (Login, Home, P2P, Templates, Alerts, etc.).
  - `StateFlow`/`SharedFlow` para estado reactivo y efectos de un solo disparo.
  - Navegación Compose y transiciones animadas coherentes.
- Dominio (Use Cases)
  - Casos de uso claros: `GetP2POffersUseCase`, `CreateP2POfferUseCase`, `CancelP2POfferUseCase`, `ManageAlertWorkManagerUseCase`, etc.
- Datos
  - Ktor con `ContentNegotiation(Json)`, `HttpTimeout` y logging configurado por `BuildConfig`.
  - Repositorios cache-first donde aplica (Room para “mis ofertas”, perfil). DataStore para sesión/ajustes.
  - Endpoints centralizados en `ApiConfig` (`/p2p/index`, `/p2p/my`, `/p2p/{id}`, apply/cancel/create).
- DI: Koin (módulos `networkModule`, `databaseModule`, `dataModule`, `domainModule`, `presentationModule`).

---

## 🧭 Funcionalidades
- Autenticación
  - Login con gestión segura de sesión en DataStore; refresco de perfil; logout.
- P2P
  - Listado paginado, filtros (tipo, monedas), ordenamiento (ratio/nombre), detalle de oferta.
  - Crear ofertas (formulario + soportes), cancelar ofertas.
  - Aceptar ofertas: actualmente mediante WebView integrada como solución temporal mientras el endpoint público “apply” está inestable/no disponible.
- Plantillas
  - Crear, editar, duplicar y reutilizar plantillas al publicar.
- Alertas P2P
  - Reglas locales (mín/máx, tipo, moneda, ratio objetivo, VIP/KYC), activar/desactivar.
  - Chequeos periódicos con WorkManager y notificaciones.
- WebView integrada
  - Vista full-screen para QvaPay (login/dashboard) con FAB expandible.
  - WebView P2P por oferta (flujo “aceptar” desde detalle).
- Ajustes
  - Tema, idioma, notificaciones y biometría; acceso a perfil y centro de alertas.

---

## 🚀 Rendimiento y Confiabilidad
- Paginación inteligente: detección de fin real de lista y auto-carga sólo tras interacción.
- Throttling centralizado por operación y global para evitar rate limits (429) y reintentos automáticos.
- Manejo de errores granular (carga inicial vs paginación) y mensajes claros en UI.
- WebView “warmed up” al arranque para reducir tiempos de carga.

---

## 🧭 Cómo aceptar ofertas (WebView temporal)
Debido a cambios y/o inestabilidad del endpoint público para “aceptar oferta”, la app implementa un flujo temporal basado en WebView para que puedas completar el proceso directamente en la web de QvaPay.

Pasos de uso
- Desde “Ofertas P2P”, abre el detalle de la oferta y toca “Aceptar”.
- La app abrirá una vista WebView específica para esa oferta.
- Si no iniciaste sesión en la web, se te pedirá hacerlo allí mismo.
- Completa el flujo de aceptación directamente en la página oficial de QvaPay.

Notas importantes
- La WebView es la estándar de Android; la app no intercepta ni almacena tus credenciales.
- La experiencia puede variar según cambios en la web de QvaPay (tiempos de carga, UI, requisitos de validación, etc.).
- Este flujo es temporal: cuando el endpoint nativo quede estable, migraremos a un proceso 100% integrado en la app.

Privacidad y seguridad
- La autenticación ocurre en el dominio oficial de QvaPay dentro de la WebView.
- Recomendamos verificar el dominio visible y mantener tu dispositivo actualizado.

---

## 🎨 UI/UX
- Material 3 con TopAppBar compactas y transiciones suaves.
- Animaciones de entrada (spring + fade) en listas y pantallas (Login, Home, P2P).
- Login tematizado (branding morado) y mensajes de éxito/error con componentes nativos.
- FAB expandible en WebView (acceso directo a Login y Dashboard).

---

## 🧱 Stack Técnico
- Lenguaje: Kotlin 2.0.x
- UI: Jetpack Compose (BOM 2024.09), Material 3, Navigation Compose
- Red: Ktor 2.3.x + Kotlinx Serialization 1.7.x
- DI: Koin 3.5.x
- Persistencia: DataStore Preferences 1.1.x, Room 2.6.x (caché de datos complejos)
- Tareas: WorkManager 2.9.x
- Build: MinSdk 26, Target/Compile 35

---

## 🗺️ Flujo y Pantallas
- Splash → Login → Main (BottomNav): Home (mis ofertas), P2P (explorar), Settings.
- Detalle de oferta P2P y de “mis ofertas”.
- WebView full-screen (QvaPay) y WebView P2P por oferta.
- Módulos extra: Plantillas, Alertas, Perfil.

---

## 🔒 Seguridad y Permisos
- Permisos: `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS` (Android 13+).
- DataStore para tokens de sesión; Room para perfil y caché de ofertas.
- Preferencia de biometría habilitable desde Ajustes.

---

## 📦 Build / Instalación
- Compilar:
  ```bash
  ./gradlew assembleDebug
  ```
- Tests y Lint:
  ```bash
  ./gradlew test
  ./gradlew lint
  ```
- APK listo para instalar: `app/release/app-release.apk`

---

## 📥 Descargas y Canal de Telegram
- Publicamos builds de desarrollo y pre‑release en nuestro canal:
  - Canal: https://t.me/qvapayandroid
- Nota:
  - Puede que debas habilitar “instalar apps de orígenes desconocidos”.
  - Si tienes una versión instalada con firma distinta, desinstálala antes de instalar la nueva.

---

## 🗒️ Changelog Reciente (extracto)
- v3.12.0: MVI en Home/Login, TopAppBars compactas, scroll mejorado, mejoras de arquitectura y manejo de errores.
- v3.11.0: Animaciones modernizadas (Splash/Login), paginación P2P inteligente, detección 429 con retry, animaciones en listas.
- v3.9.0: FAB expandible en WebView (acceso 1 toque a Login/Dashboard), navegación optimizada.

> Ver el archivo `CHANGELOG.md` para la lista detallada de cambios.

---

## 🧭 Roadmap
- Extender MVI a todas las pantallas restantes.
- Suite de tests (unitarios/UITests) y validación completa de la migración a DataStore.
- Mejoras de rendimiento de WebView, manejo offline y retry avanzado.

---

## 📣 Llamado a la acción
Descarga el APK, pruébalo y cuéntanos tu feedback. Aporta issues o PRs para seguir puliendo el flujo P2P, las alertas y la experiencia general. ¡Vamos a llevar QvaPay en Android al siguiente nivel!
