# 02. Resiliencia de servicios en Android / MIUI

## Problema actual
- En dispositivos MIUI/HyperOS, al limpiar aplicaciones recientes (`SwipeUpClean`) o cuando el sistema detecta uso prolongado en segundo plano (`ScreenOffCPUCheckKill`), el proceso `com.example.enigmafocus` es terminado.
- Cuando eso ocurre, Android marca `FocusAccessibilityService` en `Crashed services` y deja de re-vincularlo automáticamente.
- Si `FocusForegroundService` también fue detenido, `checkAndRecoverAccessibilityService()` no corre, y la app no muestra en la pantalla principal una alerta clara de que el servicio está desconectado/caído aunque siga marcado como habilitado en `Settings.Secure`.

## Cambios propuestos
1. **Estado real de salud del servicio en la UI**
   - No depender únicamente de `FocusAccessibilityService.isAccessibilityServiceEnabled(context)` (que solo lee `Settings.Secure` y devuelve `true` incluso si el servicio está en `Crashed services`).
   - Combinar la verificación con `FocusAccessibilityService.isConnected` y la frescura de `lastHeartbeatTime` cuando haya un bloqueo activo, mostrando un indicador claro en `FocusScreen` si el servicio está caído o desconectado.
2. **Mejorar la recuperación desde `FocusForegroundService`**
   - En `checkAndRecoverAccessibilityService()`, evitar ciclos rápidos innecesarios si no se cuenta con `WRITE_SECURE_SETTINGS` (capturar y registrar una sola vez la falta de permiso en lugar de reintentar cada 3 segundos).
3. **Guía rápida de supervivencia MIUI en la pantalla de permisos/ajustes**
   - Aprovechar la lógica existente en `BatteryOptimizationHelper` para verificar que el acceso a ajustes de batería / autostart sea directo y claro cuando el usuario note que el sistema cerró el servicio.

## Archivos afectados
- `app/src/main/java/com/example/enigmafocus/service/FocusAccessibilityService.kt`
- `app/src/main/java/com/example/enigmafocus/service/FocusForegroundService.kt`
- `app/src/main/java/com/example/enigmafocus/ui/main/MainScreenViewModel.kt`
- `app/src/main/java/com/example/enigmafocus/ui/main/FocusScreen.kt`

## Criterio de éxito
- Si `FocusAccessibilityService` está en `Crashed services` o desconectado, la app lo detecta y lo refleja con precisión.
- No hay bucles de reintentos fallidos cada 3 segundos cuando falta `WRITE_SECURE_SETTINGS`.
