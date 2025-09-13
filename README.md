# Algorithm Visualizer

A Java application for visualizing algorithms step by step, starting with the N-Queens problem.

## Features

- **Interactive GUI** with JavaFX
- **Step-by-step algorithm execution** with variable tracking
- **Visual representation** of algorithm progress
- **Editable parameters** (board size, execution speed)
- **Control panel** with Play, Pause, Step, and Reset functionality

## Current Algorithms

- **N-Queens Problem**: Place N queens on an N×N chessboard so that no two queens attack each other

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## How to Run

1. **Clone or download** this project to your local machine

2. **Navigate to the project directory**:
   ```bash
   cd Algorithm-Visualizer
   ```

3. **Compile and run the application**:
   ```bash
   mvn clean javafx:run
   ```

   Or if you prefer to compile first:
   ```bash
   mvn clean compile
   mvn javafx:run
   ```

## How to Use

1. **Start the application** - You'll see the main menu with available algorithms
2. **Select N-Queens** from the list and click "Select Algorithm"
3. **Set the board size** using the spinner (4-12 queens)
4. **Click "Generate Board"** to create the chessboard
5. **Use the controls**:
   - **Play**: Run the algorithm automatically
   - **Step**: Execute one step at a time
   - **Pause**: Pause the current execution
   - **Reset**: Reset the algorithm to the beginning
6. **Watch the visualization** as queens are placed and conflicts are highlighted
7. **Track variables** in the right panel to see algorithm state

## Project Structure

```
src/main/java/com/algorithmvisualizer/
├── Main.java                          # Application entry point
├── model/
│   ├── Algorithm.java                 # Base algorithm class
│   └── NQueensAlgorithm.java          # N-Queens algorithm definition
├── algorithm/
│   └── NQueensSolver.java             # N-Queens backtracking implementation
├── visualization/
│   └── ChessboardRenderer.java        # Chessboard visualization
└── ui/
    ├── MainController.java            # Main menu controller
    ├── AlgorithmViewController.java   # Base algorithm view controller
    └── NQueensController.java         # N-Queens specific controller
```

## Features in Detail

### N-Queens Visualization
- **Chessboard display** with alternating light/dark squares
- **Queen placement** with visual feedback
- **Conflict highlighting** showing attacking positions
- **Step-by-step execution** showing backtracking
- **Variable tracking** displaying current row, column, and queens placed
- **Solution counting** showing total solutions found

### Controls
- **Board Size**: Adjustable from 4 to 12 queens
- **Speed Control**: Slider to adjust execution speed
- **Step Execution**: Manual step-by-step progression
- **Reset Functionality**: Return to initial state

## Future Enhancements

- Additional algorithms (Sorting, Pathfinding, etc.)
- Animation speed controls
- Export functionality for solutions
- Algorithm comparison mode
- Custom input data support

## Troubleshooting

If you encounter issues:

1. **JavaFX not found**: Make sure you have JavaFX installed or use a JDK that includes it
2. **Maven issues**: Ensure Maven is properly installed and configured
3. **Java version**: Verify you're using Java 11 or higher

## Contributing

This project is designed to be extensible. To add new algorithms:

1. Create a new algorithm class extending `Algorithm`
2. Implement the algorithm logic with step tracking
3. Create a visualization component
4. Add a controller for the algorithm-specific UI
5. Register the algorithm in the main controller

## License

This project is open source and available under the MIT License.
