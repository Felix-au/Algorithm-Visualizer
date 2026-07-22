package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Solver for Matrix Chain Multiplication using dynamic programming.
 * Given dimensions p[0..n] for n matrices (A_i is p[i-1] × p[i]),
 * finds the optimal parenthesization to minimize scalar multiplications.
 *
 * Time Complexity: O(n³)
 * Space Complexity: O(n²)
 */
public class MatrixChainMultiplicationSolver {

    public enum StepType {
        INIT,
        SET_CHAIN_LENGTH,
        SELECT_SUBPROBLEM,
        TRY_SPLIT,
        SHOW_COST_CALCULATION,
        NEW_MINIMUM,
        SPLIT_NOT_BETTER,
        CELL_COMPLETE,
        TABLE_COMPLETE,
        TRACEBACK_START,
        TRACEBACK_STEP,
        TRACEBACK_COMPLETE,
        COMPLETE
    }

    public interface StepListener {
        void onStep(StepType type, int i, int j, Map<String, Object> meta);
    }

    // Input: dimensions array p[0..n], so n matrices where A_i = p[i-1] x p[i]
    private int[] p;
    private int n; // number of matrices

    // DP tables
    private int[][] m; // m[i][j] = minimum cost to multiply A_i..A_j
    private int[][] s; // s[i][j] = optimal split point

    // State machine
    private int chainLength;  // l = current chain length being computed (2..n)
    private int currentI;     // current row in DP table
    private int currentJ;     // current column in DP table
    private int currentK;     // current split point being tried
    private int bestCost;     // best cost found so far for m[i][j]
    private int bestSplit;    // best split point found so far
    private StepType lastStep;
    private boolean done;
    private boolean fillingTable;
    private boolean tracingBack;

    // Traceback state
    private String parenthesization;
    private List<int[]> tracebackStack; // Stack of (i, j) pairs to process

    // Listener
    private StepListener listener;

    public MatrixChainMultiplicationSolver() {
        reset();
    }

    public void setDimensions(int[] dimensions) {
        this.p = dimensions.clone();
        this.n = p.length - 1; // number of matrices
        this.m = new int[n + 1][n + 1]; // 1-indexed
        this.s = new int[n + 1][n + 1];
        reset();
    }

    public void setStepListener(StepListener listener) {
        this.listener = listener;
    }

    public void reset() {
        if (m != null) {
            for (int i = 0; i <= n; i++) {
                Arrays.fill(m[i], 0);
                Arrays.fill(s[i], 0);
            }
        }
        chainLength = 2;
        currentI = 0;
        currentJ = 0;
        currentK = 0;
        bestCost = Integer.MAX_VALUE;
        bestSplit = 0;
        lastStep = null;
        done = false;
        fillingTable = true;
        tracingBack = false;
        parenthesization = "";
        tracebackStack = new ArrayList<>();
    }

    public void step() {
        if (done) return;

        if (lastStep == null) {
            lastStep = StepType.INIT;
            Map<String, Object> meta = new HashMap<>();
            meta.put("dimensions", p.clone());
            meta.put("numMatrices", n);
            notifyListener(StepType.INIT, 0, 0, meta);
            return;
        }

        if (fillingTable) {
            stepFillTable();
        } else if (tracingBack) {
            stepTraceback();
        }
    }

    private void stepFillTable() {
        if (lastStep == StepType.INIT) {
            // Start first chain length
            chainLength = 2;
            currentI = 1;
            currentJ = currentI + chainLength - 1;
            lastStep = StepType.SET_CHAIN_LENGTH;
            Map<String, Object> meta = new HashMap<>();
            meta.put("chainLength", chainLength);
            notifyListener(StepType.SET_CHAIN_LENGTH, 0, 0, meta);
            return;
        }

        if (lastStep == StepType.SET_CHAIN_LENGTH) {
            // Select first subproblem for this chain length
            bestCost = Integer.MAX_VALUE;
            bestSplit = currentI;
            currentK = currentI;
            lastStep = StepType.SELECT_SUBPROBLEM;
            Map<String, Object> meta = new HashMap<>();
            meta.put("chainLength", chainLength);
            notifyListener(StepType.SELECT_SUBPROBLEM, currentI, currentJ, meta);
            return;
        }

        if (lastStep == StepType.SELECT_SUBPROBLEM) {
            // Try first split
            lastStep = StepType.TRY_SPLIT;
            Map<String, Object> meta = new HashMap<>();
            meta.put("k", currentK);
            meta.put("mik", m[currentI][currentK]);
            meta.put("mkj", m[currentK + 1][currentJ]);
            notifyListener(StepType.TRY_SPLIT, currentI, currentJ, meta);
            return;
        }

        if (lastStep == StepType.TRY_SPLIT) {
            // Show cost calculation
            int cost = m[currentI][currentK] + m[currentK + 1][currentJ]
                     + p[currentI - 1] * p[currentK] * p[currentJ];
            lastStep = StepType.SHOW_COST_CALCULATION;
            Map<String, Object> meta = new HashMap<>();
            meta.put("k", currentK);
            meta.put("mik", m[currentI][currentK]);
            meta.put("mkj", m[currentK + 1][currentJ]);
            meta.put("pi1", p[currentI - 1]);
            meta.put("pk", p[currentK]);
            meta.put("pj", p[currentJ]);
            meta.put("multiplicationCost", p[currentI - 1] * p[currentK] * p[currentJ]);
            meta.put("totalCost", cost);
            meta.put("currentBest", bestCost);
            notifyListener(StepType.SHOW_COST_CALCULATION, currentI, currentJ, meta);
            return;
        }

        if (lastStep == StepType.SHOW_COST_CALCULATION) {
            // Evaluate: is this a new minimum?
            int cost = m[currentI][currentK] + m[currentK + 1][currentJ]
                     + p[currentI - 1] * p[currentK] * p[currentJ];

            if (cost < bestCost) {
                bestCost = cost;
                bestSplit = currentK;
                lastStep = StepType.NEW_MINIMUM;
                Map<String, Object> meta = new HashMap<>();
                meta.put("k", currentK);
                meta.put("newBest", bestCost);
                meta.put("bestSplit", bestSplit);
                notifyListener(StepType.NEW_MINIMUM, currentI, currentJ, meta);
            } else {
                lastStep = StepType.SPLIT_NOT_BETTER;
                Map<String, Object> meta = new HashMap<>();
                meta.put("k", currentK);
                meta.put("cost", cost);
                meta.put("currentBest", bestCost);
                notifyListener(StepType.SPLIT_NOT_BETTER, currentI, currentJ, meta);
            }
            return;
        }

        if (lastStep == StepType.NEW_MINIMUM || lastStep == StepType.SPLIT_NOT_BETTER) {
            // Move to next split or finish cell
            currentK++;
            if (currentK < currentJ) {
                // Try next split
                lastStep = StepType.TRY_SPLIT;
                Map<String, Object> meta = new HashMap<>();
                meta.put("k", currentK);
                meta.put("mik", m[currentI][currentK]);
                meta.put("mkj", m[currentK + 1][currentJ]);
                notifyListener(StepType.TRY_SPLIT, currentI, currentJ, meta);
            } else {
                // Cell complete: commit best values
                m[currentI][currentJ] = bestCost;
                s[currentI][currentJ] = bestSplit;
                lastStep = StepType.CELL_COMPLETE;
                Map<String, Object> meta = new HashMap<>();
                meta.put("value", bestCost);
                meta.put("splitPoint", bestSplit);
                notifyListener(StepType.CELL_COMPLETE, currentI, currentJ, meta);
            }
            return;
        }

        if (lastStep == StepType.CELL_COMPLETE) {
            // Move to next subproblem or next chain length
            currentI++;
            currentJ = currentI + chainLength - 1;

            if (currentJ <= n) {
                // Next subproblem in same chain length
                bestCost = Integer.MAX_VALUE;
                bestSplit = currentI;
                currentK = currentI;
                lastStep = StepType.SELECT_SUBPROBLEM;
                Map<String, Object> meta = new HashMap<>();
                meta.put("chainLength", chainLength);
                notifyListener(StepType.SELECT_SUBPROBLEM, currentI, currentJ, meta);
            } else {
                // Move to next chain length
                chainLength++;
                if (chainLength <= n) {
                    currentI = 1;
                    currentJ = currentI + chainLength - 1;
                    lastStep = StepType.SET_CHAIN_LENGTH;
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("chainLength", chainLength);
                    notifyListener(StepType.SET_CHAIN_LENGTH, 0, 0, meta);
                } else {
                    // Table complete
                    fillingTable = false;
                    tracingBack = true;
                    lastStep = StepType.TABLE_COMPLETE;
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("optimalCost", m[1][n]);
                    notifyListener(StepType.TABLE_COMPLETE, 1, n, meta);
                }
            }
            return;
        }
    }

    private void stepTraceback() {
        if (lastStep == StepType.TABLE_COMPLETE) {
            lastStep = StepType.TRACEBACK_START;
            parenthesization = buildParenthesization(1, n);
            Map<String, Object> meta = new HashMap<>();
            meta.put("parenthesization", parenthesization);
            meta.put("optimalCost", m[1][n]);
            notifyListener(StepType.TRACEBACK_START, 1, n, meta);
            return;
        }

        if (lastStep == StepType.TRACEBACK_START) {
            // Show traceback through split table
            lastStep = StepType.TRACEBACK_STEP;
            Map<String, Object> meta = new HashMap<>();
            meta.put("parenthesization", parenthesization);
            meta.put("splitTable", deepCopy2D(s));
            notifyListener(StepType.TRACEBACK_STEP, 1, n, meta);
            return;
        }

        if (lastStep == StepType.TRACEBACK_STEP) {
            lastStep = StepType.TRACEBACK_COMPLETE;
            Map<String, Object> meta = new HashMap<>();
            meta.put("parenthesization", parenthesization);
            meta.put("optimalCost", m[1][n]);
            notifyListener(StepType.TRACEBACK_COMPLETE, 1, n, meta);
            return;
        }

        if (lastStep == StepType.TRACEBACK_COMPLETE) {
            done = true;
            lastStep = StepType.COMPLETE;
            Map<String, Object> meta = new HashMap<>();
            meta.put("parenthesization", parenthesization);
            meta.put("optimalCost", m[1][n]);
            notifyListener(StepType.COMPLETE, 1, n, meta);
        }
    }

    /**
     * Recursively build the optimal parenthesization string.
     */
    private String buildParenthesization(int i, int j) {
        if (i == j) {
            return "A" + i;
        }
        int k = s[i][j];
        String left = buildParenthesization(i, k);
        String right = buildParenthesization(k + 1, j);
        return "(" + left + " × " + right + ")";
    }

    private void notifyListener(StepType type, int i, int j, Map<String, Object> meta) {
        if (listener != null) {
            listener.onStep(type, i, j, meta != null ? meta : new HashMap<>());
        }
    }

    public boolean isDone() {
        return done;
    }

    public int[][] getM() {
        return m;
    }

    public int[][] getS() {
        return s;
    }

    public int[] getDimensions() {
        return p != null ? p.clone() : new int[0];
    }

    public int getN() {
        return n;
    }

    public String getParenthesization() {
        return parenthesization;
    }

    public int getOptimalCost() {
        return n > 0 ? m[1][n] : 0;
    }

    // ── Snapshot / Restore ──────────────────────────────────────────────

    public static class State {
        public int[] p;
        public int n;
        public int[][] m;
        public int[][] s;
        public int chainLength, currentI, currentJ, currentK;
        public int bestCost, bestSplit;
        public StepType lastStep;
        public boolean done, fillingTable, tracingBack;
        public String parenthesization;
        public List<int[]> tracebackStack;
    }

    public State snapshot() {
        State st = new State();
        st.p = p != null ? p.clone() : null;
        st.n = n;
        st.m = deepCopy2D(m);
        st.s = deepCopy2D(s);
        st.chainLength = chainLength;
        st.currentI = currentI;
        st.currentJ = currentJ;
        st.currentK = currentK;
        st.bestCost = bestCost;
        st.bestSplit = bestSplit;
        st.lastStep = lastStep;
        st.done = done;
        st.fillingTable = fillingTable;
        st.tracingBack = tracingBack;
        st.parenthesization = parenthesization;
        st.tracebackStack = new ArrayList<>();
        for (int[] pair : tracebackStack) {
            st.tracebackStack.add(pair.clone());
        }
        return st;
    }

    public void restore(State st) {
        this.p = st.p != null ? st.p.clone() : null;
        this.n = st.n;
        this.m = deepCopy2D(st.m);
        this.s = deepCopy2D(st.s);
        this.chainLength = st.chainLength;
        this.currentI = st.currentI;
        this.currentJ = st.currentJ;
        this.currentK = st.currentK;
        this.bestCost = st.bestCost;
        this.bestSplit = st.bestSplit;
        this.lastStep = st.lastStep;
        this.done = st.done;
        this.fillingTable = st.fillingTable;
        this.tracingBack = st.tracingBack;
        this.parenthesization = st.parenthesization;
        this.tracebackStack = new ArrayList<>();
        for (int[] pair : st.tracebackStack) {
            this.tracebackStack.add(pair.clone());
        }
    }

    /**
     * Deep copy a 2D int array (row-by-row clone to avoid shallow-copy pitfall).
     */
    private int[][] deepCopy2D(int[][] src) {
        if (src == null) return null;
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}
