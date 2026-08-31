# 📊 Paso 1: Analítica de Pantalla y Estadísticas de Enfoque
## *Screen Time Analytics & Focus Insights Dashboard*

---

## 🎯 Objetivo
Proporcionar al usuario un panel visual con gráficos interactivos que cuantifique el tiempo ahorrado, las horas de concentración completadas y las aplicaciones que más intentos de interrupción generan.

---

## 🛠️ Especificaciones Técnicas

### 1. API del Sistema
- Integración con `android.app.usage.UsageStatsManager` mediante el permiso `android.permission.PACKAGE_USAGE_STATS`.
- Cálculo de métricas:
  - **Tiempo de pantalla diario/semanal**: Tiempo total de uso activo del dispositivo.
  - **Tiempo en apps bloqueadas**: Comparativa antes y después del uso de Enigma Focus.
  - **Intentos de apertura interceptados**: Contador de veces que el usuario abrió Instagram/Reddit y fue redirigido al ejercicio de respiración.
  - **Tiempo recuperado**: Estimación de horas ganadas gracias al bloqueo.

### 2. Arquitectura de Base de Datos Local
- Base de datos **Room (SQLite)**:
  - `FocusSessionEntity`: Registro de sesiones completadas (inicio, fin, minutos, modo estricto).
  - `InterceptionLogEntity`: Registro de intentos de distracción bloqueados (timestamp, packageName).

### 3. Componentes Visuales en Compose
- Nueva pestaña en la barra de navegación: **Estadísticas**.
- Gráficos de barras interactivos (mediante Compose Canvas o librerías ligeras como `Vico` / `Patryk-Goworowski/vico`).
- Selector de rangos: **Hoy**, **Esta Semana**, **Este Mes**.

---

## ✅ Criterios de Aceptación
1. El usuario puede ver cuántas veces intentó abrir apps bloqueadas durante la jornada laboral.
2. Gráfico semanal con el total de horas de concentración acumuladas.
3. Medidor de "Minutos Salvados" basado en el tiempo promedio de uso previo.
