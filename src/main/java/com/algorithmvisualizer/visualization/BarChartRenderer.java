package com.algorithmvisualizer.visualization;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;

/**
 * Lightweight bar chart for visualizing array values, including negative values.
 * Now supports pointer overlays for algorithm visualization.
 */
public class BarChartRenderer {

    private static final double MIN_BAR_WIDTH = 6.0;
    private static final double MAX_BAR_WIDTH = 30.0;
    private static final double PADDING = 20.0;

    private final StackPane rootContainer;
    private final Pane container;
    private final PointerOverlay pointerOverlay;
    private Rectangle[] bars;
    private Label[] valueLabels;
    private int[] data;
    private int previousMinIndex = -1;
    
    // Bar position tracking for pointer overlay
    private double[] barXPositions;
    private double[] barYPositions;
    private double[] barWidths;

    public BarChartRenderer() {
        container = new Pane();
        pointerOverlay = new PointerOverlay();
        
        // Stack container and pointer overlay
        rootContainer = new StackPane();
        rootContainer.getChildren().addAll(container, pointerOverlay.getContainer());
        
        // Make container expand to fill available space
        rootContainer.setMinHeight(100.0);
        rootContainer.setPrefHeight(400.0);
        rootContainer.setMaxHeight(Double.MAX_VALUE);
        rootContainer.setMinWidth(200.0);
        rootContainer.setPrefWidth(600.0);
        rootContainer.setMaxWidth(Double.MAX_VALUE);
        
        // Setup position calculator for pointer overlay
        pointerOverlay.setPositionCalculator(new PointerOverlay.PositionCalculator() {
            @Override
            public double getElementX(int index) {
                return barXPositions != null && index >= 0 && index < barXPositions.length ? 
                    barXPositions[index] : 0;
            }
            
            @Override
            public double getElementY(int index) {
                return barYPositions != null && index >= 0 && index < barYPositions.length ? 
                    barYPositions[index] : 0;
            }
            
            @Override
            public double getElementWidth(int index) {
                return barWidths != null && index >= 0 && index < barWidths.length ? 
                    barWidths[index] : 0;
            }
        });
        
        // Add listener to rebuild when container size changes
        rootContainer.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
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
        // Do not override a bar that's already marked as sorted
        if (bars[minIndex] != null && bars[minIndex].getFill() == Color.FORESTGREEN) {
            previousMinIndex = minIndex;
            return;
        }
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

    // Mark all elements from 'fromIndex' to end as sorted (for bubble sort suffix)
    public void markSortedSuffix(int fromIndex) {
        if (bars == null) return;
        for (int k = Math.max(0, fromIndex); k < bars.length; k++) {
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
    
    public void setIndexColor(int idx, Color color) {
        if (!valid(idx)) return;
        bars[idx].setFill(color);
    }

    public Node getNode() { return rootContainer; }
    
    // ===== Pointer Management Methods =====
    
    /**
     * Show a pointer at the specified index
     */
    public void showPointer(String name, int index, Color color, boolean positionBelow) {
        if (index >= 0 && index < (data != null ? data.length : 0)) {
            pointerOverlay.showPointer(name, index, color, positionBelow);
        }
    }
    
    /**
     * Move pointer to new index with animation
     */
    public void animatePointerMove(String name, int fromIndex, int toIndex) {
        if (toIndex >= 0 && toIndex < (data != null ? data.length : 0)) {
            pointerOverlay.setPointerPosition(name, toIndex, true);
        }
    }
    
    /**
     * Set pointer position without animation
     */
    public void setPointerPosition(String name, int index) {
        if (index >= 0 && index < (data != null ? data.length : 0)) {
            pointerOverlay.setPointerPosition(name, index, false);
        }
    }
    
    /**
     * Hide a specific pointer
     */
    public void hidePointer(String name) {
        pointerOverlay.hidePointer(name);
    }
    
    /**
     * Clear all pointers
     */
    public void clearAllPointers() {
        pointerOverlay.clearAllPointers();
    }
    
    /**
     * Update pointer positions after data change
     */
    public void updatePointerPositions() {
        pointerOverlay.updateAllPositions();
    }
    
    /**
     * Cancel all active pointer animations
     */
    public void cancelPointerAnimations() {
        pointerOverlay.cancelAllAnimations();
    }

    // ---- Additional helpers for Binary Search visualization ----
    public void highlightMid(int idx) {
        if (!valid(idx)) return;
        if (bars[idx].getFill() == Color.FORESTGREEN) return; // do not override found
        bars[idx].setFill(Color.GOLD);
    }

    public void markFound(int idx) {
        if (!valid(idx)) return;
        bars[idx].setFill(Color.FORESTGREEN);
    }

    public void markEliminatedRange(int fromInclusive, int toInclusive) {
        if (bars == null) return;
        int from = Math.max(0, fromInclusive);
        int to = Math.min(bars.length - 1, toInclusive);
        if (from > to) return;
        for (int k = from; k <= to; k++) {
            if (bars[k] != null && bars[k].getFill() != Color.FORESTGREEN) {
                bars[k].setFill(Color.RED);
            }
        }
    }

    public void setRangeColor(int fromInclusive, int toInclusive, Color color) {
        if (bars == null) return;
        int from = Math.max(0, fromInclusive);
        int to = Math.min(bars.length - 1, toInclusive);
        if (from > to) return;
        for (int k = from; k <= to; k++) {
            if (bars[k] != null) {
                bars[k].setFill(color);
            }
        }
    }

    private void rebuild() {
        container.getChildren().clear();
        if (data == null || data.length == 0) {
            barXPositions = new double[0];
            barYPositions = new double[0];
            barWidths = new double[0];
            return;
        }
        
        // Get current container dimensions
        double containerWidth = rootContainer.getWidth();
        double containerHeight = rootContainer.getHeight();
        
        // Use minimum dimensions if container hasn't been sized yet
        if (containerWidth <= 0) containerWidth = 600.0;
        if (containerHeight <= 0) containerHeight = 400.0;
        
        int n = data.length;
        bars = new Rectangle[n];
        valueLabels = new Label[n];
        barXPositions = new double[n];
        barYPositions = new double[n];
        barWidths = new double[n];
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
            
            // Store positions for pointer overlay
            barXPositions[i] = x;
            barWidths[i] = barWidth;

            // Position bars relative to axis (vertically centered)
            if (data[i] >= 0) {
                r.setY(axisYPosition - h);
                barYPositions[i] = axisYPosition - h;
                lbl.setLayoutY(axisYPosition + 5);
            } else {
                r.setY(axisYPosition);
                barYPositions[i] = axisYPosition;
                lbl.setLayoutY(axisYPosition - 15);
            }
            container.getChildren().addAll(r, lbl);
        }
        
        // Update pointer positions after rebuild
        pointerOverlay.updateAllPositions();
    }

    private boolean valid(int idx) { return bars != null && idx >= 0 && idx < bars.length; }
    
    // ---- Additional helpers for Linear Search visualization ----
    public void highlightChecking(int idx) {
        if (!valid(idx)) return;
        if (bars[idx].getFill() == Color.FORESTGREEN) return; // don't override found
        bars[idx].setFill(Color.GOLD); // YELLOW for checking
    }
    
    public void markEliminated(int idx) {
        if (!valid(idx)) return;
        if (bars[idx].getFill() == Color.FORESTGREEN) return; // don't override found
        bars[idx].setFill(Color.RED); // RED for not target
    }
}
