package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.MatrixChainMultiplicationSolver;
import com.algorithmvisualizer.algorithm.MatrixChainMultiplicationSolver.StepType;
import com.algorithmvisualizer.code.implementations.MatrixChainMultiplicationCode;
import com.algorithmvisualizer.model.Algorithm;
import com.algorithmvisualizer.visualization.MatrixChainTableRenderer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

/**
 * Controller for Matrix Chain Multiplication visualization.
 * Implements AlgorithmSpecificController — all UI built programmatically (no @FXML canvas).
 */
public class MatrixChainMultiplicationController
        implements AlgorithmViewController.AlgorithmSpecificController {

    // ── Parent ──────────────────────────────────────────────────────────
    private AlgorithmViewController parent;

    // ── Algorithm components ────────────────────────────────────────────
    private MatrixChainMultiplicationSolver solver;
    private MatrixChainTableRenderer renderer;
    private MatrixChainMultiplicationCode codeImpl;

    // ── Canvas ──────────────────────────────────────────────────────────
    private Canvas canvas;

    // ── Parameters ──────────────────────────────────────────────────────
    private int numMatrices = 4;
    private int maxDimension = 30;
    private int[] dimensions; // p[0..n]

    // ── Playback state ──────────────────────────────────────────────────
    private Timeline timeline;
    private long stepDelayUntilMs = 0;
    private Stack<MatrixChainMultiplicationSolver.State> history;

    // ── Solution UI ─────────────────────────────────────────────────────
    private Label parenthesizationLabel;
    private Label statusLabel;
    private Label costFormulaLabel;
    private VBox stepInfoBox;
    private Canvas splitCanvas;

    // ── Colors ──────────────────────────────────────────────────────────
    private static final Color COLOR_CURRENT = Color.rgb(255, 235, 59);
    private static final Color COLOR_DEPENDENCY = Color.rgb(128, 222, 234);
    private static final Color COLOR_TRYING = Color.rgb(255, 183, 77);
    private static final Color COLOR_NEW_MIN = Color.rgb(129, 199, 132);
    private static final Color COLOR_CELL_COMPLETE = Color.rgb(76, 175, 80);
    private static final Color COLOR_TRACEBACK = Color.rgb(186, 104, 200);
    private static final Color COLOR_DIM_ACTIVE = Color.rgb(255, 152, 0);

    // ================================================================
    // Initialization
    // ================================================================

    @FXML
    private void initialize() {
        dimensions = generateRandomDimensions();
        canvas = new Canvas(700, 500);
        renderer = new MatrixChainTableRenderer(canvas);
        solver = new MatrixChainMultiplicationSolver();
        solver.setDimensions(dimensions);
        solver.setStepListener(this::onStepEvent);
        history = new Stack<>();
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Register code immediately
        codeImpl = new MatrixChainMultiplicationCode();

        // Place canvas in center
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(canvas);
        }

        // Hide chessboard-specific headers
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Matrix Chain Multiplication");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().add(hdr);
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(12.0);
            row.getChildren().addAll(
                legendItem(COLOR_CURRENT, "Current"),
                legendItem(COLOR_DEPENDENCY, "Dependency"),
                legendItem(COLOR_TRYING, "Trying Split"),
                legendItem(COLOR_NEW_MIN, "New Min"),
                legendItem(COLOR_CELL_COMPLETE, "Complete"),
                legendItem(COLOR_TRACEBACK, "Traceback")
            );
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Setup parameter controls
        setupParameterControls();
        buildSolutionsPane();

        // Pause button: start hidden
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }
        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        }

        // Initialize
        parent.setCurrentAlgorithmName("Matrix Chain Multiplication");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderVisualization();
        if (parent.stepDescription != null) {
            parent.stepDescription.setText("Ready. Press Play or Step Forward to begin.");
        }
    }

    private HBox legendItem(Color color, String label) {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(16, 16, color);
        rect.setStroke(Color.rgb(100, 100, 100));
        rect.setStrokeWidth(0.5);
        return new HBox(4.0, rect, new Label(label));
    }

    // ================================================================
    // Parameter Controls
    // ================================================================

    private void setupParameterControls() {
        if (parent == null) return;

        // Hide default parameter fields that don't apply
        if (parent.paramSizeLabel != null) { parent.paramSizeLabel.setVisible(false); parent.paramSizeLabel.setManaged(false); }
        if (parent.paramBoardSizeSpinner != null) { parent.paramBoardSizeSpinner.setVisible(false); parent.paramBoardSizeSpinner.setManaged(false); }
        if (parent.paramTargetLabel != null) { parent.paramTargetLabel.setVisible(false); parent.paramTargetLabel.setManaged(false); }
        if (parent.paramNumQueensSpinner != null) { parent.paramNumQueensSpinner.setVisible(false); parent.paramNumQueensSpinner.setManaged(false); }
        if (parent.paramElementsBox != null) { parent.paramElementsBox.setVisible(false); parent.paramElementsBox.setManaged(false); }

        // Repurpose existing fields
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("MCM Parameters:");
        }

        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("e.g. 30 35 15 5 10 20 25");
            parent.paramElementsField.setText(formatDimensions(dimensions));
        }

        // Add custom controls after the existing elements field
        if (parent.paramElementsField != null && parent.paramElementsField.getParent() instanceof VBox) {
            VBox elementsVBox = (VBox) parent.paramElementsField.getParent();
            int idx = elementsVBox.getChildren().indexOf(parent.paramElementsField);
            if (idx >= 0) {
                Label hint = new Label("Enter dimensions p[0]..p[n] separated by spaces");
                hint.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
                hint.setWrapText(true);

                Label dimInfo = new Label(numMatrices + " matrices, dims: " + Arrays.toString(dimensions));
                dimInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
                dimInfo.setWrapText(true);

                elementsVBox.getChildren().add(idx + 1, hint);
                elementsVBox.getChildren().add(idx + 2, dimInfo);
            }
        }

        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setText("Random Dims");
            parent.paramRandomizeButton.setOnAction(e -> onRandomize());
            parent.paramRandomizeButton.setStyle(
                "-fx-background-color: #0ea5e9; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;"
            );
        }

        if (parent.paramApplyButton != null) {
            parent.paramApplyButton.setOnAction(e -> onApply());
            parent.paramApplyButton.setStyle(
                "-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;"
            );
        }
    }

    private void buildSolutionsPane() {
        if (parent == null) return;

        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label header = new Label("MCM Details");
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().add(header);
        }

        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(true);
            parent.solutionsSubHeaderBox.setManaged(true);
            parent.solutionsSubHeaderBox.getChildren().clear();
            Label sub = new Label("m[i][j] = min{ m[i][k] + m[k+1][j] + p[i-1]·p[k]·p[j] }");
            sub.setStyle("-fx-font-size: 10px; -fx-font-family: Consolas;");
            sub.setWrapText(true);
            parent.solutionsSubHeaderBox.getChildren().add(sub);
        }

        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();

            VBox container = new VBox(12);
            container.setPrefWidth(Double.MAX_VALUE);

            // Status block
            VBox statusBlock = new VBox(6);
            statusBlock.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;");
            statusLabel = new Label("Ready. Press Play to begin.");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
            statusLabel.setWrapText(true);
            statusBlock.getChildren().add(statusLabel);

            // Step info block
            VBox stepBlock = new VBox(6);
            stepBlock.setStyle("-fx-background-color: #fff3cd; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #ffc107; -fx-border-width: 2; -fx-border-radius: 8;");
            Label stepHeader = new Label("⚡ Current Step");
            stepHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #856404;");
            stepInfoBox = new VBox(4);
            stepInfoBox.setStyle("-fx-background-color: white; -fx-padding: 8; -fx-background-radius: 5;");
            Label placeholder = new Label("Waiting to start...");
            placeholder.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            stepInfoBox.getChildren().add(placeholder);
            stepBlock.getChildren().addAll(stepHeader, stepInfoBox);

            // Cost formula block
            VBox formulaBlock = new VBox(4);
            formulaBlock.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 8;");
            costFormulaLabel = new Label("");
            costFormulaLabel.setStyle("-fx-font-size: 11px; -fx-font-family: Consolas; -fx-text-fill: #1565C0;");
            costFormulaLabel.setWrapText(true);
            formulaBlock.getChildren().add(costFormulaLabel);

            // Parenthesization block
            VBox parenBlock = new VBox(4);
            parenBlock.setStyle("-fx-background-color: #d4edda; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #28a745; -fx-border-width: 2; -fx-border-radius: 8;");
            Label parenHeader = new Label("🎯 Optimal Parenthesization");
            parenHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #155724;");
            parenthesizationLabel = new Label("(not computed yet)");
            parenthesizationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #4A148C;");
            parenthesizationLabel.setWrapText(true);
            parenBlock.getChildren().addAll(parenHeader, parenthesizationLabel);

            // Split table
            VBox splitBlock = new VBox(4);
            splitBlock.setStyle("-fx-background-color: #f3e5f5; -fx-padding: 10; -fx-background-radius: 8;");
            Label splitTitle = new Label("Split Table s[i][j]:");
            splitTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            splitCanvas = new Canvas(200, 200);
            splitBlock.getChildren().addAll(splitTitle, splitCanvas);

            container.getChildren().addAll(statusBlock, stepBlock, formulaBlock, parenBlock, splitBlock);
            parent.solutionsContent.getChildren().add(container);
        }
    }

    // ================================================================
    // Parameter actions
    // ================================================================

    private void onRandomize() {
        dimensions = generateRandomDimensions();
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setText(formatDimensions(dimensions));
        }
        numMatrices = dimensions.length - 1;
        refreshAll();
    }

    private void onApply() {
        if (parent.paramElementsField != null) {
            String text = parent.paramElementsField.getText().trim();
            if (!text.isEmpty()) {
                try {
                    String[] parts = text.split("[\\s,]+");
                    if (parts.length >= 3) {
                        int[] newDims = new int[parts.length];
                        for (int i = 0; i < parts.length; i++) {
                            newDims[i] = Integer.parseInt(parts[i]);
                            if (newDims[i] <= 0) throw new NumberFormatException("Must be positive");
                        }
                        dimensions = newDims;
                        numMatrices = dimensions.length - 1;
                    }
                } catch (NumberFormatException ex) {
                    // Keep existing dimensions on parse error
                }
            }
        }
        refreshAll();
    }

    private void refreshAll() {
        stopTimeline();
        history.clear();

        solver = new MatrixChainMultiplicationSolver();
        solver.setDimensions(dimensions);
        solver.setStepListener(this::onStepEvent);

        renderer.setData(numMatrices, dimensions);
        renderer.setMTable(solver.getM());
        renderer.setSTable(solver.getS());

        buildSolutionsPane();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderVisualization();
        updateDimInfo();
    }

    // ================================================================
    // Playback Controls
    // ================================================================

    @Override
    public void onPlay() {
        if (solver.isDone()) {
            onReset();
        }
        rebuildTimelineWithCurrentSpeed();
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(true);
            parent.pauseButton.setManaged(true);
            parent.pauseButton.setDisable(false);
        }
        if (parent.playButton != null) {
            parent.playButton.setVisible(false);
            parent.playButton.setManaged(false);
        }
    }

    @Override
    public void onPause() {
        stopTimeline();
    }

    @Override
    public void onStepForward() {
        if (!solver.isDone()) {
            history.push(solver.snapshot());
            solver.step();
        }
    }

    @Override
    public void onStepBack() {
        if (!history.isEmpty()) {
            MatrixChainMultiplicationSolver.State prevState = history.pop();
            solver.restore(prevState);
            renderer.clearHighlights();
            renderer.setMTable(solver.getM());
            renderer.setSTable(solver.getS());
            renderVisualization();
            updateVariablesPanel();
        }
    }

    @Override
    public void onReset() {
        stopTimeline();
        history.clear();
        solver.reset();
        renderer.clearHighlights();
        renderer.setData(numMatrices, dimensions);
        renderer.setMTable(solver.getM());
        renderer.setSTable(solver.getS());
        renderVisualization();
        initProgressLog();
        updateVariablesPanel();
        buildSolutionsPane();
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        if (parent.playButton != null) {
            parent.playButton.setVisible(true);
            parent.playButton.setManaged(true);
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }
    }

    private void rebuildTimelineWithCurrentSpeed() {
        stopTimeline();
        double speed = parent.speedSlider != null ? parent.speedSlider.getValue() : 1.0;
        double fps = Math.max(1.0, speed * 10);
        Duration frame = Duration.millis(1000.0 / fps);

        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (solver.isDone()) {
                stopTimeline();
                return;
            }
            long now = System.currentTimeMillis();
            if (now < stepDelayUntilMs) return;

            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updatePlaybackSpeed() {
        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
            rebuildTimelineWithCurrentSpeed();
        }
    }

    // ================================================================
    // Step event handler
    // ================================================================

    private void onStepEvent(StepType type, int i, int j, Map<String, Object> meta) {
        renderer.clearHighlights();
        renderer.setMTable(solver.getM());
        renderer.setSTable(solver.getS());

        switch (type) {
            case INIT:
                appendProgress("🚀 Matrix Chain Multiplication");
                appendProgress("   Matrices: " + numMatrices + ", Dimensions: " + Arrays.toString(dimensions));
                appendProgress("   Finding optimal parenthesization...\n");
                updateStepInfo("Initializing", "Setting up DP table");
                statusLabel.setText("Initialized. " + numMatrices + " matrices.");
                delay(800);
                break;

            case SET_CHAIN_LENGTH: {
                int l = (int) meta.get("chainLength");
                appendProgress("📏 Chain length l = " + l);
                updateStepInfo("Chain Length " + l, "Computing all subproblems of length " + l);
                statusLabel.setText("Chain length: " + l);
                delay(400);
                break;
            }

            case SELECT_SUBPROBLEM:
                renderer.highlightCell(i, j, COLOR_CURRENT);
                appendProgress("  🔍 Computing m[" + i + "][" + j + "]  (A" + i + "..A" + j + ")");
                updateStepInfo("Subproblem m[" + i + "][" + j + "]",
                    "Finding min cost to multiply A" + i + "..A" + j);
                statusLabel.setText("Subproblem: m[" + i + "][" + j + "]");
                delay(400);
                break;

            case TRY_SPLIT: {
                int k = (int) meta.get("k");
                renderer.highlightCell(i, j, COLOR_CURRENT);
                if (i != k) renderer.highlightCell(i, k, COLOR_DEPENDENCY);
                if (k + 1 != j) renderer.highlightCell(k + 1, j, COLOR_DEPENDENCY);
                renderer.highlightDimension(i - 1, COLOR_DIM_ACTIVE);
                renderer.highlightDimension(k, COLOR_DIM_ACTIVE);
                renderer.highlightDimension(j, COLOR_DIM_ACTIVE);

                appendProgress("    ✂️ Try split k = " + k);
                updateStepInfo("Split k=" + k,
                    "m[" + i + "][" + k + "] + m[" + (k+1) + "][" + j + "] + p" + (i-1) + "·p" + k + "·p" + j);
                statusLabel.setText("Trying split k = " + k + " for m[" + i + "][" + j + "]");
                delay(400);
                break;
            }

            case SHOW_COST_CALCULATION: {
                int k = (int) meta.get("k");
                int mik = (int) meta.get("mik");
                int mkj = (int) meta.get("mkj");
                int pi1 = (int) meta.get("pi1");
                int pk = (int) meta.get("pk");
                int pj = (int) meta.get("pj");
                int mulCost = (int) meta.get("multiplicationCost");
                int totalCost = (int) meta.get("totalCost");

                renderer.highlightCell(i, j, COLOR_CURRENT);
                if (i != k) renderer.highlightCell(i, k, COLOR_DEPENDENCY);
                if (k + 1 != j) renderer.highlightCell(k + 1, j, COLOR_DEPENDENCY);
                renderer.highlightDimension(i - 1, COLOR_DIM_ACTIVE);
                renderer.highlightDimension(k, COLOR_DIM_ACTIVE);
                renderer.highlightDimension(j, COLOR_DIM_ACTIVE);

                String formula = mik + " + " + mkj + " + " + pi1 + "×" + pk + "×" + pj + " = " + totalCost;
                renderer.setFormulaText("cost = " + formula);
                costFormulaLabel.setText("cost = " + formula);

                appendProgress("       " + formula);
                updateStepInfo("Cost Calculation",
                    "m[" + i + "][" + k + "]=" + mik + " + m[" + (k+1) + "][" + j + "]=" + mkj +
                    "\n+ " + pi1 + "×" + pk + "×" + pj + " = " + mulCost +
                    "\nTotal: " + totalCost);
                delay(300);
                break;
            }

            case NEW_MINIMUM: {
                int newBest = (int) meta.get("newBest");
                int bestSplit = (int) meta.get("bestSplit");
                renderer.highlightCell(i, j, COLOR_NEW_MIN);

                appendProgress("       ✅ New minimum! Cost = " + newBest + " at k = " + bestSplit);
                updateStepInfo("New Minimum!",
                    "Best cost = " + newBest + " at split k=" + bestSplit);
                statusLabel.setText("New min: " + newBest + " at k=" + bestSplit);
                delay(500);
                break;
            }

            case SPLIT_NOT_BETTER: {
                int cost = (int) meta.get("cost");
                int best = (int) meta.get("currentBest");
                renderer.highlightCell(i, j, COLOR_TRYING);

                appendProgress("       ❌ Cost " + cost + " ≥ best " + best);
                updateStepInfo("Not Better", cost + " ≥ current best " + best);
                delay(200);
                break;
            }

            case CELL_COMPLETE: {
                int value = (int) meta.get("value");
                int splitPoint = (int) meta.get("splitPoint");
                renderer.highlightCell(i, j, COLOR_CELL_COMPLETE);
                renderer.setFormulaText("m[" + i + "][" + j + "] = " + value + "  (split at k=" + splitPoint + ")");

                appendProgress("  ✔️ m[" + i + "][" + j + "] = " + value + "  (split at k=" + splitPoint + ")\n");
                updateStepInfo("Cell Complete",
                    "m[" + i + "][" + j + "] = " + value +
                    "\nOptimal split: k=" + splitPoint);
                statusLabel.setText("m[" + i + "][" + j + "] = " + value);
                delay(500);
                break;
            }

            case TABLE_COMPLETE: {
                int optCost = (int) meta.get("optimalCost");
                appendProgress("\n📊 Table complete! Optimal cost: " + optCost);
                updateStepInfo("Table Complete",
                    "Minimum multiplications: " + optCost +
                    "\nStarting traceback...");
                statusLabel.setText("Table complete! Min cost: " + optCost);
                delay(600);
                break;
            }

            case TRACEBACK_START: {
                String paren = (String) meta.get("parenthesization");
                appendProgress("\n🔄 Traceback: building optimal parenthesization...");
                parenthesizationLabel.setText(paren);
                renderer.highlightCell(1, solver.getN(), COLOR_TRACEBACK);
                updateStepInfo("Traceback Start",
                    "Following split points to build parenthesization");
                statusLabel.setText("Tracing back...");
                delay(400);
                break;
            }

            case TRACEBACK_STEP: {
                String paren = (String) meta.get("parenthesization");
                parenthesizationLabel.setText(paren);
                appendProgress("   📍 Following split points...");
                highlightSplitPath(1, solver.getN());
                updateStepInfo("Traceback", "Recursing through split table");
                delay(400);
                break;
            }

            case TRACEBACK_COMPLETE: {
                String paren = (String) meta.get("parenthesization");
                int optCost = (int) meta.get("optimalCost");
                appendProgress("   ✅ " + paren);
                parenthesizationLabel.setText(paren);
                updateStepInfo("Traceback Complete",
                    paren + "\nCost: " + optCost);
                statusLabel.setText("Done! Cost: " + optCost);
                delay(800);
                break;
            }

            case COMPLETE: {
                String paren = (String) meta.get("parenthesization");
                int optCost = (int) meta.get("optimalCost");
                appendProgress("\n🏁 Complete!");
                appendProgress("   Minimum multiplications: " + optCost);
                appendProgress("   Optimal order: " + paren);
                updateStepInfo("Complete! ✅",
                    "Min cost: " + optCost + "\n" + paren);
                statusLabel.setText("✅ Complete! Cost: " + optCost);
                delay(1000);
                break;
            }
        }

        renderVisualization();
        updateVariablesPanel();
        renderSplitTable();
    }

    // ================================================================
    // Helpers
    // ================================================================

    private void renderVisualization() {
        renderer.render();
    }

    private void highlightSplitPath(int i, int j) {
        if (i >= j) return;
        renderer.highlightCell(i, j, COLOR_TRACEBACK);
        int k = solver.getS()[i][j];
        if (k > 0) {
            highlightSplitPath(i, k);
            highlightSplitPath(k + 1, j);
        }
    }

    private void renderSplitTable() {
        if (splitCanvas == null) return;
        int[][] s = solver.getS();
        int n = solver.getN();
        if (n <= 0) return;

        double cellSize = Math.min(32, 180.0 / n);
        double headerSize = 20;

        splitCanvas.setWidth(headerSize + n * cellSize + 8);
        splitCanvas.setHeight(headerSize + n * cellSize + 8);

        var gc = splitCanvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(250, 250, 250));
        gc.fillRect(0, 0, splitCanvas.getWidth(), splitCanvas.getHeight());

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        for (int ii = 1; ii <= n; ii++) {
            gc.setFill(Color.rgb(63, 81, 181));
            gc.fillText("" + ii, headerSize + (ii - 1) * cellSize + cellSize / 2, headerSize - 4);
            gc.fillText("" + ii, headerSize / 2, headerSize + (ii - 1) * cellSize + cellSize / 2 + 3);

            for (int jj = 1; jj <= n; jj++) {
                double x = headerSize + (jj - 1) * cellSize;
                double y = headerSize + (ii - 1) * cellSize;

                if (ii >= jj) {
                    gc.setFill(Color.rgb(238, 238, 238));
                } else if (s[ii][jj] > 0) {
                    gc.setFill(Color.rgb(225, 190, 231));
                } else {
                    gc.setFill(Color.rgb(245, 245, 245));
                }
                gc.fillRect(x, y, cellSize, cellSize);
                gc.setStroke(Color.rgb(200, 200, 200));
                gc.strokeRect(x, y, cellSize, cellSize);

                if (ii < jj && s[ii][jj] > 0) {
                    gc.setFill(Color.rgb(74, 20, 140));
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                    gc.fillText("" + s[ii][jj], x + cellSize / 2, y + cellSize / 2 + 3);
                }
            }
        }
    }

    private void delay(long ms) {
        stepDelayUntilMs = System.currentTimeMillis() + ms;
    }

    private void updateStepInfo(String title, String details) {
        if (stepInfoBox == null) return;
        stepInfoBox.getChildren().clear();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-font-size: 11px;");
        detailsLabel.setWrapText(true);
        stepInfoBox.getChildren().addAll(titleLabel, detailsLabel);
    }

    private void appendProgress(String message) {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.appendText(message + "\n");
        }
    }

    private void initProgressLog() {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.clear();
            parent.progressArea.appendText("=== Matrix Chain Multiplication ===\n");
            parent.progressArea.appendText("Ready to start. Click Play to begin.\n\n");
        }
    }

    private void updateVariablesPanel() {
        if (parent == null || parent.variableList == null) return;

        ObservableList<String> vars = FXCollections.observableArrayList();
        vars.add("Matrices: " + numMatrices);
        vars.add("Dimensions: " + Arrays.toString(dimensions));

        int[][] m = solver.getM();
        int n = solver.getN();
        if (n > 0 && m[1][n] > 0) {
            vars.add("Min Cost: " + m[1][n]);
        }

        String paren = solver.getParenthesization();
        if (paren != null && !paren.isEmpty()) {
            vars.add("Order: " + paren);
        }

        vars.add(solver.isDone() ? "Status: Complete ✅" : "Status: Running...");

        parent.variableList.setItems(vars);
    }

    private void renderCode() {
        if (parent == null || codeImpl == null) return;
        codeImpl.updateParameters(dimensions);
        parent.loadCodeForCurrentLanguage();
    }

    private void updateDimInfo() {
        // Update the dimension info label in parameter panel
        if (parent == null || parent.paramElementsField == null) return;
        if (parent.paramElementsField.getParent() instanceof VBox) {
            VBox pBox = (VBox) parent.paramElementsField.getParent();
            int idx = pBox.getChildren().indexOf(parent.paramElementsField);
            // dimInfo label is at idx+2 (hint is at idx+1)
            if (idx >= 0 && idx + 2 < pBox.getChildren().size() &&
                pBox.getChildren().get(idx + 2) instanceof Label) {
                ((Label) pBox.getChildren().get(idx + 2)).setText(
                    numMatrices + " matrices, dims: " + Arrays.toString(dimensions));
            }
        }
    }

    private int[] generateRandomDimensions() {
        Random rand = new Random();
        int[] dims = new int[numMatrices + 1];
        for (int i = 0; i <= numMatrices; i++) {
            dims[i] = rand.nextInt(Math.max(1, maxDimension - 4)) + 5;
        }
        return dims;
    }

    private String formatDimensions(int[] dims) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dims.length; i++) {
            sb.append(dims[i]);
            if (i < dims.length - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
