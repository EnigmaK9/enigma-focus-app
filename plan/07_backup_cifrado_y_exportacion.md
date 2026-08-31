# 🔒 Paso 7: Copias de Seguridad Cifradas y Exportación
## *Encrypted Local Backups, Auto-Sync & CSV/JSON Data Export*

---

## 🎯 Objetivo
Permitir que el usuario guarde, restaure y exporte toda su configuración (horarios personalizados, listas de apps bloqueadas y estadísticas de concentración) con total privacidad y control sobre sus datos.

---

## 🛠️ Especificaciones Técnicas

### 1. Cifrado Local con `AndroidKeyStore`
- Exportación de un archivo `.enigmafocus` protegido mediante **AES-256-GCM** y clave derivada por contraseña del usuario (o vinculada al hardware del dispositivo vía `MasterKeys`).
- Incluye:
  - Lista de paquetes bloqueados y dominios web.
  - Horarios e intervalos programados (Jornada, Dormir, Estudio).
  - Historial de sesiones y rachas acumuladas.

### 2. Exportación de Datos Abiertos
- Botón **"Exportar Estadísticas (CSV)"** para importar en hojas de cálculo (Excel, Notion, Google Sheets) y analizar horas de concentración semanal.
- Botón **"Exportar Configuración (JSON)"** para compartir configuraciones recomendadas con otros usuarios o equipos de trabajo.

---

## ✅ Criterios de Aceptación
1. El usuario puede cambiar de teléfono y restaurar sus horarios y apps bloqueadas en 1 segundo con su archivo de copia de seguridad.
2. Cumplimiento total del principio de privacidad por diseño (*Privacy-by-Design*): ningún dato de uso o analítica sale del teléfono sin consentimiento explícito.
