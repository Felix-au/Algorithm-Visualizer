package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.SudokuSolver;
import com.algorithmvisualizer.visualization.SudokuGridRenderer;
import com.algorithmvisualizer.algorithm.SudokuGenerator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.*;

public class SudokuController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;

    private SudokuGridRenderer gridView;
    private SudokuSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;

    private final Deque<SudokuSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    private int[][] currentGrid = DEFAULT_PUZZLE();
    private boolean[][] fixedMask = new boolean[9][9];

    @FXML
    private void initialize() {
        gridView = new SudokuGridRenderer();
        applyFixedMaskFromGrid();
        gridView.setGrid(currentGrid, fixedMask);

        solver = new SudokuSolver(currentGrid);
        solver.setStepListener(this::onStepEvent);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(gridView.getNode());
        }

        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Sudoku Solver");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(hdr, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15.0);
            javafx.scene.shape.Rectangle cur = new javafx.scene.shape.Rectangle(12,12);
            cur.setFill(javafx.scene.paint.Color.GOLD);
            cur.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lCur = new javafx.scene.layout.HBox(5.0, cur, new Label("Current"));
            javafx.scene.shape.Rectangle giv = new javafx.scene.shape.Rectangle(12,12);
            giv.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            giv.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lGiv = new javafx.scene.layout.HBox(5.0, giv, new Label("Given"));
            javafx.scene.shape.Rectangle fill = new javafx.scene.shape.Rectangle(12,12);
            fill.setFill(javafx.scene.paint.Color.WHITE);
            fill.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lFill = new javafx.scene.layout.HBox(5.0, fill, new Label("Filled"));
            javafx.scene.shape.Rectangle back = new javafx.scene.shape.Rectangle(12,12);
            back.setFill(javafx.scene.paint.Color.CRIMSON);
            back.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lBack = new javafx.scene.layout.HBox(5.0, back, new Label("Backtrack"));
            row.getChildren().addAll(lCur, lGiv, lFill, lBack);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters: show a puzzle input
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Grid:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(false);
            parent.paramBoardSizeSpinner.setManaged(false);
        }
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Puzzle (81 digits; 0 = empty)");
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("e.g. 530070000600195000098000060800060003400803001700020006060000280000419005000080079");
            parent.paramElementsField.setText(gridToString(currentGrid));
        }
        if (parent.paramRandomizeButton != null) parent.paramRandomizeButton.setOnAction(e -> onRandomizePuzzle());
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyPuzzle());

        // Solutions side header
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Sudoku");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }

        // Controls
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");
    }

    // --- Parameters ---
    private void onRandomizePuzzle() {
        // Generate a puzzle with at least one solution
        currentGrid = SudokuGenerator.generatePuzzle(45); // ~45 blanks (adjust difficulty as needed)
        applyFixedMaskFromGrid();
        // reflect in parameter field for copy/edit
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(gridToString(currentGrid));
        }
        refreshAll();
    }

    private void onApplyPuzzle() {
        if (parent == null || parent.paramElementsField == null) { refreshAll(); return; }
        int[][] parsed = parsePuzzle(parent.paramElementsField.getText());
        if (parsed != null) currentGrid = parsed;
        applyFixedMaskFromGrid();
        refreshAll();
    }

    private void applyFixedMaskFromGrid() {
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) fixedMask[r][c] = currentGrid[r][c] != 0;
    }

    private void refreshAll() {
        stopTimeline();
        solver.setGrid(currentGrid);
        gridView.setGrid(currentGrid, fixedMask);
        renderCode();
        initProgressLog();
        updateVariablesPanel();
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        String boardLiteral = gridToJavaLiteral(currentGrid);
        String[] lines = new String[] {
                "import java.util.*;",
                "",
                "public class SudokuExample {",
                "    static final int[][] BOARD = " + boardLiteral + ";",
                "",
                "    public static void main(String[] args) {",
                "        long t0 = System.currentTimeMillis();",
                "        solve(BOARD);",
                "        long t1 = System.currentTimeMillis();",
                "        print(BOARD);",
                "        System.out.println(\"Solved in \" + (t1 - t0) + \" ms\");",
                "    }",
                "",
                "    static boolean solve(int[][] g) {",
                "        int r=-1,c=-1;",
                "        for (int i=0;i<9;i++) for (int j=0;j<9;j++) if (g[i][j]==0){ r=i;c=j; i=9; break; }",
                "        if (r==-1) return true;",
                "        for (int v=1; v<=9; v++) if (can(g,r,c,v)) { g[r][c]=v; if (solve(g)) return true; g[r][c]=0; }",
                "        return false;",
                "    }",
                "    static boolean can(int[][] g,int r,int c,int v){",
                "        for(int k=0;k<9;k++) if (g[r][k]==v||g[k][c]==v) return false;",
                "        int br=(r/3)*3, bc=(c/3)*3;",
                "        for(int i=0;i<3;i++) for(int j=0;j<3;j++) if (g[br+i][bc+j]==v) return false;",
                "        return true;",
                "    }",
                "    static void print(int[][] g){",
                "        for(int i=0;i<9;i++){",
                "            for(int j=0;j<9;j++) System.out.print(g[i][j]+\" \");",
                "            System.out.println();",
                "        }",
                "    }",
                "}",
        };
        parent.codeArea.setText(String.join("\n", lines));
    }

    private String gridToJavaLiteral(int[][] g) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i=0;i<9;i++) {
            if (i>0) sb.append(", ");
            sb.append("{");
            for (int j=0;j<9;j++) { if (j>0) sb.append(", "); sb.append(g[i][j]); }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private String gridToString(int[][] g) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<9;i++) for (int j=0;j<9;j++) sb.append(g[i][j]);
        return sb.toString();
    }

    private int[][] parsePuzzle(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("[^0-9]", "");
        if (digits.length() != 81) return null;
        int[][] g = new int[9][9];
        int idx = 0;
        for (int i=0;i<9;i++) for (int j=0;j<9;j++) g[i][j] = digits.charAt(idx++) - '0';
        return g;
    }

    // --- Controls ---
    public void onPlay() {
        if (isPlaying) { onPause(); return; }
        isPlaying = true;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
        if (parent != null) parent.playButton.setText("⏸ Pause");
    }

    public void onPause() { stopTimeline(); }

    public void onStepForward() {
        if (solver == null || solver.isDone()) return;
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        SudokuSolver.State s = history.pop();
        solver.restore(s);
        gridView.setGrid(s.grid, s.fixed);
        // Remove logs of last step
        if (parent != null && parent.progressArea != null) {
            if (countLogsForStep && currentStepLogLines > 0) {
                removeLastLogLines(currentStepLogLines);
                currentStepLogLines = 0;
                countLogsForStep = false;
            } else if (!progressHistory.isEmpty()) {
                int toRemove = progressHistory.pop();
                removeLastLogLines(toRemove);
            }
        }
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Step back.");
    }

    public void onReset() {
        stopTimeline();
        history.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        solver.reset();
        gridView.setGrid(solver.getGrid(), solver.getFixedMask());
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready.");
    }

    private void stopTimeline() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        isPlaying = false;
        if (parent != null) parent.playButton.setText("▶ Play");
    }

    private void updatePlaybackSpeed() {
        if (!isPlaying) return;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
    }

    private void rebuildTimelineWithCurrentSpeed() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        double speed = parent != null ? parent.speedSlider.getValue() : 5.0;
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (solver.isDone()) { stopTimeline(); return; }
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void onStepEvent(SudokuSolver.StepType type, int r, int c, int val) {
        // group logs for step-back removal
        countLogsForStep = true;
        currentStepLogLines = 0;
        switch (type) {
            case INIT:
                gridView.setGrid(solver.getGrid(), solver.getFixedMask());
                gridView.clearHighlights();
                appendProgress("🚀 Start solving");
                if (parent != null) parent.stepDescription.setText("Start solving");
                break;
            case PLACE:
                gridView.setCell(r, c, val, false);
                gridView.clearHighlights();
                gridView.highlightCurrent(r, c);
                appendProgress("✏ Place " + val + " at (" + r + "," + c + ")");
                if (parent != null) parent.stepDescription.setText("Place " + val + " at (" + r + "," + c + ")");
                break;
            case BACKTRACK:
                gridView.setCell(r, c, 0, false);
                gridView.flashBacktrack(r, c);
                appendProgress("↩ Backtrack from (" + r + "," + c + ")");
                if (parent != null) parent.stepDescription.setText("Backtrack from (" + r + "," + c + ")");
                break;
            case DONE:
                appendProgress("🎉 Solved");
                if (parent != null) parent.stepDescription.setText("Solved");
                stopTimeline();
                break;
        }
        updateVariablesPanel();
        // finalize log group
        if (countLogsForStep) {
            progressHistory.push(currentStepLogLines);
            countLogsForStep = false;
            currentStepLogLines = 0;
        }
    }

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        appendProgress("Sudoku ready.");
    }

    private void appendProgress(String line) {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.appendText(line + "\n");
        if (countLogsForStep) currentStepLogLines++;
    }

    private void removeLastLogLines(int count) {
        if (parent == null || parent.progressArea == null || count <= 0) return;
        String text = parent.progressArea.getText();
        if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n", -1);
        int total = lines.length;
        int newLen = Math.max(0, total - count);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newLen; i++) sb.append(lines[i]).append("\n");
        parent.progressArea.setText(sb.toString());
    }

    private void updateVariablesPanel() {
        if (parent == null || parent.variableList == null) return;
        parent.variableList.getItems().clear();
        int filled = 0;
        int[][] g = solver.getGrid();
        for (int i=0;i<9;i++) for (int j=0;j<9;j++) if (g[i][j] != 0) filled++;
        parent.variableList.getItems().addAll(
                "filled: " + filled + "/81",
                "done: " + solver.isDone()
        );
    }

    private static int[][] DEFAULT_PUZZLE() {
        String s = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        int[][] g = new int[9][9];
        int idx=0;
        for (int i=0;i<9;i++) for (int j=0;j<9;j++) g[i][j] = s.charAt(idx++) - '0';
        return g;
    }
}
