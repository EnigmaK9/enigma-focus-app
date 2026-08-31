# 🌐 Paso 2: Cerrar el Bypass Web en Navegadores
## *"Si una puerta está cerrada, no dejes la ventana abierta"*

---

## 🎯 Propósito
Garantizar que el bloqueo de aplicaciones sea hermético. Si el usuario tiene bloqueado Instagram o Reddit, evitar que simplemente abra `instagram.com` o `reddit.com` en Chrome, Brave, Edge o Firefox.

---

## 🛠️ Implementación
1. Utilizar el mismo `FocusAccessibilityService` para inspeccionar pasivamente los nodos de la barra de direcciones (`Omnibox` / `url_bar`).
2. Mapear dominios a las aplicaciones de la lista de bloqueo (`instagram.com`, `reddit.com`, `tiktok.com`, `x.com`, `youtube.com`).
3. Al detectar la URL durante una sesión o jornada activa, desplegar de inmediato la superposición nativa de respiración de 10 segundos.

---

## 💡 Principio Unix
- **Consistencia y coherencia**: El bloqueo debe cumplir su regla sin agujeros evidentes de comportamiento.
