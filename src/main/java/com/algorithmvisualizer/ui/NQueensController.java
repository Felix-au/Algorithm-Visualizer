package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.NQueensSolver;
import com.algorithmvisualizer.visualization.ChessboardRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Controller for the N-Queens algorithm visualization
 */
public class NQueensController implements AlgorithmViewController.AlgorithmSpecificController {
    
    @FXML
    private Spinner<Integer> boardSizeSpinner;
    
    @FXML
    private Spinner<Integer> numQueensSpinner;
    
    @FXML
    private Button generateBoardButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label solutionsLabel;
    
    @FXML
    private StackPane chessboardContainer;
    
    
    @FXML
    private TextArea codeArea;
    
    private AlgorithmViewController parentController;
    private ChessboardRenderer chessboardRenderer;
    private NQueensSolver solver;
    private int currentBoardSize = 4;
    private int solutionsFound = 0;
    private Timeline timeline;
    private boolean isPlaying = false;
    
    @FXML
    private void initialize() {
        // Initialize spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory factory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4);
        boardSizeSpinner.setValueFactory(factory);
        
        SpinnerValueFactory.IntegerSpinnerValueFactory queensFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4);
        numQueensSpinner.setValueFactory(queensFactory);
        // Hide internal queens spinner (UI handled in main layout)
        numQueensSpinner.setVisible(false);
        numQueensSpinner.setManaged(false);
        
        // Parent control panel spinners (if present)
        if (parentController != null) {
            if (parentController.paramBoardSizeSpinner != null) {
                parentController.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4));
                parentController.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> {
                    boardSizeSpinner.getValueFactory().setValue(n);
                    // Keep number of queens equal to N
                    numQueensSpinner.getValueFactory().setValue(n);
                });
            }
            // Hide external queens spinner as well
            if (parentController.paramNumQueensSpinner != null) {
                parentController.paramNumQueensSpinner.setVisible(false);
                parentController.paramNumQueensSpinner.setManaged(false);
            }
            if (parentController.paramApplyButton != null) {
                parentController.paramApplyButton.setOnAction(e -> onGenerateBoard());
            }
        }
        
        // Keep N and number of queens in sync by clamping number of queens to N
        boardSizeSpinner.valueProperty().addListener((obs, o, n) -> {
            numQueensSpinner.getValueFactory().setValue(n);
        });
        
        // Initialize views
        generateViews();
        setupCodeBindings();
        initProgressLog();
    }
    
    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parentController = parent;
        // Generate views now that we have the parent controller
        generateViews();
        // Wire parameter spinners to inner controls
        if (parentController.paramBoardSizeSpinner != null) {
            parentController.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, boardSizeSpinner.getValue()));
            parentController.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> {
                boardSizeSpinner.getValueFactory().setValue(n);
            });
        }
        if (parentController.paramNumQueensSpinner != null) {
            parentController.paramNumQueensSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, numQueensSpinner.getValue()));
            parentController.paramNumQueensSpinner.valueProperty().addListener((obs, o, n) -> {
                int N = boardSizeSpinner.getValue();
                numQueensSpinner.getValueFactory().setValue(Math.min(n, N));
            });
        }
        if (parentController.paramApplyButton != null) {
            parentController.paramApplyButton.setOnAction(e -> onGenerateBoard());
        }
    }
    
    @FXML
    private void onGenerateBoard() {
        currentBoardSize = boardSizeSpinner.getValue();
        // Set number of queens equal to N
        numQueensSpinner.getValueFactory().setValue(currentBoardSize);
        generateViews();
        resetAlgorithm();
    }
    
    private void generateViews() {
        // Board - use parent controller's containers
        if (parentController != null && parentController.chessboardContainer != null) {
            parentController.chessboardContainer.getChildren().clear();
            chessboardRenderer = new ChessboardRenderer(currentBoardSize);
            parentController.chessboardContainer.getChildren().add(chessboardRenderer.getChessboard());
        }

        // Initialize solutions display
        if (parentController != null && parentController.solutionsContent != null) {
            parentController.solutionsContent.getChildren().clear();
            parentController.solutionsContent.getChildren().add(new Label("No solutions found yet"));
        }

        // Solver
        solver = new NQueensSolver(currentBoardSize);
        solver.setVisualizationCallback(this::legacyPlaceBacktrack);
        solver.setStepListener(this::onStepEvent);

        // Code view
        renderCode();

        // Initialize variable and loop tracking
        updateVariableTracking();
        initProgressLog();

        updateStatus("Views generated. Ready to solve.");
    }

    private void legacyPlaceBacktrack(int row, int col, boolean isPlacing, boolean isBacktracking) {
        if (isPlacing) chessboardRenderer.placeQueen(row, col);
        else if (isBacktracking) chessboardRenderer.removeQueen(row, col);
        updateVariableTracking();
    }

    private void onStepEvent(NQueensSolver.StepType type, int row, int col) {
        switch (type) {
            case CHECK:
                if (chessboardRenderer != null) chessboardRenderer.highlightCurrentPosition(row, col);
                if (parentController != null) parentController.stepDescription.setText("Check row " + row + ", col " + col);
                highlightCode("CHECK");
                appendProgress("Trying queen " + solver.getQueensPlaced() + " at row " + row + " & col " + col);
                break;
            case PLACE:
                if (chessboardRenderer != null) chessboardRenderer.placeQueen(row, col);
                highlightCode("PLACE");
                appendProgress("Placed queen at row " + row + " & col " + col);
                break;
            case BACKTRACK:
                if (chessboardRenderer != null) chessboardRenderer.removeQueen(row, col);
                highlightCode("BACKTRACK");
                appendProgress("row " + row + " & col " + col + " didn't work out. Backtracking");
                break;
            case SOLUTION:
                solutionsFound = solver.getSolutionsFound();
                solutionsLabel.setText(String.valueOf(solutionsFound));
                updateStatus("Solution found: " + solutionsFound);
                highlightCode("SOLUTION");
                appendProgress("Found a solution (#" + solutionsFound + ")");
                displaySolution(solver.getSolutions().get(solver.getSolutions().size() - 1), solutionsFound);
                break;
            case DONE:
                updateStatus("Completed. Solutions: " + solver.getSolutionsFound());
                stopTimeline();
                highlightCode("DONE");
                appendProgress("Search completed.");
                finalizeProgressSummary();
                break;
        }
        updateVariableTracking();
    }
    
    private void updateVariableTracking() {
        if (parentController != null) {
            // Update variable list (0-based only)
            parentController.variableList.getItems().clear();
            if (solver != null) {
                int rowIdx0 = solver.getCurrentRow();
                int colIdx0 = solver.getCurrentColumn();
                boolean colExhausted = colIdx0 >= currentBoardSize;
                parentController.variableList.getItems().addAll(
                    "boardSize (N): " + currentBoardSize,
                    "numberOfQueens: " + numQueensSpinner.getValue(),
                    "rowIndex: " + rowIdx0,
                    "colIndex: " + (colExhausted ? (colIdx0 + " (exhausted)") : colIdx0),
                    "queensPlaced: " + solver.getQueensPlaced(),
                    "solutionsFound: " + solutionsFound,
                    "isCompleted: " + solver.isCompleted()
                );
                
                // Step description
                String stepDesc = solver.getCurrentStepDescription();
                parentController.stepDescription.setText(stepDesc);
            } else {
                parentController.variableList.getItems().addAll(
                    "boardSize (N): " + currentBoardSize,
                    "numberOfQueens: " + numQueensSpinner.getValue(),
                    "rowIndex: 0",
                    "colIndex: 0",
                    "queensPlaced: 0",
                    "solutionsFound: 0",
                    "isCompleted: false"
                );
                parentController.stepDescription.setText("Ready to start algorithm visualization.");
            }
        }
    }
    
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    private void resetAlgorithm() {
        solutionsFound = 0;
        solutionsLabel.setText("0");
        stopTimeline();
        solver.reset();
        chessboardRenderer.clearBoard();
        // Clear solutions display
        if (parentController != null && parentController.solutionsContent != null) {
            parentController.solutionsContent.getChildren().clear();
            parentController.solutionsContent.getChildren().add(new Label("No solutions found yet"));
        }
        updateStatus("Algorithm reset. Ready to solve.");
        updateVariableTracking();
    }
    
    // Methods called by parent controller
    public void onStepBack() {
        if (solver != null && !solver.isCompleted()) {
            // TODO: Implement step backward functionality
            updateStatus("Step backward not yet implemented");
        }
    }
    
    public void onPlay() {
        if (solver == null) return;
        if (isPlaying) return;
        
        isPlaying = true;
        double speed = parentController != null ? parentController.speedSlider.getValue() : 5.0;
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (solver.isCompleted()) {
                stopTimeline();
                return;
            }
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        updateStatus("Playing...");
        updatePlayButtonStates();
    }
    
    public void onPause() {
        stopTimeline();
        updateStatus("Paused.");
        updatePlayButtonStates();
    }
    
    public void onStepForward() {
        if (solver != null && !solver.isCompleted()) {
            solver.step();
            updateVariableTracking();
        }
    }
    
    public void onReset() {
        resetAlgorithm();
        updatePlayButtonStates();
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isPlaying = false;
    }
    
    private void updatePlayButtonStates() {
        if (parentController != null) {
            parentController.playButton.setDisable(isPlaying);
            parentController.pauseButton.setDisable(!isPlaying);
        }
    }

    private void setupCodeBindings() {
        if (codeArea == null) return;
        codeArea.setEditable(true); // Allow editing but we constrain it
        codeArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int extracted = extractNFromCode(newVal);
            if (extracted != -1 && extracted != currentBoardSize && extracted >= 1 && extracted <= 10) {
                currentBoardSize = extracted;
                boardSizeSpinner.getValueFactory().setValue(currentBoardSize);
                // Keep number of queens equal to N
                numQueensSpinner.getValueFactory().setValue(currentBoardSize);
                generateViews();
                resetAlgorithm();
            } else {
                // Re-render to lock any other edits
                renderCode();
            }
        });
        renderCode();
    }

    private void renderCode() {
        if (parentController != null && parentController.codeArea != null) {
            String[] lines = new String[] {
                    "public class NQueens {",
                    "    private int N;",
                    "    private int[] queens;",
                    "    private boolean[] usedColumns;",
                    "    private boolean[] usedDiagonals1; // row - col",
                    "    private boolean[] usedDiagonals2; // row + col",
                    "    private int solutionsFound;",
                    "    ",
                    "    public NQueens(int boardSize) {",
                    "        this.N = boardSize;",
                    "        this.queens = new int[N];",
                    "        this.usedColumns = new boolean[N];",
                    "        this.usedDiagonals1 = new boolean[2 * N - 1];",
                    "        this.usedDiagonals2 = new boolean[2 * N - 1];",
                    "        this.solutionsFound = 0;",
                    "        ",
                    "        // Initialize queens array to -1 (no queen placed)",
                    "        for (int i = 0; i < N; i++) {",
                    "            queens[i] = -1;",
                    "        }",
                    "    }",
                    "    ",
                    "    public void solve() {",
                    "        solve(0);",
                    "    }",
                    "    ",
                    "    private void solve(int row) {",
                    "        if (row == N) {",
                    "            // Found a solution",
                    "            solutionsFound++;",
                    "            printSolution();",
                    "            return;",
                    "        }",
                    "        ",
                    "        for (int col = 0; col < N; col++) {",
                    "            if (isSafe(row, col)) {",
                    "                placeQueen(row, col);",
                    "                solve(row + 1);",
                    "                removeQueen(row, col);",
                    "            }",
                    "        }",
                    "    }",
                    "    ",
                    "    private boolean isSafe(int row, int col) {",
                    "        return !usedColumns[col] ",
                    "            && !usedDiagonals1[row - col + N - 1] ",
                    "            && !usedDiagonals2[row + col];",
                    "    }",
                    "    ",
                    "    private void placeQueen(int row, int col) {",
                    "        queens[row] = col;",
                    "        usedColumns[col] = true;",
                    "        usedDiagonals1[row - col + N - 1] = true;",
                    "        usedDiagonals2[row + col] = true;",
                    "    }",
                    "    ",
                    "    private void removeQueen(int row, int col) {",
                    "        queens[row] = -1;",
                    "        usedColumns[col] = false;",
                    "        usedDiagonals1[row - col + N - 1] = false;",
                    "        usedDiagonals2[row + col] = false;",
                    "    }",
                    "    ",
                    "    private void printSolution() {",
                    "        System.out.println(\"\\nSolution #\" + solutionsFound + \":\");",
                    "        ",
                    "        // Print the board",
                    "        for (int row = 0; row < N; row++) {",
                    "            for (int col = 0; col < N; col++) {",
                    "                if (queens[row] == col) {",
                    "                    System.out.print(\"Q \");",
                    "                } else {",
                    "                    System.out.print(\". \");",
                    "                }",
                    "            }",
                    "            System.out.println();",
                    "        }",
                    "        ",
                    "        // Print queen positions",
                    "        System.out.print(\"Queen positions: \");",
                    "        for (int i = 0; i < N; i++) {",
                    "            System.out.print(\"(\" + i + \",\" + queens[i] + \") \");",
                    "        }",
                    "        System.out.println();",
                    "    }",
                    "    ",
                    "    public int getSolutionsFound() {",
                    "        return solutionsFound;",
                    "    }",
                    "    ",
                    "    public static void main(String[] args) {",
                    "        int boardSize = " + currentBoardSize + "; // Current board size",
                    "        ",
                    "        System.out.println(\"Solving N-Queens problem for \" + boardSize + \"x\" + boardSize + \" board:\");",
                    "        System.out.println(\"=====================================\");",
                    "        ",
                    "        NQueens solver = new NQueens(boardSize);",
                    "        long startTime = System.currentTimeMillis();",
                    "        ",
                    "        solver.solve();",
                    "        ",
                    "        long endTime = System.currentTimeMillis();",
                    "        ",
                    "        System.out.println(\"\\n=====================================\");",
                    "        System.out.println(\"Total solutions found: \" + solver.getSolutionsFound());",
                    "        System.out.println(\"Execution time: \" + (endTime - startTime) + \" ms\");",
                    "    }",
                    "}"
            };
            parentController.codeArea.setText(String.join("\n", lines));
        }
    }

    private void highlightCode(String section) {
        if (parentController == null || parentController.codeArea == null) return;
        String code = parentController.codeArea.getText();
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(">> ")) lines[i] = lines[i].substring(3);
        }
        int idx = -1;
        switch (section) {
            case "CHECK":
                idx = findLine(lines, "for (int col = 0; col < N; col++)");
                break;
            case "PLACE":
                idx = findLine(lines, "placeQueen(row, col);");
                break;
            case "BACKTRACK":
                idx = findLine(lines, "removeQueen(row, col);");
                break;
            case "SOLUTION":
                idx = findLine(lines, "if (row == N)");
                break;
            default:
                idx = -1;
        }
        // Highlight the loop block for loop-related steps
        if (("CHECK".equals(section) || "PLACE".equals(section) || "BACKTRACK".equals(section))) {
            int loopStart = findLine(lines, "for (int col = 0; col < N; col++)");
            if (loopStart >= 0) {
                int loopEnd = findBlockEnd(lines, loopStart);
                for (int i = loopStart; i <= loopEnd && i < lines.length; i++) {
                    if (!lines[i].startsWith(">> ")) lines[i] = ">> " + lines[i];
                }
            }
        }
        // Mark the most relevant single line too
        if (idx >= 0 && idx < lines.length) {
            if (!lines[idx].startsWith(">> ")) lines[idx] = ">> " + lines[idx];
        }
        String updated = String.join("\n", lines);
        parentController.codeArea.setText(updated);
        // Move caret to highlighted line to ensure it is visible
        int caretLine = idx;
        if (caretLine < 0) caretLine = findLine(lines, "for (int col = 0; col < N; col++)");
        if (caretLine >= 0) {
            int startOffset = lineStartOffset(lines, caretLine);
            int endOffset = startOffset + lines[caretLine].length();
            try {
                parentController.codeArea.positionCaret(startOffset);
                parentController.codeArea.selectRange(startOffset, endOffset);
            } catch (Exception ignored) {}
        }
    }

    private int findLine(String[] lines, String needle) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(needle)) return i;
        }
        return -1;
    }

    private int findBlockEnd(String[] lines, int startIdx) {
        int i = startIdx;
        while (i < lines.length && !lines[i].contains("{")) i++;
        if (i == lines.length) return startIdx;
        int depth = 0;
        for (int j = i; j < lines.length; j++) {
            String line = lines[j];
            for (int k = 0; k < line.length(); k++) {
                char c = line.charAt(k);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return j;
                }
            }
        }
        return lines.length - 1;
    }

    private int lineStartOffset(String[] lines, int targetLine) {
        int offset = 0;
        for (int i = 0; i < targetLine && i < lines.length; i++) {
            offset += lines[i].length() + 1; // +1 for newline
        }
        return offset;
    }

    private int extractNFromCode(String text) {
        String marker = "final int N = ";
        int idx = text.indexOf(marker);
        if (idx < 0) return -1;
        int start = idx + marker.length();
        int end = text.indexOf(";", start);
        if (end < 0) return -1;
        try {
            return Integer.parseInt(text.substring(start, end).trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void initProgressLog() {
        if (parentController != null && parentController.progressArea != null) {
            parentController.progressArea.clear();
            appendProgress("N Queens: " + currentBoardSize + "X" + currentBoardSize + " matrix, " + numQueensSpinner.getValue() + " queens");
            appendProgress("Starting execution");
            appendProgress("Starting new iteration of nQueens() with currentQueen = 0 & currentCol = 0");
            appendProgress("--------------------------------------------------");
        }
    }

    private void appendProgress(String line) {
        if (parentController != null && parentController.progressArea != null) {
            parentController.progressArea.appendText(line + "\n");
        }
    }


    private void displaySolution(int[] solution, int solutionNumber) {
        if (parentController == null || parentController.solutionsContent == null) return;
        
        // Create a VBox for this solution
        VBox solutionBox = new VBox(5);
        solutionBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");
        
        // Add solution title
        Label titleLabel = new Label("Solution " + solutionNumber + ":");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        solutionBox.getChildren().add(titleLabel);
        
        // Create grid representation
        VBox gridBox = new VBox(2);
        for (int row = 0; row < currentBoardSize; row++) {
            HBox rowBox = new HBox(2);
            for (int col = 0; col < currentBoardSize; col++) {
                Label cellLabel = new Label(solution[row] == col ? "Q" : "0");
                cellLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 16px; -fx-min-width: 20; -fx-alignment: center;");
                if (solution[row] == col) {
                    cellLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 16px; -fx-min-width: 20; -fx-alignment: center; -fx-text-fill: red; -fx-font-weight: bold;");
                }
                rowBox.getChildren().add(cellLabel);
            }
            gridBox.getChildren().add(rowBox);
        }
        solutionBox.getChildren().add(gridBox);
        
        // Add to solutions container
        parentController.solutionsContent.getChildren().add(solutionBox);
    }
    
    private void finalizeProgressSummary() {
        if (parentController == null || parentController.progressArea == null) return;
        appendProgress("--------------------------------------------------");
        appendProgress("Total solutions found: " + solver.getSolutionsFound());
        int idx = 1;
        for (int[] sol : solver.getSolutions()) {
            appendProgress("\nSolution " + idx++ + ":");
            
            // Create grid representation
            for (int row = 0; row < currentBoardSize; row++) {
                StringBuilder rowBuilder = new StringBuilder();
                for (int col = 0; col < currentBoardSize; col++) {
                    if (sol[row] == col) {
                        rowBuilder.append("Q ");
                    } else {
                        rowBuilder.append("0 ");
                    }
                }
                appendProgress(rowBuilder.toString());
            }
        }
    }
}
