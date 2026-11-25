# Plan de Mejora y Estabilidad — Enigma Focus (Sin nuevas funciones)

## Objetivo
Mejorar la **confiabilidad, eficiencia energética, fluidez y mantenibilidad** de las funciones que ya existen en Enigma Focus, sin agregar nuevas características ni ampliar el alcance funcional.

## Diagnóstico actual observado
Al revisar el código fuente y el comportamiento real en dispositivo (`dumpsys accessibility` y `dumpsys activity exit-info`), se identificaron los siguientes puntos críticos:

1. **Doble barrera simultánea (`BlockOverlayManager` + `BlockActivity`)**: Al detectar una app bloqueada se lanzan ambos a la vez, creando dos pantallas superpuestas con ciclos de vida separados.
2. **Caída silenciosa en MIUI (`Crashed services`)**: Al cerrar la app desde recientes (`SwipeUpClean`) o por consumo en segundo plano (`ScreenOffCPUCheckKill`), el servicio de accesibilidad queda en `Crashed services` y deja de mostrar el bloqueo.
3. **Watchdogs demasiado agresivos**: `FocusAccessibilityService` hace polling cada `300 ms` y `FocusForegroundService` consulta `UsageStatsHelper` cada `1000 ms` mientras la pantalla está encendida, aunque no sea necesario.
4. **Estado estático en el cartel de bloqueo**: `DirectBreathingBlockScreen` lee `remainingCooldownMillis` una sola vez al componerse; si cambia la hora (`:00`) con el cartel abierto, no se actualiza en vivo.
5. **Deuda técnica menor**: Alcance de corrutinas sin cancelar en `onDestroy()`, claves legacy en `AppPreferences` y archivos `.apk` sueltos en la raíz del repositorio.

## Índice del plan
1. [01_UNIFICAR_OVERLAY_Y_BLOCK_ACTIVITY.md](./01_UNIFICAR_OVERLAY_Y_BLOCK_ACTIVITY.md)
2. [02_RESILIENCIA_SERVICIOS_Y_MIUI.md](./02_RESILIENCIA_SERVICIOS_Y_MIUI.md)
3. [03_OPTIMIZACION_WATCHDOG_Y_BATERIA.md](./03_OPTIMIZACION_WATCHDOG_Y_BATERIA.md)
4. [04_UX_PANTALLA_BLOQUEO_EN_VIVO.md](./04_UX_PANTALLA_BLOQUEO_EN_VIVO.md)
5. [05_LIMPIEZA_APP_PREFERENCES_Y_CORRUTINAS.md](./05_LIMPIEZA_APP_PREFERENCES_Y_CORRUTINAS.md)
6. [06_TESTS_E_HIGIENE_GIT.md](./06_TESTS_E_HIGIENE_GIT.md)

## Orden recomendado de ejecución
- **Fase 1 (Impacto inmediato en estabilidad):** Documentos `01` y `02`.
- **Fase 2 (Batería y rendimiento):** Documento `03`.
- **Fase 3 (Pulido UX y código):** Documentos `04` y `05`.
- **Fase 4 (Validación y orden del repo):** Documento `06`.
