# 📊 Enigma Focus — Flowcharts & Sequence Diagrams

<p align="center">
  <strong>Visual Architecture, Lifecycle Sequences, and State Machines (Mermaid)</strong>
</p>

---

## 🌐 Table of Contents / Tabla de Contenidos
- [1. App Interception & Blocking Flow](#1-app-interception--blocking-flow)
- [2. Focus Session & Interval Scheduling Lifecycle](#2-focus-session--interval-scheduling-lifecycle)
- [3. Mindful Breathing Overlay State Machine](#3-mindful-breathing-overlay-state-machine)
- [4. Bedtime Sleep Nudge Popup Flow](#4-bedtime-sleep-nudge-popup-flow)
- [5. System Grayscale Hardware Daltonizer State Flow](#5-system-grayscale-hardware-daltonizer-state-flow)

---

## 1. App Interception & Blocking Flow

This flowchart illustrates how `FocusAccessibilityService` detects an app launch, evaluates active schedules and blocklists, and displays the mindful breathing overlay.

```mermaid
flowchart TD
    A["User Launches an App (e.g., Instagram / Reddit)"] --> B["Accessibility Event / Watchdog Polling (300ms)"]
    B --> C{"Is package System UI, Launcher, or Enigma Focus?"}
    C -- "Yes" --> D["Dismiss overlay if active; Do nothing"]
    C -- "No" --> E{"Is Package in Blocked List?"}
    E -- "No" --> F["Allow App normal execution"]
    E -- "Yes" --> G{"Is Temporary Whitelist Active for this App?"}
    G -- "Yes" --> F
    G -- "No" --> H{"Is Focus Active OR Scheduled Interval Active OR Always Block ON?"}
    H -- "No" --> F
    H -- "Yes" --> I["Trigger Haptic Vibration Feedback"]
    I --> J["Attach ComposeView with TYPE_ACCESSIBILITY_OVERLAY to WindowManager"]
    J --> K["Display Mindful Breathing Screen (10s Countdown)"]
```

---

## 2. Focus Session & Interval Scheduling Lifecycle

Sequence diagram demonstrating the interaction between the User, `MainScreenViewModel`, `AppPreferences`, `FocusSessionManager`, `GrayscaleManager`, and the Background Services.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as FocusScreen (Compose)
    participant VM as MainScreenViewModel
    participant Prefs as AppPreferences (StateFlow)
    participant FSM as FocusSessionManager
    participant GM as GrayscaleManager
    participant Svc as FocusAccessibilityService

    Note over User, Svc: Scenario A: User Starts Manual Focus Session (e.g., 9 Hours)
    User->>UI: Taps "Iniciar Sesión (9h Jornada)"
    UI->>VM: startFocusSession(540)
    VM->>FSM: startSession(context, 540)
    FSM->>Prefs: setFocusActive(true, endTimestamp, 540)
    alt Auto Grayscale is Enabled
        FSM->>GM: setGrayscaleEnabled(context, true)
        GM->>GM: Settings.Secure.putInt(DALTONIZER, Monochrome)
    end
    Prefs-->>VM: focusActiveFlow emits (true)
    VM-->>UI: Displays Countdown Timer (09:00:00)

    Note over User, Svc: Scenario B: Scheduled Interval (Workday 07:30 - 16:30)
    Svc->>Prefs: isAnyScheduledIntervalActive()
    Prefs-->>Svc: Returns true (Current time is 10:15 AM on Tuesday)
    User->>Svc: Opens Instagram
    Svc->>UI: Intercepts & displays TYPE_ACCESSIBILITY_OVERLAY
```

---

## 3. Mindful Breathing Overlay State Machine

State diagram representing the 10-second mindful breathing exercise phases.

```mermaid
stateDiagram-v2
    [*] --> InhalePhase: Overlay Attached to Screen
    
    state InhalePhase {
        [*] --> Inhaling
        Inhaling --> ExpandingCircle: Seconds 10..8
        note right of ExpandingCircle: "Inhala profundamente por la nariz"
    }

    InhalePhase --> HoldPhase: Seconds 7..5
    
    state HoldPhase {
        HoldingBreath --> GlowingReticle: Seconds 7..5
        note right of GlowingReticle: "Sostén el aire con calma"
    }

    HoldPhase --> ExhalePhase: Seconds 4..1

    state ExhalePhase {
        Exhaling --> ContractingCircle: Seconds 4..1
        note right of ContractingCircle: "Exhala suavemente y suelta tensión"
    }

    ExhalePhase --> CompletedPhase: Seconds = 0

    state CompletedPhase {
        ReadyState --> ActionButtonsVisible: Unlocks "Usar app 1 min" button (if not Strict Mode)
    }

    CompletedPhase --> HomeAction: User taps "Volver a mi enfoque"
    CompletedPhase --> TemporaryUnlock: User taps "Usar 1 minuto"
    
    HomeAction --> [*]: Removes Overlay & Minimizes to Home
    TemporaryUnlock --> [*]: Sets 1-min Whitelist & Dismisses
```

---

## 4. Bedtime Sleep Nudge Popup Flow

Flowchart representing nighttime automatic sleep detection (`22:30 - 06:30`).

```mermaid
flowchart TD
    A["Watchdog ticks during Sleep Hours (22:30 - 06:30)"] --> B{"Is Active Interval 'Descanso / Dormir'?"}
    B -- "No" --> C["Standard Execution"]
    B -- "Yes" --> D{"Is User on Launcher or Lockscreen?"}
    D -- "Yes" --> C
    D -- "No" --> E{"Can Show Nudge? (Snooze cooldown expired > 10m)"}
    E -- "No" --> C
    E -- "Yes" --> F["Show Floating Sleep Nudge Popup (SleepOverlayManager)"]
    F --> G{"User Selection"}
    G -- "Apagar pantalla e ir a dormir" --> H["Lock Screen / Minimize to Home & Enable Grayscale"]
    G -- "Recordármelo en 10 min" --> I["Dismiss Popup & Set 10-min Snooze Timer"]
```

---

## 5. System Grayscale Hardware Daltonizer State Flow

```mermaid
flowchart LR
    A["User Switch / Session Start / Sleep Trigger"] --> B["GrayscaleManager.setGrayscaleEnabled(context, enabled)"]
    B --> C{"Check WRITE_SECURE_SETTINGS Permission"}
    C -- "Not Granted" --> D["Log Warning & Prompt User via SettingsScreen ADB Guide"]
    C -- "Granted" --> E{"enabled == true"}
    E -- "Yes" --> F["Settings.Secure.putInt(accessibility_display_daltonizer_enabled, 1)"]
    F --> G["Settings.Secure.putInt(accessibility_display_daltonizer, 0) (Monochrome GPU)"]
    E -- "No" --> H["Settings.Secure.putInt(accessibility_display_daltonizer_enabled, 0)"]
    H --> I["Settings.Secure.putInt(accessibility_display_daltonizer, -1) (Full Color Restored)"]
```
