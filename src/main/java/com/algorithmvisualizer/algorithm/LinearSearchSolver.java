package com.algorithmvisualizer.algorithm;

import java.util.Arrays;

/**
 * Linear Search solver that emits fine-grained steps to drive visualization.
 * Uses simplified color scheme: YELLOW (checking) → RED (not target) or GREEN (found)
 */
public class LinearSearchSolver {

    public enum StepType {
        INIT,
        CHECK_INDEX,        // Highlight current index in YELLOW
        NOT_TARGET,         // Current element != target (blink to RED, persist)
        FOUND_TARGET,       // Current element == target (blink to GREEN, persist)
        MOVE_NEXT,          // Advance to next index
        DONE_FOUND,         // Target found at index
        DONE_NOT_FOUND      // Reached end without finding
    }

    public interface StepListener {
        void onStep(StepType type, int currentIndex, int target, int comparisons);
    }

    public static class State {
        public int[] array;
        public int currentIndex;
        public int target;
        public boolean done;
        public int foundIndex;
        public Phase phase;
        public int comparisons;
    }

    private int[] array = new int[0];
    private int currentIndex = 0;
    private int target = 0;
    private boolean done = false;
    private int foundIndex = -1;
    private int comparisons = 0;

    private StepListener listener;

    // Phase machine to ensure one event per step()
    private enum Phase { INIT, CHECK, MOVE, FOUND, DONE }
    private Phase phase = Phase.INIT;

    public LinearSearchSolver(int[] initialArray, int target) {
        setArray(initialArray);
        setTarget(target);
    }

    public void setArray(int[] arr) {
        if (arr == null) arr = new int[0];
        this.array = Arrays.copyOf(arr, arr.length);
        reset();
    }

    public void setTarget(int target) {
        this.target = target;
        reset();
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void reset() {
        currentIndex = 0;
        done = array.length == 0;
        foundIndex = -1;
        comparisons = 0;
        phase = done ? Phase.DONE : Phase.INIT;
        if (done) emit(StepType.DONE_NOT_FOUND);
        else emit(StepType.INIT);
    }

    public boolean isDone() { return done; }
    public int[] getArray() { return Arrays.copyOf(array, array.length); }
    public int getCurrentIndex() { return currentIndex; }
    public int getTarget() { return target; }
    public int getFoundIndex() { return foundIndex; }
    public int getComparisons() { return comparisons; }

    public void step() {
        if (done) return;

        // If index out of bounds, terminate
        if (currentIndex < 0 || currentIndex >= array.length) {
            done = true;
            phase = Phase.DONE;
            emit(StepType.DONE_NOT_FOUND);
            return;
        }

        switch (phase) {
            case INIT:
                // Next, check first index
                phase = Phase.CHECK;
                emit(StepType.CHECK_INDEX);
                return;
                
            case CHECK:
                // After highlighting, compare directly
                comparisons++;
                if (array[currentIndex] == target) {
                    emit(StepType.FOUND_TARGET);
                    phase = Phase.FOUND;
                } else {
                    emit(StepType.NOT_TARGET);
                    phase = Phase.MOVE;
                }
                return;
                
            case MOVE:
                // Move to next index
                currentIndex++;
                emit(StepType.MOVE_NEXT);
                
                if (currentIndex >= array.length) {
                    done = true;
                    phase = Phase.DONE;
                    emit(StepType.DONE_NOT_FOUND);
                    return;
                }
                
                phase = Phase.CHECK;
                return;
                
            case FOUND:
                foundIndex = currentIndex;
                done = true;
                phase = Phase.DONE;
                emit(StepType.DONE_FOUND);
                return;
                
            case DONE:
                return;
        }
    }

    public State snapshot() {
        State s = new State();
        s.array = Arrays.copyOf(array, array.length);
        s.currentIndex = currentIndex;
        s.target = target;
        s.done = done;
        s.foundIndex = foundIndex;
        s.phase = phase;
        s.comparisons = comparisons;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        this.array = Arrays.copyOf(s.array, s.array.length);
        this.currentIndex = s.currentIndex;
        this.target = s.target;
        this.done = s.done;
        this.foundIndex = s.foundIndex;
        this.phase = s.phase != null ? s.phase : (done ? Phase.DONE : Phase.CHECK);
        this.comparisons = s.comparisons;
    }

    private void emit(StepType type) {
        if (listener != null) listener.onStep(type, currentIndex, target, comparisons);
    }

    public String getCurrentStepDescription() {
        if (done) {
            if (foundIndex >= 0) return "Found target at index " + foundIndex;
            return "Not found. Checked all " + array.length + " elements.";
        }
        return "currentIndex=" + currentIndex + ", target=" + target + ", comparisons=" + comparisons +
                (currentIndex >= 0 && currentIndex < array.length ? ", arr[" + currentIndex + "]=" + array[currentIndex] : "");
    }
}
