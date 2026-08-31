# 📱 Paso 5: Widgets Modernos con Jetpack Glance
## *Interactive Home Screen Widgets with Material You*

---

## 🎯 Objetivo
Permitir al usuario controlar sus sesiones de concentración, alternar la escala de grises y ver el estado de su racha diaria directamente desde la pantalla de inicio de su teléfono sin necesidad de abrir la app.

---

## 🛠️ Especificaciones Técnicas

### 1. Framework: Jetpack Glance
- Uso de `androidx.glance:glance-appwidget` y `androidx.glance:glance-material3`.
- Soporte nativo para colores dinámicos (*Material You / Dynamic Colors* en Android 12+).

### 2. Variantes de Widgets Diseñadas

#### A. Widget Compacto (2x2) — *Quick Switcher*
- Indicador del estado actual: 🟢 *Enfocado (05h 22m)* / ⚪ *Inactivo*.
- Botón de 1 toque para alternar **Modo Escala de Grises**.
- Botón rápido para iniciar sesión de 25 min o pausar.

#### B. Widget Mediano (4x2) — *Dashboard de Concentración*
- Reloj con cuenta regresiva en vivo del turno actual (ej. `Jornada Laboral: 07:30 - 16:30`).
- Próximo intervalo programado (ej. `Descanso en 5 horas`).
- Mini medidor de racha diaria y estado de la planta Zen.

---

## ✅ Criterios de Aceptación
1. El widget se actualiza en tiempo real cada vez que cambia el estado de la sesión o el temporizador.
2. Tocar el botón de escala de grises en el widget cambia el color del teléfono de inmediato.
