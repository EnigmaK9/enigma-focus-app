# 🧪 07. Optimización de Batería y Plan de Pruebas Riguroso

Este documento establece los estándares de eficiencia energética de **Enigma Focus** y la matriz de pruebas automatizadas y manuales para certificar la confiabilidad del 100% en el bloqueo.

---

## 1. Eficiencia Energética: Bloqueo Firme con Consumo Cero

El objetivo del rediseño es mantener el consumo de batería por debajo del **1% diario** y el uso de memoria RAM por debajo de **25 MB**.

### Principios de Ahorro Extremo
1. **Suspensión Total con Pantalla Apagada (`Screen Off`):**
   Cuando el usuario bloquea el móvil (`Intent.ACTION_SCREEN_OFF`), el watchdog activo en corrutina se **pausa por completo**. No tiene sentido vigilar ventanas si la pantalla OLED/LCD está apagada:
   ```kotlin
   Intent.ACTION_SCREEN_OFF -> {
       isScreenOn = false
       watchdogJob?.cancel() // Cero ciclos de CPU en reposo
   }
   Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
       isScreenOn = true
       startWatchdog() // Reactivación instantánea
   }
   ```
2. **Eventos Pasivos del Kernel en lugar de Polling Continuo:**
   El `AccessibilityService` reacciona a interrupciones generadas por el subsistema `WindowManagerService` de Android (`TYPE_WINDOW_STATE_CHANGED`). La CPU duerme hasta que el usuario toca una app.
3. **Liberación Inmediata de Compose:**
   Cuando el overlay de bloqueo se destruye, se limpian las referencias de `ComposeView` y `LifecycleOwner` para prevenir retención de memoria en el recolector de basura (GC).

---

## 2. Matriz de Pruebas de Calidad (Testing Checklist)

Para asegurar que la app nunca falle en bloquear una distracción, se debe ejecutar la siguiente batería de pruebas antes de cada lanzamiento:

| ID | Escenario de Prueba | Acción Realizada | Resultado Esperado | Estado |
| :---: | :--- | :--- | :--- | :---: |
| **TC-01** | **Apertura Directa** | Tocar el icono de Instagram/TikTok desde el Launcher principal. | La pantalla de bloqueo aparece en <100ms con vibración háptica. Cero contenido visible. | ✅ |
| **TC-02** | **Apertura Nocturna** | Cambiar la hora del sistema a las 23:15 (horario de sueño) y abrir YouTube bloqueado. | Se bloquea YouTube firmemente. La lógica de sueño no anula la lista negra de apps. | ✅ |
| **TC-03** | **Spam de Clics (Anti-Debounce)** | Presionar el icono bloqueado, pulsar atrás inmediatamente y volver a tocarlo 10 veces en 3 segundos. | El bloqueo intercepta los 10 intentos sin fuga alguna. | ✅ |
| **TC-04** | **Apertura desde Notificación** | Enviar un mensaje de WhatsApp/Telegram o notificación de una app bloqueada y tocarla desde la barra superior. | Al desplegarse la app, es interceptada inmediatamente. | ✅ |
| **TC-05** | **Pantalla Dividida (Split-Screen)** | Abrir Calculadora arriba y TikTok abajo. Tocar la calculadora. | El detector multiventana detecta TikTok en la ventana inferior y bloquea la pantalla. | ✅ |
| **TC-06** | **Ventana Flotante / Pop-up** | En Samsung One UI o Xiaomi, abrir YouTube como ventana flotante pequeña. | El inspector de ventanas interactiva cierra o cubre la ventana flotante. | ✅ |
| **TC-07** | **Reinicio del Dispositivo (Boot)** | Reiniciar el smartphone por completo. Al encender, abrir inmediatamente la app bloqueada sin abrir Enigma Focus. | El bloqueo funciona de inmediato sin requerir apertura manual previa. | ✅ |
| **TC-08** | **Matanza por OOM / Simulación de Cierre Forzado** | Ejecutar por ADB: `adb shell am kill com.example.enigmafocus` mientras se usa el móvil. | El servicio centinela se reinicia con `START_STICKY` y el escudo continúa activo. | ✅ |
| **TC-09** | **Consumo de Batería en 24 Horas** | Dejar el móvil funcionando normalmente durante 24h con el centinela activo. | Ajustes de batería de Android reportan < 1.0% de consumo atribuible a Enigma Focus. | ✅ |

---

## 3. Comandos ADB para Pruebas de Estrés Automatizadas

Para validar las pruebas de estrés de forma automatizada mediante terminal Unix:

```bash
# 1. Simular evento de arranque completo
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p com.example.enigmafocus

# 2. Forzar muerte del proceso en segundo plano (simulación de OOM Killer)
adb shell am kill com.example.enigmafocus

# 3. Comprobar si el servicio centinela sigue vivo
adb shell dumpsys activity services | grep -i FocusForegroundService

# 4. Lanzar app bloqueada mediante Intent directo (intento de bypass)
adb shell am start -n com.instagram.android/com.instagram.mainactivity.MainActivity

# 5. Medir uso de CPU en tiempo real
adb shell top -b -n 1 | grep enigmafocus
```
