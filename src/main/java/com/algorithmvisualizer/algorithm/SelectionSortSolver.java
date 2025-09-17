package com.algorithmvisualizer.algorithm;

import java.util.Arrays;

/**
 * Selection Sort solver with granular step events and snapshot/restore for step-back.
 */
public class SelectionSortSolver {

    public enum StepType { INIT_OUTER, INIT_MIN, COMPARE, SET_MIN, END_SCAN, SWAP, MARK_SORTED, DONE }

    public interface StepListener {
        void onStep(StepType type, int i, int j, int minIndex);
    }

    public static class State {
        public int[] array;
        public int i;
        public int j;
        public int minIndex;
        public boolean done;
    }

    private int[] array;
    private int i;
    private int j;
    private int minIndex;
    private boolean done;

    private StepListener stepListener;

    public SelectionSortSolver(int[] initialArray) {
        setArray(initialArray);
    }

    public void setArray(int[] initialArray) {
        if (initialArray == null) initialArray = new int[0];
        this.array = Arrays.copyOf(initialArray, initialArray.length);
        reset();
    }

    public void setStepListener(StepListener listener) {
        this.stepListener = listener;
    }

    public void reset() {
        i = 0;
        j = (array.length > 0 ? 1 : 0);
        minIndex = 0;
        done = array.length <= 1;
        if (done) emit(StepType.DONE);
    }

    public void step() {
        if (done) return;

        int n = array.length;

        // If starting a new outer iteration
        if (j == i + 1 && minIndex == i) {
            emit(StepType.INIT_OUTER);
            emit(StepType.INIT_MIN);
        }

        if (j < n) {
            emit(StepType.COMPARE);
            if (array[j] < array[minIndex]) {
                minIndex = j;
                emit(StepType.SET_MIN);
            }
            j++;
            if (j < n) return; // continue scanning
            emit(StepType.END_SCAN);
        }

        // Finished scanning j..n for this i; perform swap if needed
        if (minIndex != i) {
            int tmp = array[i];
            array[i] = array[minIndex];
            array[minIndex] = tmp;
            emit(StepType.SWAP);
        }

        // Mark current i as sorted and move to next i
        emit(StepType.MARK_SORTED);
        i++;
        if (i >= n - 1) {
            done = true;
            emit(StepType.DONE);
            return;
        }
        minIndex = i;
        j = i + 1;
    }

    public State snapshot() {
        State s = new State();
        s.array = Arrays.copyOf(array, array.length);
        s.i = i;
        s.j = j;
        s.minIndex = minIndex;
        s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        this.array = Arrays.copyOf(s.array, s.array.length);
        this.i = s.i;
        this.j = s.j;
        this.minIndex = s.minIndex;
        this.done = s.done;
    }

    private void emit(StepType type) {
        if (stepListener != null) stepListener.onStep(type, i, j, minIndex);
    }

    // Getters
    public int[] getArray() { return Arrays.copyOf(array, array.length); }
    public int getI() { return i; }
    public int getJ() { return j; }
    public int getMinIndex() { return minIndex; }
    public boolean isDone() { return done; }
}


