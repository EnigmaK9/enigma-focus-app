# 📖 Enigma Focus — User Guide / Guía de Usuario

<p align="center">
  <strong>Complete Step-by-Step Manual for Enigma Focus</strong><br>
  <em>Manual completo paso a paso para el uso y configuración de Enigma Focus</em>
</p>

---

## 🌐 Table of Contents / Índice
- [English User Guide](#-english-user-guide)
  - [1. Initial Setup & Permissions](#1-initial-setup--permissions)
  - [2. Using the Focus Timer & Manual Sessions](#2-using-the-focus-timer--manual-sessions)
  - [3. Setting up Scheduled Focus Intervals](#3-setting-up-scheduled-focus-intervals)
  - [4. Mindful Breathing Screen & Actions](#4-mindful-breathing-screen--actions)
  - [5. System-Wide Grayscale Mode](#5-system-wide-grayscale-mode)
  - [6. Bedtime Sleep Reminders](#6-bedtime-sleep-reminders)
  - [7. Distraction App List Management](#7-distraction-app-list-management)
  - [8. Troubleshooting & Xiaomi / MIUI / HyperOS Setup](#8-troubleshooting--xiaomi--miui--hyperos-setup)
- [Guía de Usuario en Español](#-guía-de-usuario-en-español)
  - [1. Configuración Inicial y Permisos](#1-configuración-inicial-y-permisos)
  - [2. Uso del Temporizador y Sesiones Manuales](#2-uso-del-temporizador-y-sesiones-manuales)
  - [3. Horarios e Intervalos Programados](#3-horarios-e-intervalos-programados)
  - [4. Pantalla de Respiración Consciente](#4-pantalla-de-respiración-consciente)
  - [5. Modo Escala de Grises del Sistema](#5-modo-escala-de-grises-del-sistema)
  - [6. Recordatorio Nocturno para Dormir](#6-recordatorio-nocturno-para-dormir)
  - [7. Gestión de Aplicaciones Bloqueadas](#7-gestión-de-aplicaciones-bloqueadas)
  - [8. Solución de Problemas y Ajustes para Xiaomi / MIUI / HyperOS](#8-solución-de-problemas-y-ajustes-para-xiaomi--miui--hyperos)

---

# 🇬🇧 English User Guide

## 1. Initial Setup & Permissions
When launching Enigma Focus for the first time, make sure the following permissions are configured:
1. **Accessibility Service**: Go to `Settings -> Accessibility -> Downloaded Apps -> Enigma Focus` and enable the service.
2. **Grayscale (Write Secure Settings)**: Connect your phone to your PC via USB with USB Debugging enabled, and run:
   ```bash
   adb shell pm grant com.example.enigmafocus android.permission.WRITE_SECURE_SETTINGS
   ```

---

## 2. Using the Focus Timer & Manual Sessions
- Select your desired duration chip: **25 min** (Pomodoro), **1 hora**, **4 horas**, or **9h Jornada**.
- Tap **Iniciar Sesión** to begin.
- While the session is active, a persistent timer appears in the status bar, the screen turns to grayscale (if enabled), and all selected apps are blocked.
- To cancel at any time, tap **Detener Sesión**.

---

## 3. Setting up Scheduled Focus Intervals
Enigma Focus comes with two preconfigured default intervals:
1. **Workday Shift (Jornada Laboral)**: `07:30 - 16:30` (Monday to Friday) — *Active by default*.
2. **Sleep / Bedtime (Descanso / Dormir)**: `22:30 - 06:30` (Monday to Sunday) — *Active by default*.

### Adding or Editing Intervals:
- Tap the **Edit** icon on any interval to modify start/end times or active days.
- Tap **+ Añadir Horario / Intervalo** to create a custom recurring window (e.g. Evening Study from 18:00 to 20:30).

---

## 4. Mindful Breathing Screen & Actions
When you open a blocked app (such as Instagram or Reddit):
1. An immersive dark focus screen will appear immediately.
2. Follow the 10-second guided breathing circle:
   - **Inhale** (Seconds 10–8)
   - **Hold** (Seconds 7–5)
   - **Exhale** (Seconds 4–1)
3. After breathing:
   - Tap **"Volver a mi enfoque"** to return to your home screen.
   - Tap **"Usar app por 1 minuto"** for emergency quick access (disabled in Strict Mode).

---

## 5. System-Wide Grayscale Mode
- **Manual Toggle**: Flip the **Modo Escala de Grises** switch on the main screen to turn your display monochrome instantly.
- **Auto on Session**: Keep **Auto Escala de Grises en Sesión** enabled to automatically trigger monochrome when focus sessions start.

---

## 6. Aggressive Bedtime Sleep Lockout (`22:30 - 06:30`)
During your sleep interval:
- The moment you turn on your screen or unlock the device, a full-screen bedtime lock overlay immediately covers the display.
- Non-emergency use is capped at **strictly 1 minute (60 seconds)** after completing a 10-second mindful breathing challenge.
- When 60 seconds elapse, the background watchdog immediately re-locks the screen and returns to Home.
- Emergency exemptions are active exclusively for emergency phone calls (`InCallUI`/`Dialer`) and alarm clocks (`DeskClock`).
- Tap **"Apagar pantalla e ir a dormir"** to immediately turn off screen and lock.

---

## 7. Distraction App List Management
- Navigate to the **Apps** tab.
- Use the search bar to locate specific installed applications.
- Toggle the switch on any app to add or remove it from your blocklist.
- Use the **"Bloquear Populares"** button to block top social media and distraction apps in 1 tap.

---

## 8. Troubleshooting & Xiaomi / MIUI / HyperOS Setup
On Xiaomi / Poco / Redmi devices, apply these settings to ensure 100% background stability:
1. **Autostart**: `Settings -> Apps -> Manage Apps -> Enigma Focus -> Autostart -> Allow`.
2. **Battery Saver**: Set to `No restrictions`.
3. **Display Pop-up Windows**: Enable in App Permissions.

---

# 🇪🇸 Guía de Usuario en Español

## 1. Configuración Inicial y Permisos
Al abrir la app por primera vez, asegúrate de activar:
1. **Servicio de Accesibilidad**: `Ajustes -> Accesibilidad -> Apps descargadas -> Enigma Focus` y activa el interruptor.
2. **Escala de Grises (Write Secure Settings)**: Conecta el móvil por USB a tu ordenador con Depuración USB y ejecuta:
   ```bash
   adb shell pm grant com.example.enigmafocus android.permission.WRITE_SECURE_SETTINGS
   ```

---

## 2. Uso del Temporizador y Sesiones Manuales
- Selecciona la duración deseada: **25 min** (Pomodoro), **1 hora**, **4 horas**, o **9h Jornada**.
- Pulsa **"Iniciar Sesión"** para comenzar.
- Se activará una notificación persistente con la cuenta regresiva, la pantalla pasará a escala de grises y las apps seleccionadas quedarán bloqueadas.
- Pulsa **"Detener Sesión"** cuando hayas concluido.

---

## 3. Horarios e Intervalos Programados
Enigma Focus incluye dos horarios preconfigurados listos para usar:
1. **Jornada Laboral**: `07:30 - 16:30` (Lunes a Viernes) — *Activo por defecto*.
2. **Descanso / Dormir**: `22:30 - 06:30` (Lunes a Domingo) — *Activo por defecto*.

### Añadir o Editar Intervalos:
- Toca el icono de **Editar** en cualquier horario para ajustar horas de inicio/fin o días de la semana.
- Pulsa **"+ Añadir Horario / Intervalo"** para crear reglas personalizadas (ej. Estudio Vespertino de 18:00 a 20:30).

---

## 4. Pantalla de Respiración Consciente
Cuando intentas abrir una app bloqueada (ej. Instagram o Reddit):
1. Aparecerá inmediatamente la pantalla de bloqueo con el círculo de respiración.
2. Sigue las fases guiadas de 10 segundos:
   - **Inhala** (Segundos 10 a 8)
   - **Sostén** (Segundos 7 a 5)
   - **Exhala** (Segundos 4 a 1)
3. Al finalizar la cuenta regresiva:
   - Pulsa **"Volver a mi enfoque"** para regresar a la pantalla de inicio.
   - Pulsa **"Usar app por 1 minuto"** si necesitas consultar algo rápido (esta opción se oculta si activas el Modo Estricto).

---

## 5. Modo Escala de Grises del Sistema
- **Control Directo**: Usa el interruptor **"Modo Escala de Grises"** en la pantalla principal para cambiar tu móvil a blanco y negro en tiempo real con 1 toque.
- **Automático en Sesión**: Deja activo **"Auto Escala de Grises en Sesión"** para que se active automáticamente durante tus horas de enfoque.

---

## 6. Bloqueo Nocturno Agresivo para Dormir (`22:30 - 06:30`)
Durante tu horario de sueño:
- Al encender la pantalla o desbloquear el teléfono, una pantalla completa de bloqueo de descanso cubre el teléfono inmediatamente.
- Si requieres interactuar, deberás completar los 10 segundos de respiración consciente para habilitar una pausa de **estrictamente 1 minuto (60s)** de emergencia.
- Al expirar los 60 segundos o al apagar la pantalla, el sistema re-bloquea automáticamente el teléfono y expulsa al Home.
- Las llamadas entrantes/salientes de emergencia (`InCallUI`) y alarmas del despertador (`DeskClock`) permanecen accesibles.
- Pulsa **"Apagar pantalla e ir a dormir"** para apagar la pantalla y descansar.

---

## 7. Gestión de Aplicaciones Bloqueadas
- Entra en la pestaña **Apps**.
- Usa la barra de búsqueda para encontrar cualquier aplicación instalada.
- Activa o desactiva el interruptor para bloquearla o desbloquearla.
- Usa el botón **"Bloquear Populares"** para protegerte en 1 toque de Instagram, Reddit, TikTok, X, YouTube, etc.

---

## 8. Troubleshooting & Xiaomi / MIUI / HyperOS Setup
On Xiaomi / Redmi / Poco devices:
1. **Autostart**: `Settings -> Apps -> Manage apps -> Enigma Focus -> Autostart -> Allow`.
2. **Battery Saver**: Choose `No restrictions`.
3. **Display pop-up windows while running in background**: Allow.

---

## 9. Unix CLI Broadcasts & Declarative JSON Configuration
Enigma Focus follows the Unix philosophy (*"Do one thing and do it well"*):
- **Universal CLI Intents**:
  ```bash
  # Start 45 min focus session
  adb shell am broadcast -a com.example.enigmafocus.action.START_SESSION --ei duration_minutes 45

  # Stop session
  adb shell am broadcast -a com.example.enigmafocus.action.STOP_SESSION

  # Toggle grayscale
  adb shell am broadcast -a com.example.enigmafocus.action.TOGGLE_GRAYSCALE

  # Query status JSON
  adb shell am broadcast -a com.example.enigmafocus.action.GET_STATUS

  # Apply predefined template (DEFAULT, WORKDAY_ONLY, BEDTIME_STRICT, DIGITAL_DETOX)
  adb shell am broadcast -a com.example.enigmafocus.action.APPLY_TEMPLATE --es template BEDTIME_STRICT
  ```
- **Declarative JSON Logs**: Silent local event telemetry is written to `/data/data/com.example.enigmafocus/files/focus_events.jsonl` for clean Unix analysis (`jq`, `grep`, `awk`).

---

## Guía de Usuario en Español

---

## 8. Solución de Problemas y Ajustes para Xiaomi / MIUI / HyperOS
En dispositivos Xiaomi / Redmi / Poco, configura:
1. **Inicio Automático**: `Ajustes -> Aplicaciones -> Administrar aplicaciones -> Enigma Focus -> Inicio automático -> Permitir`.
2. **Ahorro de Batería**: Seleccionar `Sin restricciones`.
3. **Mostrar ventanas emergentes en segundo plano**: Permitido en permisos de la app.

---

## 9. Interfaz CLI Unix y Configuración Declarativa JSON
Enigma Focus implementa los principios de la filosofía Unix (*"Haz una sola cosa y hazla bien"*):
- **Comandos CLI vía ADB / Tasker / Termux**:
  ```bash
  # Iniciar sesión de 45 minutos
  adb shell am broadcast -a com.example.enigmafocus.action.START_SESSION --ei duration_minutes 45

  # Detener sesión
  adb shell am broadcast -a com.example.enigmafocus.action.STOP_SESSION

  # Alternar escala de grises
  adb shell am broadcast -a com.example.enigmafocus.action.TOGGLE_GRAYSCALE

  # Consultar estado en JSON
  adb shell am broadcast -a com.example.enigmafocus.action.GET_STATUS

  # Aplicar plantilla predefinida (DEFAULT, WORKDAY_ONLY, BEDTIME_STRICT, DIGITAL_DETOX)
  adb shell am broadcast -a com.example.enigmafocus.action.APPLY_TEMPLATE --es template BEDTIME_STRICT
  ```
- **Registros en Texto Plano (`.jsonl`)**: Registro local en `/data/data/com.example.enigmafocus/files/focus_events.jsonl` 100% privado y analizable con herramientas estándar (`jq`, `grep`, `awk`).
