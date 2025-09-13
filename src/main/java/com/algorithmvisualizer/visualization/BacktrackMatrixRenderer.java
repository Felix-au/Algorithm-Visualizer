package com.algorithmvisualizer.visualization;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Renders a matrix showing tried cells and backtracking steps.
 */
public class BacktrackMatrixRenderer {

    public enum CellState { UNVISITED, CHECKED, PLACED, BACKTRACKED }

    private final int boardSize;
    private final GridPane grid;
    private final Rectangle[][] cells;
    private final CellState[][] state;

    private static final int CELL_SIZE = 22;

    public BacktrackMatrixRenderer(int boardSize) {
        this.boardSize = boardSize;
        this.grid = new GridPane();
        this.cells = new Rectangle[boardSize][boardSize];
        this.state = new CellState[boardSize][boardSize];
        init();
    }

    private void init() {
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setStroke(Color.GRAY);
                rect.setFill(Color.WHITE);
                cells[r][c] = rect;
                state[r][c] = CellState.UNVISITED;
                grid.add(rect, c, r);
            }
        }
    }

    public void markChecked(int row, int col) { setState(row, col, CellState.CHECKED); }
    public void markPlaced(int row, int col) { setState(row, col, CellState.PLACED); }
    public void markBacktracked(int row, int col) { setState(row, col, CellState.BACKTRACKED); }

    public void clear() {
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                state[r][c] = CellState.UNVISITED;
                cells[r][c].setFill(Color.WHITE);
            }
        }
    }

    private void setState(int row, int col, CellState newState) {
        if (row < 0 || col < 0 || row >= boardSize || col >= boardSize) return;
        state[row][col] = newState;
        switch (newState) {
            case UNVISITED: cells[row][col].setFill(Color.WHITE); break;
            case CHECKED: cells[row][col].setFill(Color.LIGHTBLUE); break;
            case PLACED: cells[row][col].setFill(Color.LIGHTGREEN); break;
            case BACKTRACKED: cells[row][col].setFill(Color.PINK); break;
        }
    }

    public GridPane getGrid() { return grid; }
}


