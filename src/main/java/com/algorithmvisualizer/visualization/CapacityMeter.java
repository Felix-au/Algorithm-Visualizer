package com.algorithmvisualizer.visualization;

import com.algorithmvisualizer.algorithm.FractionalKnapsackSolver.Item;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Capacity meter visualization for Fractional Knapsack with pattern support
 */
public class CapacityMeter {

    private final VBox container;
    private final Rectangle background;
    private final Canvas patternCanvas;
    private final Label capacityLabel;
    private final Label valueLabel;
    private double capacity;
    private double used;
    private double totalValue;
    private List<ItemSegment> segments = new ArrayList<>();
    private Rectangle blinkRect;
    private javafx.animation.Timeline blinkTimeline;

    /**
     * Represents a segment of the capacity meter for one item
     */
    private static class ItemSegment {
        Item item;
        double fraction;
        
        ItemSegment(Item item, double fraction) {
            this.item = item;
            this.fraction = fraction;
        }
    }

    public CapacityMeter() {
        container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 2;");

        // Title
        Label title = new Label("🎒 Knapsack Status");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Capacity bar container
        StackPane barContainer = new StackPane();
        barContainer.setPrefWidth(400);
        barContainer.setPrefHeight(40);

        // Background bar
        background = new Rectangle(400, 40);
        background.setFill(Color.web("#E0E0E0"));
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(2.0);
        background.setArcWidth(8);
        background.setArcHeight(8);

        // Pattern canvas for items
        patternCanvas = new Canvas(400, 40);

        // Blink rectangle (initially hidden)
        blinkRect = new javafx.scene.shape.Rectangle(0, 0);
        blinkRect.setHeight(40);
        blinkRect.setFill(Color.web("#FFD700", 0.6)); // Semi-transparent gold
        blinkRect.setStroke(Color.web("#FF8C00"));
        blinkRect.setStrokeWidth(3.0);
        blinkRect.setVisible(false);

        barContainer.getChildren().addAll(background, patternCanvas, blinkRect);
        StackPane.setAlignment(patternCanvas, Pos.CENTER_LEFT);
        StackPane.setAlignment(blinkRect, Pos.CENTER_LEFT);

        // Capacity label
        capacityLabel = new Label("Capacity: 0.0 / 0.0 (0%)");
        capacityLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        // Value label
        valueLabel = new Label("Total Value: $0.00");
        valueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27AE60;");

        container.getChildren().addAll(title, barContainer, capacityLabel, valueLabel);
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
        updateDisplay();
    }

    public void setUsed(double used) {
        this.used = used;
        updateDisplay();
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
        updateDisplay();
    }

    public void update(double capacity, double used, double totalValue) {
        this.capacity = capacity;
        this.used = used;
        this.totalValue = totalValue;
        updateDisplay();
    }
    
    public void updateWithItems(double capacity, double used, double totalValue, Item[] items) {
        this.capacity = capacity;
        this.used = used;
        this.totalValue = totalValue;
        
        // Build segments from items that are taken
        segments.clear();
        for (Item item : items) {
            if (item.fractionTaken > 0) {
                segments.add(new ItemSegment(item, item.fractionTaken));
            }
        }
        
        updateDisplay();
    }

    private void updateDisplay() {
        // Update labels
        double percentage = capacity > 0 ? (used / capacity) : 0;
        percentage = Math.min(1.0, Math.max(0.0, percentage));
        
        capacityLabel.setText(String.format("Capacity: %.1f / %.1f (%.1f%%)", 
            used, capacity, percentage * 100));
        valueLabel.setText(String.format("Total Value: $%.2f", totalValue));
        
        // Draw patterns
        drawPatterns();
    }
    
    private void drawPatterns() {
        GraphicsContext gc = patternCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 400, 40);
        
        if (segments.isEmpty() || capacity <= 0) return;
        
        double x = 0;
        for (ItemSegment segment : segments) {
            double segmentWidth = (segment.item.weight * segment.fraction / capacity) * 400;
            segmentWidth = Math.min(segmentWidth, 400 - x);
            
            if (segmentWidth > 0) {
                drawItemPattern(gc, x, 0, segmentWidth, 40, segment.item.id);
                x += segmentWidth;
            }
        }
    }
    
    private void drawItemPattern(GraphicsContext gc, double x, double y, double width, double height, int itemId) {
        // Clip to rounded rectangle
        gc.save();
        gc.beginPath();
        gc.rect(x, y, width, height);
        gc.clip();
        
        // START FROM PATTERN 1 (not 0) so Item 1 isn't solid
        int patternType = ((itemId - 1) % 6) + 1; // Patterns 1-6, cycling
        if (patternType > 5) patternType = 1; // Wrap around to pattern 1
        Color baseColor = getItemColor(itemId);
        
        switch (patternType) {
            case 0: // Solid
                gc.setFill(baseColor);
                gc.fillRect(x, y, width, height);
                break;
                
            case 1: // Diagonal stripes
                gc.setFill(baseColor);
                gc.fillRect(x, y, width, height);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                for (double i = -height; i < width + height; i += 8) {
                    gc.strokeLine(x + i, y, x + i + height, y + height);
                }
                break;
                
            case 2: // Dots
                gc.setFill(baseColor);
                gc.fillRect(x, y, width, height);
                gc.setFill(Color.WHITE);
                for (double dy = 4; dy < height; dy += 8) {
                    for (double dx = 4; dx < width; dx += 8) {
                        gc.fillOval(x + dx - 2, y + dy - 2, 4, 4);
                    }
                }
                break;
                
            case 3: // Cross-hatch
                gc.setFill(baseColor);
                gc.fillRect(x, y, width, height);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                for (double i = -height; i < width + height; i += 6) {
                    gc.strokeLine(x + i, y, x + i + height, y + height);
                    gc.strokeLine(x + i, y + height, x + i + height, y);
                }
                break;
                
            case 4: // Checkered
                gc.setFill(baseColor);
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
                gc.setFill(baseColor);
                gc.fillRect(x, y, width, height);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
                for (double dy = 0; dy < height; dy += 8) {
                    gc.strokeLine(x, y + dy, x + width, y + dy);
                }
                break;
        }
        
        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
        
        gc.restore();
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

    public void blinkArea(double startPos, double endPos, double totalCapacity) {
        if (blinkTimeline != null) {
            blinkTimeline.stop();
        }
        
        double startX = (startPos / totalCapacity) * 400;
        double width = ((endPos - startPos) / totalCapacity) * 400;
        
        // Set position and size
        blinkRect.setX(startX);
        blinkRect.setY(0);
        blinkRect.setWidth(width);
        blinkRect.setHeight(40);
        blinkRect.setVisible(true);
        
        // Blink slower - 5 times over 2 seconds
        blinkTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.millis(0), e -> blinkRect.setVisible(true)),
            new javafx.animation.KeyFrame(Duration.millis(200), e -> blinkRect.setVisible(false)),
            new javafx.animation.KeyFrame(Duration.millis(400), e -> blinkRect.setVisible(true)),
            new javafx.animation.KeyFrame(Duration.millis(600), e -> blinkRect.setVisible(false)),
            new javafx.animation.KeyFrame(Duration.millis(800), e -> blinkRect.setVisible(true)),
            new javafx.animation.KeyFrame(Duration.millis(1000), e -> blinkRect.setVisible(false)),
            new javafx.animation.KeyFrame(Duration.millis(1200), e -> blinkRect.setVisible(true)),
            new javafx.animation.KeyFrame(Duration.millis(1400), e -> blinkRect.setVisible(false)),
            new javafx.animation.KeyFrame(Duration.millis(1600), e -> blinkRect.setVisible(true)),
            new javafx.animation.KeyFrame(Duration.millis(1800), e -> blinkRect.setVisible(false)),
            new javafx.animation.KeyFrame(Duration.millis(2000), e -> blinkRect.setVisible(false))
        );
        blinkTimeline.play();
    }
    
    public void clearBlink() {
        if (blinkTimeline != null) {
            blinkTimeline.stop();
        }
        blinkRect.setVisible(false);
    }

    public Node getNode() {
        return container;
    }
}
