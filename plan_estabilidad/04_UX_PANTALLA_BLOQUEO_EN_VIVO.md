# 04. Actualización en vivo de la pantalla de bloqueo (`DirectBreathingBlockScreen`)

## Problema actual
- En `BlockActivity.kt`, dentro de `DirectBreathingBlockScreen`:
  ```kotlin
  val remainingCooldownMillis = AppPreferences.getRemainingHourlyCooldownMillis(packageName)
  ```
  se evalúa únicamente durante la composición inicial.
- Si el cartel de bloqueo permanece abierto cuando cambia la hora del reloj (por ejemplo, de `18:59` a `19:00`), la UI sigue mostrando `"Límite de 1 min/hora usado · Disponible en 1 min"` y no habilita el botón de 1 minuto hasta cerrar y reabrir la pantalla.
- Además, el texto `"Disponible en %d min"` tampoco descuenta los minutos conforme avanza el reloj si la pantalla sigue abierta.

## Cambios propuestos
1. **Estado reactivo del cooldown en `DirectBreathingBlockScreen`**
   - Convertir `remainingCooldownMillis` en un estado (`remember(packageName) { mutableLongStateOf(...) }`) actualizado dentro de un `LaunchedEffect(packageName)` cada segundo (o cada 5 segundos cuando `secondsLeft == 0`).
2. **Transición limpia al cambiar de hora**
   - Al llegar `remainingCooldownMillis` a `0L`, mostrar automáticamente el botón `"Usar app por 1 minuto"` sin necesidad de salir y volver a entrar a la app bloqueada.
3. **Consistencia bilingüe**
   - Verificar que tanto en español como en inglés el cálculo de redondeo hacia arriba (`(remainingCooldownMillis + 59_999L) / 60_000L`) nunca muestre `0 min` cuando falten pocos segundos para la siguiente hora.

## Archivos afectados
- `app/src/main/java/com/example/enigmafocus/ui/block/BlockActivity.kt`

## Criterio de éxito
- Si el usuario está viendo el cartel de bloqueo a las `18:59` y el reloj marca `19:00`, el cartel se actualiza solo y muestra el botón de desbloqueo de 1 minuto.
