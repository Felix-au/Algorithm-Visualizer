# AlgoBuddy v1.0.0 Release Notes

We are thrilled to announce the first official stable release of **AlgoBuddy** (v1.0.0)! AlgoBuddy is an interactive offline desktop application that combines step-by-step algorithm visualizations with a fully featured local code compilation sandbox.

---

## 🚀 Key Features

### 🎬 Interactive Visualizer
* **Time-Travel Playback**: Play, Pause, Step-Forward (`>`), and Step-Backward (`<`) using a state snapshot restore mechanism.
* **Variable Tracker**: Inspect stack frames, loop indices, and local variables in real-time as the visualization runs.
* **Speed Controller**: Slide to dynamically alter step delays for fast or detailed inspections.
* **Multiple Renderers**: Visualizations are mapped to custom Array, Graph, Bar Chart, Maze, Grid, and Chessboard nodes.

### 💻 Code Compilation Sandbox
* **Offline Compilers**: Native support for sandboxed local GCC (MinGW), Java JDK, and Python runtimes.
* **Interactive Terminal**: Split-pane terminal console supporting real-time `stdin` inputs and stdout/stderr redirection.
* **Syntax Highlighting**: Custom RichTextFX editor configured with line numbering and syntax styling.
* **Dynamic Compiler Scan**: Automatic local folder scans (up to depth 5) that dynamically resolve compiler executable paths.

### ⚡ Core Enhancements
* **Logo Reveal Splash Screen**: Branded loading splash screen running at `1.5x` playback speed with automatic mouse/keypress skip functionality.
* **Setup Wizard Installer**: Automated downloader UI with progress indicators to download and extract missing offline toolchains.

---

## 🔧 Bug Fixes and Stability Patches

* **GCC Sub-Process Assembler (`as.exe`) Fix**: Prepend the compiler's parent `bin/` folder directly to the sandbox environment `PATH` to resolve `CreateProcess: No such file or directory` errors during compilation.
* **JavaFX Media Module Access Fix**: Added `javafx.media` to the `--add-modules` Launch4j JVM arguments to prevent `IllegalAccessError` crash issues in modular JRE environments.
* **Unpack ZIP Extraction Fix**: Swapped sequential `ZipInputStream` out for random-access `ZipFile` parser to prevent `ZipException: only DEFLATED entries can have EXT descriptor` crashes on stored directories.
* **Scene Graph Transition Fix**: Cleared intermediate `StackPane` wrapper child lists before changing scene roots to avoid parenting errors (`IllegalArgumentException: ... is already inside a scene-graph`).
* **Media Engine Codec Fallback**: Wrapped splash screen media players in a fail-safe exception catch to automatically bypass video playback and load the dashboard directly if system codecs are missing.

---

## 📦 Runtime Requirements

* **Local Runtimes**: To launch `AlgoBuddy.exe` without installing Java, ensure the `jre/` and OpenJFX SDK (`openjfx-21.0.7_windows-x64_bin-sdk/`) folders are located in the application root directory alongside the executable.
* **Offline Compilers**: Compilers will download automatically via the setup wizard or can be placed directly at the root (e.g., `mingw64/`, `jdk-17.0.18/`, `python-3.11.8/`).
