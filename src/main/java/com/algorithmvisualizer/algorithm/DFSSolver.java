package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Depth-First Search solver that emits fine-grained steps to drive visualization.
 * Iterative DFS using an explicit stack of frames for step-wise execution.
 */
public class DFSSolver {

    public enum StepType { INIT, DISCOVER, EXPLORE_EDGE, BACKTRACK, DONE }

    public interface StepListener {
        void onStep(StepType type, int u, int v);
    }

    public static class State {
        public int n;
        public List<List<Integer>> adj;
        public boolean[] visited;
        public Deque<Frame> stack;
        public List<Integer> traversal;
        public int start;
        public boolean done;
    }

    public static class Frame {
        public int u;
        public int idx; // next neighbor index to consider
        public Frame(int u, int idx) { this.u = u; this.idx = idx; }
    }

    private int n = 0;
    private List<List<Integer>> adj = new ArrayList<>();
    private boolean[] visited = new boolean[0];
    private Deque<Frame> stack = new ArrayDeque<>();
    private List<Integer> traversal = new ArrayList<>();
    private int start = 0;
    private boolean done = false;

    private StepListener listener;

    public DFSSolver(int n, List<List<Integer>> adj, int start) {
        setGraph(n, adj);
        setStart(start);
    }

    public void setGraph(int n, List<List<Integer>> adj) {
        this.n = Math.max(0, n);
        this.adj = new ArrayList<>(this.n);
        for (int i = 0; i < this.n; i++) {
            if (adj != null && i < adj.size() && adj.get(i) != null) {
                // ensure deterministic neighbor order
                List<Integer> row = new ArrayList<>(adj.get(i));
                Collections.sort(row);
                this.adj.add(row);
            } else {
                this.adj.add(new ArrayList<>());
            }
        }
        resetInternal(false);
    }

    public void setStart(int s) {
        this.start = (s >= 0 && s < n) ? s : 0;
        resetInternal(false);
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void reset() { resetInternal(true); }

    private void resetInternal(boolean emitInit) {
        visited = new boolean[n];
        stack.clear();
        traversal.clear();
        done = (n == 0);
        if (done) {
            emit(StepType.DONE, -1, -1);
            return;
        }
        stack.push(new Frame(start, 0));
        if (emitInit) emit(StepType.INIT, start, -1);
        else emit(StepType.INIT, start, -1);
    }

    public boolean isDone() { return done; }
    public int getN() { return n; }
    public List<List<Integer>> getAdj() { return copyAdj(adj); }
    public boolean[] getVisited() { return Arrays.copyOf(visited, visited.length); }
    public List<Integer> getTraversal() { return new ArrayList<>(traversal); }
    public int getStart() { return start; }

    public void step() {
        if (done) return;
        if (stack.isEmpty()) {
            // try to find next unvisited component
            int next = -1;
            for (int i = 0; i < n; i++) if (!visited[i]) { next = i; break; }
            if (next == -1) {
                done = true;
                emit(StepType.DONE, -1, -1);
                return;
            }
            stack.push(new Frame(next, 0));
            emit(StepType.INIT, next, -1);
            return;
        }

        Frame f = stack.peek();
        int u = f.u;
        if (!visited[u]) {
            visited[u] = true;
            traversal.add(u);
            emit(StepType.DISCOVER, u, -1);
            return;
        }

        // Explore neighbors
        while (f.idx < adj.get(u).size()) {
            int v = adj.get(u).get(f.idx++);
            if (!visited[v]) {
                emit(StepType.EXPLORE_EDGE, u, v);
                stack.push(new Frame(v, 0));
                return;
            }
            // if already visited, skip quietly; allow next step to continue loop
            // but emit an EXPLORE_EDGE with visited info could be noisy
            // we choose not to emit in this branch
        }

        // Backtrack from u
        stack.pop();
        emit(StepType.BACKTRACK, u, -1);
        // Next step will proceed with previous frame or finish
    }

    public State snapshot() {
        State s = new State();
        s.n = n;
        s.adj = copyAdj(adj);
        s.visited = Arrays.copyOf(visited, visited.length);
        s.stack = new ArrayDeque<>();
        for (Frame f : stack) s.stack.addLast(new Frame(f.u, f.idx));
        s.traversal = new ArrayList<>(traversal);
        s.start = start;
        s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        n = s.n;
        adj = copyAdj(s.adj);
        visited = Arrays.copyOf(s.visited, s.visited.length);
        stack = new ArrayDeque<>();
        for (Frame f : s.stack) stack.addLast(new Frame(f.u, f.idx));
        traversal = new ArrayList<>(s.traversal);
        start = s.start;
        done = s.done;
    }

    private void emit(StepType type, int u, int v) {
        if (listener != null) listener.onStep(type, u, v);
    }

    private static List<List<Integer>> copyAdj(List<List<Integer>> src) {
        List<List<Integer>> out = new ArrayList<>(src.size());
        for (List<Integer> row : src) out.add(new ArrayList<>(row));
        return out;
    }

    public String getCurrentStepDescription() {
        if (done) return "DFS complete. Traversal size=" + traversal.size();
        if (stack.isEmpty()) return "Ready";
        Frame f = stack.peek();
        return "At node " + f.u + ", next neighbor index=" + f.idx + ", stack size=" + stack.size();
    }
}
