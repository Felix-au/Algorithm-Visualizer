package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.MazeGeneratorDFS;
import com.algorithmvisualizer.algorithm.MazeGeneratorPrim;
import com.algorithmvisualizer.algorithm.MazeGeneratorKruskal;
import com.algorithmvisualizer.algorithm.PathSolverBFS;
import com.algorithmvisualizer.algorithm.PathSolverDFS;
import com.algorithmvisualizer.visualization.MazeGridRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.*;

public class MazeController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;

    // Visuals
    private MazeGridRenderer gridView;

    // Engines (type selected at runtime)
    private Object generator; // one of: MazeGeneratorDFS, MazeGeneratorPrim, MazeGeneratorKruskal
    private Object solver;    // one of: PathSolverBFS, PathSolverDFS

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
    private String pathAlgo = "BFS"; // BFS, DFS
    private int startR = 0, startC = 0, goalR = 14, goalC = 14;
    // loops removed; use generator walls directly

    // Custom controls
    private ComboBox<String> genAlgoChoice;
    private ComboBox<String> pathAlgoChoice;
    // loops controls removed
    private ToggleButton pickStartBtn;
    private ToggleButton pickGoalBtn;

    private boolean solvingPhase = false; // false until generator DONE

    // Stats + thumbnails
    private Set<String> visitedCells = new HashSet<>();
    private int stepsTaken = 0;
    private List<int[]> pathCells = new ArrayList<>();
    private boolean pathFound = false;
    // Thumbnails removed per requirements; only summary is displayed in the right pane

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
            // Backtrack pulse
            javafx.scene.shape.Rectangle backRect = new javafx.scene.shape.Rectangle(12,12);
            backRect.setFill(javafx.scene.paint.Color.TRANSPARENT);
            backRect.setStroke(javafx.scene.paint.Color.CRIMSON);
            backRect.setStrokeWidth(2.0);
            javafx.scene.layout.HBox lBack = new javafx.scene.layout.HBox(5.0, backRect, new Label("Backtrack"));
            // Start/Goal
            javafx.scene.shape.Rectangle startRect = new javafx.scene.shape.Rectangle(12,12);
            startRect.setFill(javafx.scene.paint.Color.TRANSPARENT);
            startRect.setStroke(javafx.scene.paint.Color.CORNFLOWERBLUE);
            startRect.setStrokeWidth(2.0);
            javafx.scene.shape.Rectangle goalRect = new javafx.scene.shape.Rectangle(12,12);
            goalRect.setFill(javafx.scene.paint.Color.TRANSPARENT);
            goalRect.setStroke(javafx.scene.paint.Color.CRIMSON);
            goalRect.setStrokeWidth(2.0);
            javafx.scene.layout.HBox lStart = new javafx.scene.layout.HBox(5.0, startRect, new Label("Start"));
            javafx.scene.layout.HBox lGoal = new javafx.scene.layout.HBox(5.0, goalRect, new Label("Goal"));
            row.getChildren().addAll(lGen, lFr, lPath, lBack, lStart, lGoal);
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
            
            // Modern styled ComboBoxes (matching language selector style)
            genAlgoChoice = new ComboBox<>();
            genAlgoChoice.getItems().addAll("DFS", "Prim", "Kruskal");
            genAlgoChoice.setValue("DFS");
            genAlgoChoice.setPrefWidth(140);
            genAlgoChoice.setStyle("-fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12;");
            genAlgoChoice.valueProperty().addListener((obs, o, n) -> { 
                genAlgo = n; 
                renderCode();
                refreshAll(); 
            });

            pathAlgoChoice = new ComboBox<>();
            pathAlgoChoice.getItems().addAll("BFS", "DFS");
            pathAlgoChoice.setValue("BFS");
            pathAlgoChoice.setPrefWidth(140);
            pathAlgoChoice.setStyle("-fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 12;");
            pathAlgoChoice.valueProperty().addListener((obs, o, n) -> { 
                pathAlgo = n; 
                renderCode(); 
            });

            // Start/Goal picking
            ToggleGroup pickGroup = new ToggleGroup();
            pickStartBtn = new ToggleButton("Pick Start"); 
            pickStartBtn.setToggleGroup(pickGroup);
            pickStartBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
            pickGoalBtn = new ToggleButton("Pick Goal"); 
            pickGoalBtn.setToggleGroup(pickGroup);
            pickGoalBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");

            // Row 1: algorithm choices
            HBox algoRow = new HBox(8.0);
            Label genLabel = new Label("Generate:");
            genLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            Label pathLabel = new Label("Pathfind:");
            pathLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            algoRow.getChildren().addAll(genLabel, genAlgoChoice, pathLabel, pathAlgoChoice);

            // Row 2: pick buttons + Apply in same row
            HBox controlsRow = new HBox(8.0);
            Button applyInline = new Button("Apply");
            applyInline.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
            applyInline.setOnAction(e -> onApply());
            controlsRow.getChildren().addAll(pickStartBtn, pickGoalBtn, applyInline);

            parent.paramElementsBox.getChildren().addAll(algoRow, controlsRow);
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(false);
            parent.paramElementsField.setManaged(false);
        }
        if (parent.paramRandomizeButton != null) { parent.paramRandomizeButton.setVisible(false); parent.paramRandomizeButton.setManaged(false); }
        if (parent.paramApplyButton != null) { parent.paramApplyButton.setVisible(false); parent.paramApplyButton.setManaged(false); }

        // Solutions panel header
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Path Summary");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            parent.solutionsContent.getChildren().add(new Label("Run to see path summary."));
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
        
        // Language selector listener - update code when language changes
        if (parent.languageSelector != null) {
            parent.languageSelector.valueProperty().addListener((obs, oldLang, newLang) -> {
                if (newLang != null && !newLang.equals(oldLang)) {
                    renderCode(); // Reload code in new language
                }
            });
        }
        
        parent.setCurrentAlgorithmName("Maze Pathfinding");
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
        double speed = parent != null ? parent.speedSlider.getValue() : 1.0;
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
        boolean[][][] walls = getGeneratorWalls();
        String which = normalizePathAlgo(pathAlgo);
        switch (which) {
            case "DFS": {
                PathSolverDFS ps = new PathSolverDFS(rows, cols, walls, sR, sC, gR, gC);
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
                gridView.pulseBacktrack(r, c);
                appendProgress("↩ Backtrack from (" + r + "," + c + ")");
                break;
            case DONE:
                gridView.updateWalls(getGeneratorWalls());
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
                gridView.highlightCurrent(startR, startC);
                appendProgress("🚀 Start " + normalizePathAlgo(pathAlgo) + " at (" + startR + "," + startC + ")");
                if (parent != null) parent.stepDescription.setText(normalizePathAlgo(pathAlgo) + " exploring...");
                // reset stats
                visitedCells.clear(); stepsTaken = 0; pathCells.clear(); pathFound = false;
                updatePathPreview();
                break;
            case VISIT:
                gridView.markVisited(r, c);
                appendProgress("👣 Visit (" + r + "," + c + ")");
                visitedCells.add(r + "," + c);
                stepsTaken++;
                break;
            case FRONTIER:
                gridView.markFrontier(r, c);
                appendProgress("➕ Add to frontier (" + r + "," + c + ")");
                stepsTaken++;
                break;
            case FOUND:
                gridView.markFrontier(r, c);
                appendProgress("🎯 Goal found at (" + r + "," + c + ")");
                if (parent != null) parent.stepDescription.setText("Goal found. Reconstructing path...");
                pathFound = true;
                break;
            case RECON_PATH:
                gridView.markPath(r, c);
                appendProgress("🟢 Path cell (" + r + "," + c + ")");
                pathCells.add(new int[]{r, c});
                stepsTaken++;
                break;
            case BACKTRACK:
                gridView.pulseBacktrack(r, c);
                appendProgress("↩ Backtrack from (" + r + "," + c + ")");
                stepsTaken++;
                break;
            case NO_PATH:
                appendProgress("❌ No path to goal");
                if (parent != null) parent.stepDescription.setText("No path.");
                updatePathPreview();
                break;
            case DONE:
                appendProgress("🏁 Pathfinding complete");
                if (parent != null) parent.stepDescription.setText("Done.");
                stopTimeline();
                updatePathPreview();
                break;
        }
        updateVariablesPanel();
        endLogGroup();
    }

    // --- Logging helpers ---
    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        appendProgress("Maze: " + rows + "x" + cols + ", generator=" + genAlgo + ", pathfinder=" + pathAlgo + ", start=(" + startR + "," + startC + ") goal=(" + goalR + "," + goalC + ")");
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
                "goal: (" + goalR + "," + goalC + ")"
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

    // --- Path Summary (right pane) ---
    private void updatePathPreview() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        // Summary block
        VBox summary = new VBox(3.0);
        summary.getChildren().addAll(
                new Label("Path Summary"),
                new Label("Generator: " + genAlgo + " — " + describeGenerator(genAlgo)),
                new Label("Solver: " + normalizePathAlgo(pathAlgo) + " — " + describeSolver(normalizePathAlgo(pathAlgo))),
                new Label("Start: (" + startR + "," + startC + ")  Goal: (" + goalR + "," + goalC + ")"),
                new Label("Visited cells: " + visitedCells.size()),
                new Label("Steps: " + stepsTaken),
                new Label("Path length: " + (pathFound ? pathCells.size() : -1))
        );
        parent.solutionsContent.getChildren().add(summary);
    }

    private String describeGenerator(String g) {
        switch (g) {
            case "Prim": return "Randomized Prim’s: grows MST-like spanning tree from a seed, carving shortest frontier edges.";
            case "Kruskal": return "Randomized Kruskal’s: shuffles edges and joins disjoint sets, creating a spanning tree.";
            default: return "DFS Backtracker: depth-first carve with backtracking, producing long corridors and few junctions.";
        }
    }

    private String describeSolver(String s) {
        switch (s) {
            case "DFS": return "Explores depth-first with a stack, not guaranteed shortest but simple and backtracks visibly.";
            default: return "Breadth-First Search: explores in layers and guarantees a shortest path in unweighted grids.";
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
            PathSolverDFS ps = new PathSolverDFS(rows, cols, getGeneratorWalls(), startR, startC, goalR, goalC);
            ps.setStepListener((t, r, c) -> onSolveStep(t, r, c)); solver = ps;
        } else if ("BFS".equals(type) && !(solver instanceof PathSolverBFS)) {
            PathSolverBFS ps = new PathSolverBFS(rows, cols, getGeneratorWalls(), startR, startC, goalR, goalC);
            ps.setStepListener((t, r, c) -> onSolveStep(t, r, c)); solver = ps;
        }
        try { solver.getClass().getMethod("restore", state.getClass()).invoke(solver, state); } catch (Exception ignore) {}
    }

    private String normalizePathAlgo(String s) {
        if (s == null) return "BFS";
        s = s.trim().toUpperCase();
        if (s.startsWith("BFS")) return "BFS";
        if (s.startsWith("DFS")) return "DFS";
        return s;
    }

    // removed loops post-processing

    // --- Code sample generation ---
    private void renderCode() {
        if (parent == null || parent.codeArea == null || parent.languageSelector == null) return;
        
        // Get the code implementation
        com.algorithmvisualizer.code.AlgorithmCode code = com.algorithmvisualizer.code.CodeRepository.getCode("Maze Pathfinding");
        if (code instanceof com.algorithmvisualizer.code.implementations.MazePathfindingCode) {
            com.algorithmvisualizer.code.implementations.MazePathfindingCode mazeCode = 
                (com.algorithmvisualizer.code.implementations.MazePathfindingCode) code;
            mazeCode.updateParameters(rows, cols, startR, startC, goalR, goalC, genAlgo, pathAlgo);
        }
        
        // Load and display the code for the current language
        if (code != null) {
            String selectedLanguage = parent.languageSelector.getValue();
            if (selectedLanguage != null) {
                String codeText = code.getCodeForLanguage(selectedLanguage);
                if (codeText != null && !codeText.isEmpty()) {
                    parent.codeArea.replaceText(codeText);
                    // Apply syntax highlighting
                    javafx.application.Platform.runLater(() -> {
                        com.algorithmvisualizer.ui.CodeHighlighter.applyHighlighting(parent.codeArea, selectedLanguage);
                    });
                }
            }
        }
    }
}
