# 🔌 Paso 7: Integración con Tasker / Intents Unix
## *"Permite que el programa se comunique y componga con otras herramientas"*

---

## 🎯 Propósito
Hacer que Enigma Focus sea programable y automatizable desde herramientas externas de Android (Tasker, Automate, Macrodroid o scripts de shell vía `am broadcast`) mediante Intents limpios del sistema.

---

## 🛠️ Implementación
1. Declarar acciones de Broadcast públicas en `AndroidManifest.xml`:
   - `com.example.enigmafocus.action.START_SESSION` (con extra `duration_minutes`)
   - `com.example.enigmafocus.action.STOP_SESSION`
   - `com.example.enigmafocus.action.TOGGLE_GRAYSCALE`
   - `com.example.enigmafocus.action.ENABLE_INTERVAL` (con extra `interval_id`)
2. Permitir el control desde la terminal / ADB:
   ```bash
   # Iniciar sesión de 45 min desde terminal o script
   adb shell am broadcast -a com.example.enigmafocus.action.START_SESSION --ei duration_minutes 45
   
   # Alternar escala de grises
   adb shell am broadcast -a com.example.enigmafocus.action.TOGGLE_GRAYSCALE
   ```

---

## 💡 Principio Unix
- **Componibilidad**: Diseña programas para que puedan conectarse fácilmente con otros programas y scripts del entorno.
