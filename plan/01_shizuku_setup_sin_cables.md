# ⚡ Paso 1: Shizuku / Wireless Debugging (Cero Cables)
## *"Hacer el setup tan simple como un comando Unix"*

---

## 🎯 Propósito
Permitir que el usuario active el permiso `WRITE_SECURE_SETTINGS` para la escala de grises con 1 solo toque desde el propio teléfono, sin requerir cables USB ni una computadora externa.

---

## 🛠️ Implementación
1. Integrar la librería oficial y ligera de Shizuku (`dev.rikka.shizuku:api`).
2. Si el servicio de Shizuku está corriendo, ejecutar en segundo plano:
   ```bash
   pm grant com.example.enigmafocus android.permission.WRITE_SECURE_SETTINGS
   ```
3. Si Shizuku no está disponible, mostrar el comando legible para usuarios de terminal / ADB tradicional.

---

## 💡 Principio Unix
- **Eliminación de fricción**: No obligues al usuario a saltar entre dispositivos si el propio sistema operativo puede resolverlo localmente.
