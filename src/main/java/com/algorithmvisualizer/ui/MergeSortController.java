package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.MergeSortSolver;
import com.algorithmvisualizer.visualization.ArrayRenderer;
import com.algorithmvisualizer.visualization.MergeSortTreeRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

public class MergeSortController implements AlgorithmViewController.AlgorithmSpecificController {

    @FXML private Spinner<Integer> arraySizeSpinner;
    @FXML private TextField arrayElementsField;
    @FXML private FlowPane arrayElementsBox;
    @FXML private Button randomizeButton;
    @FXML private Button applyArrayButton;
    @FXML private Label statusLabel;

    private AlgorithmViewController parent;

    // Visualizations
    private com.algorithmvisualizer.visualization.GraphRenderer treeRenderer;
    private ArrayRenderer arrayView;
    private final java.util.List<com.algorithmvisualizer.visualization.GraphRenderer> treeRendererMirrors = new java.util.ArrayList<>();
    
    // Blinking animation
    private javafx.animation.Timeline blinkTimeline;
    private boolean blinkState = false;

    // Solver
    private MergeSortSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;
    private final Deque<MergeSortSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    private int[] currentArray = new int[]{5, 3, 8, 4, 2, 7, 1, 6};

    @FXML
    private void initialize() {
        if (arraySizeSpinner != null) {
            arraySizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, currentArray.length));
        }
        if (arrayElementsField != null) {
            arrayElementsField.setText(join(currentArray));
        }
        if (arrayElementsBox != null) {
            rebuildLocalElementBoxes();
        }
        if (randomizeButton != null) randomizeButton.setOnAction(e -> onRandomize());
        if (applyArrayButton != null) applyArrayButton.setOnAction(e -> onApplyArray());

        // Create visualizations
        treeRenderer = new com.algorithmvisualizer.visualization.GraphRenderer();
        treeRenderer.setLayoutMode(com.algorithmvisualizer.visualization.GraphRenderer.LayoutMode.TREE);
        treeRenderer.setNodeRadius(22.0);  // Larger nodes for merge sort
        treeRenderer.setTreeLeafSpacingEnabled(true);
        treeRenderer.setTreeMinLeafSpacing(90.0);
        arrayView = new ArrayRenderer();
        
        // Setup blinking animation for building nodes
        setupBlinkAnimation();

        // Create solver BEFORE renderVisuals
        solver = new MergeSortSolver(currentArray);
        solver.setStepListener(this::onStepEvent);
        
        // Now render visuals
        renderVisuals();
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place tree visualization in chessboard container with ScrollPane
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
            scrollPane.setContent(treeRenderer.getNode());
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: white;");
            scrollPane.setPannable(true);
            parent.chessboardContainer.getChildren().add(scrollPane);
        }

        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        }

        // Parameters panel integration
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, currentArray.length));
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
            Label chartHeader = new Label("Merge Sort Tree Visualization");
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
            r1.setFill(javafx.scene.paint.Color.LIGHTGRAY);
            r1.setStroke(javafx.scene.paint.Color.BLACK);
            l1.getChildren().addAll(r1, new Label("Not Processed"));
            
            HBox l2 = new HBox(5.0);
            javafx.scene.shape.Rectangle r2 = new javafx.scene.shape.Rectangle(12, 12);
            r2.setFill(javafx.scene.paint.Color.GOLD);
            r2.setStroke(javafx.scene.paint.Color.BLACK);
            l2.getChildren().addAll(r2, new Label("Building"));
            
            HBox l3 = new HBox(5.0);
            javafx.scene.shape.Rectangle r3 = new javafx.scene.shape.Rectangle(12, 12);
            r3.setFill(javafx.scene.paint.Color.FORESTGREEN);
            r3.setStroke(javafx.scene.paint.Color.BLACK);
            l3.getChildren().addAll(r3, new Label("Merged"));
            
            legendRow.getChildren().addAll(l1, l2, l3);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, legendRow);
        }
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Array size:");
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        parent.setCurrentAlgorithmName("Merge Sort");
        renderCode();
        initProgressLog();
        updateVariablesPanel();

        // Update solutions panel
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Array View");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            // Add pop-out button
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Button popBtn = new Button("Pop-out Tree");
            popBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 10; -fx-cursor: hand;");
            popBtn.setOnAction(e -> openTreePopout());
            
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, spacer, popBtn);
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
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
        
        // Update code with current parameters
        com.algorithmvisualizer.code.AlgorithmCode code = 
            com.algorithmvisualizer.code.CodeRepository.getCode("Merge Sort");
        if (code instanceof com.algorithmvisualizer.code.implementations.MergeSortCode) {
            com.algorithmvisualizer.code.implementations.MergeSortCode msCode = 
                (com.algorithmvisualizer.code.implementations.MergeSortCode) code;
            msCode.updateParameters(currentArray);
        }
        
        renderVisuals();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (statusLabel != null) statusLabel.setText("Array applied. Ready.");
    }

    private void renderVisuals() {
        updateTreeFromSolver();
        arrayView.setData(currentArray);
        rebuildParentElementBoxes();
    }
    
    /**
     * Setup blinking animation for building nodes
     */
    private void setupBlinkAnimation() {
        blinkTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(400), e -> {
                blinkState = !blinkState;
                updateTreeViews(); // Refresh to apply blink state
            })
        );
        blinkTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        blinkTimeline.play();
    }
    
    /**
     * Customize text labels to use black color
     */
    private void customizeTextLabels(com.algorithmvisualizer.visualization.GraphRenderer renderer) {
        // Access the container and find all Text nodes
        javafx.scene.layout.Pane container = renderer.getContainer();
        for (javafx.scene.Node node : container.getChildren()) {
            if (node instanceof javafx.scene.text.Text) {
                javafx.scene.text.Text text = (javafx.scene.text.Text) node;
                text.setFill(javafx.scene.paint.Color.BLACK);
                text.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            }
        }
    }
    
    /**
     * Convert TreeNode structure to GraphRenderer format and update display
     */
    private void updateTreeFromSolver() {
        MergeSortSolver.TreeNode root = solver.getRoot();
        if (root == null) {
            treeRenderer.setGraph(0, new java.util.ArrayList<>());
            return;
        }
        
        // Build node list and adjacency list
        java.util.List<MergeSortSolver.TreeNode> nodes = new java.util.ArrayList<>();
        java.util.List<java.util.List<Integer>> adj = new java.util.ArrayList<>();
        java.util.List<String> labels = new java.util.ArrayList<>();
        
        // BFS to collect all nodes
        java.util.Queue<MergeSortSolver.TreeNode> queue = new java.util.LinkedList<>();
        java.util.Map<MergeSortSolver.TreeNode, Integer> nodeToId = new java.util.HashMap<>();
        
        queue.offer(root);
        nodeToId.put(root, 0);
        nodes.add(root);
        
        while (!queue.isEmpty()) {
            MergeSortSolver.TreeNode node = queue.poll();
            int nodeId = nodeToId.get(node);
            
            // Only process divided nodes
            if (node.isDivided) {
                if (node.leftChild != null && !nodeToId.containsKey(node.leftChild)) {
                    int childId = nodes.size();
                    nodeToId.put(node.leftChild, childId);
                    nodes.add(node.leftChild);
                    queue.offer(node.leftChild);
                }
                if (node.rightChild != null && !nodeToId.containsKey(node.rightChild)) {
                    int childId = nodes.size();
                    nodeToId.put(node.rightChild, childId);
                    nodes.add(node.rightChild);
                    queue.offer(node.rightChild);
                }
            }
        }
        
        // Build adjacency list
        for (int i = 0; i < nodes.size(); i++) {
            adj.add(new java.util.ArrayList<>());
        }
        
        for (MergeSortSolver.TreeNode node : nodes) {
            if (!node.isDivided) continue;
            
            int parentId = nodeToId.get(node);
            if (node.leftChild != null && nodeToId.containsKey(node.leftChild)) {
                int leftId = nodeToId.get(node.leftChild);
                adj.get(parentId).add(leftId);
                adj.get(leftId).add(parentId);
            }
            if (node.rightChild != null && nodeToId.containsKey(node.rightChild)) {
                int rightId = nodeToId.get(node.rightChild);
                adj.get(parentId).add(rightId);
                adj.get(rightId).add(parentId);
            }
        }
        
        // Build labels
        for (MergeSortSolver.TreeNode node : nodes) {
            int[] displayData = node.buildingData != null ? node.buildingData : node.data;
            labels.add(arrayToString(displayData));
        }
        
        // Update graph
        treeRenderer.setNodeLabels(labels);
        treeRenderer.setGraph(nodes.size(), adj);
        
        // Customize text color to black
        customizeTextLabels(treeRenderer);
        
        // Apply colors based on node state with blinking
        for (int i = 0; i < nodes.size(); i++) {
            MergeSortSolver.TreeNode node = nodes.get(i);
            javafx.scene.paint.Color color = getNodeColor(node);
            
            // Apply blink effect for building nodes
            if (node.isBuilding && blinkState) {
                color = color.brighter();
            }
            
            treeRenderer.setNodeColor(i, color);
        }
    }
    
    /**
     * Get color for node based on state
     */
    private javafx.scene.paint.Color getNodeColor(MergeSortSolver.TreeNode node) {
        if (node.isBuilding) {
            return javafx.scene.paint.Color.GOLD;  // Building (merge in progress)
        }
        if (node.isMerged) {
            return javafx.scene.paint.Color.FORESTGREEN;  // Merged and complete (darker green)
        }
        if (node.isActive) {
            return javafx.scene.paint.Color.LIGHTYELLOW;  // Currently processing
        }
        return javafx.scene.paint.Color.LIGHTGRAY;  // Not yet processed
    }
    
    /**
     * Convert array to string representation
     */
    private static String arrayToString(int[] arr) {
        if (arr == null || arr.length == 0) return "[]";
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length && i < 8; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        if (arr.length > 8) {
            sb.append("...");
        }
        sb.append("]");
        return sb.toString();
    }

    private void moveArrayViewToSolutions() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        parent.solutionsContent.getChildren().add(arrayView.getNode());
    }

    private void onStepEvent(MergeSortSolver.StepType type, MergeSortSolver.State state) {
        switch (type) {
            case INIT:
                countLogsForStep = true;
                currentStepLogLines = 0;
                appendProgress("🔄 Merge Sort: n = " + state.array.length);
                if (parent != null) parent.stepDescription.setText("Initializing merge sort...");
                break;
                
            case BUILD_TREE_START:
                appendProgress("🌳 Building merge sort tree structure...");
                if (parent != null) parent.stepDescription.setText("Building tree structure...");
                break;
                
            case DIVIDE_NODE:
                // Update tree visualization as nodes are divided
                updateTreeViews();
                if (parent != null) parent.stepDescription.setText("Dividing array into subarrays...");
                break;
                
            case BUILD_TREE_COMPLETE:
                int depth = solver.getTotalLevels();
                appendProgress("  ✓ Tree built with " + (depth + 1) + " levels (depth = " + depth + ")");
                appendProgress("");
                appendProgress("📊 Starting bottom-up merge phase...");
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Tree structure complete");
                break;
                
            case MERGE_LEVEL_START:
                appendProgress("━━━ Level " + state.currentLevel + ": Merging subarrays of size " + state.currentSize + " ━━━");
                if (parent != null) parent.stepDescription.setText("Starting merge level " + state.currentLevel);
                break;
                
            case MERGE_START:
                countLogsForStep = true;
                currentStepLogLines = 0;
                String leftArr = arrayRangeToString(state.array, state.left, state.mid);
                String rightArr = arrayRangeToString(state.array, state.mid + 1, state.right);
                appendProgress("🔀 Merging " + leftArr + " and " + rightArr + " (indices " + state.left + "-" + state.right + ")");
                if (parent != null) parent.stepDescription.setText("Starting merge of range [" + state.left + ".." + state.right + "]");
                updateTreeViews();
                break;
                
            case MERGE_COMPARE:
                int leftVal = state.array[state.leftPointer];
                int rightVal = state.array[state.rightPointer];
                appendProgress("  🔍 Compare: " + leftVal + " vs " + rightVal);
                if (parent != null) parent.stepDescription.setText("Comparing " + leftVal + " and " + rightVal);
                break;
                
            case MERGE_TAKE_LEFT:
                if (state.leftPointer > 0 && state.leftPointer <= state.array.length) {
                    appendProgress("  ← Take " + state.array[state.leftPointer - 1] + " from left");
                }
                if (parent != null) parent.stepDescription.setText("Taking element from left subarray");
                break;
                
            case MERGE_TAKE_RIGHT:
                if (state.rightPointer > 0 && state.rightPointer <= state.array.length) {
                    appendProgress("  ← Take " + state.array[state.rightPointer - 1] + " from right");
                }
                if (parent != null) parent.stepDescription.setText("Taking element from right subarray");
                break;
                
            case MERGE_ADD_ELEMENT:
                // Update tree visualization to show incremental building
                updateTreeViews();
                if (parent != null) parent.stepDescription.setText("Adding element to merged result");
                break;
                
            case MERGE_COPY_REMAINING_LEFT:
                appendProgress("  ← Copy remaining from left");
                if (parent != null) parent.stepDescription.setText("Copying remaining left elements");
                break;
                
            case MERGE_COPY_REMAINING_RIGHT:
                appendProgress("  ← Copy remaining from right");
                if (parent != null) parent.stepDescription.setText("Copying remaining right elements");
                break;
                
            case MERGE_COMPLETE:
                String merged = arrayRangeToString(state.array, state.left, state.right);
                appendProgress("  ✓ Merged: " + merged);
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Merge complete for range [" + state.left + ".." + state.right + "]");
                updateTreeViews();
                arrayView.updateData(state.array);
                if (countLogsForStep) {
                    progressHistory.push(currentStepLogLines);
                    countLogsForStep = false;
                    currentStepLogLines = 0;
                }
                break;
                
            case MERGE_LEVEL_COMPLETE:
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("Level " + state.currentLevel + " complete");
                break;
                
            case DONE:
                appendProgress("🎉 SORTING COMPLETE!");
                appendProgress("✓ Array is fully sorted in ascending order");
                appendProgress("✓ Total levels processed: " + solver.getTotalLevels());
                appendProgress("✓ Time complexity: O(n log n)");
                appendProgress("");
                if (parent != null) parent.stepDescription.setText("🎉 Sorting Complete!");
                updateTreeViews();
                arrayView.updateData(state.array);
                stopTimeline();
                break;
        }
        updateVariablesPanel();
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
        
        MergeSortSolver.State s = history.pop();
        solver.restore(s);
        
        updateTreeViews();
        arrayView.updateData(solver.getArray());
        
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
        updateTreeViews();
        arrayView.updateData(solver.getArray());
        initProgressLog();
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Ready.");
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
            "mergeSize: " + solver.getCurrentSize(),
            "level: " + solver.getCurrentLevel(),
            "left: " + solver.getLeft(),
            "mid: " + solver.getMid(),
            "right: " + solver.getRight(),
            "leftPtr: " + solver.getLeftPointer(),
            "rightPtr: " + solver.getRightPointer(),
            "mergePtr: " + solver.getMergePointer(),
            "state: " + (solver.isDone() ? "DONE" : "RUNNING")
        );
    }

    // Utility methods
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

    private static String arrayRangeToString(int[] arr, int from, int to) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = from; i <= to && i < arr.length; i++) {
            if (i > from) sb.append(", ");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
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
    
    /**
     * Update all tree views (main + pop-outs)
     */
    private void updateTreeViews() {
        updateTreeFromSolver();
        for (com.algorithmvisualizer.visualization.GraphRenderer mirror : treeRendererMirrors) {
            applyTreeState(mirror);
        }
    }
    
    /**
     * Apply current tree state to a GraphRenderer
     */
    private void applyTreeState(com.algorithmvisualizer.visualization.GraphRenderer renderer) {
        MergeSortSolver.TreeNode root = solver.getRoot();
        if (root == null) {
            renderer.setGraph(0, new java.util.ArrayList<>());
            return;
        }
        
        // Build node list and adjacency list
        java.util.List<MergeSortSolver.TreeNode> nodes = new java.util.ArrayList<>();
        java.util.List<java.util.List<Integer>> adj = new java.util.ArrayList<>();
        java.util.List<String> labels = new java.util.ArrayList<>();
        
        // BFS to collect all nodes
        java.util.Queue<MergeSortSolver.TreeNode> queue = new java.util.LinkedList<>();
        java.util.Map<MergeSortSolver.TreeNode, Integer> nodeToId = new java.util.HashMap<>();
        
        queue.offer(root);
        nodeToId.put(root, 0);
        nodes.add(root);
        
        while (!queue.isEmpty()) {
            MergeSortSolver.TreeNode node = queue.poll();
            
            // Only process divided nodes
            if (node.isDivided) {
                if (node.leftChild != null && !nodeToId.containsKey(node.leftChild)) {
                    int childId = nodes.size();
                    nodeToId.put(node.leftChild, childId);
                    nodes.add(node.leftChild);
                    queue.offer(node.leftChild);
                }
                if (node.rightChild != null && !nodeToId.containsKey(node.rightChild)) {
                    int childId = nodes.size();
                    nodeToId.put(node.rightChild, childId);
                    nodes.add(node.rightChild);
                    queue.offer(node.rightChild);
                }
            }
        }
        
        // Build adjacency list
        for (int i = 0; i < nodes.size(); i++) {
            adj.add(new java.util.ArrayList<>());
        }
        
        for (MergeSortSolver.TreeNode node : nodes) {
            if (!node.isDivided) continue;
            
            int parentId = nodeToId.get(node);
            if (node.leftChild != null && nodeToId.containsKey(node.leftChild)) {
                int leftId = nodeToId.get(node.leftChild);
                adj.get(parentId).add(leftId);
                adj.get(leftId).add(parentId);
            }
            if (node.rightChild != null && nodeToId.containsKey(node.rightChild)) {
                int rightId = nodeToId.get(node.rightChild);
                adj.get(parentId).add(rightId);
                adj.get(rightId).add(parentId);
            }
        }
        
        // Build labels
        for (MergeSortSolver.TreeNode node : nodes) {
            int[] displayData = node.buildingData != null ? node.buildingData : node.data;
            labels.add(arrayToString(displayData));
        }
        
        // Update graph
        renderer.setNodeLabels(labels);
        renderer.setGraph(nodes.size(), adj);
        
        // Customize text color to black
        customizeTextLabels(renderer);
        
        // Apply colors based on node state with blinking
        for (int i = 0; i < nodes.size(); i++) {
            MergeSortSolver.TreeNode node = nodes.get(i);
            javafx.scene.paint.Color color = getNodeColor(node);
            
            // Apply blink effect for building nodes
            if (node.isBuilding && blinkState) {
                color = color.brighter();
            }
            
            renderer.setNodeColor(i, color);
        }
    }
    
    /**
     * Open pop-out tree window
     */
    private void openTreePopout() {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Merge Sort Tree (Pop-out)");
        
        // Create new tree renderer
        com.algorithmvisualizer.visualization.GraphRenderer popoutRenderer = new com.algorithmvisualizer.visualization.GraphRenderer();
        popoutRenderer.setLayoutMode(com.algorithmvisualizer.visualization.GraphRenderer.LayoutMode.TREE);
        popoutRenderer.setNodeRadius(22.0);  // Same size as main view
        popoutRenderer.setTreeLeafSpacingEnabled(true);
        popoutRenderer.setTreeMinLeafSpacing(90.0);
        applyTreeState(popoutRenderer);
        
        // Track for live sync
        treeRendererMirrors.add(popoutRenderer);
        stage.setOnCloseRequest(e -> treeRendererMirrors.remove(popoutRenderer));
        
        // Wrap in Group for scaling
        javafx.scene.Group content = new javafx.scene.Group(popoutRenderer.getNode());
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(content);
        stack.setPadding(new javafx.geometry.Insets(24));
        
        // ScrollPane for panning
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(stack);
        scrollPane.setPrefViewportWidth(900);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        
        // Zoom controls
        HBox controls = new HBox(8.0);
        controls.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button zoomOut = new Button("-");
        Button zoomIn = new Button("+");
        ToggleButton autoFit = new ToggleButton();
        
        zoomOut.setTooltip(new Tooltip("Zoom Out (Ctrl+Scroll Down)"));
        zoomIn.setTooltip(new Tooltip("Zoom In (Ctrl+Scroll Up)"));
        zoomOut.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10;");
        zoomIn.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10;");
        
        final double[] scale = {1.0};
        Runnable applyScale = () -> {
            content.setScaleX(scale[0]);
            content.setScaleY(scale[0]);
        };
        
        // Auto-Fit toggle
        Runnable updateAutoFitAppearance = () -> {
            if (autoFit.isSelected()) {
                autoFit.setText("Auto-Fit: ON");
                autoFit.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-weight: bold;");
            } else {
                autoFit.setText("Auto-Fit: OFF");
                autoFit.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-weight: bold;");
            }
        };
        updateAutoFitAppearance.run();
        
        zoomIn.setOnAction(e -> {
            autoFit.setSelected(false);
            updateAutoFitAppearance.run();
            scale[0] = Math.min(5.0, scale[0] + 0.1);
            applyScale.run();
        });
        
        zoomOut.setOnAction(e -> {
            autoFit.setSelected(false);
            updateAutoFitAppearance.run();
            scale[0] = Math.max(0.2, scale[0] - 0.1);
            applyScale.run();
        });
        
        Runnable fitToView = () -> {
            javafx.geometry.Bounds b = content.getBoundsInLocal();
            double cw = Math.max(1.0, b.getWidth());
            double ch = Math.max(1.0, b.getHeight());
            double vw = Math.max(1.0, scrollPane.getViewportBounds().getWidth());
            double vh = Math.max(1.0, scrollPane.getViewportBounds().getHeight());
            double padX = stack.getPadding().getLeft() + stack.getPadding().getRight();
            double padY = stack.getPadding().getTop() + stack.getPadding().getBottom();
            double s = Math.min((vw - padX) / cw, (vh - padY) / ch);
            s *= 0.95; // Safety margin
            s = Math.max(0.2, Math.min(5.0, s));
            scale[0] = s;
            applyScale.run();
            
            // Center content
            javafx.application.Platform.runLater(() -> {
                try {
                    scrollPane.setHvalue(0.5);
                    scrollPane.setVvalue(0.5);
                } catch (Exception ignore) {}
            });
        };
        
        autoFit.setOnAction(e -> {
            updateAutoFitAppearance.run();
            if (autoFit.isSelected()) fitToView.run();
        });
        
        scrollPane.viewportBoundsProperty().addListener((o, ov, nv) -> {
            if (autoFit.isSelected()) fitToView.run();
        });
        
        content.boundsInLocalProperty().addListener((o, ov, nv) -> {
            if (autoFit.isSelected()) fitToView.run();
        });
        
        controls.getChildren().addAll(zoomOut, zoomIn, autoFit);
        
        // Ctrl + mouse wheel zooming
        scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() < 0) {
                    autoFit.setSelected(false);
                    updateAutoFitAppearance.run();
                    scale[0] = Math.max(0.2, scale[0] - 0.1);
                } else if (e.getDeltaY() > 0) {
                    autoFit.setSelected(false);
                    updateAutoFitAppearance.run();
                    scale[0] = Math.min(5.0, scale[0] + 0.1);
                }
                applyScale.run();
                e.consume();
            }
        });
        
        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane(scrollPane);
        javafx.scene.layout.BorderPane.setAlignment(controls, javafx.geometry.Pos.CENTER_RIGHT);
        root.setBottom(controls);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
