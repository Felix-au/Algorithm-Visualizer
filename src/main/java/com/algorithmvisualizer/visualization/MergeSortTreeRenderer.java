package com.algorithmvisualizer.visualization;

import com.algorithmvisualizer.algorithm.MergeSortSolver.TreeNode;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Custom Canvas-based renderer for merge sort tree visualization
 */
public class MergeSortTreeRenderer {

    private static final double NODE_HEIGHT = 35.0;
    private static final double MIN_NODE_WIDTH = 50.0;
    private static final double MAX_NODE_WIDTH = 120.0;
    private static final double LEVEL_SPACING = 70.0;
    private static final double HORIZONTAL_PADDING = 15.0;
    private static final double VERTICAL_PADDING = 20.0;
    
    private final Canvas canvas;
    private TreeNode root;
    private int maxLevel;
    private long lastBlinkTime = 0;
    private boolean blinkState = false;

    public MergeSortTreeRenderer() {
        canvas = new Canvas(600, 400);
        canvas.setStyle("-fx-background-color: white;");
    }

    /**
     * Set the tree root and render
     */
    public void setTree(TreeNode root) {
        this.root = root;
        this.maxLevel = calculateMaxLevel(root);
        calculatePositions();
        render();
    }

    /**
     * Update tree and re-render
     */
    public void updateTree(TreeNode root) {
        setTree(root);
    }

    /**
     * Calculate maximum level in tree
     */
    private int calculateMaxLevel(TreeNode node) {
        if (node == null) return 0;
        if (node.leftChild == null && node.rightChild == null) return node.level;
        return Math.max(calculateMaxLevel(node.leftChild), calculateMaxLevel(node.rightChild));
    }

    /**
     * Calculate positions for all nodes in the tree
     */
    private void calculatePositions() {
        if (root == null) return;
        
        // Count leaf nodes to determine width
        int leafCount = countLeaves(root);
        
        // Calculate node width based on array size
        double nodeWidth = Math.max(MIN_NODE_WIDTH, Math.min(MAX_NODE_WIDTH, 
            (canvas.getWidth() - 2 * HORIZONTAL_PADDING) / leafCount * 0.8));
        
        // Calculate total height needed
        double totalHeight = (maxLevel + 1) * LEVEL_SPACING + 2 * VERTICAL_PADDING;
        
        // Limit canvas height to prevent overgrowth
        double maxHeight = 500.0;
        if (totalHeight > maxHeight) {
            totalHeight = maxHeight;
        }
        
        // Resize canvas if needed
        if (totalHeight > canvas.getHeight()) {
            canvas.setHeight(totalHeight);
        }
        
        // Calculate positions recursively
        double canvasWidth = canvas.getWidth();
        assignPositions(root, HORIZONTAL_PADDING, canvasWidth - HORIZONTAL_PADDING, nodeWidth);
    }

    /**
     * Count leaf nodes in tree
     */
    private int countLeaves(TreeNode node) {
        if (node == null) return 0;
        if (node.leftChild == null && node.rightChild == null) return 1;
        return countLeaves(node.leftChild) + countLeaves(node.rightChild);
    }

    /**
     * Recursively assign positions to nodes
     */
    private void assignPositions(TreeNode node, double leftBound, double rightBound, double nodeWidth) {
        if (node == null) return;
        
        // Set node dimensions
        node.width = nodeWidth;
        node.height = NODE_HEIGHT;
        
        // Calculate y position based on level
        node.y = VERTICAL_PADDING + node.level * LEVEL_SPACING;
        
        if (node.leftChild == null && node.rightChild == null) {
            // Leaf node - center in available space
            node.x = (leftBound + rightBound) / 2;
        } else {
            // Internal node - position based on children
            if (node.leftChild != null && node.rightChild != null) {
                double mid = (leftBound + rightBound) / 2;
                assignPositions(node.leftChild, leftBound, mid, nodeWidth);
                assignPositions(node.rightChild, mid, rightBound, nodeWidth);
                node.x = (node.leftChild.x + node.rightChild.x) / 2;
            } else if (node.leftChild != null) {
                assignPositions(node.leftChild, leftBound, rightBound, nodeWidth);
                node.x = node.leftChild.x;
            } else {
                assignPositions(node.rightChild, leftBound, rightBound, nodeWidth);
                node.x = node.rightChild.x;
            }
        }
    }

    /**
     * Render the tree on canvas
     */
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        if (root == null) {
            // Draw "No data" message
            gc.setFill(Color.GRAY);
            gc.setFont(new Font("Arial", 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No data to display", canvas.getWidth() / 2, canvas.getHeight() / 2);
            return;
        }
        
        // Draw tree (edges first, then nodes)
        drawEdges(gc, root);
        drawNodes(gc, root);
    }

    /**
     * Draw edges between nodes (only for divided nodes)
     */
    private void drawEdges(GraphicsContext gc, TreeNode node) {
        if (node == null || !node.isDivided) return;
        
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(2.0);
        
        // Draw edge to left child
        if (node.leftChild != null) {
            gc.strokeLine(node.x, node.y + node.height / 2,
                         node.leftChild.x, node.leftChild.y - node.leftChild.height / 2);
            drawEdges(gc, node.leftChild);
        }
        
        // Draw edge to right child
        if (node.rightChild != null) {
            gc.strokeLine(node.x, node.y + node.height / 2,
                         node.rightChild.x, node.rightChild.y - node.rightChild.height / 2);
            drawEdges(gc, node.rightChild);
        }
    }

    /**
     * Draw all nodes (only show divided nodes and their children)
     */
    private void drawNodes(GraphicsContext gc, TreeNode node) {
        if (node == null) return;
        
        // Draw this node
        drawNode(gc, node);
        
        // Only draw children if this node has been divided
        if (node.isDivided) {
            if (node.leftChild != null) drawNodes(gc, node.leftChild);
            if (node.rightChild != null) drawNodes(gc, node.rightChild);
        }
    }

    /**
     * Draw a single node
     */
    private void drawNode(GraphicsContext gc, TreeNode node) {
        double x = node.x - node.width / 2;
        double y = node.y - node.height / 2;
        
        // Update blink state every 300ms
        long now = System.currentTimeMillis();
        if (now - lastBlinkTime > 300) {
            blinkState = !blinkState;
            lastBlinkTime = now;
        }
        
        // Determine node color based on state
        Color fillColor = getNodeColor(node);
        
        // If building, apply blink effect
        if (node.isBuilding && blinkState) {
            fillColor = fillColor.brighter();
        }
        
        Color strokeColor = node.isActive ? Color.GOLD : Color.BLACK;
        double strokeWidth = node.isActive ? 3.0 : 1.5;
        
        // Draw node background
        gc.setFill(fillColor);
        gc.fillRoundRect(x, y, node.width, node.height, 10, 10);
        
        // Draw node border
        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth);
        gc.strokeRoundRect(x, y, node.width, node.height, 10, 10);
        
        // Draw array contents - show buildingData if available, otherwise data
        int[] displayData = node.buildingData != null ? node.buildingData : node.data;
        String text = arrayToString(displayData);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Courier New", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, node.x, node.y + 5);
    }

    /**
     * Get color for node based on state
     */
    private Color getNodeColor(TreeNode node) {
        if (node.isBuilding) {
            return Color.GOLD;  // Building (merge in progress)
        }
        if (node.isMerged) {
            return Color.LIGHTGREEN;  // Merged and complete
        }
        if (node.isActive) {
            return Color.LIGHTYELLOW;  // Currently processing
        }
        return Color.LIGHTGRAY;  // Not yet processed
    }

    /**
     * Convert array to string representation
     */
    private String arrayToString(int[] arr) {
        if (arr == null || arr.length == 0) return "[]";
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length && i < 8; i++) {  // Limit to 8 elements for display
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        if (arr.length > 8) {
            sb.append("...");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Clear the canvas
     */
    public void clear() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Get the canvas node for adding to scene
     */
    public Node getNode() {
        return canvas;
    }

    /**
     * Set canvas size
     */
    public void setSize(double width, double height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
        if (root != null) {
            calculatePositions();
            render();
        }
    }

    /**
     * Get canvas width
     */
    public double getWidth() {
        return canvas.getWidth();
    }

    /**
     * Get canvas height
     */
    public double getHeight() {
        return canvas.getHeight();
    }
}
