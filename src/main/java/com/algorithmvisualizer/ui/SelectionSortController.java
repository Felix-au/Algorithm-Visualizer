package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.SelectionSortSolver;
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
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

public class SelectionSortController implements AlgorithmViewController.AlgorithmSpecificController {

    // Local controls (in selectionsort-view.fxml)
    @FXML private Spinner<Integer> arraySizeSpinner;
    @FXML private TextField arrayElementsField;
    @FXML private FlowPane arrayElementsBox;
    @FXML private Button randomizeButton;
    @FXML private Button applyArrayButton;
    @FXML private Label statusLabel;

    // Parent references
    private AlgorithmViewController parent;

    // Visuals
    private BarChartRenderer barChart;
    private ArrayRenderer arrayView;

    // Solver
    private SelectionSortSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;
    private final Deque<SelectionSortSolver.State> history = new ArrayDeque<>();
    // Track progress log lines per algorithm step to support step-back removal
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    private int[] currentArray = new int[] {5, 3, 8, 4, 2};

    // Helper to track if we should delay after swap during play
    private boolean pendingSwapDelay = false;
    private boolean pendingMinDelay = false;
    private boolean pendingEndScanDelay = false;
    private boolean pendingMarkedDelay = false;
    private boolean pendingBlinkDelay = false;

    @FXML
    private void initialize() {
        if (arraySizeSpinner != null) {
            arraySizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, currentArray.length));
        }
        if (arrayElementsField != null) {
            arrayElementsField.setText(join(currentArray));
        }
        if (arrayElementsBox != null) {
            rebuildLocalElementBoxes();
        }
        if (randomizeButton != null) randomizeButton.setOnAction(e -> onRandomize());
        if (applyArrayButton != null) applyArrayButton.setOnAction(e -> onApplyArray());

        // Create visuals
        barChart = new BarChartRenderer();
        arrayView = new ArrayRenderer();
        renderVisuals();

        // Create solver
        solver = new SelectionSortSolver(currentArray);
        solver.setStepListener(this::onStepEvent);

        // Parent speed linkage if available later
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place visuals in parent's chessboard container; titles/legends go outside the bordered box
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            StackPane centeredChart = new StackPane(barChart.getNode());
            parent.chessboardContainer.getChildren().add(centeredChart);
        }

        

        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());

        // Parameters panel integration
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, currentArray.length));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onParamSizeChanged(n));
        }
        // Prefer element boxes in shared panel
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(true);
            parent.paramElementsBox.setManaged(true);
            rebuildParentElementBoxes();
        }
        // Hide CSV field in shared panel
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
            // Hide N-Queens specific spinner in this view
            parent.paramNumQueensSpinner.setVisible(false);
            parent.paramNumQueensSpinner.setManaged(false);
        }
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyArrayFromParent());

        // Configure header and legend outside the chart box
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            // Replace header label text to Selection Sort Visualization
            parent.chessboardHeaderBox.getChildren().clear();
            Label chartHeader = new Label("Selection Sort Visualization");
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
            // Compare = Yellow
            HBox l1 = new HBox(5.0);
            javafx.scene.shape.Rectangle r1 = new javafx.scene.shape.Rectangle(12, 12);
            r1.setFill(javafx.scene.paint.Color.GOLD);
            r1.setStroke(javafx.scene.paint.Color.BLACK);
            l1.getChildren().addAll(r1, new Label("Compare"));
            // Min = Orange
            HBox l2 = new HBox(5.0);
            javafx.scene.shape.Rectangle r2 = new javafx.scene.shape.Rectangle(12, 12);
            r2.setFill(javafx.scene.paint.Color.DARKORANGE);
            r2.setStroke(javafx.scene.paint.Color.BLACK);
            l2.getChildren().addAll(r2, new Label("Minimum"));
            // Swap = Red
            HBox l3 = new HBox(5.0);
            javafx.scene.shape.Rectangle r3 = new javafx.scene.shape.Rectangle(12, 12);
            r3.setFill(javafx.scene.paint.Color.CRIMSON);
            r3.setStroke(javafx.scene.paint.Color.BLACK);
            l3.getChildren().addAll(r3, new Label("Swap"));
            // Sorted = Green
            HBox l4 = new HBox(5.0);
            javafx.scene.shape.Rectangle r4 = new javafx.scene.shape.Rectangle(12, 12);
            r4.setFill(javafx.scene.paint.Color.FORESTGREEN);
            r4.setStroke(javafx.scene.paint.Color.BLACK);
            l4.getChildren().addAll(r4, new Label("Sorted Prefix"));
            legendRow.getChildren().addAll(l1, l2, l3, l4);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, legendRow);
        }
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Array size:");
        // Hide Pause button (Play is toggle)
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        // Code and logs
        renderCode();
        initProgressLog();
        updateVariablesPanel();

        // Update solutions panel to show Array View header/legend above the array
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Array View");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(true);
            parent.solutionsSubHeaderBox.setManaged(true);
            parent.solutionsSubHeaderBox.getChildren().clear();
            HBox legendRow = new HBox(15.0);
            // Compare = Yellow
            HBox l1 = new HBox(5.0);
            javafx.scene.shape.Rectangle r1 = new javafx.scene.shape.Rectangle(12, 12);
            r1.setFill(javafx.scene.paint.Color.GOLD);
            r1.setStroke(javafx.scene.paint.Color.BLACK);
            l1.getChildren().addAll(r1, new Label("Compare"));
            // Min = Orange
            HBox l2 = new HBox(5.0);
            javafx.scene.shape.Rectangle r2 = new javafx.scene.shape.Rectangle(12, 12);
            r2.setFill(javafx.scene.paint.Color.DARKORANGE);
            r2.setStroke(javafx.scene.paint.Color.BLACK);
            l2.getChildren().addAll(r2, new Label("Minimum"));
            // Swap = Red
            HBox l3 = new HBox(5.0);
            javafx.scene.shape.Rectangle r3 = new javafx.scene.shape.Rectangle(12, 12);
            r3.setFill(javafx.scene.paint.Color.CRIMSON);
            r3.setStroke(javafx.scene.paint.Color.BLACK);
            l3.getChildren().addAll(r3, new Label("Swap"));
            // Sorted = Green
            HBox l4 = new HBox(5.0);
            javafx.scene.shape.Rectangle r4 = new javafx.scene.shape.Rectangle(12, 12);
            r4.setFill(javafx.scene.paint.Color.FORESTGREEN);
            r4.setStroke(javafx.scene.paint.Color.BLACK);
            l4.getChildren().addAll(r4, new Label("Sorted Prefix"));
            legendRow.getChildren().addAll(l1, l2, l3, l4);
            parent.solutionsSubHeaderBox.getChildren().add(legendRow);
        }
        // Move the array view into the solutions area
        moveArrayViewToSolutions();
    }

    private void onParamSizeChanged(int size) {
        if (size <= 0) size = 1;
        if (arraySizeSpinner != null) arraySizeSpinner.getValueFactory().setValue(size);
        currentArray = randomArray(size);
        if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
        rebuildLocalElementBoxes();
        rebuildParentElementBoxes();
        refreshAll();
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(join(currentArray));
        }
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

    private void refreshAll() {
        stopTimeline();
        solver.setArray(currentArray);
        renderVisuals();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (statusLabel != null) statusLabel.setText("Array applied. Ready.");
    }

    private void renderVisuals() {
        barChart.setData(currentArray);
        arrayView.setData(currentArray);
        if (parent != null && parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(barChart.getNode());
        }
        rebuildParentElementBoxes();
    }

    private void moveArrayViewToSolutions() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        parent.solutionsContent.getChildren().add(arrayView.getNode());
    }

    private void onStepEvent(SelectionSortSolver.StepType type, int i, int j, int minIndex) {
        switch (type) {
            case INIT_OUTER:
            case INIT_MIN:
                // Start counting logs for a new step at outer init
                if (type == SelectionSortSolver.StepType.INIT_OUTER) {
                    countLogsForStep = true;
                    currentStepLogLines = 0;
                }
                if (parent != null) parent.stepDescription.setText("Start outer iteration i=" + i);
                break;
            case COMPARE:
                barChart.clearHighlights();
                arrayView.clearHighlights();
                barChart.highlightCompare(j, minIndex);
                arrayView.highlightCompare(j, minIndex);
                barChart.highlightMin(minIndex);
                arrayView.highlightMin(minIndex);
                appendProgress("🔍 Comparing element at index " + j + " (value: " + solver.getArray()[j] + ") with current minimum at index " + minIndex + " (value: " + solver.getArray()[minIndex] + ")");
                if (parent != null) parent.stepDescription.setText("Comparing element at index " + j + " with current minimum at index " + minIndex);
                break;
            case SET_MIN:
                barChart.highlightMin(minIndex);
                arrayView.highlightMin(minIndex);
                appendProgress("🔄 Found new minimum! Element at index " + minIndex + " (value: " + solver.getArray()[minIndex] + ") is smaller than previous minimum");
                appendProgress("");
                // Add 2-second delay after new min (only during Play)
                if (isPlaying) {
                    pendingMinDelay = true;
                    if (timeline != null) { timeline.pause(); }
                    PauseTransition pauseMin = new PauseTransition(Duration.seconds(2));
                    pauseMin.setOnFinished(ev -> {
                        pendingMinDelay = false;
                        if (isPlaying && timeline != null) { timeline.play(); }
                        if (solver.isDone()) { showCompletion(); }
                    });
                    pauseMin.play();
                }
                break;
            case END_SCAN:
                appendProgress("✅ Finished scanning unsorted portion for position " + i);
                appendProgress("   → Found minimum element at index " + minIndex + " (value: " + solver.getArray()[minIndex] + ")");
                appendProgress("");
                // Add 2-second delay after end scan (only during Play)
                if (isPlaying) {
                    pendingEndScanDelay = true;
                    if (timeline != null) { timeline.pause(); }
                    PauseTransition pauseEnd = new PauseTransition(Duration.seconds(2));
                    pauseEnd.setOnFinished(ev -> {
                        pendingEndScanDelay = false;
                        if (isPlaying && timeline != null) { timeline.play(); }
                        if (solver.isDone()) { showCompletion(); }
                    });
                    pauseEnd.play();
                }
                break;
            case SWAP:
                // Blink elements for 2 seconds before swapping (play and manual step)
                pendingBlinkDelay = true;
                if (isPlaying && timeline != null) { timeline.pause(); }
                startBlinkingAnimation(i, minIndex, () -> {
                    // Perform actual swap after blinking
                    barChart.updateData(solver.getArray());
                    arrayView.updateData(solver.getArray());
                    appendProgress("🔄 SWAPPED! Moved element from position " + i + " to position " + minIndex);
                    appendProgress("   → Element " + solver.getArray()[minIndex] + " is now in its correct sorted position");
                    appendProgress("");
                    pendingBlinkDelay = false;
                    if (isPlaying && timeline != null) { timeline.play(); }
                    if (solver.isDone()) { showCompletion(); }
                });
                break;
            case MARK_SORTED:
                barChart.markSortedPrefix(i);
                arrayView.markSortedPrefix(i);
                appendProgress("✓ Element at index " + i + " is now in its correct sorted position");
                appendProgress("");
                // End of this step: record how many lines were written and reset counter
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                // Add 2-second delay after marked index (only during Play)
                if (isPlaying) {
                    pendingMarkedDelay = true;
                    if (timeline != null) { timeline.pause(); }
                    PauseTransition pauseMark = new PauseTransition(Duration.seconds(2));
                    pauseMark.setOnFinished(ev -> {
                        pendingMarkedDelay = false;
                        if (isPlaying && timeline != null) { timeline.play(); }
                        if (solver.isDone()) { showCompletion(); }
                    });
                    pauseMark.play();
                }
                break;
            case DONE:
                // Only show completion if no delays are pending
                if (isPlaying && (pendingMarkedDelay || pendingMinDelay || pendingEndScanDelay || pendingBlinkDelay)) {
                    // Don't show completion yet - wait for delays to finish
                    // The completion will be shown when the last delay finishes
                    return;
                } else {
                    showCompletion();
                }
                // Safeguard: if a step was in progress but didn't hit MARK_SORTED due to edge cases
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
        updateVariablesPanel(); // Update variables one last time
        // Mark all elements as sorted when algorithm is complete
        int lastIndex = solver.getArray().length - 1;
        barChart.markSortedPrefix(lastIndex);
        arrayView.markSortedPrefix(lastIndex);
        
        appendProgress("🎉 SORTING COMPLETE!");
        appendProgress("✓ All " + (lastIndex + 1) + " elements are now in their correct sorted positions");
        appendProgress("✓ Array is fully sorted in ascending order");
        appendProgress("");
        
        if (parent != null) parent.stepDescription.setText("🎉 Sorting Complete! All elements are sorted.");
        stopTimeline();
    }

    // Controls from parent
    public void onPlay() {
        if (isPlaying) { onPause(); return; }
        isPlaying = true;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
        if (parent != null) parent.playButton.setText("⏸ Pause");
    }

    public void onPause() { stopTimeline(); }

    public void onStepForward() {
        if (solver == null || solver.isDone()) return;
        // Prevent stepping while any delay/animation is active (ensures manual mode respects waits)
        if (pendingSwapDelay || pendingMinDelay || pendingEndScanDelay || pendingMarkedDelay || pendingBlinkDelay) return;
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        // Pause if playing
        if (isPlaying) { onPause(); }
        if (history.isEmpty()) return;
        SelectionSortSolver.State s = history.pop();
        solver.restore(s);

        // Reset any pending UI delays/flags
        pendingSwapDelay = false;
        pendingMinDelay = false;
        pendingEndScanDelay = false;
        pendingMarkedDelay = false;
        pendingBlinkDelay = false;

        // Update data views
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());

        // Clear and reapply highlights based on restored algorithm state
        barChart.clearHighlights();
        arrayView.clearHighlights();

        int n = solver.getArray().length;
        int i = solver.getI();
        int j = solver.getJ();
        int minIndex = solver.getMinIndex();

        // Mark sorted prefix up to (i - 1)
        if (n > 0) {
            if (solver.isDone()) {
                // Entire array sorted
                int lastIndex = n - 1;
                barChart.markSortedPrefix(lastIndex);
                arrayView.markSortedPrefix(lastIndex);
            } else if (i > 0) {
                barChart.markSortedPrefix(i - 1);
                arrayView.markSortedPrefix(i - 1);
            }
        }

        if (!solver.isDone()) {
            if (j < n) {
                // Currently scanning: highlight comparison and current min
                barChart.highlightCompare(j, minIndex);
                arrayView.highlightCompare(j, minIndex);
                barChart.highlightMin(minIndex);
                arrayView.highlightMin(minIndex);
            } else {
                // Finished scanning for this i; show intention (swap or mark)
                barChart.highlightMin(minIndex);
                arrayView.highlightMin(minIndex);
                if (minIndex != i) {
                    barChart.highlightSwap(i, minIndex);
                    arrayView.highlightSwap(i, minIndex);
                }
            }
        }

        // Remove the logs written during the step we just stepped back from
        if (parent != null && parent.progressArea != null) {
            if (countLogsForStep && currentStepLogLines > 0) {
                // Step was in progress; remove what has been logged so far for this step
                removeLastLogLines(currentStepLogLines);
                currentStepLogLines = 0;
                countLogsForStep = false;
            } else if (!progressHistory.isEmpty()) {
                int toRemove = progressHistory.pop();
                removeLastLogLines(toRemove);
            }
        }

        // Update variables and step description
        updateVariablesPanel();
        if (parent != null) {
            parent.stepDescription.setText(solver.getCurrentStepDescription());
        }
    }

    public void onReset() {
        stopTimeline();
        history.clear();
        progressHistory.clear();
        countLogsForStep = false;
        currentStepLogLines = 0;
        solver.reset();
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        barChart.clearHighlights();
        arrayView.clearHighlights();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready.");
    }

    private void onRandomizeFromParent() {
        int size = parent != null && parent.paramBoardSizeSpinner != null ? parent.paramBoardSizeSpinner.getValue() : currentArray.length;
        currentArray = randomArray(size);
        if (parent != null && parent.paramElementsField != null) parent.paramElementsField.setText(join(currentArray));
        if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
        refreshAll();
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
            if (solver.isDone()) { stopTimeline(); return; }
            if (pendingSwapDelay || pendingMinDelay || pendingEndScanDelay || pendingMarkedDelay || pendingBlinkDelay) { return; }
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void startBlinkingAnimation(int i, int j, Runnable onComplete) {
        // Force red highlighting for both elements (overrides green for blinking)
        barChart.forceHighlightSwap(i, j);
        arrayView.forceHighlightSwap(i, j);
        
        // Create blinking timeline
        Timeline blinkTimeline = new Timeline();
        
        // Add keyframes for blinking - 4 cycles over 2 seconds
        blinkTimeline.getKeyFrames().addAll(
            new KeyFrame(Duration.seconds(0.0), e -> {
                barChart.forceHighlightSwap(i, j);
                arrayView.forceHighlightSwap(i, j);
            }),
            new KeyFrame(Duration.seconds(0.5), e -> {
                barChart.clearHighlights();
                arrayView.clearHighlights();
            }),
            new KeyFrame(Duration.seconds(1.0), e -> {
                barChart.forceHighlightSwap(i, j);
                arrayView.forceHighlightSwap(i, j);
            }),
            new KeyFrame(Duration.seconds(1.5), e -> {
                barChart.clearHighlights();
                arrayView.clearHighlights();
            }),
            new KeyFrame(Duration.seconds(2.0), e -> {
                // After blinking, restore proper colors and run completion
                onComplete.run();
            })
        );
        
        blinkTimeline.play();
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        String[] lines = new String[] {
                "void selectionSort(int[] a) {",
                "  int n = a.length;",
                "  for (int i = 0; i < n - 1; i++) {",
                "    int min = i;",
                "    for (int j = i + 1; j < n; j++) {",
                "      if (a[j] < a[min]) min = j;",
                "    }",
                "    if (min != i) swap(a, i, min);",
                "  }",
                "}",
        };
        parent.codeArea.setText(String.join("\n", lines));
    }

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        appendProgress("Selection Sort: n = " + currentArray.length);
        appendProgress("Starting...");
        // Reset step log tracking at start of a new run
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
    }

    private void appendProgress(String line) {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.appendText(line + "\n");
        if (countLogsForStep) {
            currentStepLogLines++;
        }
    }

    private void removeLastLogLines(int count) {
        if (parent == null || parent.progressArea == null || count <= 0) return;
        String text = parent.progressArea.getText();
        if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n", -1); // keep trailing empty
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
                "i (current pass / sorted boundary): " + solver.getI(),
                "j (current index): " + (solver.getJ() == currentArray.length ? (solver.getJ() - 1) : solver.getJ()),
                "Minimum Index: " + solver.getMinIndex(),
                "state: " + (solver.isDone() ? "DONE" : "RUNNING")
        );
    }

    // Helpers
    private static int[] parseArray(String text) {
        if (text == null || text.trim().isEmpty()) return new int[0];
        String[] parts = text.split(",");
        int[] arr = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) arr[i] = Integer.parseInt(parts[i].trim());
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
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(100) - 50; // [-50,49]
        return a;
    }

    // Build local element input boxes (selectionsort-view.fxml)
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
        if (arrayElementsBox == null || arrayElementsBox.getChildren().isEmpty()) return parseArray(arrayElementsField != null ? arrayElementsField.getText() : null);
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

    // Build parent panel element input boxes
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
        if (parent == null || parent.paramElementsBox == null || parent.paramElementsBox.getChildren().isEmpty()) return null;
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


