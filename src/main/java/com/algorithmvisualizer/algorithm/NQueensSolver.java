package com.algorithmvisualizer.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * N-Queens solver with granular step events for visualization.
 */
public class NQueensSolver {

    public enum StepType { CHECK, PLACE, BACKTRACK, SOLUTION, DONE }

    public interface StepListener {
        void onStep(StepType type, int row, int col);
    }

    private int boardSize;
    private int[] queens; // queens[i] = column position of queen in row i
    private boolean[] usedColumns;
    private boolean[] usedDiagonals1; // main diagonal (row - col)
    private boolean[] usedDiagonals2; // anti-diagonal (row + col)

    private int currentRow = 0;
    private int currentColumn = 0; // next column to try in current row
    private int queensPlaced = 0;
    private int solutionsFound = 0;
    private boolean isCompleted = false;

    private VisualizationCallback callback; // legacy, still supported for place/backtrack
    private StepListener stepListener;

    private final List<int[]> solutions = new ArrayList<>();

    public interface VisualizationCallback {
        void onStep(int row, int col, boolean isPlacing, boolean isBacktracking);
    }

    public NQueensSolver(int boardSize) {
        this.boardSize = boardSize;
        initialize();
    }

    private void initialize() {
        queens = new int[boardSize];
        usedColumns = new boolean[boardSize];
        usedDiagonals1 = new boolean[2 * boardSize - 1];
        usedDiagonals2 = new boolean[2 * boardSize - 1];
        for (int i = 0; i < boardSize; i++) queens[i] = -1;
        solutions.clear();
    }

    public void setVisualizationCallback(VisualizationCallback callback) {
        this.callback = callback;
    }

    public void setStepListener(StepListener listener) {
        this.stepListener = listener;
    }

    public void solveAll() { // still available for full-run
        isCompleted = false;
        solutionsFound = 0;
        solutions.clear();
        solve(0);
        isCompleted = true;
        emit(StepType.DONE, -1, -1);
    }

    public void step() {
        if (isCompleted) return;

        if (currentRow == boardSize) {
            solutionsFound++;
            recordCurrentSolution();
            emit(StepType.SOLUTION, currentRow - 1, queens[currentRow - 1]);
            // backtrack one step to look for more solutions
            currentRow--;
            int lastCol = queens[currentRow];
            removeQueen(currentRow, lastCol);
            emitPlaceBacktrack(false, currentRow, lastCol);
            currentColumn = lastCol + 1;
            return;
        }

        if (currentColumn < boardSize) {
            int col = currentColumn;
            emit(StepType.CHECK, currentRow, col);
            if (isSafe(currentRow, col)) {
                placeQueen(currentRow, col);
                emitPlaceBacktrack(true, currentRow, col);
                currentRow++;
                currentColumn = 0;
            } else {
                currentColumn++;
            }
            return;
        }

        // No column worked, backtrack
        if (currentRow == 0) {
            isCompleted = true;
            emit(StepType.DONE, -1, -1);
            return;
        }
        currentRow--;
        int lastCol = queens[currentRow];
        removeQueen(currentRow, lastCol);
        emitPlaceBacktrack(false, currentRow, lastCol);
        emit(StepType.BACKTRACK, currentRow, lastCol);
        currentColumn = lastCol + 1;
    }

    private void solve(int row) {
        if (row == boardSize) {
            solutionsFound++;
            recordCurrentSolution();
            return;
        }
        for (int col = 0; col < boardSize; col++) {
            if (isSafe(row, col)) {
                placeQueen(row, col);
                solve(row + 1);
                removeQueen(row, col);
            }
        }
    }

    private void recordCurrentSolution() {
        int[] copy = new int[boardSize];
        System.arraycopy(queens, 0, copy, 0, boardSize);
        solutions.add(copy);
    }

    private void placeQueen(int row, int col) {
        queens[row] = col;
        usedColumns[col] = true;
        usedDiagonals1[row - col + boardSize - 1] = true;
        usedDiagonals2[row + col] = true;
        queensPlaced++;
    }

    private void removeQueen(int row, int col) {
        queens[row] = -1;
        usedColumns[col] = false;
        usedDiagonals1[row - col + boardSize - 1] = false;
        usedDiagonals2[row + col] = false;
        queensPlaced--;
    }

    private boolean isSafe(int row, int col) {
        return !usedColumns[col]
            && !usedDiagonals1[row - col + boardSize - 1]
            && !usedDiagonals2[row + col];
    }

    public void reset() {
        currentRow = 0;
        currentColumn = 0;
        queensPlaced = 0;
        solutionsFound = 0;
        isCompleted = false;
        initialize();
    }

    private void emit(StepType type, int row, int col) {
        if (stepListener != null) stepListener.onStep(type, row, col);
    }

    private void emitPlaceBacktrack(boolean placing, int row, int col) {
        if (callback != null) callback.onStep(row, col, placing, !placing);
    }

    // Getters
    public int getCurrentRow() { return currentRow; }
    public int getCurrentColumn() { return currentColumn; }
    public int getQueensPlaced() { return queensPlaced; }
    public int getSolutionsFound() { return solutionsFound; }
    public boolean isCompleted() { return isCompleted; }

    public String getCurrentStepDescription() {
        if (isCompleted) return "Done. Solutions: " + solutionsFound;
        return "Row " + currentRow + ", next col: " + currentColumn + ", placed: " + queensPlaced;
    }

    public int[] getQueenColumnByRow() {
        int[] copy = new int[queens.length];
        System.arraycopy(queens, 0, copy, 0, queens.length);
        return copy;
    }

    public List<int[]> getSolutions() {
        return new ArrayList<>(solutions);
    }
}
