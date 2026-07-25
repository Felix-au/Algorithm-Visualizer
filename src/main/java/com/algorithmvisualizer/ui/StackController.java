package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.StackSolver;
import com.algorithmvisualizer.visualization.StackVisualizer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Controller for Stack (Concept) visualization.
 */
public class StackController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;

    // Visual
    private StackVisualizer visualizer;

    // Model
    private StackSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;

    // History for step-back
    private final Deque<StackSolver.State> history = new ArrayDeque<>();

    // Controls
    private Button pushButton;
    private Button popButton;
    private Button peekButton;
    private Button searchButton;
    private Button searchPopButton;
    private Button reverseButton;
    private Button clearButton;

    // Progress history for step-back log removal
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    @FXML
    private void initialize() {
        visualizer = new StackVisualizer();
        // Default solver
        solver = new StackSolver(5, null);
        solver.setStepListener(this::onStep);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place visuals
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(visualizer.getNode());
        }

        // Header and legend
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Stack (Concept)");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(hdr, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15.0);
            javafx.scene.shape.Rectangle normal = new javafx.scene.shape.Rectangle(12,12);
            normal.setFill(javafx.scene.paint.Color.web("#4a90e2"));
            normal.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lNormal = new javafx.scene.layout.HBox(5.0, normal, new Label("Element"));
            javafx.scene.shape.Rectangle top = new javafx.scene.shape.Rectangle(12,12);
            top.setFill(javafx.scene.paint.Color.web("#2ecc71"));
            top.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lTop = new javafx.scene.layout.HBox(5.0, top, new Label("Top"));
            javafx.scene.shape.Rectangle search = new javafx.scene.shape.Rectangle(12,12);
            search.setFill(javafx.scene.paint.Color.web("#f39c12"));
            search.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lSearch = new javafx.scene.layout.HBox(5.0, search, new Label("Searching"));
            javafx.scene.shape.Rectangle found = new javafx.scene.shape.Rectangle(12,12);
            found.setFill(javafx.scene.paint.Color.web("#27ae60"));
            found.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lFound = new javafx.scene.layout.HBox(5.0, found, new Label("Found"));
            javafx.scene.shape.Rectangle aux = new javafx.scene.shape.Rectangle(12,12);
            aux.setFill(javafx.scene.paint.Color.web("#95a5a6"));
            aux.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lAux = new javafx.scene.layout.HBox(5.0, aux, new Label("Aux Stack"));
            row.getChildren().addAll(lNormal, lTop, lSearch, lFound, lAux);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters wiring
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Capacity:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 5));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onCapacityChanged(n));
        }
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Value:");
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("Enter a number, e.g. 42");
            parent.paramElementsField.setOnAction(e -> { doPush(); parent.paramElementsField.clear(); });
        }
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setVisible(false);
            parent.paramRandomizeButton.setManaged(false);
        }
        if (parent.paramApplyButton != null) {
            parent.paramApplyButton.setText("Initialize");
            parent.paramApplyButton.setOnAction(e -> onInitialize());
        }

        // Add operations rows under the parameters
        addOperationsRow();

        // Repurpose solutions pane for Stack concepts
        if (parent.solutionsHeaderBox != null) { parent.solutionsHeaderBox.setVisible(true); parent.solutionsHeaderBox.setManaged(true); }
        if (parent.solutionsHeaderLabel != null) parent.solutionsHeaderLabel.setText("Stack Concepts");
        if (parent.solutionsSubHeaderBox != null) { parent.solutionsSubHeaderBox.setVisible(false); parent.solutionsSubHeaderBox.setManaged(false); }
        if (parent.solutionsContainer != null) { parent.solutionsContainer.setVisible(true); parent.solutionsContainer.setManaged(true); }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            javafx.scene.layout.VBox bullets = new javafx.scene.layout.VBox(6.0);
            bullets.setStyle("-fx-padding: 4;");
            bullets.getChildren().addAll(
                bullet("Stack: linear data structure following LIFO (Last-In, First-Out)"),
                bullet("LIFO: the last pushed element is the first one popped"),
                bullet("Push(x): insert x at the top (top = top + 1)"),
                bullet("Pop(): remove and return the top element (top = top - 1)"),
                bullet("Peek(): read the top element without removing it"),
                bullet("Search(x): linearly scan from top to bottom; returns position from top (1-based) or -1")
            );
            parent.solutionsContent.getChildren().add(bullets);
        }

        // Playback controls like other algorithms (pause button hidden for this algo)
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.stepBackButton != null) parent.stepBackButton.setOnAction(e -> onStepBack());
        if (parent.resetButton != null) parent.resetButton.setOnAction(e -> onReset());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        // Panels
        initProgressLog();
        updateVariables();
        parent.setCurrentAlgorithmName("Stack");
        renderCode();
        
        // Add language selector listener
        if (parent.languageSelector != null) {
            parent.languageSelector.valueProperty().addListener((obs, oldVal, newVal) -> renderCode());
        }
        
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");

        // Apply initial capacity
        onInitialize();
    }

    private void addOperationsRow() {
        if (parent == null || parent.paramElementsField == null) return;
        javafx.scene.Parent container = parent.paramElementsField.getParent();
        if (!(container instanceof javafx.scene.layout.VBox)) return;
        javafx.scene.layout.VBox vbox = (javafx.scene.layout.VBox) container;

        // Two-row layout
        javafx.scene.layout.VBox opsBox = new javafx.scene.layout.VBox(6.0);
        javafx.scene.layout.HBox row1 = new javafx.scene.layout.HBox(8.0);
        javafx.scene.layout.HBox row2 = new javafx.scene.layout.HBox(8.0);
        opsBox.setStyle("-fx-padding: 4 0 0 0;");

        pushButton = makeButton("Push", "#4a90e2");
        popButton = makeButton("Pop", "#e74c3c");
        peekButton = makeButton("Peek", "#f1c40f");
        searchButton = makeButton("Search", "#7b68ee");
        searchPopButton = makeButton("Search & Pop", "#2ecc71");
        reverseButton = makeButton("Reverse", "#16a085");
        clearButton = makeButton("Clear", "#95a5a6");

        pushButton.setOnAction(e -> doPush());
        popButton.setOnAction(e -> doPop());
        peekButton.setOnAction(e -> doPeek());
        searchButton.setOnAction(e -> doSearch());
        searchPopButton.setOnAction(e -> doSearchAndPop());
        reverseButton.setOnAction(e -> doReverse());
        clearButton.setOnAction(e -> doClear());

        // First row: Push, Pop, Peek, Search
        row1.getChildren().addAll(pushButton, popButton, peekButton, searchButton);

        // Second row: Search & Pop, Reverse, Clear, Initialize (move Apply button here)
        if (parent.paramApplyButton != null) {
            // detach from original container row (likely an HBox with Randomize)
            javafx.scene.Parent applyParent = parent.paramApplyButton.getParent();
            if (applyParent instanceof javafx.scene.layout.HBox) {
                // remove the whole original row from vbox after moving apply button
                ((javafx.scene.layout.HBox) applyParent).getChildren().remove(parent.paramApplyButton);
                vbox.getChildren().remove(applyParent);
            }
            parent.paramApplyButton.setText("Initialize");
            row2.getChildren().addAll(searchPopButton, reverseButton, clearButton, parent.paramApplyButton);
        } else {
            row2.getChildren().addAll(searchPopButton, reverseButton, clearButton);
        }

        opsBox.getChildren().addAll(row1, row2);

        // Insert before the last child (we removed the original apply row)
        vbox.getChildren().add(opsBox);
    }

    private Button makeButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
        return b;
    }

    private void onCapacityChanged(int capacity) {
        history.clear();
        // Ensure visualizer is prepared before the solver emits INIT events
        visualizer.setCapacity(capacity);
        solver.setCapacity(capacity);
        visualizer.resetAux();
        visualizer.resetData(new int[0], -1);
        initProgressLog();
        updateVariables();
        renderCode();
    }

    private void onInitialize() {
        int cap = parent.paramBoardSizeSpinner != null ? parent.paramBoardSizeSpinner.getValue() : 5;
        onCapacityChanged(cap);
        appendProgress("📦 Initialized stack with capacity " + cap);
    }

    // Operations
    private Integer parseInput() {
        if (parent == null || parent.paramElementsField == null) return null;
        try {
            return Integer.parseInt(parent.paramElementsField.getText().trim());
        } catch (Exception ex) { return null; }
    }

    private void doPush() {
        Integer v = parseInput();
        if (v == null) { appendProgress("⚠ Enter a number to Push."); return; }
        solver.queuePush(v);
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued PUSH(" + v + ")");
        autoPlayIfQueued();
    }

    private void doPop() {
        solver.queuePop();
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued POP()");
        autoPlayIfQueued();
    }

    private void doPeek() {
        solver.queuePeek();
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued PEEK()");
        autoPlayIfQueued();
    }

    private void doSearch() {
        Integer v = parseInput();
        if (v == null) { appendProgress("⚠ Enter a number to Search."); return; }
        solver.queueSearch(v);
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued SEARCH(" + v + ")");
        autoPlayIfQueued();
    }

    private void doSearchAndPop() {
        Integer v = parseInput();
        if (v == null) { appendProgress("⚠ Enter a number to Search & Pop."); return; }
        solver.queueSearchAndPop(v);
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Queued SEARCH & POP(" + v + ")");
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

    // Step back
    @Override
    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        StackSolver.State st = history.pop();
        solver.restore(st);
        // redraw visual from state
        visualizer.setCapacity(st.capacity);
        visualizer.resetData(st.data, st.top);
        visualizer.resetAuxData(st.aux, st.auxTopWorking);
        visualizer.resetQueueData(solver.getQueueArray(), solver.getQueueSize());
        visualizer.setQueueVisible(solver.getQueueSize() > 0);
        updateVariables();
        // remove last step logs
        if (parent != null && parent.progressArea != null && !progressHistory.isEmpty()) {
            int toRemove = progressHistory.pop();
            removeLastLogLines(toRemove);
        }
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Stepped back");
    }

    @Override
    public void onReset() {
        history.clear();
        int cap = parent.paramBoardSizeSpinner != null ? parent.paramBoardSizeSpinner.getValue() : 10;
        solver = new StackSolver(cap, null);
        solver.setStepListener(this::onStep);
        visualizer.setCapacity(cap);
        visualizer.resetData(new int[0], -1);
        visualizer.resetAux();
        visualizer.resetQueue();
        visualizer.setQueueVisible(false);
        initProgressLog();
        updateVariables();
        renderCode();
        if (parent != null) parent.stepDescription.setText("Ready.");
    }

    // Step events
    private void onStep(StackSolver.StepType type, int index, Integer value) {
        // begin counting lines for this emitted step
        countLogsForStep = true;
        currentStepLogLines = 0;
        switch (type) {
            case INIT:
            case RESET:
                visualizer.resetData(solver.getData(), solver.topIndex());
                visualizer.resetAuxData(solver.getAuxArray(), solver.getAuxTopWorking());
                visualizer.resetQueueData(solver.getQueueArray(), solver.getQueueSize());
                visualizer.setQueueVisible(solver.getQueueSize() > 0);
                break;
            case PUSH_START:
                visualizer.animatePush(index, value);
                appendProgress("➕ PUSH(" + value + ") at index " + index);
                if (parent != null) parent.stepDescription.setText("PUSH " + value);
                break;
            case PUSH_SET:
            case PUSH_DONE:
                break;
            case POP_START:
                appendProgress("➖ POP from index " + index);
                if (parent != null) parent.stepDescription.setText("POP()");
                break;
            case POP_REMOVE:
                visualizer.animatePop(index);
                break;
            case POP_DONE:
                break;
            case PEEK:
                visualizer.animatePeek(index);
                appendProgress("👁 PEEK → value " + value);
                if (parent != null) parent.stepDescription.setText("PEEK → " + value);
                break;
            case SEARCH_START:
                appendProgress("🔍 SEARCH(" + value + ")");
                if (parent != null) parent.stepDescription.setText("SEARCH " + value);
                break;
            case SEARCH_COMPARE:
                visualizer.animateSearchCompare(index);
                break;
            case SEARCH_FOUND:
                visualizer.animateFound(index);
                appendProgress("✅ FOUND at index " + index + " (position from top = " + (solver.topIndex() - index + 1) + ")");
                break;
            case SEARCH_NOT_FOUND:
                appendProgress("❌ NOT FOUND");
                break;
            case OVERFLOW:
                visualizer.flashOverflowUnderflow();
                appendProgress("⛔ OVERFLOW on PUSH(" + value + ")");
                break;
            case UNDERFLOW:
                visualizer.flashOverflowUnderflow();
                appendProgress("⛔ UNDERFLOW");
                break;
            case SP_AUX_PUSH:
                visualizer.auxPush(index, value);
                appendProgress("↪ Move top → AUX: " + value);
                break;
            case SP_AUX_POP:
                visualizer.auxPop(index);
                appendProgress("↥ Pop from AUX: " + value);
                break;
            case SP_MOVE_BACK_START:
                appendProgress("↩ Restore from AUX → Stack");
                break;
            case SP_MOVE_BACK_STEP:
                visualizer.animatePush(index, value);
                break;
            case SP_DONE:
                appendProgress("✅ Search & Pop done");
                break;
            case Q_ENQ:
                visualizer.setQueueVisible(true);
                visualizer.queueEnq(index, value);
                appendProgress("➕ Enqueue to AUX Queue: " + value);
                break;
            case Q_DEQ:
                visualizer.queueDeq(index);
                appendProgress("➖ Dequeue from AUX Queue: " + value);
                break;
            case REVERSE_START:
                visualizer.setQueueVisible(true);
                appendProgress("🔁 REVERSE: Move all stack elements into queue");
                if (parent != null) parent.stepDescription.setText("REVERSE - Phase 1");
                break;
            case REVERSE_PHASE2_START:
                appendProgress("🔁 REVERSE: Move all from queue back to stack");
                if (parent != null) parent.stepDescription.setText("REVERSE - Phase 2");
                break;
            case REVERSE_DONE:
                appendProgress("✅ Reverse done");
                visualizer.resetQueue();
                visualizer.setQueueVisible(false);
                if (parent != null) parent.stepDescription.setText("Reverse complete");
                break;
            case CLEAR:
                visualizer.resetData(new int[0], -1);
                visualizer.auxClear();
                visualizer.resetQueue();
                visualizer.setQueueVisible(false);
                appendProgress("🧹 CLEAR");
                break;
        }
        updateVariables();
        // finalize log group for this step
        if (countLogsForStep) {
            progressHistory.push(currentStepLogLines);
            countLogsForStep = false;
            currentStepLogLines = 0;
        }
    }

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        appendProgress("Stack ready.");
    }

    private void appendProgress(String line) {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.appendText(line + "\n");
        if (countLogsForStep) currentStepLogLines++;
    }

    private void updateVariables() {
        if (parent == null || parent.variableList == null) return;
        parent.variableList.getItems().clear();
        parent.variableList.getItems().addAll(
                "capacity: " + solver.capacity(),
                "size: " + solver.size(),
                "top: " + solver.topIndex(),
                "isEmpty: " + (solver.size() == 0),
                "isFull: " + (solver.size() == solver.capacity())
        );
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

    // --- Playback controls ---
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
        if (!solver.hasPending()) return;
        history.push(solver.snapshot());
        solver.step();
    }

    private void stopTimeline() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        isPlaying = false;
        if (parent != null && parent.playButton != null) parent.playButton.setText("▶ Play");
    }

    private void updatePlaybackSpeed() {
        if (!isPlaying) return;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
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

    private void autoPlayIfQueued() {
        if (!isPlaying && solver.hasPending()) onPlay();
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

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        com.algorithmvisualizer.code.AlgorithmCode code = 
            com.algorithmvisualizer.code.CodeRepository.getCode("Stack");
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
}
