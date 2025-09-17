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
import javafx.scene.layout.VBox;
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

    private int[] currentArray = new int[] {5, 3, 8, 4, 2};

    // Helper to track if we should delay after swap during play
    private boolean pendingSwapDelay = false;

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
                if (parent != null) parent.stepDescription.setText("Start outer iteration i=" + i);
                break;
            case COMPARE:
                barChart.clearHighlights();
                arrayView.clearHighlights();
                barChart.highlightCompare(j, minIndex);
                arrayView.highlightCompare(j, minIndex);
                barChart.highlightMin(minIndex);
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
                appendProgress("");
                break;
            case SWAP:
                barChart.highlightSwap(i, minIndex);
                arrayView.highlightSwap(i, minIndex);
                barChart.updateData(solver.getArray());
                arrayView.updateData(solver.getArray());
                appendProgress("Swap i=" + i + " with minIndex=" + minIndex);
                appendProgress("");
                // Add 1-second pause after swaps during play
                if (isPlaying) {
                    pendingSwapDelay = true;
                    if (timeline != null) {
                        timeline.pause();
                    }
                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(ev -> {
                        pendingSwapDelay = false;
                        if (isPlaying) {
                            if (timeline != null) {
                                timeline.play();
                            }
                        }
                    });
                    pause.play();
                }
                break;
            case MARK_SORTED:
                barChart.markSortedPrefix(i);
                arrayView.markSortedPrefix(i);
                appendProgress("Marked index " + i + " as sorted");
                appendProgress("");
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
            if (pendingSwapDelay) { return; }
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
                "i (current pass / sorted boundary): " + solver.getI(),
                "j (current index): " + (solver.getJ() == currentArray.length ? solver.getJ() + " (exhausted)" : solver.getJ()),
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


