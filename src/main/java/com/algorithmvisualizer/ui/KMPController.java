package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.KMPSolver;
import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.code.implementations.KMPCode;
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
 * Controller for KMP (Knuth-Morris-Pratt) string search visualization.
 *
 * Visualization: Text displayed as boxes on top, pattern sliding below.
 * Two phases: 1) Step-by-step LPS/failure function construction
 * 2) Pattern search using the failure function
 * Colors: GOLD=comparing, GREEN=match, RED=mismatch, CYAN=LPS highlight,
 * ORANGE=shift.
 *
 * CRITICAL: Follows the minimal FXML approach. Canvas is created
 * programmatically in setParentController(), NOT defined in FXML.
 */
public class KMPController implements AlgorithmViewController.AlgorithmSpecificController {

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
    private KMPSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;

    // ── Blocking flags ──────────────────────────────────────────────
    private boolean pendingPause = false;

    // ── History for step-back ────────────────────────────────────────
    private final Deque<KMPSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // ── Data ────────────────────────────────────────────────────────
    private String currentText = "BABABABABCABABCABAB";
    private String currentPattern = "ABABCABAB";
    private KMPCode codeImpl;

    // ── Visual state tracking ───────────────────────────────────────
    private Map<Integer, Color> textCellColors = new HashMap<>();
    private Map<Integer, Color> patternCellColors = new HashMap<>();
    private int lastComparedTextIdx = -1;
    private int lastComparedPatIdx = -1;
    private boolean inLPSPhase = true;

    // ── Ghost shift state ───────────────────────────────────────────
    private Integer pendingShiftAmount = null;
    private int pendingShiftOldAlignment = -1;
    private int lpsShiftHighlightIdx = -1; // LPS index to highlight during shift

    // ── LPS build visualization ─────────────────────────────────────
    private Map<Integer, Color> lpsBuildPatternColors = new HashMap<>();
    private int lpsHighlightI = -1;
    private int lpsHighlightLen = -1;
    private Set<Integer> lpsPrefixIndices = new HashSet<>(); // prefix range highlight
    private Set<Integer> lpsSuffixIndices = new HashSet<>(); // suffix range highlight

    // ── UI controls for parameters ──────────────────────────────────
    private TextField textField;
    private TextField patternField;

    @FXML
    private void initialize() {
        // PITFALL #2: Initialize state variables FIRST
        solver = new KMPSolver(currentText, currentPattern);
        solver.setStepListener(this::onStepEvent);
        codeImpl = new KMPCode();
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
            solver = new KMPSolver(currentText, currentPattern);
            solver.setStepListener(this::onStepEvent);
        }
        if (codeImpl == null) {
            codeImpl = new KMPCode();
            codeImpl.updateParameters(currentText, currentPattern);
        }

        // ── Build canvas programmatically (pitfall #1) ──────────
        double canvasWidth = Math.max(800, (currentText.length() + 2) * (CELL_SIZE + CELL_GAP) + 2 * X_OFFSET);
        canvas = new Canvas(canvasWidth, 240);
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
            Label hdr = new Label("KMP String Search Visualization");
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
                    legendItem(Color.TOMATO, "Mismatch"));
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // ── Parameters ──────────────────────────────────────────
        setupParameterControls();

        // ── Solutions pane: LPS/Failure Function Table ───────────
        buildSolutionsPane();

        // ── Playback controls ───────────────────────────────────
        if (parent.speedSlider != null)
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        // PITFALL #3: Hide pause button initially
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        // ── Code + logs + variables ─────────────────────────────
        parent.setCurrentAlgorithmName("KMP Search");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderCanvas();

        if (parent.stepDescription != null)
            parent.stepDescription.setText("Ready.");
    }

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

        // Size label → blank
        if (parent.paramSizeLabel != null) {
            parent.paramSizeLabel.setText(" ");
        }

        // Use paramElementsBox for custom controls
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
    // SOLUTIONS PANE (LPS / Failure Function Table)
    // ═══════════════════════════════════════════════════════════════

    private void buildSolutionsPane() {
        if (parent == null)
            return;

        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("LPS (Failure Function) Table");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }

        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(true);
            parent.solutionsSubHeaderBox.setManaged(true);
            parent.solutionsSubHeaderBox.getChildren().clear();
            Label subLabel = new Label("Longest Proper Prefix which is also Suffix");
            subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            parent.solutionsSubHeaderBox.getChildren().add(subLabel);
        }

        updateLPSTable();
    }

    private void updateLPSTable() {
        if (parent == null || parent.solutionsContent == null)
            return;
        parent.solutionsContent.getChildren().clear();
        parent.solutionsContent.setPadding(new Insets(10, 0, 10, 0));
        parent.solutionsContent.setAlignment(Pos.CENTER);

        int[] lps = solver.getLPS();

        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        // Header row: Index
        Label idxHeader = new Label("Index");
        idxHeader.setStyle(
                "-fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #e0e0e0; -fx-background-radius: 3; -fx-text-fill: #333333;");
        idxHeader.setMinWidth(50);
        idxHeader.setAlignment(Pos.CENTER);
        grid.add(idxHeader, 0, 0);

        // Header row: Char
        Label charHeader = new Label("Char");
        charHeader.setStyle(
                "-fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #e0e0e0; -fx-background-radius: 3; -fx-text-fill: #333333;");
        charHeader.setMinWidth(50);
        charHeader.setAlignment(Pos.CENTER);
        grid.add(charHeader, 0, 1);

        // Header row: LPS
        Label lpsHeader = new Label("LPS");
        lpsHeader.setStyle(
                "-fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #e0e0e0; -fx-background-radius: 3; -fx-text-fill: #333333;");
        lpsHeader.setMinWidth(50);
        lpsHeader.setAlignment(Pos.CENTER);
        grid.add(lpsHeader, 0, 2);

        for (int i = 0; i < currentPattern.length(); i++) {
            String bgColor = "#f5f5f5";
            String textFill = "#333333";
            String lpsStyle = "";

            // Highest priority: shift highlight (which LPS value was used)
            if (lpsShiftHighlightIdx >= 0 && i == lpsShiftHighlightIdx) {
                bgColor = "#E65100"; // Dark orange — this LPS value drove the shift
                textFill = "#FFFFFF";
                lpsStyle = "-fx-font-size: 14px;";
            }
            // LPS build phase: current 'i' position (suffix end)
            else if (lpsHighlightI >= 0 && i == lpsHighlightI) {
                bgColor = "#E65100"; // Dark orange for suffix (i)
                textFill = "#FFFFFF";
            }
            // LPS build phase: current 'len' position (prefix end)
            else if (lpsHighlightLen >= 0 && i == lpsHighlightLen) {
                bgColor = "#1565C0"; // Dark blue for prefix (len)
                textFill = "#FFFFFF";
            }
            // Prefix range highlight
            else if (lpsPrefixIndices.contains(i)) {
                bgColor = "#BBDEFB"; // Light blue for prefix range
            }
            // Suffix range highlight
            else if (lpsSuffixIndices.contains(i)) {
                bgColor = "#FFE0B2"; // Light orange for suffix range
            }
            // Non-zero LPS values
            else if (lps[i] > 0) {
                bgColor = "#C8E6C9"; // Green for computed non-zero
            }

            String cellStyle = "-fx-padding: 3 6; -fx-background-color: " + bgColor
                    + "; -fx-background-radius: 3; -fx-font-family: monospace; -fx-text-fill: " + textFill + ";";

            Label indexLabel = new Label(String.valueOf(i));
            indexLabel.setStyle(cellStyle);
            indexLabel.setMinWidth(30);
            indexLabel.setAlignment(Pos.CENTER);

            Label charLabel = new Label("'" + currentPattern.charAt(i) + "'");
            charLabel.setStyle(cellStyle);
            charLabel.setMinWidth(30);
            charLabel.setAlignment(Pos.CENTER);

            Label lpsLabel = new Label(String.valueOf(lps[i]));
            lpsLabel.setStyle(cellStyle + " -fx-font-weight: bold;" + lpsStyle);
            lpsLabel.setMinWidth(30);
            lpsLabel.setAlignment(Pos.CENTER);

            grid.add(indexLabel, i + 1, 0);
            grid.add(charLabel, i + 1, 1);
            grid.add(lpsLabel, i + 1, 2);
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(200);
        scrollPane.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(scrollPane);

        // ── Explanation box during LPS build ──────────────────────
        if (lpsHighlightI >= 0 && lpsHighlightLen >= 0 && inLPSPhase) {
            VBox calcBox = new VBox(5);
            calcBox.setAlignment(Pos.CENTER);
            calcBox.setPadding(new Insets(5, 5, 5, 5));

            Label l1 = new Label("Comparing pattern[" + lpsHighlightI + "] = '" + currentPattern.charAt(lpsHighlightI) +
                    "' with pattern[" + lpsHighlightLen + "] = '" + currentPattern.charAt(lpsHighlightLen) + "'");
            l1.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333;");

            // Show prefix and suffix ranges
            if (lpsHighlightLen > 0) {
                String prefix = currentPattern.substring(0, lpsHighlightLen);
                Label l2 = new Label("Prefix so far: \"" + prefix + "\"");
                l2.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-font-style: italic;");
                calcBox.getChildren().addAll(l1, l2);
            } else {
                calcBox.getChildren().add(l1);
            }
            container.getChildren().add(calcBox);
        }

        // ── Explanation box during shift ──────────────────────────
        if (lpsShiftHighlightIdx >= 0 && pendingShiftAmount != null && !inLPSPhase) {
            VBox calcBox = new VBox(5);
            calcBox.setAlignment(Pos.CENTER);
            calcBox.setPadding(new Insets(8));
            calcBox.setStyle(
                    "-fx-background-color: #FFF3E0; -fx-background-radius: 6; -fx-border-color: #FFB74D; -fx-border-radius: 6;");

            int newJ = lps[lpsShiftHighlightIdx];
            int oldJ = lpsShiftHighlightIdx + 1; // Since we queried LPS[j-1], old j was lpsShiftHighlightIdx + 1

            Label l1 = new Label("Using LPS[" + lpsShiftHighlightIdx + "] = " + newJ);
            l1.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E65100;");

            Label l2 = new Label("j pointer updates: " + oldJ + " → " + newJ);
            l2.setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-text-fill: #333;");

            Label l3 = new Label(
                    "Pattern shifts right by: " + oldJ + " - " + newJ + " = " + pendingShiftAmount + " positions");
            l3.setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-text-fill: #333; -fx-font-weight: bold;");

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

        if (inLPSPhase) {
            // During LPS build: show pattern only
            renderLPSBuildPhase(gc);
        } else {
            // During search: show text + pattern
            renderSearchPhase(gc);
        }
    }

    private void renderLPSBuildPhase(GraphicsContext gc) {
        // Title
        gc.setFont(Font.font("System", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#555"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Building Failure Function (LPS array)", X_OFFSET, INDEX_ROW_Y);

        // ── Draw pattern indices ─────────────────────────────
        gc.setFont(Font.font("Monospace", 10));
        gc.setFill(Color.GRAY);
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < currentPattern.length(); i++) {
            double x = X_OFFSET + i * (CELL_SIZE + CELL_GAP);
            gc.fillText(String.valueOf(i), x + CELL_SIZE / 2, TEXT_ROW_Y - 2);
        }

        // ── Draw pattern boxes with darker colors ────────────────
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        for (int i = 0; i < currentPattern.length(); i++) {
            double x = X_OFFSET + i * (CELL_SIZE + CELL_GAP);
            Color bgColor;
            Color textColor = Color.BLACK;
            if (lpsBuildPatternColors.containsKey(i)) {
                bgColor = lpsBuildPatternColors.get(i);
                // Use white text on dark backgrounds
                if (bgColor.equals(Color.web("#1565C0")) || bgColor.equals(Color.web("#E65100"))) {
                    textColor = Color.WHITE;
                }
            } else if (lpsPrefixIndices.contains(i)) {
                bgColor = Color.web("#BBDEFB"); // light blue for prefix range
            } else if (lpsSuffixIndices.contains(i)) {
                bgColor = Color.web("#FFE0B2"); // light orange for suffix range
            } else {
                bgColor = Color.web("#dde4ff");
            }
            drawCell(gc, x, TEXT_ROW_Y, String.valueOf(currentPattern.charAt(i)), bgColor, textColor);
        }

        // ── Draw bracket annotations for prefix/suffix ──────────
        if (lpsHighlightLen > 0 && lpsHighlightI >= 0) {
            // Prefix bracket (below cells)
            gc.setStroke(Color.web("#1565C0"));
            gc.setLineWidth(2);
            double pStartX = X_OFFSET;
            double pEndX = X_OFFSET + (lpsHighlightLen - 1) * (CELL_SIZE + CELL_GAP) + CELL_SIZE;
            double bracketY = TEXT_ROW_Y + CELL_SIZE + 4;
            gc.strokeLine(pStartX, bracketY, pEndX, bracketY);
            gc.strokeLine(pStartX, bracketY - 3, pStartX, bracketY);
            gc.strokeLine(pEndX, bracketY - 3, pEndX, bracketY);
            gc.setFill(Color.web("#1565C0"));
            gc.setFont(Font.font("System", FontWeight.BOLD, 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("prefix (len=" + lpsHighlightLen + ")", (pStartX + pEndX) / 2, bracketY + 12);
        }

        // ── Draw LPS values row ─────────────────────────────
        double lpsRowY = TEXT_ROW_Y + CELL_SIZE + 50;
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#777"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("LPS:", X_OFFSET - 5, lpsRowY + 4);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        int[] lps = solver.getLPS();
        for (int i = 0; i < currentPattern.length(); i++) {
            double x = X_OFFSET + i * (CELL_SIZE + CELL_GAP);
            // Highlight current LPS cell being set
            if (lpsHighlightI >= 0 && i == lpsHighlightI) {
                gc.setFill(Color.web("#E65100"));
            } else if (lps[i] > 0) {
                gc.setFill(Color.web("#2E7D32"));
            } else {
                gc.setFill(Color.web("#333"));
            }
            gc.fillText(String.valueOf(lps[i]), x + CELL_SIZE / 2, lpsRowY + 4);
        }

        // ── Status line ──────────────────────────────────────
        gc.setFont(Font.font("System", 12));
        gc.setFill(Color.web("#555"));
        gc.setTextAlign(TextAlignment.LEFT);
        String status = solver != null ? solver.getCurrentStepDescription() : "";
        gc.fillText(status, X_OFFSET, lpsRowY + 30);
    }

    private void renderSearchPhase(GraphicsContext gc) {
        int alignment = solver != null ? solver.getAlignment() : 0;
        // Use old alignment if we're showing ghost shift
        int drawAlignment = (pendingShiftAmount != null && pendingShiftOldAlignment >= 0)
                ? pendingShiftOldAlignment
                : alignment;

        // ── Draw index numbers ──────────────────────────────
        gc.setFont(Font.font("Monospace", 10));
        gc.setFill(Color.GRAY);
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < currentText.length(); i++) {
            double x = X_OFFSET + i * (CELL_SIZE + CELL_GAP);
            gc.fillText(String.valueOf(i), x + CELL_SIZE / 2, INDEX_ROW_Y);
        }

        // ── Draw text row ─────────────────────────────────
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        for (int i = 0; i < currentText.length(); i++) {
            double x = X_OFFSET + i * (CELL_SIZE + CELL_GAP);
            Color bgColor = textCellColors.getOrDefault(i, Color.web("#f0f0f0"));
            drawCell(gc, x, TEXT_ROW_Y, String.valueOf(currentText.charAt(i)), bgColor, Color.BLACK);
        }

        // ── Draw pattern row (shifted by alignment) ─────────────
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int j = 0; j < currentPattern.length(); j++) {
            double x = X_OFFSET + (drawAlignment + j) * (CELL_SIZE + CELL_GAP);
            Color bgColor = patternCellColors.getOrDefault(j, Color.web("#dde4ff"));
            drawCell(gc, x, PATTERN_ROW_Y, String.valueOf(currentPattern.charAt(j)), bgColor, Color.BLACK);
        }

        // ── Ghost pattern + shift arrow (like Boyer-Moore) ───────
        if (pendingShiftAmount != null && pendingShiftAmount > 0) {
            int newAlignment = drawAlignment + pendingShiftAmount;

            // Ghost pattern at new position
            gc.setGlobalAlpha(0.3);
            for (int j = 0; j < currentPattern.length(); j++) {
                double x = X_OFFSET + (newAlignment + j) * (CELL_SIZE + CELL_GAP);
                drawCell(gc, x, PATTERN_ROW_Y, String.valueOf(currentPattern.charAt(j)),
                        Color.web("#cccccc"), Color.BLACK);
            }
            gc.setGlobalAlpha(1.0);

            // Shift arrow
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            double startX = X_OFFSET + (drawAlignment + currentPattern.length()) * (CELL_SIZE + CELL_GAP) + 10;
            double endX = startX + pendingShiftAmount * (CELL_SIZE + CELL_GAP) - 20;
            double arrowY = PATTERN_ROW_Y + CELL_SIZE / 2;

            if (endX > startX) {
                gc.strokeLine(startX, arrowY, endX, arrowY);
                gc.strokeLine(endX, arrowY, endX - 8, arrowY - 6);
                gc.strokeLine(endX, arrowY, endX - 8, arrowY + 6);
                gc.setFill(Color.ORANGE);
                gc.setFont(Font.font("System", FontWeight.BOLD, 12));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("Shift by " + pendingShiftAmount, startX + (endX - startX) / 2, arrowY - 10);
            }
        }

        // ── Draw alignment arrow ────────────────────────────
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(2);
        double arrowX = X_OFFSET + drawAlignment * (CELL_SIZE + CELL_GAP) + CELL_SIZE / 2;
        gc.strokeLine(arrowX, TEXT_ROW_Y + CELL_SIZE + 2, arrowX, PATTERN_ROW_Y - 2);

        // ── Status line ─────────────────────────────────
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

        KMPSolver.State s = history.pop();
        solver.restore(s);

        // Remove logs
        if (!progressHistory.isEmpty()) {
            int toRemove = progressHistory.pop();
            removeLastLogLines(toRemove);
        }

        // Repaint
        clearCellColors();
        renderCanvas();
        updateLPSTable();
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
        inLPSPhase = true;
        solver = new KMPSolver(currentText, currentPattern);
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

    private void onStepEvent(KMPSolver.StepType type, int textIdx, int patIdx,
            int shift, Map<String, Object> meta) {
        switch (type) {
            case INIT:
                clearCellColors();
                inLPSPhase = true;
                appendProgress("🚀 KMP Search initialized.");
                appendProgress("   Text:    \"" + currentText + "\" (length " + currentText.length() + ")");
                appendProgress("   Pattern: \"" + currentPattern + "\" (length " + currentPattern.length() + ")");
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Initialized. Building failure function...");
                delay(500);
                break;

            case BUILD_FAILURE_START:
                inLPSPhase = true;
                lpsBuildPatternColors.clear();
                appendProgress("🔧 Building LPS (failure function) array...");
                appendProgress("   lps[0] = 0 (always)");
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Building LPS array...");
                delay(400);
                break;

            case FAILURE_COMPARE: {
                int i = meta != null ? (Integer) meta.get("i") : 0;
                int len = meta != null ? (Integer) meta.get("len") : 0;
                String charI = meta != null ? (String) meta.get("charI") : "?";
                String charLen = meta != null ? (String) meta.get("charLen") : "?";

                lpsBuildPatternColors.clear();
                lpsPrefixIndices.clear();
                lpsSuffixIndices.clear();

                // Dark orange for suffix position (i), dark blue for prefix position (len)
                lpsBuildPatternColors.put(i, Color.web("#E65100"));
                lpsBuildPatternColors.put(len, Color.web("#1565C0"));
                lpsHighlightI = i;
                lpsHighlightLen = len;

                // Mark prefix range [0..len-1] and suffix range [i-len+1..i-1]
                for (int k = 0; k < len; k++) {
                    lpsPrefixIndices.add(k);
                }
                int suffStart = i - len + 1;
                for (int k = suffStart; k < i; k++) {
                    if (k > 0)
                        lpsSuffixIndices.add(k);
                }

                appendProgress("   🔍 Compare pattern[" + i + "]='" + charI +
                        "' with pattern[" + len + "]='" + charLen + "'");
                delay(300);
                break;
            }

            case FAILURE_MATCH: {
                int i = meta != null ? (Integer) meta.get("i") : 0;
                int value = meta != null ? (Integer) meta.get("value") : 0;

                lpsBuildPatternColors.put(i, Color.LIMEGREEN);
                if (lpsHighlightLen >= 0)
                    lpsBuildPatternColors.put(lpsHighlightLen, Color.LIMEGREEN);

                // Expand prefix/suffix visually to include the new match
                lpsHighlightI = i;
                lpsHighlightLen = value;
                lpsPrefixIndices.clear();
                lpsSuffixIndices.clear();
                for (int k = 0; k < value; k++) {
                    lpsPrefixIndices.add(k);
                }
                int suffStart = i - value + 1;
                for (int k = suffStart; k <= i; k++) {
                    if (k > 0)
                        lpsSuffixIndices.add(k);
                }

                appendProgress("   ✅ Match! lps[" + i + "] = " + value);
                delay(800);
                break;
            }

            case FAILURE_MISMATCH: {
                int i = meta != null ? (Integer) meta.get("i") : 0;
                if (meta != null && meta.containsKey("fallbackFrom")) {
                    int from = (Integer) meta.get("fallbackFrom");
                    int to = (Integer) meta.get("fallbackTo");
                    lpsBuildPatternColors.put(i, Color.TOMATO);
                    appendProgress("   ❌ Mismatch! Fallback: len " + from + " → " + to);
                }
                delay(300);
                break;
            }

            case FAILURE_SET_VALUE: {
                int i = meta != null ? (Integer) meta.get("i") : 0;
                lpsBuildPatternColors.put(i, Color.TOMATO);
                appendProgress("   ❌ Mismatch at base. lps[" + i + "] = 0");
                delay(200);
                break;
            }

            case FAILURE_COMPLETE:
                lpsBuildPatternColors.clear();
                lpsPrefixIndices.clear();
                lpsSuffixIndices.clear();
                lpsHighlightI = -1;
                lpsHighlightLen = -1;
                inLPSPhase = false;
                appendProgress("✅ LPS array built: " + java.util.Arrays.toString(solver.getLPS()));
                appendProgress("   Now searching text...");
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("LPS built. Starting search phase.");

                // Slight shake to grab attention before search phase
                if (canvas != null) {
                    javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                            javafx.util.Duration.millis(50), canvas);
                    tt.setByX(5f);
                    tt.setCycleCount(8);
                    tt.setAutoReverse(true);
                    tt.setOnFinished(e -> canvas.setTranslateX(0));
                    tt.play();
                }

                delay(1500); // Added a solid pause between phases
                break;

            case ALIGN_PATTERN:
                clearCellColors();
                pendingShiftAmount = null;
                pendingShiftOldAlignment = -1;
                lpsShiftHighlightIdx = -1;
                appendProgress("📍 Pattern aligned at position " + textIdx);
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Pattern aligned at position " + textIdx);
                delay(300);
                break;

            case COMPARE_CHAR: {
                String tc = meta != null ? (String) meta.get("textChar") : "?";
                String pc = meta != null ? (String) meta.get("patternChar") : "?";
                // Highlight comparing positions in GOLD
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

                int shiftAmt = meta != null && meta.containsKey("shift") ? (Integer) meta.get("shift") : 1;

                if (meta != null && meta.containsKey("lpsValue")) {
                    int lpsVal = (Integer) meta.get("lpsValue");
                    int oldPatPos = (Integer) meta.get("oldPatPos");
                    // Set ghost shift state
                    pendingShiftAmount = shiftAmt;
                    pendingShiftOldAlignment = solver.getAlignment() - shiftAmt; // old alignment before solver moved
                    lpsShiftHighlightIdx = oldPatPos - 1; // highlight this LPS entry
                    appendProgress("   ❌ Mismatch! Using LPS: lps[" + (oldPatPos - 1) + "] = " + lpsVal +
                            " → shift pattern by " + shiftAmt);
                } else {
                    appendProgress("   ❌ Mismatch at j=0. Advance text pointer.");
                }
                delay(800); // Longer delay so user can see ghost shift and LPS highlight
                break;
            }

            case SHIFT_PATTERN:
                clearCellColors();
                pendingShiftAmount = null;
                pendingShiftOldAlignment = -1;
                lpsShiftHighlightIdx = -1;
                appendProgress("   ➡️ Pattern shifted to alignment " + textIdx);
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Pattern shifted to alignment " + textIdx);
                delay(300);
                break;

            case PATTERN_FOUND: {
                // Mark entire matched region in GREEN
                for (int j = 0; j < currentPattern.length(); j++) {
                    textCellColors.put(textIdx + j, Color.LIMEGREEN);
                    patternCellColors.put(j, Color.LIMEGREEN);
                }
                appendProgress("🎉 Pattern FOUND at index " + textIdx + "!");
                if (parent != null && parent.stepDescription != null)
                    parent.stepDescription.setText("Pattern found at index " + textIdx + "!");

                // Show ghost shift + LPS highlight (same as mismatch — KMP uses lps[j-1] to
                // continue)
                if (meta != null && meta.containsKey("shift")) {
                    int shiftAmt = (Integer) meta.get("shift");
                    int lpsIdx = (Integer) meta.get("lpsIdx");
                    int lpsVal = (Integer) meta.get("lpsValue");
                    pendingShiftAmount = shiftAmt;
                    pendingShiftOldAlignment = textIdx; // match started here
                    lpsShiftHighlightIdx = lpsIdx;
                    appendProgress("   Using LPS[" + lpsIdx + "] = " + lpsVal +
                            " to continue searching (shift by " + shiftAmt + ")");
                }
                delay(1000);
                break;
            }

            case COMPLETE: {
                int totalFound = meta != null ? (Integer) meta.get("totalFound") : 0;
                if (totalFound > 0) {
                    @SuppressWarnings("unchecked")
                    List<Integer> positions = meta != null ? (List<Integer>) meta.get("foundPositions")
                            : Collections.emptyList();
                    appendProgress("✅ Search complete! Found " + totalFound +
                            " occurrence(s) at position(s): " + positions);
                } else {
                    appendProgress("❌ Search complete. Pattern not found.");
                }
                appendProgress("   Total comparisons: " + solver.getComparisons());
                stopTimeline();
                break;
            }
        }

        renderCanvas();
        updateLPSTable();
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
        lpsBuildPatternColors.clear();
        lpsPrefixIndices.clear();
        lpsSuffixIndices.clear();
        lastComparedTextIdx = -1;
        lastComparedPatIdx = -1;
        lpsHighlightI = -1;
        lpsHighlightLen = -1;
    }

    // ═══════════════════════════════════════════════════════════════
    // PARAMETER HANDLERS
    // ═══════════════════════════════════════════════════════════════

    private void onApply() {
        String newText = textField != null ? textField.getText().trim() : currentText;
        String newPattern = patternField != null ? patternField.getText().trim() : currentPattern;
        if (newText.isEmpty())
            newText = "BABABABABCABABCABAB";
        if (newPattern.isEmpty())
            newPattern = "ABABCABAB";
        currentText = newText;
        currentPattern = newPattern;
        refreshAll();
    }

    private void onRandomize() {
        Random rnd = new Random();
        // Generate a random text with A-D characters
        char[] alphabet = { 'A', 'B', 'C', 'D' };
        int textLen = 10 + rnd.nextInt(6); // 10-15 length
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < textLen; i++)
            sb.append(alphabet[rnd.nextInt(alphabet.length)]);
        currentText = sb.toString();

        // Pick a random pattern from the text (sublen 3-6)
        int patLen = 3 + rnd.nextInt(4);
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
        inLPSPhase = true;
        solver = new KMPSolver(currentText, currentPattern);
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
        appendProgress("KMP Search ready.");
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

        int textP = solver != null ? solver.getTextPos() : 0;
        int patP = solver != null ? solver.getPatternPos() : 0;
        int comps = solver != null ? solver.getComparisons() : 0;
        int alignment = solver != null ? solver.getAlignment() : 0;
        List<Integer> found = solver != null ? solver.getFoundPositions() : Collections.emptyList();

        parent.variableList.getItems().addAll(
                "Text length: " + currentText.length(),
                "Pattern length: " + currentPattern.length(),
                "Alignment: " + alignment,
                "Text pos (i): " + textP,
                "Pattern pos (j): " + patP,
                "Comparisons: " + comps,
                "Found at: " + (found.isEmpty() ? "—" : found.toString()),
                "Phase: " + (inLPSPhase ? "LPS BUILD"
                        : (solver != null && solver.isDone() ? (found.isEmpty() ? "NOT FOUND" : "FOUND")
                                : "SEARCHING")));
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null)
            return;

        if (codeImpl != null) {
            codeImpl.updateParameters(currentText, currentPattern);
        }

        AlgorithmCode repoCode = CodeRepository.getCode("KMP Search");
        if (repoCode instanceof KMPCode && parent.languageSelector != null) {
            ((KMPCode) repoCode).updateParameters(currentText, currentPattern);
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
