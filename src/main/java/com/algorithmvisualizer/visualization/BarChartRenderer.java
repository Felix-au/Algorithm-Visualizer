package com.algorithmvisualizer.visualization;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Lightweight bar chart for visualizing array values.
 */
public class BarChartRenderer {

    private static final double MAX_BAR_HEIGHT = 180.0;
    private static final double MIN_BAR_WIDTH = 6.0;
    private static final double MAX_BAR_WIDTH = 30.0;

    private final HBox container;
    private Rectangle[] bars;
    private Label[] valueLabels;
    private int[] data;

    public BarChartRenderer() {
        container = new HBox(4.0);
        container.setAlignment(Pos.BOTTOM_CENTER);
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
            r.setFill(Color.STEELBLUE);
            r.setStroke(Color.BLACK);
        }
    }

    public void highlightCompare(int i, int j) {
        clearHighlights();
        if (valid(i)) bars[i].setFill(Color.GOLD);
        if (valid(j)) bars[j].setFill(Color.GOLD);
    }

    public void highlightMin(int minIndex) {
        if (!valid(minIndex)) return;
        bars[minIndex].setFill(Color.DARKORANGE);
    }

    public void highlightSwap(int i, int j) {
        if (valid(i)) bars[i].setFill(Color.CRIMSON);
        if (valid(j)) bars[j].setFill(Color.CRIMSON);
    }

    public void markSortedPrefix(int uptoInclusive) {
        for (int k = 0; k <= uptoInclusive && k < bars.length; k++) {
            bars[k].setFill(Color.FORESTGREEN);
        }
    }

    public HBox getNode() { return container; }

    private void rebuild() {
        container.getChildren().clear();
        if (data == null) return;
        int n = data.length;
        bars = new Rectangle[n];
        valueLabels = new Label[n];

        int maxAbs = 1;
        for (int v : data) maxAbs = Math.max(maxAbs, Math.abs(v));
        double barWidth = Math.max(MIN_BAR_WIDTH, Math.min(MAX_BAR_WIDTH, 480.0 / Math.max(1, n)));

        for (int i = 0; i < n; i++) {
            double h = (Math.abs(data[i]) / (double) maxAbs) * MAX_BAR_HEIGHT;
            Rectangle r = new Rectangle(barWidth, Math.max(2.0, h));
            r.setFill(Color.STEELBLUE);
            r.setStroke(Color.BLACK);
            bars[i] = r;

            Label lbl = new Label(String.valueOf(data[i]));
            lbl.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
            valueLabels[i] = lbl;

            VBox barBox = new VBox(4.0);
            barBox.setAlignment(Pos.BOTTOM_CENTER);
            barBox.getChildren().addAll(r, lbl);
            container.getChildren().add(barBox);
        }
    }

    private boolean valid(int idx) { return bars != null && idx >= 0 && idx < bars.length; }
}


