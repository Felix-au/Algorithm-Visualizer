package com.algorithmvisualizer.visualization;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders a grid maze with cell walls. Provides helpers to color cells
 * as visited/frontier/path and to highlight the current cell.
 */
public class MazeGridRenderer {
    private final Pane container;
    private int rows = 1, cols = 1;
    // walls[r][c][4] => N,E,S,W (true = wall present)
    private boolean[][][] walls;

    private final Rectangle[][] cells;
    private final Map<String, Line> wallLines; // key: r-c-dir

    public MazeGridRenderer() {
        container = new Pane();
        container.setMinSize(360, 360);
        container.setPrefSize(600, 600);
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        container.layoutBoundsProperty().addListener((obs, o, n) -> layout());
        cells = new Rectangle[100][100]; // max placeholder; actual sized on setMaze
        wallLines = new HashMap<>();
        setMaze(15, 15, createFullWalls(15, 15));
    }

    public Node getNode() { return container; }

    public void setMaze(int r, int c, boolean[][][] walls) {
        this.rows = Math.max(1, r);
        this.cols = Math.max(1, c);
        this.walls = copyWalls(walls);
        rebuild();
        layout();
    }

    public void updateWalls(boolean[][][] walls) {
        this.walls = copyWalls(walls);
        drawWalls();
    }

    public void clearOverlays() {
        if (cells == null) return;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle rect = cells[r][c];
                if (rect != null) rect.setFill(Color.WHITE);
            }
        }
    }

    public void markVisited(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.LIGHTBLUE);
    }

    public void markFrontier(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.GOLD);
    }

    public void markPath(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.FORESTGREEN);
    }

    public void highlightCurrent(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.ORANGE);
    }

    private void rebuild() {
        container.getChildren().clear();
        wallLines.clear();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle rect = new Rectangle();
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.TRANSPARENT);
                cells[r][c] = rect;
                container.getChildren().add(rect);
            }
        }
        drawWalls();
    }

    private void drawWalls() {
        // remove old wall lines
        container.getChildren().removeIf(n -> n instanceof Line);
        wallLines.clear();
        double w = Math.max(360, container.getWidth());
        double h = Math.max(360, container.getHeight());
        double size = Math.min(w, h);
        double cell = size / Math.max(rows, cols);
        double x0 = (w - cols * cell) / 2.0;
        double y0 = (h - rows * cell) / 2.0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = x0 + c * cell;
                double y = y0 + r * cell;
                if (walls[r][c][0]) addLine(x, y, x + cell, y); // N
                if (walls[r][c][1]) addLine(x + cell, y, x + cell, y + cell); // E
                if (walls[r][c][2]) addLine(x, y + cell, x + cell, y + cell); // S
                if (walls[r][c][3]) addLine(x, y, x, y + cell); // W
            }
        }
    }

    private void addLine(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStroke(Color.BLACK);
        l.setStrokeWidth(2.0);
        container.getChildren().add(l);
    }

    private void layout() {
        double w = Math.max(360, container.getWidth());
        double h = Math.max(360, container.getHeight());
        double size = Math.min(w, h);
        double cell = size / Math.max(rows, cols);
        double x0 = (w - cols * cell) / 2.0;
        double y0 = (h - rows * cell) / 2.0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle rect = cells[r][c];
                if (rect == null) continue;
                double x = x0 + c * cell;
                double y = y0 + r * cell;
                rect.setX(x + 1);
                rect.setY(y + 1);
                rect.setWidth(cell - 2);
                rect.setHeight(cell - 2);
            }
        }
        drawWalls();
    }

    private static boolean[][][] createFullWalls(int rows, int cols) {
        boolean[][][] w = new boolean[rows][cols][4];
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            for (int d = 0; d < 4; d++) w[r][c][d] = true;
        return w;
    }

    private static boolean[][][] copyWalls(boolean[][][] src) {
        int r = src.length, c = src[0].length;
        boolean[][][] w = new boolean[r][c][4];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++)
            System.arraycopy(src[i][j], 0, w[i][j], 0, 4);
        return w;
    }
}
