# 🌐 Paso 4: Bloqueo de Sitios Web en Navegadores Web
## *In-Browser URL Interception (Chrome, Brave, Edge, Firefox)*

---

## 🎯 Objetivo
Evitar que el usuario eluda el bloqueo de aplicaciones abriendo las versiones web de Instagram, Reddit, Twitter/X, TikTok o YouTube en el navegador.

---

## 🛠️ Especificaciones Técnicas

### 1. Detección de URLs vía `AccessibilityService`
El servicio de accesibilidad `FocusAccessibilityService` ya procesa eventos de cambio de ventana y de contenido. Se añadirá inspección de los nodos de la barra de direcciones (Omnibox) para los navegadores más populares:

```kotlin
// IDs comunes de la barra de direcciones
val BROWSER_URL_BAR_IDS = mapOf(
    "com.android.chrome" to "url_bar",
    "com.brave.browser" to "url_bar",
    "com.microsoft.emmx" to "url_bar",
    "org.mozilla.firefox" to "mozac_browser_toolbar_url_view",
    "com.opera.browser" to "url_field"
)
```

### 2. Algoritmo de Coincidencia de Dominios
- Lista de dominios bloqueados vinculada a las aplicaciones seleccionadas:
  - `instagram.com`, `m.instagram.com`
  - `reddit.com`, `old.reddit.com`
  - `tiktok.com`
  - `x.com`, `twitter.com`
  - `youtube.com`, `m.youtube.com`
  - Posibilidad de que el usuario añada URLs personalizadas (ej. `facebook.com`, `noticias.com`).

### 3. Intervención Visual
- En cuanto se detecta una URL restringida en la barra del navegador, se lanza de inmediato la pantalla de respiración con `TYPE_ACCESSIBILITY_OVERLAY`.
- Al pulsar "Volver a mi enfoque", el servicio envía una acción `GLOBAL_ACTION_BACK` al navegador para cerrar la pestaña o regresar a la página anterior.

---

## ✅ Criterios de Aceptación
1. Abrir `instagram.com` en Chrome o Brave activa inmediatamente la pantalla de respiración de 10 segundos.
2. Posibilidad de agregar y eliminar dominios web personalizados desde la pestaña **Apps**.
