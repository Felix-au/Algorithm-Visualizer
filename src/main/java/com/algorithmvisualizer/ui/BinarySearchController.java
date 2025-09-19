package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.BinarySearchSolver;
import com.algorithmvisualizer.visualization.ArrayRenderer;
import com.algorithmvisualizer.visualization.BarChartRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

/**
 * Controller for Binary Search visualization.
 */
public class BinarySearchController implements AlgorithmViewController.AlgorithmSpecificController {

    // Parent reference and shared controls
    private AlgorithmViewController parent;

    // Visuals
    private BarChartRenderer barChart;
    private ArrayRenderer arrayView;

    // Solver
    private BinarySearchSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;
    private Timeline compareBlinkTimeline;
    private Timeline elimBlinkTimeline;

    // Flags to block stepping during waits/blinks
    private boolean pendingMidWait = false;
    private boolean pendingCompareBlink = false;
    private boolean pendingElimBlink = false;

    // History for step-back
    private final Deque<BinarySearchSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Data
    private int[] currentArray = new int[] { 1, 3, 5, 7, 9, 11 };
    private int targetValue = 7;

    @FXML
    private void initialize() {
        barChart = new BarChartRenderer();
        arrayView = new ArrayRenderer();
        renderVisuals();

        solver = new BinarySearchSolver(currentArray, targetValue);
        solver.setStepListener(this::onStepEvent);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place visuals
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            StackPane centered = new StackPane(barChart.getNode());
            parent.chessboardContainer.getChildren().add(centered);
        }

        // Header and legend
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Binary Search Visualization");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(hdr, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(15.0);
            javafx.scene.shape.Rectangle midRect = new javafx.scene.shape.Rectangle(12,12);
            midRect.setFill(javafx.scene.paint.Color.GOLD);
            midRect.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lMid = new HBox(5.0, midRect, new Label("Mid"));
            javafx.scene.shape.Rectangle elimRect = new javafx.scene.shape.Rectangle(12,12);
            elimRect.setFill(javafx.scene.paint.Color.RED);
            elimRect.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lElim = new HBox(5.0, elimRect, new Label("Eliminated"));
            javafx.scene.shape.Rectangle foundRect = new javafx.scene.shape.Rectangle(12,12);
            foundRect.setFill(javafx.scene.paint.Color.FORESTGREEN);
            foundRect.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lFound = new HBox(5.0, foundRect, new Label("Found"));
            row.getChildren().addAll(lMid, lElim, lFound);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters wiring (reuse sorting UI + Target value)
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Array size:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, currentArray.length));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onParamSizeChanged(n));
        }
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Value of elements (sorted ascending):");
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
        // Reuse the hidden Queens controls as Target control
        if (parent.paramNumQueensSpinner != null) {
            parent.paramNumQueensSpinner.setVisible(true);
            parent.paramNumQueensSpinner.setManaged(true);
            parent.paramNumQueensSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1_000_000, 1_000_000, targetValue));
            parent.paramNumQueensSpinner.valueProperty().addListener((obs, o, n) -> onTargetChanged(n));
        }
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyArrayFromParent());
        if (parent.paramRandomizeButton != null) parent.paramRandomizeButton.setOnAction(e -> onRandomizeFromParent());

        // Solutions side: show Array View
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
            // Mid = Yellow
            HBox lMid = new HBox(5.0);
            javafx.scene.shape.Rectangle rMid = new javafx.scene.shape.Rectangle(12, 12);
            rMid.setFill(javafx.scene.paint.Color.GOLD);
            rMid.setStroke(javafx.scene.paint.Color.BLACK);
            lMid.getChildren().addAll(rMid, new Label("Mid"));
            // Eliminated = Red
            HBox lElim = new HBox(5.0);
            javafx.scene.shape.Rectangle rElim = new javafx.scene.shape.Rectangle(12, 12);
            rElim.setFill(javafx.scene.paint.Color.RED);
            rElim.setStroke(javafx.scene.paint.Color.BLACK);
            lElim.getChildren().addAll(rElim, new Label("Eliminated"));
            // Found = Green
            HBox lFound = new HBox(5.0);
            javafx.scene.shape.Rectangle rFound = new javafx.scene.shape.Rectangle(12, 12);
            rFound.setFill(javafx.scene.paint.Color.FORESTGREEN);
            rFound.setStroke(javafx.scene.paint.Color.BLACK);
            lFound.getChildren().addAll(rFound, new Label("Found"));
            legendRow.getChildren().addAll(lMid, lElim, lFound);
            parent.solutionsSubHeaderBox.getChildren().add(legendRow);
        }
        moveArrayViewToSolutions();

        // Controls
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        // Unify Play/Pause behavior: hide dedicated Pause button; Play acts as toggle
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        // Code + logs + variables
        renderCode();
        initProgressLog();
        updateVariablesPanel();

        // Step description
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");
    }

    // --- Parameter handlers ---
    private void onParamSizeChanged(int size) {
        if (size <= 0) size = 1;
        currentArray = randomSortedArray(size);
        rebuildParentElementBoxes();
        refreshAll();
    }

    private void onTargetChanged(int target) {
        targetValue = target;
        solver.setTarget(targetValue);
        refreshAllRenderOnly();
    }

    private void onRandomizeFromParent() {
        int size = parent != null && parent.paramBoardSizeSpinner != null ? parent.paramBoardSizeSpinner.getValue() : currentArray.length;
        currentArray = randomSortedArray(size);
        if (parent != null && parent.paramElementsField != null) parent.paramElementsField.setText(join(currentArray));
        rebuildParentElementBoxes();
        refreshAll();
    }

    private void onApplyArrayFromParent() {
        int[] parsed = readArrayFromParentBoxes();
        if (parsed != null) {
            currentArray = parsed;
        }
        refreshAll();
    }

    private void refreshAll() {
        stopTimeline();
        solver.setArray(currentArray);
        solver.setTarget(targetValue);
        renderVisuals();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
    }

    private void refreshAllRenderOnly() {
        renderVisuals();
        renderCode();
        updateVariablesPanel();
    }

    private void renderVisuals() {
        barChart.setData(currentArray);
        arrayView.setData(currentArray);
        if (parent != null && parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(barChart.getNode());
        }
        moveArrayViewToSolutions();
    }

    private void moveArrayViewToSolutions() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        parent.solutionsContent.getChildren().add(arrayView.getNode());
    }

    // --- Playback controls ---
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
        if (pendingMidWait || pendingCompareBlink || pendingElimBlink) return;
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        BinarySearchSolver.State s = history.pop();
        solver.restore(s);
        // Reset flags
        pendingMidWait = pendingCompareBlink = pendingElimBlink = false;
        // Re-render
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        // Repaint full state for both visuals
        repaintState(solver.getLow(), solver.getMid(), solver.getHigh());
        // Remove logs of last step or any in-progress logs for the current step
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
        currentStepLogLines = 0;
        countLogsForStep = false;
        pendingMidWait = pendingCompareBlink = pendingElimBlink = false;
        solver.reset();
        renderVisuals();
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready.");
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
            if (pendingMidWait || pendingCompareBlink || pendingElimBlink) return;
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    // --- Step event handling and timing ---
    private void onStepEvent(BinarySearchSolver.StepType type, int low, int mid, int high, int target) {
        switch (type) {
            case INIT:
                initProgressLog();
                appendProgress("Binary Search: n = " + currentArray.length + ", target = " + targetValue);
                if (parent != null) parent.stepDescription.setText("Initialized. low=0, high=" + (currentArray.length - 1));
                break;
            case HIGHLIGHT_MID:
                // Start counting logs for this step so we can remove them on step-back
                countLogsForStep = true;
                currentStepLogLines = 0;
                // Stop any ongoing blinks to avoid them overriding the mid yellow highlight
                stopOngoingBlinks();
                // Repaint full state to ensure eliminated reds persist and new mid is highlighted yellow
                repaintState(low, mid, high);
                int n = currentArray.length;
                appendProgress("🔍 Checking mid index " + mid + (mid>=0 && mid<n ? " (value: " + currentArray[mid] + ")" : ""));
                pauseFixed(1.0, () -> pendingMidWait = false, flag -> pendingMidWait = flag);
                break;
            case COMPARE_EQUAL:
                blinkIndex(mid, javafx.scene.paint.Color.FORESTGREEN, 2.0, true);
                appendProgress("✅ Found target at index " + mid);
                break;
            case COMPARE_LESS:
                blinkIndex(mid, javafx.scene.paint.Color.RED, 2.0, false);
                appendProgress("mid value < target ⇒ search right half");
                break;
            case COMPARE_GREATER:
                blinkIndex(mid, javafx.scene.paint.Color.RED, 2.0, false);
                appendProgress("mid value > target ⇒ search left half");
                break;
            case ELIMINATE_LEFT:
                blinkRange(low, mid, 2.0);
                appendProgress("Eliminating left range [" + low + ".." + mid + "]");
                break;
            case ELIMINATE_RIGHT:
                blinkRange(mid, high, 2.0);
                appendProgress("Eliminating right range [" + mid + ".." + high + "]");
                break;
            case MOVE_BOUNDS:
                if (parent != null) parent.stepDescription.setText(solver.getCurrentStepDescription());
                // End of this step: record how many lines were written and reset counter
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
            case DONE_FOUND:
                // handled via green blink persistence
                stopTimeline();
                // Finalize any pending step logs
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
            case DONE_NOT_FOUND:
                appendProgress("❌ Target not found.");
                if (parent != null) parent.stepDescription.setText("Not found.");
                stopTimeline();
                // Finalize any pending step logs
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
        }
        updateVariablesPanel();
    }

    private void pauseFixed(double seconds, Runnable onDone, java.util.function.Consumer<Boolean> flagSetter) {
        // set flag and pause timeline
        flagSetter.accept(true);
        if (isPlaying && timeline != null) timeline.pause();
        PauseTransition pt = new PauseTransition(Duration.seconds(seconds));
        pt.setOnFinished(ev -> {
            if (onDone != null) onDone.run();
            if (isPlaying && timeline != null) timeline.play();
        });
        pt.play();
    }

    private void blinkIndex(int idx, javafx.scene.paint.Color color, double seconds, boolean persistFound) {
        pendingCompareBlink = true;
        if (isPlaying && timeline != null) timeline.pause();
        if (compareBlinkTimeline != null) { compareBlinkTimeline.stop(); compareBlinkTimeline = null; }
        compareBlinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.0), e -> {
                    barChart.setIndexColor(idx, color);
                    arrayView.setIndexColor(idx, color == javafx.scene.paint.Color.FORESTGREEN ? "FORESTGREEN" : "RED");
                }),
                new KeyFrame(Duration.seconds(seconds/2.0), e -> {
                    // toggle only the mid index back to default, keep eliminated reds
                    barChart.setIndexColor(idx, javafx.scene.paint.Color.STEELBLUE);
                    arrayView.setIndexColor(idx, "#f7f7f7");
                }),
                new KeyFrame(Duration.seconds(seconds), e -> {
                    if (persistFound && color == javafx.scene.paint.Color.FORESTGREEN) {
                        barChart.markFound(idx);
                        arrayView.markFound(idx);
                    } else {
                        barChart.setIndexColor(idx, color);
                        arrayView.setIndexColor(idx, "RED");
                    }
                    pendingCompareBlink = false;
                    compareBlinkTimeline = null;
                    if (isPlaying && timeline != null) timeline.play();
                })
        );
        compareBlinkTimeline.play();
    }

    private void blinkRange(int from, int to, double seconds) {
        pendingElimBlink = true;
        if (isPlaying && timeline != null) timeline.pause();
        if (elimBlinkTimeline != null) { elimBlinkTimeline.stop(); elimBlinkTimeline = null; }
        elimBlinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.0), e -> {
                    barChart.setRangeColor(from, to, javafx.scene.paint.Color.RED);
                    arrayView.setRangeColor(from, to, "RED");
                }),
                new KeyFrame(Duration.seconds(seconds/2.0), e -> {
                    // toggle only the elimination range back to default colors
                    barChart.setRangeColor(from, to, javafx.scene.paint.Color.STEELBLUE);
                    arrayView.setRangeColor(from, to, "#f7f7f7");
                }),
                new KeyFrame(Duration.seconds(seconds), e -> {
                    barChart.markEliminatedRange(from, to);
                    arrayView.markEliminatedRange(from, to);
                    pendingElimBlink = false;
                    elimBlinkTimeline = null;
                    if (isPlaying && timeline != null) timeline.play();
                })
        );
        elimBlinkTimeline.play();
    }

    // --- Logging & variables ---
    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        appendProgress("Binary Search initialized.");
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
        for (int i = 0; i < newLen; i++) sb.append(lines[i]).append("\n");
        parent.progressArea.setText(sb.toString());
    }

    private void updateVariablesPanel() {
        if (parent == null || parent.variableList == null) return;
        parent.variableList.getItems().clear();
        int[] arr = solver != null ? solver.getArray() : currentArray;
        int low = solver != null ? solver.getLow() : 0;
        int mid = solver != null ? solver.getMid() : -1;
        int high = solver != null ? solver.getHigh() : arr.length - 1;
        int tgt = targetValue;
        String midVal = (mid >= 0 && mid < arr.length) ? String.valueOf(arr[mid]) : "-";
        parent.variableList.getItems().addAll(
                "Array size: " + arr.length,
                "low: " + low,
                "mid: " + mid + " (val=" + midVal + ")",
                "high: " + high,
                "target: " + tgt,
                "state: " + (solver != null && solver.isDone() ? (solver.getFoundIndex() >= 0 ? "FOUND" : "NOT_FOUND") : "SEARCHING")
        );
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        int size = currentArray.length;
        String arrayValues = Arrays.toString(currentArray).replaceAll("[\\[\\]]", "");
        String[] lines = new String[] {
                "public class BinarySearchExample {",
                "    static final int[] arr = {" + arrayValues + "};",
                "    static final int SIZE = " + size + ";",
                "    static final int TARGET = " + targetValue + ";",
                "",
                "    public static void main(String[] args) {",
                "        System.out.println(\"Binary Search in sorted array of size \" + SIZE);",
                "        System.out.println(\"=====================================\");",
                "        System.out.print(\"Array: \");",
                "        printArray(arr);",
                "        System.out.println(\"Element to search: \" + TARGET);",
                "",
                "        long startTime = System.currentTimeMillis();",
                "",
                "        int result = binarySearch(arr, TARGET);",
                "",
                "        long endTime = System.currentTimeMillis();",
                "        System.out.println(\"=====================================\");",
                "",
                "        if (result == -1) {",
                "            System.out.println(\"Element \" + TARGET + \" not found.\");",
                "        } else {",
                "            System.out.println(\"Element \" + TARGET + \" found at index \" + result);",
                "        }",
                "",
                "        System.out.println(\"Execution time: \" + (endTime - startTime) + \" ms\");",
                "    }",
                "",
                "    static int binarySearch(int[] arr, int target) {",
                "        int left = 0, right = arr.length - 1;",
                "        int step = 1;",
                "",
                "        while (left <= right) {",
                "            int mid = (left + right) / 2;",
                "            System.out.println(\"Step \" + step + \": left=\" + left + \" right=\" + right + \" mid=\" + mid + \" (value=\" + arr[mid] + \")\");",
                "            step++;",
                "",
                "            if (arr[mid] == target) {",
                "                return mid;",
                "            }",
                "            if (arr[mid] < target) {",
                "                left = mid + 1;",
                "            } else {",
                "                right = mid - 1;",
                "            }",
                "        }",
                "        return -1;",
                "    }",
                "",
                "    static void printArray(int[] arr) {",
                "        for (int num : arr) {",
                "            System.out.print(num + \" \" );",
                "        }",
                "        System.out.println();",
                "    }",
                "}",
        };
        parent.codeArea.setText(String.join("\n", lines));
    }

    // --- Helpers: array editing ---
    private void rebuildParentElementBoxes() {
        if (parent == null || parent.paramElementsBox == null) return;
        parent.paramElementsBox.getChildren().clear();
        for (int i = 0; i < currentArray.length; i++) {
            TextField tf = new TextField(String.valueOf(currentArray[i]));
            tf.setPrefWidth(50);
            final int idx = i;
            tf.textProperty().addListener((obs, o, n) -> {
                try {
                    int v = Integer.parseInt(n.trim());
                    currentArray[idx] = v;
                } catch (NumberFormatException ex) {
                    // ignore until apply
                }
            });
            parent.paramElementsBox.getChildren().add(tf);
        }
    }

    private int[] readArrayFromParentBoxes() {
        if (parent == null || parent.paramElementsBox == null) return currentArray;
        int n = parent.paramElementsBox.getChildren().size();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            if (parent.paramElementsBox.getChildren().get(i) instanceof TextField) {
                String txt = ((TextField) parent.paramElementsBox.getChildren().get(i)).getText();
                try {
                    arr[i] = Integer.parseInt(txt.trim());
                } catch (NumberFormatException ex) {
                    return null;
                }
            } else return null;
        }
        Arrays.sort(arr);
        return arr;
    }

    private String join(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private int[] randomSortedArray(int n) {
        Random rnd = new Random();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt(100) - 50; // include negatives
        Arrays.sort(arr);
        return arr;
    }

    // --- State repaint helper to keep eliminated reds persistent and mid yellow ---
    private void repaintState(int low, int mid, int high) {
        int n = currentArray.length;
        // Reset base colors on both visuals
        barChart.setRangeColor(0, n - 1, javafx.scene.paint.Color.STEELBLUE);
        arrayView.setRangeColor(0, n - 1, "#f7f7f7");

        // Persist eliminated ranges
        barChart.markEliminatedRange(0, Math.min(n - 1, low - 1));
        arrayView.markEliminatedRange(0, Math.min(n - 1, low - 1));
        barChart.markEliminatedRange(Math.max(0, high + 1), n - 1);
        arrayView.markEliminatedRange(Math.max(0, high + 1), n - 1);

        // Persist found index if exists
        int found = solver != null ? solver.getFoundIndex() : -1;
        if (found >= 0) {
            barChart.markFound(found);
            arrayView.markFound(found);
        }

        // Highlight current mid in yellow (if within bounds and not already found)
        if (mid >= 0 && mid < n && found != mid) {
            barChart.highlightMid(mid);
            arrayView.highlightMid(mid);
        }
    }

    private void stopOngoingBlinks() {
        if (compareBlinkTimeline != null) { compareBlinkTimeline.stop(); compareBlinkTimeline = null; }
        if (elimBlinkTimeline != null) { elimBlinkTimeline.stop(); elimBlinkTimeline = null; }
    }
}
