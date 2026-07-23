package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.QuickSortSolver;
import com.algorithmvisualizer.visualization.ArrayRenderer;
import com.algorithmvisualizer.visualization.BarChartRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/**
 * Quick Sort controller with bar chart visualization and sliding pointer indicators
 */
public class QuickSortController implements AlgorithmViewController.AlgorithmSpecificController {

    // Local controls
    @FXML private Spinner<Integer> arraySizeSpinner;
    @FXML private TextField arrayElementsField;
    @FXML private FlowPane arrayElementsBox;
    @FXML private Button randomizeButton;
    @FXML private Button applyArrayButton;
    @FXML private ComboBox<String> pivotStrategyCombo;
    @FXML private Label statusLabel;

    private AlgorithmViewController parent;

    // Visualizations
    private BarChartRenderer barChart;
    private ArrayRenderer arrayView;

    // Solver
    private QuickSortSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;
    private final Deque<QuickSortSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    private int[] currentArray = new int[]{5, 3, 8, 4, 2, 7, 1, 6};
    private QuickSortSolver.PivotStrategy currentStrategy = QuickSortSolver.PivotStrategy.LAST;

    // Track sorted indices (pivots in final position)
    private final Set<Integer> sortedIndices = new HashSet<>();
    
    // Pointer tracking for animation
    private int previousI = -1;
    private int previousJ = -1;
    private int previousPivot = -1;
    
    // Delay flags
    private boolean pendingSwapDelay = false;
    private boolean pendingCompareDelay = false;
    private boolean completionShown = false;

    @FXML
    private void initialize() {
        if (arraySizeSpinner != null) {
            arraySizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, currentArray.length));
        }
        if (arrayElementsField != null) {
            arrayElementsField.setText(join(currentArray));
        }
        if (arrayElementsBox != null) {
            rebuildLocalElementBoxes();
        }
        if (randomizeButton != null) randomizeButton.setOnAction(e -> onRandomize());
        if (applyArrayButton != null) applyArrayButton.setOnAction(e -> onApplyArray());
        
        // Setup pivot strategy combo
        if (pivotStrategyCombo != null) {
            pivotStrategyCombo.getItems().addAll(
                "Last Element",
                "First Element",
                "Middle Element",
                "Random",
                "Median of Three"
            );
            pivotStrategyCombo.setValue("Last Element");
            pivotStrategyCombo.setOnAction(e -> onPivotStrategyChanged());
        }

        // Create visualizations
        barChart = new BarChartRenderer();
        arrayView = new ArrayRenderer();

        // Create solver
        solver = new QuickSortSolver(currentArray, currentStrategy);
        solver.setStepListener(this::onStepEvent);
        
        renderVisuals();
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place bar chart in chessboard container
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            StackPane centeredChart = new StackPane(barChart.getNode());
            parent.chessboardContainer.getChildren().add(centeredChart);
        }

        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        }

        // Parameters panel integration
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, currentArray.length));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onParamSizeChanged(n));
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(true);
            parent.paramElementsBox.setManaged(true);
            rebuildParentElementBoxes();
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(false);
            parent.paramElementsField.setManaged(false);
        }
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setVisible(true);
            parent.paramRandomizeButton.setManaged(true);
            parent.paramRandomizeButton.setOnAction(e -> onRandomizeFromParent());
        }
        if (parent.paramNumQueensSpinner != null) {
            parent.paramNumQueensSpinner.setVisible(false);
            parent.paramNumQueensSpinner.setManaged(false);
        }
        if (parent.paramApplyButton != null) {
            parent.paramApplyButton.setOnAction(e -> onApplyArrayFromParent());
        }

        // Configure header and legend
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label chartHeader = new Label("Quick Sort Visualization");
            chartHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(chartHeader, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox legendRow = new HBox(15.0);
            
            // i pointer: Blue
            HBox l1 = new HBox(5.0);
            javafx.scene.shape.Rectangle r1 = new javafx.scene.shape.Rectangle(12, 12);
            r1.setFill(Color.CORNFLOWERBLUE);
            r1.setStroke(Color.BLACK);
            l1.getChildren().addAll(r1, new Label("i pointer"));
            
            // j pointer: Green
            HBox l2 = new HBox(5.0);
            javafx.scene.shape.Rectangle r2 = new javafx.scene.shape.Rectangle(12, 12);
            r2.setFill(Color.LIGHTGREEN);
            r2.setStroke(Color.BLACK);
            l2.getChildren().addAll(r2, new Label("j pointer"));
            
            // pivot: Orange
            HBox l3 = new HBox(5.0);
            javafx.scene.shape.Rectangle r3 = new javafx.scene.shape.Rectangle(12, 12);
            r3.setFill(Color.ORANGE);
            r3.setStroke(Color.BLACK);
            l3.getChildren().addAll(r3, new Label("Pivot"));
            
            // Compare: Gold
            HBox l4 = new HBox(5.0);
            javafx.scene.shape.Rectangle r4 = new javafx.scene.shape.Rectangle(12, 12);
            r4.setFill(Color.GOLD);
            r4.setStroke(Color.BLACK);
            l4.getChildren().addAll(r4, new Label("Compare"));
            
            // Swap: Red
            HBox l5 = new HBox(5.0);
            javafx.scene.shape.Rectangle r5 = new javafx.scene.shape.Rectangle(12, 12);
            r5.setFill(Color.CRIMSON);
            r5.setStroke(Color.BLACK);
            l5.getChildren().addAll(r5, new Label("Swap"));
            
            // Sorted: Green
            HBox l6 = new HBox(5.0);
            javafx.scene.shape.Rectangle r6 = new javafx.scene.shape.Rectangle(12, 12);
            r6.setFill(Color.FORESTGREEN);
            r6.setStroke(Color.BLACK);
            l6.getChildren().addAll(r6, new Label("Sorted"));
            
            legendRow.getChildren().addAll(l1, l2, l3, l4, l5, l6);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, legendRow);
        }
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Array size:");
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        parent.setCurrentAlgorithmName("Quick Sort");
        renderCode();
        initProgressLog();
        updateVariablesPanel();

        // Update solutions panel
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Array View with Pointers");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }
        moveArrayViewToSolutions();
    }

    private void moveArrayViewToSolutions() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        parent.solutionsContent.getChildren().add(arrayView.getNode());
    }

    private void onStepEvent(QuickSortSolver.StepType type, QuickSortSolver.State state) {
        countLogsForStep = true;
        currentStepLogLines = 0;
        
        switch (type) {
            case INIT:
                barChart.clearAllPointers();
                sortedIndices.clear();
                previousI = -1;
                previousJ = -1;
                previousPivot = -1;
                appendProgress("🎯 Quick Sort: n = " + state.array.length);
                appendProgress("📍 Pivot Strategy: " + getPivotStrategyName());
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Initializing Quick Sort...");
                break;
                
            case PARTITION_START:
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedHighlighting();
                appendProgress("━━━ Partition [" + state.left + ".." + state.right + "] ━━━");
                if (parent != null) parent.stepDescription.setText("Starting partition of range [" + state.left + ".." + state.right + "]");
                break;
                
            case SELECT_PIVOT:
                String pivotInfo = "arr[" + state.pivotIndex + "] = " + state.pivotValue;
                appendProgress("🎲 SELECT PIVOT using " + getPivotStrategyName() + " strategy");
                appendProgress("   → Chosen: " + pivotInfo);
                appendProgress("   → Goal: Partition array around this value");
                if (parent != null) parent.stepDescription.setText("Selected pivot: " + pivotInfo);
                // Clear highlights first
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedHighlighting();
                // Highlight pivot in both views
                barChart.setIndexColor(state.pivotIndex, Color.ORANGE);
                arrayView.setIndexColor(state.pivotIndex, "ORANGE");
                // Show pivot pointer below bars (bar chart only)
                barChart.showPointer("pivot", state.pivotIndex, Color.ORANGE, true);
                previousPivot = state.pivotIndex;
                break;
                
            case INIT_POINTERS:
                appendProgress("🔄 INITIALIZE PARTITION POINTERS");
                appendProgress("   → i = " + state.i + " (partition boundary: elements ≤ pivot)");
                appendProgress("   → j = " + state.j + " (scanning pointer: current element)");
                appendProgress("   → Strategy: Scan j through array, swap when arr[j] ≤ pivot");
                if (parent != null) parent.stepDescription.setText("Initializing partition pointers");
                // Show pointers above bars - only show i if it's >= 0
                if (state.i >= 0) {
                    barChart.showPointer("i", state.i, Color.CORNFLOWERBLUE, false);
                    previousI = state.i;
                }
                if (state.j >= 0 && state.j < state.array.length) {
                    barChart.showPointer("j", state.j, Color.LIGHTGREEN, false);
                    previousJ = state.j;
                }
                break;
                
            case MOVE_J:
                appendProgress("  ➡️ Move j to position " + state.j);
                if (parent != null) parent.stepDescription.setText("Moving j pointer to position " + state.j);
                // Clear previous highlights
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedHighlighting();
                // Animate j pointer movement (bar chart only)
                if (previousJ >= 0 && state.j != previousJ && state.j >= 0 && state.j < state.array.length) {
                    barChart.animatePointerMove("j", previousJ, state.j);
                }
                previousJ = state.j;
                break;
                
            case COMPARE:
                int compareVal = state.j < state.array.length ? state.array[state.j] : 0;
                String comparison = compareVal + (compareVal <= state.pivotValue ? " ≤ " : " > ") + state.pivotValue;
                appendProgress("  🔍 Compare: arr[" + state.j + "] = " + compareVal + " with pivot = " + state.pivotValue);
                if (compareVal <= state.pivotValue) {
                    appendProgress("      ✓ " + compareVal + " ≤ " + state.pivotValue + " → Will swap with i partition");
                } else {
                    appendProgress("      ✗ " + compareVal + " > " + state.pivotValue + " → Skip, continue scanning");
                }
                if (parent != null) parent.stepDescription.setText("Comparing " + comparison);
                // Clear and highlight only the two being compared
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedHighlighting();
                // Highlight comparison - only j and pivot
                if (state.j >= 0 && state.j < state.array.length) {
                    barChart.setIndexColor(state.j, Color.GOLD);
                    arrayView.setIndexColor(state.j, "GOLD");
                }
                if (state.pivotIndex >= 0 && state.pivotIndex < state.array.length) {
                    barChart.setIndexColor(state.pivotIndex, Color.GOLD);
                    arrayView.setIndexColor(state.pivotIndex, "GOLD");
                }
                break;
                
            case INCREMENT_I:
                appendProgress("  ⬆️ Element " + state.array[state.j] + " ≤ pivot → Increment i from " + previousI + " to " + state.i);
                appendProgress("      → Expanding partition boundary to include position " + state.i);
                if (parent != null) parent.stepDescription.setText("Incrementing i pointer to " + state.i);
                // Clear highlights first
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedHighlighting();
                // Animate i pointer movement (bar chart only)
                if (previousI >= 0 && state.i != previousI && state.i >= 0) {
                    barChart.animatePointerMove("i", previousI, state.i);
                } else if (previousI < 0 && state.i >= 0) {
                    // First time showing i pointer
                    barChart.showPointer("i", state.i, Color.CORNFLOWERBLUE, false);
                }
                // Highlight i position
                if (state.i >= 0) {
                    barChart.setIndexColor(state.i, Color.CORNFLOWERBLUE);
                    arrayView.setIndexColor(state.i, "CORNFLOWERBLUE");
                }
                previousI = state.i;
                break;
                
            case SWAP:
                if (state.i >= 0 && state.j >= 0 && state.i < state.array.length && state.j < state.array.length) {
                    int valI = state.array[state.i];
                    int valJ = state.array[state.j];
                    appendProgress("  🔄 SWAP NEEDED: Position i=" + state.i + " with j=" + state.j);
                    appendProgress("      → Before: arr[" + state.i + "]=" + valI + ", arr[" + state.j + "]=" + valJ);
                    appendProgress("      → After:  arr[" + state.i + "]=" + valJ + ", arr[" + state.j + "]=" + valI);
                    appendProgress("      → Moving smaller element into ≤pivot partition");
                    if (parent != null) parent.stepDescription.setText("Swapping elements at positions " + state.i + " and " + state.j);
                    // Start blinking animation
                    pendingSwapDelay = true;
                    if (isPlaying && timeline != null) timeline.pause();
                    startBlinkingAnimation(state.i, state.j, () -> {
                        // After blink, perform swap
                        barChart.updateData(state.array);
                        arrayView.updateData(state.array);
                        restoreSortedHighlighting();
                        barChart.updatePointerPositions();
                        arrayView.updatePointerPositions();
                        appendProgress("      ✓ Swap complete!");
                        appendProgress("");
                        pendingSwapDelay = false;
                        if (isPlaying && timeline != null) timeline.play();
                        if (solver.isDone()) showCompletion();
                    });
                }
                break;
                
            case PLACE_PIVOT:
                appendProgress("📌 PLACE PIVOT IN FINAL POSITION");
                appendProgress("   → Pivot value: " + state.pivotValue);
                appendProgress("   → Final position: index " + state.pivotIndex);
                appendProgress("   → All elements to left ≤ " + state.pivotValue);
                appendProgress("   → All elements to right > " + state.pivotValue);
                appendProgress("✓ Pivot " + state.pivotValue + " is now SORTED at position " + state.pivotIndex + "!");
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Pivot placed in final position");
                barChart.updateData(state.array);
                arrayView.updateData(state.array);
                // Mark pivot as sorted
                sortedIndices.add(state.pivotIndex);
                barChart.setIndexColor(state.pivotIndex, Color.FORESTGREEN);
                arrayView.setIndexColor(state.pivotIndex, "FORESTGREEN");
                break;
                
            case PARTITION_COMPLETE:
                appendProgress("━━━ PARTITION COMPLETE ━━━");
                appendProgress("   → Range [" + state.left + ".." + state.right + "] has been partitioned");
                appendProgress("   → Pivot at position " + state.pivotIndex + " is in final sorted position");
                if (state.pivotIndex - 1 > state.left) {
                    appendProgress("   → Left subarray: [" + state.left + ".." + (state.pivotIndex - 1) + "] needs sorting");
                }
                if (state.pivotIndex + 1 < state.right) {
                    appendProgress("   → Right subarray: [" + (state.pivotIndex + 1) + ".." + state.right + "] needs sorting");
                }
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Partition complete");
                // Hide pointers for this partition (bar chart only)
                barChart.hidePointer("i");
                barChart.hidePointer("j");
                barChart.hidePointer("pivot");
                previousI = -1;
                previousJ = -1;
                previousPivot = -1;
                barChart.updateData(state.array);
                arrayView.updateData(state.array);
                restoreSortedHighlighting();
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
                
            case PUSH_LEFT:
                if (state.pivotIndex - 1 > state.left) {
                    int leftSize = state.pivotIndex - state.left;
                    appendProgress("  ⬅️ Queue left subarray: [" + state.left + ".." + (state.pivotIndex - 1) + "] (" + leftSize + " elements)");
                }
                if (parent != null) parent.stepDescription.setText("Queuing left subarray");
                break;
                
            case PUSH_RIGHT:
                if (state.pivotIndex + 1 < state.right) {
                    int rightSize = state.right - state.pivotIndex;
                    appendProgress("  ➡️ Queue right subarray: [" + (state.pivotIndex + 1) + ".." + state.right + "] (" + rightSize + " elements)");
                }
                if (parent != null) parent.stepDescription.setText("Queuing right subarray");
                break;
                
            case POP_RANGE:
                if (parent != null) parent.stepDescription.setText("Processing next range");
                break;
                
            case BACKTRACK:
                appendProgress("⬅️ Backtrack from recursion");
                if (parent != null) parent.stepDescription.setText("Returning from recursion");
                break;
                
            case DONE:
                if (pendingSwapDelay) {
                    return; // Wait for swap animation to complete
                }
                showCompletion();
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
        }
        updateVariablesPanel();
    }

    private void showCompletion() {
        if (completionShown) return;
        completionShown = true;
        
        // Clear all pointers (bar chart only)
        barChart.clearAllPointers();
        
        // Mark all as sorted
        for (int i = 0; i < solver.getArray().length; i++) {
            sortedIndices.add(i);
            barChart.setIndexColor(i, Color.FORESTGREEN);
            arrayView.setIndexColor(i, "FORESTGREEN");
        }
        
        appendProgress("🎉 SORTING COMPLETE!");
        appendProgress("✓ Array is fully sorted in ascending order");
        appendProgress("✓ Pivot Strategy: " + getPivotStrategyName());
        appendProgress("✓ Time Complexity: O(n log n) average, O(n²) worst");
        appendProgress("");
        if (parent != null) parent.stepDescription.setText("🎉 Sorting Complete!");
        stopTimeline();
    }
    
    private void startBlinkingAnimation(int i, int j, Runnable onComplete) {
        // Force red highlighting for both elements
        barChart.forceHighlightSwap(i, j);
        arrayView.forceHighlightSwap(i, j);
        
        // Create blinking timeline - 4 cycles over 2 seconds
        Timeline blinkTimeline = new Timeline();
        blinkTimeline.getKeyFrames().addAll(
            new KeyFrame(Duration.seconds(0.0), e -> {
                barChart.forceHighlightSwap(i, j);
                arrayView.forceHighlightSwap(i, j);
            }),
            new KeyFrame(Duration.seconds(0.5), e -> {
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedHighlighting();
            }),
            new KeyFrame(Duration.seconds(1.0), e -> {
                barChart.forceHighlightSwap(i, j);
                arrayView.forceHighlightSwap(i, j);
            }),
            new KeyFrame(Duration.seconds(1.5), e -> {
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedHighlighting();
            }),
            new KeyFrame(Duration.seconds(2.0), e -> {
                onComplete.run();
            })
        );
        blinkTimeline.play();
    }
    
    private void restoreSortedHighlighting() {
        for (int idx : sortedIndices) {
            if (idx >= 0 && idx < solver.getArray().length) {
                barChart.setIndexColor(idx, Color.FORESTGREEN);
                arrayView.setIndexColor(idx, "FORESTGREEN");
            }
        }
    }
    
    private String getPivotStrategyName() {
        switch (currentStrategy) {
            case FIRST: return "First Element";
            case MIDDLE: return "Middle Element";
            case RANDOM: return "Random";
            case MEDIAN_OF_THREE: return "Median of Three";
            case LAST:
            default: return "Last Element";
        }
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
        if (pendingSwapDelay || pendingCompareDelay) return; // Wait for animations
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        
        QuickSortSolver.State s = history.pop();
        solver.restore(s);
        
        // Cancel all animations (bar chart only)
        barChart.cancelPointerAnimations();
        pendingSwapDelay = false;
        pendingCompareDelay = false;
        
        // Update data views
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        
        // Clear and restore highlights
        barChart.clearHighlights();
        arrayView.clearHighlights();
        barChart.clearAllPointers();
        
        // Rebuild sorted indices from state
        sortedIndices.clear();
        // Note: We'd need to track sorted indices in state for perfect restoration
        // For now, just restore visual state
        
        // Restore pointers if they exist in state (bar chart only)
        if (!solver.isDone()) {
            if (solver.getI() >= 0) {
                barChart.setPointerPosition("i", solver.getI());
                previousI = solver.getI();
            }
            if (solver.getJ() >= 0 && solver.getJ() < solver.getArray().length) {
                barChart.setPointerPosition("j", solver.getJ());
                previousJ = solver.getJ();
            }
            if (solver.getPivotIndex() >= 0) {
                barChart.setPointerPosition("pivot", solver.getPivotIndex());
                previousPivot = solver.getPivotIndex();
            }
        }
        
        // Remove progress log lines
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
        sortedIndices.clear();
        completionShown = false;
        previousI = -1;
        previousJ = -1;
        previousPivot = -1;
        pendingSwapDelay = false;
        pendingCompareDelay = false;
        
        solver.reset();
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        barChart.clearHighlights();
        arrayView.clearHighlights();
        barChart.clearAllPointers();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready.");
    }

    private void onParamSizeChanged(int size) {
        if (size <= 0) size = 1;
        if (size > 15) size = 15;
        if (arraySizeSpinner != null) arraySizeSpinner.getValueFactory().setValue(size);
        currentArray = randomArray(size);
        if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
        rebuildLocalElementBoxes();
        rebuildParentElementBoxes();
        refreshAll();
    }

    private void onRandomize() {
        int size = arraySizeSpinner != null ? arraySizeSpinner.getValue() : currentArray.length;
        currentArray = randomArray(size);
        if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
        rebuildLocalElementBoxes();
        rebuildParentElementBoxes();
        refreshAll();
    }

    private void onApplyArray() {
        int[] parsed = readArrayFromLocalBoxes();
        if (parsed == null) {
            if (statusLabel != null) statusLabel.setText("Invalid input. Use comma-separated integers.");
            return;
        }
        currentArray = parsed;
        if (arraySizeSpinner != null) arraySizeSpinner.getValueFactory().setValue(currentArray.length);
        refreshAll();
    }

    private void onApplyArrayFromParent() {
        int[] parsed = readArrayFromParentBoxes();
        if (parsed != null) {
            currentArray = parsed;
            if (arraySizeSpinner != null) arraySizeSpinner.getValueFactory().setValue(currentArray.length);
            if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
        } else if (statusLabel != null) {
            statusLabel.setText("Invalid input. Please enter integers in the boxes.");
        }
        refreshAll();
    }

    private void onPivotStrategyChanged() {
        if (pivotStrategyCombo == null) return;
        String selected = pivotStrategyCombo.getValue();
        switch (selected) {
            case "First Element":
                currentStrategy = QuickSortSolver.PivotStrategy.FIRST;
                break;
            case "Middle Element":
                currentStrategy = QuickSortSolver.PivotStrategy.MIDDLE;
                break;
            case "Random":
                currentStrategy = QuickSortSolver.PivotStrategy.RANDOM;
                break;
            case "Median of Three":
                currentStrategy = QuickSortSolver.PivotStrategy.MEDIAN_OF_THREE;
                break;
            case "Last Element":
            default:
                currentStrategy = QuickSortSolver.PivotStrategy.LAST;
                break;
        }
        refreshAll();
    }

    private void refreshAll() {
        stopTimeline();
        sortedIndices.clear();
        completionShown = false;
        solver = new QuickSortSolver(currentArray, currentStrategy);
        solver.setStepListener(this::onStepEvent);
        
        // Update code with current parameters
        com.algorithmvisualizer.code.AlgorithmCode code = 
            com.algorithmvisualizer.code.CodeRepository.getCode("Quick Sort");
        if (code instanceof com.algorithmvisualizer.code.implementations.QuickSortCode) {
            com.algorithmvisualizer.code.implementations.QuickSortCode qsCode = 
                (com.algorithmvisualizer.code.implementations.QuickSortCode) code;
            qsCode.updateParameters(currentArray);
        }
        
        renderVisuals();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (statusLabel != null) statusLabel.setText("Array applied. Ready.");
    }

    private void renderVisuals() {
        barChart.setData(currentArray);
        arrayView.setData(currentArray);
        rebuildParentElementBoxes();
    }

    private void onRandomizeFromParent() {
        int size = parent != null && parent.paramBoardSizeSpinner != null ? 
            parent.paramBoardSizeSpinner.getValue() : currentArray.length;
        currentArray = randomArray(size);
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(join(currentArray));
        }
        if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
        refreshAll();
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
            if (pendingSwapDelay || pendingCompareDelay) {
                return; // Wait for animations
            }
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void renderCode() {
        if (parent == null) return;
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
        parent.variableList.getItems().addAll(
            "array: " + Arrays.toString(solver.getArray()),
            "phase: " + solver.getPhase(),
            "left: " + solver.getLeft(),
            "right: " + solver.getRight(),
            "pivot: " + solver.getPivotValue(),
            "pivotIndex: " + solver.getPivotIndex(),
            "i: " + solver.getI(),
            "j: " + solver.getJ(),
            "depth: " + solver.getRecursionDepth(),
            "strategy: " + getPivotStrategyName(),
            "state: " + (solver.isDone() ? "DONE" : "RUNNING")
        );
    }

    // Utility methods
    private static String arrayToString(int[] arr) {
        if (arr == null || arr.length == 0) return "[]";
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length && i < 15; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        if (arr.length > 15) {
            sb.append("...");
        }
        sb.append("]");
        return sb.toString();
    }

    private static int[] parseArray(String text) {
        if (text == null || text.trim().isEmpty()) return new int[0];
        String[] parts = text.split(",");
        int[] arr = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                arr[i] = Integer.parseInt(parts[i].trim());
            }
            return arr;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String join(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private static int[] randomArray(int n) {
        Random rnd = new Random();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt(100) - 50;
        }
        return a;
    }

    private void rebuildLocalElementBoxes() {
        if (arrayElementsBox == null) return;
        arrayElementsBox.getChildren().clear();
        for (int i = 0; i < currentArray.length; i++) {
            TextField tf = new TextField(String.valueOf(currentArray[i]));
            tf.setPrefWidth(60);
            tf.setPromptText("a[" + i + "]");
            arrayElementsBox.getChildren().add(tf);
        }
    }

    private int[] readArrayFromLocalBoxes() {
        if (arrayElementsBox == null || arrayElementsBox.getChildren().isEmpty()) {
            return parseArray(arrayElementsField != null ? arrayElementsField.getText() : null);
        }
        int n = arrayElementsBox.getChildren().size();
        int[] arr = new int[n];
        try {
            for (int i = 0; i < n; i++) {
                String txt = ((TextField) arrayElementsBox.getChildren().get(i)).getText();
                arr[i] = Integer.parseInt(txt.trim());
            }
        } catch (Exception ex) {
            return null;
        }
        return arr;
    }

    private void rebuildParentElementBoxes() {
        if (parent == null || parent.paramElementsBox == null) return;
        FlowPane pane = parent.paramElementsBox;
        pane.getChildren().clear();
        for (int i = 0; i < currentArray.length; i++) {
            TextField tf = new TextField(String.valueOf(currentArray[i]));
            tf.setPrefWidth(60);
            tf.setPromptText("a[" + i + "]");
            pane.getChildren().add(tf);
        }
    }

    private int[] readArrayFromParentBoxes() {
        if (parent == null || parent.paramElementsBox == null || parent.paramElementsBox.getChildren().isEmpty()) {
            return null;
        }
        int n = parent.paramElementsBox.getChildren().size();
        int[] arr = new int[n];
        try {
            for (int i = 0; i < n; i++) {
                String txt = ((TextField) parent.paramElementsBox.getChildren().get(i)).getText();
                arr[i] = Integer.parseInt(txt.trim());
            }
        } catch (Exception ex) {
            return null;
        }
        return arr;
    }
}
