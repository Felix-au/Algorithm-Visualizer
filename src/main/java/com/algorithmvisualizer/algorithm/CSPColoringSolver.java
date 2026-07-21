package com.algorithmvisualizer.algorithm;

import com.algorithmvisualizer.model.ColoringMode;
import java.util.*;

public class CSPColoringSolver implements GraphColoringSolver {
    private int n;
    private List<List<Integer>> adj;
    private int[] coloring;
    private int[] degrees;
    private Map<Integer, Set<Integer>> domains;
    private Deque<Integer> assignOrder = new ArrayDeque<>();
    private Deque<Map<Integer, Boolean>> removedMapStack = new ArrayDeque<>();
    private ColoringMode mode = ColoringMode.FIND_MINIMUM;
    private int maxColors;
    private int attemptK = 0;
    private boolean done = false;
    private StepListener listener;
    // granular stepping state
    private Integer pendingNode = null;         // node selected (SELECT_NODE emitted)
    private Integer pendingColor = null;        // color chosen to try (TRY_COLOR emitted)
    private Deque<Integer> reduceQueue = new ArrayDeque<>(); // neighbors to process domain reductions
    private Map<Integer, Boolean> currentRemoved = new HashMap<>();

    @Override
    public void setGraph(int n, List<List<Integer>> adj) {
        this.n = Math.max(0, n);
        this.adj = copyAdj(adj);
        this.coloring = new int[this.n];
        Arrays.fill(this.coloring, -1);
        this.degrees = new int[this.n];
        for (int i = 0; i < this.n; i++) {
            this.degrees[i] = (this.adj != null && i < this.adj.size()) ? this.adj.get(i).size() : 0;
        }
        this.domains = new HashMap<>();
        this.assignOrder.clear();
        this.removedMapStack.clear();
        this.done = false;
        this.attemptK = 0;
        this.pendingNode = null;
        this.pendingColor = null;
        this.reduceQueue.clear();
        this.currentRemoved.clear();
    }

    @Override
    public void setMode(ColoringMode mode) { this.mode = mode; }

    @Override
    public void setMaxColors(int k) { this.maxColors = Math.max(2, k); initDomains(this.maxColors); }

    @Override
    public void setStepListener(StepListener l) { this.listener = l; }

    @Override
    public void reset() {
        Arrays.fill(this.coloring, -1);
        this.assignOrder.clear();
        this.removedMapStack.clear();
        this.done = false;
        if (mode == ColoringMode.FIND_MINIMUM) {
            this.attemptK = 0;
        }
        initDomains(mode == ColoringMode.USE_K_COLORS ? maxColors : Math.max(2, maxColors));
        this.pendingNode = null;
        this.pendingColor = null;
        this.reduceQueue.clear();
        this.currentRemoved.clear();
    }

    @Override
    public void step() {
        if (done) return;
        if (mode == ColoringMode.FIND_MINIMUM) {
            stepFindMinimum();
        } else {
            stepUseK();
        }
    }

    private void stepFindMinimum() {
        if (attemptK == 0) {
            attemptK = 2;
            emit(StepType.TRY_K_COLORS, -1, attemptK);
            initDomains(attemptK);
            return;
        }
        boolean progressed = attemptColoringStep();
        if (allAssigned()) {
            emit(StepType.SOLUTION_FOUND, -1, countActualColors());
            done = true;
            return;
        }
        if (!progressed && assignOrder.isEmpty()) {
            emit(StepType.NO_SOLUTION, -1, attemptK);
            attemptK++;
            emit(StepType.TRY_K_COLORS, -1, attemptK);
            initDomains(attemptK);
        }
    }

    private void stepUseK() {
        boolean progressed = attemptColoringStep();
        if (allAssigned()) {
            emit(StepType.SOLUTION_FOUND, -1, countActualColors());
            done = true;
            return;
        }
        if (!progressed && assignOrder.isEmpty()) {
            emit(StepType.NO_SOLUTION, -1, maxColors);
            done = true;
        }
    }

    private boolean attemptColoringStep() {
        // 1) If any unassigned node has empty domain and there is a previous assignment, backtrack
        boolean emptyDomainExists = false;
        for (int i = 0; i < n; i++) {
            if (coloring[i] == -1 && domains.get(i).isEmpty()) { emptyDomainExists = true; break; }
        }
        if (emptyDomainExists) {
            if (!assignOrder.isEmpty()) { backtrack(); return true; }
            // root with empty domain: no progress possible at this k
            return false;
        }

        // 2) If in reduction phase (after assignment), process one neighbor per step
        if (pendingNode != null && pendingColor != null && coloring[pendingNode] != -1 && !reduceQueue.isEmpty()) {
            int nb = reduceQueue.removeFirst();
            if (coloring[nb] == -1 && domains.get(nb).remove(pendingColor)) {
                currentRemoved.put(nb, Boolean.TRUE);
                emit(StepType.REDUCE_DOMAIN, nb, pendingColor);
                return true;
            }
            // even if no removal, we count as a step to be granular
            return true;
        }

        // 3) If a node was selected but no color tried yet
        if (pendingNode != null && pendingColor == null) {
            // choose first color from domain
            if (domains.get(pendingNode).isEmpty()) {
                // domain exhausted at selection time -> backtrack or fail
                if (!assignOrder.isEmpty()) { backtrack(); return true; }
                return false;
            }
            int color = domains.get(pendingNode).iterator().next();
            pendingColor = color;
            emit(StepType.TRY_COLOR, pendingNode, color);
            return true;
        }

        // 4) If a color was tried but not yet assigned, assign now and start reductions lazily
        if (pendingNode != null && pendingColor != null && coloring[pendingNode] == -1) {
            coloring[pendingNode] = pendingColor;
            assignOrder.addLast(pendingNode);
            emit(StepType.ASSIGN_COLOR, pendingNode, pendingColor);
            // initialize reduction queue with neighbors
            reduceQueue.clear();
            for (int nb : adj.get(pendingNode)) reduceQueue.addLast(nb);
            currentRemoved.clear();
            return true;
        }

        // 5) If reduction finished after assignment, commit removed map
        if (pendingNode != null && pendingColor != null && coloring[pendingNode] != -1 && reduceQueue.isEmpty()) {
            removedMapStack.addLast(new HashMap<>(currentRemoved));
            currentRemoved.clear();
            // Reset pending to move on
            pendingNode = null;
            pendingColor = null;
            return true;
        }

        // 6) Otherwise, select a new node (emit SELECT_NODE)
        int node = selectSmallestDomainNode();
        if (node == -1) return false; // no move
        pendingNode = node;
        emit(StepType.SELECT_NODE, node, -1);
        return true;
    }

    private void backtrack() {
        if (assignOrder.isEmpty()) return;
        int node = assignOrder.removeLast();
        Map<Integer, Boolean> removed = removedMapStack.isEmpty() ? new HashMap<>() : new HashMap<>(removedMapStack.removeLast());
        // Merge any in-progress reductions (if we backtrack before committing)
        if (!currentRemoved.isEmpty()) {
            for (Map.Entry<Integer, Boolean> e : currentRemoved.entrySet()) {
                removed.putIfAbsent(e.getKey(), e.getValue());
            }
        }
        int usedColor = coloring[node];
        coloring[node] = -1;
        // Restore neighbor domains
        for (Map.Entry<Integer, Boolean> e : removed.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) {
                domains.get(e.getKey()).add(usedColor);
            }
        }
        // Remove tried color from node's domain so next time we try different one
        domains.get(node).remove(usedColor);
        emit(StepType.BACKTRACK, node, usedColor);
        // clear pending actions that might point to invalid state
        pendingNode = null;
        pendingColor = null;
        reduceQueue.clear();
        currentRemoved.clear();
    }

    private int selectSmallestDomainNode() {
        int min = Integer.MAX_VALUE;
        int sel = -1;
        for (int i = 0; i < n; i++) {
            if (coloring[i] != -1) continue;
            int ds = domains.get(i).size();
            if (ds < min) { min = ds; sel = i; }
            else if (ds == min) {
                // tie-break by higher degree
                if (sel == -1 || degrees[i] > degrees[sel]) sel = i;
            }
        }
        return sel;
    }

    private boolean allAssigned() {
        for (int v : coloring) if (v == -1) return false;
        return true;
    }

    private int countActualColors() {
        int max = -1;
        for (int v : coloring) max = Math.max(max, v);
        return max + 1;
    }

    private void initDomains(int k) {
        domains.clear();
        for (int i = 0; i < n; i++) {
            Set<Integer> s = new LinkedHashSet<>();
            for (int c = 0; c < k; c++) s.add(c);
            domains.put(i, s);
        }
        // Reset assignments
        Arrays.fill(coloring, -1);
        assignOrder.clear();
        removedMapStack.clear();
    }

    @Override
    public boolean isDone() { return done; }

    @Override
    public int[] getColoring() { return coloring.clone(); }

    @Override
    public int getColorsUsed() { return countActualColors(); }

    @Override
    public State snapshot() {
        State s = new State();
        s.n = n;
        s.coloring = coloring.clone();
        s.currentNode = assignOrder.isEmpty() ? -1 : assignOrder.getLast();
        s.currentColor = -1;
        s.attemptK = (mode == ColoringMode.FIND_MINIMUM ? attemptK : maxColors);
        s.done = done;
        // deep copy domains
        Map<Integer, Set<Integer>> dcopy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> e : domains.entrySet()) dcopy.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
        s.domains = dcopy;
        return s;
    }

    @Override
    public void restore(State s) {
        if (s == null) return;
        this.n = s.n;
        this.coloring = s.coloring.clone();
        this.attemptK = s.attemptK;
        this.done = s.done;
        Map<Integer, Set<Integer>> dcopy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> e : s.domains.entrySet()) dcopy.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
        this.domains = dcopy;
        // rebuild assignOrder heuristically
        this.assignOrder.clear();
        boolean[] assigned = new boolean[n];
        for (int i = 0; i < n; i++) if (coloring[i] != -1) { assignOrder.add(i); assigned[i] = true; }
        this.removedMapStack.clear(); // can't reconstruct precisely
    }

    private void emit(StepType t, int node, int color) {
        if (listener != null) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("attemptK", attemptK == 0 ? maxColors : attemptK);
            meta.put("degree", node >= 0 && node < n ? degrees[node] : -1);
            meta.put("domain", node >= 0 ? new LinkedHashSet<>(domains.getOrDefault(node, Collections.emptySet())) : Collections.emptySet());
            listener.onStep(t, node, color, meta);
        }
    }

    private List<List<Integer>> copyAdj(List<List<Integer>> src) {
        List<List<Integer>> out = new ArrayList<>();
        if (src == null) return out;
        for (List<Integer> row : src) out.add(new ArrayList<>(row));
        return out;
    }
}
