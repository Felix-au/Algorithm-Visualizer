package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Solver for 0/1 Knapsack Problem using dynamic programming.
 * Implements step-by-step execution with fine-grained visualization
 * similar to LCSSolver — builds DP table cell by cell, then traces back
 * to identify selected items.
 */
public class ZeroOneKnapsackSolver {

    public enum StepType {
        INIT,
        HIGHLIGHT_CELL,
        CHECK_ITEM_WEIGHT,
        ITEM_TOO_HEAVY,
        SHOW_EXCLUDE_OPTION,
        SHOW_INCLUDE_OPTION,
        COMPARE_OPTIONS,
        UPDATE_CELL,
        ROW_COMPLETE,
        TABLE_COMPLETE,
        TRACEBACK_START,
        TRACEBACK_HIGHLIGHT_CELL,
        TRACEBACK_STEP,
        TRACEBACK_ITEM_SELECTED,
        TRACEBACK_ITEM_SKIPPED,
        TRACEBACK_COMPLETE,
        COMPLETE
    }

    public interface StepListener {
        void onStep(StepType type, int i, int w, Map<String, Object> meta);
    }

    /**
     * Item in the knapsack problem
     */
    public static class Item {
        public int id;
        public int weight;
        public int value;
        public boolean selected; // After traceback

        public Item(int id, int weight, int value) {
            this.id = id;
            this.weight = weight;
            this.value = value;
            this.selected = false;
        }

        public Item(Item other) {
            this.id = other.id;
            this.weight = other.weight;
            this.value = other.value;
            this.selected = other.selected;
        }
    }

    // Input
    private Item[] items;
    private int capacity;
    private int n;

    // DP table: dp[i][w] = max value using first i items with capacity w
    private int[][] dp;

    // Current position in table build
    private int currentI;
    private int currentW;

    // Traceback state
    private int traceI, traceW;
    private List<Integer> selectedItems; // Indices of selected items (0-based)
    private Map<String, String> pathCells; // Cells in traceback path: key="i,w",
                                           // value="start"|"checking"|"selected"|"skipped"

    // State machine
    private StepType lastStep;
    private boolean done;
    private boolean buildingTable;
    private boolean tracingBack;

    // Sub-step tracking for cell computation
    private int cellSubStep;

    // Sub-step tracking for traceback (0 = highlight cell, 1 = decide)
    private int traceSubStep;

    // Listener
    private StepListener listener;

    public ZeroOneKnapsackSolver() {
        // Default empty
    }

    public void setItems(Item[] items, int capacity) {
        this.items = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            this.items[i] = new Item(items[i]);
        }
        this.capacity = capacity;
        this.n = items.length;
        this.dp = new int[n + 1][capacity + 1];
        reset();
    }

    public void setStepListener(StepListener listener) {
        this.listener = listener;
    }

    public void reset() {
        if (dp != null) {
            for (int i = 0; i <= n; i++) {
                Arrays.fill(dp[i], 0);
            }
        }
        currentI = 0;
        currentW = 0;
        traceI = 0;
        traceW = 0;
        selectedItems = new ArrayList<>();
        pathCells = new LinkedHashMap<>();
        lastStep = null;
        done = false;
        buildingTable = true;
        tracingBack = false;
        cellSubStep = 0;
        traceSubStep = 0;

        // Reset selection on items
        if (items != null) {
            for (Item item : items) {
                item.selected = false;
            }
        }
    }

    public void step() {
        if (done)
            return;

        if (lastStep == null) {
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
        // Start from (1, 0) — row 0 is all zeros (base case)
        if (currentI == 0 && currentW == 0) {
            currentI = 1;
            currentW = 0;
            cellSubStep = 0;
        }

        if (currentI > n) {
            // Table complete
            lastStep = StepType.TABLE_COMPLETE;
            buildingTable = false;
            tracingBack = true;
            traceI = n;
            traceW = capacity;
            notifyListener(StepType.TABLE_COMPLETE, n, capacity, null);
            return;
        }

        // Process current cell based on sub-step
        Item item = items[currentI - 1]; // 0-based item index

        if (cellSubStep == 0) {
            // Step 1: Highlight cell
            lastStep = StepType.HIGHLIGHT_CELL;
            Map<String, Object> meta = new HashMap<>();
            meta.put("itemId", item.id);
            meta.put("itemWeight", item.weight);
            meta.put("itemValue", item.value);
            meta.put("capacity", currentW);
            notifyListener(StepType.HIGHLIGHT_CELL, currentI, currentW, meta);
            cellSubStep = 1;
            return;
        }

        if (cellSubStep == 1) {
            // Step 2: Check if item weight exceeds current capacity
            lastStep = StepType.CHECK_ITEM_WEIGHT;
            Map<String, Object> meta = new HashMap<>();
            meta.put("itemWeight", item.weight);
            meta.put("capacity", currentW);
            meta.put("fits", item.weight <= currentW);
            notifyListener(StepType.CHECK_ITEM_WEIGHT, currentI, currentW, meta);
            cellSubStep = 2;
            return;
        }

        if (cellSubStep == 2) {
            if (item.weight > currentW) {
                // Item too heavy — can only exclude
                dp[currentI][currentW] = dp[currentI - 1][currentW];
                lastStep = StepType.ITEM_TOO_HEAVY;
                Map<String, Object> meta = new HashMap<>();
                meta.put("excludeValue", dp[currentI - 1][currentW]);
                meta.put("value", dp[currentI][currentW]);
                notifyListener(StepType.ITEM_TOO_HEAVY, currentI, currentW, meta);
                cellSubStep = 5; // Skip to UPDATE_CELL
                return;
            } else {
                // Show exclude option: dp[i-1][w]
                lastStep = StepType.SHOW_EXCLUDE_OPTION;
                Map<String, Object> meta = new HashMap<>();
                meta.put("excludeValue", dp[currentI - 1][currentW]);
                notifyListener(StepType.SHOW_EXCLUDE_OPTION, currentI, currentW, meta);
                cellSubStep = 3;
                return;
            }
        }

        if (cellSubStep == 3) {
            // Show include option: dp[i-1][w - weight] + value
            int includeValue = dp[currentI - 1][currentW - item.weight] + item.value;
            lastStep = StepType.SHOW_INCLUDE_OPTION;
            Map<String, Object> meta = new HashMap<>();
            meta.put("includeValue", includeValue);
            meta.put("prevValue", dp[currentI - 1][currentW - item.weight]);
            meta.put("itemValue", item.value);
            meta.put("lookupW", currentW - item.weight);
            notifyListener(StepType.SHOW_INCLUDE_OPTION, currentI, currentW, meta);
            cellSubStep = 4;
            return;
        }

        if (cellSubStep == 4) {
            // Compare and decide
            int excludeVal = dp[currentI - 1][currentW];
            int includeVal = dp[currentI - 1][currentW - item.weight] + item.value;
            dp[currentI][currentW] = Math.max(excludeVal, includeVal);

            lastStep = StepType.COMPARE_OPTIONS;
            Map<String, Object> meta = new HashMap<>();
            meta.put("excludeValue", excludeVal);
            meta.put("includeValue", includeVal);
            meta.put("chosen", dp[currentI][currentW] == includeVal ? "include" : "exclude");
            meta.put("value", dp[currentI][currentW]);
            notifyListener(StepType.COMPARE_OPTIONS, currentI, currentW, meta);
            cellSubStep = 5;
            return;
        }

        if (cellSubStep == 5) {
            // Update cell value in visualization
            lastStep = StepType.UPDATE_CELL;
            Map<String, Object> meta = new HashMap<>();
            meta.put("value", dp[currentI][currentW]);
            notifyListener(StepType.UPDATE_CELL, currentI, currentW, meta);

            // Move to next cell
            currentW++;
            cellSubStep = 0;

            if (currentW > capacity) {
                // Row complete
                currentW = 0;
                currentI++;
                Map<String, Object> rowMeta = new HashMap<>();
                rowMeta.put("row", currentI - 1);
                notifyListener(StepType.ROW_COMPLETE, currentI - 1, capacity, rowMeta);
                lastStep = StepType.ROW_COMPLETE;
            }
            return;
        }
    }

    private void stepTraceback() {
        if (lastStep == StepType.TABLE_COMPLETE) {
            lastStep = StepType.TRACEBACK_START;
            traceSubStep = 0;
            pathCells.put(traceI + "," + traceW, "start");
            notifyListener(StepType.TRACEBACK_START, traceI, traceW, null);
            return;
        }

        if (traceI == 0 || traceW == 0) {
            // Traceback complete
            lastStep = StepType.TRACEBACK_COMPLETE;
            Map<String, Object> meta = new HashMap<>();
            meta.put("selectedItems", new ArrayList<>(selectedItems));
            meta.put("totalValue", dp[n][capacity]);
            meta.put("totalWeight", computeSelectedWeight());
            notifyListener(StepType.TRACEBACK_COMPLETE, 0, 0, meta);

            done = true;
            lastStep = StepType.COMPLETE;
            notifyListener(StepType.COMPLETE, 0, 0, meta);
            return;
        }

        if (traceSubStep == 0) {
            // Sub-step 0: Highlight the current traceback cell
            lastStep = StepType.TRACEBACK_HIGHLIGHT_CELL;
            Map<String, Object> meta = new HashMap<>();
            meta.put("cellValue", dp[traceI][traceW]);
            meta.put("aboveValue", dp[traceI - 1][traceW]);
            meta.put("itemId", items[traceI - 1].id);
            pathCells.put(traceI + "," + traceW, "checking");
            notifyListener(StepType.TRACEBACK_HIGHLIGHT_CELL, traceI, traceW, meta);
            traceSubStep = 1;
            return;
        }

        // Sub-step 1: Decide include or skip
        traceSubStep = 0;
        Map<String, Object> meta = new HashMap<>();
        meta.put("cellValue", dp[traceI][traceW]);

        if (dp[traceI][traceW] != dp[traceI - 1][traceW]) {
            // Item was included
            Item item = items[traceI - 1];
            item.selected = true;
            selectedItems.add(traceI - 1);
            meta.put("itemId", item.id);
            meta.put("itemWeight", item.weight);
            meta.put("itemValue", item.value);
            pathCells.put(traceI + "," + traceW, "selected");
            notifyListener(StepType.TRACEBACK_ITEM_SELECTED, traceI, traceW, meta);
            traceW -= item.weight;
            traceI--;
        } else {
            // Item was excluded
            meta.put("itemId", items[traceI - 1].id);
            pathCells.put(traceI + "," + traceW, "skipped");
            notifyListener(StepType.TRACEBACK_ITEM_SKIPPED, traceI, traceW, meta);
            traceI--;
        }

        lastStep = StepType.TRACEBACK_STEP;
    }

    private int computeSelectedWeight() {
        int total = 0;
        for (int idx : selectedItems) {
            total += items[idx].weight;
        }
        return total;
    }

    private void notifyListener(StepType type, int i, int w, Map<String, Object> meta) {
        if (listener != null) {
            listener.onStep(type, i, w, meta != null ? meta : new HashMap<>());
        }
    }

    public boolean isDone() {
        return done;
    }

    public int[][] getDP() {
        return dp;
    }

    public Item[] getItems() {
        if (items == null)
            return new Item[0];
        Item[] copy = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            copy[i] = new Item(items[i]);
        }
        return copy;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getN() {
        return n;
    }

    public List<Integer> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public Map<String, String> getPathCells() {
        return new LinkedHashMap<>(pathCells);
    }

    // State management for step-back
    public static class State {
        public Item[] items;
        public int capacity;
        public int n;
        public int[][] dp;
        public int currentI, currentW;
        public int traceI, traceW;
        public List<Integer> selectedItems;
        public Map<String, String> pathCells;
        public StepType lastStep;
        public boolean done, buildingTable, tracingBack;
        public int cellSubStep;
        public int traceSubStep;
    }

    public State snapshot() {
        State s = new State();
        s.items = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            s.items[i] = new Item(items[i]);
        }
        s.capacity = capacity;
        s.n = n;
        s.dp = new int[n + 1][capacity + 1];
        for (int i = 0; i <= n; i++) {
            s.dp[i] = dp[i].clone();
        }
        s.currentI = currentI;
        s.currentW = currentW;
        s.traceI = traceI;
        s.traceW = traceW;
        s.selectedItems = new ArrayList<>(selectedItems);
        s.pathCells = new LinkedHashMap<>(pathCells);
        s.lastStep = lastStep;
        s.done = done;
        s.buildingTable = buildingTable;
        s.tracingBack = tracingBack;
        s.cellSubStep = cellSubStep;
        s.traceSubStep = traceSubStep;
        return s;
    }

    public void restore(State s) {
        this.items = new Item[s.items.length];
        for (int i = 0; i < s.items.length; i++) {
            this.items[i] = new Item(s.items[i]);
        }
        this.capacity = s.capacity;
        this.n = s.n;
        this.dp = new int[n + 1][capacity + 1];
        for (int i = 0; i <= n; i++) {
            this.dp[i] = s.dp[i].clone();
        }
        this.currentI = s.currentI;
        this.currentW = s.currentW;
        this.traceI = s.traceI;
        this.traceW = s.traceW;
        this.selectedItems = new ArrayList<>(s.selectedItems);
        this.pathCells = new LinkedHashMap<>(s.pathCells);
        this.lastStep = s.lastStep;
        this.done = s.done;
        this.buildingTable = s.buildingTable;
        this.tracingBack = s.tracingBack;
        this.cellSubStep = s.cellSubStep;
        this.traceSubStep = s.traceSubStep;
    }
}
