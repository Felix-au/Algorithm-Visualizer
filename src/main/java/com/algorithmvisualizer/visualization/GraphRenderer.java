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
 * Renders an undirected graph with nodes arranged on a circle.
 * Provides helpers to highlight current node, visited nodes, and edges.
 */
public class GraphRenderer {

    private final Pane container;
    private final Map<Integer, Circle> nodeCircles = new HashMap<>();
    private final Map<Integer, Text> nodeLabels = new HashMap<>();
    private final Map<String, Line> edgeLines = new HashMap<>();
    private final Map<String, Boolean> activePath = new HashMap<>();
    private int nodeCount = 0;

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

    private void rebuildPositions() {
        double w = Math.max(300, container.getWidth());
        double h = Math.max(300, container.getHeight());
        double cx = w / 2.0;
        double cy = h / 2.0;
        double radius = Math.max(100, Math.min(w, h) / 2.5);
        // Positions
        Map<Integer, double[]> pos = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            double angle = 2 * Math.PI * i / Math.max(1, nodeCount);
            double x = cx + radius * Math.cos(angle);
            double y = cy + radius * Math.sin(angle);
            pos.put(i, new double[]{x, y});
            Circle c = nodeCircles.get(i);
            Text t = nodeLabels.get(i);
            if (c != null) {
                c.setCenterX(x);
                c.setCenterY(y);
            }
            if (t != null) {
                t.setX(x - 4);
                t.setY(y + 4);
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

    private String edgeKey(int u, int v) {
        if (u > v) { int tmp = u; u = v; v = tmp; }
        return u + "-" + v;
    }
}
