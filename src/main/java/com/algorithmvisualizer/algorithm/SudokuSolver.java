package com.algorithmvisualizer.algorithm;

import java.util.*;

public class SudokuSolver {
    public enum StepType { INIT, PLACE, BACKTRACK, DONE }
    public interface StepListener { void onStep(StepType type, int r, int c, int val); }

    public static class Frame {
        public int posIndex; // index in empties list
        public int nextVal;  // next candidate value to try (1..9)
        public Frame(int posIndex, int nextVal) { this.posIndex = posIndex; this.nextVal = nextVal; }
    }

    public static class State {
        public int[][] grid;
        public boolean[][] fixed;
        public List<int[]> empties;
        public Deque<Frame> stack;
        public boolean done;
    }

    private int[][] grid = new int[9][9];
    private boolean[][] fixed = new boolean[9][9];
    private List<int[]> empties = new ArrayList<>();
    private Deque<Frame> stack = new ArrayDeque<>();
    private boolean done = false;

    private StepListener listener;

    public SudokuSolver(int[][] startGrid) {
        setGrid(startGrid);
    }

    public void setGrid(int[][] startGrid) {
        // copy
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            grid[r][c] = (startGrid != null ? startGrid[r][c] : 0);
            fixed[r][c] = grid[r][c] != 0;
        }
        rebuildEmpties();
        resetInternal(true);
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void reset() { resetInternal(true); }

    private void resetInternal(boolean emitInit) {
        done = false;
        stack.clear();
        // Start at first empty position
        if (empties.isEmpty()) {
            done = true;
            emit(StepType.DONE, -1, -1, -1);
            return;
        }
        stack.push(new Frame(0, 1));
        if (emitInit) emit(StepType.INIT, empties.get(0)[0], empties.get(0)[1], 0);
    }

    public boolean isDone() { return done; }
    public int[][] getGrid() { int[][] out = new int[9][9]; for (int r=0;r<9;r++) System.arraycopy(grid[r],0,out[r],0,9); return out; }
    public boolean[][] getFixedMask() { boolean[][] out = new boolean[9][9]; for (int r=0;r<9;r++) System.arraycopy(fixed[r],0,out[r],0,9); return out; }

    public void step() {
        if (done) return;
        if (stack.isEmpty()) {
            // No more frames: solved
            done = true;
            emit(StepType.DONE, -1, -1, -1);
            return;
        }
        Frame f = stack.peek();
        int[] pos = empties.get(f.posIndex);
        int r = pos[0], c = pos[1];

        // Try next candidates from f.nextVal..9
        for (int v = f.nextVal; v <= 9; v++) {
            if (canPlace(r, c, v)) {
                grid[r][c] = v;
                // advance frame for next time (in case of backtrack)
                f.nextVal = v + 1;
                // Push next empty position frame
                if (f.posIndex + 1 == empties.size()) {
                    // All filled
                    done = true;
                    emit(StepType.PLACE, r, c, v);
                    emit(StepType.DONE, -1, -1, -1);
                    return;
                } else {
                    stack.push(new Frame(f.posIndex + 1, 1));
                    emit(StepType.PLACE, r, c, v);
                    return;
                }
            }
        }
        // No candidate fits -> backtrack from this cell, clear it and pop
        grid[r][c] = 0;
        stack.pop();
        emit(StepType.BACKTRACK, r, c, 0);
        // Next step will continue on previous frame
    }

    private boolean canPlace(int r, int c, int v) {
        for (int k = 0; k < 9; k++) if (grid[r][k] == v || grid[k][c] == v) return false;
        int br = (r/3)*3, bc = (c/3)*3;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) if (grid[br+i][bc+j] == v) return false;
        return true;
    }

    private void rebuildEmpties() {
        empties.clear();
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) if (!fixed[r][c]) empties.add(new int[]{r,c});
    }

    public State snapshot() {
        State s = new State();
        s.grid = getGrid();
        s.fixed = getFixedMask();
        s.empties = new ArrayList<>();
        for (int[] p : empties) s.empties.add(new int[]{p[0],p[1]});
        s.stack = new ArrayDeque<>();
        for (Frame f : stack) s.stack.addLast(new Frame(f.posIndex, f.nextVal));
        s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        for (int r = 0; r < 9; r++) System.arraycopy(s.grid[r], 0, grid[r], 0, 9);
        for (int r = 0; r < 9; r++) System.arraycopy(s.fixed[r], 0, fixed[r], 0, 9);
        empties = new ArrayList<>();
        for (int[] p : s.empties) empties.add(new int[]{p[0],p[1]});
        stack = new ArrayDeque<>();
        for (Frame f : s.stack) stack.addLast(new Frame(f.posIndex, f.nextVal));
        done = s.done;
    }

    private void emit(StepType type, int r, int c, int val) { if (listener != null) listener.onStep(type, r, c, val); }
}
