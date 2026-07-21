package com.algorithmvisualizer.algorithm;

import com.algorithmvisualizer.model.ColoringMode;
import java.util.*;

public class BruteForceColoringSolver implements GraphColoringSolver {
    private int n;
    private List<List<Integer>> adj;
    private int[] coloring;
    private int currentNode;
    private int currentColor;
    private int maxColors;
    private ColoringMode mode = ColoringMode.FIND_MINIMUM;
    private int attemptK = 0;
    private boolean done = false;
    private StepListener listener;
    // granularity: after TRY_COLOR, wait one step to CHECK/ASSIGN
    private boolean pendingCheck = false;
    private int pendingNode = -1;
    private int pendingColor = -1;
    // symmetry break: anchor node 0 to color 0
    private boolean anchorV0 = true;

    @Override
    public void setGraph(int n, List<List<Integer>> adj) {
        this.n = Math.max(0, n);
        this.adj = copyAdj(adj);
        this.coloring = new int[this.n];
        Arrays.fill(this.coloring, -1);
        this.currentNode = 0;
        this.currentColor = 0;
        this.done = false;
        this.attemptK = 0;
    }

    @Override
    public void setMode(ColoringMode mode) { this.mode = mode; }

    @Override
    public void setMaxColors(int k) { this.maxColors = Math.max(2, k); }

    @Override
    public void setStepListener(StepListener l) { this.listener = l; }

    @Override
    public void reset() {
        if (this.coloring == null) return;
        Arrays.fill(this.coloring, -1);
        this.currentNode = 0;
        this.currentColor = 0;
        this.done = false;
        this.pendingCheck = false;
        this.pendingNode = -1;
        this.pendingColor = -1;
        if (mode == ColoringMode.FIND_MINIMUM) attemptK = 0;
    }

    @Override
    public void step() {
        if (done) return;
        if (coloring == null || adj == null) return;
        if (mode == ColoringMode.FIND_MINIMUM) {
            stepFindMinimum();
        } else {
            stepUseK();
        }
    }

    private void stepFindMinimum() {
        if (attemptK == 0) {
            attemptK = 2;
            emit(StepType.TRY_K_COLORS, -1, attemptK);
            maxColors = attemptK;
            resetForNewAttempt();
            return;
        }
        stepCore();
        if (!done && backtrackedBeyondRoot()) {
            emit(StepType.NO_SOLUTION, -1, attemptK);
            attemptK++;
            emit(StepType.TRY_K_COLORS, -1, attemptK);
            maxColors = attemptK;
            resetForNewAttempt();
        }
    }

    private void stepUseK() {
        stepCore();
        if (!done && backtrackedBeyondRoot()) { emit(StepType.NO_SOLUTION, -1, maxColors); done = true; }
    }

    private void stepCore() {
        if (done) return;
        if (currentNode >= n) { emit(StepType.SOLUTION_FOUND, -1, colorsUsed()); done = true; return; }
        // Anchor: if at node 0 and not assigned, try color 0 only.
        // If we've already backtracked to root (currentColor>0), do NOT retry; signal beyond-root by forcing currentColor=maxColors.
        if (anchorV0 && currentNode == 0) {
            if (coloring[0] == -1) {
                if (currentColor > 0) {
                    // We've exhausted root under anchoring; trigger backtrackedBeyondRoot path
                    currentColor = maxColors;
                } else if (!pendingCheck) {
                    emit(StepType.TRY_COLOR, 0, 0);
                    pendingNode = 0; pendingColor = 0; pendingCheck = true; return;
                }
            } else {
                // root already assigned -> proceed to node 1
                currentNode = 1;
                currentColor = 0;
            }
        }
        if (pendingCheck) {
            // perform check for the pending node/color
            boolean ok = isValid(pendingNode, pendingColor);
            if (ok) {
                coloring[pendingNode] = pendingColor;
                emit(StepType.ASSIGN_COLOR, pendingNode, pendingColor);
                currentNode = pendingNode + 1;
                currentColor = 0;
            } else {
                emit(StepType.CHECK_CONSTRAINT, pendingNode, pendingColor);
                // advance color
                currentNode = pendingNode;
                currentColor = pendingColor + 1;
            }
            pendingCheck = false; pendingNode = -1; pendingColor = -1; 
            return;
        }
        // try a color or backtrack
        if (anchorV0 && currentNode == 0 && currentColor > 0) {
            // prevent trying colors > 0 at root
            currentColor = maxColors;
        }
        if (currentColor >= maxColors) {
            emit(StepType.BACKTRACK, currentNode, -1);
            if (currentNode == 0) return;
            currentNode--;
            currentColor = coloring[currentNode] + 1;
            coloring[currentNode] = -1;
            return;
        }
        // emit try color now and defer check to next step
        emit(StepType.TRY_COLOR, currentNode, currentColor);
        pendingNode = currentNode;
        pendingColor = currentColor;
        pendingCheck = true;
    }

    private boolean isValid(int node, int color) {
        for (int nb : adj.get(node)) {
            if (coloring[nb] == color) return false;
        }
        return true;
    }

    private void resetForNewAttempt() {
        if (coloring == null || coloring.length != n) {
            coloring = new int[Math.max(0, n)];
        }
        Arrays.fill(coloring, -1);
        currentNode = 0;
        currentColor = 0;
    }

    private boolean backtrackedBeyondRoot() {
        if (coloring == null || coloring.length == 0) return true;
        return currentNode == 0 && currentColor >= maxColors && coloring[0] == -1;
    }

    private int colorsUsed() {
        int[] arr = this.coloring;
        if (arr == null) return 0;
        int max = -1;
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }
        return max + 1;
    }

    @Override
    public boolean isDone() { return done; }

    @Override
    public int[] getColoring() { return coloring != null ? coloring.clone() : new int[0]; }

    @Override
    public int getColorsUsed() { return colorsUsed(); }

    @Override
    public State snapshot() {
        State s = new State();
        s.n = n;
        s.coloring = (coloring != null) ? coloring.clone() : new int[0];
        s.currentNode = currentNode;
        s.currentColor = currentColor;
        s.attemptK = attemptK;
        s.done = done;
        return s;
    }

    @Override
    public void restore(State s) {
        if (s == null) return;
        this.n = s.n;
        this.coloring = (s.coloring != null) ? s.coloring.clone() : new int[Math.max(0, n)];
        if (this.coloring.length == 0 && n > 0) {
            this.coloring = new int[n];
            Arrays.fill(this.coloring, -1);
        }
        this.currentNode = s.currentNode;
        this.currentColor = s.currentColor;
        this.attemptK = s.attemptK;
        this.done = s.done;
    }

    private void emit(StepType t, int node, int color) {
        if (listener != null) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("attemptK", attemptK);
            meta.put("currentNode", currentNode);
            meta.put("currentColor", currentColor);
            meta.put("anchorV0", anchorV0);
            listener.onStep(t, node, color, meta);
        }
    }

    // public API to control anchoring
    public void setAnchorV0(boolean anchor) { this.anchorV0 = anchor; }

    private List<List<Integer>> copyAdj(List<List<Integer>> src) {
        List<List<Integer>> out = new ArrayList<>();
        if (src == null) return out;
        for (List<Integer> row : src) out.add(new ArrayList<>(row));
        return out;
    }
}
