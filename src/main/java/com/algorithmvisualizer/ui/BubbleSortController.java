package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.BubbleSortSolver;
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

public class BubbleSortController implements AlgorithmViewController.AlgorithmSpecificController {

    @FXML private Spinner<Integer> arraySizeSpinner;
    @FXML private TextField arrayElementsField;
    @FXML private FlowPane arrayElementsBox;
    @FXML private Button randomizeButton;
    @FXML private Button applyArrayButton;
    @FXML private Label statusLabel;

    private AlgorithmViewController parent;

    private BarChartRenderer barChart;
    private ArrayRenderer arrayView;

    private BubbleSortSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;
    private final Deque<BubbleSortSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Track number of completed passes (sorted suffix)
    private int passesCompleted = 0;
    private boolean completionShown = false;

    private int[] currentArray = new int[] {5, 3, 8, 4, 2};

    private boolean pendingBlinkDelay = false;
    private boolean pendingMarkedDelay = false;

    @FXML
    private void initialize() {
        if (arraySizeSpinner != null) {
            arraySizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, currentArray.length));
        }
        if (arrayElementsField != null) {
            arrayElementsField.setText(join(currentArray));
        }
        if (arrayElementsBox != null) { rebuildLocalElementBoxes(); }
        if (randomizeButton != null) randomizeButton.setOnAction(e -> onRandomize());
        if (applyArrayButton != null) applyArrayButton.setOnAction(e -> onApplyArray());

        barChart = new BarChartRenderer();
        arrayView = new ArrayRenderer();
        renderVisuals();

        solver = new BubbleSortSolver(currentArray);
        solver.setStepListener(this::onStepEvent);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            StackPane centeredChart = new StackPane(barChart.getNode());
            parent.chessboardContainer.getChildren().add(centeredChart);
        }

        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());

        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, currentArray.length));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onParamSizeChanged(n));
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(true);
            parent.paramElementsBox.setManaged(true);
            rebuildParentElementBoxes();
        }
        if (parent.paramElementsField != null) { parent.paramElementsField.setVisible(false); parent.paramElementsField.setManaged(false); }
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setVisible(true);
            parent.paramRandomizeButton.setManaged(true);
            parent.paramRandomizeButton.setOnAction(e -> onRandomizeFromParent());
        }
        if (parent.paramNumQueensSpinner != null) { parent.paramNumQueensSpinner.setVisible(false); parent.paramNumQueensSpinner.setManaged(false); }
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyArrayFromParent());

        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label chartHeader = new Label("Bubble Sort Visualization");
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
            HBox l1 = new HBox(5.0);
            javafx.scene.shape.Rectangle r1 = new javafx.scene.shape.Rectangle(12, 12);
            r1.setFill(javafx.scene.paint.Color.GOLD);
            r1.setStroke(javafx.scene.paint.Color.BLACK);
            l1.getChildren().addAll(r1, new Label("Compare"));
            HBox l2 = new HBox(5.0);
            javafx.scene.shape.Rectangle r2 = new javafx.scene.shape.Rectangle(12, 12);
            r2.setFill(javafx.scene.paint.Color.CRIMSON);
            r2.setStroke(javafx.scene.paint.Color.BLACK);
            l2.getChildren().addAll(r2, new Label("Swap"));
            HBox l3 = new HBox(5.0);
            javafx.scene.shape.Rectangle r3 = new javafx.scene.shape.Rectangle(12, 12);
            r3.setFill(javafx.scene.paint.Color.FORESTGREEN);
            r3.setStroke(javafx.scene.paint.Color.BLACK);
            l3.getChildren().addAll(r3, new Label("Sorted Suffix"));
            legendRow.getChildren().addAll(l1, l2, l3);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, legendRow);
        }
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Array size:");
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        renderCode();
        initProgressLog();
        updateVariablesPanel();

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
            HBox l1 = new HBox(5.0);
            javafx.scene.shape.Rectangle r1 = new javafx.scene.shape.Rectangle(12, 12);
            r1.setFill(javafx.scene.paint.Color.GOLD);
            r1.setStroke(javafx.scene.paint.Color.BLACK);
            l1.getChildren().addAll(r1, new Label("Compare"));
            HBox l2 = new HBox(5.0);
            javafx.scene.shape.Rectangle r2 = new javafx.scene.shape.Rectangle(12, 12);
            r2.setFill(javafx.scene.paint.Color.CRIMSON);
            r2.setStroke(javafx.scene.paint.Color.BLACK);
            l2.getChildren().addAll(r2, new Label("Swap"));
            HBox l3 = new HBox(5.0);
            javafx.scene.shape.Rectangle r3 = new javafx.scene.shape.Rectangle(12, 12);
            r3.setFill(javafx.scene.paint.Color.FORESTGREEN);
            r3.setStroke(javafx.scene.paint.Color.BLACK);
            l3.getChildren().addAll(r3, new Label("Sorted Suffix"));
            legendRow.getChildren().addAll(l1, l2, l3);
            parent.solutionsSubHeaderBox.getChildren().add(legendRow);
        }
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
        if (parent != null && parent.paramElementsField != null) parent.paramElementsField.setText(join(currentArray));
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
        completionShown = false;
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

    private void onStepEvent(BubbleSortSolver.StepType type, int i, int j) {
        int n = solver.getArray().length;
        switch (type) {
            case INIT_PASS:
                countLogsForStep = true; currentStepLogLines = 0;
                if (parent != null) parent.stepDescription.setText("Start pass i=" + i);
                restoreSortedSuffixHighlighting();
                break;
            case COMPARE:
                barChart.clearHighlights();
                arrayView.clearHighlights();
                restoreSortedSuffixHighlighting();
                barChart.highlightCompare(j, j + 1);
                arrayView.highlightCompare(j, j + 1);
                appendProgress("🔍 Comparing indices " + j + " and " + (j + 1) + " (" + solver.getArray()[j] + ", " + solver.getArray()[j + 1] + ")");
                if (parent != null) parent.stepDescription.setText("Comparing adjacent elements at j=" + j);
                break;
            case SWAP:
                pendingBlinkDelay = true;
                if (isPlaying && timeline != null) { timeline.pause(); }
                startBlinkingAnimation(j, j + 1, () -> {
                    barChart.updateData(solver.getArray());
                    arrayView.updateData(solver.getArray());
                    restoreSortedSuffixHighlighting();
                    appendProgress("🔄 SWAPPED indices " + j + " and " + (j + 1));
                    appendProgress("");
                    pendingBlinkDelay = false;
                    if (isPlaying && timeline != null) { timeline.play(); }
                    if (solver.isDone()) { showCompletion(); }
                });
                break;
            case ADVANCE:
                // no-op visual besides compare highlight already shown
                break;
            case MARK_SORTED:
                int sortedFrom = n - i - 1;
                barChart.markSortedSuffix(sortedFrom);
                arrayView.markSortedSuffix(sortedFrom);
                passesCompleted = i + 1;
                appendProgress("✓ Element at index " + (n - 1 - i) + " is in final position");
                appendProgress("");
                if (countLogsForStep) { progressHistory.push(currentStepLogLines); countLogsForStep = false; currentStepLogLines = 0; }
                if (isPlaying) {
                    pendingMarkedDelay = true;
                    if (timeline != null) { timeline.pause(); }
                    PauseTransition pauseMark = new PauseTransition(Duration.seconds(2));
                    pauseMark.setOnFinished(ev -> { pendingMarkedDelay = false; if (isPlaying && timeline != null) { timeline.play(); } if (solver.isDone()) { showCompletion(); } });
                    pauseMark.play();
                }
                break;
            case DONE:
                if (pendingMarkedDelay || pendingBlinkDelay) return;
                showCompletion();
                if (countLogsForStep) { progressHistory.push(currentStepLogLines); countLogsForStep = false; currentStepLogLines = 0; }
                break;
        }
        updateVariablesPanel();
    }

    private void showCompletion() {
        if (completionShown) return;
        completionShown = true;
        updateVariablesPanel();
        int lastIndex = solver.getArray().length - 1;
        barChart.markSortedSuffix(0);
        arrayView.markSortedSuffix(0);
        PauseTransition completionDelay = new PauseTransition(Duration.seconds(0.5));
        completionDelay.setOnFinished(ev -> {
            appendProgress("🎉 SORTING COMPLETE!");
            appendProgress("✓ All " + (lastIndex + 1) + " elements are now in their correct sorted positions");
            appendProgress("✓ Array is fully sorted in ascending order");
            appendProgress("");
            if (parent != null) parent.stepDescription.setText("🎉 Sorting Complete! All elements are sorted.");
        });
        completionDelay.play();
        stopTimeline();
    }

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
        if (pendingBlinkDelay || pendingMarkedDelay) return;
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (isPlaying) { onPause(); }
        if (history.isEmpty()) return;
        BubbleSortSolver.State s = history.pop();
        solver.restore(s);
        pendingBlinkDelay = false; pendingMarkedDelay = false;
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        barChart.clearHighlights();
        arrayView.clearHighlights();

        int n = solver.getArray().length;
        int i = solver.getI();
        int j = solver.getJ();
        if (solver.isDone()) passesCompleted = n; else passesCompleted = i;

        if (n > 0) {
            if (solver.isDone()) {
                barChart.markSortedSuffix(0);
                arrayView.markSortedSuffix(0);
            } else if (i > 0) {
                int from = n - i;
                barChart.markSortedSuffix(from);
                arrayView.markSortedSuffix(from);
            }
        }

        if (!solver.isDone() && j < n - 1 - i) {
            barChart.highlightCompare(j, j + 1);
            arrayView.highlightCompare(j, j + 1);
        }

        if (parent != null && parent.progressArea != null) {
            if (countLogsForStep && currentStepLogLines > 0) {
                removeLastLogLines(currentStepLogLines);
                currentStepLogLines = 0; countLogsForStep = false;
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
        countLogsForStep = false; currentStepLogLines = 0; passesCompleted = 0; completionShown = false;
        solver.reset();
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        barChart.clearHighlights(); arrayView.clearHighlights();
        initProgressLog(); updateVariablesPanel();
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
            if (pendingBlinkDelay || pendingMarkedDelay) { return; }
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void startBlinkingAnimation(int i, int j, Runnable onComplete) {
        barChart.forceHighlightSwap(i, j);
        arrayView.forceHighlightSwap(i, j);
        Timeline blinkTimeline = new Timeline();
        blinkTimeline.getKeyFrames().addAll(
            new KeyFrame(Duration.seconds(0.0), e -> { barChart.forceHighlightSwap(i, j); arrayView.forceHighlightSwap(i, j); }),
            new KeyFrame(Duration.seconds(0.5), e -> { barChart.clearHighlights(); arrayView.clearHighlights(); }),
            new KeyFrame(Duration.seconds(1.0), e -> { barChart.forceHighlightSwap(i, j); arrayView.forceHighlightSwap(i, j); }),
            new KeyFrame(Duration.seconds(1.5), e -> { barChart.clearHighlights(); arrayView.clearHighlights(); }),
            new KeyFrame(Duration.seconds(2.0), e -> { onComplete.run(); })
        );
        blinkTimeline.play();
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        int size = currentArray.length;
        String arrayValues = java.util.Arrays.toString(currentArray).replaceAll("[\\[\\]]", "");
        String[] lines = new String[] {
                "public class BubbleSortExample {",
                "    static final int SIZE = " + size + ";",
                "    static int[] arr = {" + arrayValues + "};",
                "",
                "    public static void main(String[] args) {",
                "        System.out.println(\"Bubble Sort for array of size \" + SIZE);",
                "        System.out.println(\"=====================================\");",
                "",
                "        System.out.print(\"Original Array: \");",
                "        printArray(arr);",
                "",
                "        long startTime = System.currentTimeMillis();",
                "",
                "        bubbleSort(arr);",
                "",
                "        long endTime = System.currentTimeMillis();",
                "        System.out.println(\"=====================================\");",
                "        System.out.print(\"Sorted Array:   \");",
                "        printArray(arr);",
                "        System.out.println(\"Execution time: \" + (endTime - startTime) + \" ms\");",
                "    }",
                "",
                "    static void bubbleSort(int[] arr) {",
                "        int n = arr.length;",
                "        for (int i = 0; i < n - 1; i++) {",
                "            for (int j = 0; j < n - 1 - i; j++) {",
                "                if (arr[j] > arr[j + 1]) {",
                "                    int temp = arr[j];",
                "                    arr[j] = arr[j + 1];",
                "                    arr[j + 1] = temp;",
                "                }",
                "            }",
                "            System.out.print(\"After pass \" + (i + 1) + \": \");",
                "            printArray(arr);",
                "        }",
                "    }",
                "",
                "    static void printArray(int[] arr) {",
                "        for (int num : arr) {",
                "            System.out.print(num + \" \" );",
                "        }",
                "        System.out.println();",
                "    }",
                "}"
        };
        parent.codeArea.setText(String.join("\n", lines));
    }

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        appendProgress("Bubble Sort: n = " + currentArray.length);
        appendProgress("Starting...");
        progressHistory.clear();
        currentStepLogLines = 0; countLogsForStep = false;
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
                "i (passes completed): " + passesCompleted,
                "j (current index): " + solver.getJ(),
                // "i (pass; sorted suffix length): " + solver.getI(),
                "state: " + (solver.isDone() ? "DONE" : "RUNNING")
        );
    }

    private void restoreSortedSuffixHighlighting() {
        int n = solver.getArray().length;
        int sortedFrom = Math.max(0, n - passesCompleted);
        if (passesCompleted > 0) {
            barChart.markSortedSuffix(sortedFrom);
            arrayView.markSortedSuffix(sortedFrom);
        }
        if (solver.getI() == n - 1) {
            barChart.markSortedSuffix(0);
            arrayView.markSortedSuffix(0);
        }
    }

    private static int[] parseArray(String text) {
        if (text == null || text.trim().isEmpty()) return new int[0];
        String[] parts = text.split(",");
        int[] arr = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) arr[i] = Integer.parseInt(parts[i].trim());
            return arr;
        } catch (NumberFormatException ex) { return null; }
    }

    private static String join(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) { if (i > 0) sb.append(", "); sb.append(arr[i]); }
        return sb.toString();
    }

    private static int[] randomArray(int n) {
        Random rnd = new Random();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(100) - 50;
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
        if (arrayElementsBox == null || arrayElementsBox.getChildren().isEmpty()) return parseArray(arrayElementsField != null ? arrayElementsField.getText() : null);
        int n = arrayElementsBox.getChildren().size();
        int[] arr = new int[n];
        try {
            for (int i = 0; i < n; i++) {
                String txt = ((TextField) arrayElementsBox.getChildren().get(i)).getText();
                arr[i] = Integer.parseInt(txt.trim());
            }
        } catch (Exception ex) { return null; }
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
        if (parent == null || parent.paramElementsBox == null || parent.paramElementsBox.getChildren().isEmpty()) return null;
        int n = parent.paramElementsBox.getChildren().size();
        int[] arr = new int[n];
        try {
            for (int i = 0; i < n; i++) {
                String txt = ((TextField) parent.paramElementsBox.getChildren().get(i)).getText();
                arr[i] = Integer.parseInt(txt.trim());
            }
        } catch (Exception ex) { return null; }
        return arr;
    }
}


