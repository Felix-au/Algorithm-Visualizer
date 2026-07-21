package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Solver for Longest Common Subsequence using dynamic programming.
 * Implements step-by-step execution with fine-grained visualization.
 */
public class LCSSolver {
    
    public enum StepType {
        INIT,
        HIGHLIGHT_CELL,
        COMPARE_CHARS,
        CHARS_MATCH,
        CHARS_DIFFER,
        SHOW_DEPENDENCIES,
        UPDATE_CELL,
        ROW_COMPLETE,
        TABLE_COMPLETE,
        TRACEBACK_START,
        TRACEBACK_STEP,
        TRACEBACK_CHAR_ADDED,
        TRACEBACK_COMPLETE,
        COMPLETE
    }
    
    public interface StepListener {
        void onStep(StepType type, int i, int j, Map<String, Object> meta);
    }
    
    // Input
    private String str1;
    private String str2;
    private int m, n;
    
    // DP table
    private int[][] dp;
    
    // Current position
    private int currentI;
    private int currentJ;
    
    // Traceback
    private List<Character> lcs;
    private Set<String> pathCells;  // Cells in traceback path
    private int traceI, traceJ;
    
    // State machine
    private StepType lastStep;
    private boolean done;
    private boolean buildingTable;
    private boolean tracingBack;
    
    // Listener
    private StepListener listener;
    
    public LCSSolver() {
        reset();
    }
    
    public void setStrings(String s1, String s2) {
        this.str1 = s1;
        this.str2 = s2;
        this.m = s1.length();
        this.n = s2.length();
        this.dp = new int[m + 1][n + 1];
        reset();
    }
    
    public void setStepListener(StepListener listener) {
        this.listener = listener;
    }
    
    public void reset() {
        if (dp != null) {
            for (int i = 0; i <= m; i++) {
                Arrays.fill(dp[i], 0);
            }
        }
        currentI = 0;
        currentJ = 0;
        lcs = new ArrayList<>();
        pathCells = new LinkedHashSet<>();
        traceI = 0;
        traceJ = 0;
        lastStep = null;
        done = false;
        buildingTable = true;
        tracingBack = false;
    }
    
    public void step() {
        if (done) return;
        
        if (lastStep == null) {
            // INIT
            lastStep = StepType.INIT;
            notifyListener(StepType.INIT, 0, 0, null);
            return;
        }
        
        if (buildingTable) {
            stepBuildTable();
        } else if (tracingBack) {
            stepTraceback();
        }
    }
    
    private void stepBuildTable() {
        // Build DP table row by row, cell by cell
        if (currentI == 0 && currentJ == 0) {
            // Start from (1, 1)
            currentI = 1;
            currentJ = 1;
        }
        
        if (currentI > m) {
            // Table complete
            lastStep = StepType.TABLE_COMPLETE;
            buildingTable = false;
            tracingBack = true;
            traceI = m;
            traceJ = n;
            notifyListener(StepType.TABLE_COMPLETE, m, n, null);
            return;
        }
        
        // Process current cell
        if (lastStep == StepType.INIT || lastStep == StepType.UPDATE_CELL || lastStep == StepType.ROW_COMPLETE) {
            // Highlight current cell
            lastStep = StepType.HIGHLIGHT_CELL;
            notifyListener(StepType.HIGHLIGHT_CELL, currentI, currentJ, null);
            return;
        }
        
        if (lastStep == StepType.HIGHLIGHT_CELL) {
            // Compare characters
            lastStep = StepType.COMPARE_CHARS;
            Map<String, Object> meta = new HashMap<>();
            meta.put("char1", str1.charAt(currentI - 1));
            meta.put("char2", str2.charAt(currentJ - 1));
            notifyListener(StepType.COMPARE_CHARS, currentI, currentJ, meta);
            return;
        }
        
        if (lastStep == StepType.COMPARE_CHARS) {
            char c1 = str1.charAt(currentI - 1);
            char c2 = str2.charAt(currentJ - 1);
            
            if (c1 == c2) {
                // Characters match
                lastStep = StepType.CHARS_MATCH;
                Map<String, Object> meta = new HashMap<>();
                meta.put("char", c1);
                meta.put("prevValue", dp[currentI - 1][currentJ - 1]);
                notifyListener(StepType.CHARS_MATCH, currentI, currentJ, meta);
            } else {
                // Characters differ
                lastStep = StepType.CHARS_DIFFER;
                Map<String, Object> meta = new HashMap<>();
                meta.put("topValue", dp[currentI - 1][currentJ]);
                meta.put("leftValue", dp[currentI][currentJ - 1]);
                notifyListener(StepType.CHARS_DIFFER, currentI, currentJ, meta);
            }
            return;
        }
        
        if (lastStep == StepType.CHARS_MATCH || lastStep == StepType.CHARS_DIFFER) {
            // Show dependencies
            lastStep = StepType.SHOW_DEPENDENCIES;
            char c1 = str1.charAt(currentI - 1);
            char c2 = str2.charAt(currentJ - 1);
            Map<String, Object> meta = new HashMap<>();
            meta.put("match", c1 == c2);
            meta.put("diagValue", dp[currentI - 1][currentJ - 1]);
            meta.put("topValue", dp[currentI - 1][currentJ]);
            meta.put("leftValue", dp[currentI][currentJ - 1]);
            notifyListener(StepType.SHOW_DEPENDENCIES, currentI, currentJ, meta);
            return;
        }
        
        if (lastStep == StepType.SHOW_DEPENDENCIES) {
            // Update cell value
            char c1 = str1.charAt(currentI - 1);
            char c2 = str2.charAt(currentJ - 1);
            
            if (c1 == c2) {
                dp[currentI][currentJ] = dp[currentI - 1][currentJ - 1] + 1;
            } else {
                dp[currentI][currentJ] = Math.max(dp[currentI - 1][currentJ], dp[currentI][currentJ - 1]);
            }
            
            lastStep = StepType.UPDATE_CELL;
            Map<String, Object> meta = new HashMap<>();
            meta.put("value", dp[currentI][currentJ]);
            notifyListener(StepType.UPDATE_CELL, currentI, currentJ, meta);
            
            // Move to next cell
            currentJ++;
            if (currentJ > n) {
                // Row complete
                currentJ = 1;
                currentI++;
                Map<String, Object> rowMeta = new HashMap<>();
                rowMeta.put("row", currentI - 1);
                notifyListener(StepType.ROW_COMPLETE, currentI - 1, n, rowMeta);
                lastStep = StepType.ROW_COMPLETE;
            }
            return;
        }
    }
    
    private void stepTraceback() {
        if (lastStep == StepType.TABLE_COMPLETE) {
            // Start traceback
            lastStep = StepType.TRACEBACK_START;
            notifyListener(StepType.TRACEBACK_START, traceI, traceJ, null);
            return;
        }
        
        if (traceI == 0 || traceJ == 0) {
            // Traceback complete
            Collections.reverse(lcs);
            lastStep = StepType.TRACEBACK_COMPLETE;
            Map<String, Object> meta = new HashMap<>();
            meta.put("lcs", new ArrayList<>(lcs));
            meta.put("length", lcs.size());
            notifyListener(StepType.TRACEBACK_COMPLETE, 0, 0, meta);
            
            done = true;
            lastStep = StepType.COMPLETE;
            notifyListener(StepType.COMPLETE, 0, 0, meta);
            return;
        }
        
        // Traceback step
        char c1 = str1.charAt(traceI - 1);
        char c2 = str2.charAt(traceJ - 1);
        
        String cellKey = traceI + "," + traceJ;
        pathCells.add(cellKey);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("cellValue", dp[traceI][traceJ]);
        
        if (c1 == c2) {
            // Characters match - this is part of LCS
            lcs.add(c1);
            meta.put("charAdded", c1);
            meta.put("direction", "diagonal");
            notifyListener(StepType.TRACEBACK_CHAR_ADDED, traceI, traceJ, meta);
            traceI--;
            traceJ--;
        } else {
            // Move to cell with larger value
            meta.put("direction", dp[traceI - 1][traceJ] > dp[traceI][traceJ - 1] ? "up" : "left");
            notifyListener(StepType.TRACEBACK_STEP, traceI, traceJ, meta);
            
            if (dp[traceI - 1][traceJ] > dp[traceI][traceJ - 1]) {
                traceI--;
            } else {
                traceJ--;
            }
        }
        
        lastStep = StepType.TRACEBACK_STEP;
    }
    
    private void notifyListener(StepType type, int i, int j, Map<String, Object> meta) {
        if (listener != null) {
            listener.onStep(type, i, j, meta != null ? meta : new HashMap<>());
        }
    }
    
    public boolean isDone() {
        return done;
    }
    
    public int[][] getDP() {
        return dp;
    }
    
    public List<Character> getLCS() {
        return new ArrayList<>(lcs);
    }
    
    public Set<String> getPathCells() {
        return new LinkedHashSet<>(pathCells);
    }
    
    public String getStr1() {
        return str1;
    }
    
    public String getStr2() {
        return str2;
    }
    
    // State management for step-back
    public static class State {
        public String str1, str2;
        public int m, n;
        public int[][] dp;
        public int currentI, currentJ;
        public List<Character> lcs;
        public Set<String> pathCells;
        public int traceI, traceJ;
        public StepType lastStep;
        public boolean done, buildingTable, tracingBack;
    }
    
    public State snapshot() {
        State s = new State();
        s.str1 = str1;
        s.str2 = str2;
        s.m = m;
        s.n = n;
        s.dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) {
            s.dp[i] = dp[i].clone();
        }
        s.currentI = currentI;
        s.currentJ = currentJ;
        s.lcs = new ArrayList<>(lcs);
        s.pathCells = new LinkedHashSet<>(pathCells);
        s.traceI = traceI;
        s.traceJ = traceJ;
        s.lastStep = lastStep;
        s.done = done;
        s.buildingTable = buildingTable;
        s.tracingBack = tracingBack;
        return s;
    }
    
    public void restore(State s) {
        this.str1 = s.str1;
        this.str2 = s.str2;
        this.m = s.m;
        this.n = s.n;
        this.dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) {
            this.dp[i] = s.dp[i].clone();
        }
        this.currentI = s.currentI;
        this.currentJ = s.currentJ;
        this.lcs = new ArrayList<>(s.lcs);
        this.pathCells = new LinkedHashSet<>(s.pathCells);
        this.traceI = s.traceI;
        this.traceJ = s.traceJ;
        this.lastStep = s.lastStep;
        this.done = s.done;
        this.buildingTable = s.buildingTable;
        this.tracingBack = s.tracingBack;
    }
}
