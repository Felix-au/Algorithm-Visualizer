package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.BoyerMooreSolver;
import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.code.implementations.BoyerMooreCode;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.*;

/**
 * Controller for Boyer-Moore string search visualization.
 *
 * Visualization: Text displayed as boxes on top, pattern sliding below.
 * Comparisons proceed right-to-left (true to Boyer-Moore).
 * Colors: YELLOW=comparing, RED=mismatch, GREEN=match, CYAN=skipped,
 * ORANGE=shift.
 *
 * CRITICAL: Follows the minimal FXML approach. Canvas is created
 * programmatically
 * in setParentController(), NOT defined in FXML.
 */
public class BoyerMooreController implements AlgorithmViewController.AlgorithmSpecificController {

    // ── Parent reference ────────────────────────────────────────────
    private AlgorithmViewController parent;

    // ── Visualization ───────────────────────────────────────────────
    private Canvas canvas;
    private static final double CELL_SIZE = 36;
    private static final double CELL_GAP = 2;
    private static final double TEXT_ROW_Y = 40;
    private static final double PATTERN_ROW_Y = 100;
    private static final double INDEX_ROW_Y = 20;
    private static final double X_OFFSET = 20;

    // ── Solver ──────────────────────────────────────────────────────
    private BoyerMooreSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;

    // ── Blocking flags ──────────────────────────────────────────────
    private boolean pendingPause = false;

    // ── History for step-back ────────────────────────────────────────
    private final Deque<BoyerMooreSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // ── Data ────────────────────────────────────────────────────────
    private String currentText = "ABAAABCDABDE";
    private String currentPattern = "ABCDABD";
    private BoyerMooreCode codeImpl;

    // ── Visual state tracking ───────────────────────────────────────
    private Map<Integer, Color> textCellColors = new HashMap<>();
    private Map<Integer, Color> patternCellColors = new HashMap<>();
    private int lastComparedTextIdx = -1;
    private int lastComparedPatIdx = -1;
    private Integer pendingShiftAmount = null;
    private Character currentBadChar = null;
    private Character currentPatternChar = null;
    private Integer currentLastOcc = null;

    // ── UI controls for parameters ──────────────────────────────────
    private TextField textField;
    private TextField patternField;

    @FXML
    private void initialize() {
        solver = new BoyerMooreSolver(currentText, currentPattern);
        solver.setStepListener(this::onStepEvent);
        codeImpl = new BoyerMooreCode();
        codeImpl.updateParameters(currentText, currentPattern);
    }

    // ═══════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // CRITICAL: Initialize state variables FIRST (pitfall #2)
        if (solver == null) {
            solver = new BoyerMooreSolver(currentText, currentPattern);
            solver.setStepListener(this::onStepEvent);
        }
        if (codeImpl == null) {
            codeImpl = new BoyerMooreCode();
            codeImpl.updateParameters(currentText, currentPattern);
        }

        // ── Build canvas programmatically (pitfall #1) ──────────
        double canvasWidth = Math.max(800, (currentText.length() + 2) * (CELL_SIZE + CELL_GAP) + 2 * X_OFFSET);
        canvas = new Canvas(canvasWidth, 200);
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            ScrollPane sp = new ScrollPane(new StackPane(canvas));
            sp.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");
            sp.setFitToHeight(true);
            parent.chessboardContainer.getChildren().add(sp);
        }

        // ── Header ──────────────────────────────────────────────
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Boyer-Moore String Search Visualization");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(hdr, new Separator());
        }

        // ── Legend ──────────────────────────────────────────────
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(12);
            row.getChildren().addAll(
                    legendItem(Color.GOLD, "Comparing"),
                    legendItem(Color.LIMEGREEN, "Match"),
                    legendItem(Color.TOMATO, "Mismatch"),
                    legendItem(Color.CYAN, "Skipped"),
                    legendItem(Color.ORANGE, "Shift"));
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // ── Parameters ──────────────────────────────────────────
        setupParameterControls();

        // ── Solutions pane: Bad Char Table ───────────────────────
        buildSolutionsPane();

        // ── Playback controls ───────────────────────────────────
        if (parent.speedSlider != null)
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        // ── Code + logs + variables ─────────────────────────────
        parent.setCurrentAlgorithmName("Boyer-Moore Search");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderCanvas();

        if (parent.stepDescription != null)
            parent.stepDescription.setText("Ready.");
    }

    // Note: AlgorithmSpecificController interface only defines
    // setParentController() and default playback methods (onPlay, etc.).
    // No initializeVisualization, handleParametersSet, resetVisualization,
    // or updateVisualization methods exist in the interface.

    // ═══════════════════════════════════════════════════════════════
    // PARAMETER CONTROLS
    // ═══════════════════════════════════════════════════════════════

    private void setupParameterControls() {
        if (parent == null)
            return;

        // Hide default spinner-based controls
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(false);
            parent.paramBoardSizeSpinner.setManaged(false);
        }
        if (parent.paramNumQueensSpinner != null) {
            parent.paramNumQueensSpinner.setVisible(false);
            parent.paramNumQueensSpinner.setManaged(false);
        }
        if (parent.paramTargetLabel != null) {
            parent.paramTargetLabel.setVisible(false);
            parent.paramTargetLabel.setManaged(false);
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(false);
            parent.paramElementsField.setManaged(false);
        }
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(false);
            parent.paramElementsLabel.setManaged(false);
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }

        // Size label → "Text:"
        if (parent.paramSizeLabel != null) {
            parent.paramSizeLabel.setText(" ");
        }

        // Use paramElementsBox for custom controls (since parameterControls doesn't
        // exist in new UI)
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(true);
            parent.paramElementsBox.setManaged(true);
            parent.paramElementsBox.getChildren().clear();

            VBox customParams = new VBox(8);
            customParams.setPadding(new Insets(5, 0, 0, 0));

            HBox textRow = new HBox(8);
            textRow.setAlignment(Pos.CENTER_LEFT);
            Label textLabel = new Label("Text:");
            textLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            textLabel.setPrefWidth(55);
            textField = new TextField(currentText);
            textField.setPrefWidth(180);
            textRow.getChildren().addAll(textLabel, textField);

            HBox pattRow = new HBox(8);
            pattRow.setAlignment(Pos.CENTER_LEFT);
            Label patLabel = new Label("Pattern:");
            patLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            patLabel.setPrefWidth(55);
            patternField = new TextField(currentPattern);
            patternField.setPrefWidth(180);
            pattRow.getChildren().addAll(patLabel, patternField);

            customParams.getChildren().addAll(textRow, pattRow);
            parent.paramElementsBox.getChildren().add(customParams);
        }

        if (parent.paramApplyButton != null)
            parent.paramApplyButton.setOnAction(e -> onApply());
        if (parent.paramRandomizeButton != null)
            parent.paramRandomizeButton.setOnAction(e -> onRandomize());
    }

    // ═══════════════════════════════════════════════════════════════
    // SOLUTIONS PANE (Bad Character Table)
    // ═══════════════════════════════════════════════════════════════

    private void buildSolutionsPane() {
        if (parent == null)
            return;

        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Bad Character Table");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }

        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(true);
            parent.solutionsSubHeaderBox.setManaged(true);
            parent.solutionsSubHeaderBox.getChildren().clear();
            Label subLabel = new Label("Last occurrence of each character in pattern");
            subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            parent.solutionsSubHeaderBox.getChildren().add(subLabel);
        }

        updateBadCharTable();
    }

    private void updateBadCharTable() {
        if (parent == null || parent.solutionsContent == null)
            return;
        parent.solutionsContent.getChildren().clear();
        parent.solutionsContent.setPadding(new Insets(10, 0, 10, 0));
        parent.solutionsContent.setAlignment(Pos.CENTER);

        // Build a readable bad char table showing only relevant characters
        int[] bct = solver.getBadCharTable();
        Set<Character> relevantChars = new LinkedHashSet<>();

        // Add all unique characters from text and pattern
        for (char c : currentText.toCharArray())
            relevantChars.add(c);
        for (char c : currentPattern.toCharArray())
            relevantChars.add(c);

        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);
        // Header
        Label chHeader = new Label("Char");
        chHeader.setStyle(
                "-fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #e0e0e0; -fx-background-radius: 3; -fx-text-fill: #333333;");
        chHeader.setMinWidth(50);
        chHeader.setAlignment(Pos.CENTER);
        grid.add(chHeader, 0, 0);

        Label idxHeader = new Label("Last Pos");
        idxHeader.setStyle(
                "-fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #e0e0e0; -fx-background-radius: 3; -fx-text-fill: #333333;");
        idxHeader.setMinWidth(60);
        idxHeader.setAlignment(Pos.CENTER);
        grid.add(idxHeader, 1, 0);

        int row = 1;
        for (char c : relevantChars) {
            int lastPos = bct[c];
            String bgColor = lastPos >= 0 ? "#e8f5e9" : "#ffebee";

            // Highlight current bad character (text mismatched char) in yellow
            if (currentBadChar != null && c == currentBadChar) {
                bgColor = "#ffff00";
            } else if (currentPatternChar != null && c == currentPatternChar) {
                // Highlight pattern character at mismatch position in light blue
                bgColor = "#add8e6";
            }

            Label charLabel = new Label("'" + c + "'");
            charLabel.setStyle("-fx-padding: 3 8; -fx-background-color: " + bgColor
                    + "; -fx-background-radius: 3; -fx-font-family: monospace; -fx-text-fill: #333333;");
            charLabel.setMinWidth(50);
            charLabel.setAlignment(Pos.CENTER);

            Label posLabel = new Label(String.valueOf(lastPos));
            posLabel.setStyle("-fx-padding: 3 8; -fx-background-color: " + bgColor
                    + "; -fx-background-radius: 3; -fx-font-family: monospace; -fx-text-fill: #333333;");
            posLabel.setMinWidth(60);
            posLabel.setAlignment(Pos.CENTER);

            grid.add(charLabel, 0, row);
            grid.add(posLabel, 1, row);
            row++;
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(scrollPane);
        
        if (currentBadChar != null && currentPatternChar != null && pendingShiftAmount != null && currentLastOcc != null) {
            VBox calcBox = new VBox(5);
            calcBox.setAlignment(Pos.CENTER);
            calcBox.setPadding(new Insets(5, 5, 5, 5));
            
            Label l1 = new Label("Pattern has '" + currentPatternChar + "' at pos " + lastComparedPatIdx);
            Label l2 = new Label("matched with '" + currentBadChar + "' (last occ = " + currentLastOcc + ")");
            l1.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333;");
            l2.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333;");
            
            Label l3 = new Label("shift = max(1, pIdx - lastOcc)\n      = max(1, " + lastComparedPatIdx + " - " + currentLastOcc + ")\n      = " + pendingShiftAmount);
            l3.setStyle("-fx-font-family: monospace; -fx-font-weight: bold; -fx-text-fill: #e65100; -fx-background-color: #fff3e0; -fx-padding: 8; -fx-background-radius: 4;");
            
            calcBox.getChildren().addAll(l1, l2, l3);
            container.getChildren().add(calcBox);
        }

        parent.solutionsContent.getChildren().add(container);
    }

    // ═══════════════════════════════════════════════════════════════
    // CANVAS RENDERING
    // ═══════════════════════════════════════════════════════════════

    private void renderCanvas() {
        if (canvas == null)
            return;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Resize canvas if needed
        double neededWidth = Math.max(800, (currentText.length() + 2) * (CELL_SIZE + CELL_GAP) + 2 * X_OFFSET);
        if (canvas.getWidth() < neededWidth) {
            canvas.setWidth(neededWidth);
        }

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int alignment = solver != null ? solver.getAlignment() : 0;

        // ── Draw index numbers ──────────────────────────────────
        gc.setFont(Font.font("Monospace", 10));
        gc.setFill(Color.GRAY);
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < currentText.length(); i++) {
            double x = X_OFFSET + i * (CELL_SIZE + CELL_GAP);
            gc.fillText(String.valueOf(i), x + CELL_SIZE / 2, INDEX_ROW_Y);
        }

        // ── Draw text row ───────────────────────────────────────
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        for (int i = 0; i < currentText.length(); i++) {
            double x = X_OFFSET + i * (CELL_SIZE + CELL_GAP);
            Color bgColor = textCellColors.getOrDefault(i, Color.web("#f0f0f0"));
            drawCell(gc, x, TEXT_ROW_Y, String.valueOf(currentText.charAt(i)), bgColor, Color.BLACK);
        }

        // ── Label rows ──────────────────────────────────────────
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#333"));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(" ", X_OFFSET - 5, TEXT_ROW_Y + CELL_SIZE / 2 + 5);
        gc.fillText(" ", X_OFFSET - 5, PATTERN_ROW_Y + CELL_SIZE / 2 + 5);

        // ── Draw pattern row (shifted by alignment) ─────────────
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int j = 0; j < currentPattern.length(); j++) {
            double x = X_OFFSET + (alignment + j) * (CELL_SIZE + CELL_GAP);
            Color bgColor = patternCellColors.getOrDefault(j, Color.web("#dde4ff"));
            drawCell(gc, x, PATTERN_ROW_Y, String.valueOf(currentPattern.charAt(j)), bgColor, Color.BLACK);
        }

        // ── Draw ghost pattern and shift arrow if pending shift ─
        if (pendingShiftAmount != null) {
            gc.setGlobalAlpha(0.3); // Transparent
            for (int j = 0; j < currentPattern.length(); j++) {
                double x = X_OFFSET + (alignment + pendingShiftAmount + j) * (CELL_SIZE + CELL_GAP);
                drawCell(gc, x, PATTERN_ROW_Y, String.valueOf(currentPattern.charAt(j)), Color.web("#cccccc"),
                        Color.BLACK);
            }
            gc.setGlobalAlpha(1.0); // Reset alpha

            // Draw shift arrow
            if (pendingShiftAmount > 0) {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(2);

                // Start arrow near the end of the current pattern
                double startX = X_OFFSET + (alignment + currentPattern.length()) * (CELL_SIZE + CELL_GAP) + 10;
                // End arrow where the pattern will move
                double endX = startX + pendingShiftAmount * (CELL_SIZE + CELL_GAP) - 20;
                double arrowY = PATTERN_ROW_Y + CELL_SIZE / 2;

                if (endX > startX) {
                    // Main line
                    gc.strokeLine(startX, arrowY, endX, arrowY);
                    // Arrow head
                    gc.strokeLine(endX, arrowY, endX - 8, arrowY - 6);
                    gc.strokeLine(endX, arrowY, endX - 8, arrowY + 6);

                    // Shift text
                    gc.setFill(Color.ORANGE);
                    gc.setFont(Font.font("System", FontWeight.BOLD, 12));
                    gc.fillText("Shift by " + pendingShiftAmount, startX + (endX - startX) / 2, arrowY - 10);
                }
            }
        }

        // ── Draw alignment arrow ────────────────────────────────
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(2);
        double arrowX = X_OFFSET + alignment * (CELL_SIZE + CELL_GAP) + CELL_SIZE / 2;
        gc.strokeLine(arrowX, TEXT_ROW_Y + CELL_SIZE + 2, arrowX, PATTERN_ROW_Y - 2);

        // ── Status line ─────────────────────────────────────────
        gc.setFont(Font.font("System", 12));
        gc.setFill(Color.web("#555"));
        gc.setTextAlign(TextAlignment.LEFT);
        String status = solver != null ? solver.getCurrentStepDescription() : "";
        gc.fillText(status, X_OFFSET, PATTERN_ROW_Y + CELL_SIZE + 30);
    }

    private void drawCell(GraphicsContext gc, double x, double y, String text, Color bgColor, Color textColor) {
        // Background
        gc.setFill(bgColor);
        gc.fillRoundRect(x, y, CELL_SIZE, CELL_SIZE, 6, 6);

        // Border
        gc.setStroke(Color.web("#aaa"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, CELL_SIZE, CELL_SIZE, 6, 6);

        // Text
        gc.setFill(textColor);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, x + CELL_SIZE / 2, y + CELL_SIZE / 2 + 6);
    }

    private HBox legendItem(Color color, String label) {
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(12, 12);
        rect.setFill(color);
        rect.setStroke(Color.BLACK);
        return new HBox(4, rect, new Label(label));
    }

    // ═══════════════════════════════════════════════════════════════
    // PLAYBACK CONTROLS
    // ═══════════════════════════════════════════════════════════════

    public void onPlay() {
        if (isPlaying) {
            onPause();
            return;
        }
        isPlaying = true;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null)
            timeline.play();
        if (parent != null) {
            parent.playButton.setVisible(false);
            parent.playButton.setManaged(false);
            // Pitfall #3: manage pause button visibility
            if (parent.pauseButton != null) {
                parent.pauseButton.setVisible(true);
                parent.pauseButton.setManaged(true);
                parent.pauseButton.setDisable(false);
            }
        }
    }

    public void onPause() {
        stopTimeline();
    }

    public void onStepForward() {
        if (solver == null || solver.isDone())
            return;
        if (pendingPause)
            return;
        history.push(solver.snapshot());
        progressHistory.push(currentStepLogLines);
        currentStepLogLines = 0;
        countLogsForStep = true;
        solver.step();
        countLogsForStep = false;
    }

    public void onStepBack() {
        if (isPlaying)
            onPause();
        if (history.isEmpty())
            return;

        pendingPause = false;

        BoyerMooreSolver.State s = history.pop();
        solver.restore(s);

        // Remove logs
        if (!progressHistory.isEmpty()) {
            int toRemove = progressHistory.pop();
            removeLastLogLines(toRemove);
        }

        // Repaint
        clearCellColors();
        renderCanvas();
        updateVariablesPanel();
        if (parent != null && parent.stepDescription != null) {
            parent.stepDescription.setText(solver.getCurrentStepDescription());
        }
    }

    public void onReset() {
        stopTimeline();
        history.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        pendingPause = false;
        solver = new BoyerMooreSolver(currentText, currentPattern);
        solver.setStepListener(this::onStepEvent);
        clearCellColors();
        renderCanvas();
        buildSolutionsPane();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null && parent.stepDescription != null)
            parent.stepDescription.setText("Ready.");
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isPlaying = false;
        if (parent != null) {
            parent.playButton.setVisible(true);
            parent.playButton.setManaged(true);
            // Pitfall #3: hide pause button when stopped
            if (parent.pauseButton != null) {
                parent.pauseButton.setVisible(false);
                parent.pauseButton.setManaged(false);
            }
        }
    }

    private void updatePlaybackSpeed() {
        if (!isPlaying)
            return;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null)
            timeline.play();
    }

    private void rebuildTimelineWithCurrentSpeed() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        double speed = parent != null ? parent.speedSlider.getValue() : 1.0;
        double fps = Math.max(0.5, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (solver.isDone()) {
                stopTimeline();
                return;
            }
            if (pendingPause)
                return;
            history.push(solver.snapshot());
            progressHistory.push(currentStepLogLines);
            currentStepLogLines = 0;
            countLogsForStep = true;
            solver.step();
            countLogsForStep = false;
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    // ═══════════════════════════════════════════════════════════════
    // STEP EVENT HANDLING
    // ═══════════════════════════════════════════════════════════════

    private void onStepEvent(BoyerMooreSolver.StepType type, int textIdx, int patIdx,
            int shift, Map<String, Object> meta) {
        switch (type) {
            case INIT:
                clearCellColors();
                appendProgress("🚀 Boyer-Moore Search initialized.");
                appendProgress("   Text:    \"" + currentText + "\" (length " + currentText.length() + ")");
                appendProgress("   Pattern: \"" + currentPattern + "\" (length " + currentPattern.length() + ")");
                appendProgress("   Bad Character Table built.");
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Initialized. Ready to search.");
                delay(500);
                break;

            case ALIGN_PATTERN:
                clearCellColors();
                appendProgress("📍 Aligning pattern at position " + textIdx);
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Pattern aligned at position " + textIdx);
                delay(400);
                break;

            case COMPARE_CHAR: {
                String tc = meta != null ? (String) meta.get("textChar") : "?";
                String pc = meta != null ? (String) meta.get("patternChar") : "?";
                // Highlight comparing positions in YELLOW
                textCellColors.put(textIdx, Color.GOLD);
                patternCellColors.put(patIdx, Color.GOLD);
                lastComparedTextIdx = textIdx;
                lastComparedPatIdx = patIdx;
                appendProgress("🔍 Comparing text[" + textIdx + "]='" + tc +
                        "' with pattern[" + patIdx + "]='" + pc + "'");
                delay(300);
                break;
            }

            case CHAR_MATCH:
                // Mark matched positions in GREEN
                if (lastComparedTextIdx >= 0)
                    textCellColors.put(lastComparedTextIdx, Color.LIMEGREEN);
                if (lastComparedPatIdx >= 0)
                    patternCellColors.put(lastComparedPatIdx, Color.LIMEGREEN);
                appendProgress("   ✅ Match!");
                delay(200);
                break;

            case CHAR_MISMATCH: {
                // Mark mismatched positions in RED
                if (lastComparedTextIdx >= 0)
                    textCellColors.put(lastComparedTextIdx, Color.TOMATO);
                if (lastComparedPatIdx >= 0)
                    patternCellColors.put(lastComparedPatIdx, Color.TOMATO);
                String bc = meta != null ? (String) meta.get("badChar") : "?";
                int lastOcc = meta != null ? (Integer) meta.get("lastOccurrence") : -1;
                int shiftAmt = meta != null ? (Integer) meta.get("shift") : 1;

                if (bc != null && !bc.equals("?")) {
                    currentBadChar = bc.charAt(0);
                }
                if (lastComparedPatIdx >= 0 && lastComparedPatIdx < currentPattern.length()) {
                    currentPatternChar = currentPattern.charAt(lastComparedPatIdx);
                }
                currentLastOcc = lastOcc;
                pendingShiftAmount = shiftAmt;
                updateBadCharTable(); // Refresh table to show highlight

                String explicitCalculation = "shift = max(1, patIdx - lastOcc) = max(1, " + lastComparedPatIdx + " - "
                        + lastOcc + ") = " + shiftAmt;

                appendProgress("   ❌ Mismatch! Bad char '" + bc + "' last at pattern index " +
                        lastOcc + ".");
                appendProgress("   " + explicitCalculation);
                delay(800); // Slightly longer delay to let user see the lookup and arrow
                break;
            }

            case SHIFT_PATTERN:
                // Mark skipped region in CYAN
                clearCellColors();
                pendingShiftAmount = null;
                currentBadChar = null;
                currentPatternChar = null;
                currentLastOcc = null;
                updateBadCharTable(); // Remove highlight

                appendProgress("   ➡️ Shifting pattern to position " + textIdx + " (shifted " + shift + ")");
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Shifted pattern by " + shift + " to position " + textIdx);
                delay(300);
                break;

            case PATTERN_FOUND:
                // Mark entire matched region in GREEN
                for (int j = 0; j < currentPattern.length(); j++) {
                    textCellColors.put(textIdx + j, Color.LIMEGREEN);
                    patternCellColors.put(j, Color.LIMEGREEN);
                }
                appendProgress("🎉 Pattern FOUND at index " + textIdx + "!");
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Pattern found at index " + textIdx + "!");
                delay(800);
                break;

            case COMPLETE: {
                int totalFound = meta != null ? (Integer) meta.get("totalFound") : 0;
                if (totalFound > 0) {
                    List<Integer> positions = meta != null ? (List<Integer>) meta.get("foundPositions")
                            : Collections.emptyList();
                    appendProgress("✅ Search complete! Found " + totalFound +
                            " occurrence(s) at position(s): " + positions);
                } else {
                    appendProgress("❌ Search complete. Pattern not found.");
                }
                appendProgress("   Total comparisons: " + solver.getComparisons());
                appendProgress("   Total shifts: " + solver.getShifts());
                stopTimeline();
                break;
            }
        }

        renderCanvas();
        updateVariablesPanel();
    }

    private void delay(long ms) {
        pendingPause = true;
        if (isPlaying && timeline != null)
            timeline.pause();
        PauseTransition pt = new PauseTransition(Duration.millis(ms));
        pt.setOnFinished(e -> {
            pendingPause = false;
            if (isPlaying && timeline != null)
                timeline.play();
        });
        pt.play();
    }

    private void clearCellColors() {
        textCellColors.clear();
        patternCellColors.clear();
        lastComparedTextIdx = -1;
        lastComparedPatIdx = -1;
        pendingShiftAmount = null;
        currentBadChar = null;
        currentPatternChar = null;
        currentLastOcc = null;
    }

    // ═══════════════════════════════════════════════════════════════
    // PARAMETER HANDLERS
    // ═══════════════════════════════════════════════════════════════

    private void onApply() {
        String newText = textField != null ? textField.getText().trim() : currentText;
        String newPattern = patternField != null ? patternField.getText().trim() : currentPattern;
        if (newText.isEmpty())
            newText = "ABAAABCDABDE";
        if (newPattern.isEmpty())
            newPattern = "ABCDABD";
        currentText = newText;
        currentPattern = newPattern;
        refreshAll();
    }

    private void onRandomize() {
        Random rnd = new Random();
        // Generate a random text with A-D characters
        char[] alphabet = { 'A', 'B', 'C', 'D' };
        int textLen = 8 + rnd.nextInt(5); // 8-12 length
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < textLen; i++)
            sb.append(alphabet[rnd.nextInt(alphabet.length)]);
        currentText = sb.toString();

        // Pick a random pattern from the text (sublen 2-5)
        int patLen = 2 + rnd.nextInt(4);
        int start = rnd.nextInt(Math.max(1, currentText.length() - patLen));
        currentPattern = currentText.substring(start, Math.min(start + patLen, currentText.length()));

        if (textField != null)
            textField.setText(currentText);
        if (patternField != null)
            patternField.setText(currentPattern);
        refreshAll();
    }

    private void refreshAll() {
        stopTimeline();
        history.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        pendingPause = false;
        solver = new BoyerMooreSolver(currentText, currentPattern);
        solver.setStepListener(this::onStepEvent);
        codeImpl.updateParameters(currentText, currentPattern);

        // Resize canvas if needed
        double neededWidth = Math.max(800, (currentText.length() + 2) * (CELL_SIZE + CELL_GAP) + 2 * X_OFFSET);
        if (canvas != null)
            canvas.setWidth(neededWidth);

        clearCellColors();
        renderCanvas();
        buildSolutionsPane();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null && parent.stepDescription != null)
            parent.stepDescription.setText("Ready.");
    }

    // ═══════════════════════════════════════════════════════════════
    // LOGGING & VARIABLES
    // ═══════════════════════════════════════════════════════════════

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null)
            return;
        parent.progressArea.clear();
        appendProgress("Boyer-Moore Search ready.");
        appendProgress("Text: \"" + currentText + "\"");
        appendProgress("Pattern: \"" + currentPattern + "\"");
    }

    private void appendProgress(String line) {
        if (parent == null || parent.progressArea == null)
            return;
        parent.progressArea.appendText(line + "\n");
        if (countLogsForStep)
            currentStepLogLines++;
    }

    private void removeLastLogLines(int count) {
        if (parent == null || parent.progressArea == null || count <= 0)
            return;
        String text = parent.progressArea.getText();
        if (text == null || text.isEmpty())
            return;
        String[] lines = text.split("\n", -1);
        int total = lines.length;
        int newLen = Math.max(0, total - count);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newLen; i++)
            sb.append(lines[i]).append("\n");
        parent.progressArea.setText(sb.toString());
    }

    private void updateVariablesPanel() {
        if (parent == null || parent.variableList == null)
            return;
        parent.variableList.getItems().clear();

        int alignment = solver != null ? solver.getAlignment() : 0;
        int patPos = solver != null ? solver.getPatternPos() : 0;
        int comps = solver != null ? solver.getComparisons() : 0;
        int shiftCount = solver != null ? solver.getShifts() : 0;
        List<Integer> found = solver != null ? solver.getFoundPositions() : Collections.emptyList();

        parent.variableList.getItems().addAll(
                "Text length: " + currentText.length(),
                "Pattern length: " + currentPattern.length(),
                "Alignment (s): " + alignment,
                "Pattern pos (j): " + patPos,
                "Comparisons: " + comps,
                "Shifts: " + shiftCount,
                "Found at: " + (found.isEmpty() ? "—" : found.toString()),
                "State: " + (solver != null && solver.isDone() ? (found.isEmpty() ? "NOT FOUND" : "FOUND")
                        : "SEARCHING"));
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null)
            return;

        if (codeImpl != null) {
            codeImpl.updateParameters(currentText, currentPattern);
        }

        AlgorithmCode repoCode = CodeRepository.getCode("Boyer-Moore Search");
        if (repoCode instanceof BoyerMooreCode && parent.languageSelector != null) {
            ((BoyerMooreCode) repoCode).updateParameters(currentText, currentPattern);
            String selectedLanguage = parent.languageSelector.getValue();
            if (selectedLanguage != null && !parent.isUpdatingCode) {
                parent.isUpdatingCode = true;
                try {
                    String code = repoCode.getCodeForLanguage(selectedLanguage);
                    if (code != null && !code.isEmpty()) {
                        parent.codeArea.replaceText(code);
                        return;
                    }
                } finally {
                    parent.isUpdatingCode = false;
                }
            }
        }

        // Fallback: use Java code from local impl
        if (codeImpl != null) {
            parent.codeArea.replaceText(codeImpl.getJavaCode());
        }
    }
}
