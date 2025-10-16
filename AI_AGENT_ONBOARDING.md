# AI Agent Onboarding Instructions

## Purpose
This document helps AI coding agents quickly become effective contributors to the Algorithm Visualizer project. It explains the codebase structure, safe working practices, and key conventions.

---

## Project Overview

**AlgoQuest** (Algorithm Visualizer) is a JavaFX-based desktop application that provides interactive visualizations for various computer science algorithms including sorting, searching, graph traversal, and puzzle-solving algorithms.

### Technology Stack
- **Language**: Java 11
- **UI Framework**: JavaFX 17.0.2 (SDK 21.0.7 bundled for distribution)
- **Build Tool**: Maven
- **Packaging**: Launch4j (creates Windows executable with embedded JRE)

### Main Layers
1. **Entry Point**: `com.algorithmvisualizer.Main` - JavaFX application launcher
2. **UI Controllers**: Handle user interaction and visualization rendering
3. **Algorithm Models**: Define metadata and provide algorithm-specific configuration
4. **Algorithm Solvers/Generators**: Implement algorithm logic and step generation
5. **Visualization Renderers**: Render algorithm states to JavaFX canvas/panes

### Entry Points
- **Application Entry**: `src/main/java/com/algorithmvisualizer/Main.java`
- **Build Scripts**: `run.bat` (Windows), `run.sh` (Unix/Linux)
- **Build Configuration**: `pom.xml` (Maven), `PrashnaSetu.xml` (Launch4j)

### Project Structure
```
src/main/
├── java/com/algorithmvisualizer/
│   ├── Main.java                    # JavaFX application entry point
│   ├── algorithm/                   # Algorithm solvers and generators
│   │   ├── BinarySearchSolver.java
│   │   ├── BubbleSortSolver.java
│   │   ├── DFSSolver.java
│   │   ├── MazeGenerator*.java
│   │   ├── NQueensSolver.java
│   │   └── SudokuSolver.java
│   ├── model/                       # Algorithm metadata and configuration
│   │   ├── Algorithm.java           # Abstract base class
│   │   ├── BinarySearchAlgorithm.java
│   │   ├── SelectionSortAlgorithm.java
│   │   └── [Other]Algorithm.java
│   ├── ui/                          # UI controllers
│   │   ├── MainController.java      # Algorithm selection screen
│   │   ├── AlgorithmViewController.java  # Base visualization controller
│   │   ├── BinarySearchController.java
│   │   ├── SelectionSortController.java
│   │   └── [Other]Controller.java
│   └── visualization/               # Rendering components
│       ├── ArrayRenderer.java
│       ├── BarChartRenderer.java
│       ├── ChessboardRenderer.java
│       ├── GraphRenderer.java
│       ├── MazeGridRenderer.java
│       └── SudokuGridRenderer.java
└── resources/
    ├── fxml/                        # JavaFX UI layouts
    │   ├── main-view.fxml           # Algorithm selection screen
    │   ├── algorithm-view-new.fxml  # Shared visualization template
    │   └── [algorithm]-view.fxml    # Algorithm-specific views
    └── Logo.png                     # Application icon
```

---

## Architecture and Data Flow

### Application Flow
```
Main.java → main-view.fxml → MainController
    ↓ (user selects algorithm)
AlgorithmViewController → algorithm-view-new.fxml
    ↓ (loads algorithm-specific controller)
[Algorithm]Controller → [Algorithm]Solver/Generator → [Algorithm]Renderer
```

### Component Responsibilities

#### 1. Main.java
- Extends `javafx.application.Application`
- Loads `main-view.fxml` as the initial scene
- Sets application title ("AlgoQuest") and icon

#### 2. MainController.java
- Populates `ListView<Algorithm>` with available algorithms
- Handles algorithm selection
- Loads `algorithm-view-new.fxml` when algorithm is selected
- Passes selected algorithm to `AlgorithmViewController`

#### 3. Algorithm.java (Abstract Base)
- Defines common properties: `name`, `description`, `category`
- **Abstract methods**:
  - `getVisualizationFxml()`: Returns FXML file path for the algorithm
  - `getControllerClass()`: Returns controller class for the algorithm

#### 4. Concrete Algorithm Classes
- Extend `Algorithm` base class
- Provide metadata (name, description, category)
- Return algorithm-specific FXML path and controller class
- Examples: `BinarySearchAlgorithm`, `SelectionSortAlgorithm`, `BFSAlgorithm`

#### 5. AlgorithmViewController.java
- **Base controller** for all algorithm visualizations
- Provides common UI elements:
  - Play/Pause/Stop/Step/StepBack buttons
  - Speed slider
  - Progress area (TextArea)
  - Variable list (ListView)
  - Algorithm visualizer container (Pane)
  - Code display area
- Manages playback state via JavaFX `Timeline`
- Defines `AlgorithmSpecificController` interface:
  ```java
  interface AlgorithmSpecificController {
      void setParentController(AlgorithmViewController parent);
      void initializeVisualization(Algorithm algorithm);
      void handleParametersSet(Map<String, Object> params);
      void resetVisualization();
      void updateVisualization(int step);
  }
  ```

#### 6. Algorithm-Specific Controllers
- Implement `AlgorithmViewController.AlgorithmSpecificController`
- Integrate with solver/generator classes
- Use renderer classes to draw visualizations
- Handle algorithm-specific parameters
- Manage step-by-step execution state
- Examples: `BinarySearchController`, `SelectionSortController`

#### 7. Solver/Generator Classes
- Implement algorithm logic
- Generate step-by-step execution data
- Store state for each step (for step-back functionality)
- Examples: `BinarySearchSolver`, `BubbleSortSolver`, `DFSSolver`

#### 8. Renderer Classes
- Render algorithm state to JavaFX nodes (Canvas, Pane)
- Handle visual updates for each step
- Examples: `BarChartRenderer`, `ArrayRenderer`, `GraphRenderer`

### Data Flow Example (Binary Search)
1. User selects "Binary Search" from `MainController`
2. `AlgorithmViewController` loads `binarysearch-view.fxml`
3. `BinarySearchController` is instantiated and linked to parent
4. User sets parameters (array size, target value)
5. `BinarySearchController` calls `BinarySearchSolver.solve()`
6. Solver generates list of execution steps
7. User clicks Play → Timeline animates through steps
8. Each step calls `BinarySearchController.updateVisualization(step)`
9. Controller updates `ArrayRenderer` and `BarChartRenderer`
10. Visual updates displayed on canvas

### Category System (NEW)

#### AlgorithmCategory Enum
- **Location**: `src/main/java/com/algorithmvisualizer/model/AlgorithmCategory.java`
- **Purpose**: Centralized management of algorithm categories
- **Categories**:
  - `ALL` - All algorithms (default view)
  - `SEARCHING` - Binary Search, etc.
  - `SORTING` - Bubble Sort, Selection Sort, etc.
  - `GRAPH` - DFS, BFS, graph traversal algorithms
  - `BACKTRACKING` - N-Queens, constraint satisfaction
  - `PATHFINDING` - Maze generation and solving
  - `REAL_WORLD` - Sudoku, practical applications

#### Category Properties
Each category has:
- **Display Name**: User-friendly name (e.g., "Graph Algorithms")
- **Description**: Brief explanation of category purpose
- **Emoji**: Visual identifier (🔍, 📊, 🕸️, etc.)
- **Color**: Unique color scheme for UI cards

#### Main UI Flow (Card-Based)
1. **Initial Screen**: Category cards displayed in grid layout
   - 7 category cards with emojis and algorithm counts
   - Search bar in header for real-time filtering
   - Modern gradient background with card shadows
2. **Category Selection**: User clicks category card
   - Transitions to algorithm list for that category
   - Shows algorithm cards with details
   - Back button to return to categories
3. **Search**: User types in search bar
   - Real-time filtering across all algorithms
   - Shows matching algorithms regardless of category
   - Displays result count or "No results found"
4. **Algorithm Selection**: User clicks algorithm card
   - Opens algorithm visualization view (same as before)

#### UI/UX Features
- **90% Screen Size**: Window opens at 90% of screen dimensions, centered
- **Card-Based Design**: Modern, hoverable cards with smooth animations
- **Color-Coded Categories**: Each category has distinct color scheme
- **Responsive Layout**: FlowPane adapts to window size
- **Gradient Background**: Professional appearance with depth
- **Hover Effects**: Scale transitions and shadow changes on interaction

---

## Developer Workflows

### Building and Running

#### Quick Run (Development)
```bash
# Windows
run.bat

# Unix/Linux/Mac
./run.sh
```
Both scripts execute:
```bash
mvn clean compile
mvn javafx:run
```

#### Manual Build
```bash
# Compile only
mvn clean compile

# Run application
mvn javafx:run

# Package JAR
mvn clean package

# Create shaded JAR (with dependencies)
mvn clean package -P shade
```

#### Create Windows Executable
1. Build shaded JAR: `mvn clean package`
2. Run Launch4j with `PrashnaSetu.xml` configuration
3. Output: `AlgoQuest.exe` with embedded JRE

### Project Requirements
- **JDK**: Java 11 or higher
- **Maven**: 3.6+ recommended
- **JavaFX SDK**: 21.0.7 (bundled in `openjfx-21.0.7_windows-x64_bin-sdk/`)
- **JRE**: Embedded in `jre/` directory for distribution

### Environment Setup
- No `.env` files required
- Ensure `JAVA_HOME` points to JDK 11+
- Maven must be on system `PATH`
- For distribution, ensure `jre/` and JavaFX SDK paths are correct in `PrashnaSetu.xml`

### Testing
⚠️ **No test directory exists** - Project currently lacks unit tests.

### Build Artifacts
- **Target Directory**: `target/`
  - `algorithm-visualizer-1.0.0.jar`: Standard JAR
  - `algorithm-visualizer-1.0.0-all.jar`: Shaded JAR with dependencies
  - `classes/`: Compiled class files
- **Distribution**: `AlgoQuest.exe` (created via Launch4j)

### CI/CD
❌ No CI/CD configuration found (no `.github/workflows/`, `.gitlab-ci.yml`, etc.)

---

## Conventions and Patterns

### Naming Conventions

#### Algorithm Model Classes
- **Pattern**: `[AlgorithmName]Algorithm.java`
- **Location**: `src/main/java/com/algorithmvisualizer/model/`
- **Examples**: `BinarySearchAlgorithm.java`, `SelectionSortAlgorithm.java`

#### Controller Classes
- **Pattern**: `[AlgorithmName]Controller.java`
- **Location**: `src/main/java/com/algorithmvisualizer/ui/`
- **Examples**: `BinarySearchController.java`, `SelectionSortController.java`

#### Solver/Generator Classes
- **Pattern**: `[AlgorithmName]Solver.java` or `[AlgorithmName]Generator.java`
- **Location**: `src/main/java/com/algorithmvisualizer/algorithm/`
- **Examples**: `BinarySearchSolver.java`, `MazeGeneratorDFS.java`

#### Renderer Classes
- **Pattern**: `[VisualizationType]Renderer.java`
- **Location**: `src/main/java/com/algorithmvisualizer/visualization/`
- **Examples**: `BarChartRenderer.java`, `GraphRenderer.java`

#### FXML Files
- **Main view**: `main-view.fxml`
- **Shared template**: `algorithm-view-new.fxml`
- **Algorithm-specific**: `[algorithmlowercase]-view.fxml`
- **Location**: `src/main/resources/fxml/`
- **Examples**: `binarysearch-view.fxml`, `selectionsort-view.fxml`

### Architecture Patterns

#### MVC Pattern
- **Model**: `com.algorithmvisualizer.model.*` - Algorithm metadata
- **View**: `src/main/resources/fxml/*` - FXML layouts
- **Controller**: `com.algorithmvisualizer.ui.*` - UI logic and event handling

#### Template Method Pattern
- `Algorithm.java` defines abstract methods for subclasses to implement
- Common structure with algorithm-specific implementations

#### Strategy Pattern (via Interface)
- `AlgorithmSpecificController` interface allows different algorithm implementations
- `AlgorithmViewController` delegates to specific controllers

### Code Organization Rules

#### Package Responsibilities
- **`model/`**: Algorithm metadata, configuration, and data models
- **`ui/`**: JavaFX controllers, UI logic, event handling
- **`algorithm/`**: Pure algorithm implementations (solvers, generators)
- **`visualization/`**: Rendering logic for visual components

#### Separation of Concerns
- ✅ **Solver/Generator**: Pure logic, no UI dependencies
- ✅ **Renderer**: Drawing logic, JavaFX Canvas/Pane manipulation
- ✅ **Controller**: Coordinates solver + renderer, handles UI events
- ✅ **Model**: Metadata only, no execution logic

### Adding a New Algorithm (Step-by-Step)

1. **Create Algorithm Model Class**
   ```java
   // src/main/java/com/algorithmvisualizer/model/NewAlgorithm.java
   public class NewAlgorithm extends Algorithm {
       public NewAlgorithm() {
           // Use appropriate category string:
           // "Searching", "Sorting", "Graph Algorithms", 
           // "Backtracking", "Pathfinding", "Real-World Problems"
           super("New Algorithm", "Brief description of the algorithm", "Sorting");
       }
       
       @Override
       public String getVisualizationFxml() {
           return "/fxml/newalgorithm-view.fxml";
       }
       
       @Override
       public Class<?> getControllerClass() {
           return com.algorithmvisualizer.ui.NewAlgorithmController.class;
       }
   }
   ```
   
   **Important - Multi-Category Support (v1.1.0+)**: Algorithms can now have multiple categories. Use the varargs constructor:
   ```java
   public NewAlgorithm() {
       super(
           "Algorithm Name",
           "Description",
           "Primary Category",
           "Secondary Category",
           "Additional Category"
       );
   }
   ```
   
   Valid category strings (case-insensitive):
   - `"Searching"` → SEARCHING category
   - `"Sorting"` → SORTING category
   - `"Graph Algorithms"` or `"Graph"` → GRAPH category
   - `"Backtracking"` → BACKTRACKING category
   - `"Pathfinding"` → PATHFINDING category
   - `"Practical Applications"` or `"Practical"` → PRACTICAL category (NEW)
   - `"Real-World Problems"` or `"Real-World"` → REAL_WORLD category

2. **Create Solver/Generator Class**
   ```java
   // src/main/java/com/algorithmvisualizer/algorithm/NewAlgorithmSolver.java
   public class NewAlgorithmSolver {
       public List<StepData> solve(Parameters params) {
           // Implementation
       }
   }
   ```

3. **Create Renderer Class** (if needed)
   ```java
   // src/main/java/com/algorithmvisualizer/visualization/NewRenderer.java
   public class NewRenderer {
       public void render(Canvas canvas, StepData data) {
           // Rendering logic
       }
   }
   ```

4. **Create Controller Class**
   ```java
   // src/main/java/com/algorithmvisualizer/ui/NewAlgorithmController.java
   public class NewAlgorithmController implements AlgorithmViewController.AlgorithmSpecificController {
       private AlgorithmViewController parent;
       private NewAlgorithmSolver solver;
       private NewRenderer renderer;
       
       @Override
       public void setParentController(AlgorithmViewController parent) {
           this.parent = parent;
       }
       
       @Override
       public void initializeVisualization(Algorithm algorithm) {
           // Setup UI, create solver/renderer instances
       }
       
       @Override
       public void handleParametersSet(Map<String, Object> params) {
           // Run solver with params
       }
       
       @Override
       public void resetVisualization() {
           // Clear state
       }
       
       @Override
       public void updateVisualization(int step) {
           // Update renderer for current step
       }
   }
   ```

5. **Create FXML View**
   ```xml
   <!-- src/main/resources/fxml/newalgorithm-view.fxml -->
   <?xml version="1.0" encoding="UTF-8"?>
   <?import javafx.scene.layout.*?>
   <VBox xmlns:fx="http://javafx.com/fxml"
         fx:controller="com.algorithmvisualizer.ui.NewAlgorithmController">
       <!-- Minimal structure; controller registers with parent -->
   </VBox>
   ```

6. **Register in MainController**
   ```java
   // Add to initializeAlgorithms() method in MainController.java
   // Location: src/main/java/com/algorithmvisualizer/ui/MainController.java
   allAlgorithms = FXCollections.observableArrayList(
       new BinarySearchAlgorithm(),
       new BubbleSortAlgorithm(),
       // ... existing algorithms ...
       new NewAlgorithm()  // Add your new algorithm here
   );
   ```
   
   **Note**: The algorithm will automatically appear in the appropriate category based on its category string. The UI will update the algorithm count for that category automatically.

### JavaFX UI Patterns

#### FXML Controller Binding
- Use `fx:controller` attribute in FXML root element
- Use `@FXML` annotation for injected UI elements
- Use `fx:id` in FXML to link to controller fields

#### Event Handling
- Define handlers in controller: `@FXML private void handleAction(ActionEvent event)`
- Bind in FXML: `<Button onAction="#handleAction" />`

#### Animation with Timeline
```java
Timeline timeline = new Timeline(
    new KeyFrame(Duration.millis(speedMs), e -> {
        updateVisualization(currentStep++);
    })
);
timeline.setCycleCount(totalSteps);
timeline.play();
```

---

## Integrations and External Dependencies

### Core Dependencies (from pom.xml)

#### JavaFX
- **Modules**: `javafx-controls`, `javafx-fxml`
- **Version**: 17.0.2
- **Purpose**: UI framework for desktop application
- **Scope**: Compile

#### Ikonli Icon Library (v1.1.0+)
- **Modules**: `ikonli-core`, `ikonli-javafx`, `ikonli-fontawesome5-pack`, `ikonli-materialdesign2-pack`
- **Version**: 12.3.1
- **Purpose**: Professional SVG icons for category cards and UI elements
- **Icon Packs**: FontAwesome 5 (solid, regular, brands), Material Design 2
- **Scope**: Compile (included in shaded JAR)
- **Usage**: `FontIcon icon = new FontIcon(FontAwesomeSolid.SEARCH);`

#### Maven Plugins
- **maven-compiler-plugin**: Java compilation (target: 11)
- **maven-jar-plugin**: JAR packaging, manifest generation
- **javafx-maven-plugin**: JavaFX application execution
- **maven-shade-plugin**: Creates uber-JAR with dependencies (includes Ikonli)

### External Tools

#### Launch4j
- **Purpose**: Creates Windows executable (`.exe`) from JAR
- **Configuration**: `PrashnaSetu.xml`
- **Features**: Embeds JRE, configures JavaFX module path
- **Output**: `AlgoQuest.exe`

### Resource Dependencies
- **JRE**: Bundled in `jre/` directory (for distribution)
- **JavaFX SDK**: Bundled in `openjfx-21.0.7_windows-x64_bin-sdk/` (for distribution)

### No External APIs
- ❌ No REST APIs
- ❌ No database connections
- ❌ No authentication services
- ❌ No logging frameworks (using standard `System.out`)
- ❌ No error tracking services

---

## Agent Behavior Guidelines

### 1. Match Existing Patterns
- **Always follow** the MVC structure (`model/`, `ui/`, `algorithm/`, `visualization/`)
- Use existing naming conventions for new classes
- Follow the template method for adding algorithms (see "Adding a New Algorithm")
- Maintain separation of concerns: solvers should not depend on JavaFX

### 2. Search Before Creating
- Check if a renderer already exists before creating a new one
- Look for similar algorithm implementations to reuse logic
- Verify that utility methods don't already exist

### 3. Preserve Interfaces
- **Never modify** `AlgorithmViewController.AlgorithmSpecificController` without careful consideration
- Changing `Algorithm.java` abstract methods affects all subclasses
- Maintain backward compatibility with existing FXML files

### 4. Avoid Generated/Build Directories
- **Never edit** files in `target/` directory
- **Never edit** `AlgoQuest.exe` or bundled JRE files
- Build artifacts are regenerated on each build

### 5. Documentation Standards
- Add Javadoc comments for public classes and methods
- Include algorithm complexity information in algorithm model descriptions
- Document any non-obvious implementation decisions

### 6. Code Style
- Use **4 spaces** for indentation (matches existing code)
- Follow Java naming conventions:
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- Keep methods focused and concise
- Prefer composition over inheritance where appropriate

### 7. JavaFX Best Practices
- Always run UI updates on JavaFX Application Thread
- Use `Platform.runLater()` for async UI updates
- Properly dispose of Timeline/Animation resources
- Use FXML for UI layout, not programmatic scene graph construction

### 8. Testing Strategy
⚠️ **Currently no tests exist**. When adding tests:
- Create `src/test/java/` directory structure
- Use JUnit 5 (already in pom.xml)
- Test solver logic independently from UI
- Mock JavaFX components for controller tests

### 9. Safe Refactoring
- If changing shared components (Algorithm, AlgorithmViewController), test with multiple algorithms
- When modifying renderers, verify visual output manually
- Keep FXML and controller classes synchronized

### 10. Performance Considerations
- Large datasets: Consider algorithm step limit
- Animation speed: Respect user-configurable speed slider
- Memory: Clean up old step data when resetting

---

## Reference Files

Before making changes, always inspect:

### Essential Files
1. **`pom.xml`**
   - Dependencies and versions
   - Build plugins and configurations
   - Main class declaration

2. **`src/main/java/com/algorithmvisualizer/Main.java`**
   - Application entry point
   - Initial scene setup

3. **`src/main/java/com/algorithmvisualizer/ui/MainController.java`**
   - Algorithm registration
   - Navigation flow

4. **`src/main/java/com/algorithmvisualizer/ui/AlgorithmViewController.java`**
   - Shared visualization framework
   - `AlgorithmSpecificController` interface definition
   - Common UI controls and playback logic

5. **`src/main/java/com/algorithmvisualizer/model/Algorithm.java`**
   - Abstract base class for algorithms
   - Required methods for new algorithms

### Example Implementations
For reference when adding new algorithms:

- **Simple Sorting**: `SelectionSortAlgorithm` + `SelectionSortController` + `SelectionSortSolver`
- **Searching**: `BinarySearchAlgorithm` + `BinarySearchController` + `BinarySearchSolver`
- **Graph Algorithms**: `BFSAlgorithm` + `BFSController` + `BFSSolver`
- **Puzzle**: `SudokuAlgorithm` + `SudokuController` + `SudokuSolver`

### FXML Templates
- **`src/main/resources/fxml/algorithm-view-new.fxml`**: Shared visualization template
- **`src/main/resources/fxml/binarysearch-view.fxml`**: Minimal algorithm-specific FXML example

### Build and Distribution
- **`run.bat` / `run.sh`**: Development workflow
- **`PrashnaSetu.xml`**: Launch4j configuration for Windows executable

---

## Quick Start Checklist for AI Agents

- [ ] Read this onboarding document thoroughly
- [ ] Review `pom.xml` for dependencies and build setup
- [ ] Examine `Main.java` and `MainController.java` for application flow
- [ ] Study `AlgorithmViewController.java` for shared visualization framework
- [ ] Look at one complete algorithm example (e.g., BinarySearch):
  - [ ] Model: `BinarySearchAlgorithm.java`
  - [ ] Controller: `BinarySearchController.java`
  - [ ] Solver: `BinarySearchSolver.java`
  - [ ] Renderer: `ArrayRenderer.java`, `BarChartRenderer.java`
  - [ ] FXML: `binarysearch-view.fxml`
- [ ] Understand package structure and naming conventions
- [ ] Know how to build and run: `run.bat` or `mvn javafx:run`

---

## Common Tasks

### Add a New Sorting Algorithm
1. Create `[Name]Algorithm.java` in `model/`
2. Create `[Name]Solver.java` in `algorithm/`
3. Reuse `BarChartRenderer.java` or `ArrayRenderer.java`
4. Create `[Name]Controller.java` in `ui/`
5. Create minimal FXML in `resources/fxml/`
6. Register in `MainController.getAlgorithms()`

### Add a New Graph Algorithm
1. Create `[Name]Algorithm.java` in `model/`
2. Create `[Name]Solver.java` in `algorithm/`
3. Use/extend `GraphRenderer.java`
4. Create `[Name]Controller.java` in `ui/`
5. Create FXML with graph configuration UI
6. Register in `MainController.getAlgorithms()`

### Modify Playback Controls
- Edit `AlgorithmViewController.java`
- Changes affect all algorithms uniformly
- Test with multiple algorithm types

### Change Application Theme/Styling
- Edit FXML files (inline styles or styleClass)
- Add CSS file in `resources/` and link via scene.getStylesheets()
- Apply consistently across all views

---

## Troubleshooting

### Common Issues

#### JavaFX Module Not Found
- **Cause**: JavaFX not on module path
- **Solution**: Use `mvn javafx:run` or ensure JavaFX SDK is configured

#### ClassNotFoundException for Controller
- **Cause**: FXML `fx:controller` attribute doesn't match actual class name
- **Solution**: Verify package + class name in FXML matches Java file

#### Timeline Not Animating
- **Cause**: Timeline not started, or cycle count is 0
- **Solution**: Call `timeline.play()` and set appropriate `setCycleCount()`

#### UI Not Updating During Algorithm Execution
- **Cause**: Long-running task on JavaFX Application Thread
- **Solution**: Use Timeline for step-by-step execution, not blocking loops

#### Launch4j Executable Won't Start
- **Cause**: JRE path incorrect, JavaFX modules missing
- **Solution**: Verify `jre/` and JavaFX SDK paths in `PrashnaSetu.xml`

---

## Project Improvement Opportunities

### Suggested Enhancements
1. **Testing**: Add JUnit tests for solver classes
2. **CI/CD**: Set up GitHub Actions for automated builds
3. **Documentation**: Add README.md (see generated README)
4. **Code Quality**: Integrate Checkstyle or SpotBugs
5. **Logging**: Replace System.out with proper logging framework (SLF4J)
6. **Error Handling**: Improve user-facing error messages
7. **Code Display**: Implement algorithm pseudocode display in visualization
8. **Export**: Add feature to export visualization as video/GIF
9. **Themes**: Support dark mode and custom color schemes
10. **Localization**: Add internationalization (i18n) support

---

## Conclusion

This onboarding guide provides a comprehensive overview of the Algorithm Visualizer project's structure, patterns, and conventions. Always prioritize consistency with existing code, maintain clear separation of concerns, and test changes across multiple algorithm implementations.

For questions or clarifications, refer to the existing codebase examples—they are the source of truth for implementation patterns.
