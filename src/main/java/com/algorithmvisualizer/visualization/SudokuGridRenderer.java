package com.algorithmvisualizer.visualization;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class SudokuGridRenderer {
    private final Pane container;
    private final Rectangle[][] cells = new Rectangle[9][9];
    private final Text[][] labels = new Text[9][9];
    private final boolean[][] fixed = new boolean[9][9];

    public SudokuGridRenderer() {
        container = new Pane();
        container.setMinSize(360, 360);
        container.setPrefSize(540, 540);
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        container.layoutBoundsProperty().addListener((obs, o, n) -> layout());
        build();
    }

    public Node getNode() { return container; }

    public void setGrid(int[][] grid, boolean[][] fixedMask) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int v = grid[r][c];
                labels[r][c].setText(v == 0 ? "" : String.valueOf(v));
                fixed[r][c] = fixedMask != null && fixedMask[r][c];
                if (fixed[r][c]) {
                    cells[r][c].setFill(Color.LIGHTGRAY);
                    labels[r][c].setFill(Color.BLACK);
                } else {
                    cells[r][c].setFill(Color.WHITE);
                    labels[r][c].setFill(Color.DARKBLUE);
                }
            }
        }
    }

    public void setCell(int r, int c, int v, boolean isFixed) {
        labels[r][c].setText(v == 0 ? "" : String.valueOf(v));
        fixed[r][c] = isFixed;
        if (fixed[r][c]) {
            cells[r][c].setFill(Color.LIGHTGRAY);
            labels[r][c].setFill(Color.BLACK);
        } else {
            cells[r][c].setFill(Color.WHITE);
            labels[r][c].setFill(Color.DARKBLUE);
        }
    }

    public void clearHighlights() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (fixed[r][c]) {
                    cells[r][c].setFill(Color.LIGHTGRAY);
                } else {
                    cells[r][c].setFill(Color.WHITE);
                }
                cells[r][c].setStroke(Color.BLACK);
                cells[r][c].setStrokeWidth(1.0);
            }
        }
    }

    public void highlightCurrent(int r, int c) {
        cells[r][c].setFill(Color.GOLD);
    }

    public void markConflict(int r, int c) {
        cells[r][c].setFill(Color.SALMON);
    }

    public void flashBacktrack(int r, int c) {
        // Simple visual cue: border goes red and thick briefly
        cells[r][c].setStroke(Color.CRIMSON);
        cells[r][c].setStrokeWidth(3.0);
    }

    private void build() {
        container.getChildren().clear();
        // Create cells and labels
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Rectangle rect = new Rectangle();
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(1.0);
                Text t = new Text("");
                t.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
                cells[r][c] = rect;
                labels[r][c] = t;
                container.getChildren().addAll(rect, t);
            }
        }
        // Draw thick lines for 3x3 boxes (on top)
        layout();
    }

    private void layout() {
        double w = Math.max(360, container.getWidth());
        double h = Math.max(360, container.getHeight());
        double size = Math.min(w, h);
        double cell = size / 9.0;
        double x0 = (w - size) / 2.0;
        double y0 = (h - size) / 2.0;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Rectangle rect = cells[r][c];
                Text t = labels[r][c];
                double x = x0 + c * cell;
                double y = y0 + r * cell;
                rect.setX(x);
                rect.setY(y);
                rect.setWidth(cell);
                rect.setHeight(cell);
                // center text
                t.setX(x + cell / 2.0 - 5);
                t.setY(y + cell / 2.0 + 6);
            }
        }

        // Remove old thick lines and redraw
        container.getChildren().removeIf(n -> n instanceof Line);
        for (int k = 0; k <= 9; k++) {
            double stroke = (k % 3 == 0) ? 3.0 : 1.0;
            Line vert = new Line(x0 + k * cell, y0, x0 + k * cell, y0 + size);
            vert.setStrokeWidth(stroke);
            Line hor = new Line(x0, y0 + k * cell, x0 + size, y0 + k * cell);
            hor.setStrokeWidth(stroke);
            container.getChildren().addAll(vert, hor);
        }
    }
}
