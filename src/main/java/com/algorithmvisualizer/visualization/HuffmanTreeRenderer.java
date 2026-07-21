package com.algorithmvisualizer.visualization;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HuffmanTreeRenderer {

    public static class TreeNode {
        public int id;
        public Character ch;
        public int freq;
        public int left = -1;
        public int right = -1;
        public double x, y;
    }

    private final Pane rootPane;
    private final Canvas canvas;
    private final Pane overlayPane;
    private final List<TreeNode> nodes = new ArrayList<>();
    private int rootId = -1;
    private boolean forestMode = false;

    private static final double NODE_RADIUS = 22;
    private static final double LEVEL_GAP = 55;
    private static final double PAD_X = 30;
    private static final double PAD_TOP = 35;
    private static final Color INTERNAL_COLOR = Color.web("#6366f1");
    private static final Color LEAF_COLOR = Color.web("#10b981");
    private static final Color HIGHLIGHT_COLOR = Color.web("#f59e0b");
    private static final Color EXTRACT_COLOR = Color.web("#ef4444");
    private static final Color PATH_COLOR = Color.web("#3b82f6");
    private static final Color TEXT_COLOR = Color.WHITE;

    private final Set<Integer> highlightedNodes = new HashSet<>();
    private Color highlightOverride = null;
    private final Set<String> highlightedEdges = new HashSet<>();
    private final List<Timeline> activeAnimations = new ArrayList<>();

    public HuffmanTreeRenderer() {
        rootPane = new Pane();
        canvas = new Canvas(800, 500);
        overlayPane = new Pane();
        overlayPane.setPickOnBounds(false);
        rootPane.getChildren().addAll(canvas, overlayPane);
        rootPane.widthProperty().addListener(
            (obs, o, n) -> {
                canvas.setWidth(n.doubleValue());
                rerender();
            });
        rootPane.heightProperty().addListener(
            (obs, o, n) -> {
                canvas.setHeight(n.doubleValue());
                rerender();
            });
    }

    /** Re-renders using current mode (forest or single root). */
    private void rerender() {
        if (forestMode) {
            renderForest();
        } else {
            render();
        }
    }

    public Node getNode() { return rootPane; }

    public void clear() {
        nodes.clear();
        rootId = -1;
        forestMode = false;
        highlightedNodes.clear();
        highlightedEdges.clear();
        highlightOverride = null;
        stopAllAnimations();
        overlayPane.getChildren().clear();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void addNode(int id, Character ch, int freq) {
        while (nodes.size() <= id) nodes.add(null);
        TreeNode tn = new TreeNode();
        tn.id = id;
        tn.ch = ch;
        tn.freq = freq;
        nodes.set(id, tn);
    }

    public void setChildren(int parentId, int leftId, int rightId) {
        if (parentId < nodes.size() && nodes.get(parentId) != null) {
            nodes.get(parentId).left = leftId;
            nodes.get(parentId).right = rightId;
        }
    }

    public void setRoot(int id) { this.rootId = id; }

    public void buildFromSolverNodes(List<?> solverNodes, int root) {
        nodes.clear();
        for (Object obj : solverNodes) {
            if (obj instanceof com.algorithmvisualizer.algorithm.HuffmanCodingSolverImpl.HNode) {
                com.algorithmvisualizer.algorithm.HuffmanCodingSolverImpl.HNode hn =
                    (com.algorithmvisualizer.algorithm.HuffmanCodingSolverImpl.HNode) obj;
                addNode(hn.id, hn.ch, hn.freq);
                if (hn.left >= 0 || hn.right >= 0) {
                    setChildren(hn.id, hn.left, hn.right);
                }
            }
        }
        this.rootId = root;
        this.forestMode = false;
        render();
    }

    public void highlightNode(int id, Color color) {
        highlightedNodes.add(id);
        highlightOverride = color;
        rerender();
    }

    public void clearHighlights() {
        highlightedNodes.clear();
        highlightedEdges.clear();
        highlightOverride = null;
        rerender();
    }

    public void highlightEdge(int parentId, int childId) {
        highlightedEdges.add(parentId + "-" + childId);
        rerender();
    }

    public void pulseNode(int id) {
        if (id < 0 || id >= nodes.size() || nodes.get(id) == null) return;
        TreeNode tn = nodes.get(id);
        Circle pulse = new Circle(tn.x, tn.y, NODE_RADIUS);
        pulse.setFill(Color.TRANSPARENT);
        pulse.setStroke(HIGHLIGHT_COLOR);
        pulse.setStrokeWidth(3);
        overlayPane.getChildren().add(pulse);
        ScaleTransition st = new ScaleTransition(Duration.millis(400), pulse);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.8); st.setToY(1.8);
        FadeTransition ft = new FadeTransition(Duration.millis(400), pulse);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.setOnFinished(ev -> overlayPane.getChildren().remove(pulse));
        pt.play();
    }

    public void glowNode(int id, Color color) {
        highlightedNodes.add(id);
        highlightOverride = color;
        rerender();
        if (id >= 0 && id < nodes.size() && nodes.get(id) != null) {
            TreeNode tn = nodes.get(id);
            Circle glow = new Circle(tn.x, tn.y, NODE_RADIUS + 4);
            glow.setFill(Color.TRANSPARENT);
            glow.setStroke(color);
            glow.setStrokeWidth(3);
            glow.setEffect(new Glow(0.8));
            overlayPane.getChildren().add(glow);
            FadeTransition ft = new FadeTransition(Duration.millis(600), glow);
            ft.setFromValue(1.0); ft.setToValue(0.0);
            ft.setCycleCount(2);
            ft.setAutoReverse(true);
            ft.setOnFinished(ev -> overlayPane.getChildren().remove(glow));
            ft.play();
        }
    }

    public void shakeNode(int id) {
        if (id < 0 || id >= nodes.size() || nodes.get(id) == null) return;
        TreeNode tn = nodes.get(id);
        Circle shake = new Circle(tn.x, tn.y, NODE_RADIUS);
        shake.setFill(Color.TRANSPARENT);
        shake.setStroke(EXTRACT_COLOR);
        shake.setStrokeWidth(2);
        overlayPane.getChildren().add(shake);
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), shake);
        tt.setByX(5);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(ev -> overlayPane.getChildren().remove(shake));
        tt.play();
    }

    public void render() {
        forestMode = false;
        if (rootId < 0 || rootId >= nodes.size() || nodes.get(rootId) == null) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }
        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();
        int depth = treeDepth(rootId);
        double neededH = PAD_TOP + depth * LEVEL_GAP + NODE_RADIUS * 2 + 20;
        if (neededH > canvasH) {
            canvas.setHeight(neededH);
            canvasH = neededH;
        }
        layoutTree(rootId, PAD_X, canvasW - PAD_X, PAD_TOP, 0);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);
        drawEdges(gc, rootId);
        drawNodes(gc, rootId);
    }

    public void renderForest() {
        forestMode = true;
        if (nodes.isEmpty()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }

        // Find which nodes are children of some other node
        Set<Integer> childSet = new HashSet<>();
        for (TreeNode tn : nodes) {
            if (tn == null) continue;
            if (tn.left >= 0) childSet.add(tn.left);
            if (tn.right >= 0) childSet.add(tn.right);
        }

        // Roots = nodes NOT referenced as children
        List<Integer> roots = new ArrayList<>();
        for (TreeNode tn : nodes) {
            if (tn != null && !childSet.contains(tn.id)) {
                roots.add(tn.id);
            }
        }

        if (roots.isEmpty()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            return;
        }

        // Find max depth across all subtrees
        int maxDepth = 0;
        for (int rid : roots) {
            int d = treeDepth(rid);
            if (d > maxDepth) maxDepth = d;
        }

        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();
        double neededH = PAD_TOP + maxDepth * LEVEL_GAP + NODE_RADIUS * 2 + 20;
        if (neededH > canvasH) {
            canvas.setHeight(neededH);
            canvasH = neededH;
        }

        // Give each root subtree width proportional to leaf count
        int totalLeaves = 0;
        int[] leafCounts = new int[roots.size()];
        for (int i = 0; i < roots.size(); i++) {
            int lc = countLeaves(roots.get(i));
            if (lc < 1) lc = 1;
            leafCounts[i] = lc;
            totalLeaves += lc;
        }
        if (totalLeaves < 1) totalLeaves = 1;

        double usableW = canvasW - 2 * PAD_X;
        double xOff = PAD_X;
        for (int i = 0; i < roots.size(); i++) {
            double sliceW = usableW * leafCounts[i] / totalLeaves;
            layoutTree(roots.get(i), xOff, xOff + sliceW, PAD_TOP, 0);
            xOff += sliceW;
        }

        // Draw everything
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, canvasH);
        for (int rid : roots) {
            drawEdges(gc, rid);
        }
        for (int rid : roots) {
            drawNodes(gc, rid);
        }
    }

    private int countLeaves(int nodeId) {
        if (nodeId < 0 || nodeId >= nodes.size() || nodes.get(nodeId) == null) return 0;
        TreeNode tn = nodes.get(nodeId);
        if (tn.left < 0 && tn.right < 0) return 1;
        return countLeaves(tn.left) + countLeaves(tn.right);
    }

    private void layoutTree(int nodeId, double left, double right, double y, int depth) {
        if (nodeId < 0 || nodeId >= nodes.size() || nodes.get(nodeId) == null) return;
        TreeNode tn = nodes.get(nodeId);
        double mid = (left + right) / 2;
        tn.x = mid;
        tn.y = y;
        double childY = y + LEVEL_GAP;
        if (tn.left >= 0) layoutTree(tn.left, left, mid, childY, depth + 1);
        if (tn.right >= 0) layoutTree(tn.right, mid, right, childY, depth + 1);
    }

    private void drawEdges(GraphicsContext gc, int nodeId) {
        if (nodeId < 0 || nodeId >= nodes.size() || nodes.get(nodeId) == null) return;
        TreeNode tn = nodes.get(nodeId);
        if (tn.left >= 0 && tn.left < nodes.size() && nodes.get(tn.left) != null) {
            TreeNode child = nodes.get(tn.left);
            boolean hl = highlightedEdges.contains(nodeId + "-" + tn.left);
            gc.setStroke(hl ? PATH_COLOR : Color.web("#94a3b8"));
            gc.setLineWidth(hl ? 3 : 2);
            gc.strokeLine(tn.x, tn.y + NODE_RADIUS, child.x, child.y - NODE_RADIUS);
            double mx = (tn.x + child.x) / 2 - 12;
            double my = (tn.y + child.y) / 2;
            gc.setFill(hl ? PATH_COLOR : Color.web("#64748b"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("0", mx, my);
            drawEdges(gc, tn.left);
        }
        if (tn.right >= 0 && tn.right < nodes.size() && nodes.get(tn.right) != null) {
            TreeNode child = nodes.get(tn.right);
            boolean hl = highlightedEdges.contains(nodeId + "-" + tn.right);
            gc.setStroke(hl ? PATH_COLOR : Color.web("#94a3b8"));
            gc.setLineWidth(hl ? 3 : 2);
            gc.strokeLine(tn.x, tn.y + NODE_RADIUS, child.x, child.y - NODE_RADIUS);
            double mx = (tn.x + child.x) / 2 + 12;
            double my = (tn.y + child.y) / 2;
            gc.setFill(hl ? PATH_COLOR : Color.web("#64748b"));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("1", mx, my);
            drawEdges(gc, tn.right);
        }
    }

    private void drawNodes(GraphicsContext gc, int nodeId) {
        if (nodeId < 0 || nodeId >= nodes.size() || nodes.get(nodeId) == null) return;
        TreeNode tn = nodes.get(nodeId);
        boolean isLeaf = (tn.left < 0 && tn.right < 0);
        boolean isHighlighted = highlightedNodes.contains(nodeId);
        Color fill;
        if (isHighlighted && highlightOverride != null) {
            fill = highlightOverride;
        } else if (isLeaf) {
            fill = LEAF_COLOR;
        } else {
            fill = INTERNAL_COLOR;
        }
        gc.setEffect(new DropShadow(6, 2, 2, Color.rgb(0, 0, 0, 0.3)));
        gc.setFill(fill);
        gc.fillOval(tn.x - NODE_RADIUS, tn.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        gc.setEffect(null);
        gc.setStroke(isHighlighted ? Color.WHITE : fill.darker());
        gc.setLineWidth(isHighlighted ? 3 : 1.5);
        gc.strokeOval(tn.x - NODE_RADIUS, tn.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        gc.setFill(TEXT_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        if (isLeaf && tn.ch != null) {
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.fillText(displayChar(tn.ch), tn.x, tn.y - 6);
            gc.setFont(Font.font("System", 10));
            gc.fillText(String.valueOf(tn.freq), tn.x, tn.y + 8);
        } else {
            gc.setFont(Font.font("System", FontWeight.BOLD, 13));
            gc.fillText(String.valueOf(tn.freq), tn.x, tn.y);
        }
        if (tn.left >= 0) drawNodes(gc, tn.left);
        if (tn.right >= 0) drawNodes(gc, tn.right);
    }

    private int treeDepth(int nodeId) {
        if (nodeId < 0 || nodeId >= nodes.size() || nodes.get(nodeId) == null) return 0;
        TreeNode tn = nodes.get(nodeId);
        return 1 + Math.max(treeDepth(tn.left), treeDepth(tn.right));
    }

    private void stopAllAnimations() {
        for (Timeline t : activeAnimations) t.stop();
        activeAnimations.clear();
    }

    private String displayChar(char c) {
        if (c == ' ') return "SP";
        if (c == '\n') return "NL";
        if (c == '\t') return "TAB";
        return String.valueOf(c);
    }
}
