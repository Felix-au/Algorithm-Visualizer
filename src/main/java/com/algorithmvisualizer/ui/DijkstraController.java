package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.DijkstraSolver;
import com.algorithmvisualizer.model.Algorithm;
import com.algorithmvisualizer.model.DijkstraAlgorithm;
import com.algorithmvisualizer.visualization.GraphRenderer;
import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.code.implementations.DijkstraCode;
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

public class DijkstraController implements AlgorithmViewController.AlgorithmSpecificController {
    
    private AlgorithmViewController parent;
    private DijkstraAlgorithm algorithm;
    private DijkstraSolver solver;
    private GraphRenderer graphView;
    
    // Parameters
    private int nodeCount = 6;
    private int source = 0;  // Always 0
    private Integer target = null;  // Always null (all targets)
    private int[][] edges;
    
    // UI Components
    private TableView<DistanceRow> distanceTable;
    private ObservableList<DistanceRow> distanceData;
    
    // State
    private Stack<DijkstraSolver.State> history;
    private Timeline timeline;
    private int currentStep = 0;
    private int previousCurrentVertex = -1;  // Track previous current vertex for highlight removal
    
    // Colors
    private static final Color COLOR_UNVISITED = Color.LIGHTGRAY;
    private static final Color COLOR_CURRENT = Color.GOLD;
    private static final Color COLOR_FRONTIER = Color.CYAN;
    private static final Color COLOR_VISITED = Color.LIGHTGREEN;
    private static final Color COLOR_PATH = Color.rgb(50, 205, 50);
    private static final Color COLOR_CONSIDERING = Color.ORANGE;
    private static final Color COLOR_REJECTED = Color.RED;
    
    // Multi-path animation state
    private List<List<Integer>> allPaths;  // All paths from source to each target
    private int currentPathIndex = 0;  // Which path we're animating
    private int currentPathStepIndex = 0;  // Which step in current path
    
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
        
        public int getVertex() { return vertex; }
        public String getDistance() { return distance; }
        public String getPrevious() { return previous; }
        public String getStatus() { return status; }
        
        public void setDistance(String distance) { this.distance = distance; }
        public void setPrevious(String previous) { this.previous = previous; }
        public void setStatus(String status) { this.status = status; }
    }
    
    @FXML
    private void initialize() {
        graphView = new GraphRenderer();
        graphView.showEdgeWeights(true);
        
        distanceTable = buildDistanceTable();
        
        initDefaultGraph();
        renderGraph();
        solver = new DijkstraSolver();
        solver.setGraph(nodeCount, edges, source, target);
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
            Label hdr = new Label("Dijkstra's Shortest Path");
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
            
            javafx.scene.shape.Circle unvisited = new javafx.scene.shape.Circle(8, COLOR_UNVISITED);
            HBox lUnvisited = new HBox(5.0, unvisited, new Label("Unvisited"));
            
            javafx.scene.shape.Circle current = new javafx.scene.shape.Circle(8, COLOR_CURRENT);
            HBox lCurrent = new HBox(5.0, current, new Label("Current"));
            
            javafx.scene.shape.Circle frontier = new javafx.scene.shape.Circle(8, COLOR_FRONTIER);
            HBox lFrontier = new HBox(5.0, frontier, new Label("Frontier"));
            
            javafx.scene.shape.Circle visited = new javafx.scene.shape.Circle(8, COLOR_VISITED);
            HBox lVisited = new HBox(5.0, visited, new Label("Visited"));
            
            javafx.scene.shape.Circle pathNode = new javafx.scene.shape.Circle(8, COLOR_PATH);
            HBox lPath = new HBox(5.0, pathNode, new Label("Shortest Path"));
            
            row.getChildren().addAll(lUnvisited, lCurrent, lFrontier, lVisited, lPath);
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
        parent.setCurrentAlgorithmName("Dijkstra's Shortest Path");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) {
            parent.stepDescription.setText("Ready.");
        }
    }
    
    private void setupParameterControls() {
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Nodes:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 12, nodeCount)
            );
            // Auto-apply on node count change
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> {
                nodeCount = n;
                onNodeCountChanged();
            });
        }
        
        // Replace source/target field with edges field
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
            parent.paramElementsField.setPromptText("e.g. 0-1:4, 1-2:3, 2-3:5");
            parent.paramElementsField.setText(edgesToString(edges));
        }
        
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setOnAction(e -> onRandomizeGraph());
            parent.paramRandomizeButton.setStyle(
                "-fx-background-color: #0ea5e9; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;"
            );
        }
        
        if (parent.paramApplyButton != null) {
            parent.paramApplyButton.setOnAction(e -> onApplyGraph());
            parent.paramApplyButton.setStyle(
                "-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;"
            );
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
                    case "Current":
                        dot.setFill(COLOR_CURRENT);
                        break;
                    case "Frontier":
                        dot.setFill(COLOR_FRONTIER);
                        break;
                    case "Visited":
                        dot.setFill(COLOR_VISITED);
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
        // Create a default connected graph
        edges = new int[][] {
            {0, 1, 4}, {0, 2, 2},
            {1, 2, 1}, {1, 3, 5},
            {2, 3, 8}, {2, 4, 10},
            {3, 4, 2}, {3, 5, 6},
            {4, 5, 3}
        };
    }
    
    private void onNodeCountChanged() {
        // Auto-regenerate graph when node count changes
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
        // Parse edges from text field
        if (parent.paramElementsField != null) {
            String text = parent.paramElementsField.getText();
            int[][] parsedEdges = parseEdges(text, nodeCount);
            if (parsedEdges.length > 0) {
                edges = parsedEdges;
            }
        }
        // Source is always 0, target is always null (all)
        source = 0;
        target = null;
        refreshAll();
    }
    
    private int[][] generateRandomGraph(int n) {
        Random rand = new Random();
        // Use LinkedHashMap to prevent duplicate edges
        Map<String, int[]> edgeMap = new LinkedHashMap<>();
        
        // Create connected graph using spanning tree first
        for (int i = 1; i < n; i++) {
            int parent = rand.nextInt(i);
            int weight = rand.nextInt(15) + 1;
            String key = parent < i ? parent + "-" + i : i + "-" + parent;
            edgeMap.put(key, new int[]{parent, i, weight});
        }
        
        // Add additional random edges (avoid duplicates)
        int additionalEdges = n + rand.nextInt(n);
        int attempts = 0;
        int maxAttempts = additionalEdges * 3; // Prevent infinite loop
        
        while (edgeMap.size() < (n - 1) + additionalEdges && attempts < maxAttempts) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);
            if (u != v) {
                String key = u < v ? u + "-" + v : v + "-" + u;
                if (!edgeMap.containsKey(key)) {
                    int weight = rand.nextInt(15) + 1;
                    edgeMap.put(key, new int[]{u, v, weight});
                }
            }
            attempts++;
        }
        
        return edgeMap.values().toArray(new int[0][]);
    }
    
    private static int[][] parseEdges(String text, int n) {
        if (text == null || text.trim().isEmpty()) return new int[0][];
        
        // Use LinkedHashMap to track edges and prevent duplicates
        Map<String, int[]> edgeMap = new LinkedHashMap<>();
        String[] parts = text.split(",");
        
        try {
            for (String p : parts) {
                String s = p.trim();
                if (s.isEmpty()) continue;
                
                // Format: u-v:w
                String[] edgeWeight = s.split(":");
                if (edgeWeight.length != 2) continue;
                
                String[] uv = edgeWeight[0].split("-");
                if (uv.length != 2) continue;
                
                int u = Integer.parseInt(uv[0].trim());
                int v = Integer.parseInt(uv[1].trim());
                int w = Integer.parseInt(edgeWeight[1].trim());
                
                if (u < 0 || v < 0 || u >= n || v >= n || u == v) continue;
                if (w < 1) continue;
                
                // Create normalized key to prevent duplicates (e.g., 0-1 and 1-0 are same edge)
                String key = u < v ? u + "-" + v : v + "-" + u;
                
                // Store edge (if duplicate, last one wins - same as renderGraph behavior)
                edgeMap.put(key, new int[]{u, v, w});
            }
            return edgeMap.values().toArray(new int[0][]);
        } catch (Exception ex) {
            return new int[0][];
        }
    }
    
    private static String edgesToString(int[][] edges) {
        if (edges == null || edges.length == 0) return "";
        
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
        solver = new DijkstraSolver();
        solver.setGraph(nodeCount, edges, source, target);
        solver.setStepListener(this::onStepEvent);
        solver.reset();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        updateDistanceTable();
    }
    
    private void renderGraph() {
        // Build adjacency list and weights map
        // IMPORTANT: Use LinkedHashMap to preserve insertion order for debugging
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
            adj.get(v).add(u); // Undirected
            String key = u < v ? u + "-" + v : v + "-" + u;
            
            // If duplicate edge exists, keep the one from edges array (last wins)
            // This ensures UI and solver see the same weight
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
        boolean[] visited = solver.getVisited();
        
        for (int i = 0; i < nodeCount; i++) {
            String distStr = formatDistance(dist[i]);
            String prevStr = prev[i] == -1 ? "-" : String.valueOf(prev[i]);
            String status = visited[i] ? "Visited" : "Unvisited";
            
            distanceData.add(new DistanceRow(i, distStr, prevStr, status));
        }
    }
    
    // Helper method to format distance (show ∞ instead of Integer.MAX_VALUE)
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
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(true);
            parent.pauseButton.setManaged(true);
            parent.pauseButton.setDisable(false);  // Ensure pause button is clickable
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
            DijkstraSolver.State prevState = history.pop();
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
        long delayMs = (long)(1000.0 / speed);
        
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
    
    private void onStepEvent(DijkstraSolver.StepType type, int u, int v, int distance, Map<String, Object> meta) {
        int edgeWeight;  // Declare once for entire method
        
        switch (type) {
            case INIT:
                appendProgress("🚀 Starting Dijkstra's algorithm...");
                appendProgress("Source vertex: " + source);
                if (target != null) {
                    appendProgress("Target vertex: " + target);
                }
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
                
            case SELECT_CURRENT:
                // Remove glow from previous current vertex
                if (previousCurrentVertex != -1) {
                    graphView.removeGlow(previousCurrentVertex);
                }
                
                appendProgress("🎯 Selected vertex " + u + " (distance: " + formatDistance((Integer) meta.get("distance")) + ")");
                graphView.setNodeColor(u, COLOR_CURRENT);
                graphView.glowVertex(u, COLOR_CURRENT);
                updateDistanceTableRow(u, "Current");
                
                previousCurrentVertex = u;  // Track for next iteration
                break;
                
            case MARK_VISITED:
                appendProgress("✓ Marked vertex " + u + " as visited");
                graphView.removeGlow(u);
                graphView.setNodeColor(u, COLOR_VISITED);
                updateDistanceTableRow(u, "Visited");
                break;
                
            case EXPLORE_NEIGHBOR_START:
                if ((Boolean) meta.get("visited")) {
                    appendProgress("  → Neighbor " + v + " already visited, skipping");
                } else {
                    appendProgress("  → Exploring neighbor " + v + " (edge weight: " + meta.get("edgeWeight") + ")");
                    graphView.highlightEdgeWithColor(u, v, COLOR_CONSIDERING, 3.0);
                    graphView.blinkEdge(u, v, COLOR_CONSIDERING, 1);
                }
                break;
                
            case CALCULATE_DISTANCE:
                appendProgress("    Calculating: " + meta.get("currentDist") + " + " + 
                             meta.get("edgeWeight") + " = " + meta.get("tentativeDist"));
                break;
                
            case COMPARE_DISTANCES:
                boolean improved = (Boolean) meta.get("improved");
                if (improved) {
                    appendProgress("    ✓ " + formatDistance((Integer) meta.get("newDist")) + " < " + 
                                 formatDistance((Integer) meta.get("oldDist")) + " - Better path found!");
                } else {
                    appendProgress("    ✗ " + formatDistance((Integer) meta.get("newDist")) + " >= " + 
                                 formatDistance((Integer) meta.get("oldDist")) + " - No improvement");
                }
                break;
                
            case UPDATE_DISTANCE:
                appendProgress("    ✅ Updated distance to " + v + ": " + meta.get("newDist"));
                graphView.pulseEdge(u, v, COLOR_VISITED);
                graphView.highlightEdgeWithColor(u, v, COLOR_VISITED, 2.0);
                // Mark neighbor as in frontier if not yet visited
                boolean[] vis = solver.getVisited();
                if (v < vis.length && !vis[v]) {
                    graphView.setNodeColor(v, COLOR_FRONTIER);
                }
                updateDistanceTable();
                break;
                
            case REJECT_DISTANCE:
                appendProgress("    ❌ Keeping existing distance");
                graphView.shakeEdge(u, v);
                graphView.highlightEdgeWithColor(u, v, COLOR_REJECTED, 2.0);
                break;
                
            case EXPLORE_NEIGHBOR_END:
                // Reset edge color
                graphView.highlightEdgeWithColor(u, v, Color.LIGHTGRAY, 1.0);
                break;
                
            case SCAN_UNVISITED:
                appendProgress("🔍 Scanning for next unvisited vertex...");
                break;
                
            case PATH_RECONSTRUCTION:
                appendProgress("🛤️ Reconstructing shortest path...");
                break;
                
            case PATH_ANIMATION_START:
                // Clear all previous highlights
                for (int i = 0; i < nodeCount; i++) {
                    graphView.setNodeColor(i, COLOR_UNVISITED);
                    graphView.removeGlow(i);
                }
                // Reset all edges to default
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
                targetVert = (Integer) meta.get("targetVertex");
                
                appendProgress("  📍 Node " + u + " (step " + (nodeIdx + 1) + "/" + totalNodes + ")");
                graphView.setNodeColor(u, COLOR_PATH);
                graphView.glowVertex(u, COLOR_PATH);
                break;
                
            case PATH_ANIMATION_EDGE:
                int edgeIdx = (Integer) meta.get("edgeIndex");
                int totalEdges = (Integer) meta.get("totalEdges");
                targetVert = (Integer) meta.get("targetVertex");
                
                // Find edge weight
                edgeWeight = 0;
                for (int[] edge : edges) {
                    if ((edge[0] == u && edge[1] == v) || (edge[0] == v && edge[1] == u)) {
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
                
                // Generate rich summary for this path
                List<List<Integer>> allPaths = solver.getAllPaths();
                if (allPaths != null && pathNum - 1 < allPaths.size()) {
                    List<Integer> path = allPaths.get(pathNum - 1);
                    
                    appendProgress("");
                    appendProgress("📊 SHORTEST PATH SUMMARY:");
                    appendProgress("");
                    appendProgress("Source: 0");
                    appendProgress("Target: " + targetVert);
                    appendProgress("Total Distance: " + formatDistance(pathDist));
                    appendProgress("");
                    appendProgress("🛤️ PATH DETAILS:");
                    
                    for (int i = 0; i < path.size() - 1; i++) {
                        int from = path.get(i);
                        int to = path.get(i + 1);
                        
                        // Find edge weight
                        edgeWeight = 0;
                        for (int[] edge : edges) {
                            if ((edge[0] == from && edge[1] == to) || (edge[0] == to && edge[1] == from)) {
                                edgeWeight = edge[2];
                                break;
                            }
                        }
                        
                        int[] dist = solver.getDistances();
                        appendProgress(from + " → " + to + " (distance: " + edgeWeight + ")");
                    }
                    
                    appendProgress("");
                    appendProgress("📈 STATISTICS:");
                    int visitedCount = 0;
                    for (boolean visited : solver.getVisited()) {
                        if (visited) visitedCount++;
                    }
                    appendProgress("Vertices Visited: " + visitedCount + "/" + nodeCount);
                    appendProgress("Total Edges Explored: " + edges.length);
                    appendProgress("Path Length: " + (path.size() - 1) + " edges");
                    appendProgress("");
                    appendProgress("═".repeat(50));
                }
                break;
                
            case ALL_PATHS_COMPLETE:
                appendProgress("\n" + "═".repeat(50));
                appendProgress("✅ ALGORITHM COMPLETE");
                appendProgress("═".repeat(50));
                appendProgress("");
                appendProgress("All shortest paths from source 0 have been computed and visualized.");
                appendProgress("");
                appendProgress("═".repeat(50));
                break;
                
            case PATH_HIGHLIGHT_STEP:
                if (v != -1) {
                    int pathIdx = (Integer) meta.get("pathIndex");
                    int pathLen = (Integer) meta.get("pathLength");
                    
                    appendProgress("🛤️ Path step " + (pathIdx + 1) + "/" + pathLen + ": vertex " + u + " → " + v);
                    
                    // Highlight edge with dark green
                    graphView.highlightEdgeWithColor(u, v, COLOR_PATH, 4.0);
                    graphView.pulseEdge(u, v, COLOR_PATH);
                    
                    // Set node colors to dark green
                    graphView.setNodeColor(u, COLOR_PATH);
                    graphView.setNodeColor(v, COLOR_PATH);
                    
                    // Glow current node
                    graphView.glowVertex(v, COLOR_PATH);
                } else {
                    // Last node in path
                    appendProgress("🛤️ Path complete at vertex " + u);
                    graphView.setNodeColor(u, COLOR_PATH);
                    graphView.glowVertex(u, COLOR_PATH);
                }
                break;
                
            case COMPLETE:
                if (target != null && meta.containsKey("totalDistance")) {
                    int totalDist = (Integer) meta.get("totalDistance");
                    List<Integer> path = solver.getShortestPath();
                    
                    appendProgress("\n" + "═".repeat(50));
                    appendProgress("✅ ALGORITHM COMPLETE!");
                    appendProgress("═".repeat(50));
                    appendProgress("");
                    appendProgress("📍 Source: " + source + " → Target: " + target);
                    appendProgress("📏 Total Distance: " + formatDistance(totalDist));
                    appendProgress("");
                    
                    if (path != null && !path.isEmpty()) {
                        appendProgress("🛤️ SHORTEST PATH:");
                        appendProgress("   Path: " + path.toString());
                        appendProgress("");
                        appendProgress("   Step-by-step breakdown:");
                        
                        int[] dist = solver.getDistances();
                        for (int i = 0; i < path.size() - 1; i++) {
                            int from = path.get(i);
                            int to = path.get(i + 1);
                            
                            // Find edge weight
                            edgeWeight = 0;
                            for (int[] edge : edges) {
                                if ((edge[0] == from && edge[1] == to) || (edge[0] == to && edge[1] == from)) {
                                    edgeWeight = edge[2];
                                    break;
                                }
                            }
                            
                            appendProgress("   " + (i + 1) + ". Vertex " + from + " → " + to + 
                                         " (weight: " + edgeWeight + ", cumulative: " + formatDistance(dist[to]) + ")");
                        }
                        
                        appendProgress("");
                        appendProgress("📊 STATISTICS:");
                        int visitedCount = 0;
                        for (boolean visited : solver.getVisited()) {
                            if (visited) visitedCount++;
                        }
                        appendProgress("   • Vertices visited: " + visitedCount + "/" + nodeCount);
                        appendProgress("   • Edges explored: " + edges.length);
                        appendProgress("   • Path length: " + path.size() + " vertices");
                    }
                    
                    appendProgress("");
                    appendProgress("═".repeat(50));
                } else {
                    appendProgress("\n✅ Algorithm complete!");
                    appendProgress("All shortest distances computed from source " + source);
                }
                
                // Highlight final path
                if (solver.getShortestPath() != null) {
                    List<Integer> path = solver.getShortestPath();
                    for (int i = 0; i < path.size() - 1; i++) {
                        graphView.highlightEdgeWithColor(path.get(i), path.get(i + 1), COLOR_PATH, 4.0);
                    }
                }
                break;
        }
        
        updateVariablesPanel();
    }
    
    private void visualizeState(DijkstraSolver.State s) {
        // Restore visual state from snapshot
        for (int i = 0; i < nodeCount; i++) {
            if (s.visited[i]) {
                graphView.setNodeColor(i, COLOR_VISITED);
            } else {
                graphView.setNodeColor(i, COLOR_UNVISITED);
            }
        }
    }
    
    private void updateDistanceTableRow(int vertex, String status) {
        for (DistanceRow row : distanceData) {
            if (row.getVertex() == vertex) {
                row.setStatus(status);
                distanceTable.refresh();
                break;
            }
        }
    }
    
    private void initProgressLog() {
        if (parent != null && parent.progressArea != null) {
            parent.progressArea.clear();
            appendProgress("=== Dijkstra's Shortest Path Algorithm ===");
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
            if (target != null) {
                vars.add("Target: " + target);
                int[] dist = solver.getDistances();
                if (dist[target] != Integer.MAX_VALUE) {
                    vars.add("Distance to target: " + dist[target]);
                }
            }
            
            int visitedCount = 0;
            for (boolean v : solver.getVisited()) {
                if (v) visitedCount++;
            }
            vars.add("Visited: " + visitedCount + "/" + nodeCount);
            
            parent.variableList.setItems(vars);
        }
    }
    
    private void renderCode() {
        // Refresh code display by calling setCurrentAlgorithmName
        if (parent != null) {
            parent.setCurrentAlgorithmName("Dijkstra's Shortest Path");
        }
        
        AlgorithmCode code = CodeRepository.getCode("Dijkstra's Shortest Path");
        if (code instanceof DijkstraCode) {
            ((DijkstraCode) code).updateParameters(nodeCount, edges, source, target);
        }
    }
}
