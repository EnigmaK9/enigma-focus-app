# 🎛️ Paso 3: Control Rápido Minimalista (Widget Glance)
## *"Acceso directo sin interfaces innecesarias"*

---

## 🎯 Propósito
Permitir que el usuario alterne la escala de grises y consulte su horario actual desde la pantalla de inicio con 1 toque, sin tener que navegar por menús ni abrir la aplicación principal.

---

## 🛠️ Implementación
1. Desarrollar un widget 2x1 y 2x2 ultra-limpio con **Jetpack Glance**.
2. Mostrar únicamente:
   - Estado del horario actual (ej. `Jornada: 07:30 - 16:30`).
   - Botón directo de **Blanco y Negro / Color**.
   - Botón de **Iniciar / Detener Sesión**.
3. Cero consumo en segundo plano: el widget solo se redibuja cuando el estado de las preferencias cambia.

---

## 💡 Principio Unix
- **Economía de clics**: Las acciones frecuentes deben requerir el menor esfuerzo posible.
