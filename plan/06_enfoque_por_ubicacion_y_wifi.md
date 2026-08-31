# 📍 Paso 6: Enfoque por Contexto (Ubicación y Wi-Fi)
## *Geofencing & Context-Aware Automatic Focus Triggers*

---

## 🎯 Objetivo
Activar automáticamente el modo de concentración y el bloqueo de distracciones en función del entorno físico del usuario (llegar a la oficina, a la universidad o conectarse a la red Wi-Fi de trabajo).

---

## 🛠️ Especificaciones Técnicas

### 1. Disparadores por Red Wi-Fi (Bajo consumo de batería)
- Detección del SSID / BSSID de la red conectada vía `android.net.wifi.WifiManager` / `ConnectivityManager.NetworkCallback`.
- El usuario puede marcar su red del trabajo / biblioteca como "Zona de Enfoque".
- En cuanto el teléfono se conecta a esa red Wi-Fi:
  - Se activa el bloqueo de apps distractoras.
  - La pantalla pasa a escala de grises automáticamente.

### 2. Disparadores por Geovalla (*Geofencing API*)
- Integración con Google Play Services Geofencing API o LocationManager nativo.
- Permite definir un radio circular (ej. 150 metros) alrededor del lugar de trabajo o estudio.
- Transiciones `GEOFENCE_TRANSITION_ENTER` (inicia enfoque) y `GEOFENCE_TRANSITION_EXIT` (finaliza).

---

## ✅ Criterios de Aceptación
1. Conectarse a la red Wi-Fi de la oficina activa automáticamente el modo concentración.
2. Cero consumo innecesario de batería usando callbacks pasivos del sistema de red.
