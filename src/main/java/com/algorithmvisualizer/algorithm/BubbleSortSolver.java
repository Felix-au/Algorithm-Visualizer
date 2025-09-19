package com.algorithmvisualizer.algorithm;

import java.util.Arrays;

/**
 * Bubble Sort solver with granular step events and snapshot/restore for step-back.
 */
public class BubbleSortSolver {

    public enum StepType { INIT_PASS, COMPARE, SWAP, ADVANCE, MARK_SORTED, DONE }

    public interface StepListener {
        void onStep(StepType type, int i, int j);
    }

    public static class State {
        public int[] array;
        public int i; // current pass (0..n-2)
        public int j; // current index for comparison (0..n-2-i)
        public boolean done;
    }

    private int[] array;
    private int i;
    private int j;
    private boolean done;

    private StepListener stepListener;

    public BubbleSortSolver(int[] initialArray) {
        setArray(initialArray);
    }

    public void setArray(int[] initialArray) {
        if (initialArray == null) initialArray = new int[0];
        this.array = Arrays.copyOf(initialArray, initialArray.length);
        reset();
    }

    public void setStepListener(StepListener listener) { this.stepListener = listener; }

    public void reset() {
        int n = array.length;
        i = 0;
        j = 0;
        done = n <= 1;
        if (!done) emit(StepType.INIT_PASS);
        else emit(StepType.DONE);
    }

    public void step() {
        if (done) return;
        int n = array.length;

        // When starting or after advancing to next pass, INIT_PASS is emitted
        if (j <= 0 && i < n - 1) {
            emit(StepType.INIT_PASS);
        }

        if (j < n - 1 - i) {
            emit(StepType.COMPARE);
            if (array[j] > array[j + 1]) {
                int tmp = array[j];
                array[j] = array[j + 1];
                array[j + 1] = tmp;
                emit(StepType.SWAP);
            }
            j++;
            emit(StepType.ADVANCE);
            if (j < n - 1 - i) return; // continue current pass
        }

        // End of pass: the largest element for this pass is at n-1-i
        emit(StepType.MARK_SORTED);
        i++;
        j = 0;
        if (i >= n - 1) {
            done = true;
            emit(StepType.DONE);
            return;
        }
    }

    public State snapshot() {
        State s = new State();
        s.array = Arrays.copyOf(array, array.length);
        s.i = i;
        s.j = j;
        s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        this.array = Arrays.copyOf(s.array, s.array.length);
        this.i = s.i;
        this.j = s.j;
        this.done = s.done;
    }

    private void emit(StepType type) {
        if (stepListener != null) stepListener.onStep(type, i, j);
    }

    public int[] getArray() { return Arrays.copyOf(array, array.length); }
    public int getI() { return i; }
    public int getJ() { return j; }
    public boolean isDone() { return done; }

    public String getCurrentStepDescription() {
        int n = array != null ? array.length : 0;
        if (done) return "Done. Array sorted (n=" + n + ")";
        if (j == 0) return "Start pass i=" + i + "; scanning j from 0 to " + (n - 2 - i);
        if (j <= n - 1 - i) return "Comparing j=" + (j - 1) + " and j+1=" + j + " in pass i=" + i;
        return "i=" + i + ", j=" + j;
    }
}


