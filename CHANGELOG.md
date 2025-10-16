# Changelog - AlgoQuest Algorithm Visualizer

## [1.3.0] - 2025-10-16

### 🎨 Visualization Screen UI/UX Overhaul

#### Added
- **Professional Button Icons**
  - All control buttons use FontAwesome 5 SVG icons
  - Icon + text combination for clarity
  - Step Back (`fas-step-backward`), Play (`fas-play`), Pause (`fas-pause`)
  - Step Forward (`fas-step-forward`), Reset (`fas-redo-alt`)
  - Back (`fas-arrow-left`), Randomize (`fas-random`), Apply (`fas-check`)

- **Section Header Icons**
  - Parameters: `fas-sliders-h`
  - Algorithm Code: `fas-code`
  - Current Step: `fas-info-circle`
  - Progress Log: `fas-list-ul`
  - Variables: `fas-database`

- **Enhanced Speed Controls**
  - **Default Speed**: 1x (changed from 5x)
  - **Max Speed**: 20x (increased from 10x)
  - **Min Speed**: 1x (whole numbers only)
  - Speed range indicators (1x and 20x labels)
  - Whole number speed display (1x, 2x, 3x... 20x)
  - Slider snaps to integer values for precise control

#### Changed
- **Button Styling**
  - Modern rounded corners (8px border-radius)
  - Color-coded by function:
    - Blue (#4a90e2): Navigation (Step Back/Forward)
    - Green (#2ecc71): Positive actions (Play, Apply)
    - Yellow (#ffc107): Pause/Warning
    - Red (#e74c3c): Reset
    - Purple (#7b68ee): Randomize
    - Gray (#6c757d): Back navigation
  - Bold text for better readability
  - Hover cursor changes to hand pointer
  - Consistent padding (10-20px)

- **Control Bar Design**
  - Light gray background (#f8f9fa)
  - Rounded container (12px border-radius)
  - Drop shadow for elevation
  - Vertical separator before speed controls
  - Better spacing between elements

- **Header Improvements**
  - White background with drop shadow
  - Larger title text (22px, bold)
  - Vertical separator between back button and title
  - Better spacing and padding

- **Speed Label Formatting**
  - Shows whole numbers only (1x, 2x, 3x... 20x)
  - Slider snaps to integer ticks for precise control
  - Right-aligned with fixed width (45px)
  - Bolder and more prominent

#### Technical Details
- Updated `algorithm-view-new.fxml` with Ikonli FontIcon
- Modified `AlgorithmViewController.java` speed label format
- Updated all 7 algorithm controllers with new default speed
- Speed slider: min=1.0, max=20.0, value=1.0, snapToTicks=true, majorTickUnit=1.0
- All icons use size 14px for buttons, 16px for headers

#### Files Modified
1. `algorithm-view-new.fxml` - Complete button and control redesign
2. `AlgorithmViewController.java` - Speed label formatting
3. `BinarySearchController.java` - Default speed 1.0
4. `BubbleSortController.java` - Default speed 1.0
5. `DFSController.java` - Default speed 1.0
6. `MazeController.java` - Default speed 1.0
7. `NQueensController.java` - Default speed 1.0
8. `SelectionSortController.java` - Default speed 1.0
9. `SudokuController.java` - Default speed 1.0

#### Files Created
1. `VISUALIZATION_UI_IMPROVEMENTS.md` - Comprehensive documentation

#### User Benefits
- ✅ More reasonable 1x default speed (better for learning)
- ✅ Whole number speeds (1-20x) for intuitive control
- ✅ Slider snaps to integers for precise selection
- ✅ 20x max speed for faster testing/debugging
- ✅ Professional SVG icons reduce cognitive load
- ✅ Color coding helps identify button functions instantly
- ✅ Better visual hierarchy with section icons
- ✅ Enhanced accessibility with icon+text buttons

#### Performance
- No measurable performance impact
- Icons render smoothly at all speeds
- All algorithms tested at 1x to 20x range
- Integer snapping provides smooth, predictable playback

---

## [1.2.0] - 2025-10-16

### 🎨 Multi-Tag Support & Professional Icons

#### Added
- **Ikonli Icon Library Integration**
  - Professional SVG icons replace emojis
  - FontAwesome 5 and Material Design 2 icon packs
  - Scalable, crisp rendering at any size
  - Color-coded icons matching category themes
  - All icons embedded in shaded JAR

- **Multi-Category Algorithm Support**
  - Algorithms can now belong to multiple categories
  - New varargs constructor: `Algorithm(name, desc, category1, category2, ...)`
  - Helper methods: `getTags()`, `hasCategory()`
  - Backward compatible with single-category constructor

- **New Category: Practical Applications**
  - Dedicated category for real-world problem-solving
  - Icon: Cogs/Settings (Material Design)
  - Color scheme: Purple (`#9b59b6`)

#### Changed
- **Algorithm Categorization**
  - `DFS`: Added "Backtracking" category
  - `Sudoku`: Added "Backtracking" and "Practical Applications"
  - `N-Queens`: Added "Practical Applications"
  - `Maze`: Added "Practical Applications"

- **Icon System**
  - `AlgorithmCategory.getEmoji()` → `getIcon()` (returns `Ikon` object)
  - Category icons now rendered via `FontIcon` component
  - Icons colorized to match category color schemes

- **MainController**
  - Updated to support multi-category algorithm organization
  - Each algorithm appears in all relevant categories
  - No duplicate entries per category

#### Technical Details
- **Dependencies**: Ikonli 12.3.1 (core, javafx, fontawesome5, materialdesign2)
- **Shaded JAR**: All Ikonli libraries included automatically
- **Build**: Verified with `mvn package` - all dependencies bundled
- **EXE Compatible**: Works seamlessly with Launch4j

#### Icon Mapping
| Category | Icon | Library |
|----------|------|---------|
| All | Grid | FontAwesome TH_LARGE |
| Searching | Magnifying Glass | FontAwesome SEARCH |
| Sorting | Sort Ascending | Material Design |
| Graph | Graph Outline | Material Design |
| Backtracking | Undo | FontAwesome |
| Pathfinding | Map Marker | Material Design |
| Practical | Cogs | Material Design |
| Real-World | Puzzle Piece | FontAwesome |

#### Files Created
1. `MULTI_TAG_SUPPORT.md` - Comprehensive documentation

#### Files Modified
1. `pom.xml` - Added Ikonli dependencies
2. `AlgorithmCategory.java` - Icon support, new PRACTICAL category
3. `Algorithm.java` - Multi-category constructor and helper methods
4. `MainController.java` - Multi-tag support, FontIcon rendering
5. `MazeAlgorithm.java` - Added "Practical Applications"
6. `SudokuAlgorithm.java` - Added "Backtracking" and "Practical Applications"
7. `DepthFirstSearchAlgorithm.java` - Added "Backtracking"
8. `NQueensAlgorithm.java` - Added "Practical Applications"
9. `AI_AGENT_ONBOARDING.md` - Updated with multi-tag and Ikonli info

#### Migration Notes
- **Breaking**: `getEmoji()` replaced with `getIcon()`
- **Compatible**: Single-category algorithms still work
- **Recommended**: Update relevant algorithms to use multiple categories

---

## [1.1.0] - 2025-10-15

### 🎨 Major UI Redesign - Category-Based Navigation

#### Added
- **AlgorithmCategory Enum** (`model/AlgorithmCategory.java`)
  - 7 distinct categories: All, Searching, Sorting, Graph Algorithms, Backtracking, Pathfinding, Real-World Problems
  - Each with display name, description, emoji, and color scheme
  - Smart `fromString()` mapping for automatic categorization

- **Modern Card-Based Main Screen**
  - Category selection cards with emojis and algorithm counts
  - Algorithm detail cards with hover effects
  - Smooth scale animations (200ms transitions)
  - Color-coded categories with distinct visual identities

- **Enhanced Search Functionality**
  - Real-time filtering across all algorithms
  - Integrated in header for persistent access
  - Shows result count and empty states
  - Searches name, description, and category fields

- **90% Screen Sizing**
  - Window opens at 90% of screen dimensions
  - Automatically centered on primary display
  - Improved first-launch user experience

- **Two-Level Navigation**
  - Category browsing → Algorithm selection
  - Back button for easy navigation
  - Clear visual hierarchy with section titles

#### Changed
- **MainController.java** - Complete rewrite
  - Programmatic card creation with dynamic styling
  - Category-based algorithm organization
  - Map-based data structure for efficient lookups
  - Hover effect implementations

- **main-view.fxml** - Complete redesign
  - FlowPane grids for responsive layout
  - ScrollPane for content overflow
  - Modern gradient background
  - Elevated panels with drop shadows
  - Header with integrated search

- **Main.java** - Window sizing improvements
  - Calculates 90% of screen bounds
  - Centers window on launch
  - Better initial user experience

- **Algorithm Categories** - Standardized naming
  - `DepthFirstSearchAlgorithm`: "Graph Traversal" → "Graph Algorithms"
  - `SudokuAlgorithm`: "Real-world" → "Real-World Problems"

#### Technical Details
- **Dependencies**: No new dependencies required
- **Compatibility**: Java 11+, JavaFX 17.0.2
- **Build**: `mvn clean compile` successful
- **Testing**: Application launches and runs correctly

#### Files Created
1. `src/main/java/com/algorithmvisualizer/model/AlgorithmCategory.java` - Category enum
2. `UI_IMPROVEMENTS.md` - Comprehensive documentation of UI changes
3. `CHANGELOG.md` - This file

#### Files Modified
1. `src/main/java/com/algorithmvisualizer/Main.java` - Window sizing
2. `src/main/java/com/algorithmvisualizer/ui/MainController.java` - Complete rewrite
3. `src/main/resources/fxml/main-view.fxml` - Complete redesign
4. `src/main/java/com/algorithmvisualizer/model/DepthFirstSearchAlgorithm.java` - Category update
5. `src/main/java/com/algorithmvisualizer/model/SudokuAlgorithm.java` - Category update
6. `AI_AGENT_ONBOARDING.md` - Updated with category system documentation

#### Design System
**Category Colors:**
- All: `#4a90e2` (Blue)
- Searching: `#7b68ee` (Purple)
- Sorting: `#ff6b6b` (Coral Red)
- Graph: `#4ecdc4` (Teal)
- Backtracking: `#f39c12` (Orange)
- Pathfinding: `#2ecc71` (Green)
- Real-World: `#e74c3c` (Crimson)

**UI Colors:**
- Background: `#f5f7fa` → `#e8eef5` (Gradient)
- Cards: `#ffffff` (White)
- Primary Text: `#2b4c7e` (Dark Blue)
- Secondary Text: `#6c757d` (Gray)
- Badges: `#e8eefb` (Light Blue)

#### Migration Notes
For developers adding new algorithms:
- Use standardized category strings: "Searching", "Sorting", "Graph Algorithms", "Backtracking", "Pathfinding", "Real-World Problems"
- Register algorithms in `MainController.initializeAlgorithms()`
- Category assignment is automatic based on string matching
- Algorithm count updates automatically per category

#### Future Enhancements (Planned)
- Dark mode theme toggle
- Favorites/bookmarking system
- Algorithm difficulty levels
- Recently viewed algorithms
- Quick preview on hover
- Export functionality

---

## [1.0.0] - Initial Release

### Features
- Algorithm visualization framework
- 7 algorithms implemented:
  - Binary Search
  - Bubble Sort
  - Selection Sort
  - Depth-First Search (DFS)
  - N-Queens
  - Sudoku Solver
  - Maze Generation + Pathfinding
- Step-by-step playback controls
- Variable tracking
- Progress logging
- Adjustable animation speed

---

## Version Numbering
- **Major** (X.0.0): Breaking changes, major features
- **Minor** (1.X.0): New features, UI improvements, backward compatible
- **Patch** (1.0.X): Bug fixes, minor tweaks

---

*For detailed technical documentation, see `AI_AGENT_ONBOARDING.md`*  
*For UI improvement details, see `UI_IMPROVEMENTS.md`*
