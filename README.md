# DriveSafe (LWMS: Lightweight Multimodal System)

DriveSafe is an advanced, camera-free, on-device Android application designed to detect driver distraction (texting, scrolling, tapping, calling) and grade its severity (**Low / Moderate / High**) using a deterministic, expert-rule decision tree.

## Overview & Screenshots

| Simulator (Home) | History Logs | Safety Dashboard |
| :---: | :---: | :---: |
| ![Simulator](Ressources/simulator.png) | ![History](Ressources/history.png) | ![Dashboard](Ressources/dashboard.png) |

## Key Features
- **Deterministic 8-Layer Decision Tree**: Pure-Kotlin core logic evaluating speed, IMU motion, and app-level behavior without requiring cloud ML or training data.
- **Multimodal Telemetry & Data Streams**:
  - OBD-II speed via Bluetooth ELM327 or interactive Virtual OBD-II Simulator.
  - Phone IMU (accelerometer & gyroscope) for handling classification.
  - App-level behavior via Android AccessibilityService & TelephonyManager.
- **Precision Telemetry UI Kit**: 3 primary screens (Simulator, History, and Dashboard) linked via a bottom navigation bar, built with Jetpack Compose following the Precision Telemetry Light design system.
- **Persistent Room Database**: Local SQLite persistence ensuring logs and driving sessions remain stored across app restarts.
- **Instant Severity-Mapped Notifications**: Real-time notifications mapped precisely to risk levels (Low = muted, Moderate = bip, High = high alert with sound, vibration, and TTS).

## Deterministic Decision Tree (8-Layer Logic)

The core decision engine (`DecisionTree.kt`) operates via an authoritative 8-layer rule evaluation per window (5s stride):

1. **R0 (Stationary)**: If vehicle speed `< 5 km/h`, severity defaults to `NONE` (overriding all distractions when parked).
2. **R1 (Navigation Exemption)**: If foreground app is `NAVIGATION` and phone handling is `NO` (cradled), severity defaults to `NONE`.
3. **R2 (Inactivity)**: If no active interaction is detected (`!is_active`), severity is `NONE`.
4. **R3 (Texting)**: If typing is detected (`isTyping`), base severity is `HIGH`.
5. **R4 (Calling)**: Evaluates active phone calls:
   - Handled call -> `HIGH`
   - Hands-free / unknown handling -> `MODERATE`
6. **R5 (Scrolling / Tapping / Unspecified)**:
   - Tapping (`> 1.5 taps/s`) -> `MODERATE` (beats scrolling)
   - Scrolling -> `LOW`
   - Unspecified handling -> `MODERATE`
7. **R6 (Speed Escalation)**: Scales base severity tiers by vehicle speed:
   - `vMean ≥ 60 km/h` -> +2 tiers (capped at `HIGH`)
   - `30 ≤ vMean < 60 km/h` -> +1 tier
   - `vMean < 30 km/h` -> +0 tiers
8. **R7 (Hysteresis)**: Requires **3 consecutive evaluation windows** (`N_on = 3`) to emit an alert, and **3 consecutive NONE windows** (`N_off = 3`) to clear, eliminating false alarms.

## Architecture
- **`logic/`**: Pure-Kotlin decision tree, hysteresis state machine, feature vector models, and configuration defaults.
- **`features/`**: Feature extraction and z-normalized phone-handled classification.
- **`sensor/`**: OBD-II simulator and telemetry data sources.
- **`data/`**: Room database entities, DAOs, converters, and persistent event repository.
- **`service/`**: Foreground monitoring service and accessibility event listeners.
- **`ui/`**: Jetpack Compose screens (Dashboard, History, Simulator, Splash).

## Research Paper & Specifications
- **Source Material**: [Progress Report v2.pdf](Ressources/Progress%20Report%20v2.pdf) (Based on *Progress Report #2, "Proposed DT Approach", RIF Grant 23081, Aug 2026*).

## How to Use
1. Clone the repository and open in Android Studio (Giraffe or newer).
2. Grant notification permissions and enable the unified **DriveSafe** Accessibility Service in system settings.
3. Use the **Simulator** screen to adjust driving speed or connect to OBD-II, and test distraction interactions (texting, scrolling, tapping).
4. Monitor risk distribution, history logs, and session analytics in the **Dashboard** and **History** tabs.
