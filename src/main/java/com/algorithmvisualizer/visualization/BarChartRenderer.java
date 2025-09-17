package com.algorithmvisualizer.visualization;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;

/**
 * Lightweight bar chart for visualizing array values, including negative values.
 */
public class BarChartRenderer {

    private static final double MIN_BAR_WIDTH = 6.0;
    private static final double MAX_BAR_WIDTH = 30.0;
    private static final double PADDING = 20.0;

    private final Pane container;
    private Rectangle[] bars;
    private Label[] valueLabels;
    private int[] data;
    private int previousMinIndex = -1;

    public BarChartRenderer() {
        container = new Pane();
        // Make container expand to fill available space
        container.setMinHeight(100.0);
        container.setPrefHeight(400.0);
        container.setMaxHeight(Double.MAX_VALUE);
        container.setMinWidth(200.0);
        container.setPrefWidth(600.0);
        container.setMaxWidth(Double.MAX_VALUE);
        
        // Add listener to rebuild when container size changes
        container.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds.getWidth() > 0 && newBounds.getHeight() > 0) {
                rebuild();
            }
        });
    }

    public void setData(int[] data) {
        this.data = data == null ? new int[0] : data.clone();
        rebuild();
    }

    public void updateData(int[] data) {
        setData(data);
    }

    public void clearHighlights() {
        if (bars == null) return;
        for (Rectangle r : bars) {
            if (r != null && r.getFill() != Color.FORESTGREEN) {
                r.setFill(Color.STEELBLUE);
                r.setStroke(Color.BLACK);
            }
        }
    }

    public void highlightCompare(int i, int j) {
        if (valid(i) && bars[i].getFill() != Color.FORESTGREEN) bars[i].setFill(Color.GOLD);
        if (valid(j) && bars[j].getFill() != Color.FORESTGREEN) bars[j].setFill(Color.GOLD);
    }

    public void highlightMin(int minIndex) {
        if (valid(previousMinIndex) && previousMinIndex != minIndex) {
            if (bars[previousMinIndex] != null && bars[previousMinIndex].getFill() != Color.FORESTGREEN) {
                bars[previousMinIndex].setFill(Color.STEELBLUE);
            }
        }
        if (!valid(minIndex)) return;
        bars[minIndex].setFill(Color.DARKORANGE);
        previousMinIndex = minIndex;
    }

    public void highlightSwap(int i, int j) {
        if (valid(i) && bars[i].getFill() != Color.FORESTGREEN) bars[i].setFill(Color.RED);
        if (valid(j) && bars[j].getFill() != Color.FORESTGREEN) bars[j].setFill(Color.RED);
    }

    public void markSortedPrefix(int uptoInclusive) {
        for (int k = 0; k <= uptoInclusive && k < bars.length; k++) {
            if (bars[k] != null) {
                bars[k].setFill(Color.FORESTGREEN);
            }
        }
    }

    // Force red highlighting for blinking animation (overrides green)
    public void forceHighlightSwap(int i, int j) {
        if (valid(i)) bars[i].setFill(Color.RED);
        if (valid(j)) bars[j].setFill(Color.RED);
    }

    public Node getNode() { return container; }

    private void rebuild() {
        container.getChildren().clear();
        if (data == null || data.length == 0) return;
        
        // Get current container dimensions
        double containerWidth = container.getWidth();
        double containerHeight = container.getHeight();
        
        // Use minimum dimensions if container hasn't been sized yet
        if (containerWidth <= 0) containerWidth = 600.0;
        if (containerHeight <= 0) containerHeight = 400.0;
        
        int n = data.length;
        bars = new Rectangle[n];
        valueLabels = new Label[n];
        previousMinIndex = -1;

        // Calculate available space for bars
        double chartWidth = containerWidth - 2 * PADDING;
        double chartHeight = containerHeight - 2 * PADDING;
        double axisYPosition = containerHeight / 2;
        double maxBarHeight = chartHeight / 2 - 10; // Leave some margin

        // Find maximum absolute value for scaling
        int maxAbs = 1;
        for (int v : data) maxAbs = Math.max(maxAbs, Math.abs(v));

        // Calculate bar width and spacing
        double barWidth = Math.max(MIN_BAR_WIDTH, Math.min(MAX_BAR_WIDTH, chartWidth / Math.max(1, n)));
        double spacing = (chartWidth - n * barWidth) / (n + 1);

        // Draw horizontal axis
        Line axis = new Line(PADDING, axisYPosition, PADDING + chartWidth, axisYPosition);
        axis.setStroke(Color.BLACK);
        axis.setStrokeWidth(2.0);
        container.getChildren().add(axis);

        // Create bars and labels
        for (int i = 0; i < n; i++) {
            double h = (Math.abs(data[i]) / (double) maxAbs) * maxBarHeight;
            Rectangle r = new Rectangle(barWidth, Math.max(2.0, h));
            r.setFill(Color.STEELBLUE);
            r.setStroke(Color.BLACK);
            bars[i] = r;

            Label lbl = new Label(String.valueOf(data[i]));
            lbl.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
            lbl.setPrefWidth(barWidth);
            lbl.setAlignment(Pos.CENTER);
            valueLabels[i] = lbl;

            // Calculate x position
            double x = PADDING + spacing + i * (barWidth + spacing);
            r.setX(x);
            lbl.setLayoutX(x);

            // Position bars relative to axis (vertically centered)
            if (data[i] >= 0) {
                r.setY(axisYPosition - h);
                lbl.setLayoutY(axisYPosition + 5);
            } else {
                r.setY(axisYPosition);
                lbl.setLayoutY(axisYPosition - 15);
            }
            container.getChildren().addAll(r, lbl);
        }
    }

    private boolean valid(int idx) { return bars != null && idx >= 0 && idx < bars.length; }
}
