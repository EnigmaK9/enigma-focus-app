# 📄 Paso 6: Configuración en Texto Plano (JSON Puro)
## *"Los datos deben ser legibles y portables por humanos"*

---

## 🎯 Propósito
Permitir que el usuario guarde, edite y transfiera toda su configuración (horarios, apps bloqueadas, duración predeterminada) mediante un archivo JSON simple, sin bases de datos complejas ni formatos propietarios.

---

## 🛠️ Implementación
1. Estructura de archivo estándar `enigma_focus_config.json`:
   ```json
   {
     "version": 1,
     "auto_grayscale": true,
     "always_block": true,
     "blocked_packages": [
       "com.instagram.android",
       "com.reddit.frontpage",
       "com.zhiliaoapp.musically"
     ],
     "intervals": [
       {
         "label": "Jornada Laboral",
         "start": "07:30",
         "end": "16:30",
         "days": [2, 3, 4, 5, 6],
         "enabled": true
       },
       {
         "label": "Descanso / Dormir",
         "start": "22:30",
         "end": "06:30",
         "days": [1, 2, 3, 4, 5, 6, 7],
         "enabled": true
       }
     ]
   }
   ```
2. Botones directos de **"Exportar Configuración"** e **"Importar Configuración"** usando el selector de archivos del sistema Android (`Storage Access Framework`).

---

## 💡 Principio Unix
- **Transparencia en los datos**: Guarda la información en texto plano para que el usuario siempre sea el dueño absoluto de su configuración.
