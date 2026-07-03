# Dynamic Composition Grid Overlay 📸📐

A lightweight, high-performance Android utility application that renders a system-level floating transparent grid overlay on top of any native mobile camera application. It helps photographers, filmmakers, and content creators achieve professional framing and mathematical composition (e.g., Rule of Thirds, Golden Spiral, Dynamic Symmetry) in real-time.

---

## ✨ Features

- **System-Level Floating Overlay**: Runs as a persistent background service. Stays visible even when you minimize this app and open your phone's native camera.
- **Touch-Passthrough Mode**:
  - **Unlocked**: Move and configure overlay settings (grid mode, opacity, line thickness).
  - **Locked**: Enable touch-passthrough so you can tap through the grid to control the underlying camera app (focus, exposure, shutter button).
- **18 Composition Modes**:
  - Rule of Thirds
  - Golden Ratio (Phi Grid)
  - Golden Spiral (Fibonacci Spiral in 4 rotations)
  - Golden Triangle (2 orientations)
  - Diagonal Method
  - Dynamic Symmetry Grid
  - Harmonic Armature
  - Center Crosshair
  - Custom Grid (adjustable grid lines)
- **Real-Time Adjustments**: Customize grid line opacity, color, and thickness on the fly using the overlay panel.
- **Strictly Mobile Optimized**: Tailored for both portrait and landscape camera layouts on smartphones.

---

## 📲 Direct APK Installation (Quick Start)

We have provided a pre-compiled debug APK inside the repository:
👉 **[Download Dynamic-Grid-Overlay.apk](./release/Dynamic-Grid-Overlay.apk)** (or grab it from the root folder).

### How to install using USB Debugging:
1. Connect your phone to your laptop via USB.
2. Ensure **USB Debugging** is turned on in your phone's *Developer Options*.
3. Open a terminal on your computer and run:
   ```bash
   adb install Overlay-Debug.apk
   ```

---

## 🛠️ Project Setup & Build Instructions

If you want to build the project from scratch, follow these instructions.

### Prerequisites
- JDK 17
- Android SDK (Platform 34, Build-Tools 34)
- Gradle 8.4+

*Note: The project contains an automated build bootstrap script (`build_apk.ps1`) for Windows users to automatically setup Java, Android SDK, and Gradle.*

### Build via Command Line
If your environment is set up, run:
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Permissions Required
- **System Alert Window (`SYSTEM_ALERT_WINDOW`)**: Required to draw the grid and control panel over the camera and other apps. The app will guide you to enable "Display over other apps" on launch.

---

## 🧑‍💻 Technical Architecture
- **Kotlin**: Core language.
- **WindowManager Service**: Used to attach the transparent grid custom View (`GridOverlayView`) and the control panel (`control_panel.xml`) directly to the system window hierarchy with dynamic layout parameters.
- **Canvas API**: Custom drawing logic for rendering the mathematical formulas of the compositions.
- **FLAG_NOT_FOCUSABLE & FLAG_NOT_TOUCHABLE**: Used to achieve touch-passthrough dynamically when the grid is locked.

---

## 📄 License
This project is open-source. Feel free to use, modify, and distribute.
