# 05. Limpieza de `AppPreferences` y ciclo de vida de corrutinas

## Problema actual
1. **Escrituras fragmentadas y claves legacy en `AppPreferences.kt`**
   - Métodos como `setTemporaryWhitelist()` y `clearHourlyCooldowns()` realizan múltiples llamadas consecutivas a `prefs.edit().put...().apply()`, generando varias transacciones innecesarias en disco.
   - Permanecen claves legacy de un solo paquete (`KEY_TEMP_WHITELIST_PKG`, `KEY_TEMP_WHITELIST_EXPIRY`) y la constante `HOURLY_COOLDOWN_MS` (60 min fijos), que ya no representa la regla de reinicio por hora del reloj.
2. **Scopes de corrutinas sin cancelar en servicios**
   - En `FocusAccessibilityService` (`private val serviceScope = CoroutineScope(Dispatchers.Main + Job())`) y `FocusForegroundService`, el `Job()` raíz del `serviceScope` nunca se cancela en `onDestroy()` (solo se cancelan `watchdogJob` / `timerJob`), y usar `Job()` en lugar de `SupervisorJob()` hace que una excepción no capturada cancele todo el scope permanentemente.

## Cambios propuestos
1. **Agrupar ediciones de `SharedPreferences`**
   - Usar un único `prefs.edit().apply { ... }.apply()` en `setTemporaryWhitelist`, `clearTemporaryWhitelist` y `clearHourlyCooldowns`.
2. **Depurar código legacy en `AppPreferences.kt`**
   - Revisar si `KEY_TEMP_WHITELIST_PKG` y `KEY_TEMP_WHITELIST_EXPIRY` todavía tienen algún consumidor; si no, eliminarlos de `setTemporaryWhitelist`, `clearTemporaryWhitelist` e `isTemporarilyWhitelisted`.
   - Eliminar o marcar como obsoleta `HOURLY_COOLDOWN_MS` si ya no se utiliza.
3. **Corrutinas seguras en servicios**
   - Cambiar `Job()` por `SupervisorJob()` en `FocusAccessibilityService` y `FocusForegroundService`, y llamar `serviceScope.cancel()` en `onDestroy()`.

## Archivos afectados
- `app/src/main/java/com/example/enigmafocus/data/AppPreferences.kt`
- `app/src/main/java/com/example/enigmafocus/service/FocusAccessibilityService.kt`
- `app/src/main/java/com/example/enigmafocus/service/FocusForegroundService.kt`

## Criterio de éxito
- Menos operaciones de escritura en `SharedPreferences` por cada desbloqueo temporal.
- Cero fugas de corrutinas al destruirse los servicios.
