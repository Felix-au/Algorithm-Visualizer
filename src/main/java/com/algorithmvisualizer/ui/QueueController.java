package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.QueueSolver;
import com.algorithmvisualizer.ui.AlgorithmViewController.AlgorithmSpecificController;
import com.algorithmvisualizer.visualization.QueueVisualizer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;

public class QueueController implements AlgorithmSpecificController {

    private AlgorithmViewController parent;

    private QueueVisualizer visualizer;
    private QueueSolver solver;

    // Controls
    private Button enqButton;
    private Button deqButton;
    private Button searchButton;
    private Button searchDeqButton;
    private Button reverseButton;
    private Button clearButton;

    private final Deque<QueueSolver.State> history = new ArrayDeque<>();

    private Timeline timeline;
    private boolean isPlaying = false;

    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private boolean countLogsForStep = false;
    private int currentStepLogLines = 0;

    @FXML
    private void initialize() {
        visualizer = new QueueVisualizer();
        solver = new QueueSolver(5, this::onStep);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place visuals
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(visualizer.getNode());
        }

        // Legend
        if (parent.chessboardHeaderBox != null) parent.chessboardHeaderBox.setVisible(true);
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(12);
            javafx.scene.shape.Rectangle main = new javafx.scene.shape.Rectangle(14, 14, javafx.scene.paint.Color.web("#4a90e2"));
            main.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Rectangle auxQ = new javafx.scene.shape.Rectangle(14, 14, javafx.scene.paint.Color.web("#95a5a6"));
            auxQ.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Rectangle auxS = new javafx.scene.shape.Rectangle(14, 14, javafx.scene.paint.Color.web("#8e44ad"));
            auxS.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Rectangle searching = new javafx.scene.shape.Rectangle(14, 14, javafx.scene.paint.Color.web("#f39c12"));
            searching.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Rectangle found = new javafx.scene.shape.Rectangle(14, 14, javafx.scene.paint.Color.web("#27ae60"));
            found.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Rectangle alert = new javafx.scene.shape.Rectangle(14, 14, javafx.scene.paint.Color.TRANSPARENT);
            alert.setStroke(javafx.scene.paint.Color.web("#e74c3c"));
            row.getChildren().addAll(
                main, new Label("Queue"),
                auxQ, new Label("Aux Queue"),
                auxS, new Label("Aux Stack"),
                searching, new Label("Searching"),
                found, new Label("Found"),
                alert, new Label("Alert")
            );
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Capacity:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 5));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onCapacityChanged(n));
        }
        if (parent.paramElementsLabel != null) { parent.paramElementsLabel.setVisible(true); parent.paramElementsLabel.setManaged(true); parent.paramElementsLabel.setText("Value:"); }
        if (parent.paramElementsBox != null) { parent.paramElementsBox.setVisible(false); parent.paramElementsBox.setManaged(false); }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("Enter a number, e.g. 42");
            parent.paramElementsField.setOnAction(e -> { doEnqueue(); parent.paramElementsField.clear(); });
        }
        if (parent.paramRandomizeButton != null) { parent.paramRandomizeButton.setVisible(false); parent.paramRandomizeButton.setManaged(false); }
        if (parent.paramApplyButton != null) { parent.paramApplyButton.setText("Initialize"); parent.paramApplyButton.setOnAction(e -> onInitialize()); }

        addOperationsRow();

        // Playback controls
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.stepBackButton != null) parent.stepBackButton.setOnAction(e -> onStepBack());
        if (parent.resetButton != null) parent.resetButton.setOnAction(e -> onReset());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        // Solutions (Queue concepts)
        if (parent.solutionsHeaderBox != null) { parent.solutionsHeaderBox.setVisible(true); parent.solutionsHeaderBox.setManaged(true); }
        if (parent.solutionsHeaderLabel != null) parent.solutionsHeaderLabel.setText("Queue Concepts");
        if (parent.solutionsSubHeaderBox != null) { parent.solutionsSubHeaderBox.setVisible(false); parent.solutionsSubHeaderBox.setManaged(false); }
        if (parent.solutionsContainer != null) { parent.solutionsContainer.setVisible(true); parent.solutionsContainer.setManaged(true); }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            javafx.scene.layout.VBox bullets = new javafx.scene.layout.VBox(6.0);
            bullets.setStyle("-fx-padding: 4;");
            bullets.getChildren().addAll(
                bullet("Queue: linear data structure following FIFO (First-In, First-Out)"),
                bullet("FIFO: the earliest enqueued element is dequeued first"),
                bullet("Enqueue(x): add x at the tail"),
                bullet("Dequeue(): remove and return the front element"),
                bullet("Search(x): scan from front to tail"),
                bullet("Search & Dequeue(x): move non-targets to Aux Queue, drop x, restore back"),
                bullet("Reverse(): drain to Aux Stack then enqueue back to reverse order")
            );
            parent.solutionsContent.getChildren().add(bullets);
        }

        initProgressLog();
        updateVariables();
        parent.setCurrentAlgorithmName("Queue");
        renderCode();
        
        // Add language selector listener
        if (parent.languageSelector != null) {
            parent.languageSelector.valueProperty().addListener((obs, oldVal, newVal) -> renderCode());
        }
        
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");

        onInitialize();
    }

    private void addOperationsRow() {
        if (parent == null || parent.paramElementsField == null) return;
        javafx.scene.Parent container = parent.paramElementsField.getParent();
        if (!(container instanceof javafx.scene.layout.VBox)) return;
        javafx.scene.layout.VBox vbox = (javafx.scene.layout.VBox) container;

        javafx.scene.layout.VBox opsBox = new javafx.scene.layout.VBox(6.0);
        javafx.scene.layout.HBox row1 = new javafx.scene.layout.HBox(8.0);
        javafx.scene.layout.HBox row2 = new javafx.scene.layout.HBox(8.0);
        opsBox.setStyle("-fx-padding: 4 0 0 0;");

        enqButton = makeButton("Enqueue", "#4a90e2");
        deqButton = makeButton("Dequeue", "#e74c3c");
        searchButton = makeButton("Search", "#7b68ee");
        searchDeqButton = makeButton("Search & Dequeue", "#2ecc71");
        reverseButton = makeButton("Reverse", "#16a085");
        clearButton = makeButton("Clear", "#95a5a6");

        enqButton.setOnAction(e -> doEnqueue());
        deqButton.setOnAction(e -> doDequeue());
        searchButton.setOnAction(e -> doSearch());
        searchDeqButton.setOnAction(e -> doSearchAndDequeue());
        reverseButton.setOnAction(e -> doReverse());
        clearButton.setOnAction(e -> doClear());

        row1.getChildren().addAll(enqButton, deqButton, searchButton);

        if (parent.paramApplyButton != null) {
            javafx.scene.Parent applyParent = parent.paramApplyButton.getParent();
            if (applyParent instanceof javafx.scene.layout.HBox) {
                ((javafx.scene.layout.HBox) applyParent).getChildren().remove(parent.paramApplyButton);
                vbox.getChildren().remove(applyParent);
            }
            parent.paramApplyButton.setText("Initialize");
            row2.getChildren().addAll(searchDeqButton, reverseButton, clearButton, parent.paramApplyButton);
        } else {
            row2.getChildren().addAll(searchDeqButton, reverseButton, clearButton);
        }

        opsBox.getChildren().addAll(row1, row2);
        vbox.getChildren().add(opsBox);
    }

    private Button makeButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        return b;
    }

    private void onCapacityChanged(int capacity) {
        history.clear();
        solver.setCapacity(capacity);
        visualizer.setCapacity(capacity);
        initProgressLog();
        updateVariables();
        renderCode();
    }

    private void onInitialize() {
        int cap = parent.paramBoardSizeSpinner != null ? parent.paramBoardSizeSpinner.getValue() : 5;
        onCapacityChanged(cap);
        appendProgress("📦 Initialized queue with capacity " + cap);
    }

    private Integer parseInput() {
        if (parent == null || parent.paramElementsField == null) return null;
        try { return Integer.parseInt(parent.paramElementsField.getText().trim()); }
        catch (Exception ex) { return null; }
    }

    private void doEnqueue() {
        Integer v = parseInput();
        if (v == null) { appendProgress("⚠ Enter a number to Enqueue."); return; }
        solver.queueEnqueue(v);
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued ENQUEUE(" + v + ")");
        autoPlayIfQueued();
    }

    private void doDequeue() {
        solver.queueDequeue();
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued DEQUEUE()");
        autoPlayIfQueued();
    }

    private void doSearch() {
        Integer v = parseInput();
        if (v == null) { appendProgress("⚠ Enter a number to Search."); return; }
        solver.queueSearch(v);
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued SEARCH(" + v + ")");
        autoPlayIfQueued();
    }

    private void doSearchAndDequeue() {
        Integer v = parseInput();
        if (v == null) { appendProgress("⚠ Enter a number to Search & Dequeue."); return; }
        solver.queueSearchAndDequeue(v);
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued SEARCH & DEQUEUE(" + v + ")");
        autoPlayIfQueued();
    }

    private void doReverse() {
        solver.queueReverse();
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued REVERSE()");
        autoPlayIfQueued();
    }

    private void doClear() {
        solver.queueClear();
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued CLEAR()");
        autoPlayIfQueued();
    }

    @Override
    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        QueueSolver.State st = history.pop();
        solver.restore(st);
        // redraw
        visualizer.setCapacity(st.capacity);
        visualizer.resetData(solver.getData(), solver.size());
        visualizer.resetAuxQueueData(solver.getAuxQueue(), solver.getAuxQSize());
        visualizer.resetAuxStackData(solver.getAuxStack(), solver.getAuxTop());
        visualizer.setAuxQueueVisible(solver.getAuxQSize() > 0);
        visualizer.setAuxStackVisible(solver.getAuxTop() >= 0);
        updateVariables();
        if (parent != null && parent.progressArea != null && !progressHistory.isEmpty()) {
            int remove = progressHistory.pop(); removeLastLogLines(remove);
        }
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Stepped back");
    }

    @Override
    public void onPlay() {
        if (isPlaying) { onPause(); return; }
        if (!solver.hasPending()) { if (parent != null) parent.stepDescription.setText("No queued steps."); return; }
        isPlaying = true;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
        if (parent != null && parent.playButton != null) parent.playButton.setText("⏸ Pause");
    }

    @Override
    public void onPause() {
        stopTimeline();
    }

    @Override
    public void onStepForward() {
        if (isPlaying) onPause();
        if (!solver.hasPending()) return;
        history.push(solver.snapshot());
        solver.step();
    }

    @Override
    public void onReset() {
        history.clear();
        int cap = parent.paramBoardSizeSpinner != null ? parent.paramBoardSizeSpinner.getValue() : 5;
        solver = new QueueSolver(cap, this::onStep);
        visualizer.setCapacity(cap);
        visualizer.resetData(new int[0], 0);
        visualizer.resetAuxQueueData(new int[0], 0);
        visualizer.resetAuxStackData(new int[0], -1);
        visualizer.setAuxQueueVisible(false);
        visualizer.setAuxStackVisible(false);
        initProgressLog();
        updateVariables();
        renderCode();
        if (parent != null) parent.stepDescription.setText("Ready.");
    }

    private void stopTimeline() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        isPlaying = false;
        if (parent != null && parent.playButton != null) parent.playButton.setText("▶ Play");
    }

    private void rebuildTimelineWithCurrentSpeed() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        double speed = parent != null && parent.speedSlider != null ? parent.speedSlider.getValue() : 1.0;
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (!solver.hasPending()) { stopTimeline(); return; }
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void updatePlaybackSpeed() {
        if (isPlaying) {
            rebuildTimelineWithCurrentSpeed();
            if (timeline != null) timeline.play();
        }
    }

    private void autoPlayIfQueued() { if (!isPlaying && solver.hasPending()) onPlay(); }

    private void initProgressLog() {
        if (parent != null && parent.progressArea != null) parent.progressArea.setText("");
        progressHistory.clear(); countLogsForStep = false; currentStepLogLines = 0;
    }

    private void appendProgress(String s) {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.appendText(s + "\n");
        if (countLogsForStep) currentStepLogLines++;
    }

    private void removeLastLogLines(int count) {
        if (parent == null || parent.progressArea == null || count <= 0) return;
        String text = parent.progressArea.getText(); if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n", -1);
        int total = lines.length; int newLen = Math.max(0, total - count);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newLen; i++) sb.append(lines[i]).append("\n");
        parent.progressArea.setText(sb.toString());
    }

    private void updateVariables() {
        if (parent == null || parent.variableList == null) return;
        parent.variableList.getItems().clear();
        parent.variableList.getItems().addAll(
                "capacity: " + solver.capacity(),
                "size: " + solver.size()
        );
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        com.algorithmvisualizer.code.AlgorithmCode code = 
            com.algorithmvisualizer.code.CodeRepository.getCode("Queue");
        if (code == null) {
            parent.codeArea.replaceText(0, parent.codeArea.getLength(), 
                "// Code not available");
            return;
        }
        String selectedLang = parent.languageSelector != null ? 
            parent.languageSelector.getValue() : "Java";
        String codeText = code.getCodeForLanguage(selectedLang);
        parent.codeArea.replaceText(0, parent.codeArea.getLength(), codeText);
    }

    private javafx.scene.Node bullet(String text) {
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(3, javafx.scene.paint.Color.web("#2b4c7e"));
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
        lbl.setStyle("-fx-text-fill: #2b4c7e; -fx-font-size: 12px;");
        lbl.setWrapText(true);
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(8.0, dot, lbl);
        row.setFillHeight(true);
        return row;
    }

    // Step handler
    private void onStep(QueueSolver.StepType type, int index, Integer value) {
        countLogsForStep = true;
        switch (type) {
            case INIT:
            case RESET:
                visualizer.resetData(solver.getData(), solver.size());
                visualizer.resetAuxQueueData(solver.getAuxQueue(), solver.getAuxQSize());
                visualizer.resetAuxStackData(solver.getAuxStack(), solver.getAuxTop());
                visualizer.setAuxQueueVisible(false);
                visualizer.setAuxStackVisible(false);
                break;
            case ENQ_START:
                appendProgress("➕ ENQUEUE(" + value + ")");
                break;
            case ENQ_DONE:
                visualizer.mainEnq(index, value);
                if (parent != null) parent.stepDescription.setText("ENQUEUE " + value);
                break;
            case OVERFLOW:
                appendProgress("⚠ Overflow");
                visualizer.flashMainOutlineRed();
                break;
            case DEQ_START:
                visualizer.mainDeq();
                appendProgress("➖ DEQUEUE()");
                if (parent != null) parent.stepDescription.setText("DEQUEUE");
                break;
            case DEQ_REMOVE:
            case DEQ_DONE:
                break;
            case UNDERFLOW:
                appendProgress("⚠ Underflow");
                visualizer.flashMainOutlineRed();
                break;
            case SEARCH_START:
                appendProgress("🔍 SEARCH(" + value + ")");
                if (parent != null) parent.stepDescription.setText("SEARCH " + value);
                visualizer.clearMainSearchHighlight();
                break;
            case SEARCH_COMPARE:
                appendProgress("• compare at index " + index);
                visualizer.mainSearchCompare(index);
                break;
            case SEARCH_FOUND:
                appendProgress("✅ Found at index " + index);
                visualizer.mainSearchFound(index);
                break;
            case SEARCH_NOT_FOUND:
                appendProgress("❌ Not found");
                visualizer.clearMainSearchHighlight();
                visualizer.flashMainOutlineRed();
                break;
            case SDQ_AUX_ENQ:
                visualizer.setAuxQueueVisible(true);
                visualizer.auxQEnq(index, value);
                appendProgress("⟹ aux enqueue " + value);
                break;
            case SDQ_AUX_DEQ:
                visualizer.auxQDeq(index);
                break;
            case SDQ_MOVE_BACK_START:
                appendProgress("↩ Restore from Aux Queue → Queue");
                break;
            case SDQ_MOVE_BACK_STEP:
                visualizer.mainEnq(index, value);
                break;
            case SDQ_DONE:
                appendProgress("✅ Search & Dequeue done");
                visualizer.setAuxQueueVisible(false);
                break;
            case REV_START:
                visualizer.setAuxStackVisible(true);
                appendProgress("🔁 REVERSE: drain to Aux Stack");
                if (parent != null) parent.stepDescription.setText("REVERSE - Phase 1");
                break;
            case AUXS_PUSH:
                visualizer.auxSPush(index, value);
                break;
            case REV_PHASE2_START:
                appendProgress("🔁 REVERSE: enqueue back from Aux Stack");
                if (parent != null) parent.stepDescription.setText("REVERSE - Phase 2");
                break;
            case AUXS_POP:
                visualizer.auxSPop(index);
                break;
            case REV_DONE:
                appendProgress("✅ Reverse done");
                visualizer.setAuxStackVisible(false);
                break;
            case CLEAR:
                visualizer.resetData(new int[0], 0);
                visualizer.resetAuxQueueData(new int[0], 0);
                visualizer.resetAuxStackData(new int[0], -1);
                visualizer.setAuxQueueVisible(false);
                visualizer.setAuxStackVisible(false);
                appendProgress("🧹 CLEAR");
                break;
        }
        updateVariables();
        if (countLogsForStep) { progressHistory.push(currentStepLogLines); countLogsForStep = false; currentStepLogLines = 0; }
    }
}
