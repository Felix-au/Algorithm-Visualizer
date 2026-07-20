package com.algorithmvisualizer.algorithm;

import java.util.Arrays;

/**
 * Fractional Knapsack solver with granular step-by-step visualization.
 * Greedy algorithm: sort by value/weight ratio, take items greedily.
 */
public class FractionalKnapsackSolver {

    public enum StepType {
        INIT,                   // Initialize problem
        CALCULATE_RATIOS,       // Calculate value/weight ratios
        DISPLAY_RATIOS,         // Show calculated ratios
        SORT_START,             // Begin sorting by ratio
        COMPARE_RATIOS,         // Compare two items
        SWAP_HIGHLIGHT,         // Highlight items to swap (red blink) - WAIT 1 SEC
        SWAP_ITEMS,             // Swap items during sort
        SWAP_COMPLETE,          // Swap animation complete
        SORT_COMPLETE,          // Sorting finished
        SELECT_ITEM,            // Select current item - SHOW POINTER
        HOVER_ITEM,             // Hover pointer over item for 1 sec
        CHECK_CAPACITY,         // Check if item fits
        TAKE_FULL,              // Take entire item
        TAKE_PARTIAL,           // Take fraction of item
        UPDATE_KNAPSACK,        // Update capacity and value - WAIT 1 SEC
        MARK_COMPLETE,          // Mark item as taken
        SKIP_ITEM,              // Skip item (capacity full)
        DONE                    // Algorithm complete
    }

    public interface StepListener {
        void onStep(StepType type, State state);
    }

    /**
     * Item in the knapsack problem
     */
    public static class Item {
        public int id;                  // Item number (1-based)
        public double weight;           // Item weight
        public double value;            // Item value
        public double ratio;            // value/weight ratio
        public double fractionTaken;    // 0.0 to 1.0
        public double valueGained;      // value * fractionTaken
        public boolean isConsidered;    // Has been evaluated
        public boolean isTaken;         // Fully or partially taken

        public Item(int id, double weight, double value) {
            this.id = id;
            this.weight = weight;
            this.value = value;
            this.ratio = 0.0;
            this.fractionTaken = 0.0;
            this.valueGained = 0.0;
            this.isConsidered = false;
            this.isTaken = false;
        }

        public Item(Item other) {
            this.id = other.id;
            this.weight = other.weight;
            this.value = other.value;
            this.ratio = other.ratio;
            this.fractionTaken = other.fractionTaken;
            this.valueGained = other.valueGained;
            this.isConsidered = other.isConsidered;
            this.isTaken = other.isTaken;
        }
    }

    /**
     * Complete state snapshot
     */
    public static class State {
        public Item[] items;
        public double capacity;
        public double remainingCapacity;
        public double totalValue;
        public int currentIndex;
        public String phase;            // INIT, CALCULATE, SORT, SELECT, DONE
        public int compareIndex1;       // For sorting visualization
        public int compareIndex2;       // For sorting visualization
        public boolean done;

        public State() {}

        public State(State other) {
            this.items = new Item[other.items.length];
            for (int i = 0; i < other.items.length; i++) {
                this.items[i] = new Item(other.items[i]);
            }
            this.capacity = other.capacity;
            this.remainingCapacity = other.remainingCapacity;
            this.totalValue = other.totalValue;
            this.currentIndex = other.currentIndex;
            this.phase = other.phase;
            this.compareIndex1 = other.compareIndex1;
            this.compareIndex2 = other.compareIndex2;
            this.done = other.done;
        }
    }

    // Solver state
    private Item[] items;
    private double capacity;
    private double remainingCapacity;
    private double totalValue;
    private int currentIndex;
    private String phase;
    private boolean done;

    // Sorting state
    private int sortI, sortJ;
    private boolean sorting;
    private boolean sortingComplete;

    // Step listener
    private StepListener stepListener;

    public FractionalKnapsackSolver(Item[] items, double capacity) {
        this.items = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            this.items[i] = new Item(items[i]);
        }
        this.capacity = capacity;
        reset();
    }

    public void setStepListener(StepListener listener) {
        this.stepListener = listener;
    }

    public void reset() {
        remainingCapacity = capacity;
        totalValue = 0.0;
        currentIndex = 0;
        phase = "INIT";
        done = false;
        sorting = false;
        sortingComplete = false;
        sortI = 0;
        sortJ = 0;

        // Reset all items
        for (Item item : items) {
            item.ratio = 0.0;
            item.fractionTaken = 0.0;
            item.valueGained = 0.0;
            item.isConsidered = false;
            item.isTaken = false;
        }

        emit(StepType.INIT);
    }

    /**
     * Execute one granular step
     */
    public void step() {
        if (done) return;

        switch (phase) {
            case "INIT":
                // Calculate ratios
                phase = "CALCULATE";
                for (Item item : items) {
                    item.ratio = item.value / item.weight;
                }
                emit(StepType.CALCULATE_RATIOS);
                break;

            case "CALCULATE":
                // Display ratios
                phase = "DISPLAY";
                emit(StepType.DISPLAY_RATIOS);
                break;

            case "DISPLAY":
                // Start sorting
                phase = "SORT";
                sorting = true;
                sortI = 0;
                sortJ = 0;
                emit(StepType.SORT_START);
                break;

            case "SORT":
                if (!sortingComplete) {
                    stepBubbleSort();
                } else {
                    phase = "SELECT";
                    currentIndex = 0;
                    emit(StepType.SORT_COMPLETE);
                }
                break;

            case "SELECT":
                if (currentIndex >= items.length || remainingCapacity <= 0.0001) {
                    // Done
                    phase = "DONE";
                    done = true;
                    emit(StepType.DONE);
                } else {
                    selectCurrentItem();
                }
                break;

            case "DONE":
                // Already done
                break;
        }
    }

    /**
     * Bubble sort with granular steps (descending by ratio)
     */
    private void stepBubbleSort() {
        int n = items.length;

        if (sortI >= n - 1) {
            sortingComplete = true;
            return;
        }

        if (sortJ >= n - sortI - 1) {
            sortI++;
            sortJ = 0;
            return;
        }

        // Compare adjacent items
        if (items[sortJ].ratio < items[sortJ + 1].ratio) {
            emit(StepType.COMPARE_RATIOS);
            // Highlight items to swap (red blink)
            emit(StepType.SWAP_HIGHLIGHT);
            // Swap needed
            Item temp = items[sortJ];
            items[sortJ] = items[sortJ + 1];
            items[sortJ + 1] = temp;
            emit(StepType.SWAP_ITEMS);
            // Swap complete
            emit(StepType.SWAP_COMPLETE);
        } else {
            emit(StepType.COMPARE_RATIOS);
        }

        sortJ++;
    }

    /**
     * Select and process current item
     */
    private void selectCurrentItem() {
        Item item = items[currentIndex];
        item.isConsidered = true;
        emit(StepType.SELECT_ITEM);
        
        // Hover over item
        emit(StepType.HOVER_ITEM);

        // Check capacity
        emit(StepType.CHECK_CAPACITY);

        if (item.weight <= remainingCapacity) {
            // Take full item
            item.fractionTaken = 1.0;
            item.valueGained = item.value;
            item.isTaken = true;
            totalValue += item.value;
            remainingCapacity -= item.weight;
            emit(StepType.TAKE_FULL);
        } else if (remainingCapacity > 0.0001) {
            // Take partial item
            item.fractionTaken = remainingCapacity / item.weight;
            item.valueGained = item.value * item.fractionTaken;
            item.isTaken = true;
            totalValue += item.valueGained;
            remainingCapacity = 0.0;
            emit(StepType.TAKE_PARTIAL);
        } else {
            // Skip item
            emit(StepType.SKIP_ITEM);
        }

        emit(StepType.UPDATE_KNAPSACK);
        emit(StepType.MARK_COMPLETE);
        currentIndex++;
    }

    /**
     * Create state snapshot
     */
    public State snapshot() {
        State s = new State();
        s.items = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            s.items[i] = new Item(items[i]);
        }
        s.capacity = capacity;
        s.remainingCapacity = remainingCapacity;
        s.totalValue = totalValue;
        s.currentIndex = currentIndex;
        s.phase = phase;
        s.compareIndex1 = sortJ;
        s.compareIndex2 = sortJ + 1;
        s.done = done;
        return s;
    }

    /**
     * Restore from snapshot
     */
    public void restore(State s) {
        if (s == null) return;
        this.items = new Item[s.items.length];
        for (int i = 0; i < s.items.length; i++) {
            this.items[i] = new Item(s.items[i]);
        }
        this.capacity = s.capacity;
        this.remainingCapacity = s.remainingCapacity;
        this.totalValue = s.totalValue;
        this.currentIndex = s.currentIndex;
        this.phase = s.phase;
        this.done = s.done;
    }

    private void emit(StepType type) {
        if (stepListener != null) {
            stepListener.onStep(type, snapshot());
        }
    }

    // Getters
    public Item[] getItems() {
        Item[] copy = new Item[items.length];
        for (int i = 0; i < items.length; i++) {
            copy[i] = new Item(items[i]);
        }
        return copy;
    }

    public double getCapacity() { return capacity; }
    public double getRemainingCapacity() { return remainingCapacity; }
    public double getTotalValue() { return totalValue; }
    public int getCurrentIndex() { return currentIndex; }
    public String getPhase() { return phase; }
    public boolean isDone() { return done; }

    public String getCurrentStepDescription() {
        if (done) return "Done. Total value: $" + String.format("%.2f", totalValue);
        if ("INIT".equals(phase)) return "Initializing Fractional Knapsack...";
        if ("CALCULATE".equals(phase)) return "Calculating value/weight ratios...";
        if ("DISPLAY".equals(phase)) return "Displaying ratios...";
        if ("SORT".equals(phase)) return "Sorting items by ratio...";
        if ("SELECT".equals(phase)) return "Selecting items greedily...";
        return "Processing...";
    }
}
