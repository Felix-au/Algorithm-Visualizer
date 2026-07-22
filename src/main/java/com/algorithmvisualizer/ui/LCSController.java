package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.LCSSolver;
import com.algorithmvisualizer.model.Algorithm;
import com.algorithmvisualizer.model.LCSAlgorithm;
import com.algorithmvisualizer.visualization.LCSTableRenderer;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.code.implementations.LCSCode;
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
 * Controller for Longest Common Subsequence visualization.
 */
public class LCSController implements AlgorithmViewController.AlgorithmSpecificController {
    
    private AlgorithmViewController parent;
    private LCSAlgorithm algorithm;
    private LCSSolver solver;
    private LCSTableRenderer renderer;
    
    // Parameters
    private String str1 = "ABCDGH";
    private String str2 = "AEDFHR";
    
    // UI Components
    private TextField str1Field;
    private TextField str2Field;
    private Label lcsResultLabel;
    private Label lcsLengthLabel;
    private VBox stepInfoBox;
    
    // State
    private Stack<LCSSolver.State> history;
    private Timeline timeline;
    private long stepDelayUntilMs = 0L;
    
    // Colors
    private static final Color COLOR_CURRENT = Color.rgb(255, 235, 59);
    private static final Color COLOR_COMPARING = Color.rgb(144, 202, 249);
    private static final Color COLOR_MATCH = Color.rgb(76, 175, 80);  // Green
    private static final Color COLOR_DEPENDENCY = Color.rgb(179, 229, 252);
    
    @FXML
    private void initialize() {
        Canvas canvas = new Canvas(800, 600);
        renderer = new LCSTableRenderer(canvas);
        
        // Initialize renderer with strings
        renderer.setStrings(str1, str2);
        
        solver = new LCSSolver();
        solver.setStrings(str1, str2);
        solver.setStepListener(this::onStepEvent);
        solver.reset();
        
        history = new Stack<>();
    }
    
    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;
        
        // Register code immediately
        new LCSCode();
        
        // Place canvas in center
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(renderer.getCanvas());
        }
        
        // Header
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Longest Common Subsequence (LCS)");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().add(hdr);
        }
        
        // Legend
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(15.0);
            
            javafx.scene.shape.Rectangle current = new javafx.scene.shape.Rectangle(20, 20, COLOR_CURRENT);
            HBox lCurrent = new HBox(5.0, current, new Label("Current Cell"));
            
            javafx.scene.shape.Rectangle comparing = new javafx.scene.shape.Rectangle(20, 20, COLOR_COMPARING);
            HBox lComparing = new HBox(5.0, comparing, new Label("Comparing"));
            
            javafx.scene.shape.Rectangle match = new javafx.scene.shape.Rectangle(20, 20, Color.rgb(76, 175, 80));
            HBox lMatch = new HBox(5.0, match, new Label("Match"));
            
            javafx.scene.shape.Rectangle dependency = new javafx.scene.shape.Rectangle(20, 20, COLOR_DEPENDENCY);
            HBox lDependency = new HBox(5.0, dependency, new Label("Dependency"));
            
            javafx.scene.shape.Rectangle path = new javafx.scene.shape.Rectangle(20, 20, Color.rgb(255, 183, 77));
            HBox lPath = new HBox(5.0, path, new Label("LCS Path"));
            
            row.getChildren().addAll(lCurrent, lComparing, lMatch, lDependency, lPath);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }
        
        setupParameterControls();
        buildSolutionsPane();
        
        // Controls wiring
        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }
        
        // Initialize
        parent.setCurrentAlgorithmName("Longest Common Subsequence");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderVisualization();
        if (parent.stepDescription != null) {
            parent.stepDescription.setText("Ready.");
        }
    }
    
    private void setupParameterControls() {
        if (parent.paramSizeLabel != null) {
            parent.paramSizeLabel.setVisible(false);
            parent.paramSizeLabel.setManaged(false);
        }
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(false);
            parent.paramBoardSizeSpinner.setManaged(false);
        }
        
        // String 1 field
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("String 1:");
        }
        
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }
        
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("e.g. ABCDGH");
            parent.paramElementsField.setText(str1);
            str1Field = parent.paramElementsField;
            
            // Add listener to sync with code
            str1Field.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals(str1)) {
                    str1 = newVal.trim().toUpperCase();
                    if (str1.isEmpty()) str1 = "A";
                    renderCode();
                }
            });
        }
        
        // Add String 2 field in parameter controls
        if (parent.paramElementsField != null && parent.paramElementsField.getParent() != null) {
            javafx.scene.Parent elementsParent = parent.paramElementsField.getParent();
            
            if (elementsParent instanceof VBox) {
                VBox elementsVBox = (VBox) elementsParent;
                
                Label str2Label = new Label("String 2:");
                str2Label.setStyle("-fx-font-weight: normal;");
                
                str2Field = new TextField(str2);
                str2Field.setPromptText("e.g. AEDFHR");
                str2Field.setPrefWidth(220);
                
                // Add listener to sync with code
                str2Field.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && !newVal.equals(str2)) {
                        str2 = newVal.trim().toUpperCase();
                        if (str2.isEmpty()) str2 = "A";
                        renderCode();
                    }
                });
                
                // Find the index of paramElementsField and add str2 components after it
                int elementsFieldIndex = elementsVBox.getChildren().indexOf(parent.paramElementsField);
                if (elementsFieldIndex >= 0) {
                    elementsVBox.getChildren().add(elementsFieldIndex + 1, str2Label);
                    elementsVBox.getChildren().add(elementsFieldIndex + 2, str2Field);
                }
            }
        }
        
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setText("Random Strings");
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
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label header = new Label("LCS Details");
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().add(header);
        }
        
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }
        
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            
            // Create a VBox to hold all blocks
            VBox container = new VBox(15);
            container.setPrefWidth(Double.MAX_VALUE);
            
            // Input strings display block
            VBox inputBlock = new VBox(8);
            inputBlock.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;");
            
            Label inputHeader = new Label("📝 Input Strings");
            inputHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
            
            Label str1Display = new Label("String 1: \"" + str1 + "\"");
            str1Display.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-text-fill: #212529;");
            str1Display.setWrapText(true);
            
            Label str2Display = new Label("String 2: \"" + str2 + "\"");
            str2Display.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-text-fill: #212529;");
            str2Display.setWrapText(true);
            
            inputBlock.getChildren().addAll(inputHeader, str1Display, str2Display);
            
            // Current step info block
            VBox stepBlock = new VBox(8);
            stepBlock.setStyle("-fx-background-color: #fff3cd; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ffc107; -fx-border-width: 2; -fx-border-radius: 8;");
            
            Label stepHeader = new Label("⚡ Current Step");
            stepHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #856404;");
            
            stepInfoBox = new VBox(6);
            stepInfoBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");
            Label stepPlaceholder = new Label("Waiting to start...");
            stepPlaceholder.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            stepInfoBox.getChildren().add(stepPlaceholder);
            
            stepBlock.getChildren().addAll(stepHeader, stepInfoBox);
            
            // LCS result block
            VBox resultBlock = new VBox(8);
            resultBlock.setStyle("-fx-background-color: #d4edda; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #28a745; -fx-border-width: 2; -fx-border-radius: 8;");
            
            Label resultHeader = new Label("🎯 LCS Result");
            resultHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #155724;");
            
            lcsResultLabel = new Label("LCS: (not computed yet)");
            lcsResultLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #155724;");
            lcsResultLabel.setWrapText(true);
            
            lcsLengthLabel = new Label("Length: 0");
            lcsLengthLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #155724;");
            
            resultBlock.getChildren().addAll(resultHeader, lcsResultLabel, lcsLengthLabel);
            
            // Add all blocks to container
            container.getChildren().addAll(inputBlock, stepBlock, resultBlock);
            
            // Add container to parent
            parent.solutionsContent.getChildren().add(container);
        }
    }
    
    private void onRandomize() {
        Random rand = new Random();
        int len1 = 3 + rand.nextInt(3);  // 3-5
        int len2 = 3 + rand.nextInt(3);  // 3-5
        
        str1 = generateRandomString(len1);
        str2 = generateRandomString(len2);
        
        if (str1Field != null) str1Field.setText(str1);
        if (str2Field != null) str2Field.setText(str2);
        
        renderCode();
        refreshAll();
    }
    
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    private void onApply() {
        if (str1Field != null) str1 = str1Field.getText().trim().toUpperCase();
        if (str2Field != null) str2 = str2Field.getText().trim().toUpperCase();
        
        if (str1.isEmpty()) str1 = "A";
        if (str2.isEmpty()) str2 = "A";
        
        renderCode();
        refreshAll();
    }
    
    private void refreshAll() {
        stopTimeline();
        history.clear();
        
        solver = new LCSSolver();
        solver.setStrings(str1, str2);
        solver.setStepListener(this::onStepEvent);
        solver.reset();
        
        renderer.setStrings(str1, str2);
        renderer.setTable(solver.getDP());
        
        buildSolutionsPane();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderVisualization();
    }
    
    private void renderVisualization() {
        renderer.setTable(solver.getDP());
        renderer.setPathCells(solver.getPathCells());
        renderer.render();
    }
    
    @Override
    public void onPlay() {
        if (solver.isDone()) {
            onReset();
        }
        rebuildTimelineWithCurrentSpeed();
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(true);
            parent.pauseButton.setManaged(true);
            parent.pauseButton.setDisable(false);  // Enable pause button
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
            LCSSolver.State prevState = history.pop();
            solver.restore(prevState);
            renderVisualization();
            updateVariablesPanel();
            updateLCSResult();
        }
    }
    
    @Override
    public void onReset() {
        stopTimeline();
        history.clear();
        solver.reset();
        renderer.clearHighlights();
        renderVisualization();
        initProgressLog();
        updateVariablesPanel();
        updateLCSResult();
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
    
    private void onStepEvent(LCSSolver.StepType type, int i, int j, Map<String, Object> meta) {
        renderer.clearHighlights();
        
        switch (type) {
            case INIT:
                appendProgress("🚀 Starting LCS algorithm...");
                appendProgress("String 1: \"" + str1 + "\" (length: " + str1.length() + ")");
                appendProgress("String 2: \"" + str2 + "\" (length: " + str2.length() + ")");
                appendProgress("Initializing DP table...");
                updateStepInfo("Initializing", "Setting up DP table with zeros");
                delay(1200);  // 800 * 1.5
                break;
                
            case HIGHLIGHT_CELL:
                appendProgress("📍 Processing cell [" + i + "][" + j + "]");
                renderer.highlightCell(i, j, COLOR_CURRENT);
                updateStepInfo("Cell [" + i + "][" + j + "]", "Determining value for this cell");
                delay(450);  // 300 * 1.5
                break;
                
            case COMPARE_CHARS:
                char c1 = (char) meta.get("char1");
                char c2 = (char) meta.get("char2");
                appendProgress("🔍 Comparing: '" + c1 + "' vs '" + c2 + "'");
                renderer.highlightCell(i, j, COLOR_COMPARING);
                updateStepInfo("Comparing Characters",
                    "String 1[" + (i-1) + "] = '" + c1 + "'\n" +
                    "String 2[" + (j-1) + "] = '" + c2 + "'");
                delay(600);  // 400 * 1.5
                break;
                
            case CHARS_MATCH:
                char ch = (char) meta.get("char");
                int prevVal = (int) meta.get("prevValue");
                appendProgress("✓ Match! '" + ch + "' = '" + ch + "'");
                appendProgress("  dp[" + i + "][" + j + "] = dp[" + (i-1) + "][" + (j-1) + "] + 1 = " + (prevVal + 1));
                renderer.highlightCell(i, j, COLOR_MATCH);
                renderer.highlightCell(i-1, j-1, COLOR_DEPENDENCY);
                renderer.setArrow(i, j, "↖");
                updateStepInfo("Characters Match!",
                    "Taking diagonal value + 1\n" +
                    "dp[" + (i-1) + "][" + (j-1) + "] + 1 = " + (prevVal + 1));
                delay(900);  // 600 * 1.5
                break;
                
            case CHARS_DIFFER:
                int topVal = (int) meta.get("topValue");
                int leftVal = (int) meta.get("leftValue");
                appendProgress("✗ No match. Taking max of top and left");
                appendProgress("  max(dp[" + (i-1) + "][" + j + "], dp[" + i + "][" + (j-1) + "]) = max(" + topVal + ", " + leftVal + ")");
                renderer.highlightCell(i, j, COLOR_COMPARING);
                updateStepInfo("Characters Differ",
                    "Taking max of:\n" +
                    "Top: dp[" + (i-1) + "][" + j + "] = " + topVal + "\n" +
                    "Left: dp[" + i + "][" + (j-1) + "] = " + leftVal);
                delay(750);  // 500 * 1.5
                break;
                
            case SHOW_DEPENDENCIES:
                boolean match = (boolean) meta.get("match");
                int diagVal = (int) meta.get("diagValue");
                topVal = (int) meta.get("topValue");
                leftVal = (int) meta.get("leftValue");
                
                if (match) {
                    renderer.highlightCell(i-1, j-1, COLOR_DEPENDENCY);
                } else {
                    renderer.highlightCell(i-1, j, COLOR_DEPENDENCY);
                    renderer.highlightCell(i, j-1, COLOR_DEPENDENCY);
                }
                delay(600);  // 400 * 1.5
                break;
                
            case UPDATE_CELL:
                int value = (int) meta.get("value");
                appendProgress("✅ Updated: dp[" + i + "][" + j + "] = " + value);
                renderer.highlightCell(i, j, COLOR_MATCH);
                delay(450);  // 300 * 1.5
                break;
                
            case ROW_COMPLETE:
                int row = (int) meta.get("row");
                appendProgress("📊 Row " + row + " complete");
                delay(300);  // 200 * 1.5
                break;
                
            case TABLE_COMPLETE:
                appendProgress("\n" + "═".repeat(50));
                appendProgress("✅ DP table construction complete!");
                appendProgress("LCS length: " + solver.getDP()[i][j]);
                appendProgress("═".repeat(50));
                updateStepInfo("Table Complete", "Starting traceback to find LCS");
                delay(1500);  // 1000 * 1.5
                break;
                
            case TRACEBACK_START:
                appendProgress("\n🔙 Starting traceback from [" + i + "][" + j + "]");
                renderer.highlightCell(i, j, Color.rgb(255, 183, 77));
                updateStepInfo("Traceback", "Finding the actual LCS sequence");
                delay(900);  // 600 * 1.5
                break;
                
            case TRACEBACK_CHAR_ADDED:
                char added = (char) meta.get("charAdded");
                appendProgress("📝 Added '" + added + "' to LCS (moving diagonal)");
                renderer.highlightCell(i, j, Color.rgb(255, 183, 77));
                renderer.setArrow(i, j, "↖");
                updateStepInfo("Character Added", "'" + added + "' is part of LCS");
                delay(750);  // 500 * 1.5
                break;
                
            case TRACEBACK_STEP:
                String direction = (String) meta.get("direction");
                String arrow = direction.equals("up") ? "↑" : "←";
                appendProgress("➡️  Moving " + direction + " from [" + i + "][" + j + "]");
                renderer.highlightCell(i, j, Color.rgb(255, 183, 77));
                renderer.setArrow(i, j, arrow);
                delay(600);  // 400 * 1.5
                break;
                
            case TRACEBACK_COMPLETE:
                List<Character> lcs = (List<Character>) meta.get("lcs");
                int length = (int) meta.get("length");
                StringBuilder lcsStr = new StringBuilder();
                for (char c : lcs) lcsStr.append(c);
                
                appendProgress("\n" + "═".repeat(50));
                appendProgress("🎉 LCS Found: \"" + lcsStr + "\"");
                appendProgress("Length: " + length);
                appendProgress("═".repeat(50));
                updateStepInfo("Complete!", "LCS: \"" + lcsStr + "\"");
                updateLCSResult();
                delay(1800);  // 1200 * 1.5
                break;
                
            case COMPLETE:
                appendProgress("✅ Algorithm complete!");
                delay(750);  // 500 * 1.5
                break;
        }
        
        renderVisualization();
        updateVariablesPanel();
        updateLCSResult();
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
    
    private void updateLCSResult() {
        if (lcsResultLabel == null || lcsLengthLabel == null) return;
        
        List<Character> lcs = solver.getLCS();
        if (lcs.isEmpty()) {
            lcsResultLabel.setText("LCS: (computing...)");
            lcsLengthLabel.setText("Length: 0");
        } else {
            StringBuilder lcsStr = new StringBuilder();
            for (char c : lcs) lcsStr.append(c);
            lcsResultLabel.setText("LCS: \"" + lcsStr + "\"");
            lcsLengthLabel.setText("Length: " + lcs.size());
        }
    }
    
    private void renderCode() {
        if (parent == null) return;
        
        // Update LCS code with current parameters
        com.algorithmvisualizer.code.AlgorithmCode code = 
            com.algorithmvisualizer.code.CodeRepository.getCode("Longest Common Subsequence");
        
        if (code instanceof com.algorithmvisualizer.code.implementations.LCSCode) {
            com.algorithmvisualizer.code.implementations.LCSCode lcsCode = 
                (com.algorithmvisualizer.code.implementations.LCSCode) code;
            lcsCode.updateParameters(str1, str2);
        }
        
        // Notify parent to reload code for current language
        parent.loadCodeForCurrentLanguage();
    }
    
    private void initProgressLog() {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.clear();
            parent.progressArea.appendText("=== Longest Common Subsequence ===\n");
            parent.progressArea.appendText("Ready to start. Click Play to begin.\n\n");
        }
    }
    
    private void appendProgress(String message) {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.appendText(message + "\n");
        }
    }
    
    private void updateVariablesPanel() {
        if (parent != null && parent.variableList != null) {
            ObservableList<String> vars = FXCollections.observableArrayList();
            vars.add("String 1: \"" + str1 + "\"");
            vars.add("String 2: \"" + str2 + "\"");
            vars.add("Length 1: " + str1.length());
            vars.add("Length 2: " + str2.length());
            vars.add("Table Size: " + (str1.length() + 1) + " × " + (str2.length() + 1));
            
            int[][] dp = solver.getDP();
            if (dp != null && dp.length > 0 && dp[0].length > 0) {
                vars.add("LCS Length: " + dp[str1.length()][str2.length()]);
            }
            
            List<Character> lcs = solver.getLCS();
            if (!lcs.isEmpty()) {
                StringBuilder lcsStr = new StringBuilder();
                for (char c : lcs) lcsStr.append(c);
                vars.add("LCS: \"" + lcsStr + "\"");
            }
            
            parent.variableList.setItems(vars);
        }
    }
}
