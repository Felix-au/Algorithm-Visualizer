package com.algorithmvisualizer.algorithm;

import java.util.Arrays;

/**
 * Binary Search solver that emits fine-grained steps to drive visualization.
 * It does not perform any time-based delays; the controller orchestrates timing.
 */
public class BinarySearchSolver {

    public enum StepType {
        INIT,
        HIGHLIGHT_MID,
        COMPARE_EQUAL,
        COMPARE_LESS,
        COMPARE_GREATER,
        ELIMINATE_LEFT,
        ELIMINATE_RIGHT,
        MOVE_BOUNDS,
        DONE_FOUND,
        DONE_NOT_FOUND
    }

    public interface StepListener {
        void onStep(StepType type, int low, int mid, int high, int target);
    }

    public static class State {
        public int[] array;
        public int low;
        public int mid;
        public int high;
        public int target;
        public boolean done;
        public int foundIndex;
        public Phase phase;
    }

    private int[] array = new int[0];
    private int low = 0;
    private int mid = -1;
    private int high = -1;
    private int target = 0;
    private boolean done = false;
    private int foundIndex = -1;

    private StepListener listener;

    // Phase machine to ensure one event per step()
    private enum Phase { INIT, HIGHLIGHT, COMPARE, ELIM_LEFT, ELIM_RIGHT, MOVE_BOUNDS, FOUND, DONE }
    private Phase phase = Phase.INIT;

    public BinarySearchSolver(int[] initialArray, int target) {
        setArray(initialArray);
        setTarget(target);
    }

    public void setArray(int[] arr) {
        if (arr == null) arr = new int[0];
        // Expect array sorted ascending for binary search; controller can sort/validate if needed
        this.array = Arrays.copyOf(arr, arr.length);
        reset();
    }

    public void setTarget(int target) {
        this.target = target;
        reset();
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void reset() {
        low = 0;
        high = Math.max(0, array.length - 1);
        mid = (array.length > 0) ? (low + (high - low) / 2) : -1;
        done = array.length == 0;
        foundIndex = -1;
        phase = done ? Phase.DONE : Phase.INIT;
        if (done) emit(StepType.DONE_NOT_FOUND);
        else emit(StepType.INIT);
    }

    public boolean isDone() { return done; }
    public int[] getArray() { return Arrays.copyOf(array, array.length); }
    public int getLow() { return low; }
    public int getMid() { return mid; }
    public int getHigh() { return high; }
    public int getTarget() { return target; }
    public int getFoundIndex() { return foundIndex; }

    public void step() {
        if (done) return;

        // If bounds invalid, terminate
        if (mid < 0 || low > high) {
            done = true;
            phase = Phase.DONE;
            emit(StepType.DONE_NOT_FOUND);
            return;
        }

        switch (phase) {
            case INIT:
                // Next, highlight mid
                phase = Phase.HIGHLIGHT;
                emit(StepType.HIGHLIGHT_MID);
                return;
            case HIGHLIGHT:
                // After highlight, compare
                phase = Phase.COMPARE;
                if (array[mid] == target) {
                    emit(StepType.COMPARE_EQUAL);
                    phase = Phase.FOUND;
                } else if (array[mid] < target) {
                    emit(StepType.COMPARE_LESS);
                    phase = Phase.ELIM_LEFT; // eliminate left (including mid)
                } else {
                    emit(StepType.COMPARE_GREATER);
                    phase = Phase.ELIM_RIGHT; // eliminate right (including mid)
                }
                return;
            case ELIM_LEFT:
                emit(StepType.ELIMINATE_LEFT);
                phase = Phase.MOVE_BOUNDS;
                return;
            case ELIM_RIGHT:
                emit(StepType.ELIMINATE_RIGHT);
                phase = Phase.MOVE_BOUNDS;
                return;
            case MOVE_BOUNDS:
                // Adjust bounds based on last compare
                if (array[mid] < target) {
                    low = mid + 1;
                } else if (array[mid] > target) {
                    high = mid - 1;
                }
                emit(StepType.MOVE_BOUNDS);
                if (low > high) {
                    done = true;
                    phase = Phase.DONE;
                    emit(StepType.DONE_NOT_FOUND);
                    return;
                }
                mid = low + (high - low) / 2;
                phase = Phase.HIGHLIGHT;
                return;
            case FOUND:
                foundIndex = mid;
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
        s.low = low;
        s.mid = mid;
        s.high = high;
        s.target = target;
        s.done = done;
        s.foundIndex = foundIndex;
        s.phase = phase;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        this.array = Arrays.copyOf(s.array, s.array.length);
        this.low = s.low;
        this.mid = s.mid;
        this.high = s.high;
        this.target = s.target;
        this.done = s.done;
        this.foundIndex = s.foundIndex;
        this.phase = s.phase != null ? s.phase : (done ? Phase.DONE : Phase.HIGHLIGHT);
    }

    private void emit(StepType type) {
        if (listener != null) listener.onStep(type, low, mid, high, target);
    }

    public String getCurrentStepDescription() {
        if (done) {
            if (foundIndex >= 0) return "Found target at index " + foundIndex;
            return "Not found. low > high";
        }
        return "low=" + low + ", mid=" + mid + ", high=" + high + ", target=" + target +
                (mid >= 0 && mid < array.length ? ", arr[mid]=" + array[mid] : "");
    }
}
