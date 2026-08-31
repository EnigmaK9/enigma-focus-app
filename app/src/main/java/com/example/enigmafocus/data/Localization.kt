package com.example.enigmafocus.data

object AppStrings {

    fun get(key: String, isEnglish: Boolean = true): String {
        return if (isEnglish) enStrings[key] ?: key else esStrings[key] ?: enStrings[key] ?: key
    }

    private val enStrings = mapOf(
        // Tabs
        "tab_focus" to "Focus",
        "tab_apps" to "Apps",
        "tab_settings" to "Settings",

        // Focus Screen
        "focus_active_title" to "Focus Session Active",
        "focus_inactive_title" to "Focus Session",
        "focus_active_subtitle" to "Instagram, Reddit, and distracting apps are blocked.",
        "focus_inactive_subtitle" to "Choose your focus duration or set automated schedules below.",
        "in_concentration" to "In Focus",
        "btn_start_session" to "Start Session",
        "btn_stop_session" to "Stop Session",
        "session_duration_title" to "Session Duration",
        "min_25" to "25 min",
        "hr_1" to "1 hour",
        "hr_4" to "4 hours",
        "hr_9_workday" to "9h Workday",

        // Scheduled Intervals
        "intervals_card_title" to "Schedules & Intervals",
        "intervals_card_subtitle" to "Automated blocking during your work and sleep hours",
        "btn_add_interval" to "Add Schedule / Interval",
        "badge_in_progress" to "IN PROGRESS",
        "interval_workday_label" to "Workday Shift",
        "interval_sleep_label" to "Rest / Sleep",
        "active_interval_alert" to "Scheduled Interval Active",
        "all_days" to "Every day",
        "mon_to_fri" to "Mon to Fri",
        "weekends" to "Weekends",

        // Controls
        "phone_controls_title" to "Phone Controls",
        "grayscale_mode_title" to "Grayscale Mode",
        "grayscale_mode_active" to "Monochrome Screen (Black & White)",
        "grayscale_mode_inactive" to "Full Color Screen",
        "auto_grayscale_title" to "Auto Grayscale on Focus",
        "auto_grayscale_desc" to "Turns screen black & white during active focus/schedules",
        "strict_mode_title" to "Strict Mode (No Breaks)",
        "strict_mode_desc" to "Full lockout with no 1-minute temporary unlock option",
        "always_block_title" to "Always Block Distractions",
        "always_block_desc" to "Block distractions 24/7 even outside timer sessions",
        "anti_impulse_title" to "Anti-Impulse Cooldown (60s)",
        "anti_impulse_desc" to "Requires a 60-second reflection countdown before stopping focus",

        // Bedtime Pop-up
        "sleep_nudge_title" to "Time to sleep! 🌙",
        "sleep_nudge_body" to "Your sleep schedule (10:30 PM - 6:30 AM) is active.\n\nScreen light disrupts melatonin and deep rest. Put the phone down and give your body the rest it deserves.",
        "btn_sleep_lock" to "Turn off screen & Go to sleep",
        "btn_sleep_snooze" to "Remind me in 10 min",

        // Breathing Block Screen
        "block_access_to" to "Access Blocked:",
        "block_breathe_title" to "Take a Mindful Breath",
        "block_breathe_subtitle" to "Slow down before opening distractions.",
        "phase_inhale" to "Inhale",
        "phase_hold" to "Hold",
        "phase_exhale" to "Exhale",
        "phase_ready" to "Ready",
        "btn_back_to_focus" to "Back to my focus",
        "btn_use_1_min" to "Use app for 1 minute",

        // Apps Screen
        "apps_title" to "Blocked Apps",
        "apps_subtitle" to "Select which apps interrupt your focus",
        "search_placeholder" to "Search installed apps...",
        "popular_card_title" to "Popular Distractions",
        "popular_card_subtitle" to "Instagram, Reddit, TikTok, X, YouTube...",
        "btn_block_all_popular" to "Block All Popular",
        "btn_unblock_all" to "Unblock All",
        "browser_url_blocking_title" to "Browser URL Interception",
        "browser_url_blocking_desc" to "Automatically blocks web versions in Chrome, Brave, Edge & Firefox",

        // Settings Screen
        "settings_title" to "Settings & System",
        "settings_subtitle" to "Configuration, Shizuku, backups & command integration",
        "language_title" to "Language / Idioma",
        "language_desc" to "Select application display language",
        "permission_accessibility_title" to "Accessibility Service",
        "permission_accessibility_active" to "Active (Window & URL interception)",
        "permission_accessibility_inactive" to "Disabled",
        "permission_accessibility_desc" to "Instantly catches blocked apps and displays the mindful breathing screen.",
        "permission_grayscale_title" to "Grayscale (Secure Settings)",
        "permission_grayscale_active" to "Permission granted",
        "permission_grayscale_inactive" to "Requires one-time ADB or Shizuku grant",
        "permission_grayscale_desc" to "WRITE_SECURE_SETTINGS for system-level monochrome GPU toggle.",
        "btn_shizuku_grant" to "Grant via Shizuku (1-Tap)",
        "json_backup_title" to "Plaintext JSON Configuration",
        "json_backup_desc" to "Export or import your schedules and blocked apps in a clean, human-readable file.",
        "btn_export_json" to "Export Configuration (JSON)",
        "btn_import_json" to "Import Configuration (JSON)",
        "unix_intents_title" to "CLI & Tasker Automation",
        "unix_intents_desc" to "Control focus and grayscale from scripts or terminal via ADB broadcast intents."
    )

    private val esStrings = mapOf(
        // Tabs
        "tab_focus" to "Enfoque",
        "tab_apps" to "Apps",
        "tab_settings" to "Ajustes",

        // Focus Screen
        "focus_active_title" to "Sesión de Enfoque Activa",
        "focus_inactive_title" to "Sesión de Concentración",
        "focus_active_subtitle" to "Instagram, Reddit y apps distractoras están pausadas.",
        "focus_inactive_subtitle" to "Elige la duración o programa horarios automáticos abajo.",
        "in_concentration" to "En concentración",
        "btn_start_session" to "Iniciar Sesión",
        "btn_stop_session" to "Detener Sesión",
        "session_duration_title" to "Duración de la sesión",
        "min_25" to "25 min",
        "hr_1" to "1 hora",
        "hr_4" to "4 horas",
        "hr_9_workday" to "9h Jornada",

        // Scheduled Intervals
        "intervals_card_title" to "Horarios e Intervalos",
        "intervals_card_subtitle" to "Bloqueo automático en tus horas de trabajo y descanso",
        "btn_add_interval" to "Añadir Horario / Intervalo",
        "badge_in_progress" to "EN CURSO",
        "interval_workday_label" to "Jornada Laboral",
        "interval_sleep_label" to "Descanso / Dormir",
        "active_interval_alert" to "Intervalo Programado Activo",
        "all_days" to "Todos los días",
        "mon_to_fri" to "Lun a Vie",
        "weekends" to "Fines de semana",

        // Controls
        "phone_controls_title" to "Controles del Teléfono",
        "grayscale_mode_title" to "Modo Escala de Grises",
        "grayscale_mode_active" to "Pantalla en Blanco y Negro",
        "grayscale_mode_inactive" to "Pantalla a Todo Color",
        "auto_grayscale_title" to "Auto Escala de Grises en Sesión",
        "auto_grayscale_desc" to "Se activa en blanco y negro al iniciar sesión o en horarios activos",
        "strict_mode_title" to "Modo Estricto (Sin Pausas)",
        "strict_mode_desc" to "Bloqueo total sin opción a usar 1 min tras respirar",
        "always_block_title" to "Bloqueo Siempre Activo",
        "always_block_desc" to "Bloquear continuamente las 24 horas del día",
        "anti_impulse_title" to "Retardo Anti-Impulso (60s)",
        "anti_impulse_desc" to "Exige 60 segundos de espera reflexiva antes de detener el enfoque",

        // Bedtime Pop-up
        "sleep_nudge_title" to "¡Es hora de ir a dormir! 🌙",
        "sleep_nudge_body" to "Tu horario de descanso (10:30 PM - 6:30 AM) está activo.\n\nLa luz de la pantalla reduce la melatonina y altera tu descanso. Deja el teléfono y dale a tu cuerpo la noche que merece.",
        "btn_sleep_lock" to "Apagar pantalla e ir a dormir",
        "btn_sleep_snooze" to "Recordármelo en 10 min",

        // Breathing Block Screen
        "block_access_to" to "Acceso bloqueado:",
        "block_breathe_title" to "Toma una pausa consciente",
        "block_breathe_subtitle" to "Respira hondo antes de abrir distracciones.",
        "phase_inhale" to "Inhala",
        "phase_hold" to "Sostén",
        "phase_exhale" to "Exhala",
        "phase_ready" to "Listo",
        "btn_back_to_focus" to "Volver a mi enfoque",
        "btn_use_1_min" to "Usar app por 1 minuto",

        // Apps Screen
        "apps_title" to "Aplicaciones Bloqueadas",
        "apps_subtitle" to "Selecciona qué aplicaciones interrumpen tu concentración",
        "search_placeholder" to "Buscar aplicaciones instaladas...",
        "popular_card_title" to "Distracciones Populares",
        "popular_card_subtitle" to "Instagram, Reddit, TikTok, X, YouTube...",
        "btn_block_all_popular" to "Bloquear Populares",
        "btn_unblock_all" to "Desbloquear Todas",
        "browser_url_blocking_title" to "Intercepción Web en Navegadores",
        "browser_url_blocking_desc" to "Bloquea automáticamente las versiones web en Chrome, Brave, Edge y Firefox",

        // Settings Screen
        "settings_title" to "Ajustes y Sistema",
        "settings_subtitle" to "Configuración, Shizuku, copias de seguridad e integraciones",
        "language_title" to "Idioma / Language",
        "language_desc" to "Selecciona el idioma de la aplicación (English por defecto)",
        "permission_accessibility_title" to "Servicio de Accesibilidad",
        "permission_accessibility_active" to "Activo (Detección de ventanas y URLs)",
        "permission_accessibility_inactive" to "Desactivado",
        "permission_accessibility_desc" to "Detecta en tiempo real cuando abres apps bloqueadas para desplegar la pantalla de respiración.",
        "permission_grayscale_title" to "Escala de Grises (Ajustes Seguros)",
        "permission_grayscale_active" to "Permiso concedido",
        "permission_grayscale_inactive" to "Requiere permiso único por ADB o Shizuku",
        "permission_grayscale_desc" to "WRITE_SECURE_SETTINGS para activar la escala de grises nativa por GPU.",
        "btn_shizuku_grant" to "Conceder con Shizuku (1 Toque)",
        "json_backup_title" to "Configuración en Texto Plano (JSON)",
        "json_backup_desc" to "Exporta o importa tus horarios y lista de apps bloqueadas en un archivo limpio y legible.",
        "btn_export_json" to "Exportar Configuración (JSON)",
        "btn_import_json" to "Importar Configuración (JSON)",
        "unix_intents_title" to "Automatización por CLI y Tasker",
        "unix_intents_desc" to "Controla el enfoque y la escala de grises desde scripts o terminal mediante Intents de broadcast."
    )
}
