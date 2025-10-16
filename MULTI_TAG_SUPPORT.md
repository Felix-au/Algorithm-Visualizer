# Multi-Tag Support & Icon Library Update

## Overview
AlgoQuest now supports **multiple categories per algorithm** and uses **professional SVG icons** via the Ikonli library instead of emojis.

---

## Key Features

### 1. **Multi-Category Support**
Algorithms can now belong to multiple categories simultaneously. For example:
- **Sudoku Solver**: Real-World Problems + Backtracking + Practical Applications
- **DFS**: Graph Algorithms + Backtracking
- **Maze Generation**: Pathfinding + Practical Applications

### 2. **Professional Icon Library (Ikonli)**
- Replaced emojis with scalable SVG icons
- Uses FontAwesome 5 and Material Design 2 icon packs
- Icons are colorized to match category color schemes
- Crisp rendering at any size

### 3. **New Category: Practical Applications**
Added a dedicated category for real-world problem-solving applications including:
- Maze generation and solving
- N-Queens problem
- Sudoku solver
- Other constraint satisfaction problems

---

## Technical Implementation

### Ikonli Integration

#### Dependencies Added (pom.xml)
```xml
<ikonli.version>12.3.1</ikonli.version>

<dependency>
    <groupId>org.kordamp.ikonli</groupId>
    <artifactId>ikonli-core</artifactId>
    <version>${ikonli.version}</version>
</dependency>
<dependency>
    <groupId>org.kordamp.ikonli</groupId>
    <artifactId>ikonli-javafx</artifactId>
    <version>${ikonli.version}</version>
</dependency>
<dependency>
    <groupId>org.kordamp.ikonli</groupId>
    <artifactId>ikonli-fontawesome5-pack</artifactId>
    <version>${ikonli.version}</version>
</dependency>
<dependency>
    <groupId>org.kordamp.ikonli</groupId>
    <artifactId>ikonli-materialdesign2-pack</artifactId>
    <version>${ikonli.version}</version>
</dependency>
```

**✅ All dependencies are automatically included in the shaded JAR for EXE generation**

#### Category Icons Mapping
| Category | Icon | Library | Code |
|----------|------|---------|------|
| All Algorithms | Grid/Tiles | FontAwesome | `FontAwesomeSolid.TH_LARGE` |
| Searching | Magnifying Glass | FontAwesome | `FontAwesomeSolid.SEARCH` |
| Sorting | Sort Ascending | Material Design | `MaterialDesignS.SORT_ASCENDING` |
| Graph Algorithms | Graph Outline | Material Design | `MaterialDesignG.GRAPH_OUTLINE` |
| Backtracking | Undo Arrow | FontAwesome | `FontAwesomeSolid.UNDO` |
| Pathfinding | Map Marker Path | Material Design | `MaterialDesignM.MAP_MARKER_PATH` |
| Practical Applications | Cogs/Settings | Material Design | `MaterialDesignC.COGS` |
| Real-World Problems | Puzzle Piece | FontAwesome | `FontAwesomeSolid.PUZZLE_PIECE` |

### Algorithm Class Enhancement

#### New Constructor
```java
// Multiple categories
public Algorithm(String name, String description, String... categories)

// Example usage:
public SudokuAlgorithm() {
    super(
        "Sudoku Solver",
        "Solve a 9x9 Sudoku puzzle using backtracking",
        "Real-World Problems",
        "Backtracking",
        "Practical Applications"
    );
}
```

#### New Methods
```java
// Get all tags/categories
public List<String> getTags()

// Check if algorithm has a specific category
public boolean hasCategory(String categoryName)
public boolean hasCategory(AlgorithmCategory category)
```

#### Backward Compatibility
The single-category constructor still works:
```java
public Algorithm(String name, String description, String category)
```

---

## Current Algorithm Categorization

| Algorithm | Primary Category | Additional Categories |
|-----------|------------------|----------------------|
| **Binary Search** | Searching | - |
| **Bubble Sort** | Sorting | - |
| **Selection Sort** | Sorting | - |
| **DFS** | Graph Algorithms | Backtracking |
| **N-Queens** | Backtracking | Practical Applications |
| **Sudoku Solver** | Real-World Problems | Backtracking, Practical Applications |
| **Maze Generation** | Pathfinding | Practical Applications |

---

## Adding Multi-Category Algorithms

### Step 1: Use Varargs Constructor
```java
public class NewAlgorithm extends Algorithm {
    public NewAlgorithm() {
        super(
            "Algorithm Name",
            "Description",
            "Primary Category",
            "Secondary Category",
            "Tertiary Category"
            // ... as many as needed
        );
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

### Step 2: Valid Category Strings
Use these exact strings for automatic categorization:
- `"Searching"`
- `"Sorting"`
- `"Graph Algorithms"` or `"Graph"`
- `"Backtracking"`
- `"Pathfinding"`
- `"Practical Applications"` or `"Practical"`
- `"Real-World Problems"` or `"Real-World"`

### Step 3: Register in MainController
Add to `initializeAlgorithms()`:
```java
allAlgorithms = FXCollections.observableArrayList(
    new BinarySearchAlgorithm(),
    // ... existing algorithms ...
    new NewAlgorithm()  // Your new algorithm
);
```

**The algorithm will automatically appear in ALL specified categories.**

---

## UI Updates

### Category Card Rendering
```java
// Old: Label with emoji
Label emoji = new Label(category.getEmoji());

// New: FontIcon with SVG
FontIcon icon = new FontIcon(category.getIcon());
icon.setIconSize(42);
icon.setIconColor(Color.web(categoryColor));
```

### Multi-Category Handling
The MainController now iterates through all algorithm tags:
```java
for (String tag : algo.getTags()) {
    AlgorithmCategory category = AlgorithmCategory.fromString(tag);
    if (!algorithmsByCategory.get(category).contains(algo)) {
        algorithmsByCategory.get(category).add(algo);
    }
}
```

This ensures:
- No duplicate entries in a category
- Algorithms appear in multiple categories
- "All" category contains every algorithm

---

## Icon Customization

### Changing Category Icons
Edit `AlgorithmCategory.java`:
```java
public enum AlgorithmCategory {
    SORTING("Sorting", "Description", MaterialDesignS.SORT_ASCENDING),
    // Change to a different icon:
    SORTING("Sorting", "Description", FontAwesomeSolid.SORT_AMOUNT_DOWN),
}
```

### Available Icon Packs
- **FontAwesome 5**: `FontAwesomeSolid.*`, `FontAwesomeRegular.*`, `FontAwesomeBrands.*`
- **Material Design 2**: `MaterialDesignA.*` through `MaterialDesignW.*` (alphabetical)

Browse icons:
- FontAwesome: https://fontawesome.com/icons
- Material Design: https://materialdesignicons.com/

---

## Build & Distribution

### Shaded JAR Verification
All Ikonli dependencies are included:
```
✅ ikonli-core-12.3.1.jar
✅ ikonli-javafx-12.3.1.jar
✅ ikonli-fontawesome5-pack-12.3.1.jar
✅ ikonli-materialdesign2-pack-12.3.1.jar
```

### Creating Windows EXE
```bash
# 1. Build shaded JAR
mvn clean package

# 2. Use Launch4j with PrashnaSetu.xml
# Output: AlgoQuest.exe with embedded JRE

# 3. Distribute with:
#    - AlgoQuest.exe
#    - jre/ (embedded JRE)
#    - openjfx-sdk/ (JavaFX SDK)
```

**✅ All icon dependencies are bundled - no external files needed**

---

## Benefits

### For Users
- ✅ **Better Discoverability**: Algorithms appear in multiple relevant categories
- ✅ **Professional Icons**: Sharp, scalable SVG icons instead of emojis
- ✅ **Consistent Design**: Unified icon style across the application
- ✅ **Visual Clarity**: Color-coded icons enhance recognition

### For Developers
- ✅ **Type Safety**: Icon enum prevents typos
- ✅ **Easy Customization**: Change icons in one place
- ✅ **Rich Icon Library**: 1000+ icons available
- ✅ **Semantic Tags**: Algorithms have meaningful categorization
- ✅ **Flexible Architecture**: Easy to add new categories

### For Distribution
- ✅ **No External Assets**: Icons embedded in JAR
- ✅ **Cross-Platform**: Works on Windows, Mac, Linux
- ✅ **Small Footprint**: Icon libraries add minimal size (~1MB total)
- ✅ **Launch4j Compatible**: Works seamlessly with EXE generation

---

## Troubleshooting

### Icons Not Displaying
1. Verify Ikonli dependencies in `pom.xml`
2. Check icon import statements
3. Ensure correct icon pack is imported (FontAwesome vs Material Design)
4. Run `mvn clean compile` to refresh dependencies

### Wrong Icon Shown
- Check spelling of icon constant
- Verify icon exists in the pack
- Browse available icons at official websites

### EXE Not Working
- Ensure shaded JAR includes all dependencies
- Verify `PrashnaSetu.xml` points to correct JAR
- Check JRE and JavaFX SDK paths

---

## Migration Notes

### From v1.0.0 to v1.1.0

**Breaking Changes:**
- `AlgorithmCategory.getEmoji()` → `AlgorithmCategory.getIcon()`
- Returns `Ikon` object instead of `String`

**Backward Compatible:**
- Single-category algorithm constructor still works
- Existing algorithms don't need to be updated (but can benefit from multi-category support)
- Primary category remains accessible via `getCategory()`

**Recommended Updates:**
1. Update algorithms to use multiple categories where appropriate
2. Add "Practical Applications" to relevant algorithms
3. Test category filtering in UI

---

## Future Enhancements

### Planned Features
- [ ] User-customizable icon themes
- [ ] Icon color customization in settings
- [ ] Category badges showing all tags on algorithm cards
- [ ] Filter by multiple categories simultaneously
- [ ] Icon animation effects
- [ ] Custom user-defined categories

---

## Resources

- **Ikonli Documentation**: https://kordamp.org/ikonli/
- **FontAwesome Icons**: https://fontawesome.com/v5/search
- **Material Design Icons**: https://materialdesignicons.com/
- **JavaFX Integration**: https://kordamp.org/ikonli/#_javafx

---

## Example: Complete Multi-Category Algorithm

```java
package com.algorithmvisualizer.model;

public class AStarAlgorithm extends Algorithm {
    public AStarAlgorithm() {
        super(
            "A* Search",
            "Find the shortest path using heuristics and cost functions",
            "Pathfinding",              // Primary
            "Graph Algorithms",          // Also a graph algorithm
            "Practical Applications"     // Used in real-world navigation
        );
    }
    
    @Override
    public String getVisualizationFxml() {
        return "/fxml/astar-view.fxml";
    }
    
    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.AStarController.class;
    }
}
```

This algorithm will appear in:
- ✅ All Algorithms
- ✅ Pathfinding
- ✅ Graph Algorithms
- ✅ Practical Applications

---

**Version**: 1.1.0  
**Last Updated**: October 16, 2025  
**Dependencies**: Java 11+, JavaFX 17.0.2, Ikonli 12.3.1
