package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.FractionalKnapsackSolver;
import com.algorithmvisualizer.algorithm.FractionalKnapsackSolver.Item;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.code.implementations.FractionalKnapsackCode;
import com.algorithmvisualizer.visualization.ItemBarChart;
import com.algorithmvisualizer.visualization.CapacityMeter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.converter.DoubleStringConverter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Fractional Knapsack controller with item bar chart and capacity meter
 */
public class FractionalKnapsackController implements AlgorithmViewController.AlgorithmSpecificController {

    @FXML private Spinner<Integer> numItemsSpinner;
    @FXML private Spinner<Double> capacitySpinner;
    @FXML private TableView<Item> itemsTable;
    @FXML private Button randomizeButton;
    @FXML private Button applyButton;
    @FXML private Label statusLabel;

    private AlgorithmViewController parent;
    private ItemBarChart itemChart;
    private CapacityMeter capacityMeter;
    private FractionalKnapsackSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;
    private final Deque<FractionalKnapsackSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;
    private boolean waitingForDelay = false;
    private PauseTransition delayTransition;

    private Item[] currentItems = new Item[]{
        new Item(1, 10, 60),
        new Item(2, 20, 100),
        new Item(3, 30, 120)
    };
    private double currentCapacity = 50.0;

    @FXML
    private void initialize() {
        if (numItemsSpinner != null) {
            numItemsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, currentItems.length));
            numItemsSpinner.valueProperty().addListener((obs, o, n) -> onNumItemsChanged(n));
        }
        if (capacitySpinner != null) {
            capacitySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 200.0, currentCapacity, 5.0));
        }
        if (randomizeButton != null) randomizeButton.setOnAction(e -> onRandomize());
        if (applyButton != null) applyButton.setOnAction(e -> onApply());

        // Setup items table
        setupItemsTable();

        // Create visualizations
        itemChart = new ItemBarChart();
        capacityMeter = new CapacityMeter();

        // Create solver
        solver = new FractionalKnapsackSolver(currentItems, currentCapacity);
        solver.setStepListener(this::onStepEvent);

        renderVisuals();
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place item chart in chessboard container
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(itemChart.getNode());
        }

        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        }

        // Parameters panel integration - Use existing controls
        if (parent.paramSizeLabel != null) {
            parent.paramSizeLabel.setText("Items:");
            parent.paramSizeLabel.setVisible(true);
            parent.paramSizeLabel.setManaged(true);
        }
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, currentItems.length));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> {
                onNumItemsChanged(n);
                refreshAll();
            });
        }
        if (parent.paramTargetLabel != null) {
            parent.paramTargetLabel.setText("Capacity:");
            parent.paramTargetLabel.setVisible(true);
            parent.paramTargetLabel.setManaged(true);
        }
        if (parent.paramNumQueensSpinner != null) {
            parent.paramNumQueensSpinner.setVisible(true);
            parent.paramNumQueensSpinner.setManaged(true);
            parent.paramNumQueensSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, (int)currentCapacity));
            parent.paramNumQueensSpinner.valueProperty().addListener((obs, o, n) -> {
                currentCapacity = n.doubleValue();
                refreshAll();
            });
        }
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
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setVisible(true);
            parent.paramRandomizeButton.setManaged(true);
            parent.paramRandomizeButton.setOnAction(e -> {
                onRandomize();
                updateElementsField();
                refreshAll(); // Auto-apply
            });
        }
        if (parent.paramApplyButton != null) {
            parent.paramApplyButton.setVisible(true);
            parent.paramApplyButton.setManaged(true);
            parent.paramApplyButton.setOnAction(e -> {
                parseElementsField();
                refreshAll();
            });
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }

        // Configure header and legend
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label chartHeader = new Label("Fractional Knapsack - Items");
            chartHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(chartHeader, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(false);
            parent.chessboardLegendBox.setManaged(false);
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        parent.setCurrentAlgorithmName("Fractional Knapsack");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        updateElementsField();

        // Place capacity meter in solutions panel
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Knapsack Status");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            parent.solutionsContent.getChildren().add(capacityMeter.getNode());
        }
    }

    private void setupItemsTable() {
        if (itemsTable == null) return;

        itemsTable.setEditable(true);

        TableColumn<Item, Number> idCol = new TableColumn<>("Item");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().id));
        idCol.setPrefWidth(50);

        TableColumn<Item, Double> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().weight).asObject());
        weightCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        weightCol.setOnEditCommit(event -> {
            event.getRowValue().weight = event.getNewValue().doubleValue();
        });
        weightCol.setPrefWidth(80);
        weightCol.setEditable(true);

        TableColumn<Item, Double> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().value).asObject());
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        valueCol.setOnEditCommit(event -> {
            event.getRowValue().value = event.getNewValue().doubleValue();
        });
        valueCol.setPrefWidth(80);
        valueCol.setEditable(true);

        itemsTable.getColumns().clear();
        itemsTable.getColumns().addAll(idCol, weightCol, valueCol);
        itemsTable.getItems().clear();
        for (Item item : currentItems) {
            itemsTable.getItems().add(item);
        }
    }

    private void onStepEvent(FractionalKnapsackSolver.StepType type, FractionalKnapsackSolver.State state) {
        countLogsForStep = true;
        currentStepLogLines = 0;

        switch (type) {
            case INIT:
                appendProgress("🎒 Fractional Knapsack");
                appendProgress("   Items: " + state.items.length + ", Capacity: " + String.format("%.1f", state.capacity));
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Initializing...");
                itemChart.hidePointer();
                capacityMeter.clearBlink();
                break;

            case CALCULATE_RATIOS:
                appendProgress("📊 CALCULATE VALUE/WEIGHT RATIOS");
                for (Item item : state.items) {
                    appendProgress(String.format("   Item %d: W=%.1f, V=%.1f → Ratio = %.2f", 
                        item.id, item.weight, item.value, item.ratio));
                }
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Calculating ratios...");
                // Update bar chart to show ratios immediately
                itemChart.updateData(state.items);
                break;

            case DISPLAY_RATIOS:
                appendProgress("✓ Ratios calculated");
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Ratios displayed");
                break;

            case SORT_START:
                appendProgress("🔄 SORTING BY RATIO (Descending)");
                if (parent != null) parent.stepDescription.setText("Sorting items...");
                break;

            case COMPARE_RATIOS:
                if (state.compareIndex1 >= 0 && state.compareIndex2 < state.items.length) {
                    itemChart.highlightCompare(state.compareIndex1, state.compareIndex2);
                }
                break;

            case SWAP_HIGHLIGHT:
                // Highlight items in red before swap - START BLINKING
                if (state.compareIndex1 >= 0 && state.compareIndex2 < state.items.length) {
                    startSwapBlinking(state.compareIndex1, state.compareIndex2);
                }
                break;

            case SWAP_ITEMS:
                // Swap happens during blinking animation
                break;

            case SWAP_COMPLETE:
                // Clear swap highlights after blinking completes
                itemChart.clearHighlights();
                break;

            case SORT_COMPLETE:
                appendProgress("✓ Sorting complete - items ordered by ratio");
                appendProgress("   Sorted order: ");
                StringBuilder sortedOrder = new StringBuilder("   [");
                for (int i = 0; i < state.items.length; i++) {
                    if (i > 0) sortedOrder.append(", ");
                    sortedOrder.append(String.format("Item%d(R=%.2f)", state.items[i].id, state.items[i].ratio));
                }
                sortedOrder.append("]");
                appendProgress(sortedOrder.toString());
                appendProgress("");
                appendProgress("━━━ GREEDY SELECTION ━━━");
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Sorting complete");
                itemChart.clearHighlights();
                break;

            case SELECT_ITEM:
                Item current = state.items[state.currentIndex];
                appendProgress(String.format("🎯 SELECT Item %d (W=%.1f, V=%.1f, R=%.2f)", 
                    current.id, current.weight, current.value, current.ratio));
                appendProgress(String.format("   Remaining capacity: %.1f units", state.remainingCapacity));
                if (parent != null) parent.stepDescription.setText("Selecting item " + current.id);
                itemChart.showPointer(state.currentIndex);
                break;

            case HOVER_ITEM:
                // Hover pointer over item for 1 sec
                scheduleDelay(1000);
                break;

            case CHECK_CAPACITY:
                Item checking = state.items[state.currentIndex];
                if (checking.weight <= state.remainingCapacity) {
                    appendProgress(String.format("   ✓ Weight %.1f ≤ %.1f → TAKE FULL ITEM", 
                        checking.weight, state.remainingCapacity));
                } else if (state.remainingCapacity > 0.0001) {
                    appendProgress(String.format("   ⚠️ Weight %.1f > %.1f → TAKE PARTIAL ITEM", 
                        checking.weight, state.remainingCapacity));
                } else {
                    appendProgress("   ✗ No capacity left → SKIP ITEM");
                }
                break;

            case TAKE_FULL:
                Item full = state.items[state.currentIndex];
                appendProgress(String.format("   Value gained: $%.2f", full.valueGained));
                appendProgress(String.format("   Capacity used: %.1f/%.1f (%.1f%%)", 
                    state.capacity - state.remainingCapacity, state.capacity, 
                    ((state.capacity - state.remainingCapacity) / state.capacity) * 100));
                break;

            case TAKE_PARTIAL:
                Item partial = state.items[state.currentIndex];
                appendProgress(String.format("   Fraction: %.1f/%.1f = %.4f (%.2f%%)", 
                    state.remainingCapacity, partial.weight, partial.fractionTaken, partial.fractionTaken * 100));
                appendProgress(String.format("   Value gained: $%.2f × %.4f = $%.2f", 
                    partial.value, partial.fractionTaken, partial.valueGained));
                appendProgress(String.format("   Capacity used: %.1f/%.1f (100%%)", state.capacity, state.capacity));
                break;

            case UPDATE_KNAPSACK:
                capacityMeter.updateWithItems(state.capacity, state.capacity - state.remainingCapacity, state.totalValue, state.items);
                appendProgress(String.format("   📦 Total value so far: $%.2f", state.totalValue));
                // Wait 1 second after adding item
                scheduleDelay(1000);
                break;

            case MARK_COMPLETE:
                appendProgress("");
                itemChart.updateData(state.items);
                itemChart.hidePointer();
                break;

            case DONE:
                appendProgress("🎉 KNAPSACK COMPLETE!");
                appendProgress(String.format("✓ Total Value: $%.2f", state.totalValue));
                appendProgress(String.format("✓ Capacity Used: %.1f/%.1f (%.1f%%)", 
                    state.capacity - state.remainingCapacity, state.capacity,
                    ((state.capacity - state.remainingCapacity) / state.capacity) * 100));
                int fullCount = 0, partialCount = 0;
                for (Item item : state.items) {
                    if (item.fractionTaken >= 0.9999) fullCount++;
                    else if (item.fractionTaken > 0) partialCount++;
                }
                appendProgress(String.format("✓ Items Taken: %d full + %d partial", fullCount, partialCount));
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("🎉 Complete!");
                itemChart.hidePointer();
                capacityMeter.clearBlink();
                stopTimeline();
                break;
        }

        updateVariablesPanel();
        if (countLogsForStep) {
            progressHistory.push(currentStepLogLines);
            countLogsForStep = false;
            currentStepLogLines = 0;
        }
    }
    
    private void scheduleDelay(int milliseconds) {
        if (!isPlaying) return; // Only delay during playback
        
        waitingForDelay = true;
        if (delayTransition != null) {
            delayTransition.stop();
        }
        delayTransition = new PauseTransition(Duration.millis(milliseconds));
        delayTransition.setOnFinished(e -> {
            waitingForDelay = false;
            delayTransition = null;
        });
        delayTransition.play();
    }
    
    private void startSwapBlinking(int i, int j) {
        waitingForDelay = true;
        if (timeline != null) timeline.pause();
        
        itemChart.highlightSwap(i, j);
        
        Timeline blinkTimeline = new Timeline();
        blinkTimeline.getKeyFrames().addAll(
            new KeyFrame(Duration.seconds(0.0), e -> itemChart.highlightSwap(i, j)),
            new KeyFrame(Duration.seconds(0.25), e -> itemChart.clearHighlights()),
            new KeyFrame(Duration.seconds(0.5), e -> itemChart.highlightSwap(i, j)),
            new KeyFrame(Duration.seconds(0.75), e -> itemChart.clearHighlights()),
            new KeyFrame(Duration.seconds(1.0), e -> {
                itemChart.highlightSwap(i, j);
                // Perform the actual swap and update
                Item[] items = solver.getItems();
                appendProgress(String.format("   Swap: Item %d ↔ Item %d", items[i].id, items[j].id));
                itemChart.updateData(items);
            }),
            new KeyFrame(Duration.seconds(1.5), e -> {
                itemChart.clearHighlights();
                waitingForDelay = false;
                if (isPlaying && timeline != null) timeline.play();
            })
        );
        blinkTimeline.play();
    }
    
    
    // Playback controls
    public void onPlay() {
        if (isPlaying) {
            onPause();
            return;
        }
        isPlaying = true;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
        if (parent != null) parent.playButton.setText("⏸ Pause");
    }

    public void onPause() {
        stopTimeline();
    }

    public void onStepForward() {
        if (solver == null || solver.isDone()) return;
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;

        FractionalKnapsackSolver.State s = history.pop();
        solver.restore(s);

        itemChart.updateData(solver.getItems());
        capacityMeter.updateWithItems(solver.getCapacity(), 
            solver.getCapacity() - solver.getRemainingCapacity(), 
            solver.getTotalValue(),
            solver.getItems());
        itemChart.clearHighlights();

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
        if (parent != null) parent.stepDescription.setText(solver.getCurrentStepDescription());
    }

    public void onReset() {
        stopTimeline();
        history.clear();
        progressHistory.clear();
        countLogsForStep = false;
        currentStepLogLines = 0;

        solver.reset();
        itemChart.updateData(solver.getItems());
        capacityMeter.update(solver.getCapacity(), 0, 0);
        itemChart.clearHighlights();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready");
    }

    private void onNumItemsChanged(int n) {
        if (n < 1) n = 1;
        if (n > 10) n = 10;
        Item[] newItems = new Item[n];
        for (int i = 0; i < n; i++) {
            if (i < currentItems.length) {
                newItems[i] = new Item(currentItems[i]);
                newItems[i].id = i + 1;
            } else {
                newItems[i] = new Item(i + 1, 10, 50);
            }
        }
        currentItems = newItems;
        updateElementsField();
    }

    private void onRandomize() {
        Random rnd = new Random();
        for (Item item : currentItems) {
            item.weight = 5 + rnd.nextInt(46); // 5-50
            item.value = 20 + rnd.nextInt(181); // 20-200
        }
        updateElementsField();
        if (statusLabel != null) statusLabel.setText("Items randomized");
    }

    private void onApply() {
        if (capacitySpinner != null) {
            currentCapacity = capacitySpinner.getValue();
        }
        refreshAll();
        if (statusLabel != null) statusLabel.setText("Applied. Ready to solve.");
    }

    private void refreshAll() {
        stopTimeline();
        solver = new FractionalKnapsackSolver(currentItems, currentCapacity);
        solver.setStepListener(this::onStepEvent);
        renderVisuals();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        updateElementsField();
    }

    private void renderVisuals() {
        itemChart.setData(currentItems);
        capacityMeter.update(currentCapacity, 0, 0);
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isPlaying = false;
        if (parent != null) parent.playButton.setText("▶ Play");
    }

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
        double speed = parent != null ? parent.speedSlider.getValue() : 1.0;
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (solver.isDone()) {
                stopTimeline();
                return;
            }
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void renderCode() {
        if (parent == null) return;
        // Update code with current parameters
        FractionalKnapsackCode codeImpl = (FractionalKnapsackCode) CodeRepository.getCode("Fractional Knapsack");
        if (codeImpl != null) {
            codeImpl.updateParameters(currentItems, currentCapacity);
        }
        parent.loadCodeForCurrentLanguage();
    }

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
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
        for (int idx = 0; idx < newLen; idx++) {
            sb.append(lines[idx]);
            if (idx < newLen - 1) sb.append('\n');
        }
        parent.progressArea.setText(sb.toString());
    }

    private void updateVariablesPanel() {
        if (parent == null || parent.variableList == null) return;
        parent.variableList.getItems().clear();
        
        Item[] items = solver.getItems();
        StringBuilder itemsStr = new StringBuilder("[");
        for (int i = 0; i < Math.min(3, items.length); i++) {
            if (i > 0) itemsStr.append(", ");
            itemsStr.append(String.format("Item%d(%.1f,%.1f)", items[i].id, items[i].weight, items[i].value));
        }
        if (items.length > 3) itemsStr.append("...");
        itemsStr.append("]");

        parent.variableList.getItems().addAll(
            "items: " + itemsStr.toString(),
            "capacity: " + String.format("%.1f", solver.getCapacity()),
            "remaining: " + String.format("%.1f", solver.getRemainingCapacity()),
            "totalValue: " + String.format("%.2f", solver.getTotalValue()),
            "currentIndex: " + solver.getCurrentIndex(),
            "phase: " + solver.getPhase(),
            "state: " + (solver.isDone() ? "DONE" : "RUNNING")
        );
    }
    
    private void updateElementsField() {
        if (parent == null || parent.paramElementsField == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentItems.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.0f,%.0f", currentItems[i].weight, currentItems[i].value));
        }
        parent.paramElementsField.setText(sb.toString());
    }
    
    private void parseElementsField() {
        if (parent == null || parent.paramElementsField == null) return;
        String text = parent.paramElementsField.getText().trim();
        if (text.isEmpty()) return;
        
        try {
            String[] pairs = text.split(",\\s*");
            if (pairs.length % 2 != 0) {
                if (statusLabel != null) statusLabel.setText("Error: Items must be in pairs (weight,value)");
                return;
            }
            
            int numItems = pairs.length / 2;
            Item[] newItems = new Item[numItems];
            for (int i = 0; i < numItems; i++) {
                double weight = Double.parseDouble(pairs[i * 2]);
                double value = Double.parseDouble(pairs[i * 2 + 1]);
                newItems[i] = new Item(i + 1, weight, value);
            }
            currentItems = newItems;
            
            // Update spinner
            if (parent.paramBoardSizeSpinner != null) {
                parent.paramBoardSizeSpinner.getValueFactory().setValue(numItems);
            }
        } catch (NumberFormatException e) {
            if (statusLabel != null) statusLabel.setText("Error: Invalid number format");
        }
    }
}
