# 🏗️ Enigma Focus — System Architecture & Technical Specifications

<p align="center">
  <strong>Technical Blueprint & Architecture Reference for Enigma Focus</strong>
</p>

---

## 🌐 Table of Contents / Tabla de Contenidos
- [1. High-Level Architectural Overview](#1-high-level-architectural-overview)
- [2. Component Breakdown](#2-component-breakdown)
  - [2.1 UI Layer (Jetpack Compose & MVVM)](#21-ui-layer-jetpack-compose--mvvm)
  - [2.2 Data Layer & Reactive State (StateFlow + SharedPreferences)](#22-data-layer--reactive-state-stateflow--sharedpreferences)
  - [2.3 Interception Engine (`FocusAccessibilityService`)](#23-interception-engine-focusaccessibilityservice)
  - [2.4 Accessibility Overlay Engine (`BlockOverlayManager` & `SleepOverlayManager`)](#24-accessibility-overlay-engine-blockoverlaymanager--sleepoverlaymanager)
  - [2.5 System Grayscale Daltonizer Manager (`GrayscaleManager`)](#25-system-grayscale-daltonizer-manager-grayscalemanager)
  - [2.6 Scheduled Intervals Engine (`FocusInterval`)](#26-scheduled-intervals-engine-focusinterval)
  - [2.7 24/7 Persistence & OEM Compatibility (Xiaomi/MIUI/HyperOS)](#27-247-persistence--oem-compatibility-xiaomimuihyperos)
- [3. Arquitectura en Español](#3-arquitectura-en-español)

---

## 1. High-Level Architectural Overview

Enigma Focus is built around clean modern Android architecture patterns combining **Unidirectional Data Flow (UDF)**, **Jetpack Compose Material 3**, and low-level **Android System Framework Services** (`AccessibilityService`, `WindowManager`, `Settings.Secure`, `QuickSettingsTileService`).

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│                                                                        │
│   MainActivity ◄──► NavigationHost                                    │
│   ├── FocusScreen (Timer, Grayscale Switch, Schedule Intervals)       │
│   ├── AppsScreen (Distractions Catalog, Search Bar, Multi-select)      │
│   └── SettingsScreen (Diagnostics, ADB Guide, System Overlays)         │
│                              ▲                                         │
│                              ▼ StateFlow                               │
│                   MainScreenViewModel (MVVM)                           │
└──────────────────────────────┬─────────────────────────────────────────┘
                               │
┌──────────────────────────────▼─────────────────────────────────────────┐
│                     DATA & PREFERENCES LAYER                           │
│                                                                        │
│   AppPreferences (Singleton with StateFlow dispatchers)                │
│   ├── SharedPreferences: "enigma_focus_prefs"                          │
│   ├── Blocked Packages Set (Instagram, Reddit, TikTok, X, etc.)        │
│   ├── Scheduled Focus Intervals (Workday 7:30-16:30, Sleep 22:30-6:30) │
│   └── Temporary Whitelists & Strict Mode Toggles                       │
└──────────────────────────────┬─────────────────────────────────────────┘
                               │
┌──────────────────────────────▼─────────────────────────────────────────┐
│                       SYSTEM SERVICES LAYER                            │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │ FocusAccessibilityService (Background Daemon)                 │     │
│   │ ├── Window State Watchdog (300ms polling loop)               │     │
│   │ ├── BlockOverlayManager (TYPE_ACCESSIBILITY_OVERLAY)         │     │
│   │ └── SleepOverlayManager (Bedtime popups 22:30 - 06:30)        │     │
│   └──────────────────────────────────────────────────────────────┘     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │ GrayscaleManager (Settings.Secure Daltonizer Hardware Hook)  │     │
│   └──────────────────────────────────────────────────────────────┘     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │ FocusForegroundService (Sticky Countdown Status Bar Service) │     │
│   └──────────────────────────────────────────────────────────────┘     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │ BootReceiver (Reboot autostart & device state sync)          │     │
│   └──────────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Component Breakdown

### 2.1 UI Layer (Jetpack Compose & MVVM)
- **`MainActivity.kt`**: Single Activity container with edge-to-edge rendering, hosting the Material 3 Navigation Bar (Enfoque, Apps, Ajustes).
- **`FocusScreen.kt`**:
  - Live animated Circular Timer countdown (`hh:mm:ss`).
  - 1-tap manual **Grayscale switch** with live Daltonizer status.
  - Preset duration chips (25 min, 1 hora, 4 horas, 9h Jornada).
  - Multiple Scheduled Focus Intervals card with TimePickerDialog edit modals.
- **`AppsScreen.kt`**:
  - Reactive search filter over installed applications.
  - Quick-preset distractions badge (*Bloquear Populares: Instagram, Reddit, TikTok, etc.*).
- **`SettingsScreen.kt`**:
  - Real-time diagnostic permission checks (`WRITE_SECURE_SETTINGS`, Accessibility, Overlays).
  - 1-tap clipboard command copy for ADB setup.

### 2.2 Data Layer & Reactive State (StateFlow + SharedPreferences)
- **`AppPreferences.kt`**:
  - Central reactive state hub. Backed by private Android `SharedPreferences`.
  - Exposes hot `StateFlow` streams (`blockedPackagesFlow`, `focusActiveFlow`, `autoGrayscaleFlow`, `alwaysBlockFlow`, `strictModeFlow`, `intervalsFlow`).
  - Automatic JSON serialization/deserialization for recurring `FocusInterval` objects.

### 2.3 Interception Engine (`FocusAccessibilityService`)
- Extends `android.accessibilityservice.AccessibilityService`.
- Configured with:
  ```xml
  android:accessibilityEventTypes="typeWindowStateChanged|typeWindowsChanged|typeWindowContentChanged"
  android:accessibilityFeedbackType="feedbackGeneric"
  android:accessibilityFlags="flagIncludeNotImportantViews|flagReportViewIds|flagRetrieveInteractiveWindows"
  ```
- **Continuous Watchdog**: Runs a coroutine polling loop (300ms interval) inspecting `rootInActiveWindow?.packageName` to catch app switches even if the OEM launcher suppresses standard accessibility events.

### 2.4 Accessibility Overlay Engine (`BlockOverlayManager` & `SleepOverlayManager`)
- **Zero Activity Dependencies**: Standard app blockers call `startActivity(BlockActivity)` which is blocked by Android 14 Background Activity Launch (BAL) restrictions and OEM security policies (Xiaomi MIUI/HyperOS `OP_BACKGROUND_START_ACTIVITY`).
- **`TYPE_ACCESSIBILITY_OVERLAY`**:
  - Injects a `ComposeView` directly into the system `WindowManager`.
  - Bypasses all background activity launch restrictions.
  - Instantaneous appearance with zero delay.
  - Provides a self-contained `LifecycleOwner` and `SavedStateRegistryOwner` for Compose animations and state handling.

### 2.5 System Grayscale Daltonizer Manager (`GrayscaleManager`)
- Integrates with Android's system-level accessibility Daltonizer:
  ```kotlin
  Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", 1)
  Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", 0) // 0 = Monochrome
  ```
- Pure hardware GPU monochrome transformation without screen overlay lag or battery drain.

### 2.6 Scheduled Intervals Engine (`FocusInterval`)
- Supports multiple daily time windows (e.g. `07:30 - 16:30` workday and `22:30 - 06:30` sleep).
- **Overnight Midnight Resolution**: Accurately determines active state across midnight boundaries:
  - If before midnight ($T \ge T_{\text{start}}$), checks current day.
  - If after midnight ($T < T_{\text{end}}$), checks previous day.

### 2.7 24/7 Persistence & OEM Compatibility (Xiaomi/MIUI/HyperOS)
1. **Battery Whitelist**: Added to `deviceidle whitelist` so Doze mode does not suspend the daemon.
2. **Autostart & Background Execution**: AppOps `10008` (Autostart) and `RUN_IN_BACKGROUND` enabled.
3. **`BootReceiver`**: Listens for `ACTION_BOOT_COMPLETED` and `ACTION_MY_PACKAGE_REPLACED` to initialize preferences upon device reboot.

---

## 3. Arquitectura en Español

### Resumen Técnico
Enigma Focus está desarrollado bajo la arquitectura estándar recomendada por Google: **MVVM (Model-View-ViewModel)** con **Jetpack Compose Material 3** y un servicio daemon de sistema en segundo plano (**`FocusAccessibilityService`**).

### Componentes Clave:
1. **Superposición Nativa de Accesibilidad (`TYPE_ACCESSIBILITY_OVERLAY`)**:
   En lugar de abrir actividades secundarias (que pueden ser bloqueadas por el sistema en segundo plano), la app inyecta una vista de Compose directamente en el gestor de ventanas (`WindowManager`). Esto garantiza un bloqueo **instantáneo, sin lag y 100% compatible con Xiaomi / MIUI / HyperOS / Samsung / Pixel**.
2. **Escala de Grises por Hardware (`GrayscaleManager`)**:
   Modifica directamente las claves de configuración seguras del sistema (`accessibility_display_daltonizer`), activando el modo monocromático en la GPU del dispositivo sin sobrecargar la batería.
3. **Motor de Horarios (`FocusInterval`)**:
   Permite programar múltiples turnos al día (Jornada laboral de 7:30 a 16:30 y Descanso nocturno de 22:30 a 6:30). Maneja automáticamente los cambios de día en horarios que cruzan la medianoche.
4. **Persistencia Total 24/7**:
   Se implementó [`BootReceiver`](file:///home/enigma/github/kotlin/enigma-focus-app/app/src/main/java/com/example/enigmafocus/receiver/BootReceiver.kt) y configuración de lista blanca de batería para que el bloqueo y los recordatorios sigan funcionando sin interrupciones aunque la app se cierre de la multitarea.
