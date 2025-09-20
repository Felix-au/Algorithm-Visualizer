package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.MazeGeneratorDFS;
import com.algorithmvisualizer.algorithm.MazeGeneratorPrim;
import com.algorithmvisualizer.algorithm.MazeGeneratorKruskal;
import com.algorithmvisualizer.algorithm.PathSolverBFS;
import com.algorithmvisualizer.algorithm.PathSolverDFS;
import com.algorithmvisualizer.algorithm.PathSolverDijkstra;
import com.algorithmvisualizer.algorithm.PathSolverAStar;
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

    // Engines (type selected at runtime)
    private Object generator; // one of: MazeGeneratorDFS, MazeGeneratorPrim, MazeGeneratorKruskal
    private Object solver;    // one of: PathSolverBFS, PathSolverDFS, PathSolverDijkstra, PathSolverAStar

    // Playback
    private Timeline timeline;
    private boolean isPlaying = false;

    // History
    private static class Snapshot {
        boolean solving; // false = generation, true = solving
        String genType;
        Object genState;
        String solverType;
        Object solveState;
        int logLines;
    }
    private final Deque<Snapshot> history = new ArrayDeque<>();

    // Logging
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Params
    private int rows = 15, cols = 15;
    private String genAlgo = "DFS"; // DFS, Prim, Kruskal
    private String pathAlgo = "BFS"; // BFS, DFS, Dijkstra, A*
    private int loopsPercent = 0; // extra openings percentage 0..50
    private int startR = 0, startC = 0, goalR = 14, goalC = 14;
    private boolean[][][] lastWalls; // after loops

    // Custom controls
    private ComboBox<String> genAlgoChoice;
    private ComboBox<String> pathAlgoChoice;
    private Slider loopsSlider;
    private Label loopsValueLabel;
    private ToggleButton pickStartBtn;
    private ToggleButton pickGoalBtn;

    private boolean solvingPhase = false; // false until generator DONE

    @FXML
    private void initialize() {
        gridView = new MazeGridRenderer();
        MazeGeneratorDFS g = new MazeGeneratorDFS(rows, cols);
        g.setStepListener(this::onGenStep);
        generator = g;
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
        // Enable picking start/goal by clicking on cells
        gridView.setCellClickListener((r, c) -> {
            if (pickStartBtn != null && pickStartBtn.isSelected()) {
                startR = r; startC = c;
            } else if (pickGoalBtn != null && pickGoalBtn.isSelected()) {
                goalR = r; goalC = c;
            }
            gridView.setStartGoal(startR, startC, goalR, goalC);
            renderCode();
        });

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
            genAlgoChoice.getItems().addAll("DFS (Backtracker)", "Prim", "Kruskal");
            genAlgoChoice.getSelectionModel().select(0);
            genAlgoChoice.valueProperty().addListener((obs, o, n) -> { genAlgo = n.startsWith("DFS") ? "DFS" : n; refreshAll(); });

            pathAlgoChoice = new ComboBox<>();
            pathAlgoChoice.getItems().addAll("BFS (Shortest Path)", "DFS", "Dijkstra", "A* (Manhattan)", "A* (Euclidean)");
            pathAlgoChoice.getSelectionModel().select(0);
            pathAlgoChoice.valueProperty().addListener((obs, o, n) -> { pathAlgo = n; renderCode(); });

            // Loops slider (extra openings)
            loopsSlider = new Slider(0, 50, loopsPercent);
            loopsSlider.setPrefWidth(120);
            loopsValueLabel = new Label(loopsPercent + "%");
            loopsSlider.valueProperty().addListener((obs, o, n) -> {
                loopsPercent = n.intValue();
                loopsValueLabel.setText(loopsPercent + "%");
                if (!solvingPhase) {
                    lastWalls = applyLoopsToWalls(getGeneratorWalls());
                    gridView.updateWalls(lastWalls);
                }
                renderCode();
            });

            // Start/Goal picking
            ToggleGroup pickGroup = new ToggleGroup();
            pickStartBtn = new ToggleButton("Pick Start"); pickStartBtn.setToggleGroup(pickGroup);
            pickGoalBtn = new ToggleButton("Pick Goal"); pickGoalBtn.setToggleGroup(pickGroup);

            parent.paramElementsBox.getChildren().addAll(
                    new Label("Generate:"), genAlgoChoice,
                    new Label("Pathfind:"), pathAlgoChoice,
                    new Label("Loops:"), loopsSlider, loopsValueLabel,
                    pickStartBtn, pickGoalBtn
            );
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
        gridView.setMaze(rows, cols, getGeneratorWalls());
        gridView.setStartGoal(startR, startC, goalR, goalC);
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready to generate maze.");
        renderCode();
    }

    // --- Parameters handlers ---
    private void onSizeChanged(int n) {
        rows = cols = Math.max(5, n);
        goalR = rows - 1; goalC = cols - 1;
        refreshAll();
    }

    private void onRandomize() { refreshAll(); }
    private void onApply() { refreshAll(); }

    private void refreshAll() {
        stopTimeline();
        solvingPhase = false;
        // pick generator
        switch (genAlgo) {
            case "Prim": {
                MazeGeneratorPrim g = new MazeGeneratorPrim(rows, cols);
                g.setStepListener((t, r, c, nr, nc) -> onGenStep(mapPrimStep(t), r, c, nr, nc));
                generator = g; break; }
            case "Kruskal": {
                MazeGeneratorKruskal g = new MazeGeneratorKruskal(rows, cols);
                g.setStepListener((t, r, c, nr, nc) -> onGenStep(mapKruskalStep(t), r, c, nr, nc));
                generator = g; break; }
            default: {
                MazeGeneratorDFS g = new MazeGeneratorDFS(rows, cols);
                g.setStepListener(this::onGenStep);
                generator = g; break; }
        }
        solver = null;
        gridView.setMaze(rows, cols, getGeneratorWalls());
        lastWalls = applyLoopsToWalls(getGeneratorWalls());
        gridView.updateWalls(lastWalls);
        gridView.clearOverlays();
        gridView.setStartGoal(startR, startC, goalR, goalC);
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready to generate maze.");
        renderCode();
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
            if (isGeneratorDone()) { ensureSolver(); return; }
            pushHistory();
            stepGenerator();
        } else {
            if (solver == null || isSolverDone()) return;
            pushHistory();
            stepSolver();
        }
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        Snapshot s = history.pop();
        this.solvingPhase = s.solving;
        if (!s.solving) {
            restoreGeneratorState(s.genType, s.genState);
            gridView.setMaze(getGeneratorRows(), getGeneratorCols(), getGeneratorWalls());
            lastWalls = applyLoopsToWalls(getGeneratorWalls());
            gridView.updateWalls(lastWalls);
        } else {
            // ensure solver exists
            if (solver == null) ensureSolver();
            restoreSolverState(s.solverType, s.solveState);
        }
        // Remove last step logs
        removeLastLogLines(s.logLines);
        updateVariablesPanel();
    }

    public void onReset() {
        stopTimeline();
        history.clear();
        solvingPhase = false;
        resetGenerator();
        gridView.setMaze(rows, cols, getGeneratorWalls());
        lastWalls = applyLoopsToWalls(getGeneratorWalls());
        gridView.updateWalls(lastWalls);
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
                if (isGeneratorDone()) { ensureSolver(); return; }
                pushHistory();
                stepGenerator();
            } else {
                if (solver == null || isSolverDone()) { stopTimeline(); return; }
                pushHistory();
                stepSolver();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void ensureSolver() {
        if (solver != null) return;
        solvingPhase = true;
        int sR = startR, sC = startC, gR = goalR, gC = goalC;
        boolean[][][] walls = (lastWalls != null) ? lastWalls : getGeneratorWalls();
        String which = normalizePathAlgo(pathAlgo);
        switch (which) {
            case "DFS": {
                PathSolverDFS ps = new PathSolverDFS(rows, cols, walls, sR, sC, gR, gC);
                ps.setStepListener((t, r, c) -> onSolveStep(t, r, c));
                solver = ps; break; }
            case "DIJKSTRA": {
                PathSolverDijkstra ps = new PathSolverDijkstra(rows, cols, walls, sR, sC, gR, gC);
                ps.setStepListener((t, r, c) -> onSolveStep(t, r, c));
                solver = ps; break; }
            case "ASTAR_EUCLIDEAN": {
                PathSolverAStar ps = new PathSolverAStar(rows, cols, walls, sR, sC, gR, gC, PathSolverAStar.Heuristic.EUCLIDEAN);
                ps.setStepListener((t, r, c) -> onSolveStep(t, r, c));
                solver = ps; break; }
            case "ASTAR_MANHATTAN": {
                PathSolverAStar ps = new PathSolverAStar(rows, cols, walls, sR, sC, gR, gC, PathSolverAStar.Heuristic.MANHATTAN);
                ps.setStepListener((t, r, c) -> onSolveStep(t, r, c));
                solver = ps; break; }
            default: {
                PathSolverBFS ps = new PathSolverBFS(rows, cols, walls, sR, sC, gR, gC);
                ps.setStepListener((t, r, c) -> onSolveStep(t, r, c));
                solver = ps; break; }
        }
        if (parent != null) parent.stepDescription.setText("Maze generated. Starting " + which + " pathfinding...");
    }

    private void pushHistory() {
        Snapshot snap = new Snapshot();
        snap.solving = solvingPhase;
        snap.logLines = finalizeLogGroupForSnapshot();
        if (!solvingPhase) {
            snap.genType = genAlgo;
            snap.genState = snapshotGenerator();
        } else {
            snap.solverType = normalizePathAlgo(pathAlgo);
            snap.solveState = snapshotSolver();
        }
        history.push(snap);
    }

    // --- Event handling ---
    private void onGenStep(MazeGeneratorDFS.StepType type, int r, int c, int nr, int nc) {
        beginLogGroup();
        switch (type) {
            case INIT:
                gridView.setMaze(rows, cols, getGeneratorWalls());
                gridView.clearOverlays();
                appendProgress("🚀 Start DFS maze generation at (0,0)");
                if (parent != null) parent.stepDescription.setText("Generating maze...");
                break;
            case CARVE:
                gridView.updateWalls(getGeneratorWalls());
                gridView.markVisited(r, c);
                gridView.highlightCurrent(nr, nc);
                appendProgress("🪓 Carve wall between (" + r + "," + c + ") and (" + nr + "," + nc + ")");
                break;
            case BACKTRACK:
                gridView.markVisited(r, c);
                appendProgress("↩ Backtrack from (" + r + "," + c + ")");
                break;
            case DONE:
                gridView.updateWalls(getGeneratorWalls());
                gridView.clearOverlays();
                appendProgress("✅ Maze generation complete");
                if (parent != null) parent.stepDescription.setText("Maze generated. Ready to solve.");
                lastWalls = applyLoopsToWalls(getGeneratorWalls());
                gridView.updateWalls(lastWalls);
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
                gridView.highlightCurrent(startR, startC);
                appendProgress("🚀 Start " + normalizePathAlgo(pathAlgo) + " at (" + startR + "," + startC + ")");
                if (parent != null) parent.stepDescription.setText(normalizePathAlgo(pathAlgo) + " exploring...");
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
        appendProgress("Maze: " + rows + "x" + cols + ", generator=" + genAlgo + ", pathfinder=" + pathAlgo + ", loops=" + loopsPercent + "% start=(" + startR + "," + startC + ") goal=(" + goalR + "," + goalC + ")");
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
                "phase: " + (solvingPhase ? "SOLVE" : "GENERATE"),
                "start: (" + startR + "," + startC + ")",
                "goal: (" + goalR + "," + goalC + ")",
                "loops%: " + loopsPercent
        );
        // Add queue size etc. if solving
        if (solvingPhase && solver != null) {
            try {
                java.lang.reflect.Method m = solver.getClass().getMethod("getQueueSnapshot");
                Object list = m.invoke(solver);
                if (list instanceof java.util.List) {
                    int q = ((java.util.List<?>) list).size();
                    parent.variableList.getItems().add("queue: " + q);
                }
            } catch (Exception ignore) { }
        }
    }

    // --- Helpers: generator reflection adapters ---
    private boolean[][][] getGeneratorWalls() {
        try { return (boolean[][][]) generator.getClass().getMethod("getWalls").invoke(generator); }
        catch (Exception e) { return new boolean[rows][cols][4]; }
    }
    private int getGeneratorRows() { try { return (int) generator.getClass().getMethod("getRows").invoke(generator); } catch (Exception e) { return rows; } }
    private int getGeneratorCols() { try { return (int) generator.getClass().getMethod("getCols").invoke(generator); } catch (Exception e) { return cols; } }
    private boolean isGeneratorDone() { try { return (boolean) generator.getClass().getMethod("isDone").invoke(generator); } catch (Exception e) { return true; } }
    private void stepGenerator() { try { generator.getClass().getMethod("step").invoke(generator); } catch (Exception ignore) {} }
    private void resetGenerator() { try { generator.getClass().getMethod("reset").invoke(generator); } catch (Exception ignore) {} }
    private Object snapshotGenerator() { try { return generator.getClass().getMethod("snapshot").invoke(generator); } catch (Exception e) { return null; } }
    private void restoreGeneratorState(String type, Object state) {
        if ("Prim".equals(type) && !(generator instanceof MazeGeneratorPrim)) {
            MazeGeneratorPrim g = new MazeGeneratorPrim(rows, cols);
            g.setStepListener((t, r, c, nr, nc) -> onGenStep(mapPrimStep(t), r, c, nr, nc));
            generator = g;
        } else if ("Kruskal".equals(type) && !(generator instanceof MazeGeneratorKruskal)) {
            MazeGeneratorKruskal g = new MazeGeneratorKruskal(rows, cols);
            g.setStepListener((t, r, c, nr, nc) -> onGenStep(mapKruskalStep(t), r, c, nr, nc));
            generator = g;
        } else if ("DFS".equals(type) && !(generator instanceof MazeGeneratorDFS)) {
            MazeGeneratorDFS g = new MazeGeneratorDFS(rows, cols);
            g.setStepListener(this::onGenStep);
            generator = g;
        }
        try { generator.getClass().getMethod("restore", state.getClass()).invoke(generator, state); } catch (Exception ignore) {}
    }
    private MazeGeneratorDFS.StepType mapPrimStep(MazeGeneratorPrim.StepType t) {
        switch (t) {
            case INIT: return MazeGeneratorDFS.StepType.INIT;
            case CARVE: return MazeGeneratorDFS.StepType.CARVE;
            case DONE: return MazeGeneratorDFS.StepType.DONE;
            default: return MazeGeneratorDFS.StepType.DONE;
        }
    }
    private MazeGeneratorDFS.StepType mapKruskalStep(MazeGeneratorKruskal.StepType t) {
        switch (t) {
            case INIT: return MazeGeneratorDFS.StepType.INIT;
            case CARVE: return MazeGeneratorDFS.StepType.CARVE;
            case DONE: return MazeGeneratorDFS.StepType.DONE;
            default: return MazeGeneratorDFS.StepType.DONE;
        }
    }

    // --- Helpers: solver reflection adapters ---
    private boolean isSolverDone() { try { return (boolean) solver.getClass().getMethod("isDone").invoke(solver); } catch (Exception e) { return true; } }
    private void stepSolver() { try { solver.getClass().getMethod("step").invoke(solver); } catch (Exception ignore) {} }
    private Object snapshotSolver() { try { return solver.getClass().getMethod("snapshot").invoke(solver); } catch (Exception e) { return null; } }
    private void restoreSolverState(String type, Object state) {
        if ("DFS".equals(type) && !(solver instanceof PathSolverDFS)) {
            PathSolverDFS ps = new PathSolverDFS(rows, cols, lastWalls != null ? lastWalls : getGeneratorWalls(), startR, startC, goalR, goalC);
            ps.setStepListener((t, r, c) -> onSolveStep(t, r, c)); solver = ps;
        } else if ("DIJKSTRA".equals(type) && !(solver instanceof PathSolverDijkstra)) {
            PathSolverDijkstra ps = new PathSolverDijkstra(rows, cols, lastWalls != null ? lastWalls : getGeneratorWalls(), startR, startC, goalR, goalC);
            ps.setStepListener((t, r, c) -> onSolveStep(t, r, c)); solver = ps;
        } else if ("ASTAR_EUCLIDEAN".equals(type)) {
            PathSolverAStar ps = new PathSolverAStar(rows, cols, lastWalls != null ? lastWalls : getGeneratorWalls(), startR, startC, goalR, goalC, PathSolverAStar.Heuristic.EUCLIDEAN);
            ps.setStepListener((t, r, c) -> onSolveStep(t, r, c)); solver = ps;
        } else if ("ASTAR_MANHATTAN".equals(type)) {
            PathSolverAStar ps = new PathSolverAStar(rows, cols, lastWalls != null ? lastWalls : getGeneratorWalls(), startR, startC, goalR, goalC, PathSolverAStar.Heuristic.MANHATTAN);
            ps.setStepListener((t, r, c) -> onSolveStep(t, r, c)); solver = ps;
        } else if ("BFS".equals(type) && !(solver instanceof PathSolverBFS)) {
            PathSolverBFS ps = new PathSolverBFS(rows, cols, lastWalls != null ? lastWalls : getGeneratorWalls(), startR, startC, goalR, goalC);
            ps.setStepListener((t, r, c) -> onSolveStep(t, r, c)); solver = ps;
        }
        try { solver.getClass().getMethod("restore", state.getClass()).invoke(solver, state); } catch (Exception ignore) {}
    }

    private String normalizePathAlgo(String s) {
        if (s == null) return "BFS";
        s = s.trim().toUpperCase();
        if (s.startsWith("BFS")) return "BFS";
        if (s.startsWith("DFS")) return "DFS";
        if (s.startsWith("DIJK")) return "DIJKSTRA";
        if (s.contains("EUCLIDEAN")) return "ASTAR_EUCLIDEAN";
        if (s.startsWith("A*")) return "ASTAR_MANHATTAN";
        return s;
    }

    private boolean[][][] applyLoopsToWalls(boolean[][][] walls) {
        if (walls == null) return null;
        int r = walls.length, c = walls[0].length;
        boolean[][][] out = new boolean[r][c][4];
        for (int i=0;i<r;i++) for (int j=0;j<c;j++) System.arraycopy(walls[i][j],0,out[i][j],0,4);
        if (loopsPercent <= 0) return out;
        List<int[]> cand = new ArrayList<>();
        for (int i=0;i<r;i++) for (int j=0;j<c;j++) {
            if (j+1 < c && out[i][j][1]) cand.add(new int[]{i,j,1});
            if (i+1 < r && out[i][j][2]) cand.add(new int[]{i,j,2});
        }
        Collections.shuffle(cand, new Random());
        int openings = Math.max(0, Math.min(cand.size(), (loopsPercent * r * c) / 200));
        for (int k=0; k<openings; k++) {
            int[] e = cand.get(k);
            int i0 = e[0], j0 = e[1], dir = e[2];
            int ni = i0 + (dir==2?1:0) + (dir==0?-1:0);
            int nj = j0 + (dir==1?1:0) + (dir==3?-1:0);
            if (ni<0||nj<0||ni>=r||nj>=c) continue;
            out[i0][j0][dir] = false;
            out[ni][nj][(dir+2)%4] = false;
        }
        return out;
    }

    // --- Code sample generation ---
    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        String which = normalizePathAlgo(pathAlgo);
        String heuristic = which.startsWith("ASTAR_") ? (which.endsWith("EUCLIDEAN") ? "EUCLIDEAN" : "MANHATTAN") : "-";
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\n\n");
        sb.append("public class MazeDemo {\n");
        sb.append("    static final int ROWS = ").append(rows).append(";\n");
        sb.append("    static final int COLS = ").append(cols).append(";\n");
        sb.append("    static final int START_R = ").append(startR).append(";\n");
        sb.append("    static final int START_C = ").append(startC).append(";\n");
        sb.append("    static final int GOAL_R = ").append(goalR).append(";\n");
        sb.append("    static final int GOAL_C = ").append(goalC).append(";\n");
        sb.append("    static final String GEN = \"").append(genAlgo).append("\";\n");
        sb.append("    static final String SOLVER = \"").append(which).append("\";\n");
        sb.append("    static final String HEURISTIC = \"").append(heuristic).append("\";\n");
        sb.append("    static final int LOOPS_PCT = ").append(loopsPercent).append(";\n\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        boolean[][][] walls = generateMaze(ROWS, COLS, GEN);\n");
        sb.append("        applyLoops(walls, LOOPS_PCT);\n");
        sb.append("        List<int[]> path = solve(walls, START_R, START_C, GOAL_R, GOAL_C, SOLVER, HEURISTIC);\n");
        sb.append("        System.out.println(\"Path length: \" + (path==null? -1 : path.size()));\n");
        sb.append("    }\n\n");
        sb.append("    static boolean[][][] generateMaze(int R, int C, String alg){\n");
        sb.append("        if (\"Prim\".equalsIgnoreCase(alg)) return genPrim(R,C);\n");
        sb.append("        if (\"Kruskal\".equalsIgnoreCase(alg)) return genKruskal(R,C);\n");
        sb.append("        return genDFS(R,C);\n");
        sb.append("    }\n");
        sb.append("    static boolean[][][] genDFS(int R,int C){ boolean[][][] w=new boolean[R][C][4]; for(int i=0;i<R;i++) for(int j=0;j<C;j++) Arrays.fill(w[i][j], true); boolean[][] vis=new boolean[R][C]; Deque<int[]> st=new ArrayDeque<>(); st.push(new int[]{0,0}); vis[0][0]=true; Random rnd=new Random(); int[] dr={-1,0,1,0}, dc={0,1,0,-1}; while(!st.isEmpty()){ int[] cur=st.peek(); int r=cur[0],c=cur[1]; List<Integer> dirs=Arrays.asList(0,1,2,3); Collections.shuffle(dirs,rnd); boolean moved=false; for(int d:dirs){ int nr=r+dr[d], nc=c+dc[d]; if(nr<0||nc<0||nr>=R||nc>=C||vis[nr][nc]) continue; w[r][c][d]=false; w[nr][nc][(d+2)%4]=false; vis[nr][nc]=true; st.push(new int[]{nr,nc}); moved=true; break; } if(!moved) st.pop(); } return w; }\n");
        sb.append("    static boolean[][][] genPrim(int R,int C){ boolean[][][] w=new boolean[R][C][4]; for(int i=0;i<R;i++) for(int j=0;j<C;j++) Arrays.fill(w[i][j], true); boolean[][] in=new boolean[R][C]; List<int[]> F=new ArrayList<>(); int[] dr={-1,0,1,0}, dc={0,1,0,-1}; Random rnd=new Random(); in[0][0]=true; for(int d=0;d<4;d++){int nr=0+dr[d],nc=0+dc[d]; if(nr>=0&&nc>=0&&nr<R&&nc<C) F.add(new int[]{0,0,d});} while(!F.isEmpty()){ int idx=rnd.nextInt(F.size()); int[] e=F.remove(idx); int r=e[0],c=e[1],d=e[2]; int nr=r+dr[d], nc=c+dc[d]; if(nr<0||nc<0||nr>=R||nc>=C) continue; if(!in[nr][nc]){ w[r][c][d]=false; w[nr][nc][(d+2)%4]=false; in[nr][nc]=true; for(int dd=0;dd<4;dd++){int nr2=nr+dr[dd],nc2=nc+dc[dd]; if(nr2>=0&&nc2>=0&&nr2<R&&nc2<C&&!in[nr2][nc2]) F.add(new int[]{nr,nc,dd});} } } return w; }\n");
        sb.append("    static boolean[][][] genKruskal(int R,int C){ boolean[][][] w=new boolean[R][C][4]; for(int i=0;i<R;i++) for(int j=0;j<C;j++) Arrays.fill(w[i][j], true); int N=R*C; int[] p=new int[N], rk=new int[N]; for(int i=0;i<N;i++){p[i]=i;rk[i]=0;} List<int[]> E=new ArrayList<>(); for(int r=0;r<R;r++) for(int c=0;c<C;c++){ if(c+1<C) E.add(new int[]{r,c,1}); if(r+1<R) E.add(new int[]{r,c,2}); } Collections.shuffle(E,new Random()); int[] dr={-1,0,1,0}, dc={0,1,0,-1}; for(int[] e:E){ int r=e[0],c=e[1],d=e[2]; int nr=r+dr[d], nc=c+dc[d]; int a=r*C+c, b=nr*C+nc; if(find(p,a)!=find(p,b)){ unite(p,rk,a,b); w[r][c][d]=false; w[nr][nc][(d+2)%4]=false; } } return w; }\n");
        sb.append("    static int find(int[] p,int x){ return p[x]==x?x:(p[x]=find(p,p[x])); }\n");
        sb.append("    static void unite(int[] p,int[] rk,int a,int b){ a=find(p,a); b=find(p,b); if(a==b) return; if(rk[a]<rk[b]) p[a]=b; else if(rk[a]>rk[b]) p[b]=a; else{ p[b]=a; rk[a]++; } }\n\n");
        sb.append("    static void applyLoops(boolean[][][] w,int pct){ if(pct<=0) return; int R=w.length,C=w[0].length; List<int[]> cand=new ArrayList<>(); for(int i=0;i<R;i++) for(int j=0;j<C;j++){ if(j+1<C && w[i][j][1]) cand.add(new int[]{i,j,1}); if(i+1<R && w[i][j][2]) cand.add(new int[]{i,j,2}); } Collections.shuffle(cand,new Random()); int openings=Math.min(cand.size(), (pct*R*C)/200); for(int k=0;k<openings;k++){ int[] e=cand.get(k); int i=e[0],j=e[1],d=e[2]; int ni=i+(d==2?1:0)+(d==0?-1:0), nj=j+(d==1?1:0)+(d==3?-1:0); w[i][j][d]=false; w[ni][nj][(d+2)%4]=false; } }\n\n");
        sb.append("    static List<int[]> solve(boolean[][][] w,int sr,int sc,int gr,int gc,String alg,String h){ alg=alg.toUpperCase(); if(alg.equals(\"DFS\")) return dfs(w,sr,sc,gr,gc); if(alg.equals(\"DIJKSTRA\")) return dijkstra(w,sr,sc,gr,gc); if(alg.startsWith(\"ASTAR\")) return astar(w,sr,sc,gr,gc,h); return bfs(w,sr,sc,gr,gc); }\n");
        sb.append("    static List<int[]> bfs(boolean[][][] w,int sr,int sc,int gr,int gc){ int R=w.length,C=w[0].length; boolean[][] vis=new boolean[R][C]; int[][] pr=new int[R][C], pc=new int[R][C]; for(int i=0;i<R;i++){ Arrays.fill(pr[i],-1); Arrays.fill(pc[i],-1);} Deque<int[]> q=new ArrayDeque<>(); q.add(new int[]{sr,sc}); int[] dr={-1,0,1,0}, dc={0,1,0,-1}; while(!q.isEmpty()){ int[] x=q.poll(); int r=x[0],c=x[1]; if(vis[r][c]) continue; vis[r][c]=true; if(r==gr&&c==gc) break; for(int d=0;d<4;d++){ if(w[r][c][d]) continue; int nr=r+dr[d], nc=c+dc[d]; if(nr<0||nc<0||nr>=R||nc>=C) continue; if(!vis[nr][nc] && pr[nr][nc]==-1){ pr[nr][nc]=r; pc[nr][nc]=c; q.add(new int[]{nr,nc}); } } } return buildPath(pr,pc,sr,sc,gr,gc); }\n");
        sb.append("    static List<int[]> dfs(boolean[][][] w,int sr,int sc,int gr,int gc){ int R=w.length,C=w[0].length; boolean[][] vis=new boolean[R][C]; int[][] pr=new int[R][C], pc=new int[R][C]; for(int i=0;i<R;i++){ Arrays.fill(pr[i],-1); Arrays.fill(pc[i],-1);} Deque<int[]> st=new ArrayDeque<>(); st.push(new int[]{sr,sc,0}); int[] dr={-1,0,1,0}, dc={0,1,0,-1}; while(!st.isEmpty()){ int[] top=st.peek(); int r=top[0],c=top[1]; if(!vis[r][c]){ vis[r][c]=true; if(r==gr&&c==gc) break; } if(top[2]>=4){ st.pop(); continue; } int d=top[2]++; if(w[r][c][d]) continue; int nr=r+dr[d], nc=c+dc[d]; if(nr<0||nc<0||nr>=R||nc>=C) continue; if(!vis[nr][nc] && pr[nr][nc]==-1){ pr[nr][nc]=r; pc[nr][nc]=c; st.push(new int[]{nr,nc,0}); } } return buildPath(pr,pc,sr,sc,gr,gc); }\n");
        sb.append("    static List<int[]> dijkstra(boolean[][][] w,int sr,int sc,int gr,int gc){ int R=w.length,C=w[0].length; boolean[][] vis=new boolean[R][C]; int[][] pr=new int[R][C], pc=new int[R][C]; double[][] dist=new double[R][C]; for(int i=0;i<R;i++){ Arrays.fill(pr[i],-1); Arrays.fill(pc[i],-1); Arrays.fill(dist[i],1e18);} PriorityQueue<double[]> pq=new PriorityQueue<>(Comparator.comparingDouble(a->a[2])); dist[sr][sc]=0; pq.add(new double[]{sr,sc,0}); int[] dr={-1,0,1,0}, dc={0,1,0,-1}; while(!pq.isEmpty()){ double[] cur=pq.poll(); int r=(int)cur[0], c=(int)cur[1]; if(vis[r][c]) continue; vis[r][c]=true; if(r==gr&&c==gc) break; for(int d=0;d<4;d++){ if(w[r][c][d]) continue; int nr=r+dr[d], nc=c+dc[d]; if(nr<0||nc<0||nr>=R||nc>=C) continue; double nd=dist[r][c]+1; if(nd<dist[nr][nc]){ dist[nr][nc]=nd; pr[nr][nc]=r; pc[nr][nc]=c; pq.add(new double[]{nr,nc,nd}); } } } return buildPath(pr,pc,sr,sc,gr,gc); }\n");
        sb.append("    static List<int[]> astar(boolean[][][] w,int sr,int sc,int gr,int gc,String h){ int R=w.length,C=w[0].length; boolean[][] vis=new boolean[R][C]; int[][] pr=new int[R][C], pc=new int[R][C]; double[][] g=new double[R][C]; for(int i=0;i<R;i++){ Arrays.fill(pr[i],-1); Arrays.fill(pc[i],-1); Arrays.fill(g[i],1e18);} Comparator<double[]> cmp=Comparator.comparingDouble(a->a[2]); PriorityQueue<double[]> open=new PriorityQueue<>(cmp); g[sr][sc]=0; open.add(new double[]{sr,sc,heur(sr,sc,gr,gc,h)}); int[] dr={-1,0,1,0}, dc={0,1,0,-1}; while(!open.isEmpty()){ double[] cur=open.poll(); int r=(int)cur[0], c=(int)cur[1]; if(vis[r][c]) continue; vis[r][c]=true; if(r==gr&&c==gc) break; for(int d=0;d<4;d++){ if(w[r][c][d]) continue; int nr=r+dr[d], nc=c+dc[d]; if(nr<0||nc<0||nr>=R||nc>=C) continue; double tg=g[r][c]+1; if(tg<g[nr][nc]){ g[nr][nc]=tg; pr[nr][nc]=r; pc[nr][nc]=c; open.add(new double[]{nr,nc, tg+heur(nr,nc,gr,gc,h)}); } } } return buildPath(pr,pc,sr,sc,gr,gc); }\n");
        sb.append("    static double heur(int r,int c,int gr,int gc,String h){ if(\"EUCLIDEAN\".equalsIgnoreCase(h)){ double dr=r-gr, dc=c-gc; return Math.sqrt(dr*dr+dc*dc);} return Math.abs(r-gr)+Math.abs(c-gc); }\n");
        sb.append("    static List<int[]> buildPath(int[][] pr,int[][] pc,int sr,int sc,int gr,int gc){ List<int[]> path=new ArrayList<>(); int r=gr,c=gc; if(pr[r][c]==-1 && !(r==sr&&c==sc)) return null; path.add(new int[]{r,c}); while(!(r==sr&&c==sc)){ int prr=pr[r][c], pcc=pc[r][c]; if(prr==-1) break; r=prr; c=pcc; path.add(new int[]{r,c}); } Collections.reverse(path); return path; }\n");
        sb.append("}\n");
        parent.codeArea.setText(sb.toString());
    }
}
