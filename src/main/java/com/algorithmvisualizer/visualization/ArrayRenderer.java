package com.algorithmvisualizer.visualization;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Renders an array as boxes with index and value.
 * Now supports pointer overlays for algorithm visualization.
 */
public class ArrayRenderer {

    private final StackPane rootContainer;
    private final FlowPane container;
    private final PointerOverlay pointerOverlay;
    private VBox[] cells;
    private int[] data;
    private int previousMinIndex = -1;
    
    // Cell position tracking for pointer overlay
    private double[] cellXPositions;
    private double[] cellYPositions;
    private double[] cellWidths;

    public ArrayRenderer() {
        container = new FlowPane(6.0, 6.0);
        container.setAlignment(Pos.CENTER);
        container.setPrefWrapLength(400.0); // Set preferred wrap length
        
        pointerOverlay = new PointerOverlay();
        
        // Stack container and pointer overlay
        rootContainer = new StackPane();
        rootContainer.getChildren().addAll(container, pointerOverlay.getContainer());
        
        // Setup position calculator for pointer overlay
        pointerOverlay.setPositionCalculator(new PointerOverlay.PositionCalculator() {
            @Override
            public double getElementX(int index) {
                return cellXPositions != null && index >= 0 && index < cellXPositions.length ? 
                    cellXPositions[index] : 0;
            }
            
            @Override
            public double getElementY(int index) {
                return cellYPositions != null && index >= 0 && index < cellYPositions.length ? 
                    cellYPositions[index] : 0;
            }
            
            @Override
            public double getElementWidth(int index) {
                return cellWidths != null && index >= 0 && index < cellWidths.length ? 
                    cellWidths[index] : 0;
            }
        });
        
        // Update cell positions after layout
        container.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            updateCellPositions();
        });
    }
    
    public StackPane getNode() { return rootContainer; }

    public void setData(int[] data) {
        this.data = data == null ? new int[0] : data.clone();
        rebuild();
    }

    public void updateData(int[] data) { setData(data); }

    public void clearHighlights() {
        if (cells == null) return;
        for (int i = 0; i < cells.length; i++) {
            if (!cells[i].getStyle().contains("FORESTGREEN")) {
                cells[i].setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #333; -fx-padding: 6;");
            }
        }
    }

    public void highlightCompare(int i, int j) {
        if (valid(i) && !cells[i].getStyle().contains("FORESTGREEN")) {
            cells[i].setStyle("-fx-background-color: GOLD; -fx-border-color: #333; -fx-padding: 6;");
        }
        if (valid(j) && !cells[j].getStyle().contains("FORESTGREEN")) {
            cells[j].setStyle("-fx-background-color: GOLD; -fx-border-color: #333; -fx-padding: 6;");
        }
    }

    public void highlightMin(int minIndex) {
        if (valid(previousMinIndex) && previousMinIndex != minIndex) {
            if (!cells[previousMinIndex].getStyle().contains("FORESTGREEN")) {
                cells[previousMinIndex].setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #333; -fx-padding: 6;");
            }
        }
        if (!valid(minIndex)) return;
        cells[minIndex].setStyle("-fx-background-color: DARKORANGE; -fx-border-color: #333; -fx-padding: 6;");
        previousMinIndex = minIndex;
    }

    public void highlightSwap(int i, int j) {
        if (valid(i) && !cells[i].getStyle().contains("FORESTGREEN")) {
            cells[i].setStyle("-fx-background-color: RED; -fx-border-color: #333; -fx-padding: 6;");
        }
        if (valid(j) && !cells[j].getStyle().contains("FORESTGREEN")) {
            cells[j].setStyle("-fx-background-color: RED; -fx-border-color: #333; -fx-padding: 6;");
        }
    }

    public void markSortedPrefix(int uptoInclusive) {
        for (int k = 0; k <= uptoInclusive && k < cells.length; k++) {
            cells[k].setStyle("-fx-background-color: FORESTGREEN; -fx-border-color: #333; -fx-padding: 6;");
        }
    }

    // Mark all elements from 'fromIndex' to end as sorted (for bubble sort suffix)
    public void markSortedSuffix(int fromIndex) {
        if (cells == null) return;
        for (int k = Math.max(0, fromIndex); k < cells.length; k++) {
            cells[k].setStyle("-fx-background-color: FORESTGREEN; -fx-border-color: #333; -fx-padding: 6;");
        }
    }

    // Force red highlighting for blinking animation (overrides green)
    public void forceHighlightSwap(int i, int j) {
        if (valid(i)) cells[i].setStyle("-fx-background-color: RED; -fx-border-color: #333; -fx-padding: 6;");
        if (valid(j)) cells[j].setStyle("-fx-background-color: RED; -fx-border-color: #333; -fx-padding: 6;");
    }

    // ---- Additional helpers for Binary Search visualization ----
    public void highlightMid(int idx) {
        if (!valid(idx)) return;
        if (cells[idx].getStyle().contains("FORESTGREEN")) return; // don't override found
        cells[idx].setStyle("-fx-background-color: GOLD; -fx-border-color: #333; -fx-padding: 6;");
    }

    public void markFound(int idx) {
        if (!valid(idx)) return;
        cells[idx].setStyle("-fx-background-color: FORESTGREEN; -fx-border-color: #333; -fx-padding: 6;");
    }

    public void markEliminatedRange(int fromInclusive, int toInclusive) {
        if (cells == null) return;
        int from = Math.max(0, fromInclusive);
        int to = Math.min(cells.length - 1, toInclusive);
        if (from > to) return;
        for (int k = from; k <= to; k++) {
            cells[k].setStyle("-fx-background-color: RED; -fx-border-color: #333; -fx-padding: 6;");
        }
    }

    public void setIndexColor(int idx, String cssColorName) {
        if (!valid(idx)) return;
        cells[idx].setStyle("-fx-background-color: " + cssColorName + "; -fx-border-color: #333; -fx-padding: 6;");
    }

    public void setRangeColor(int fromInclusive, int toInclusive, String cssColorName) {
        if (cells == null) return;
        int from = Math.max(0, fromInclusive);
        int to = Math.min(cells.length - 1, toInclusive);
        if (from > to) return;
        for (int k = from; k <= to; k++) {
            cells[k].setStyle("-fx-background-color: " + cssColorName + "; -fx-border-color: #333; -fx-padding: 6;");
        }
    }
    
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
        updateCellPositions();
        pointerOverlay.updateAllPositions();
    }
    
    /**
     * Cancel all active pointer animations
     */
    public void cancelPointerAnimations() {
        pointerOverlay.cancelAllAnimations();
    }

    private void rebuild() {
        container.getChildren().clear();
        if (data == null) {
            cellXPositions = new double[0];
            cellYPositions = new double[0];
            cellWidths = new double[0];
            return;
        }
        int n = data.length;
        cells = new VBox[n];
        cellXPositions = new double[n];
        cellYPositions = new double[n];
        cellWidths = new double[n];
        previousMinIndex = -1;
        for (int i = 0; i < n; i++) {
            VBox cell = new VBox(4.0);
            cell.setAlignment(Pos.CENTER);
            cell.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #333; -fx-padding: 6;");
            Label value = new Label(String.valueOf(data[i]));
            value.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");
            cell.getChildren().addAll(value);
            cells[i] = cell;
            container.getChildren().add(cell);
        }
        
        // Update cell positions after layout
        javafx.application.Platform.runLater(this::updateCellPositions);
    }
    
    /**
     * Update cell position tracking for pointer overlay
     */
    private void updateCellPositions() {
        if (cells == null) return;
        
        for (int i = 0; i < cells.length; i++) {
            VBox cell = cells[i];
            if (cell != null) {
                // Get cell bounds in parent coordinates
                javafx.geometry.Bounds boundsInParent = cell.localToParent(cell.getBoundsInLocal());
                cellXPositions[i] = boundsInParent.getMinX();
                cellYPositions[i] = boundsInParent.getMinY();
                cellWidths[i] = boundsInParent.getWidth();
            }
        }
    }

    private boolean valid(int idx) { return cells != null && idx >= 0 && idx < cells.length; }
    
    // ---- Additional helpers for Linear Search visualization ----
    public void highlightChecking(int idx) {
        if (!valid(idx)) return;
        if (cells[idx].getStyle().contains("FORESTGREEN")) return; // don't override found
        cells[idx].setStyle("-fx-background-color: GOLD; -fx-border-color: #333; -fx-padding: 6;");
    }
    
    public void markEliminated(int idx) {
        if (!valid(idx)) return;
        if (cells[idx].getStyle().contains("FORESTGREEN")) return; // don't override found
        cells[idx].setStyle("-fx-background-color: RED; -fx-border-color: #333; -fx-padding: 6;");
    }
}


