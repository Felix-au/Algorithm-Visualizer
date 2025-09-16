package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.NQueensSolver;
import com.algorithmvisualizer.visualization.ChessboardRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.geometry.Pos;
import java.util.ArrayDeque;
import java.util.Deque;

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
    
    // Backtracking animation fields
    private double backtrackDelay = 1.0; // seconds
    private boolean isBacktrackingInProgress = false;
    private Timeline blinkTimeline;
    private Label backtrackingIndicator;
    
    // Step-back history
    private Deque<com.algorithmvisualizer.algorithm.NQueensSolver.State> history = new ArrayDeque<>();
    
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

        // Live speed adjustment mid-run
        if (parentController != null && parentController.speedSlider != null) {
            parentController.speedSlider.valueProperty().addListener((obs, ov, nv) -> {
                updatePlaybackSpeed();
            });
        }
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
        // Ensure live speed adjustment listener is attached (parent available here)
        if (parentController.speedSlider != null) {
            parentController.speedSlider.valueProperty().addListener((obs, ov, nv) -> updatePlaybackSpeed());
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
            // Add backtracking indicator overlay (top center)
            if (backtrackingIndicator == null) {
                backtrackingIndicator = new Label();
                backtrackingIndicator.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 6;");
                backtrackingIndicator.setVisible(false);
                backtrackingIndicator.setManaged(false);
            }
            if (!parentController.chessboardContainer.getChildren().contains(backtrackingIndicator)) {
                parentController.chessboardContainer.getChildren().add(backtrackingIndicator);
                StackPane.setAlignment(backtrackingIndicator, Pos.TOP_CENTER);
            }
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
        // Clear step-back history when creating a new solver/board
        history.clear();

        // Code view
        renderCode();

        // Initialize variable and loop tracking
        updateVariableTracking();
        initProgressLog();

        updateStatus("Views generated. Ready to solve.");
    }

    private void legacyPlaceBacktrack(int row, int col, boolean isPlacing, boolean isBacktracking) {
        if (isPlacing) {
            if (chessboardRenderer != null) chessboardRenderer.placeQueen(row, col);
        } else if (isBacktracking) {
            // Start animated backtracking: blink then remove after a pause
            startBacktrackingAnimation(row, col);
            return; // defer updateVariableTracking until animation tick
        }
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
                // Visual handling is initiated in legacyPlaceBacktrack; keep logs here
                highlightCode("BACKTRACK");
                appendProgress("");
                appendProgress("row " + row + " & col " + col + " didn't work out. Backtracking");
                appendProgress("");
                break;
            case SOLUTION:
                solutionsFound = solver.getSolutionsFound();
                solutionsLabel.setText(String.valueOf(solutionsFound));
                updateStatus("Solution found: " + solutionsFound);
                highlightCode("SOLUTION");
                appendProgress("");
                appendProgress("Found a solution (#" + solutionsFound + ")");
                appendProgress("");
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
                
                // Format queens array as (row,col) pairs
                String queensFormatted = formatQueensArray(solver.getQueenColumnByRow());
                
                parentController.variableList.getItems().addAll(
                    "Board size and No. of Queens, N: " + currentBoardSize,
                    "solutionCount: " + solutionsFound,
                    "currentRow: " + rowIdx0,
                    "currentCol: " + (colExhausted ? (colIdx0 + " (exhausted)") : colIdx0),
                    "queens[]: " + queensFormatted,
                    "algorithmState: " + (solver.isCompleted() ? "COMPLETED" : "SEARCHING")
                );
                
                // Step description
                String stepDesc = solver.getCurrentStepDescription();
                parentController.stepDescription.setText(stepDesc);
            } else {
                // Format empty queens array
                String emptyQueens = "[" + " (-1,-1)".repeat(currentBoardSize).substring(1) + " ]";
                
                parentController.variableList.getItems().addAll(
                    "Board size and No. of Queens, N: " + currentBoardSize,
                    "solutionCount: 0",
                    "currentRow: 0",
                    "currentCol: 0",
                    "queens[]: " + emptyQueens,
                    "algorithmState: READY"
                );
                parentController.stepDescription.setText("Ready to start algorithm visualization.");
            }
        }
    }
    
    private String formatQueensArray(int[] queens) {
        if (queens == null || queens.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < queens.length; i++) {
            if (i > 0) sb.append(", ");
            int col = queens[i];
            if (col == -1) {
                sb.append("(-1,-1)");
            } else {
                sb.append("(").append(i).append(",").append(col).append(")");
            }
        }
        sb.append("]");
        return sb.toString();
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
        history.clear();
        // Clear solutions display
        if (parentController != null && parentController.solutionsContent != null) {
            parentController.solutionsContent.getChildren().clear();
            parentController.solutionsContent.getChildren().add(new Label("No solutions found yet"));
        }
        // Clear progress area per request
        if (parentController != null && parentController.progressArea != null) {
            parentController.progressArea.clear();
        }
        updateStatus("Algorithm reset. Ready to solve.");
        updateVariableTracking();
    }
    
    // Methods called by parent controller
    public void onStepBack() {
        if (solver == null) return;
        // Pause if playing
        if (isPlaying) onPause();
        if (!history.isEmpty()) {
            com.algorithmvisualizer.algorithm.NQueensSolver.State prev = history.pop();
            solver.restore(prev);
            // Re-render board from restored state
            rerenderBoardFromSolver();
            // Sync solutions UI with model to remove any solutions beyond restored point
            syncSolutionsUIFromModel();
            // Update solutions count label
            solutionsFound = solver.getSolutionsFound();
            solutionsLabel.setText(String.valueOf(solutionsFound));
            updateVariableTracking();
            updateStatus("Stepped backward.");
        } else {
            updateStatus("No previous step to go back to.");
        }
    }
    
    public void onPlay() {
        if (solver == null) return;
        if (isPlaying) {
            onPause();
            return;
        }
        
        isPlaying = true;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
        updateStatus("Playing...");
        updatePlayButtonStates();
    }
    
    public void onPause() {
        stopTimeline();
        updateStatus("Paused.");
        updatePlayButtonStates();
    }
    
    public void onStepForward() {
        if (isBacktrackingInProgress) return;
        if (solver != null && !solver.isCompleted()) {
            // Snapshot before stepping for step-back support
            history.push(solver.snapshot());
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

    // --- Backtracking visuals ---
    private void startBacktrackingAnimation(int row, int col) {
        if (isBacktrackingInProgress) return;
        isBacktrackingInProgress = true;
        // Ensure the queen is visible to blink (in case it was hidden previously)
        if (chessboardRenderer != null) chessboardRenderer.placeQueen(row, col);

        // Show indicator
        if (backtrackingIndicator != null) {
            backtrackingIndicator.setText("BACKTRACKING: Row " + row + ", Col " + col);
            backtrackingIndicator.setVisible(true);
        }

        // Start blink timeline (toggle visibility)
        if (blinkTimeline != null) {
            blinkTimeline.stop();
        }
        javafx.scene.shape.Circle queenNode = chessboardRenderer != null ? chessboardRenderer.getQueenNode(row, col) : null;
        blinkTimeline = new Timeline(
                new KeyFrame(Duration.millis(200), ev -> { if (queenNode != null) queenNode.setVisible(false); }),
                new KeyFrame(Duration.millis(400), ev -> { if (queenNode != null) queenNode.setVisible(true); })
        );
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        blinkTimeline.play();

        // Pause before final removal
        PauseTransition pause = new PauseTransition(Duration.seconds(backtrackDelay));
        pause.setOnFinished(ev -> finishBacktrackingAnimation(row, col));
        pause.play();
    }

    private void finishBacktrackingAnimation(int row, int col) {
        // Stop blinking
        if (blinkTimeline != null) {
            blinkTimeline.stop();
            blinkTimeline = null;
        }
        // Remove queen
        if (chessboardRenderer != null) chessboardRenderer.removeQueen(row, col);
        // Hide indicator
        if (backtrackingIndicator != null) backtrackingIndicator.setVisible(false);
        isBacktrackingInProgress = false;
        updateVariableTracking();
    }
    
    private void rerenderBoardFromSolver() {
        if (chessboardRenderer == null || solver == null) return;
        chessboardRenderer.clearBoard();
        int[] q = solver.getQueenColumnByRow();
        for (int r = 0; r < q.length; r++) {
            int c = q[r];
            if (c >= 0) chessboardRenderer.placeQueen(r, c);
        }
    }
    
    private void syncSolutionsUIFromModel() {
        if (parentController == null || parentController.solutionsContent == null || solver == null) return;
        parentController.solutionsContent.getChildren().clear();
        java.util.List<int[]> sols = solver.getSolutions();
        if (sols.isEmpty()) {
            parentController.solutionsContent.getChildren().add(new Label("No solutions found yet"));
            return;
        }
        for (int i = 0; i < sols.size(); i++) {
            displaySolution(sols.get(i), i + 1);
        }
    }
    
    // --- Playback speed helpers ---
    private void updatePlaybackSpeed() {
        if (!isPlaying) return;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
    }
    
    private void rebuildTimelineWithCurrentSpeed() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        double speed = parentController != null ? parentController.speedSlider.getValue() : 5.0;
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (isBacktrackingInProgress) return;
            if (solver.isCompleted()) {
                stopTimeline();
                return;
            }
            // Snapshot before stepping for step-back support
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }
    
    private void updatePlayButtonStates() {
        if (parentController != null) {
            // Toggle single play/pause button
            parentController.playButton.setText(isPlaying ? "⏸ Pause" : "▶ Play");
            parentController.playButton.setDisable(false);
            // Hide dedicated pause button
            if (parentController.pauseButton != null) {
                parentController.pauseButton.setVisible(false);
                parentController.pauseButton.setManaged(false);
            }
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
                    "    static final int N = " + currentBoardSize + ";",
                    "    static int solutionCount = 0;",
                    "",
                    "    public static void main(String[] args) {",
                    "        System.out.println(\"Solving N-Queens problem for \" + N + \"x\" + N + \" board:\");",
                    "        System.out.println(\"=====================================\");",
                    "",
                    "        long startTime = System.currentTimeMillis();",
                    "",
                    "        int[] queens = new int[N]; // queens[i] = column of queen at row i",
                    "        solve(0, queens);",
                    "",
                    "        long endTime = System.currentTimeMillis();",
                    "        System.out.println(\"=====================================\");",
                    "        System.out.println(\"Total solutions found: \" + solutionCount);",
                    "        System.out.println(\"Execution time: \" + (endTime - startTime) + \" ms\");",
                    "    }",
                    "",
                    "    static void solve(int row, int[] queens) {",
                    "        if (row == N) {",
                    "            solutionCount++;",
                    "            printSolution(queens);",
                    "            return;",
                    "        }",
                    "",
                    "        for (int col = 0; col < N; col++) {",
                    "            if (isSafe(row, col, queens)) {",
                    "                queens[row] = col;",
                    "                solve(row + 1, queens);",
                    "            }",
                    "        }",
                    "    }",
                    "",
                    "    static boolean isSafe(int row, int col, int[] queens) {",
                    "        for (int i = 0; i < row; i++) {",
                    "            int otherCol = queens[i];",
                    "            if (otherCol == col || Math.abs(otherCol - col) == Math.abs(i - row)) {",
                    "                return false;",
                    "            }",
                    "        }",
                    "        return true;",
                    "    }",
                    "",
                    "    static void printSolution(int[] queens) {",
                    "        System.out.println(\"\\nSolution #\" + solutionCount + \":\");",
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
                    "",
                    "        System.out.print(\"Queen positions:\");",
                    "        for (int row = 0; row < N; row++) {",
                    "            System.out.print(\" (\" + row + \",\" + queens[row] + \")\");",
                    "        }",
                    "        System.out.println();",
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
        // Remove any existing highlighting
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(">> ")) lines[i] = lines[i].substring(3);
        }
        
        // Update the code without any highlighting or cursor movement
        String updated = String.join("\n", lines);
        parentController.codeArea.setText(updated);
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
        
        // Remove the placeholder label if it's still present
        if (!parentController.solutionsContent.getChildren().isEmpty()
                && parentController.solutionsContent.getChildren().size() == 1
                && parentController.solutionsContent.getChildren().get(0) instanceof Label) {
            Label placeholder = (Label) parentController.solutionsContent.getChildren().get(0);
            if ("No solutions found yet".equals(placeholder.getText())) {
                parentController.solutionsContent.getChildren().clear();
            }
        }

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
