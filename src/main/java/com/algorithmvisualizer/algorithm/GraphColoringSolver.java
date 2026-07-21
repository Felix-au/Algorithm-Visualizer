package com.algorithmvisualizer.algorithm;

import com.algorithmvisualizer.model.ColoringMode;
import java.util.*;

public interface GraphColoringSolver {
    enum StepType {
        INIT,
        TRY_K_COLORS,
        SELECT_NODE,
        TRY_COLOR,
        CHECK_CONSTRAINT,
        ASSIGN_COLOR,
        REDUCE_DOMAIN,
        BACKTRACK,
        SOLUTION_FOUND,
        NO_SOLUTION,
        DONE
    }

    interface StepListener {
        void onStep(StepType type, int node, int color, Map<String, Object> meta);
    }

    void setGraph(int n, List<List<Integer>> adj);
    void setMode(ColoringMode mode);
    void setMaxColors(int k);
    void setStepListener(StepListener l);

    void reset();
    void step();
    boolean isDone();

    int[] getColoring();
    int getColorsUsed();

    State snapshot();
    void restore(State s);

    class State {
        public int n;
        public int[] coloring;
        public int currentNode;
        public int currentColor;
        public int attemptK;
        public boolean done;
        public Map<Integer, Set<Integer>> domains; // optional for CSP
    }
}
