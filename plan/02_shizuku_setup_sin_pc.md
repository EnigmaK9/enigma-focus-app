# ⚡ Paso 2: Integración con Shizuku (Setup sin PC)
## *On-Device ADB Grant via Shizuku Framework*

---

## 🎯 Objetivo
Permitir que el usuario active el permiso `WRITE_SECURE_SETTINGS` (necesario para el modo escala de grises / Daltonizer) directamente desde su teléfono sin necesidad de conectar un cable USB a una computadora.

---

## 🛠️ Especificaciones Técnicas

### 1. ¿Qué es Shizuku?
Shizuku es un framework estándar en Android que permite a aplicaciones de terceros ejecutar comandos con privilegios de ADB/Sistema utilizando el servicio de **Depuración Inalámbrica de Android (Wireless Debugging)**.

### 2. Dependencias Requeridas
```kotlin
// build.gradle.kts
dependencies {
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
```

### 3. Implementación en `GrayscaleManager.kt`
```kotlin
fun grantPermissionViaShizuku(context: Context, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
    if (!Shizuku.pingBinder()) {
        onError(IllegalStateException("Shizuku no está en ejecución"))
        return
    }
    
    val command = "pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
    Shizuku.newProcess(arrayOf("sh", "-c", command), null, null).apply {
        waitFor()
        onSuccess()
    }
}
```

### 4. Flujo de Usuario en `SettingsScreen.kt`
- Detección automática del estado de Shizuku.
- Si Shizuku está instalado y corriendo, se muestra un botón prominente: **"Conceder Permiso con 1 Toque (Shizuku)"**.
- Si no está disponible, se mantiene la guía visual tradicional con el comando ADB para PC.

---

## ✅ Criterios de Aceptación
1. Usuarios con Shizuku pueden conceder `WRITE_SECURE_SETTINGS` en 2 segundos desde el propio móvil.
2. Cero fricción para activar el modo blanco y negro en dispositivos sin acceso inmediato a PC.
