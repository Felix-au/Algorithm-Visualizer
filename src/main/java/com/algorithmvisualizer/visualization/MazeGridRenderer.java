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

    // start/goal overlay via stroke
    private int startR = 0, startC = 0, goalR = 0, goalC = 0;
    private boolean showStartGoal = false;

    public interface CellClickListener { void onClick(int r, int c); }
    private CellClickListener clickListener;

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
                if (rect != null) rect.setStroke(Color.TRANSPARENT);
            }
        }
    }

    public void markVisited(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.LIGHTBLUE);
        reapplyStartGoalStrokes();
    }

    public void markFrontier(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.GOLD);
        reapplyStartGoalStrokes();
    }

    public void markPath(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.FORESTGREEN);
        reapplyStartGoalStrokes();
    }

    public void highlightCurrent(int r, int c) {
        Rectangle rect = cells[r][c];
        if (rect != null) rect.setFill(Color.ORANGE);
        reapplyStartGoalStrokes();
    }

    public void setStartGoal(int sR, int sC, int gR, int gC) {
        this.startR = sR; this.startC = sC; this.goalR = gR; this.goalC = gC;
        this.showStartGoal = true;
        reapplyStartGoalStrokes();
    }

    public void setCellClickListener(CellClickListener l) {
        this.clickListener = l;
        // rebind to all cell rectangles
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle rect = cells[r][c];
                if (rect != null) {
                    final int rr = r, cc = c;
                    rect.setOnMouseClicked(e -> { if (clickListener != null) clickListener.onClick(rr, cc); });
                }
            }
        }
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
                if (clickListener != null) {
                    final int rr = r, cc = c;
                    rect.setOnMouseClicked(e -> clickListener.onClick(rr, cc));
                }
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
        reapplyStartGoalStrokes();
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

    private void reapplyStartGoalStrokes() {
        if (!showStartGoal) return;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle rect = cells[r][c];
                if (rect == null) continue;
                rect.setStroke(Color.TRANSPARENT);
                rect.setStrokeWidth(1.0);
            }
        }
        if (startR >= 0 && startR < rows && startC >= 0 && startC < cols) {
            Rectangle rs = cells[startR][startC];
            if (rs != null) { rs.setStroke(Color.CORNFLOWERBLUE); rs.setStrokeWidth(3.0); }
        }
        if (goalR >= 0 && goalR < rows && goalC >= 0 && goalC < cols) {
            Rectangle rg = cells[goalR][goalC];
            if (rg != null) { rg.setStroke(Color.CRIMSON); rg.setStrokeWidth(3.0); }
        }
    }
}
