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

    private static final double CHART_HEIGHT = 220.0;
    private static final double AXIS_Y_POSITION = CHART_HEIGHT / 2;
    private static final double MAX_BAR_HEIGHT = CHART_HEIGHT / 2 - 20;
    private static final double MIN_BAR_WIDTH = 6.0;
    private static final double MAX_BAR_WIDTH = 30.0;
    private static final double CHART_WIDTH = 480.0;

    private final Pane container;
    private Rectangle[] bars;
    private Label[] valueLabels;
    private int[] data;
    private int previousMinIndex = -1;

    public BarChartRenderer() {
        container = new Pane();
        container.setMinHeight(CHART_HEIGHT);
        container.setPrefHeight(CHART_HEIGHT);
        container.setPrefWidth(CHART_WIDTH);
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
        if (valid(i)) bars[i].setFill(Color.GOLD);
        if (valid(j)) bars[j].setFill(Color.GOLD);
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
        if (valid(i)) bars[i].setFill(Color.CRIMSON);
        if (valid(j)) bars[j].setFill(Color.CRIMSON);
    }

    public void markSortedPrefix(int uptoInclusive) {
        for (int k = 0; k <= uptoInclusive && k < bars.length; k++) {
            if (bars[k] != null) {
                bars[k].setFill(Color.FORESTGREEN);
            }
        }
    }

    public Node getNode() { return container; }

    private void rebuild() {
        container.getChildren().clear();
        if (data == null) return;
        int n = data.length;
        bars = new Rectangle[n];
        valueLabels = new Label[n];
        previousMinIndex = -1;

        int maxAbs = 1;
        for (int v : data) maxAbs = Math.max(maxAbs, Math.abs(v));
        double barWidth = Math.max(MIN_BAR_WIDTH, Math.min(MAX_BAR_WIDTH, CHART_WIDTH / Math.max(1, n)));
        double spacing = (CHART_WIDTH - n * barWidth) / (n + 1);

        Line axis = new Line(0, AXIS_Y_POSITION, CHART_WIDTH, AXIS_Y_POSITION);
        axis.setStroke(Color.BLACK);
        container.getChildren().add(axis);

        for (int i = 0; i < n; i++) {
            double h = (Math.abs(data[i]) / (double) maxAbs) * MAX_BAR_HEIGHT;
            Rectangle r = new Rectangle(barWidth, Math.max(2.0, h));
            r.setFill(Color.STEELBLUE);
            r.setStroke(Color.BLACK);
            bars[i] = r;

            Label lbl = new Label(String.valueOf(data[i]));
            lbl.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
            lbl.setPrefWidth(barWidth);
            lbl.setAlignment(Pos.CENTER);
            valueLabels[i] = lbl;

            double x = spacing + i * (barWidth + spacing);
            r.setX(x);
            lbl.setLayoutX(x);

            if (data[i] >= 0) {
                r.setY(AXIS_Y_POSITION - h);
                lbl.setLayoutY(AXIS_Y_POSITION + 5);
            } else {
                r.setY(AXIS_Y_POSITION);
                lbl.setLayoutY(AXIS_Y_POSITION - 15);
            }
            container.getChildren().addAll(r, lbl);
        }
    }

    private boolean valid(int idx) { return bars != null && idx >= 0 && idx < bars.length; }
}
