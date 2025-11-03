package com.algorithmvisualizer.visualization;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.StrokeTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

/**
 * Renders an undirected graph with nodes arranged on a circle or tree layout.
 * Provides helpers to highlight current node, visited nodes, and edges.
 */
public class GraphRenderer {

    public enum LayoutMode { CIRCULAR, TREE }

    private final Pane container;
    private final Map<Integer, Circle> nodeCircles = new HashMap<>();
    private final Map<Integer, Text> nodeLabels = new HashMap<>();
    private final Map<String, Line> edgeLines = new HashMap<>();
    private final Map<String, Boolean> activePath = new HashMap<>();
    private int nodeCount = 0;
    private List<List<Integer>> adjacency = new ArrayList<>();
    private LayoutMode layoutMode = LayoutMode.CIRCULAR;

    public GraphRenderer() {
        container = new Pane();
        container.setMinSize(300, 300);
        container.setPrefSize(600, 500);
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        container.layoutBoundsProperty().addListener((obs, o, n) -> rebuildPositions());
    }

    public Node getNode() { return container; }

    public void setGraph(int n, List<List<Integer>> adj) {
        nodeCount = Math.max(0, n);
        this.adjacency = adj != null ? copyAdj(adj) : new ArrayList<>();
        container.getChildren().clear();
        nodeCircles.clear();
        nodeLabels.clear();
        edgeLines.clear();
        activePath.clear();

        // Pre-create nodes
        for (int i = 0; i < nodeCount; i++) {
            Circle c = new Circle(16.0, Color.STEELBLUE);
            c.setStroke(Color.BLACK);
            nodeCircles.put(i, c);
            Text t = new Text(String.valueOf(i));
            t.setFill(Color.WHITE);
            t.setStyle("-fx-font-weight: bold;");
            nodeLabels.put(i, t);
            container.getChildren().addAll(c, t);
        }

        // Create edges (undirected, avoid duplicates by i<j)
        if (adj != null) {
            for (int i = 0; i < adj.size(); i++) {
                for (int j : adj.get(i)) {
                    if (i < j) {
                        String key = edgeKey(i, j);
                        if (!edgeLines.containsKey(key)) {
                            Line line = new Line();
                            line.setStroke(Color.GRAY);
                            line.setStrokeWidth(2.0);
                            edgeLines.put(key, line);
                            container.getChildren().add(0, line); // draw edges behind nodes
                        }
                    }
                }
            }
        }
        rebuildPositions();
    }

    public void clearHighlights() {
        for (Map.Entry<Integer, Circle> e : nodeCircles.entrySet()) {
            Circle c = e.getValue();
            // Do not override visited (FORESTGREEN)
            if (c.getFill() != Color.FORESTGREEN) {
                c.setFill(Color.STEELBLUE);
            }
        }
        for (Line l : edgeLines.values()) {
            l.setStroke(Color.GRAY);
            l.setStrokeWidth(2.0);
        }
        // keep activePath state but visual reset to baseline until setActivePathEdge called again
    }

    public void highlightCurrent(int v) {
        Circle c = nodeCircles.get(v);
        if (c != null && c.getFill() != Color.FORESTGREEN) {
            c.setFill(Color.GOLD);
        }
    }

    public void markVisited(int v) {
        Circle c = nodeCircles.get(v);
        if (c != null) {
            c.setFill(Color.FORESTGREEN);
        }
    }

    public void highlightEdge(int u, int v) {
        Line l = edgeLines.get(edgeKey(u, v));
        if (l == null) l = edgeLines.get(edgeKey(v, u));
        if (l != null) {
            l.setStroke(Color.DARKORANGE);
        }
    }

    /**
     * Mark or unmark an edge as part of the active DFS path (stack). Active edges are thicker and blue.
     */
    public void setActivePathEdge(int u, int v, boolean active) {
        String key = edgeKey(u, v);
        Line l = edgeLines.get(key);
        if (l == null) l = edgeLines.get(edgeKey(v, u));
        if (l == null) return;
        activePath.put(edgeKey(Math.min(u, v), Math.max(u, v)), active);
        if (active) {
            l.setStroke(Color.CORNFLOWERBLUE);
            l.setStrokeWidth(4.0);
        } else {
            l.setStroke(Color.GRAY);
            l.setStrokeWidth(2.0);
        }
    }

    /**
     * Animate unwinding of an active edge during backtracking.
     */
    public void animateBacktrackEdge(int u, int v) {
        Line l = edgeLines.get(edgeKey(u, v));
        if (l == null) l = edgeLines.get(edgeKey(v, u));
        if (l == null) return;
        Color from = (l.getStroke() instanceof Color) ? (Color) l.getStroke() : Color.CORNFLOWERBLUE;
        StrokeTransition color = new StrokeTransition(Duration.millis(220), l, from, Color.GRAY);
        Timeline widthShrink = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(l.strokeWidthProperty(), l.getStrokeWidth())),
                new KeyFrame(Duration.millis(220), new KeyValue(l.strokeWidthProperty(), 2.0))
        );
        ParallelTransition pt = new ParallelTransition(color, widthShrink);
        pt.play();
    }

    /**
     * Brief pulse on the node to indicate backtracking from it.
     */
    public void flashBacktrackNode(int v) {
        Circle c = nodeCircles.get(v);
        if (c == null) return;
        Circle ring = new Circle(c.getCenterX(), c.getCenterY(), c.getRadius());
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.CRIMSON);
        ring.setStrokeWidth(3.0);
        container.getChildren().add(ring);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(ring.opacityProperty(), 1.0),
                        new KeyValue(ring.radiusProperty(), c.getRadius())
                ),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(ring.opacityProperty(), 0.0),
                        new KeyValue(ring.radiusProperty(), c.getRadius() + 16)
                )
        );
        tl.setOnFinished(e -> container.getChildren().remove(ring));
        tl.play();
    }

    /**
     * Set the layout mode (CIRCULAR or TREE) and rebuild positions
     */
    public void setLayoutMode(LayoutMode mode) {
        this.layoutMode = mode;
        rebuildPositions();
    }

    public LayoutMode getLayoutMode() {
        return layoutMode;
    }

    private void rebuildPositions() {
        Map<Integer, double[]> pos = (layoutMode == LayoutMode.TREE) 
            ? computeTreeLayout() 
            : computeCircularLayout();
        
        // Update node positions
        for (int i = 0; i < nodeCount; i++) {
            double[] p = pos.get(i);
            if (p == null) continue;
            Circle c = nodeCircles.get(i);
            Text t = nodeLabels.get(i);
            if (c != null) {
                c.setCenterX(p[0]);
                c.setCenterY(p[1]);
            }
            if (t != null) {
                t.setX(p[0] - 4);
                t.setY(p[1] + 4);
            }
        }
        
        // Update edges
        for (Map.Entry<String, Line> e : edgeLines.entrySet()) {
            String[] uv = e.getKey().split("-");
            int u = Integer.parseInt(uv[0]);
            int v = Integer.parseInt(uv[1]);
            double[] pu = pos.get(u);
            double[] pv = pos.get(v);
            if (pu != null && pv != null) {
                Line l = e.getValue();
                l.setStartX(pu[0]);
                l.setStartY(pu[1]);
                l.setEndX(pv[0]);
                l.setEndY(pv[1]);
            }
        }
    }

    private Map<Integer, double[]> computeCircularLayout() {
        double w = Math.max(300, container.getWidth());
        double h = Math.max(300, container.getHeight());
        double cx = w / 2.0;
        double cy = h / 2.0;
        double radius = Math.max(100, Math.min(w, h) / 2.5);
        
        Map<Integer, double[]> pos = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            double angle = 2 * Math.PI * i / Math.max(1, nodeCount);
            double x = cx + radius * Math.cos(angle);
            double y = cy + radius * Math.sin(angle);
            pos.put(i, new double[]{x, y});
        }
        return pos;
    }

    private Map<Integer, double[]> computeTreeLayout() {
        double w = Math.max(300, container.getWidth());
        double h = Math.max(300, container.getHeight());
        
        Map<Integer, double[]> pos = new HashMap<>();
        if (nodeCount == 0) return pos;
        
        // Build tree structure using BFS from node 0
        boolean[] visited = new boolean[nodeCount];
        Map<Integer, Integer> parent = new HashMap<>();
        Map<Integer, List<Integer>> children = new HashMap<>();
        Map<Integer, Integer> depth = new HashMap<>();
        
        for (int i = 0; i < nodeCount; i++) {
            children.put(i, new ArrayList<>());
        }
        
        // BFS to build tree
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(0);
        visited[0] = true;
        depth.put(0, 0);
        int maxDepth = 0;
        
        while (!queue.isEmpty()) {
            int u = queue.poll();
            int d = depth.get(u);
            maxDepth = Math.max(maxDepth, d);
            
            if (adjacency != null && u < adjacency.size()) {
                for (int v : adjacency.get(u)) {
                    if (!visited[v]) {
                        visited[v] = true;
                        parent.put(v, u);
                        children.get(u).add(v);
                        depth.put(v, d + 1);
                        queue.offer(v);
                    }
                }
            }
        }
        
        // Handle disconnected components
        for (int i = 0; i < nodeCount; i++) {
            if (!visited[i]) {
                visited[i] = true;
                depth.put(i, maxDepth + 1);
            }
        }
        maxDepth = Math.max(maxDepth, depth.values().stream().max(Integer::compare).orElse(0));
        
        // Compute positions using recursive layout
        double verticalSpacing = Math.min(80, h / Math.max(1, maxDepth + 2));
        Map<Integer, Integer> subtreeSize = new HashMap<>();
        computeSubtreeSize(0, children, subtreeSize);
        
        layoutTree(0, children, depth, subtreeSize, pos, 0, w, verticalSpacing, 40);
        
        // Position disconnected nodes
        double disconnectedY = h - 40;
        double disconnectedX = 40;
        for (int i = 0; i < nodeCount; i++) {
            if (!pos.containsKey(i)) {
                pos.put(i, new double[]{disconnectedX, disconnectedY});
                disconnectedX += 60;
                if (disconnectedX > w - 40) {
                    disconnectedX = 40;
                    disconnectedY -= 60;
                }
            }
        }
        
        return pos;
    }

    private int computeSubtreeSize(int node, Map<Integer, List<Integer>> children, Map<Integer, Integer> subtreeSize) {
        int size = 1;
        for (int child : children.get(node)) {
            size += computeSubtreeSize(child, children, subtreeSize);
        }
        subtreeSize.put(node, size);
        return size;
    }

    private void layoutTree(int node, Map<Integer, List<Integer>> children, Map<Integer, Integer> depth,
                           Map<Integer, Integer> subtreeSize, Map<Integer, double[]> pos,
                           double leftX, double rightX, double verticalSpacing, double topMargin) {
        int d = depth.getOrDefault(node, 0);
        double y = topMargin + d * verticalSpacing;
        double x = (leftX + rightX) / 2.0;
        pos.put(node, new double[]{x, y});
        
        List<Integer> kids = children.get(node);
        if (kids.isEmpty()) return;
        
        // Distribute children horizontally based on subtree sizes
        double currentX = leftX;
        double totalWidth = rightX - leftX;
        int totalSize = subtreeSize.get(node) - 1; // exclude current node
        
        for (int child : kids) {
            int childSize = subtreeSize.get(child);
            double childWidth = totalSize > 0 ? (childSize * totalWidth / totalSize) : (totalWidth / kids.size());
            layoutTree(child, children, depth, subtreeSize, pos, currentX, currentX + childWidth, verticalSpacing, topMargin);
            currentX += childWidth;
        }
    }

    private List<List<Integer>> copyAdj(List<List<Integer>> src) {
        List<List<Integer>> out = new ArrayList<>();
        for (List<Integer> row : src) {
            out.add(new ArrayList<>(row));
        }
        return out;
    }

    private String edgeKey(int u, int v) {
        if (u > v) { int tmp = u; u = v; v = tmp; }
        return u + "-" + v;
    }
}
