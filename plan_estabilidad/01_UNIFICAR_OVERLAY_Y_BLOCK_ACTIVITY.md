# 01. Unificar flujo entre `BlockOverlayManager` y `BlockActivity`

## Problema actual
En `FocusAccessibilityService.checkAndBlockPackage`:
- Se llama primero a `BlockOverlayManager.showOverlay(this, packageName)` (dibuja `DirectBreathingBlockScreen` en `TYPE_ACCESSIBILITY_OVERLAY`).
- Inmediatamente después se llama a `startActivity(blockIntent)` (`BlockActivity`), que vuelve a componer otra instancia de `DirectBreathingBlockScreen` debajo del overlay.
- Cuando el usuario toca **"Volver a Enfocar"** en el overlay, el overlay se oculta y ejecuta `GLOBAL_ACTION_HOME`, pero `BlockActivity` puede quedar viva en segundo plano.
- En `FocusForegroundService.checkForegroundPackageFallback()`, solo se valida `if (BlockActivity.isBlockScreenShowing) return` sin revisar `BlockOverlayManager.isShowing()`, lo que puede lanzar `BlockActivity` innecesariamente sobre bloqueos web (donde solo se usa `BlockOverlayManager`).

## Cambios propuestos
1. **Definir una jerarquía única de barrera**
   - Mantener `BlockOverlayManager` como barrera visual instantánea (`TYPE_ACCESSIBILITY_OVERLAY`) y usar `BlockActivity` únicamente como respaldo cuando el overlay no pueda adjuntarse (por ejemplo, si `showOverlay` falla o `BlockOverlayManager.isShowing()` es `false`), **o** cerrar `BlockActivity` explícitamente al interactuar con el overlay.
2. **Sincronizar el estado de visibilidad**
   - Crear un helper o propiedad única (por ejemplo, `BlockOverlayManager.isShowing() || BlockActivity.isBlockScreenShowing`) y usarla de forma uniforme tanto en `FocusAccessibilityService` como en `FocusForegroundService.checkForegroundPackageFallback()`.
3. **Evitar doble vibración y doble registro de eventos**
   - Garantizar que `vibratePhone()` y `FocusEventLogger.logEvent(..., "app_blocked", ...)` solo se disparen una vez por intercepción real.

## Archivos afectados
- `app/src/main/java/com/example/enigmafocus/service/FocusAccessibilityService.kt`
- `app/src/main/java/com/example/enigmafocus/service/FocusForegroundService.kt`
- `app/src/main/java/com/example/enigmafocus/ui/block/BlockOverlayManager.kt`
- `app/src/main/java/com/example/enigmafocus/ui/block/BlockActivity.kt`

## Criterio de éxito
- Al abrir una app bloqueada aparece una sola instancia de `DirectBreathingBlockScreen`.
- Al pulsar "Volver a Enfocar" o "Usar app por 1 minuto", no quedan ventanas o actividades huérfanas detrás.
- El fallback de `FocusForegroundService` nunca relanza `BlockActivity` si el overlay ya está visible.
