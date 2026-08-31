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

## 6. Bedtime Sleep Reminders
During the sleep interval (`22:30 - 06:30`):
- If you use the phone, a gentle floating bedtime popup will appear with a glowing moon.
- Tap **"Apagar pantalla e ir a dormir"** to lock the device and rest.
- Tap **"Recordármelo en 10 min"** to snooze the reminder.

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
2. **Escala de Grises (WRITE_SECURE_SETTINGS)**: Conecta el móvil a tu ordenador con la depuración USB activa y ejecuta:
   ```bash
   adb shell pm grant com.example.enigmafocus android.permission.WRITE_SECURE_SETTINGS
   ```

---

## 2. Uso del Temporizador y Sesiones Manuales
- Selecciona la duración que desees: **25 min** (Pomodoro), **1 hora**, **4 horas** o **9h Jornada**.
- Pulsa **Iniciar Sesión**.
- Mientras la sesión esté activa, las apps distractoras estarán bloqueadas y verás la cuenta regresiva en tu barra de notificaciones.
- Para finalizar antes de tiempo, pulsa **Detener Sesión**.

---

## 3. Horarios e Intervalos Programados
Enigma Focus incluye dos horarios predeterminados y activados por defecto:
1. 💼 **Jornada Laboral**: `07:30 - 16:30` (Lunes a Viernes).
2. 🌙 **Descanso / Dormir**: `22:30 - 06:30` (Todos los días).

### Añadir o Modificar Horarios:
- Pulsa el icono de **Editar** (lápiz) en cualquier intervalo para ajustar la hora de inicio/fin o los días activos.
- Pulsa **"+ Añadir Horario / Intervalo"** para crear nuevos turnos (ej. Estudio vespertino de 18:00 a 20:30).

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

## 6. Recordatorio Nocturno para Dormir
Durante tu horario de sueño (`22:30 - 06:30`):
- Si estás usando el móvil, aparecerá un aviso flotante relajante con una luna animada.
- Pulsa **"Apagar pantalla e ir a dormir"** para apagar la pantalla y descansar.
- Pulsa **"Recordármelo en 10 min"** para posponer el aviso 10 minutos.

---

## 7. Gestión de Aplicaciones Bloqueadas
- Entra en la pestaña **Apps**.
- Usa la barra de búsqueda para encontrar cualquier aplicación instalada.
- Activa o desactiva el interruptor para bloquearla o desbloquearla.
- Usa el botón **"Bloquear Populares"** para protegerte en 1 toque de Instagram, Reddit, TikTok, X, YouTube, etc.

---

## 8. Solución de Problemas y Ajustes para Xiaomi / MIUI / HyperOS
En dispositivos Xiaomi / Redmi / Poco, configura:
1. **Inicio Automático**: `Ajustes -> Aplicaciones -> Administrar aplicaciones -> Enigma Focus -> Inicio automático -> Permitir`.
2. **Ahorro de Batería**: Seleccionar `Sin restricciones`.
3. **Mostrar ventanas emergentes en segundo plano**: Permitido en permisos de la app.
