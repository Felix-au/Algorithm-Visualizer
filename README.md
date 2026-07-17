# AlgoBuddy - Algorithm Visualizer

<div align="center">

![AlgoBuddy Logo](src/main/resources/Logo.png)

**An interactive desktop application for visualizing computer science algorithms**

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-blue.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 📖 About

**AlgoBuddy** is an educational tool designed to help students, educators, and programming enthusiasts understand how algorithms work through interactive visualizations. Watch algorithms execute step-by-step, control playback speed, and see exactly how data structures change during execution.

### Supported Algorithm Categories

- **🔍 Searching Algorithms**: Binary Search, Linear Search, Jump Search
- **📊 Sorting Algorithms**: Bubble Sort, Selection Sort, Insertion Sort, Merge Sort, Quick Sort, Heap Sort
- **🌳 Graph Algorithms**: BFS, DFS, Dijkstra's Algorithm, Prim's Algorithm, Kruskal's Algorithm
- **🧩 Puzzle Solvers**: N-Queens, Sudoku, Maze Generation and Solving
- **🔗 Pathfinding**: A* Search, Best-First Search

---

## ✨ Features

- **🎬 Step-by-Step Visualization**: Watch algorithms execute one step at a time
- **⏯️ Playback Controls**: Play, Pause, Step Forward, Step Backward
- **⚡ Adjustable Speed**: Control animation speed with an intuitive slider
- **📊 Multiple Visualizations**: Bar charts, arrays, graphs, grids, and chessboards
- **📝 Variable Tracking**: Monitor algorithm variables in real-time
- **💻 Code Display**: View algorithm pseudocode alongside visualization
- **🎨 Clean UI**: Modern JavaFX interface with intuitive controls
- **📦 Standalone Executable**: Includes embedded JRE—no Java installation required

---

## 🚀 Quick Start

### Prerequisites

- **Java Development Kit (JDK) 11 or higher** - [Download here](https://www.oracle.com/java/technologies/javase-downloads.html)
- **Apache Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)

### Running from Source

#### Windows
```bash
run.bat
```

#### Linux/Mac
```bash
chmod +x run.sh
./run.sh
```

### Using Maven Directly

```bash
# Compile the project
mvn clean compile

# Run the application
mvn javafx:run
```

### Running the Executable (Windows Only)

If you have the pre-built `AlgoBuddy.exe`:
1. Double-click `AlgoBuddy.exe`
2. No Java installation required—JRE is embedded!

---

## 🏗️ Building from Source

### Build JAR File

```bash
# Standard JAR (requires JavaFX on system)
mvn clean package

# Shaded JAR (includes all dependencies)
mvn clean package
```

Output: `target/algorithm-visualizer-1.0.0.jar`

### Create Windows Executable

1. **Build the shaded JAR**:
   ```bash
   mvn clean package
   ```

2. **Use Launch4j**:
   - Install [Launch4j](http://launch4j.sourceforge.net/)
   - Open `algobuddy.xml` in Launch4j
   - Click "Build wrapper" to generate `AlgoBuddy.exe`

3. **Package for distribution**:
   - Include `AlgoBuddy.exe`
   - Include `jre/` directory (embedded JRE)
   - Include `openjfx-21.0.7_windows-x64_bin-sdk/` (JavaFX SDK)

---

## 🎮 How to Use

### 1. Select an Algorithm
- Launch the application
- Browse the algorithm list by category
- Click on an algorithm to open its visualization

### 2. Configure Parameters
- Set input size, target values, or other algorithm-specific parameters
- Click **"Generate"** or **"Start"** to initialize

### 3. Control Playback
- **▶️ Play**: Auto-advance through algorithm steps
- **⏸️ Pause**: Pause automatic playback
- **⏭️ Step**: Advance one step forward
- **⏮️ Step Back**: Go back one step
- **🔄 Reset**: Return to initial state

### 4. Adjust Speed
- Use the speed slider to control animation speed
- Slower speeds help understand complex operations
- Faster speeds good for observing overall behavior

### 5. Monitor Execution
- **Visualization Area**: Watch the algorithm in action
- **Progress Log**: Read textual descriptions of each step
- **Variable List**: Track variable values during execution
- **Code Display**: View the algorithm's pseudocode

---

## 🏛️ Project Structure

```
algorithm-visualizer/
├── src/main/
│   ├── java/com/algorithmvisualizer/
│   │   ├── Main.java                   # Application entry point
│   │   ├── algorithm/                  # Algorithm implementations
│   │   │   ├── BinarySearchSolver.java
│   │   │   ├── BubbleSortSolver.java
│   │   │   ├── DFSSolver.java
│   │   │   └── ...
│   │   ├── model/                      # Algorithm metadata
│   │   │   ├── Algorithm.java          # Abstract base class
│   │   │   ├── BinarySearchAlgorithm.java
│   │   │   └── ...
│   │   ├── ui/                         # UI controllers
│   │   │   ├── MainController.java
│   │   │   ├── AlgorithmViewController.java
│   │   │   └── ...
│   │   └── visualization/              # Renderers
│   │       ├── BarChartRenderer.java
│   │       ├── GraphRenderer.java
│   │       └── ...
│   └── resources/
│       ├── fxml/                       # UI layouts
│       │   ├── main-view.fxml
│       │   ├── algorithm-view-new.fxml
│       │   └── ...
│       └── Logo.png
├── jre/                                # Embedded JRE (for distribution)
├── openjfx-21.0.7_windows-x64_bin-sdk/ # JavaFX SDK (for distribution)
├── pom.xml                             # Maven configuration
├── algobuddy.xml                       # Launch4j configuration
├── run.bat                             # Windows run script
├── run.sh                              # Unix/Linux run script
└── README.md                           # This file
```

---

## 🛠️ Technology Stack

- **Language**: Java 11
- **UI Framework**: JavaFX 17.0.2
- **Build Tool**: Apache Maven
- **Packaging**: Launch4j (Windows executable creation)
- **Architecture**: Model-View-Controller (MVC)

---

## 🤝 Contributing

We welcome contributions! Whether it's adding new algorithms, improving visualizations, or fixing bugs, your help is appreciated.

### Adding a New Algorithm

1. **Create Algorithm Model** (`model/[Name]Algorithm.java`):
   ```java
   public class NewAlgorithm extends Algorithm {
       public NewAlgorithm() {
           super("Algorithm Name", "Description", Category.SORTING);
       }
       
       @Override
       public String getVisualizationFxml() {
           return "/fxml/newalgorithm-view.fxml";
       }
       
       @Override
       public Class<?> getControllerClass() {
           return NewAlgorithmController.class;
       }
   }
   ```

2. **Implement Algorithm Logic** (`algorithm/[Name]Solver.java`):
   ```java
   public class NewAlgorithmSolver {
       public List<StepData> solve(Parameters params) {
           // Your algorithm implementation
       }
   }
   ```

3. **Create Controller** (`ui/[Name]Controller.java`):
   - Implement `AlgorithmViewController.AlgorithmSpecificController`
   - Handle parameter input and visualization updates

4. **Create or Reuse Renderer** (`visualization/[Name]Renderer.java`):
   - Render algorithm state to JavaFX Canvas or Pane

5. **Create FXML View** (`resources/fxml/[name]-view.fxml`):
   - Link to your controller

6. **Register Algorithm**:
   - Add to `MainController.getAlgorithms()` method

For detailed contribution guidelines, see [AI_AGENT_ONBOARDING.md](AI_AGENT_ONBOARDING.md).

---

## 📚 Documentation

- **[AI Agent Onboarding Guide](AI_AGENT_ONBOARDING.md)**: Comprehensive developer documentation
- **[JavaDoc](target/site/apidocs/)**: API documentation (generated via `mvn javadoc:javadoc`)

---

## 🐛 Known Issues

- No automated tests currently exist
- Code display feature is not fully implemented for all algorithms
- Step-back functionality may have issues with certain puzzle algorithms
- Linux/Mac executable not available (currently Windows only via Launch4j)

---

## 🗺️ Roadmap

### Version 2.0 (Planned)
- [ ] Add automated tests (JUnit)
- [ ] Implement complete code display for all algorithms
- [ ] Add dark mode theme
- [ ] Export visualization as video/GIF
- [ ] Linux and macOS native packages
- [ ] Enhanced graph algorithm visualizations
- [ ] Tutorial mode for beginners
- [ ] Algorithm comparison mode

### Future Enhancements
- [ ] Dynamic programming visualizations
- [ ] String matching algorithms
- [ ] Tree traversal animations
- [ ] Interactive parameter tuning
- [ ] Performance metrics display
- [ ] Multi-language support (i18n)
- [ ] Web-based version

---

## 🧪 Testing

Currently, the project lacks automated tests. To test manually:

1. **Run the application**: `mvn javafx:run`
2. **Test each algorithm category**:
   - Select an algorithm
   - Configure parameters
   - Verify visualization renders correctly
   - Test all playback controls (Play, Pause, Step, Step Back, Reset)
   - Adjust speed and verify responsiveness
3. **Check edge cases**:
   - Minimum input size
   - Maximum input size
   - Already sorted arrays (for sorting algorithms)
   - Graph with cycles vs. acyclic graphs

---

## 💡 Tips for Educators

- **Start with Simple Algorithms**: Begin with Binary Search or Selection Sort
- **Use Step-by-Step Mode**: Pause and discuss each step with students
- **Vary Input Sizes**: Show how algorithm behavior changes with data size
- **Compare Algorithms**: Demonstrate different approaches to the same problem
- **Encourage Exploration**: Let students adjust parameters and observe results

---

## 🙏 Acknowledgments

- Built with ❤️ using [JavaFX](https://openjfx.io/)
- Icons and UI inspired by modern design principles
- Educational content based on standard computer science curriculum

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📧 Contact & Support

- **Issues**: Report bugs or request features via [GitHub Issues](https://github.com/Felix-au/Algorithm-Visualizer/issues)
- **Discussions**: Join conversations in [GitHub Discussions](https://github.com/Felix-au/Algorithm-Visualizer/discussions)
- **Email**: [Your contact email]

---

## 🌟 Star History

If you find this project helpful, please consider giving it a ⭐ on GitHub!

---

<div align="center">

**Made with 🧠 for algorithm enthusiasts**

[⬆ Back to Top](#algobuddy---algorithm-visualizer)

</div>
