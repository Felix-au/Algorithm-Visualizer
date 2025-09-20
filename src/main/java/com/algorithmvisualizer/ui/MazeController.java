package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.MazeGeneratorDFS;
import com.algorithmvisualizer.algorithm.PathSolverBFS;
import com.algorithmvisualizer.visualization.MazeGridRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.*;

public class MazeController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;

    // Visuals
    private MazeGridRenderer gridView;

    // Engines
    private MazeGeneratorDFS generator;
    private PathSolverBFS solver;

    // Playback
    private Timeline timeline;
    private boolean isPlaying = false;

    // History
    private static class Snapshot {
        boolean solving; // false = generation, true = solving
        MazeGeneratorDFS.State genState;
        PathSolverBFS.State solveState;
        int logLines;
    }
    private final Deque<Snapshot> history = new ArrayDeque<>();

    // Logging
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Params
    private int rows = 15, cols = 15;
    private String genAlgo = "DFS"; // future: Prim, Kruskal
    private String pathAlgo = "BFS"; // future: DFS, Dijkstra, A*

    // Custom controls
    private ComboBox<String> genAlgoChoice;
    private ComboBox<String> pathAlgoChoice;

    private boolean solvingPhase = false; // false until generator DONE

    @FXML
    private void initialize() {
        gridView = new MazeGridRenderer();
        generator = new MazeGeneratorDFS(rows, cols);
        generator.setStepListener(this::onGenStep);
        solver = null; // created after generation
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place visuals
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(gridView.getNode());
        }

        // Header and legend
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Maze Generation + Pathfinding");
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
            javafx.scene.shape.Rectangle genRect = new javafx.scene.shape.Rectangle(12,12);
            genRect.setFill(javafx.scene.paint.Color.LIGHTBLUE);
            genRect.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lGen = new javafx.scene.layout.HBox(5.0, genRect, new Label("Visited (gen)"));
            javafx.scene.shape.Rectangle frRect = new javafx.scene.shape.Rectangle(12,12);
            frRect.setFill(javafx.scene.paint.Color.GOLD);
            frRect.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lFr = new javafx.scene.layout.HBox(5.0, frRect, new Label("Frontier (BFS)"));
            javafx.scene.shape.Rectangle pathRect = new javafx.scene.shape.Rectangle(12,12);
            pathRect.setFill(javafx.scene.paint.Color.FORESTGREEN);
            pathRect.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lPath = new javafx.scene.layout.HBox(5.0, pathRect, new Label("Shortest Path"));
            row.getChildren().addAll(lGen, lFr, lPath);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters wiring
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Grid size:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 50, rows));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onSizeChanged(n));
        }
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Algorithms:");
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(true);
            parent.paramElementsBox.setManaged(true);
            parent.paramElementsBox.getChildren().clear();
            genAlgoChoice = new ComboBox<>();
            genAlgoChoice.getItems().addAll("DFS (Backtracker)");
            genAlgoChoice.getSelectionModel().select(0);
            genAlgoChoice.valueProperty().addListener((obs, o, n) -> genAlgo = "DFS");

            pathAlgoChoice = new ComboBox<>();
            pathAlgoChoice.getItems().addAll("BFS (Shortest Path)");
            pathAlgoChoice.getSelectionModel().select(0);
            pathAlgoChoice.valueProperty().addListener((obs, o, n) -> pathAlgo = "BFS");

            parent.paramElementsBox.getChildren().addAll(new Label("Generate:"), genAlgoChoice, new Label("Pathfind:"), pathAlgoChoice);
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(false);
            parent.paramElementsField.setManaged(false);
        }
        if (parent.paramRandomizeButton != null) parent.paramRandomizeButton.setOnAction(e -> onRandomize());
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApply());

        // Solutions panel header
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Path Preview");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            parent.solutionsContent.getChildren().add(new Label("Run to preview path on grid."));
        }

        // Controls
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        // Initial render
        gridView.setMaze(rows, cols, generator.getWalls());
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready to generate maze.");
    }

    // --- Parameters handlers ---
    private void onSizeChanged(int n) {
        rows = cols = Math.max(5, n);
        refreshAll();
    }

    private void onRandomize() { refreshAll(); }
    private void onApply() { refreshAll(); }

    private void refreshAll() {
        stopTimeline();
        solvingPhase = false;
        generator = new MazeGeneratorDFS(rows, cols);
        generator.setStepListener(this::onGenStep);
        solver = null;
        gridView.setMaze(rows, cols, generator.getWalls());
        gridView.clearOverlays();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready to generate maze.");
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
        if (!solvingPhase) {
            if (generator == null || generator.isDone()) { ensureSolver(); return; }
            pushHistory();
            generator.step();
        } else {
            if (solver == null || solver.isDone()) return;
            pushHistory();
            solver.step();
        }
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        Snapshot s = history.pop();
        this.solvingPhase = s.solving;
        if (!s.solving) {
            generator.restore(s.genState);
            gridView.setMaze(generator.getRows(), generator.getCols(), generator.getWalls());
        } else {
            // ensure solver exists
            if (solver == null) ensureSolver();
            solver.restore(s.solveState);
        }
        // Remove last step logs
        removeLastLogLines(s.logLines);
        updateVariablesPanel();
    }

    public void onReset() {
        stopTimeline();
        history.clear();
        solvingPhase = false;
        generator.reset();
        gridView.setMaze(rows, cols, generator.getWalls());
        gridView.clearOverlays();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready to generate maze.");
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
            if (!solvingPhase) {
                if (generator.isDone()) { ensureSolver(); return; }
                pushHistory();
                generator.step();
            } else {
                if (solver == null || solver.isDone()) { stopTimeline(); return; }
                pushHistory();
                solver.step();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void ensureSolver() {
        if (solver != null) return;
        solvingPhase = true;
        int sR = 0, sC = 0, gR = rows - 1, gC = cols - 1;
        solver = new PathSolverBFS(rows, cols, generator.getWalls(), sR, sC, gR, gC);
        solver.setStepListener(this::onSolveStep);
        if (parent != null) parent.stepDescription.setText("Maze generated. Starting BFS pathfinding...");
    }

    private void pushHistory() {
        Snapshot snap = new Snapshot();
        snap.solving = solvingPhase;
        snap.logLines = finalizeLogGroupForSnapshot();
        if (!solvingPhase) {
            snap.genState = generator.snapshot();
        } else {
            snap.solveState = solver.snapshot();
        }
        history.push(snap);
    }

    // --- Event handling ---
    private void onGenStep(MazeGeneratorDFS.StepType type, int r, int c, int nr, int nc) {
        beginLogGroup();
        switch (type) {
            case INIT:
                gridView.setMaze(rows, cols, generator.getWalls());
                gridView.clearOverlays();
                appendProgress("🚀 Start DFS maze generation at (0,0)");
                if (parent != null) parent.stepDescription.setText("Generating maze...");
                break;
            case CARVE:
                gridView.updateWalls(generator.getWalls());
                gridView.markVisited(r, c);
                gridView.highlightCurrent(nr, nc);
                appendProgress("🪓 Carve wall between (" + r + "," + c + ") and (" + nr + "," + nc + ")");
                break;
            case BACKTRACK:
                gridView.markVisited(r, c);
                appendProgress("↩ Backtrack from (" + r + "," + c + ")");
                break;
            case DONE:
                gridView.updateWalls(generator.getWalls());
                gridView.clearOverlays();
                appendProgress("✅ Maze generation complete");
                if (parent != null) parent.stepDescription.setText("Maze generated. Ready to solve.");
                break;
        }
        updateVariablesPanel();
        endLogGroup();
    }

    private void onSolveStep(PathSolverBFS.StepType type, int r, int c) {
        beginLogGroup();
        switch (type) {
            case INIT:
                gridView.clearOverlays();
                gridView.highlightCurrent(0, 0);
                appendProgress("🚀 Start BFS at (0,0)");
                if (parent != null) parent.stepDescription.setText("BFS exploring...");
                break;
            case VISIT:
                gridView.markVisited(r, c);
                appendProgress("👣 Visit (" + r + "," + c + ")");
                break;
            case FRONTIER:
                gridView.markFrontier(r, c);
                appendProgress("➕ Add to frontier (" + r + "," + c + ")");
                break;
            case FOUND:
                gridView.markFrontier(r, c);
                appendProgress("🎯 Goal found at (" + r + "," + c + ")");
                if (parent != null) parent.stepDescription.setText("Goal found. Reconstructing path...");
                break;
            case RECON_PATH:
                gridView.markPath(r, c);
                appendProgress("🟢 Path cell (" + r + "," + c + ")");
                break;
            case NO_PATH:
                appendProgress("❌ No path to goal");
                if (parent != null) parent.stepDescription.setText("No path.");
                break;
            case DONE:
                appendProgress("🏁 Pathfinding complete");
                if (parent != null) parent.stepDescription.setText("Done.");
                stopTimeline();
                break;
        }
        updateVariablesPanel();
        endLogGroup();
    }

    // --- Logging helpers ---
    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        appendProgress("Maze: " + rows + "x" + cols + ", generator=" + genAlgo + ", pathfinder=" + pathAlgo);
    }

    private void beginLogGroup() { countLogsForStep = true; currentStepLogLines = 0; }
    private void endLogGroup() {
        if (countLogsForStep) {
            // nothing else; number of lines stored in snapshot
            countLogsForStep = false;
        }
    }
    private int finalizeLogGroupForSnapshot() {
        if (countLogsForStep) {
            int lines = currentStepLogLines;
            countLogsForStep = false;
            currentStepLogLines = 0;
            return lines;
        }
        return 0;
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
        parent.variableList.getItems().addAll(
                "rows: " + rows,
                "cols: " + cols,
                "phase: " + (solvingPhase ? "SOLVE" : "GENERATE")
        );
        // Add queue size etc. if solving
        if (solvingPhase && solver != null) {
            int q = solver.getQueueSnapshot().size();
            parent.variableList.getItems().add("queue: " + q);
        }
    }
}
