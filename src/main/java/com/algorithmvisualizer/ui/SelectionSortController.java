package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.SelectionSortSolver;
import com.algorithmvisualizer.visualization.ArrayRenderer;
import com.algorithmvisualizer.visualization.BarChartRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

public class SelectionSortController implements AlgorithmViewController.AlgorithmSpecificController {

    // Local controls (in selectionsort-view.fxml)
    @FXML private Spinner<Integer> arraySizeSpinner;
    @FXML private TextField arrayElementsField;
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

    private int[] currentArray = new int[] {5, 3, 8, 4, 2};

    @FXML
    private void initialize() {
        if (arraySizeSpinner != null) {
            arraySizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, currentArray.length));
        }
        if (arrayElementsField != null) {
            arrayElementsField.setText(join(currentArray));
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

        // Place visuals in parent's chessboard container
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10.0);
            vbox.setAlignment(javafx.geometry.Pos.CENTER);
            // Add legend above chart
            javafx.scene.control.Label chartLegend = new javafx.scene.control.Label("Legend: Compare = Yellow, Min = Orange, Swap = Red, Sorted = Green");
            chartLegend.setStyle("-fx-font-style: italic;");
            vbox.getChildren().addAll(chartLegend, barChart.getNode());
            parent.chessboardContainer.getChildren().add(vbox);
        }

        // Wire controls
        if (parent.playButton != null) parent.playButton.setOnAction(e -> onPlay());
        if (parent.pauseButton != null) parent.pauseButton.setOnAction(e -> onPause());
        if (parent.stepForwardButton != null) parent.stepForwardButton.setOnAction(e -> onStepForward());
        if (parent.stepBackButton != null) parent.stepBackButton.setOnAction(e -> onStepBack());
        if (parent.resetButton != null) parent.resetButton.setOnAction(e -> onReset());

        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());

        // Parameters panel integration
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, currentArray.length));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onParamSizeChanged(n));
        }
        // Elements field + Randomize button in shared panel
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setText(join(currentArray));
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

        // Hide N-Queens specific bits
        if (parent.chessboardHeaderBox != null) { parent.chessboardHeaderBox.setVisible(false); parent.chessboardHeaderBox.setManaged(false); }
        if (parent.chessboardLegendBox != null) { parent.chessboardLegendBox.setVisible(false); parent.chessboardLegendBox.setManaged(false); }
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Array size:");

        // Code and logs
        renderCode();
        initProgressLog();
        updateVariablesPanel();

        // Move the array view into the solutions area
        moveArrayViewToSolutions();
    }

    private void onParamSizeChanged(int size) {
        if (size <= 0) size = 1;
        if (arraySizeSpinner != null) arraySizeSpinner.getValueFactory().setValue(size);
        if (arrayElementsField != null) {
            currentArray = randomArray(size);
            arrayElementsField.setText(join(currentArray));
            refreshAll();
        }
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(join(currentArray));
        }
    }

    private void onRandomize() {
        int size = arraySizeSpinner != null ? arraySizeSpinner.getValue() : currentArray.length;
        currentArray = randomArray(size);
        if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
        refreshAll();
    }

    private void onApplyArray() {
        int[] parsed = parseArray(arrayElementsField.getText());
        if (parsed == null) {
            if (statusLabel != null) statusLabel.setText("Invalid input. Use comma-separated integers.");
            return;
        }
        currentArray = parsed;
        if (arraySizeSpinner != null) arraySizeSpinner.getValueFactory().setValue(currentArray.length);
        refreshAll();
    }

    private void onApplyArrayFromParent() {
        if (parent != null && parent.paramElementsField != null) {
            int[] parsed = parseArray(parent.paramElementsField.getText());
            if (parsed != null) {
                currentArray = parsed;
                if (arraySizeSpinner != null) arraySizeSpinner.getValueFactory().setValue(currentArray.length);
                if (arrayElementsField != null) arrayElementsField.setText(join(currentArray));
            } else if (statusLabel != null) {
                statusLabel.setText("Invalid input. Use comma-separated integers.");
            }
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
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10.0);
            vbox.setAlignment(javafx.geometry.Pos.CENTER);
            vbox.getChildren().addAll(barChart.getNode());
            parent.chessboardContainer.getChildren().add(vbox);
        }
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(join(currentArray));
        }
    }

    private void moveArrayViewToSolutions() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        javafx.scene.layout.VBox header = new javafx.scene.layout.VBox(4.0);
        Label title = new Label("Array View");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label arrayLegend = new Label("Legend: Compare = Yellow, Min = Orange, Swap = Red, Sorted Prefix = Green");
        arrayLegend.setStyle("-fx-font-style: italic;");
        header.getChildren().addAll(title, arrayLegend);
        parent.solutionsContent.getChildren().add(header);
        parent.solutionsContent.getChildren().add(arrayView.getNode());
    }

    private void onStepEvent(SelectionSortSolver.StepType type, int i, int j, int minIndex) {
        switch (type) {
            case INIT_OUTER:
            case INIT_MIN:
                barChart.clearHighlights();
                arrayView.clearHighlights();
                if (parent != null) parent.stepDescription.setText("Start outer iteration i=" + i);
                break;
            case COMPARE:
                barChart.highlightCompare(i, Math.max(0, j));
                barChart.highlightMin(minIndex);
                arrayView.highlightCompare(i, Math.max(0, j));
                arrayView.highlightMin(minIndex);
                appendProgress("Compare j=" + j + " with minIndex=" + minIndex);
                if (parent != null) parent.stepDescription.setText("Comparing j=" + j + " with current min at " + minIndex);
                break;
            case SET_MIN:
                barChart.highlightMin(minIndex);
                arrayView.highlightMin(minIndex);
                appendProgress("New min at " + minIndex);
                break;
            case END_SCAN:
                appendProgress("End scan for i=" + i);
                break;
            case SWAP:
                barChart.highlightSwap(i, minIndex);
                arrayView.highlightSwap(i, minIndex);
                barChart.updateData(solver.getArray());
                arrayView.updateData(solver.getArray());
                appendProgress("Swap i=" + i + " with minIndex=" + minIndex);
                break;
            case MARK_SORTED:
                barChart.markSortedPrefix(i);
                arrayView.markSortedPrefix(i);
                appendProgress("Marked index " + i + " as sorted");
                break;
            case DONE:
                barChart.markSortedPrefix(solver.getArray().length - 1);
                arrayView.markSortedPrefix(solver.getArray().length - 1);
                appendProgress("Done.");
                if (parent != null) parent.stepDescription.setText("Done.");
                stopTimeline();
                break;
        }
        updateVariablesPanel();
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
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (history.isEmpty()) return;
        SelectionSortSolver.State s = history.pop();
        solver.restore(s);
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        updateVariablesPanel();
    }

    public void onReset() {
        stopTimeline();
        history.clear();
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
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
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
    }

    private void appendProgress(String line) {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.appendText(line + "\n");
    }

    private void updateVariablesPanel() {
        if (parent == null || parent.variableList == null) return;
        parent.variableList.getItems().clear();
        parent.variableList.getItems().addAll(
                "array: " + Arrays.toString(solver.getArray()),
                "i: " + solver.getI(),
                "j: " + solver.getJ(),
                "minIndex: " + solver.getMinIndex(),
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
}


