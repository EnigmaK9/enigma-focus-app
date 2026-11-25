# 06. Tests de regresión e higiene del repositorio Git

## Problema actual
- Hay 9 archivos modificados sin commitear en el working tree y varios archivos `.apk` binarios (`EnigmaFocus-v1.0.0.apk` a `EnigmaFocus-v1.4.0.apk`) acumulados en la raíz del repositorio.
- Los tests unitarios actuales cubren `AppPreferences` de forma aislada, pero falta cubrir de punta a punta la interacción entre `FocusInterceptor.shouldBlockPackage`, el desbloqueo temporal de 1 minuto, su expiración y el reinicio al cambio de hora (`:00`).

## Cambios propuestos
1. **Ampliar cobertura en `ComprehensiveFocusUnitTests.kt`**
   - Probar el flujo completo: app bloqueada -> `shouldBlockPackage == true` -> desbloqueo 1 min -> `shouldBlockPackage == false` durante 60s -> `shouldBlockPackage == true` al segundo 61 -> `canUseTemporaryUnlock == false` hasta `:59:59` -> `canUseTemporaryUnlock == true` al minuto `:00` de la siguiente hora.
   - Probar cruce de medianoche (`23:55` -> `00:00` del día siguiente).
2. **Limpieza del repositorio Git**
   - Agregar `*.apk` al `.gitignore` (o mover los binarios fuera del repo) para no ensuciar el historial ni `git status`.
   - Hacer commits pequeños y atómicos por bloque (primero los cambios actuales ya probados de v1.4.0 + fix horario, y luego cada fase del plan).

## Archivos afectados
- `app/src/test/java/com/example/enigmafocus/ComprehensiveFocusUnitTests.kt`
- `.gitignore`

## Criterio de éxito
- `./gradlew testDebugUnitTest` pasa al 100% con los nuevos casos de transición horaria y medianoche.
- `git status` queda limpio, sin `.apk` sin seguimiento ni cambios mezclados.
