package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.DFSSolver;
import com.algorithmvisualizer.visualization.GraphRenderer;
import com.algorithmvisualizer.visualization.ArrayRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.*;

/**
 * Controller for Depth-First Search visualization.
 * Graph is shown in the main visual area, traversal order as an array on the right.
 */
public class DFSController implements AlgorithmViewController.AlgorithmSpecificController {

    // Parent reference and shared controls
    private AlgorithmViewController parent;

    // Visuals
    private GraphRenderer graphView;
    private ArrayRenderer traversalView;

    // Model / Solver
    private DFSSolver solver;
    private Timeline timeline;
    private boolean isPlaying = false;

    // History for step-back
    private final Deque<DFSSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Graph data
    private int nodeCount = 6;
    private List<List<Integer>> adj = new ArrayList<>();
    private int startNode = 0;

    @FXML
    private void initialize() {
        graphView = new GraphRenderer();
        traversalView = new ArrayRenderer();
        initDefaultGraph();
        renderVisuals();

        solver = new DFSSolver(nodeCount, adj, startNode);
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

        // Header and legend
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Depth-First Search (DFS)");
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
            javafx.scene.shape.Rectangle cur = new javafx.scene.shape.Rectangle(12,12);
            cur.setFill(javafx.scene.paint.Color.GOLD);
            cur.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lCur = new javafx.scene.layout.HBox(5.0, cur, new Label("Current"));
            javafx.scene.shape.Rectangle vis = new javafx.scene.shape.Rectangle(12,12);
            vis.setFill(javafx.scene.paint.Color.FORESTGREEN);
            vis.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lVis = new javafx.scene.layout.HBox(5.0, vis, new Label("Visited"));
            javafx.scene.shape.Rectangle ex = new javafx.scene.shape.Rectangle(12,12);
            ex.setFill(javafx.scene.paint.Color.DARKORANGE);
            ex.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox lEx = new javafx.scene.layout.HBox(5.0, ex, new Label("Exploring edge"));
            row.getChildren().addAll(lCur, lVis, lEx);
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
        if (parent.paramRandomizeButton != null) parent.paramRandomizeButton.setOnAction(e -> onRandomizeGraph());
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyGraph());

        // Solutions side: traversal array
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label solHeader = new Label("Traversal Order");
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
        adj = randomConnectedGraph(nodeCount);
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
        if (parent != null && parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(graphView.getNode());
        }
        moveTraversalToSolutions();
    }

    private void moveTraversalToSolutions() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsContent.getChildren().clear();
        parent.solutionsContent.getChildren().add(traversalView.getNode());
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
        DFSSolver.State s = history.pop();
        solver.restore(s);
        // Re-render graph and traversal from restored state
        graphView.setGraph(s.n, s.adj);
        // Mark visited nodes
        if (s.visited != null) {
            for (int i = 0; i < s.visited.length; i++) if (s.visited[i]) graphView.markVisited(i);
        }
        // Highlight current node on top of stack if present
        if (s.stack != null && !s.stack.isEmpty()) {
            DFSSolver.Frame top = s.stack.peekLast();
            if (top != null) graphView.highlightCurrent(top.u);
        }
        // Update traversal array view
        updateTraversalView();

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

    // --- Step event handling ---
    private void onStepEvent(DFSSolver.StepType type, int u, int v) {
        // Treat every event as a step for logging/step-back removal
        countLogsForStep = true;
        currentStepLogLines = 0;
        switch (type) {
            case INIT:
                graphView.clearHighlights();
                graphView.highlightCurrent(u);
                appendProgress("🚀 Start/continue DFS at node " + u);
                if (parent != null) parent.stepDescription.setText("Start at node " + u);
                break;
            case DISCOVER:
                graphView.markVisited(u);
                graphView.highlightCurrent(u);
                appendProgress("✅ Discovered node " + u);
                if (parent != null) parent.stepDescription.setText("Discovered node " + u);
                break;
            case EXPLORE_EDGE:
                graphView.highlightEdge(u, v);
                graphView.highlightCurrent(v);
                appendProgress("➡ Exploring edge " + u + "→" + v);
                if (parent != null) parent.stepDescription.setText("Exploring edge " + u + "→" + v);
                break;
            case BACKTRACK:
                appendProgress("↩ Backtrack from node " + u);
                if (parent != null) parent.stepDescription.setText("Backtrack from node " + u);
                break;
            case DONE:
                appendProgress("🎉 DFS complete");
                if (parent != null) parent.stepDescription.setText("DFS complete");
                stopTimeline();
                break;
        }
        updateTraversalView();
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

    // --- Logging & variables ---
    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        // Reset step log tracking
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        appendProgress("DFS: n = " + nodeCount + ", start = " + startNode);
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
        parent.variableList.getItems().addAll(
            "nodes: " + nodeCount,
            "start: " + startNode,
            "visitedCount: " + visitedCount,
            "done: " + (solver != null && solver.isDone())
        );
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null) return;
        String[] lines = new String[] {
                "public class DFSExample {",
                "    static void dfs(int start, List<List<Integer>> adj) {",
                "        boolean[] visited = new boolean[adj.size()];",
                "        Deque<Integer> stack = new ArrayDeque<>();",
                "        stack.push(start);",
                "        while (!stack.isEmpty()) {",
                "            int u = stack.peek();",
                "            if (!visited[u]) {",
                "                visited[u] = true;",
                "                System.out.println(\"discover \" + u);",
                "            }",
                "            boolean advanced = false;",
                "            for (int v : adj.get(u)) {",
                "                if (!visited[v]) {",
                "                    stack.push(v);",
                "                    advanced = true;",
                "                    break;",
                "                }",
                "            }",
                "            if (!advanced) stack.pop();",
                "        }",
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

    private static List<List<Integer>> randomConnectedGraph(int n) {
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
        // add extra random edges
        int extra = Math.max(0, n);
        for (int k = 0; k < extra; k++) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u != v && !g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
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
}
