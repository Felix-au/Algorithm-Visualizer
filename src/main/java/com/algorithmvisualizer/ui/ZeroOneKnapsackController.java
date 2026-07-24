package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.ZeroOneKnapsackSolver;
import com.algorithmvisualizer.algorithm.ZeroOneKnapsackSolver.Item;
import com.algorithmvisualizer.model.ZeroOneKnapsackAlgorithm;
import com.algorithmvisualizer.visualization.ZeroOneKnapsackTableRenderer;
import com.algorithmvisualizer.code.implementations.ZeroOneKnapsackCode;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;

/**
 * Controller for 0/1 Knapsack visualization.
 * Uses DP table renderer (like LCS), parameters matching Fractional Knapsack,
 * no capacity meter, rich step-by-step animation.
 */
public class ZeroOneKnapsackController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;
    private ZeroOneKnapsackAlgorithm algorithm;
    private ZeroOneKnapsackSolver solver;
    private ZeroOneKnapsackTableRenderer renderer;

    // Parameters — same as Fractional Knapsack
    private Item[] currentItems;
    private int currentCapacity = 10;

    // UI Components
    private VBox stepInfoBox;
    private Label resultLabel;
    private Label resultDetailLabel;

    // State
    private Stack<ZeroOneKnapsackSolver.State> history;
    private Timeline timeline;
    private long stepDelayUntilMs = 0L;

    // Colors
    private static final Color COLOR_CURRENT = Color.rgb(255, 235, 59); // Yellow
    private static final Color COLOR_COMPARING = Color.rgb(144, 202, 249); // Light blue
    private static final Color COLOR_INCLUDE = Color.rgb(76, 175, 80); // Green
    private static final Color COLOR_EXCLUDE = Color.rgb(239, 154, 154); // Light red
    private static final Color COLOR_DEPENDENCY = Color.rgb(179, 229, 252); // Sky blue
    private static final Color COLOR_PATH = Color.rgb(255, 183, 77); // Gold

    @FXML
    private void initialize() {
        // Initialize default items with low weights
        currentItems = new Item[] {
                new Item(1, 2, 12),
                new Item(2, 1, 10),
                new Item(3, 3, 20),
                new Item(4, 2, 15)
        };

        Canvas canvas = new Canvas(800, 600);
        renderer = new ZeroOneKnapsackTableRenderer(canvas);

        // Initialize solver
        solver = new ZeroOneKnapsackSolver();
        solver.setItems(currentItems, currentCapacity);
        solver.setStepListener(this::onStepEvent);
        solver.reset();

        history = new Stack<>();

        // Set up renderer
        int[] weights = new int[currentItems.length];
        int[] values = new int[currentItems.length];
        for (int i = 0; i < currentItems.length; i++) {
            weights[i] = currentItems[i].weight;
            values[i] = currentItems[i].value;
        }
        renderer.setParameters(currentItems.length, currentCapacity, weights, values);
        renderer.setTable(solver.getDP());
        renderer.render();
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Register code immediately
        new ZeroOneKnapsackCode();

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
            Label hdr = new Label("0/1 Knapsack — DP Table");
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
            HBox row = new HBox(12.0);

            javafx.scene.shape.Rectangle rCurrent = new javafx.scene.shape.Rectangle(18, 18, COLOR_CURRENT);
            HBox lCurrent = new HBox(4.0, rCurrent, new Label("Current"));

            javafx.scene.shape.Rectangle rInclude = new javafx.scene.shape.Rectangle(18, 18, COLOR_INCLUDE);
            HBox lInclude = new HBox(4.0, rInclude, new Label("Include"));

            javafx.scene.shape.Rectangle rDep = new javafx.scene.shape.Rectangle(18, 18, COLOR_DEPENDENCY);
            HBox lDep = new HBox(4.0, rDep, new Label("Dependency"));

            row.getChildren().addAll(lCurrent, lInclude, lDep);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        setupParameterControls();
        buildSolutionsPane();

        // Speed slider
        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        // Initialize
        parent.setCurrentAlgorithmName("0/1 Knapsack");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderVisualization();
        if (parent.stepDescription != null) {
            parent.stepDescription.setText("Ready.");
        }
    }

    private void setupParameterControls() {
        // Items count (reuse paramBoardSizeSpinner like Fractional Knapsack)
        if (parent.paramSizeLabel != null) {
            parent.paramSizeLabel.setText("Items:");
            parent.paramSizeLabel.setVisible(true);
            parent.paramSizeLabel.setManaged(true);
        }
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, currentItems.length));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> {
                onNumItemsChanged(n);
                refreshAll();
            });
        }

        // Capacity (reuse paramNumQueensSpinner like Fractional Knapsack)
        if (parent.paramTargetLabel != null) {
            parent.paramTargetLabel.setText("Capacity:");
            parent.paramTargetLabel.setVisible(true);
            parent.paramTargetLabel.setManaged(true);
        }
        if (parent.paramNumQueensSpinner != null) {
            parent.paramNumQueensSpinner.setVisible(true);
            parent.paramNumQueensSpinner.setManaged(true);
            parent.paramNumQueensSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, currentCapacity));
            parent.paramNumQueensSpinner.valueProperty().addListener((obs, o, n) -> {
                currentCapacity = n;
                refreshAll();
            });
        }

        // Items field (W,V pairs)
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setText("Items (W,V):");
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            updateElementsField();
            parent.paramElementsField.setOnAction(e -> parseElementsField());
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }

        // Randomize button
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setVisible(true);
            parent.paramRandomizeButton.setManaged(true);
            parent.paramRandomizeButton.setText("Randomize");
            parent.paramRandomizeButton.setOnAction(e -> {
                onRandomize();
                updateElementsField();
                refreshAll();
            });
            parent.paramRandomizeButton.setStyle(
                    "-fx-background-color: #0ea5e9; -fx-text-fill: white; " +
                            "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        }

        // Apply button
        if (parent.paramApplyButton != null) {
            parent.paramApplyButton.setVisible(true);
            parent.paramApplyButton.setManaged(true);
            parent.paramApplyButton.setOnAction(e -> {
                parseElementsField();
                refreshAll();
            });
            parent.paramApplyButton.setStyle(
                    "-fx-background-color: #10b981; -fx-text-fill: white; " +
                            "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        }
    }

    private void buildSolutionsPane() {
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label header = new Label("0/1 Knapsack Details");
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().add(header);
        }

        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }

        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();

            VBox container = new VBox(15);
            container.setPrefWidth(Double.MAX_VALUE);

            // Input info block
            VBox inputBlock = new VBox(8);
            inputBlock.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 8; " +
                    "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;");
            Label inputHeader = new Label("📦 Problem Setup");
            inputHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");

            StringBuilder itemsStr = new StringBuilder();
            for (Item item : currentItems) {
                itemsStr.append(String.format("Item %d: w=%d, v=%d\n", item.id, item.weight, item.value));
            }
            Label itemsLabel = new Label(itemsStr.toString().trim());
            itemsLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-text-fill: #212529;");
            itemsLabel.setWrapText(true);

            Label capLabel = new Label("Capacity: " + currentCapacity);
            capLabel.setStyle(
                    "-fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #212529;");

            inputBlock.getChildren().addAll(inputHeader, itemsLabel, capLabel);

            // Current step info block
            VBox stepBlock = new VBox(8);
            stepBlock.setStyle("-fx-background-color: #fff3cd; -fx-padding: 12; -fx-background-radius: 8; " +
                    "-fx-border-color: #ffc107; -fx-border-width: 2; -fx-border-radius: 8;");
            Label stepHeader = new Label("⚡ Current Step");
            stepHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #856404;");

            stepInfoBox = new VBox(6);
            stepInfoBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");
            Label placeholder = new Label("Waiting to start...");
            placeholder.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
            stepInfoBox.getChildren().add(placeholder);

            stepBlock.getChildren().addAll(stepHeader, stepInfoBox);

            // Result block
            VBox resultBlock = new VBox(8);
            resultBlock.setStyle("-fx-background-color: #d4edda; -fx-padding: 12; -fx-background-radius: 8; " +
                    "-fx-border-color: #28a745; -fx-border-width: 2; -fx-border-radius: 8;");
            Label resultHeader = new Label("🎯 Result");
            resultHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #155724;");

            resultLabel = new Label("Max Value: (not computed yet)");
            resultLabel.setStyle(
                    "-fx-font-family: 'Courier New'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #155724;");
            resultLabel.setWrapText(true);

            resultDetailLabel = new Label("Selected items: —");
            resultDetailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #155724;");
            resultDetailLabel.setWrapText(true);

            resultBlock.getChildren().addAll(resultHeader, resultLabel, resultDetailLabel);

            container.getChildren().addAll(inputBlock, stepBlock, resultBlock);
            parent.solutionsContent.getChildren().add(container);
        }
    }

    // ======================== Randomize / Parse ========================

    private void onRandomize() {
        Random rnd = new Random();
        for (Item item : currentItems) {
            item.weight = 1 + rnd.nextInt(5); // 1–5 (low weights)
            item.value = 5 + rnd.nextInt(26); // 5–30
        }
    }

    private void onNumItemsChanged(int n) {
        if (n < 1)
            n = 1;
        if (n > 8)
            n = 8;
        Item[] newItems = new Item[n];
        for (int i = 0; i < n; i++) {
            if (i < currentItems.length) {
                newItems[i] = new Item(currentItems[i]);
                newItems[i].id = i + 1;
            } else {
                newItems[i] = new Item(i + 1, 1 + new Random().nextInt(5), 5 + new Random().nextInt(26));
            }
        }
        currentItems = newItems;
        updateElementsField();
    }

    private void updateElementsField() {
        if (parent == null || parent.paramElementsField == null)
            return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentItems.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(currentItems[i].weight).append(",").append(currentItems[i].value);
        }
        parent.paramElementsField.setText(sb.toString());
    }

    private void parseElementsField() {
        if (parent == null || parent.paramElementsField == null)
            return;
        String text = parent.paramElementsField.getText().trim();
        if (text.isEmpty())
            return;

        try {
            String[] pairs = text.split(",\\s*");
            if (pairs.length % 2 != 0)
                return;

            int numItems = pairs.length / 2;
            Item[] newItems = new Item[numItems];
            for (int i = 0; i < numItems; i++) {
                int weight = Integer.parseInt(pairs[i * 2].trim());
                int value = Integer.parseInt(pairs[i * 2 + 1].trim());
                newItems[i] = new Item(i + 1, weight, value);
            }
            currentItems = newItems;

            if (parent.paramBoardSizeSpinner != null) {
                parent.paramBoardSizeSpinner.getValueFactory().setValue(numItems);
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }

    // ======================== Refresh / Render ========================

    private void refreshAll() {
        stopTimeline();
        history.clear();

        solver = new ZeroOneKnapsackSolver();
        solver.setItems(currentItems, currentCapacity);
        solver.setStepListener(this::onStepEvent);
        solver.reset();

        int[] weights = new int[currentItems.length];
        int[] values = new int[currentItems.length];
        for (int i = 0; i < currentItems.length; i++) {
            weights[i] = currentItems[i].weight;
            values[i] = currentItems[i].value;
        }
        renderer.setParameters(currentItems.length, currentCapacity, weights, values);
        renderer.setTable(solver.getDP());

        buildSolutionsPane();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        renderVisualization();

        if (parent.stepDescription != null) {
            parent.stepDescription.setText("Ready.");
        }
    }

    private void renderVisualization() {
        renderer.setTable(solver.getDP());
        renderer.setPathCells(solver.getPathCells());
        renderer.render();
    }

    // ======================== Playback ========================

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
            ZeroOneKnapsackSolver.State prevState = history.pop();
            solver.restore(prevState);
            renderer.clearHighlights();
            renderVisualization();
            updateVariablesPanel();
            updateResult();
        }
    }

    @Override
    public void onReset() {
        stopTimeline();
        history.clear();
        solver.reset();
        renderer.clearAll();
        renderVisualization();
        initProgressLog();
        updateVariablesPanel();
        updateResult();
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
            if (now < stepDelayUntilMs)
                return;

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

    // ======================== Step Event Handler ========================

    private void onStepEvent(ZeroOneKnapsackSolver.StepType type, int i, int w, Map<String, Object> meta) {
        renderer.clearHighlights();

        switch (type) {
            case INIT:
                appendProgress("🎒 0/1 Knapsack Problem");
                appendProgress("   Items: " + currentItems.length + ", Capacity: " + currentCapacity);
                appendProgress("   Building DP table...");
                appendProgress("");
                updateStepInfo("Initializing",
                        "Setting up DP table dp[" + (currentItems.length + 1) + "][" + (currentCapacity + 1) + "]");
                delay(1800);
                break;

            case HIGHLIGHT_CELL: {
                int itemId = (int) meta.get("itemId");
                int itemW = (int) meta.get("itemWeight");
                int itemV = (int) meta.get("itemValue");
                int cap = (int) meta.get("capacity");
                appendProgress("📍 Cell [" + i + "][" + w + "] — Item " + itemId + " (w=" + itemW + ", v=" + itemV
                        + "), capacity=" + cap);
                renderer.highlightCell(i, w, COLOR_CURRENT);
                updateStepInfo("Cell [" + i + "][" + w + "]",
                        "Item " + itemId + ": weight=" + itemW + ", value=" + itemV + "\nCapacity: " + cap);
                delay(700);
                break;
            }

            case CHECK_ITEM_WEIGHT: {
                int itemW = (int) meta.get("itemWeight");
                int cap = (int) meta.get("capacity");
                boolean fits = (boolean) meta.get("fits");
                if (fits) {
                    appendProgress("   ✓ Item weight " + itemW + " ≤ capacity " + cap + " → can include or exclude");
                } else {
                    appendProgress("   ✗ Item weight " + itemW + " > capacity " + cap + " → must exclude");
                }
                renderer.highlightCell(i, w, fits ? COLOR_COMPARING : COLOR_EXCLUDE);
                updateStepInfo("Weight Check",
                        "Item weight: " + itemW + "\nAvailable capacity: " + cap + "\n"
                                + (fits ? "✓ Item can fit" : "✗ Too heavy"));
                delay(900);
                break;
            }

            case ITEM_TOO_HEAVY: {
                int excludeVal = (int) meta.get("excludeValue");
                int val = (int) meta.get("value");
                appendProgress("   → dp[" + i + "][" + w + "] = dp[" + (i - 1) + "][" + w + "] = " + val);
                renderer.highlightCell(i, w, COLOR_EXCLUDE);
                renderer.highlightCell(i - 1, w, COLOR_DEPENDENCY);
                renderer.setArrow(i, w, "↑");
                updateStepInfo("Item Too Heavy",
                        "Can only exclude → copy from above\ndp[" + (i - 1) + "][" + w + "] = " + excludeVal);
                delay(1100);
                break;
            }

            case SHOW_EXCLUDE_OPTION: {
                int excludeVal = (int) meta.get("excludeValue");
                appendProgress("   Option 1 (exclude): dp[" + (i - 1) + "][" + w + "] = " + excludeVal);
                renderer.highlightCell(i - 1, w, COLOR_DEPENDENCY);
                renderer.highlightCell(i, w, COLOR_CURRENT);
                updateStepInfo("Exclude Option",
                        "Don't take item → dp[" + (i - 1) + "][" + w + "] = " + excludeVal);
                delay(900);
                break;
            }

            case SHOW_INCLUDE_OPTION: {
                int includeVal = (int) meta.get("includeValue");
                int prevVal = (int) meta.get("prevValue");
                int itemVal = (int) meta.get("itemValue");
                int lookupW = (int) meta.get("lookupW");
                appendProgress("   Option 2 (include): dp[" + (i - 1) + "][" + lookupW + "] + " + itemVal + " = "
                        + prevVal + " + " + itemVal + " = " + includeVal);
                renderer.highlightCell(i - 1, lookupW, COLOR_DEPENDENCY);
                renderer.highlightCell(i, w, COLOR_CURRENT);
                updateStepInfo("Include Option",
                        "Take item → dp[" + (i - 1) + "][" + lookupW + "] + " + itemVal +
                                "\n= " + prevVal + " + " + itemVal + " = " + includeVal);
                delay(900);
                break;
            }

            case COMPARE_OPTIONS: {
                int excludeVal = (int) meta.get("excludeValue");
                int includeVal = (int) meta.get("includeValue");
                String chosen = (String) meta.get("chosen");
                int val = (int) meta.get("value");
                appendProgress("   → max(" + excludeVal + ", " + includeVal + ") = " + val + " (" + chosen + ")");
                renderer.highlightCell(i, w, "include".equals(chosen) ? COLOR_INCLUDE : COLOR_COMPARING);
                updateStepInfo("Compare & Choose",
                        "Exclude: " + excludeVal + "\nInclude: " + includeVal +
                                "\n→ Choose " + chosen + ": " + val);
                delay(1100);
                break;
            }

            case UPDATE_CELL: {
                int val = (int) meta.get("value");
                appendProgress("   ✅ dp[" + i + "][" + w + "] = " + val);
                renderer.highlightCell(i, w, COLOR_INCLUDE);
                delay(500);
                break;
            }

            case ROW_COMPLETE: {
                int row = (int) meta.get("row");
                appendProgress("📊 Row " + row + " complete");
                appendProgress("");
                delay(500);
                break;
            }

            case TABLE_COMPLETE:
                appendProgress("═".repeat(50));
                appendProgress("✅ DP table construction complete!");
                appendProgress("Maximum value: " + solver.getDP()[solver.getN()][solver.getCapacity()]);
                appendProgress("═".repeat(50));
                appendProgress("");
                updateStepInfo("Table Complete", "Starting traceback to find selected items");
                delay(2000);
                break;

            case TRACEBACK_START:
                appendProgress("🔙 Starting traceback from [" + i + "][" + w + "]");
                renderer.highlightCell(i, w, COLOR_CURRENT);
                updateStepInfo("Traceback",
                        "Finding which items were selected\nStarting from dp[" + i + "][" + w + "]");
                delay(1400);
                break;

            case TRACEBACK_HIGHLIGHT_CELL: {
                int cellVal = (int) meta.get("cellValue");
                int aboveVal = (int) meta.get("aboveValue");
                int itemId = (int) meta.get("itemId");
                appendProgress("🔍 Checking cell [" + i + "][" + w + "] = " + cellVal + " vs above = " + aboveVal);
                renderer.highlightCell(i, w, COLOR_CURRENT);
                renderer.highlightCell(i - 1, w, COLOR_DEPENDENCY);
                updateStepInfo("Traceback: Cell [" + i + "][" + w + "]",
                        "Item " + itemId + "\nCurrent: " + cellVal + "\nAbove: " + aboveVal +
                                "\n" + (cellVal != aboveVal ? "→ Values differ → item was taken!"
                                        : "→ Same → item was not taken"));
                delay(1200);
                break;
            }

            case TRACEBACK_ITEM_SELECTED: {
                int itemId = (int) meta.get("itemId");
                int itemW = (int) meta.get("itemWeight");
                int itemV = (int) meta.get("itemValue");
                appendProgress("📝 Item " + itemId + " SELECTED (w=" + itemW + ", v=" + itemV + ")");
                renderer.highlightCell(i, w, COLOR_INCLUDE);
                renderer.setArrow(i, w, "↖");
                // Highlight corresponding row header
                renderer.highlightRowHeader(i);
                updateStepInfo("✅ Item Selected!",
                        "Item " + itemId + " (w=" + itemW + ", v=" + itemV + ") is in optimal set");
                delay(1400);
                break;
            }

            case TRACEBACK_ITEM_SKIPPED: {
                int itemId = (int) meta.get("itemId");
                appendProgress("   Item " + itemId + " skipped (moving up)");
                renderer.highlightCell(i, w, COLOR_CURRENT);
                renderer.setArrow(i, w, "↑");
                updateStepInfo("Item Skipped",
                        "Item " + itemId + " was not selected → move up");
                delay(1000);
                break;
            }

            case TRACEBACK_STEP:
                // Intermediate traceback step — visual handled by highlight/selected/skipped
                break;

            case TRACEBACK_COMPLETE: {
                @SuppressWarnings("unchecked")
                List<Integer> selected = (List<Integer>) meta.get("selectedItems");
                int totalVal = (int) meta.get("totalValue");
                int totalWeight = (int) meta.get("totalWeight");
                appendProgress("");
                appendProgress("═".repeat(50));
                appendProgress("🎉 Optimal Solution Found!");
                appendProgress("   Max Value: " + totalVal);
                appendProgress("   Total Weight: " + totalWeight + "/" + currentCapacity);
                StringBuilder selStr = new StringBuilder("   Selected: ");
                for (int idx : selected) {
                    selStr.append("Item ").append(currentItems[idx].id).append(" ");
                }
                appendProgress(selStr.toString());
                appendProgress("═".repeat(50));
                updateStepInfo("Complete!", "Max Value: " + totalVal);
                updateResult();
                delay(2400);
                break;
            }

            case COMPLETE:
                appendProgress("✅ Algorithm complete!");
                delay(1000);
                break;
        }

        renderVisualization();
        updateVariablesPanel();
        updateResult();
    }

    private void delay(long ms) {
        stepDelayUntilMs = System.currentTimeMillis() + ms;
    }

    // ======================== UI Updates ========================

    private void updateStepInfo(String title, String details) {
        if (stepInfoBox == null)
            return;
        stepInfoBox.getChildren().clear();

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-font-size: 11px;");
        detailsLabel.setWrapText(true);

        stepInfoBox.getChildren().addAll(titleLabel, detailsLabel);
    }

    private void updateResult() {
        if (resultLabel == null || resultDetailLabel == null)
            return;

        int[][] dp = solver.getDP();
        if (dp != null && solver.getN() > 0 && solver.getCapacity() >= 0) {
            int maxVal = dp[solver.getN()][solver.getCapacity()];
            resultLabel.setText("Max Value: " + maxVal);
        }

        List<Integer> selected = solver.getSelectedItems();
        if (selected.isEmpty()) {
            resultDetailLabel.setText("Selected items: (computing...)");
        } else {
            StringBuilder sb = new StringBuilder("Selected: ");
            int totalWeight = 0;
            for (int idx : selected) {
                Item item = currentItems[idx];
                sb.append("Item ").append(item.id).append(" ");
                totalWeight += item.weight;
            }
            sb.append("\nWeight: ").append(totalWeight).append("/").append(currentCapacity);
            resultDetailLabel.setText(sb.toString());
        }
    }

    private void renderCode() {
        if (parent == null)
            return;

        com.algorithmvisualizer.code.AlgorithmCode code = com.algorithmvisualizer.code.CodeRepository
                .getCode("0/1 Knapsack");

        if (code instanceof ZeroOneKnapsackCode) {
            ZeroOneKnapsackCode knapsackCode = (ZeroOneKnapsackCode) code;
            knapsackCode.updateParameters(currentItems, currentCapacity);
        }

        parent.loadCodeForCurrentLanguage();
    }

    private void initProgressLog() {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.clear();
            parent.progressArea.appendText("=== 0/1 Knapsack Problem ===\n");
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
            vars.add("Items: " + currentItems.length);
            vars.add("Capacity: " + currentCapacity);

            StringBuilder itemsStr = new StringBuilder("[");
            for (int i = 0; i < Math.min(4, currentItems.length); i++) {
                if (i > 0)
                    itemsStr.append(", ");
                itemsStr.append("(w=").append(currentItems[i].weight)
                        .append(",v=").append(currentItems[i].value).append(")");
            }
            if (currentItems.length > 4)
                itemsStr.append("...");
            itemsStr.append("]");
            vars.add("items: " + itemsStr.toString());

            int[][] dp = solver.getDP();
            if (dp != null && solver.getN() > 0) {
                vars.add("Table: " + (solver.getN() + 1) + " × " + (solver.getCapacity() + 1));
                vars.add("Max Value: " + dp[solver.getN()][solver.getCapacity()]);
            }

            List<Integer> selected = solver.getSelectedItems();
            if (!selected.isEmpty()) {
                StringBuilder selStr = new StringBuilder("Selected: ");
                for (int idx : selected) {
                    selStr.append("I").append(currentItems[idx].id).append(" ");
                }
                vars.add(selStr.toString().trim());
            }

            vars.add("State: " + (solver.isDone() ? "DONE" : "RUNNING"));

            parent.variableList.setItems(vars);
        }
    }
}
