package com.algorithmvisualizer.visualization;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Renders an array as boxes with index and value.
 */
public class ArrayRenderer {

    private final HBox container;
    private VBox[] cells;
    private int[] data;

    private int previousMinIndex = -1;

    public ArrayRenderer() {
        container = new HBox(6.0);
        container.setAlignment(Pos.CENTER);
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
        if (valid(i)) cells[i].setStyle("-fx-background-color: GOLD; -fx-border-color: #333; -fx-padding: 6;");
        if (valid(j)) cells[j].setStyle("-fx-background-color: GOLD; -fx-border-color: #333; -fx-padding: 6;");
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
        if (valid(i)) cells[i].setStyle("-fx-background-color: CRIMSON; -fx-border-color: #333; -fx-padding: 6;");
        if (valid(j)) cells[j].setStyle("-fx-background-color: CRIMSON; -fx-border-color: #333; -fx-padding: 6;");
    }

    public void markSortedPrefix(int uptoInclusive) {
        for (int k = 0; k <= uptoInclusive && k < cells.length; k++) {
            cells[k].setStyle("-fx-background-color: FORESTGREEN; -fx-border-color: #333; -fx-padding: 6;");
        }
    }

    public HBox getNode() { return container; }

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


