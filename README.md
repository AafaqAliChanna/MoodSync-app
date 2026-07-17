# MoodSync 🌟

**MoodSync** is a lightweight, intuitive mood-tracking Android application designed to help users monitor their emotional well-being through journaling and data visualization. 

This project was built as a robust MVP (Minimum Viable Product) focusing on privacy and local performance. It operates entirely offline, using zero external APIs or cloud services.

---

## ✨ New Features
- **Dynamic Splash Screen**: A beautiful opening phase that introduces the app with a smooth transition.
- **About Developer Profile**: A dedicated, scrollable profile section for **Aafaq Ali Channa**, detailing his professional ventures in software engineering, research, and community leadership.

---

## 🚀 Key Features

| Feature | Description |
|---|---|
| **Intelligent Dashboard** | Get a quick snapshot of your day: today's mood, a 7-day visualization strip, and your weekly happiness average. |
| **Smart Mood Logging** | Write freely in your journal. The app uses a keyword-based detection system to automatically suggest your mood, or you can select it manually. |
| **Visual History** | A custom-built, canvas-drawn bar chart showing your mood trends over the last 7 days, paired with a detailed history list. |
| **Tailored Recommendations** | Receive personalized, mood-based tips to help you stay calm, get motivated, or find balance. |

---

## 🛠️ Technical Stack

- **Language**: Java
- **Architecture**: Model-View-Controller (MVC)
- **Storage**: `SharedPreferences` with JSON serialization (100% Offline & Private).
- **UI Framework**: XML with Material Design Components, `CardView`, and `ConstraintLayout`.
- **Custom Graphics**: Advanced `Canvas` drawing for the Mood History Bar Chart.
- **Mood Detection**: Internal algorithmic keyword analysis (supporting 5 moods and 50+ emotional triggers).

---

## 📂 Project Structure

```text
MoodSync/
├── app/src/main/
    ├── java/com/example/moodsync/
    │   ├── SplashActivity.java        — Opening splash sequence
    │   ├── MainActivity.java          — Central dashboard
    │   ├── AboutDeveloperActivity.java — Developer biography & profile
    │   ├── MoodEntryActivity.java     — Journaling & logging
    │   ├── MoodHistoryActivity.java   — Visual trends & chart
    │   ├── RecommendationsActivity.java — Mood-based tips
    │   ├── MoodEntry.java             — Data model
    │   ├── MoodManager.java           — Local persistence logic
    │   ├── MoodDetector.java          — Keyword analysis engine
    │   └── views/
    │       └── MoodChartView.java     — Custom Canvas drawing
    └── res/
        ├── layout/                    — UI layouts
        ├── values/                    — Styles, colors, and strings
```

---

## 👤 About the Developer

**Aafaq Ali Channa (Muhammad Ismail Channa)**  
*Software Engineer | Independent Researcher | Arena Candidate Master (ACM)*

Associated with **DHA Suffa University**, Sindh, Pakistan. Aafaq applies strategic analysis from competitive chess to build scalable software solutions and document socio-political issues through his "Marginalized Maps" research series.

---

## ⚙️ Requirements & Installation

1.  **Android Studio**: Hedgehog (2023.1.1) or newer.
2.  **SDK**: Android API 26+ (Oreo) or higher.
3.  **Setup**:
    *   Clone this repository.
    *   Open the project in Android Studio.
    *   Wait for Gradle to sync.
    *   Run on an emulator or physical device.

---

## 📄 License
This project is developed for educational and MVP purposes.

also made apk
