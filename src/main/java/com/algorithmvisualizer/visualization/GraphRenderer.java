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
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

/**
 * Renders a graph with nodes arranged on a circle or tree layout.
 * Supports both undirected and directed modes.
 * Provides helpers to highlight current node, visited nodes, and edges.
 */
public class GraphRenderer {

    public enum LayoutMode {
        CIRCULAR, TREE
    }

    private final Pane container;
    private final Map<Integer, Circle> nodeCircles = new HashMap<>();
    private final Map<Integer, Text> nodeLabels = new HashMap<>();
    private final Map<String, Line> edgeLines = new HashMap<>();
    private final Map<String, Boolean> activePath = new HashMap<>();
    private final Set<Integer> errorNodes = new HashSet<>();
    private int nodeCount = 0;
    private List<List<Integer>> adjacency = new ArrayList<>();
    private LayoutMode layoutMode = LayoutMode.CIRCULAR;
    private List<String> customLabels = new ArrayList<>();
    // Configurables (defaults keep legacy behavior)
    private double nodeRadius = 16.0; // default size
    private boolean treeLeafSpacingEnabled = false;
    private double treeMinLeafSpacing = 90.0;

    // Weighted edge support
    private final Map<String, Integer> edgeWeights = new HashMap<>();
    private final Map<String, Text> edgeWeightLabels = new HashMap<>();
    private boolean showWeights = false;

    // Directed edge support
    private boolean directed = false;
    private final Map<String, Polygon> arrowHeads = new HashMap<>();
    private final Map<String, javafx.scene.shape.Rectangle> weightBgRects = new HashMap<>();
    // Colors for forward/reverse directed edges
    public static final Color DIRECTED_FORWARD_COLOR = Color.web("#22c55e"); // green
    public static final Color DIRECTED_REVERSE_COLOR = Color.web("#3b82f6"); // blue

    // Animation tracking
    private final Map<javafx.scene.Node, javafx.animation.Animation> activeAnimations = new HashMap<>();

    private void cancelAnimation(javafx.scene.Node node) {
        javafx.animation.Animation anim = activeAnimations.remove(node);
        if (anim != null)
            anim.stop();
    }

    private void registerAnimation(javafx.scene.Node node, javafx.animation.Animation anim) {
        cancelAnimation(node);
        activeAnimations.put(node, anim);
        anim.setOnFinished(e -> activeAnimations.remove(node));
    }

    public GraphRenderer() {
        container = new Pane();
        container.setMinSize(300, 300);
        container.setPrefSize(600, 500);
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        container.layoutBoundsProperty().addListener((obs, o, n) -> rebuildPositions());
    }

    public Node getNode() {
        return container;
    }

    /** Configure node radius (affects future setGraph calls). */
    public void setNodeRadius(double r) {
        this.nodeRadius = Math.max(8.0, r);
        rebuildPositions();
    }

    /** Enable/disable extra spacing for deeper tree leaves. */
    public void setTreeLeafSpacingEnabled(boolean enabled) {
        this.treeLeafSpacingEnabled = enabled;
        rebuildPositions();
    }

    /** Set minimum center-to-center spacing for deep leaves when enabled. */
    public void setTreeMinLeafSpacing(double px) {
        this.treeMinLeafSpacing = Math.max(40.0, px);
        rebuildPositions();
    }

    /**
     * Set custom labels for nodes (e.g., "T(3,Aâ†’C)" for Tower of Hanoi).
     * If not set or index out of bounds, defaults to numeric ID.
     */
    public void setNodeLabels(List<String> labels) {
        this.customLabels = labels != null ? new ArrayList<>(labels) : new ArrayList<>();
        // Update existing labels
        for (int i = 0; i < nodeCount; i++) {
            Text t = nodeLabels.get(i);
            if (t != null) {
                String label = (i < customLabels.size() && customLabels.get(i) != null)
                        ? customLabels.get(i)
                        : String.valueOf(i);
                t.setText(label);
            }
        }
    }

    /**
     * Enable or disable directed mode.
     * In directed mode, edges uâ†’v and vâ†’u are drawn as separate offset lines
     * with
     * arrowheads.
     */
    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public boolean isDirected() {
        return directed;
    }

    /**
     * Directed edge key preserving direction: "u->v".
     */
    private String directedEdgeKey(int u, int v) {
        return u + "->" + v;
    }

    public void setGraph(int n, List<List<Integer>> adj) {
        nodeCount = Math.max(0, n);
        this.adjacency = adj != null ? copyAdj(adj) : new ArrayList<>();
        container.getChildren().clear();
        nodeCircles.clear();
        nodeLabels.clear();
        edgeLines.clear();
        edgeWeightLabels.clear();
        arrowHeads.clear();
        weightBgRects.clear();
        activePath.clear();

        // Pre-create nodes
        for (int i = 0; i < nodeCount; i++) {
            Circle c = new Circle(nodeRadius, Color.STEELBLUE);
            c.setStroke(Color.BLACK);
            c.setStrokeWidth(1.5);
            nodeCircles.put(i, c);
            String label = (i < customLabels.size() && customLabels.get(i) != null)
                    ? customLabels.get(i)
                    : String.valueOf(i);
            Text t = new Text(label);
            t.setFill(Color.WHITE);
            t.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
            nodeLabels.put(i, t);
            container.getChildren().addAll(c, t);
        }
        // Reapply error styling to any remembered error nodes
        for (Integer id : errorNodes) {
            Circle c = nodeCircles.get(id);
            if (c != null)
                c.setFill(Color.CRIMSON);
        }

        if (directed) {
            // Directed mode: create a separate Line for each directed edge
            if (adj != null) {
                // First, detect which node pairs have edges in both directions
                Set<String> biEdges = new HashSet<>();
                for (int i = 0; i < adj.size(); i++) {
                    for (int j : adj.get(i)) {
                        if (i != j && adj.get(j).contains(i)) {
                            biEdges.add(Math.min(i, j) + "-" + Math.max(i, j));
                        }
                    }
                }

                for (int i = 0; i < adj.size(); i++) {
                    for (int j : adj.get(i)) {
                        String dKey = directedEdgeKey(i, j);
                        if (!edgeLines.containsKey(dKey)) {
                            Line line = new Line();
                            line.setStroke(Color.LIGHTGRAY);
                            line.setStrokeWidth(2.0);
                            edgeLines.put(dKey, line);
                            container.getChildren().add(0, line);

                            // Create arrowhead polygon
                            Polygon arrow = new Polygon();
                            Color directionalColor = (i < j) ? DIRECTED_FORWARD_COLOR : DIRECTED_REVERSE_COLOR;
                            arrow.setFill(directionalColor);
                            arrow.setStroke(directionalColor);
                            arrowHeads.put(dKey, arrow);
                            container.getChildren().add(arrow);
                        }
                    }
                }
            }
        } else {
            // Undirected mode: avoid duplicates by i<j
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
                                container.getChildren().add(0, line);
                            }
                        }
                    }
                }
            }
        }
        rebuildPositions();
    }

    public void clearHighlights() {
        for (Map.Entry<Integer, Circle> e : nodeCircles.entrySet()) {
            int id = e.getKey();
            Circle c = e.getValue();
            // Keep error nodes red and visited nodes green
            if (errorNodes.contains(id))
                continue;
            if (c.getFill() != Color.FORESTGREEN) {
                c.setFill(Color.STEELBLUE);
            }
        }
        for (Line l : edgeLines.values()) {
            l.setStroke(Color.GRAY);
            l.setStrokeWidth(2.0);
        }
        // keep activePath state but visual reset to baseline until setActivePathEdge
        // called again
    }

    public void highlightCurrent(int v) {
        if (errorNodes.contains(v))
            return; // keep persistent error red
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

    /**
     * Explicitly set the fill color of a node (used by Graph Coloring).
     */
    public void setNodeColor(int v, Color color) {
        Circle c = nodeCircles.get(v);
        if (c != null) {
            cancelAnimation(c);
            c.setFill(color);
        }
    }

    /**
     * Access to the underlying container for advanced composition if needed.
     */
    public Pane getContainer() {
        return container;
    }

    /** Highlight a node as an error (e.g., full/blocked) */
    public void highlightError(int v) {
        Circle c = nodeCircles.get(v);
        if (c != null) {
            c.setFill(Color.CRIMSON);
        }
        errorNodes.add(v);
    }

    /** Clear all error flags (not used by default) */
    public void clearErrors() {
        errorNodes.clear();
    }

    public void highlightEdge(int u, int v) {
        Line l = findEdgeLine(u, v);
        if (l != null) {
            l.setStroke(Color.DARKORANGE);
            syncArrowColor(u, v);
        }
    }

    /**
     * Briefly shakes a node left-right to indicate a conflict.
     */
    public void shakeNode(int v) {
        Circle c = nodeCircles.get(v);
        Text t = nodeLabels.get(v);
        if (c == null)
            return;
        // Use translate animations so we don't disturb layout permanently
        Timeline shake = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(c.translateXProperty(), 0),
                        new KeyValue(t != null ? t.translateXProperty() : c.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(60),
                        new KeyValue(c.translateXProperty(), 6),
                        new KeyValue(t != null ? t.translateXProperty() : c.translateXProperty(), 6)),
                new KeyFrame(Duration.millis(120),
                        new KeyValue(c.translateXProperty(), -6),
                        new KeyValue(t != null ? t.translateXProperty() : c.translateXProperty(), -6)),
                new KeyFrame(Duration.millis(180),
                        new KeyValue(c.translateXProperty(), 5),
                        new KeyValue(t != null ? t.translateXProperty() : c.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(240),
                        new KeyValue(c.translateXProperty(), -5),
                        new KeyValue(t != null ? t.translateXProperty() : c.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(450),
                        new KeyValue(c.translateXProperty(), 0),
                        new KeyValue(t != null ? t.translateXProperty() : c.translateXProperty(), 0)));
        registerAnimation(c, shake);
        shake.play();
    }

    /**
     * Mark or unmark an edge as part of the active DFS path (stack). Active edges
     * are thicker and blue.
     */
    public void setActivePathEdge(int u, int v, boolean active) {
        String key = findEdgeLineKey(u, v);
        Line l = key != null ? edgeLines.get(key) : null;
        if (l == null)
            return;
        activePath.put(key, active);
        if (active) {
            l.setStroke(Color.CORNFLOWERBLUE);
            l.setStrokeWidth(4.0);
        } else {
            l.setStroke(Color.GRAY);
            l.setStrokeWidth(2.0);
        }
        syncArrowColor(u, v);
    }

    /**
     * Animate unwinding of an active edge during backtracking.
     */
    public void animateBacktrackEdge(int u, int v) {
        Line l = findEdgeLine(u, v);
        if (l == null)
            return;
        Color from = (l.getStroke() instanceof Color) ? (Color) l.getStroke() : Color.CORNFLOWERBLUE;
        StrokeTransition color = new StrokeTransition(Duration.millis(220), l, from, Color.GRAY);
        Timeline widthShrink = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(l.strokeWidthProperty(), l.getStrokeWidth())),
                new KeyFrame(Duration.millis(220), new KeyValue(l.strokeWidthProperty(), 2.0)));
        ParallelTransition pt = new ParallelTransition(color, widthShrink);
        pt.play();
    }

    /**
     * Brief pulse on the node to indicate backtracking from it.
     */
    public void flashBacktrackNode(int v) {
        Circle c = nodeCircles.get(v);
        if (c == null)
            return;
        Circle ring = new Circle(c.getCenterX(), c.getCenterY(), c.getRadius());
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.CRIMSON);
        ring.setStrokeWidth(3.0);
        container.getChildren().add(ring);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(ring.opacityProperty(), 1.0),
                        new KeyValue(ring.radiusProperty(), c.getRadius())),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(ring.opacityProperty(), 0.0),
                        new KeyValue(ring.radiusProperty(), c.getRadius() + 16)));
        tl.setOnFinished(e -> container.getChildren().remove(ring));
        tl.play();
    }

    /**
     * Flash all nodes with a ring to indicate a global event (e.g., k changes).
     */
    public void flashAllNodes() {
        for (int i = 0; i < nodeCount; i++) {
            flashBacktrackNode(i);
        }
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
            if (p == null)
                continue;
            Circle c = nodeCircles.get(i);
            Text t = nodeLabels.get(i);
            if (c != null) {
                c.setCenterX(p[0]);
                c.setCenterY(p[1]);
            }
            if (t != null) {
                // Center text properly using its bounds
                javafx.geometry.Bounds bounds = t.getLayoutBounds();
                t.setX(p[0] - bounds.getWidth() / 2.0);
                t.setY(p[1] + bounds.getHeight() / 4.0);
            }
        }

        // Update edges
        if (directed) {
            // Directed mode: offset bidirectional edges and add arrowheads
            // Detect bidirectional pairs
            Set<String> biPairs = new HashSet<>();
            for (String key : edgeLines.keySet()) {
                if (key.contains("->")) {
                    String[] parts = key.split("->");
                    int eu = Integer.parseInt(parts[0]);
                    int ev = Integer.parseInt(parts[1]);
                    String reverseKey = directedEdgeKey(ev, eu);
                    if (edgeLines.containsKey(reverseKey)) {
                        biPairs.add(Math.min(eu, ev) + "-" + Math.max(eu, ev));
                    }
                }
            }

            for (Map.Entry<String, Line> e : edgeLines.entrySet()) {
                String key = e.getKey();
                if (!key.contains("->"))
                    continue;

                String[] parts = key.split("->");
                int eu = Integer.parseInt(parts[0]);
                int ev = Integer.parseInt(parts[1]);
                double[] pu = pos.get(eu);
                double[] pv = pos.get(ev);
                if (pu == null || pv == null)
                    continue;

                Line l = e.getValue();
                double dx = pv[0] - pu[0];
                double dy = pv[1] - pu[1];
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0)
                    continue;

                // Normal vector (perpendicular)
                double nx = -dy / len;
                double ny = dx / len;

                // Determine offset: if bidirectional, offset; otherwise center
                double offset = 0;
                String pairKey = Math.min(eu, ev) + "-" + Math.max(eu, ev);
                if (biPairs.contains(pairKey)) {
                    // Forward (u < v) goes one side, reverse goes other
                    offset = (eu < ev) ? 6.0 : -6.0;
                }

                double sx = pu[0] + nx * offset;
                double sy = pu[1] + ny * offset;
                double ex = pv[0] + nx * offset;
                double ey = pv[1] + ny * offset;

                // Shorten line so it doesn't go inside node circles
                double unitDx = dx / len;
                double unitDy = dy / len;
                double shortenStart = nodeRadius + 2;
                double shortenEnd = nodeRadius + 10; // leave room for arrowhead
                sx += unitDx * shortenStart;
                sy += unitDy * shortenStart;
                ex -= unitDx * shortenEnd;
                ey -= unitDy * shortenEnd;

                l.setStartX(sx);
                l.setStartY(sy);
                l.setEndX(ex);
                l.setEndY(ey);

                // Update arrowhead
                Polygon arrow = arrowHeads.get(key);
                if (arrow != null) {
                    double arrowSize = 8.0;
                    // Arrow tip is at the edge of the target node
                    double tipX = pv[0] + nx * offset - unitDx * (nodeRadius + 2);
                    double tipY = pv[1] + ny * offset - unitDy * (nodeRadius + 2);
                    // Two base points
                    double baseX1 = tipX - unitDx * arrowSize + nx * arrowSize * 0.5;
                    double baseY1 = tipY - unitDy * arrowSize + ny * arrowSize * 0.5;
                    double baseX2 = tipX - unitDx * arrowSize - nx * arrowSize * 0.5;
                    double baseY2 = tipY - unitDy * arrowSize - ny * arrowSize * 0.5;

                    arrow.getPoints().clear();
                    arrow.getPoints().addAll(
                            tipX, tipY,
                            baseX1, baseY1,
                            baseX2, baseY2);
                    // Arrow color remains fixed by the directional coloring set at graph creation
                    // (Option A)
                }
            }
        } else {
            // Undirected mode: simple line between nodes
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

        // Update edge weight label positions
        updateEdgeWeightPositions(pos);
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
            pos.put(i, new double[] { x, y });
        }
        return pos;
    }

    private Map<Integer, double[]> computeTreeLayout() {
        double w = Math.max(300, container.getWidth());
        double h = Math.max(300, container.getHeight());

        Map<Integer, double[]> pos = new HashMap<>();
        if (nodeCount == 0)
            return pos;

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

        double leftBound = 40, rightBound = w - 40;
        if (treeLeafSpacingEnabled) {
            // For deeper levels (depth >= 3), enforce minimum horizontal spacing between
            // leaves
            int deepLeafCount = 0;
            for (int i = 0; i < nodeCount; i++) {
                int d = depth.getOrDefault(i, 0);
                if (d >= 3 && children.get(i).isEmpty())
                    deepLeafCount++;
            }
            double contentWidth = Math.max(w, 80 + deepLeafCount * treeMinLeafSpacing);
            // Expand container width so ScrollPane can provide room when needed
            container.setMinWidth(contentWidth);
            container.setPrefWidth(contentWidth);
            leftBound = 40;
            rightBound = contentWidth - 40;
        }

        layoutTree(0, children, depth, subtreeSize, pos, leftBound, rightBound, verticalSpacing, 40);

        // Position disconnected nodes
        double disconnectedY = h - 40;
        double disconnectedX = 40;
        for (int i = 0; i < nodeCount; i++) {
            if (!pos.containsKey(i)) {
                pos.put(i, new double[] { disconnectedX, disconnectedY });
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
        pos.put(node, new double[] { x, y });

        List<Integer> kids = children.get(node);
        if (kids.isEmpty())
            return;

        // Distribute children horizontally based on subtree sizes
        double currentX = leftX;
        double totalWidth = rightX - leftX;
        int totalSize = subtreeSize.get(node) - 1; // exclude current node

        for (int child : kids) {
            int childSize = subtreeSize.get(child);
            double childWidth = totalSize > 0 ? (childSize * totalWidth / totalSize) : (totalWidth / kids.size());
            layoutTree(child, children, depth, subtreeSize, pos, currentX, currentX + childWidth, verticalSpacing,
                    topMargin);
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
        if (u > v) {
            int tmp = u;
            u = v;
            v = tmp;
        }
        return u + "-" + v;
    }

    /**
     * Find the Line for an edge, checking directed keys first in directed mode.
     */
    private Line findEdgeLine(int u, int v) {
        if (directed) {
            Line l = edgeLines.get(directedEdgeKey(u, v));
            if (l != null)
                return l;
            l = edgeLines.get(directedEdgeKey(v, u));
            if (l != null)
                return l;
        }
        Line l = edgeLines.get(edgeKey(u, v));
        if (l != null)
            return l;
        return edgeLines.get(edgeKey(v, u));
    }

    /**
     * Find the key used for an edge in the edgeLines map.
     */
    private String findEdgeLineKey(int u, int v) {
        if (directed) {
            String dk = directedEdgeKey(u, v);
            if (edgeLines.containsKey(dk))
                return dk;
            dk = directedEdgeKey(v, u);
            if (edgeLines.containsKey(dk))
                return dk;
        }
        String k = edgeKey(u, v);
        if (edgeLines.containsKey(k))
            return k;
        k = edgeKey(v, u);
        if (edgeLines.containsKey(k))
            return k;
        return null;
    }

    /**
     * Update arrowhead color to match edge line color.
     */
    private void syncArrowColor(int u, int v) {
        // Option A chosen by user: Arrow color is permanently fixed to direction, do
        // not sync with line
    }

    /**
     * Set edge weights for weighted graph visualization.
     * 
     * @param weights Map from edge key "u-v" to weight value
     */
    public void setEdgeWeights(Map<String, Integer> weights) {
        this.edgeWeights.clear();
        if (weights != null) {
            this.edgeWeights.putAll(weights);
        }
        updateEdgeWeightLabels();
    }

    /**
     * Toggle display of edge weights.
     */
    public void showEdgeWeights(boolean show) {
        this.showWeights = show;
        for (Text label : edgeWeightLabels.values()) {
            label.setVisible(show);
        }
    }

    /**
     * Highlight an edge with a specific color and thickness.
     */
    public void highlightEdgeWithColor(int u, int v, Color color, double thickness) {
        Line l = findEdgeLine(u, v);
        if (l != null) {
            cancelAnimation(l);
            l.setStroke(color);
            l.setStrokeWidth(thickness);
            syncArrowColor(u, v);
        }
    }

    /**
     * Set edge thickness.
     */
    public void setEdgeThickness(int u, int v, double thickness) {
        Line l = findEdgeLine(u, v);
        if (l != null) {
            l.setStrokeWidth(thickness);
        }
    }

    /**
     * Animate edge blinking effect.
     */
    public void blinkEdge(int u, int v, Color color, int times) {
        Line l = findEdgeLine(u, v);
        if (l == null)
            return;

        Color originalColor = (Color) l.getStroke();
        double originalWidth = l.getStrokeWidth();

        Timeline blink = new Timeline();
        for (int i = 0; i < times; i++) {
            blink.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(i * 200), new KeyValue(l.strokeProperty(), originalColor)),
                    new KeyFrame(Duration.millis(i * 200 + 100), new KeyValue(l.strokeProperty(), color)));
        }
        blink.getKeyFrames().add(
                new KeyFrame(Duration.millis(times * 200), new KeyValue(l.strokeProperty(), originalColor)));
        registerAnimation(l, blink);
        blink.play();
    }

    /**
     * Pulse an edge with glow effect.
     */
    public void pulseEdge(int u, int v, Color color) {
        Line l = findEdgeLine(u, v);
        if (l == null)
            return;

        double originalWidth = l.getStrokeWidth();
        Color originalColor = (Color) l.getStroke();

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(l.strokeWidthProperty(), originalWidth),
                        new KeyValue(l.strokeProperty(), originalColor)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(l.strokeWidthProperty(), originalWidth + 3),
                        new KeyValue(l.strokeProperty(), color)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(l.strokeWidthProperty(), originalWidth),
                        new KeyValue(l.strokeProperty(), originalColor)));
        registerAnimation(l, pulse);
        pulse.play();
    }

    /**
     * Transition edge color smoothly.
     */
    public void transitionEdgeColor(int u, int v, Color fromColor, Color toColor, double toThickness,
            Duration duration) {
        Line l = findEdgeLine(u, v);
        if (l == null)
            return;

        l.setStroke(fromColor);
        StrokeTransition colorTransition = new StrokeTransition(duration, l, fromColor, toColor);
        Timeline thicknessTransition = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(l.strokeWidthProperty(), l.getStrokeWidth())),
                new KeyFrame(duration, new KeyValue(l.strokeWidthProperty(), toThickness)));

        ParallelTransition transition = new ParallelTransition(colorTransition, thicknessTransition);
        transition.play();
    }

    /**
     * Make edge dashed (for rejected edges).
     */
    public void setEdgeDashed(int u, int v, boolean dashed) {
        Line l = findEdgeLine(u, v);
        if (l != null) {
            if (dashed) {
                l.getStrokeDashArray().addAll(5.0, 5.0);
            } else {
                l.getStrokeDashArray().clear();
            }
        }
    }

    /**
     * Add glow effect to a vertex.
     */
    public void glowVertex(int v, Color color) {
        Circle c = nodeCircles.get(v);
        if (c == null)
            return;

        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(color);
        glow.setRadius(20);
        glow.setSpread(0.6);
        c.setEffect(glow);
    }

    /**
     * Remove glow effect from a vertex.
     */
    public void removeGlow(int v) {
        Circle c = nodeCircles.get(v);
        if (c == null)
            return;
        c.setEffect(null);
    }

    /**
     * Wave animation through multiple edges.
     */
    public void waveEdges(List<String> edgeKeys, Color color, Duration duration) {
        for (int i = 0; i < edgeKeys.size(); i++) {
            String key = edgeKeys.get(i);
            String[] parts = key.split("-");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);

            final int delay = i;
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                    Duration.millis(delay * 100));
            pause.setOnFinished(e -> pulseEdge(u, v, color));
            pause.play();
        }
    }

    /**
     * Shake an edge to indicate rejection.
     */
    public void shakeEdge(int u, int v) {
        Line l = findEdgeLine(u, v);
        if (l == null)
            return;

        double origX1 = l.getStartX();
        double origY1 = l.getStartY();
        double origX2 = l.getEndX();
        double origY2 = l.getEndY();

        javafx.animation.Timeline shake = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.millis(0),
                        new javafx.animation.KeyValue(l.startXProperty(), origX1),
                        new javafx.animation.KeyValue(l.startYProperty(), origY1)),
                new javafx.animation.KeyFrame(Duration.millis(50),
                        new javafx.animation.KeyValue(l.startXProperty(), origX1 - 3),
                        new javafx.animation.KeyValue(l.startYProperty(), origY1 - 3)),
                new javafx.animation.KeyFrame(Duration.millis(100),
                        new javafx.animation.KeyValue(l.startXProperty(), origX1 + 3),
                        new javafx.animation.KeyValue(l.startYProperty(), origY1 + 3)),
                new javafx.animation.KeyFrame(Duration.millis(150),
                        new javafx.animation.KeyValue(l.startXProperty(), origX1),
                        new javafx.animation.KeyValue(l.startYProperty(), origY1)));
        shake.setCycleCount(2);
        registerAnimation(l, shake);
        shake.play();
    }

    /**
     * Highlight edge with prominent weight label.
     */
    public void emphasizeEdgeWeight(int u, int v) {
        String key = findEdgeLineKey(u, v);
        Text label = key != null ? edgeWeightLabels.get(key) : null;
        if (label == null)
            return;

        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(Duration.millis(300), label);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.5);
        scale.setToY(1.5);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    private void updateEdgeWeightLabels() {
        // Remove old labels and backgrounds
        for (Text label : edgeWeightLabels.values()) {
            container.getChildren().remove(label);
        }
        for (javafx.scene.shape.Rectangle bg : weightBgRects.values()) {
            container.getChildren().remove(bg);
        }
        edgeWeightLabels.clear();
        weightBgRects.clear();

        if (!showWeights)
            return;

        for (Map.Entry<String, Integer> entry : edgeWeights.entrySet()) {
            String key = entry.getKey();
            Integer weight = entry.getValue();

            if (edgeLines.containsKey(key)) {
                Text label = new Text(String.valueOf(weight));
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

                // Color-code directed edge weights
                if (directed && key.contains("->")) {
                    String[] parts = key.split("->");
                    int eu = Integer.parseInt(parts[0]);
                    int ev = Integer.parseInt(parts[1]);
                    label.setFill(eu < ev ? DIRECTED_FORWARD_COLOR : DIRECTED_REVERSE_COLOR);
                } else {
                    label.setFill(Color.BLACK);
                }

                // Background rectangle
                javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle();
                bg.setFill(Color.color(1, 1, 1, 0.85));
                bg.setStroke(Color.LIGHTGRAY);
                bg.setStrokeWidth(0.5);
                bg.setArcWidth(4);
                bg.setArcHeight(4);

                javafx.geometry.Bounds bounds = label.getLayoutBounds();
                bg.setWidth(bounds.getWidth() + 6);
                bg.setHeight(bounds.getHeight() + 4);

                edgeWeightLabels.put(key, label);
                weightBgRects.put(key, bg);
                container.getChildren().addAll(bg, label);
            }
        }

        rebuildPositions();
    }

    private void updateEdgeWeightPositions(Map<Integer, double[]> pos) {
        if (directed) {
            updateDirectedWeightPositions(pos);
        } else {
            updateUndirectedWeightPositions(pos);
        }
    }

    private void updateDirectedWeightPositions(Map<Integer, double[]> pos) {
        // Detect bidirectional pairs for offset calculation
        Set<String> biPairs = new HashSet<>();
        for (String key : edgeLines.keySet()) {
            if (key.contains("->")) {
                String[] parts = key.split("->");
                int eu = Integer.parseInt(parts[0]);
                int ev = Integer.parseInt(parts[1]);
                if (edgeLines.containsKey(directedEdgeKey(ev, eu))) {
                    biPairs.add(Math.min(eu, ev) + "-" + Math.max(eu, ev));
                }
            }
        }

        for (Map.Entry<String, Text> entry : edgeWeightLabels.entrySet()) {
            String key = entry.getKey();
            Text label = entry.getValue();
            if (!key.contains("->"))
                continue;

            String[] parts = key.split("->");
            int eu = Integer.parseInt(parts[0]);
            int ev = Integer.parseInt(parts[1]);

            double[] pu = pos.get(eu);
            double[] pv = pos.get(ev);
            if (pu == null || pv == null)
                continue;

            double dx = pv[0] - pu[0];
            double dy = pv[1] - pu[1];
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0)
                continue;

            // Normal vector (perpendicular)
            double nx = -dy / len;
            double ny = dx / len;

            // Midpoint of the edge
            double midX = (pu[0] + pv[0]) / 2.0;
            double midY = (pu[1] + pv[1]) / 2.0;

            double labelX, labelY;
            String pairKey = Math.min(eu, ev) + "-" + Math.max(eu, ev);

            if (biPairs.contains(pairKey)) {
                // Bidirectional: offset labels above/below the edge center
                double perpOffset = 16.0;
                double lineOffset = (eu < ev) ? 6.0 : -6.0;
                labelX = midX + nx * (perpOffset + lineOffset);
                labelY = midY + ny * (perpOffset + lineOffset);
            } else {
                // Unidirectional: small offset from center
                labelX = midX + nx * 12.0;
                labelY = midY + ny * 12.0;
            }

            javafx.geometry.Bounds bounds = label.getLayoutBounds();
            label.setX(labelX - bounds.getWidth() / 2.0);
            label.setY(labelY + bounds.getHeight() / 4.0);

            // Update background
            javafx.scene.shape.Rectangle bg = weightBgRects.get(key);
            if (bg != null) {
                bg.setWidth(bounds.getWidth() + 6);
                bg.setHeight(bounds.getHeight() + 4);
                bg.setX(labelX - bounds.getWidth() / 2.0 - 3);
                bg.setY(labelY - bounds.getHeight() / 2.0 - 2);
            }
        }
    }

    private void updateUndirectedWeightPositions(Map<Integer, double[]> pos) {
        // First pass: calculate all midpoint positions
        Map<String, double[]> midpoints = new HashMap<>();
        for (Map.Entry<String, Text> entry : edgeWeightLabels.entrySet()) {
            String key = entry.getKey();
            String[] uv = key.split("-");
            int u = Integer.parseInt(uv[0]);
            int v = Integer.parseInt(uv[1]);

            double[] pu = pos.get(u);
            double[] pv = pos.get(v);

            if (pu != null && pv != null) {
                double midX = (pu[0] + pv[0]) / 2.0;
                double midY = (pu[1] + pv[1]) / 2.0;
                midpoints.put(key, new double[] { midX, midY });
            }
        }

        // Detect overlaps
        Set<String> overlappingEdges = new HashSet<>();
        List<String> keys = new ArrayList<>(midpoints.keySet());
        double overlapThreshold = 20.0;

        for (int i = 0; i < keys.size(); i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                double[] mid1 = midpoints.get(keys.get(i));
                double[] mid2 = midpoints.get(keys.get(j));
                double ddx = mid1[0] - mid2[0];
                double ddy = mid1[1] - mid2[1];
                if (Math.sqrt(ddx * ddx + ddy * ddy) < overlapThreshold) {
                    overlappingEdges.add(keys.get(i));
                    overlappingEdges.add(keys.get(j));
                }
            }
        }

        // Second pass: position labels
        for (Map.Entry<String, Text> entry : edgeWeightLabels.entrySet()) {
            String key = entry.getKey();
            Text label = entry.getValue();

            String[] uv = key.split("-");
            int u = Integer.parseInt(uv[0]);
            int v = Integer.parseInt(uv[1]);

            double[] pu = pos.get(u);
            double[] pv = pos.get(v);

            if (pu != null && pv != null) {
                double dx = pv[0] - pu[0];
                double dy = pv[1] - pu[1];

                double labelX, labelY;

                if (overlappingEdges.contains(key)) {
                    int hash = key.hashCode();
                    double offsetRatio = ((hash % 2) == 0) ? 0.60 : 0.40;
                    labelX = pu[0] + dx * offsetRatio;
                    labelY = pu[1] + dy * offsetRatio;
                } else {
                    labelX = (pu[0] + pv[0]) / 2.0;
                    labelY = (pu[1] + pv[1]) / 2.0;
                }

                javafx.geometry.Bounds bounds = label.getLayoutBounds();
                label.setX(labelX - bounds.getWidth() / 2.0);
                label.setY(labelY + bounds.getHeight() / 4.0);

                // Update background
                javafx.scene.shape.Rectangle bg = weightBgRects.get(key);
                if (bg != null) {
                    bg.setX(labelX - bounds.getWidth() / 2.0 - 3);
                    bg.setY(labelY - bounds.getHeight() / 2.0 - 2);
                } else {
                    int labelIndex = container.getChildren().indexOf(label);
                    if (labelIndex > 0) {
                        javafx.scene.Node bgNode = container.getChildren().get(labelIndex - 1);
                        if (bgNode instanceof javafx.scene.shape.Rectangle) {
                            javafx.scene.shape.Rectangle oldBg = (javafx.scene.shape.Rectangle) bgNode;
                            oldBg.setX(labelX - bounds.getWidth() / 2.0 - 3);
                            oldBg.setY(labelY - bounds.getHeight() / 2.0 - 2);
                        }
                    }
                }
            }
        }
    }
}
