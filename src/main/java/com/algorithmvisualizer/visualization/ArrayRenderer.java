package com.algorithmvisualizer.visualization;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Renders an array as boxes with index and value.
 */
public class ArrayRenderer {

    private final FlowPane container;
    private VBox[] cells;
    private int[] data;

    private int previousMinIndex = -1;

    public ArrayRenderer() {
        container = new FlowPane(6.0, 6.0);
        container.setAlignment(Pos.CENTER);
        container.setPrefWrapLength(400.0); // Set preferred wrap length
    }

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

    public FlowPane getNode() { return container; }

    private void rebuild() {
        container.getChildren().clear();
        if (data == null) return;
        int n = data.length;
        cells = new VBox[n];
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
    }

    private boolean valid(int idx) { return cells != null && idx >= 0 && idx < cells.length; }
}


