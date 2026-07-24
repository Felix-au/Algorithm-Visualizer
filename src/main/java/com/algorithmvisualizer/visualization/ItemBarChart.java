package com.algorithmvisualizer.visualization;

import com.algorithmvisualizer.algorithm.FractionalKnapsackSolver.Item;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Bar chart visualization for Fractional Knapsack items with pattern support
 */
public class ItemBarChart {

    private static final double PADDING = 30.0;
    private static final double MIN_BAR_WIDTH = 60.0;
    private static final double MAX_BAR_WIDTH = 100.0;

    private final StackPane rootContainer;
    private final Pane container;
    private Item[] items;
    private Canvas[] barCanvases;
    private VBox[] labelBoxes;
    private int swapIndex1 = -1;
    private int swapIndex2 = -1;
    private int pointerIndex = -1;
    private javafx.scene.shape.Polygon pointerArrow;
    private javafx.scene.layout.StackPane pointerContainer;

    public ItemBarChart() {
        container = new Pane();
        rootContainer = new StackPane(container);
        rootContainer.setMinHeight(100.0);
        rootContainer.setPrefHeight(500.0);
        rootContainer.setMaxHeight(Double.MAX_VALUE);
        rootContainer.setMinWidth(200.0);
        rootContainer.setPrefWidth(700.0);
        rootContainer.setMaxWidth(Double.MAX_VALUE);

        // Create pointer arrow (downward pointing)
        pointerArrow = new javafx.scene.shape.Polygon();
        pointerArrow.getPoints().addAll(
            0.0, 15.0,     // Bottom point
            -10.0, 0.0,    // Top left
            10.0, 0.0      // Top right
        );
        pointerArrow.setFill(Color.web("#FF6B35")); // Orange-red
        pointerArrow.setStroke(Color.BLACK);
        pointerArrow.setStrokeWidth(2.0);
        pointerArrow.setVisible(false);
        
        pointerContainer = new javafx.scene.layout.StackPane(pointerArrow);
        pointerContainer.setVisible(false);
        container.getChildren().add(pointerContainer);

        rootContainer.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds.getWidth() > 0 && newBounds.getHeight() > 0) {
                rebuild();
            }
        });
    }

    public void setData(Item[] items) {
        this.items = items == null ? new Item[0] : items;
        rebuild();
    }

    public void updateData(Item[] items) {
        setData(items);
    }

    public void clearHighlights() {
        swapIndex1 = -1;
        swapIndex2 = -1;
        rebuild();
    }

    public void highlightCompare(int i, int j) {
        swapIndex1 = -1;
        swapIndex2 = -1;
        rebuild();
    }

    public void highlightSwap(int i, int j) {
        swapIndex1 = i;
        swapIndex2 = j;
        rebuild();
    }

    public void highlightCurrent(int idx) {
        swapIndex1 = -1;
        swapIndex2 = -1;
        rebuild();
    }

    public void markTaken(int idx, boolean full) {
        swapIndex1 = -1;
        swapIndex2 = -1;
        rebuild();
    }

    public void showPointer(int idx) {
        pointerIndex = idx;
        updatePointerPosition();
        pointerContainer.setVisible(true);
    }

    public void hidePointer() {
        pointerIndex = -1;
        pointerContainer.setVisible(false);
    }

    private void updatePointerPosition() {
        if (pointerIndex < 0 || pointerIndex >= items.length) {
            pointerContainer.setVisible(false);
            return;
        }

        double containerWidth = rootContainer.getWidth();
        double containerHeight = rootContainer.getHeight();
        if (containerWidth <= 0) containerWidth = 700.0;
        if (containerHeight <= 0) containerHeight = 500.0;

        int n = items.length;
        double chartWidth = containerWidth - 2 * PADDING;
        double barWidth = Math.max(MIN_BAR_WIDTH, Math.min(MAX_BAR_WIDTH, chartWidth / Math.max(1, n) - 10));
        double spacing = (chartWidth - n * barWidth) / (n + 1);

        double x = PADDING + spacing + pointerIndex * (barWidth + spacing) + barWidth / 2;
        double y = PADDING - 20; // Above the bars

        pointerContainer.setLayoutX(x);
        pointerContainer.setLayoutY(y);
        pointerContainer.setVisible(true);
    }

    public Node getNode() {
        return rootContainer;
    }

    private void rebuild() {
        container.getChildren().clear();
        container.getChildren().add(pointerContainer); // Re-add pointer
        
        if (items == null || items.length == 0) {
            barCanvases = new Canvas[0];
            labelBoxes = new VBox[0];
            return;
        }

        double containerWidth = rootContainer.getWidth();
        double containerHeight = rootContainer.getHeight();

        if (containerWidth <= 0) containerWidth = 700.0;
        if (containerHeight <= 0) containerHeight = 500.0;

        int n = items.length;
        barCanvases = new Canvas[n];
        labelBoxes = new VBox[n];

        double chartWidth = containerWidth - 2 * PADDING;
        double chartHeight = containerHeight - 2 * PADDING - 100;
        double barWidth = Math.max(MIN_BAR_WIDTH, Math.min(MAX_BAR_WIDTH, chartWidth / Math.max(1, n) - 10));
        double spacing = (chartWidth - n * barWidth) / (n + 1);

        double maxValue = 1.0;
        for (Item item : items) {
            maxValue = Math.max(maxValue, item.value);
        }

        for (int i = 0; i < n; i++) {
            Item item = items[i];
            double barHeight = (item.value / maxValue) * chartHeight;
            double x = PADDING + spacing + i * (barWidth + spacing);
            double y = PADDING + chartHeight - barHeight;

            // Create canvas for bar with pattern
            Canvas barCanvas = new Canvas(barWidth, barHeight);
            barCanvas.setLayoutX(x);
            barCanvas.setLayoutY(y);
            drawItemBar(barCanvas.getGraphicsContext2D(), barWidth, barHeight, item, i);
            barCanvases[i] = barCanvas;
            container.getChildren().add(barCanvas);

            // Labels
            VBox labelBox = new VBox(3);
            labelBox.setAlignment(Pos.CENTER);
            labelBox.setLayoutX(x);
            labelBox.setLayoutY(PADDING + chartHeight + 10);
            labelBox.setPrefWidth(barWidth);

            Label idLabel = new Label("Item " + item.id);
            idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            Label weightLabel = new Label("W: " + String.format("%.1f", item.weight));
            weightLabel.setStyle("-fx-font-size: 10px;");

            Label valueLabel = new Label("V: " + String.format("%.1f", item.value));
            valueLabel.setStyle("-fx-font-size: 10px;");

            Label ratioLabel = new Label("R: " + String.format("%.2f", item.ratio));
            ratioLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #FF8C00;");

            labelBox.getChildren().addAll(idLabel, weightLabel, valueLabel, ratioLabel);

            if (item.fractionTaken > 0 && item.fractionTaken < 0.9999) {
                Label fracLabel = new Label(String.format("%.1f%%", item.fractionTaken * 100));
                fracLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");
                labelBox.getChildren().add(fracLabel);
            }

            labelBoxes[i] = labelBox;
            container.getChildren().add(labelBox);
        }
        
        // Update pointer position if visible
        if (pointerIndex >= 0) {
            updatePointerPosition();
        }
    }

    private void drawItemBar(GraphicsContext gc, double width, double height, Item item, int itemIndex) {
        // Get base color and pattern - START FROM PATTERN 1 (not 0) so Item 1 isn't solid
        Color baseColor = getItemColor(item.id);
        int patternType = ((item.id - 1) % 6) + 1; // Patterns 1-6, cycling
        if (patternType > 5) patternType = 1; // Wrap around to pattern 1
        
        // Determine bar color based on state
        Color barColor = baseColor;
        
        // Check for swap highlight first (RED - highest priority)
        if (itemIndex == swapIndex1 || itemIndex == swapIndex2) {
            barColor = Color.web("#FF0000"); // Bright red for swap
        }
        // No highlighting for taken items - they keep their base color
        
        // Draw pattern
        drawPattern(gc, 0, 0, width, height, patternType, barColor);
        
        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.strokeRect(0, 0, width, height);
    }
    
    private void drawPattern(GraphicsContext gc, double x, double y, double width, double height, int patternType, Color color) {
        switch (patternType) {
            case 0: // Solid
                gc.setFill(color);
                gc.fillRect(x, y, width, height);
                break;
                
            case 1: // Diagonal stripes
                gc.setFill(color);
                gc.fillRect(x, y, width, height);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                for (double i = -height; i < width + height; i += 8) {
                    gc.strokeLine(x + i, y, x + i + height, y + height);
                }
                break;
                
            case 2: // Dots
                gc.setFill(color);
                gc.fillRect(x, y, width, height);
                gc.setFill(Color.WHITE);
                for (double dy = 4; dy < height; dy += 8) {
                    for (double dx = 4; dx < width; dx += 8) {
                        gc.fillOval(x + dx - 2, y + dy - 2, 4, 4);
                    }
                }
                break;
                
            case 3: // Cross-hatch
                gc.setFill(color);
                gc.fillRect(x, y, width, height);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                for (double i = -height; i < width + height; i += 6) {
                    gc.strokeLine(x + i, y, x + i + height, y + height);
                    gc.strokeLine(x + i, y + height, x + i + height, y);
                }
                break;
                
            case 4: // Checkered
                gc.setFill(color);
                gc.fillRect(x, y, width, height);
                gc.setFill(Color.WHITE);
                for (double dy = 0; dy < height; dy += 8) {
                    for (double dx = 0; dx < width; dx += 8) {
                        if (((int)(dx / 8) + (int)(dy / 8)) % 2 == 0) {
                            gc.fillRect(x + dx, y + dy, 8, 8);
                        }
                    }
                }
                break;
                
            case 5: // Horizontal stripes
                gc.setFill(color);
                gc.fillRect(x, y, width, height);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                for (double dy = 0; dy < height; dy += 8) {
                    gc.strokeLine(x, y + dy, x + width, y + dy);
                }
                break;
        }
    }
    
    private Color getItemColor(int itemId) {
        Color[] colors = {
            Color.web("#4A90E2"), // Blue
            Color.web("#E74C3C"), // Red
            Color.web("#2ECC71"), // Green
            Color.web("#F39C12"), // Orange
            Color.web("#9B59B6"), // Purple
            Color.web("#1ABC9C")  // Teal
        };
        return colors[(itemId - 1) % colors.length];
    }
}
