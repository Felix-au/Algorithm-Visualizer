package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.LinearSearchSolver;
import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.code.implementations.LinearSearchCode;
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
 * Controller for Linear Search visualization with simplified color scheme.
 * YELLOW (checking) → RED (not target) or GREEN (found)
 */
public class LinearSearchController implements AlgorithmViewController.AlgorithmSpecificController {

    // Parent reference and shared controls
    private AlgorithmViewController parent;

    // Visuals
    private BarChartRenderer barChart;
    private ArrayRenderer arrayView;

    // Solver
    private LinearSearchSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;
    private Timeline notTargetBlinkTimeline;
    private Timeline foundBlinkTimeline;

    // Flags to block stepping during waits/blinks
    private boolean pendingCheckPause = false;
    private boolean pendingNotTargetBlink = false;
    private boolean pendingFoundBlink = false;

    // History for step-back
    private final Deque<LinearSearchSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Data
    private int[] currentArray = new int[] { 15, 8, 23, 42, 7, 19, 33, 12, 51, 28 };
    private int targetValue = 23;
    private LinearSearchCode codeImpl; // Multi-language code implementation

    @FXML
    private void initialize() {
        barChart = new BarChartRenderer();
        arrayView = new ArrayRenderer();
        renderVisuals();

        solver = new LinearSearchSolver(currentArray, targetValue);
        solver.setStepListener(this::onStepEvent);
        
        // Initialize code implementation
        codeImpl = new LinearSearchCode();
        codeImpl.updateParameters(currentArray, targetValue);
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
            Label hdr = new Label("Linear Search Visualization");
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
            
            // Checking (Yellow)
            javafx.scene.shape.Rectangle checkRect = new javafx.scene.shape.Rectangle(12,12);
            checkRect.setFill(javafx.scene.paint.Color.GOLD);
            checkRect.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lCheck = new HBox(5.0, checkRect, new Label("Checking"));
            
            // Not Target (Red)
            javafx.scene.shape.Rectangle elimRect = new javafx.scene.shape.Rectangle(12,12);
            elimRect.setFill(javafx.scene.paint.Color.RED);
            elimRect.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lElim = new HBox(5.0, elimRect, new Label("Not Target"));
            
            // Found (Green)
            javafx.scene.shape.Rectangle foundRect = new javafx.scene.shape.Rectangle(12,12);
            foundRect.setFill(javafx.scene.paint.Color.FORESTGREEN);
            foundRect.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lFound = new HBox(5.0, foundRect, new Label("Found"));
            
            row.getChildren().addAll(lCheck, lElim, lFound);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters wiring
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
            parent.paramElementsLabel.setText("Value of elements (can be unsorted):");
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
        
        // Target control (reuse Queens spinner)
        if (parent.paramTargetLabel != null) {
            parent.paramTargetLabel.setVisible(true);
            parent.paramTargetLabel.setManaged(true);
            parent.paramTargetLabel.setText("Target:");
        }
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
            
            HBox lCheck = new HBox(5.0);
            javafx.scene.shape.Rectangle rCheck = new javafx.scene.shape.Rectangle(12, 12);
            rCheck.setFill(javafx.scene.paint.Color.GOLD);
            rCheck.setStroke(javafx.scene.paint.Color.BLACK);
            lCheck.getChildren().addAll(rCheck, new Label("Checking"));
            
            HBox lElim = new HBox(5.0);
            javafx.scene.shape.Rectangle rElim = new javafx.scene.shape.Rectangle(12, 12);
            rElim.setFill(javafx.scene.paint.Color.RED);
            rElim.setStroke(javafx.scene.paint.Color.BLACK);
            lElim.getChildren().addAll(rElim, new Label("Not Target"));
            
            HBox lFound = new HBox(5.0);
            javafx.scene.shape.Rectangle rFound = new javafx.scene.shape.Rectangle(12, 12);
            rFound.setFill(javafx.scene.paint.Color.FORESTGREEN);
            rFound.setStroke(javafx.scene.paint.Color.BLACK);
            lFound.getChildren().addAll(rFound, new Label("Found"));
            
            legendRow.getChildren().addAll(lCheck, lElim, lFound);
            parent.solutionsSubHeaderBox.getChildren().add(legendRow);
        }
        moveArrayViewToSolutions();

        // Controls
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        // Code + logs + variables
        parent.setCurrentAlgorithmName("Linear Search");
        renderCode();
        initProgressLog();
        updateVariablesPanel();

        // Step description
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");
    }

    // --- Parameter handlers ---
    private void onParamSizeChanged(int size) {
        if (size <= 0) size = 1;
        currentArray = randomUnsortedArray(size);
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
        currentArray = randomUnsortedArray(size);
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
        if (pendingCheckPause || pendingNotTargetBlink || pendingFoundBlink) return;
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        
        // Stop all ongoing animations immediately
        stopOngoingBlinks();
        
        // Reset all blocking flags
        pendingCheckPause = false;
        pendingNotTargetBlink = false;
        pendingFoundBlink = false;
        
        // Restore solver state
        LinearSearchSolver.State s = history.pop();
        solver.restore(s);
        
        // Completely rebuild visuals from scratch
        barChart.updateData(solver.getArray());
        arrayView.updateData(solver.getArray());
        
        // Repaint full state based on restored solver state
        repaintState(solver.getCurrentIndex());
        
        // Remove logs
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
        
        // Update UI components
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
        pendingCheckPause = pendingNotTargetBlink = pendingFoundBlink = false;
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
        double speed = parent != null ? parent.speedSlider.getValue() : 1.0;
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (solver.isDone()) { stopTimeline(); return; }
            if (pendingCheckPause || pendingNotTargetBlink || pendingFoundBlink) return;
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    // --- Step event handling and timing ---
    private void onStepEvent(LinearSearchSolver.StepType type, int currentIndex, int target, int comparisons) {
        switch (type) {
            case INIT:
                initProgressLog();
                appendProgress("Linear Search: n = " + currentArray.length + ", target = " + targetValue);
                if (parent != null) parent.stepDescription.setText("Initialized. Ready to search.");
                break;
                
            case CHECK_INDEX:
                // Stop any ongoing blinks
                stopOngoingBlinks();
                
                // CRITICAL: Finalize previous index if it was being checked
                // This prevents the "yellow leak" when stepping back and playing
                if (currentIndex > 0) {
                    int prevIdx = currentIndex - 1;
                    // Check if previous index should be RED (not target)
                    // It should be RED if we moved past it (meaning it wasn't the target)
                    if (solver.getFoundIndex() != prevIdx) {
                        barChart.markEliminated(prevIdx);
                        arrayView.markEliminated(prevIdx);
                    }
                }
                
                // Highlight current index in YELLOW IMMEDIATELY
                barChart.highlightChecking(currentIndex);
                arrayView.highlightChecking(currentIndex);
                
                int n = currentArray.length;
                appendProgress("🔍 Checking index " + currentIndex + (currentIndex>=0 && currentIndex<n ? " (value: " + currentArray[currentIndex] + ")" : ""));
                
                // CRITICAL: Pause for 0.5s to show YELLOW before comparison
                // This blocks the next step() call which would trigger NOT_TARGET/FOUND_TARGET
                pauseFixed(0.5, () -> pendingCheckPause = false, flag -> pendingCheckPause = flag);
                break;
                
            case NOT_TARGET:
                // Blink YELLOW → RED, persist RED
                blinkNotTarget(currentIndex, 1.5);
                appendProgress("❌ Not target. Moving to next...");
                
                // End of this comparison step: record log lines
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
                
            case FOUND_TARGET:
                // Blink YELLOW → GREEN, persist GREEN
                blinkFound(currentIndex, 2.0);
                appendProgress("✅ Target found at index " + currentIndex + "!");
                
                // End of this comparison step: record log lines
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
                
            case MOVE_NEXT:
                if (parent != null) parent.stepDescription.setText(solver.getCurrentStepDescription());
                
                // Start counting logs for NEXT step (the next CHECK_INDEX)
                countLogsForStep = true;
                currentStepLogLines = 0;
                break;
                
            case DONE_FOUND:
                stopTimeline();
                appendProgress("Search complete. Total comparisons: " + comparisons);
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
                
            case DONE_NOT_FOUND:
                appendProgress("❌ Target not found after checking all " + currentArray.length + " elements.");
                if (parent != null) parent.stepDescription.setText("Not found.");
                stopTimeline();
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
        flagSetter.accept(true);
        if (isPlaying && timeline != null) timeline.pause();
        PauseTransition pt = new PauseTransition(Duration.seconds(seconds));
        pt.setOnFinished(ev -> {
            if (onDone != null) onDone.run();
            if (isPlaying && timeline != null) timeline.play();
        });
        pt.play();
    }

    private void blinkNotTarget(int idx, double seconds) {
        pendingNotTargetBlink = true;
        if (isPlaying && timeline != null) timeline.pause();
        if (notTargetBlinkTimeline != null) { notTargetBlinkTimeline.stop(); notTargetBlinkTimeline = null; }
        
        // YELLOW is already visible for 0.5s from CHECK_INDEX pause
        // Now just do the blink animation
        notTargetBlinkTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0.0), e -> {
                // Start blink to RED immediately (YELLOW already shown for 0.5s)
                barChart.setIndexColor(idx, javafx.scene.paint.Color.RED);
                arrayView.setIndexColor(idx, "RED");
            }),
            new KeyFrame(Duration.seconds(seconds/2.0), e -> {
                // Blink back to YELLOW
                barChart.setIndexColor(idx, javafx.scene.paint.Color.GOLD);
                arrayView.setIndexColor(idx, "GOLD");
            }),
            new KeyFrame(Duration.seconds(seconds), e -> {
                // Persist RED (eliminated)
                barChart.markEliminated(idx);
                arrayView.markEliminated(idx);
                pendingNotTargetBlink = false;
                notTargetBlinkTimeline = null;
                if (isPlaying && timeline != null) timeline.play();
            })
        );
        notTargetBlinkTimeline.play();
    }

    private void blinkFound(int idx, double seconds) {
        pendingFoundBlink = true;
        if (isPlaying && timeline != null) timeline.pause();
        if (foundBlinkTimeline != null) { foundBlinkTimeline.stop(); foundBlinkTimeline = null; }
        
        // YELLOW is already visible for 0.5s from CHECK_INDEX pause
        // Now just do the blink animation
        foundBlinkTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0.0), e -> {
                // Start blink to GREEN immediately (YELLOW already shown for 0.5s)
                barChart.setIndexColor(idx, javafx.scene.paint.Color.FORESTGREEN);
                arrayView.setIndexColor(idx, "FORESTGREEN");
            }),
            new KeyFrame(Duration.seconds(seconds/3.0), e -> {
                // Blink to YELLOW
                barChart.setIndexColor(idx, javafx.scene.paint.Color.GOLD);
                arrayView.setIndexColor(idx, "GOLD");
            }),
            new KeyFrame(Duration.seconds(2.0 * seconds/3.0), e -> {
                // Blink to GREEN again
                barChart.setIndexColor(idx, javafx.scene.paint.Color.FORESTGREEN);
                arrayView.setIndexColor(idx, "FORESTGREEN");
            }),
            new KeyFrame(Duration.seconds(seconds), e -> {
                // Persist GREEN (found!)
                barChart.markFound(idx);
                arrayView.markFound(idx);
                pendingFoundBlink = false;
                foundBlinkTimeline = null;
                if (isPlaying && timeline != null) timeline.play();
            })
        );
        foundBlinkTimeline.play();
    }

    // --- Logging & variables ---
    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        appendProgress("Linear Search initialized.");
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
        int curIdx = solver != null ? solver.getCurrentIndex() : 0;
        int tgt = targetValue;
        int comp = solver != null ? solver.getComparisons() : 0;
        String curVal = (curIdx >= 0 && curIdx < arr.length) ? String.valueOf(arr[curIdx]) : "-";
        parent.variableList.getItems().addAll(
            "Array size: " + arr.length,
            "Current index: " + curIdx,
            "Current value: " + curVal,
            "Target: " + tgt,
            "Comparisons: " + comp,
            "State: " + (solver != null && solver.isDone() ? (solver.getFoundIndex() >= 0 ? "FOUND" : "NOT_FOUND") : "SEARCHING")
        );
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        
        // Update code implementation with current parameters
        if (codeImpl != null) {
            codeImpl.updateParameters(currentArray, targetValue);
        }
        
        // Check if we should use multi-language code from repository
        AlgorithmCode repoCode = CodeRepository.getCode("Linear Search");
        if (repoCode instanceof LinearSearchCode && parent.languageSelector != null) {
            // Update the repository code with current parameters
            ((LinearSearchCode) repoCode).updateParameters(currentArray, targetValue);
            
            // Use code from repository (supports multiple languages)
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
        
        // Fallback to original Java code if repository code not available
        int size = currentArray.length;
        String arrayValues = Arrays.toString(currentArray).replaceAll("[\\[\\]]", "");
        String[] lines = new String[] {
            "public class LinearSearchExample {",
            "    static final int[] arr = {" + arrayValues + "};",
            "    static final int SIZE = " + size + ";",
            "    static final int TARGET = " + targetValue + ";",
            "",
            "    public static void main(String[] args) {",
            "        System.out.println(\"Linear Search in array of size \" + SIZE);",
            "        System.out.println(\"=====================================\");",
            "        System.out.print(\"Array: \");",
            "        printArray(arr);",
            "        System.out.println(\"Element to search: \" + TARGET);",
            "",
            "        long startTime = System.currentTimeMillis();",
            "",
            "        int result = linearSearch(arr, TARGET);",
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
            "    static int linearSearch(int[] arr, int target) {",
            "        int comparisons = 0;",
            "        ",
            "        for (int i = 0; i < arr.length; i++) {",
            "            comparisons++;",
            "            System.out.println(\"Step \" + comparisons + \": Checking index \" + i + \" (value: \" + arr[i] + \")\");",
            "            ",
            "            if (arr[i] == target) {",
            "                System.out.println(\"Match found at index \" + i);",
            "                return i;",
            "            }",
            "        }",
            "        ",
            "        System.out.println(\"Element not found after \" + comparisons + \" comparisons\");",
            "        return -1;",
            "    }",
            "",
            "    static void printArray(int[] arr) {",
            "        for (int num : arr) {",
            "            System.out.print(num + \" \");",
            "        }",
            "        System.out.println();",
            "    }",
            "}",
        };
        parent.codeArea.replaceText(String.join("\n", lines));
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
        // NO SORTING - Linear search works on unsorted arrays!
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

    private int[] randomUnsortedArray(int n) {
        Random rnd = new Random();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt(100) - 50; // -50 to 49
        // NO SORTING - keep it unsorted!
        return arr;
    }

    // --- State repaint helper ---
    private void repaintState(int currentIndex) {
        int n = currentArray.length;
        
        // Reset ALL colors to default first (complete clean slate)
        for (int i = 0; i < n; i++) {
            barChart.setIndexColor(i, javafx.scene.paint.Color.STEELBLUE);
            arrayView.setIndexColor(i, "#f7f7f7");
        }

        // Check if search is done
        boolean isDone = solver != null && solver.isDone();
        int found = solver != null ? solver.getFoundIndex() : -1;
        
        if (isDone && found >= 0) {
            // If found, mark all indices before found as RED, and found as GREEN
            for (int i = 0; i < found && i < n; i++) {
                barChart.markEliminated(i);
                arrayView.markEliminated(i);
            }
            barChart.markFound(found);
            arrayView.markFound(found);
        } else if (isDone && found < 0) {
            // If not found, mark all indices as RED
            for (int i = 0; i < n; i++) {
                barChart.markEliminated(i);
                arrayView.markEliminated(i);
            }
        } else {
            // In progress: mark all indices before current as RED
            for (int i = 0; i < currentIndex && i < n; i++) {
                barChart.markEliminated(i);
                arrayView.markEliminated(i);
            }
            
            // Highlight current index in YELLOW (if within bounds)
            if (currentIndex >= 0 && currentIndex < n) {
                barChart.highlightChecking(currentIndex);
                arrayView.highlightChecking(currentIndex);
            }
        }
    }

    private void stopOngoingBlinks() {
        if (notTargetBlinkTimeline != null) {
            notTargetBlinkTimeline.stop();
            notTargetBlinkTimeline = null;
        }
        if (foundBlinkTimeline != null) {
            foundBlinkTimeline.stop();
            foundBlinkTimeline = null;
        }
        // Reset blink flags
        pendingNotTargetBlink = false;
        pendingFoundBlink = false;
    }
}
