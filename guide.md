# AlgoBuddy: Your Offline Algorithm Playground — Quick Run Guide

An interactive desktop application for visualizing computer science algorithms and executing custom code. Select an algorithm, watch its step-by-step playback, control animation speeds, write and run C/C++/Java/Python code in the local compilation sandbox—all fully offline.

> [!IMPORTANT]
> **Unlike web-based visualizers** that require active internet connections and only show pre-programmed static animations, AlgoBuddy compiles and executes your actual custom source code locally. It leverages a dynamic environment resolver to safely sandbox executions on your machine.

---

## Table of Contents

- 🚀 [How to Run](#how-to-run)
  - [Option A: From Source (Development)](#option-a-from-source-development)
  - [Option B: Standalone Executable (Release)](#option-b-standalone-executable-release)
- 📦 [Packaging and Executable Building](#packaging-and-executable-building)
- 🎯 [How to Use](#how-to-use)
- 💻 [Interactive Code Sandbox Guide](#interactive-code-sandbox-guide)
- 🎨 [Dashboard Layout Settings](#dashboard-layout-settings)
- 📁 [Directory Index Checklist](#directory-index-checklist)
- ⚠️ [Important Notes](#important-notes)

---

## How to Run

### Option A: From Source (Development)

**Prerequisites:** Windows 10/11, Java JDK 11+, Apache Maven 3.6+, 8 GB RAM recommended.

To run the application from source in your development environment:

```powershell
# Compile the classes and compile resources
mvn clean compile

# Launch the JavaFX Application
mvn javafx:run
```

On first launch, the application displays a 1.5x sped-up logo reveal splash screen. It then runs an environment check. If local compilers (GCC/JDK/Python) are missing from the project directories, it automatically launches the **Setup Wizard Installer** to let you download and unpack these dependencies.

### Option B: Standalone Executable (Release)

If you have a compiled release distribution:
1. Double-click the pre-built `AlgoBuddy.exe` in the root folder.
2. The application runs immediately.
3. No JRE or Java setup is required on the user's machine, as the runtime is fully embedded inside the wrapper.

---

## Packaging and Executable Building

To build the executable and package it for distribution:

1. **Create Shaded Uber JAR**:
   Run Maven package with the shade profile:
   ```powershell
   mvn clean package
   ```
   This creates `target/algorithm-visualizer-1.0.0-all.jar` containing all compiled classes and JavaFX modules.

2. **Wrap Shaded JAR in Launch4j**:
   * Open the **Launch4j** visual wrapper compiler.
   * Open the provided Launch4j config file: [`algobuddy.xml`](algobuddy.xml).
   * Click **Build wrapper** (gear icon) to compile the executable.
   * The compiler outputs a fully packaged, self-contained standalone executable: `AlgoBuddy.exe`.

---

## How to Use

1. **Launch AlgoBuddy** — Watch the brief logo reveal splash screen.
2. **Select an Algorithm** — Click any algorithm card (e.g. Dijkstra, Quick Sort, N-Queens) on the category dashboard.
3. **Configure Parameters** — Adjust settings (e.g. array size, grid layout, graph nodes) in the visualizer controls panel.
4. **Interactive Playback**:
   * Click **Play** to start animating.
   * Click **Pause** to stop at any frame.
   * Use **Step Forward** (`>`) to progress exactly one state transition.
   * Use **Step Backward** (`<`) to return to the previous state transition (powered by solver state snapshots).
5. **Adjust Speed** — Slide the speed controller left or right to change animation step delays.

---

## Interactive Code Sandbox Guide

AlgoBuddy provides a split-pane code compiler console directly beside the visualization space:

1. **Write Code**: Enter your custom code inside the syntax-highlighted editor (RichTextFX).
2. **Interactive Run**: Click the **Run** button to launch compilation.
3. **Provide Console Input**:
   * If your script expects inputs (e.g., `scanf` in C or `input()` in Python), the console displays a yellow blinking prompt.
   * Type your input in the terminal and press `Enter`.
   * Stdin inputs are buffered and piped directly into the running sandbox process.

---

## Dashboard Layout Settings

Configuration options are maintained locally in the project root:

| File | Purpose |
|---|---|
| `.env` | Holds application variables, such as active version numbers (`VERSION=0.0`) |
| `algobuddy.xml` | Launch4j wrapper builder options (targets local modular JavaFX SDK paths) |

---

## Directory Index Checklist

Here is a checklist of critical files and directories mapping out the codebase architecture:

| Path | Description |
|---|---|
| `src/main/java/com/algorithmvisualizer/Main.java` | App entry point, stage initializer, and window manager |
| `src/main/java/com/algorithmvisualizer/execution/ExecutionEnvironment.java` | Recursive depth-5 scanner resolving compiler executable roots |
| `src/main/java/com/algorithmvisualizer/execution/CodeExecutor.java` | Core process engine sandboxing code execution and prepending PATHs |
| `src/main/java/com/algorithmvisualizer/ui/MainController.java` | Manages the main selection card dashboard and algorithm listings |
| `src/main/java/com/algorithmvisualizer/ui/AlgorithmViewController.java` | Directs unified visualizer playback controls and parameter panels |
| `src/main/java/com/algorithmvisualizer/ui/SplashView.java` | Coordinates 1.5x sped-up video splash screen and key/click skips |
| `src/main/java/com/algorithmvisualizer/ui/EnvInstallerController.java` | Wizard dialog automating download and ZIP unpacking of compilers |
| `src/main/java/com/algorithmvisualizer/algorithm/` | Contains modular Java algorithm step-solvers |
| `src/main/java/com/algorithmvisualizer/visualization/` | Holds visual node canvas renderers (Array, Graph, Maze, Chessboard) |
| `src/main/resources/fxml/` | Contains visual FXML grid and panel layouts |
| `src/main/resources/css/style.css` | Color styling tokens, Dark-Mode variables, and editor styles |

---

## Important Notes

* **Windows OS Focus** — Application building, runtime resolution, and keyboard/process hooks are optimized for Windows environments.
* **Offline Compilers** — All compiled binaries are sandboxed. The application automatically searches directories up to 5 levels deep to resolve local toolchains cleanly.
* **10-Second Sandbox Cap** — Executing custom user programs are forced-terminated after 10 seconds to protect host system stability against infinite loop scripts.
* **Codec Fallback** — In case of system JFX media codec issues, the splash screen safely catches exceptions and instantly loads the main dashboard layout.
* **Flicker-Free Transitions** — Switching between scenes is managed directly on the primary Application Stage to guarantee high-performance, seamless UI rendering.
