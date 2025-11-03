package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.BFSSolver;
import com.algorithmvisualizer.visualization.GraphRenderer;
import com.algorithmvisualizer.visualization.ArrayRenderer;
import com.algorithmvisualizer.visualization.VisitedMatrixRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.*;

/**
 * Controller for Breadth-First Search visualization.
 * Graph is shown in the main visual area, traversal order as an array on the right.
 */
public class BFSController implements AlgorithmViewController.AlgorithmSpecificController {

    // Parent reference and shared controls
    private AlgorithmViewController parent;

    // Visuals
    private GraphRenderer graphView;
    private ArrayRenderer traversalView;
    private ArrayRenderer queueView;
    private VisitedMatrixRenderer visitedMatrixView;

    // Model / Solver
    private BFSSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;

    // History for step-back
    private final Deque<BFSSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Graph data
    private int nodeCount = 6;
    private List<List<Integer>> adj = new ArrayList<>();
    private int startNode = 0;
    private boolean allowCycles = true; // Toggle for cycle generation

    @FXML
    private void initialize() {
        graphView = new GraphRenderer();
        traversalView = new ArrayRenderer();
        queueView = new ArrayRenderer();
        visitedMatrixRenderer();
        initDefaultGraph();
        renderVisuals();

        solver = new BFSSolver(nodeCount, adj, startNode);
        solver.setStepListener(this::onStepEvent);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place visuals
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(graphView.getNode());
        }

        // Header and legend with layout toggle
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Breadth-First Search (BFS)");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            // Add toggle button for layout switching
            ToggleButton layoutToggle = new ToggleButton("Tree View");
            layoutToggle.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 11px; -fx-cursor: hand;");
            layoutToggle.setOnAction(e -> {
                if (layoutToggle.isSelected()) {
                    graphView.setLayoutMode(GraphRenderer.LayoutMode.TREE);
                    layoutToggle.setText("Circular View");
                    layoutToggle.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 11px; -fx-cursor: hand;");
                } else {
                    graphView.setLayoutMode(GraphRenderer.LayoutMode.CIRCULAR);
                    layoutToggle.setText("Tree View");
                    layoutToggle.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 11px; -fx-cursor: hand;");
                }
            });
            
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            parent.chessboardHeaderBox.getChildren().addAll(hdr, spacer, layoutToggle, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15.0);
            javafx.scene.shape.Rectangle cur = new javafx.scene.shape.Rectangle(12,12);
            cur.setFill(javafx.scene.paint.Color.GOLD);
            cur.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lCur = new javafx.scene.layout.HBox(5.0, cur, new Label("Current"));
            javafx.scene.shape.Rectangle vis = new javafx.scene.shape.Rectangle(12,12);
            vis.setFill(javafx.scene.paint.Color.FORESTGREEN);
            vis.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lVis = new javafx.scene.layout.HBox(5.0, vis, new Label("Visited"));
            javafx.scene.shape.Rectangle queued = new javafx.scene.shape.Rectangle(12,12);
            queued.setFill(javafx.scene.paint.Color.LIGHTSKYBLUE);
            queued.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lQueued = new javafx.scene.layout.HBox(5.0, queued, new Label("In Queue"));
            row.getChildren().addAll(lCur, lVis, lQueued);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters wiring
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Nodes:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, nodeCount));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onNodeCountChanged(n));
        }
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Edges (u-v, comma-separated):");
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("e.g. 0-1, 1-2, 2-3, 0-4");
            parent.paramElementsField.setText(edgesToString(adj));
        }
        // Add Cycles toggle next to Randomize button
        if (parent.paramRandomizeButton != null) {
            // Create a ToggleButton for cycles toggle with modern styling
            ToggleButton cyclesToggle = new ToggleButton("Cycles: ON");
            cyclesToggle.setSelected(allowCycles);
            cyclesToggle.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
            cyclesToggle.setOnAction(e -> {
                allowCycles = cyclesToggle.isSelected();
                if (allowCycles) {
                    cyclesToggle.setText("Cycles: ON");
                    cyclesToggle.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                } else {
                    cyclesToggle.setText("Cycles: OFF");
                    cyclesToggle.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                }
            });
            
            // Find the HBox containing the Randomize button
            javafx.scene.Parent buttonParent = parent.paramRandomizeButton.getParent();
            if (buttonParent instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox buttonBox = (javafx.scene.layout.HBox) buttonParent;
                // Add the toggle button after the Randomize button
                int randomizeIndex = buttonBox.getChildren().indexOf(parent.paramRandomizeButton);
                if (randomizeIndex >= 0) {
                    buttonBox.getChildren().add(randomizeIndex + 1, cyclesToggle);
                }
            }
            
            parent.paramRandomizeButton.setOnAction(e -> onRandomizeGraph());
        }
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyGraph());

        // Solutions side: traversal array and queue
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Traversal + Queue");
            solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().addAll(solHeader, new Separator());
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }
        moveTraversalToSolutions();

        // Controls
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        // Code + logs + variables
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");
    }

    // --- Parameter handlers ---
    private void onNodeCountChanged(int n) {
        nodeCount = Math.max(1, n);
        ensureAdjSize();
        refreshAll();
    }

    private void onRandomizeGraph() {
        adj = randomConnectedGraph(nodeCount, allowCycles);
        if (parent != null && parent.paramElementsField != null) parent.paramElementsField.setText(edgesToString(adj));
        refreshAll();
    }

    private void onApplyGraph() {
        if (parent == null || parent.paramElementsField == null) { refreshAll(); return; }
        List<List<Integer>> parsed = parseEdges(parent.paramElementsField.getText(), nodeCount);
        if (parsed != null) { adj = parsed; }
        refreshAll();
    }

    private void refreshAll() {
        stopTimeline();
        solver.setGraph(nodeCount, adj);
        solver.setStart(startNode);
        renderVisuals();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
    }

    private void renderVisuals() {
        graphView.setGraph(nodeCount, adj);
        updateTraversalView();
        updateQueueView();
        if (visitedMatrixView != null) visitedMatrixView.setSize(nodeCount);
        updateVisitedMatrix();
        if (parent != null && parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(graphView.getNode());
        }
        moveTraversalToSolutions();
    }

    private void moveTraversalToSolutions() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        
        // Add traversal label and view
        Label traversalLabel = new Label("Traversal Order:");
        traversalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        parent.solutionsContent.getChildren().addAll(traversalLabel, traversalView.getNode());
        
        // Add queue label and view
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        spacer.setPrefHeight(20.0);
        Label queueLabel = new Label("Current Queue:");
        queueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        parent.solutionsContent.getChildren().addAll(spacer, queueLabel, queueView.getNode());
        
        // Add visited matrix
        if (visitedMatrixView != null) {
            javafx.scene.layout.Region spacer2 = new javafx.scene.layout.Region();
            spacer2.setPrefHeight(20.0);
            parent.solutionsContent.getChildren().add(spacer2);
            parent.solutionsContent.getChildren().add(visitedMatrixView.getNode());
        }
    }

    // --- Controls ---
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
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        BFSSolver.State s = history.pop();
        solver.restore(s);
        // Re-render graph and traversal from restored state
        graphView.setGraph(s.n, s.adj);
        // Mark visited nodes
        if (s.visited != null) {
            for (int i = 0; i < s.visited.length; i++) if (s.visited[i]) graphView.markVisited(i);
        }
        // Highlight nodes in queue
        if (s.queue != null) {
            for (int node : s.queue) {
                graphView.highlightCurrent(node);
            }
        }
        // Update views
        updateTraversalView();
        updateQueueView();
        updateVisitedMatrix();

        // Remove the logs of the last step
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
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    // --- Step event handling ---
    private void onStepEvent(BFSSolver.StepType type, int u, int v) {
        // Treat every event as a step for logging/step-back removal
        countLogsForStep = true;
        currentStepLogLines = 0;
        switch (type) {
            case INIT:
                graphView.clearHighlights();
                graphView.highlightCurrent(u);
                appendProgress("🚀 Start/continue BFS at node " + u);
                if (parent != null) parent.stepDescription.setText("Start at node " + u);
                break;
            case DEQUEUE:
                graphView.highlightCurrent(u);
                appendProgress("⬅ Dequeue node " + u);
                if (parent != null) parent.stepDescription.setText("Dequeue node " + u);
                break;
            case DISCOVER:
                graphView.markVisited(u);
                appendProgress("✅ Discovered node " + u);
                if (parent != null) parent.stepDescription.setText("Discovered node " + u);
                break;
            case EXPLORE_EDGE:
                graphView.highlightEdge(u, v);
                appendProgress("➡ Exploring edge " + u + "→" + v);
                if (parent != null) parent.stepDescription.setText("Exploring edge " + u + "→" + v);
                break;
            case ENQUEUE:
                graphView.highlightCurrent(u);
                appendProgress("➕ Enqueue node " + u);
                if (parent != null) parent.stepDescription.setText("Enqueue node " + u);
                break;
            case DONE:
                appendProgress("🎉 BFS complete");
                if (parent != null) parent.stepDescription.setText("BFS complete");
                stopTimeline();
                break;
        }
        updateTraversalView();
        updateQueueView();
        updateVisitedMatrix();
        updateVariablesPanel();
        // finalize log group
        if (countLogsForStep) {
            progressHistory.push(currentStepLogLines);
            countLogsForStep = false;
            currentStepLogLines = 0;
        }
    }

    private void updateTraversalView() {
        List<Integer> list = solver != null ? solver.getTraversal() : Collections.emptyList();
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        traversalView.setData(arr);
    }

    private void updateQueueView() {
        Queue<Integer> queue = solver != null ? solver.getQueue() : new LinkedList<>();
        int[] arr = new int[queue.size()];
        int idx = 0;
        for (int node : queue) arr[idx++] = node;
        queueView.setData(arr);
    }

    // --- Logging & variables ---
    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        // Reset step log tracking
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        appendProgress("BFS: n = " + nodeCount + ", start = " + startNode);
    }

    private void updateVisitedMatrix() {
        if (visitedMatrixView == null) return;
        boolean[] vis = solver != null ? solver.getVisited() : new boolean[nodeCount];
        visitedMatrixView.updateVisited(vis);
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
        boolean[] vis = solver != null ? solver.getVisited() : new boolean[nodeCount];
        int visitedCount = 0;
        for (boolean b : vis) if (b) visitedCount++;
        int queueSize = solver != null ? solver.getQueue().size() : 0;
        parent.variableList.getItems().addAll(
            "nodes: " + nodeCount,
            "start: " + startNode,
            "visitedCount: " + visitedCount,
            "queueSize: " + queueSize,
            "done: " + (solver != null && solver.isDone())
        );
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        // Build edges literal from current adjacency (unique undirected pairs i<j)
        List<String> edgePairs = new ArrayList<>();
        for (int i = 0; i < adj.size(); i++) {
            for (int j : adj.get(i)) {
                if (i < j) edgePairs.add("{" + i + ", " + j + "}");
            }
        }
        String edgesLiteral = edgePairs.isEmpty() ? "" : String.join(", ", edgePairs);
        String[] lines = new String[] {
                "import java.util.*;",
                "",
                "public class BFSExample {",
                "    static final int N = " + nodeCount + ";",
                "    static final int START = " + startNode + ";",
                "    static final int[][] EDGES = { " + edgesLiteral + " };",
                "",
                "    public static void main(String[] args) {",
                "        List<List<Integer>> adj = new ArrayList<>();",
                "        for (int i = 0; i < N; i++) adj.add(new ArrayList<>());",
                "        for (int[] e : EDGES) { int u = e[0], v = e[1]; adj.get(u).add(v); adj.get(v).add(u); }",
                "        for (int i = 0; i < N; i++) Collections.sort(adj.get(i));",
                "",
                "        long startTime = System.currentTimeMillis();",
                "        List<Integer> order = bfs(START, adj);",
                "        long endTime = System.currentTimeMillis();",
                "",
                "        System.out.println(\"BFS traversal order: \" + order);",
                "        System.out.println(\"Execution time: \" + (endTime - startTime) + \" ms\");",
                "    }",
                "",
                "    static List<Integer> bfs(int start, List<List<Integer>> adj) {",
                "        boolean[] visited = new boolean[adj.size()];",
                "        Queue<Integer> queue = new LinkedList<>();",
                "        List<Integer> order = new ArrayList<>();",
                "        queue.offer(start);",
                "        while (!queue.isEmpty()) {",
                "            int u = queue.poll();",
                "            if (!visited[u]) {",
                "                visited[u] = true;",
                "                order.add(u);",
                "                for (int v : adj.get(u)) {",
                "                    if (!visited[v]) {",
                "                        queue.offer(v);",
                "                    }",
                "                }",
                "            }",
                "        }",
                "        return order;",
                "    }",
                "}",
        };
        parent.codeArea.setText(String.join("\n", lines));
    }

    // --- Helpers: graph ---
    private void initDefaultGraph() {
        adj = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) adj.add(new ArrayList<>());
        // create a simple connected shape
        addEdge(0,1); addEdge(1,2); addEdge(2,3); addEdge(3,0); // a square
        addEdge(1,4); addEdge(4,5);
    }

    private void ensureAdjSize() {
        if (adj == null) adj = new ArrayList<>();
        while (adj.size() < nodeCount) adj.add(new ArrayList<>());
        while (adj.size() > nodeCount) adj.remove(adj.size()-1);
        for (int i = 0; i < nodeCount; i++) {
            final int ii = i; // capture for lambda
            List<Integer> row = adj.get(ii);
            row.removeIf(v -> v < 0 || v >= nodeCount || v == ii);
            // ensure symmetry for undirected
            for (int v : new ArrayList<>(row)) {
                if (!adj.get(v).contains(ii)) adj.get(v).add(ii);
            }
            Collections.sort(row);
        }
    }

    private void addEdge(int u, int v) {
        if (u == v) return;
        while (adj.size() <= Math.max(u, v)) adj.add(new ArrayList<>());
        if (!adj.get(u).contains(v)) adj.get(u).add(v);
        if (!adj.get(v).contains(u)) adj.get(v).add(u);
    }

    private static List<List<Integer>> randomConnectedGraph(int n, boolean allowCycles) {
        Random rnd = new Random();
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        // create a random spanning tree
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add(i);
        Collections.shuffle(order, rnd);
        for (int i = 1; i < n; i++) {
            int u = order.get(i);
            int v = order.get(rnd.nextInt(i));
            if (!g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
        }
        // add extra random edges only if cycles are allowed
        if (allowCycles) {
            int extra = Math.max(0, n);
            for (int k = 0; k < extra; k++) {
                int u = rnd.nextInt(n), v = rnd.nextInt(n);
                if (u != v && !g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
            }
        }
        for (List<Integer> row : g) Collections.sort(row);
        return g;
    }

    private static List<List<Integer>> parseEdges(String text, int n) {
        if (text == null) return null;
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        String[] parts = text.split(",");
        try {
            for (String p : parts) {
                String s = p.trim();
                if (s.isEmpty()) continue;
                String[] uv = s.split("-");
                if (uv.length != 2) return null;
                int u = Integer.parseInt(uv[0].trim());
                int v = Integer.parseInt(uv[1].trim());
                if (u < 0 || v < 0 || u >= n || v >= n || u == v) return null;
                if (!g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
            }
            for (List<Integer> row : g) Collections.sort(row);
            return g;
        } catch (Exception ex) { return null; }
    }

    private static String edgesToString(List<List<Integer>> g) {
        if (g == null) return "";
        Set<String> set = new LinkedHashSet<>();
        for (int i = 0; i < g.size(); i++) {
            for (int j : g.get(i)) if (i < j) set.add(i + "-" + j);
        }
        return String.join(", ", set);
    }

    private void visitedMatrixRenderer() {
        visitedMatrixView = new VisitedMatrixRenderer();
    }
}
