package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.BellmanFordSolver;
import com.algorithmvisualizer.visualization.GraphRenderer;
import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.code.implementations.BellmanFordCode;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.*;

public class BellmanFordController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;
    private BellmanFordSolver solver;
    private GraphRenderer graphView;

    // Parameters
    private int nodeCount = 6;
    private int source = 0;
    private int[][] edges;

    // UI Components
    private TableView<DistanceRow> distanceTable;
    private ObservableList<DistanceRow> distanceData;

    // State
    private Stack<BellmanFordSolver.State> history;
    private Timeline timeline;

    // Colors
    private static final Color COLOR_UNVISITED = Color.LIGHTGRAY;
    private static final Color COLOR_CURRENT = Color.GOLD;
    private static final Color COLOR_FRONTIER = Color.CYAN;
    private static final Color COLOR_VISITED = Color.LIGHTGREEN;
    private static final Color COLOR_PATH = Color.rgb(50, 205, 50);
    private static final Color COLOR_CONSIDERING = Color.ORANGE;
    private static final Color COLOR_REJECTED = Color.RED;
    private static final Color COLOR_NEGATIVE_CYCLE = Color.MAGENTA;

    public static class DistanceRow {
        private final int vertex;
        private String distance;
        private String previous;
        private String status;

        public DistanceRow(int vertex, String distance, String previous, String status) {
            this.vertex = vertex;
            this.distance = distance;
            this.previous = previous;
            this.status = status;
        }

        public int getVertex() {
            return vertex;
        }

        public String getDistance() {
            return distance;
        }

        public String getPrevious() {
            return previous;
        }

        public String getStatus() {
            return status;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public void setPrevious(String previous) {
            this.previous = previous;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @FXML
    private void initialize() {
        // Initialize state FIRST (per guide critical mistake #2)
        graphView = new GraphRenderer();
        graphView.setDirected(true);
        graphView.showEdgeWeights(true);

        distanceTable = buildDistanceTable();

        initDefaultGraph();
        renderGraph();
        solver = new BellmanFordSolver();
        solver.setGraph(nodeCount, edges, source);
        solver.setStepListener(this::onStepEvent);
        solver.reset();

        history = new Stack<>();
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place graph view
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(graphView.getNode());
        }

        // Header
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Bellman-Ford Shortest Path");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().add(hdr);
        }

        // Legend
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(15.0);

            Circle current = new Circle(8, COLOR_CURRENT);
            HBox lCurrent = new HBox(5.0, current, new Label("Current Edge"));

            Circle updated = new Circle(8, COLOR_FRONTIER);
            HBox lUpdated = new HBox(5.0, updated, new Label("Updated"));

            Circle pathNode = new Circle(8, COLOR_PATH);
            HBox lPath = new HBox(5.0, pathNode, new Label("Shortest Path"));

            Circle negCycle = new Circle(8, COLOR_NEGATIVE_CYCLE);
            HBox lNegCycle = new HBox(5.0, negCycle, new Label("Neg. Cycle"));

            row.getChildren().addAll(lCurrent, lUpdated, lPath, lNegCycle);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        setupParameterControls();
        buildSolutionsPane();

        // Controls wiring
        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }

        // Initialize text areas
        parent.setCurrentAlgorithmName("Bellman-Ford Shortest Path");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) {
            parent.stepDescription.setText("Ready.");
        }
    }

    private void setupParameterControls() {
        if (parent.paramSizeLabel != null)
            parent.paramSizeLabel.setText("Nodes:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 12, nodeCount));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> {
                nodeCount = n;
                onNodeCountChanged();
            });
        }

        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Edges (u-v:w, comma-separated):");
        }

        if (parent.paramElementsBox != null) {
            parent.paramElementsBox.setVisible(false);
            parent.paramElementsBox.setManaged(false);
        }

        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("e.g. 0-1:4, 1-2:-2, 2-3:5");
            parent.paramElementsField.setText(edgesToString(edges));
        }

        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setOnAction(e -> onRandomizeGraph());
            parent.paramRandomizeButton.setStyle(
                    "-fx-background-color: #0ea5e9; -fx-text-fill: white; " +
                            "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        }

        if (parent.paramApplyButton != null) {
            parent.paramApplyButton.setOnAction(e -> onApplyGraph());
            parent.paramApplyButton.setStyle(
                    "-fx-background-color: #10b981; -fx-text-fill: white; " +
                            "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
        }
    }

    private void buildSolutionsPane() {
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox.setVisible(true);
            parent.solutionsHeaderBox.setManaged(true);
            parent.solutionsHeaderBox.getChildren().clear();
            Label header = new Label("Shortest Distances");
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.solutionsHeaderBox.getChildren().add(header);
        }

        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox.setVisible(false);
            parent.solutionsSubHeaderBox.setManaged(false);
        }

        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            VBox.setVgrow(distanceTable, Priority.ALWAYS);
            parent.solutionsContent.getChildren().add(distanceTable);
        }
    }

    private TableView<DistanceRow> buildDistanceTable() {
        TableView<DistanceRow> table = new TableView<>();

        TableColumn<DistanceRow, Integer> vertexCol = new TableColumn<>("Vertex");
        vertexCol.setCellValueFactory(new PropertyValueFactory<>("vertex"));
        vertexCol.setPrefWidth(60);

        TableColumn<DistanceRow, String> distCol = new TableColumn<>("Distance");
        distCol.setCellValueFactory(new PropertyValueFactory<>("distance"));
        distCol.setPrefWidth(80);

        TableColumn<DistanceRow, String> prevCol = new TableColumn<>("Previous");
        prevCol.setCellValueFactory(new PropertyValueFactory<>("previous"));
        prevCol.setPrefWidth(70);

        TableColumn<DistanceRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(90);

        statusCol.setCellFactory(col -> new TableCell<DistanceRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Circle dot = new Circle(6);
                switch (item) {
                    case "Unvisited":
                        dot.setFill(COLOR_UNVISITED);
                        break;
                    case "Updated":
                        dot.setFill(COLOR_FRONTIER);
                        break;
                    case "Relaxed":
                        dot.setFill(COLOR_VISITED);
                        break;
                    case "Neg. Cycle":
                        dot.setFill(COLOR_NEGATIVE_CYCLE);
                        break;
                    default:
                        dot.setFill(Color.TRANSPARENT);
                }

                HBox box = new HBox(5, dot, new Label(item));
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(vertexCol, distCol, prevCol, statusCol);

        distanceData = FXCollections.observableArrayList();
        table.setItems(distanceData);

        return table;
    }

    private void initDefaultGraph() {
        // Default graph with some edges — positive weights only for default
        edges = new int[][] {
                { 0, 1, 6 }, { 0, 2, 7 },
                { 1, 2, 8 }, { 1, 3, 5 }, { 1, 4, -4 },
                { 2, 3, -3 }, { 2, 4, 9 },
                { 3, 1, -2 },
                { 4, 0, 2 }, { 4, 3, 7 }
        };
    }

    private void onNodeCountChanged() {
        edges = generateRandomGraph(nodeCount);
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(edgesToString(edges));
        }
        refreshAll();
    }

    private void onRandomizeGraph() {
        edges = generateRandomGraph(nodeCount);
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(edgesToString(edges));
        }
        refreshAll();
    }

    private void onApplyGraph() {
        if (parent.paramElementsField != null) {
            String text = parent.paramElementsField.getText();
            int[][] parsedEdges = parseEdges(text, nodeCount);
            if (parsedEdges.length > 0) {
                edges = parsedEdges;
            }
        }
        source = 0;
        refreshAll();
    }

    private int[][] generateRandomGraph(int n) {
        Random rand = new Random();
        Map<String, int[]> edgeMap = new LinkedHashMap<>();

        // Create connected graph using spanning tree first (directed edges)
        for (int i = 1; i < n; i++) {
            int p = rand.nextInt(i);
            int weight = rand.nextInt(20) - 4; // range -4 to 15
            edgeMap.put(p + "->" + i, new int[] { p, i, weight });
        }

        // Add additional random directed edges
        int additionalEdges = n + rand.nextInt(n);
        int attempts = 0;
        int maxAttempts = additionalEdges * 3;

        while (edgeMap.size() < (n - 1) + additionalEdges && attempts < maxAttempts) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);
            if (u != v) {
                String key = u + "->" + v;
                if (!edgeMap.containsKey(key)) {
                    int weight = rand.nextInt(20) - 4; // range -4 to 15
                    edgeMap.put(key, new int[] { u, v, weight });
                }
            }
            attempts++;
        }

        return edgeMap.values().toArray(new int[0][]);
    }

    private static int[][] parseEdges(String text, int n) {
        if (text == null || text.trim().isEmpty())
            return new int[0][];

        Map<String, int[]> edgeMap = new LinkedHashMap<>();
        String[] parts = text.split(",");

        try {
            for (String p : parts) {
                String s = p.trim();
                if (s.isEmpty())
                    continue;

                // Format: u-v:w
                String[] edgeWeight = s.split(":");
                if (edgeWeight.length != 2)
                    continue;

                String[] uv = edgeWeight[0].split("-");
                if (uv.length != 2)
                    continue;

                int u = Integer.parseInt(uv[0].trim());
                int v = Integer.parseInt(uv[1].trim());
                int w = Integer.parseInt(edgeWeight[1].trim());

                if (u < 0 || v < 0 || u >= n || v >= n || u == v)
                    continue;

                String key = u + "->" + v;
                edgeMap.put(key, new int[] { u, v, w });
            }
            return edgeMap.values().toArray(new int[0][]);
        } catch (Exception ex) {
            return new int[0][];
        }
    }

    private static String edgesToString(int[][] edges) {
        if (edges == null || edges.length == 0)
            return "";

        List<String> parts = new ArrayList<>();
        for (int[] e : edges) {
            parts.add(e[0] + "-" + e[1] + ":" + e[2]);
        }
        return String.join(", ", parts);
    }

    private void refreshAll() {
        stopTimeline();
        history.clear();
        renderGraph();
        solver = new BellmanFordSolver();
        solver.setGraph(nodeCount, edges, source);
        solver.setStepListener(this::onStepEvent);
        solver.reset();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        updateDistanceTable();
    }

    private void renderGraph() {
        // Build adjacency list for graph display
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            adj.add(new ArrayList<>());
        }

        Map<String, Integer> weights = new LinkedHashMap<>();
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int w = edge[2];
            adj.get(u).add(v);
            // Use directed edge key format "u->v" to match GraphRenderer directed mode
            String key = u + "->" + v;
            weights.put(key, w);
        }

        graphView.setGraph(nodeCount, adj);
        graphView.setEdgeWeights(weights);

        // Reset colors
        for (int i = 0; i < nodeCount; i++) {
            graphView.setNodeColor(i, COLOR_UNVISITED);
        }
    }

    private void updateDistanceTable() {
        distanceData.clear();

        int[] dist = solver.getDistances();
        int[] prev = solver.getPrevious();

        for (int i = 0; i < nodeCount; i++) {
            String distStr = formatDistance(dist[i]);
            String prevStr = prev[i] == -1 ? "-" : String.valueOf(prev[i]);
            String status = dist[i] != Integer.MAX_VALUE && i != source ? "Updated"
                    : (i == source ? "Source" : "Unvisited");

            distanceData.add(new DistanceRow(i, distStr, prevStr, status));
        }
    }

    private String formatDistance(int dist) {
        return dist == Integer.MAX_VALUE ? "∞" : String.valueOf(dist);
    }

    @Override
    public void onPause() {
        stopTimeline();
    }

    @Override
    public void onPlay() {
        if (solver.isDone()) {
            onReset();
        }
        rebuildTimelineWithCurrentSpeed();
        // Per guide critical mistake #3
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(true);
            parent.pauseButton.setManaged(true);
            parent.pauseButton.setDisable(false);
        }
        if (parent.playButton != null) {
            parent.playButton.setVisible(false);
            parent.playButton.setManaged(false);
        }
    }

    @Override
    public void onStepForward() {
        if (!solver.isDone()) {
            history.push(solver.snapshot());
            solver.step();
        }
    }

    @Override
    public void onStepBack() {
        if (!history.isEmpty()) {
            BellmanFordSolver.State prevState = history.pop();
            solver.restore(prevState);
            renderGraph();
            visualizeState(prevState);
            updateDistanceTable();
            updateVariablesPanel();
        }
    }

    @Override
    public void onReset() {
        stopTimeline();
        history.clear();
        solver.reset();
        renderGraph();
        initProgressLog();
        updateVariablesPanel();
        updateDistanceTable();
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        if (parent.playButton != null) {
            parent.playButton.setVisible(true);
            parent.playButton.setManaged(true);
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }
    }

    private void rebuildTimelineWithCurrentSpeed() {
        stopTimeline();
        double speed = parent.speedSlider != null ? parent.speedSlider.getValue() : 1.0;
        long delayMs = (long) (1000.0 / speed);

        timeline = new Timeline(new KeyFrame(Duration.millis(delayMs), e -> {
            if (!solver.isDone()) {
                history.push(solver.snapshot());
                solver.step();
            } else {
                stopTimeline();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updatePlaybackSpeed() {
        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
            rebuildTimelineWithCurrentSpeed();
        }
    }

    private void onStepEvent(BellmanFordSolver.StepType type, int u, int v, int distance, Map<String, Object> meta) {
        int edgeWeight;

        switch (type) {
            case INIT:
                appendProgress("🚀 Starting Bellman-Ford algorithm...");
                appendProgress("Source vertex: " + source);
                appendProgress("Vertices: " + nodeCount + ", Edges: " + edges.length);
                break;

            case HIGHLIGHT_SOURCE:
                appendProgress("⭐ Highlighting source vertex " + source);
                graphView.glowVertex(source, COLOR_CURRENT);
                graphView.shakeNode(source);
                break;

            case INITIALIZE_DISTANCES:
                appendProgress("📊 Initializing distances: source = 0, others = ∞");
                graphView.removeGlow(source);
                graphView.setNodeColor(source, COLOR_FRONTIER);
                updateDistanceTable();
                break;

            case START_ITERATION:
                int iteration = (Integer) meta.get("iteration");
                int totalIterations = (Integer) meta.get("totalIterations");
                appendProgress("\n═══ Iteration " + iteration + "/" + totalIterations + " ═══");
                break;

            case RESET_EDGES_FOR_ITERATION:
                appendProgress("  🔄 Resetting edges for new iteration...");
                clearIterationVisuals();
                break;

            case SELECT_EDGE:
                edgeWeight = (Integer) meta.get("edgeWeight");
                int edgeIndex = (Integer) meta.get("edgeIndex");
                int totalEdges = (Integer) meta.get("totalEdges");
                appendProgress("  📌 Edge " + (edgeIndex + 1) + "/" + totalEdges + ": " + u + " → " + v + " (weight: "
                        + edgeWeight + ")");
                graphView.highlightEdgeWithColor(u, v, COLOR_CONSIDERING, 3.0);
                graphView.blinkEdge(u, v, COLOR_CONSIDERING, 1);
                break;

            case CALCULATE_DISTANCE:
                appendProgress("    Calculating: " + formatDistance((Integer) meta.get("currentDist")) + " + " +
                        meta.get("edgeWeight") + " = " + meta.get("tentativeDist"));
                break;

            case COMPARE_DISTANCES:
                boolean improved = (Boolean) meta.get("improved");
                if (improved) {
                    appendProgress("    ✓ " + formatDistance((Integer) meta.get("newDist")) + " < " +
                            formatDistance((Integer) meta.get("oldDist")) + " — Better path found!");
                } else {
                    appendProgress("    ✗ " + formatDistance((Integer) meta.get("newDist")) + " >= " +
                            formatDistance((Integer) meta.get("oldDist")) + " — No improvement");
                }
                break;

            case UPDATE_DISTANCE:
                appendProgress("    ✅ Updated distance to " + v + ": " + meta.get("newDist") + " (via " + u + ")");
                graphView.pulseEdge(u, v, COLOR_VISITED);
                graphView.highlightEdgeWithColor(u, v, COLOR_VISITED, 2.0);
                graphView.setNodeColor(v, COLOR_FRONTIER);
                updateDistanceTable();
                break;

            case REJECT_DISTANCE:
                if (meta != null && meta.containsKey("reason")) {
                    appendProgress("    \u23ED Skipped: " + meta.get("reason"));
                } else {
                    appendProgress("    \u274C Keeping existing distance");
                }
                graphView.highlightEdgeWithColor(u, v, COLOR_REJECTED, 2.0);
                graphView.shakeEdge(u, v);
                if (v != source && distance != Integer.MAX_VALUE) {
                    graphView.shakeNode(v);
                }
                break;

            case END_ITERATION:
                boolean changed = (Boolean) meta.get("changed");
                if (changed) {
                    appendProgress("  ✅ Iteration complete — distances were updated");
                } else {
                    appendProgress("  ℹ️ Iteration complete — no changes (early convergence possible)");
                }
                break;

            case SHOW_ITERATION_SUMMARY:
                boolean sumChanged = (Boolean) meta.get("changed");
                int[] sumDist = (int[]) meta.get("distances");
                appendProgress("  📋 Iteration Summary:");
                StringBuilder distSummary = new StringBuilder("    Distances: [");
                for (int i = 0; i < nodeCount; i++) {
                    if (i > 0)
                        distSummary.append(", ");
                    distSummary.append(sumDist[i] == Integer.MAX_VALUE ? "∞" : sumDist[i]);
                }
                distSummary.append("]");
                appendProgress(distSummary.toString());

                // Glow updated vertices in cyan
                if (sumChanged) {
                    for (int i = 0; i < nodeCount; i++) {
                        if (sumDist[i] != Integer.MAX_VALUE && i != source) {
                            graphView.glowVertex(i, Color.CYAN);
                        }
                    }
                }
                break;

            case EARLY_TERMINATION:
                appendProgress("\n🎉 EARLY CONVERGENCE! No changes detected — algorithm converged.");
                // Flash all reachable nodes green
                int[] earlyDist = solver.getDistances();
                for (int i = 0; i < nodeCount; i++) {
                    if (earlyDist[i] != Integer.MAX_VALUE) {
                        graphView.setNodeColor(i, Color.web("#22c55e"));
                        graphView.glowVertex(i, Color.web("#22c55e"));
                    }
                }
                break;

            case START_NEGATIVE_CYCLE_CHECK:
                appendProgress("\n═══ Negative Cycle Detection Pass ═══");
                appendProgress("Checking if any edge can still be relaxed — Iterating over all edges...");
                clearIterationVisuals();
                break;

            case CHECK_NEGATIVE_CYCLE_SELECT:
                int negEdgeIdx = (Integer) meta.get("edgeIndex");
                int totalNegEdges = (Integer) meta.get("totalEdges");
                appendProgress("  📌 Edge " + (negEdgeIdx + 1) + "/" + totalNegEdges + ": " + u + " → " + v);
                graphView.highlightEdgeWithColor(u, v, Color.DARKORCHID, 3.0);
                graphView.blinkEdge(u, v, Color.DARKORCHID, 1);
                break;

            case CHECK_NEGATIVE_CYCLE_CALC:
                appendProgress(
                        "    Distance Calculation: " + formatDistance((Integer) meta.get("currentDist")) + " + " +
                                meta.get("edgeWeight") + " = "
                                + ((Integer) meta.get("currentDist") + (Integer) meta.get("edgeWeight")));
                break;

            case NEG_CHECK_SKIP:
                if (meta != null && meta.containsKey("reason")) {
                    appendProgress("    [Skip] Source unreachable");
                } else {
                    appendProgress("    [Skip] No cycle detected at this edge");
                }
                graphView.highlightEdgeWithColor(u, v, Color.DARKORCHID, 2.0);
                break;

            case CHECK_NEGATIVE_CYCLE_COMPARE:
                int currentDist = (Integer) meta.get("currentDist");
                int weight = (Integer) meta.get("edgeWeight");
                int targetDist = solver.getDistances()[v];
                boolean canRelax = (currentDist + weight) < targetDist;
                appendProgress("    Compare: " + (currentDist + weight) + " < " + formatDistance(targetDist) + " ? "
                        + (canRelax ? "YES" : "NO"));
                break;

            case NEGATIVE_CYCLE_FOUND:
                int eu = (Integer) meta.get("edgeU");
                int ev = (Integer) meta.get("edgeV");
                edgeWeight = (Integer) meta.get("edgeWeight");
                appendProgress("🚨 NEGATIVE CYCLE DETECTED!");
                appendProgress(
                        "  [ALERT] Edge " + eu + " → " + ev + " (weight: " + edgeWeight + ") can STILL be relaxed!");
                appendProgress("  This confirms an infinite path-shortening loop exists.");

                graphView.highlightEdgeWithColor(eu, ev, COLOR_NEGATIVE_CYCLE, 5.0);
                graphView.pulseEdge(eu, ev, COLOR_NEGATIVE_CYCLE);
                graphView.shakeEdge(eu, ev);
                graphView.shakeNode(eu);
                graphView.shakeNode(ev);
                graphView.setNodeColor(eu, COLOR_NEGATIVE_CYCLE);
                graphView.setNodeColor(ev, COLOR_NEGATIVE_CYCLE);
                graphView.glowVertex(eu, COLOR_NEGATIVE_CYCLE);
                graphView.glowVertex(ev, COLOR_NEGATIVE_CYCLE);
                break;

            case TRACE_NEGATIVE_CYCLE:
                List<Integer> cycleNodes = (List<Integer>) meta.get("cycleNodes");
                List<int[]> cycleEdges = (List<int[]>) meta.get("cycleEdges");
                appendProgress("\n🔍 TRACING THE CYCLE LOOP:");
                StringBuilder traceStr = new StringBuilder("  Loop Path: ");
                for (int i = 0; i < cycleNodes.size(); i++) {
                    if (i > 0)
                        traceStr.append(" -> ");
                    traceStr.append(cycleNodes.get(i));
                    graphView.glowVertex(cycleNodes.get(i), COLOR_NEGATIVE_CYCLE);
                    graphView.shakeNode(cycleNodes.get(i));
                }
                appendProgress(traceStr.toString());

                for (int[] edge : cycleEdges) {
                    graphView.highlightEdgeWithColor(edge[0], edge[1], COLOR_NEGATIVE_CYCLE, 4.0);
                    graphView.pulseEdge(edge[0], edge[1], COLOR_NEGATIVE_CYCLE);
                }
                appendProgress("  [Conclusion] The sum of weights in this cycle is NEGATIVE.");
                break;

            case NO_NEGATIVE_CYCLE:
                appendProgress("✅ No negative cycle detected. Distances are valid.");
                // Mark all reachable nodes as relaxed
                int[] dist = solver.getDistances();
                for (int i = 0; i < nodeCount; i++) {
                    if (dist[i] != Integer.MAX_VALUE) {
                        graphView.setNodeColor(i, COLOR_VISITED);
                    }
                }
                break;

            case PATH_ANIMATION_START:
                // Clear and rebuild graph for path display
                renderGraph();
                int targetVert = (Integer) meta.get("targetVertex");
                int pathNum = (Integer) meta.get("pathNumber");
                int totalPaths = (Integer) meta.get("totalPaths");

                appendProgress("\n" + "═".repeat(50));
                appendProgress("🎯 PATH " + pathNum + "/" + totalPaths + ": Source 0 → Target " + targetVert);
                appendProgress("═".repeat(50));
                break;

            case PATH_ANIMATION_NODE:
                int nodeIdx = (Integer) meta.get("nodeIndex");
                int totalNodes = (Integer) meta.get("totalNodes");

                appendProgress("  📍 Node " + u + " (step " + (nodeIdx + 1) + "/" + totalNodes + ")");
                graphView.setNodeColor(u, COLOR_PATH);
                graphView.glowVertex(u, COLOR_PATH);
                break;

            case PATH_ANIMATION_EDGE:
                targetVert = (Integer) meta.get("targetVertex");

                // Find edge weight
                edgeWeight = 0;
                for (int[] edge : edges) {
                    if (edge[0] == u && edge[1] == v) {
                        edgeWeight = edge[2];
                        break;
                    }
                }

                appendProgress("  ➡️  Edge " + u + " → " + v + " (weight: " + edgeWeight + ")");
                graphView.highlightEdgeWithColor(u, v, COLOR_PATH, 4.0);
                graphView.pulseEdge(u, v, COLOR_PATH);
                break;

            case PATH_ANIMATION_END:
                targetVert = (Integer) meta.get("targetVertex");
                pathNum = (Integer) meta.get("pathNumber");
                totalPaths = (Integer) meta.get("totalPaths");
                int pathDist = (Integer) meta.get("distance");

                List<List<Integer>> allPaths = solver.getAllPaths();
                if (allPaths != null && pathNum - 1 < allPaths.size()) {
                    List<Integer> path = allPaths.get(pathNum - 1);

                    appendProgress("");
                    appendProgress("📊 SHORTEST PATH SUMMARY:");
                    appendProgress("Source: 0 → Target: " + targetVert);
                    appendProgress("Total Distance: " + formatDistance(pathDist));
                    appendProgress("Path Length: " + (path.size() - 1) + " edges");
                    appendProgress("═".repeat(50));
                }
                break;

            case ALL_PATHS_COMPLETE:
                appendProgress("\n" + "═".repeat(50));
                appendProgress("✅ ALGORITHM COMPLETE");
                appendProgress("═".repeat(50));
                appendProgress("All shortest paths from source 0 have been computed and visualized.");
                appendProgress("═".repeat(50));
                break;

            case COMPLETE:
                if (solver.hasNegativeCycle()) {
                    appendProgress("\n" + "═".repeat(50));
                    appendProgress("⚠️ BELLMAN-FORD TERMINATED: Negative Cycle Detected");
                    appendProgress("═".repeat(50));
                    appendProgress("The graph contains a negative-weight cycle reachable from the source.");
                    appendProgress("Shortest paths are undefined in the presence of negative cycles.");
                    appendProgress("═".repeat(50));
                } else {
                    appendProgress("\n✅ Bellman-Ford complete!");
                }
                break;
        }

        updateVariablesPanel();
    }

    private void clearIterationVisuals() {
        // Clear vertex glows
        for (int i = 0; i < nodeCount; i++) {
            graphView.removeGlow(i);
        }
        // Reset node highlights — source stays cyan, others reset to base blue
        for (int i = 0; i < nodeCount; i++) {
            if (i == source) {
                graphView.setNodeColor(i, COLOR_FRONTIER);
            } else {
                graphView.setNodeColor(i, Color.STEELBLUE);
            }
            graphView.shakeNode(i);
        }
        // Reset edge colors for new iteration
        for (int[] edge : edges) {
            graphView.highlightEdgeWithColor(edge[0], edge[1], Color.LIGHTGRAY, 1.0);
        }
    }

    private void visualizeState(BellmanFordSolver.State s) {
        for (int i = 0; i < nodeCount; i++) {
            if (s.dist[i] != Integer.MAX_VALUE) {
                graphView.setNodeColor(i, COLOR_VISITED);
            } else {
                graphView.setNodeColor(i, COLOR_UNVISITED);
            }
        }
    }

    private void initProgressLog() {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.clear();
            appendProgress("=== Bellman-Ford Shortest Path Algorithm ===");
            appendProgress("Supports negative edge weights and detects negative cycles.");
            appendProgress("Ready to start. Click Play or Step Forward.");
            appendProgress("");
        }
    }

    private void appendProgress(String message) {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.appendText(message + "\n");
        }
    }

    private void updateVariablesPanel() {
        if (parent != null && parent.variableList != null) {
            ObservableList<String> vars = FXCollections.observableArrayList();

            vars.add("Nodes: " + nodeCount);
            vars.add("Edges: " + edges.length);
            vars.add("Source: " + source);
            vars.add("Iteration: " + solver.getCurrentIteration() + "/" + (nodeCount - 1));

            int relaxedCount = 0;
            int[] dist = solver.getDistances();
            for (int d : dist) {
                if (d != Integer.MAX_VALUE)
                    relaxedCount++;
            }
            vars.add("Relaxed: " + relaxedCount + "/" + nodeCount);

            if (solver.hasNegativeCycle()) {
                vars.add("⚠️ Negative Cycle!");
            }

            parent.variableList.setItems(vars);
        }
    }

    private void renderCode() {
        if (parent != null) {
            parent.setCurrentAlgorithmName("Bellman-Ford Shortest Path");
        }

        AlgorithmCode code = CodeRepository.getCode("Bellman-Ford Shortest Path");
        if (code instanceof BellmanFordCode) {
            ((BellmanFordCode) code).updateParameters(nodeCount, edges, source);
        }
    }
}
