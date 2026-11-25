# 03. Optimización de watchdogs y consumo de batería

## Problema actual
- `FocusAccessibilityService.startWatchdog()` ejecuta un ciclo cada `300 ms` de forma continua mientras la pantalla está encendida, llamando a `rootInActiveWindow`, `MultiWindowDetector.findBlockedWindowThreat()` y `checkBrowserUrls()` (recorrido recursivo de nodos hasta profundidad 6).
- `FocusForegroundService.startTimer()` llama a `checkForegroundPackageFallback()` (que consulta `UsageStatsManager`) cada `1000 ms` mientras la pantalla está encendida, aunque `FocusAccessibilityService` esté funcionando normalmente.
- Este consumo constante en segundo plano provoca advertencias y cierres del sistema como `ScreenOffCPUCheckKill`.

## Cambios propuestos
1. **Watchdog adaptativo en `FocusAccessibilityService`**
   - Usar `onAccessibilityEvent` (`TYPE_WINDOW_STATE_CHANGED`, `TYPE_WINDOWS_CHANGED`) como mecanismo principal de intercepción instantánea.
   - Mantener el intervalo rápido (`300–500 ms`) **únicamente** cuando:
     - Hay un desbloqueo temporal de 1 minuto activo (para bloquear al instante en que expira), o
     - El paquete activo es un navegador de `FocusInterceptor.BROWSER_PACKAGES` (para vigilar cambios de URL sin cambio de ventana).
   - En cualquier otro caso con pantalla encendida, usar un intervalo mucho más ligero (ej. `1500–2000 ms`) o pausar el escaneo de URLs si el paquete activo no es un navegador.
2. **Watchdog condicional en `FocusForegroundService`**
   - Ejecutar `checkForegroundPackageFallback()` **solo** cuando `FocusAccessibilityService` esté desconectado (`!isConnected`) o con heartbeat vencido (`heartbeatStale`), evitando consultas redundantes a `UsageStatsManager` cada segundo.
3. **Modo centinela sin trabajo innecesario**
   - En `isSentinelOnlyMode` sin sesión temporizada, no reconstruir ni reenviar notificaciones en cada tick.

## Archivos afectados
- `app/src/main/java/com/example/enigmafocus/service/FocusAccessibilityService.kt`
- `app/src/main/java/com/example/enigmafocus/service/FocusForegroundService.kt`

## Criterio de éxito
- Reducción drástica de ciclos de CPU con la pantalla encendida cuando el usuario está en apps normales.
- El corte al expirar el minuto de desbloqueo y el bloqueo de dominios en navegadores siguen siendo inmediatos.
