package com.algorithmvisualizer.algorithm;

import java.util.*;

public class BellmanFordSolver {

    public enum StepType {
        INIT,
        HIGHLIGHT_SOURCE,
        INITIALIZE_DISTANCES,
        START_ITERATION,
        RESET_EDGES_FOR_ITERATION,
        SELECT_EDGE,
        CALCULATE_DISTANCE,
        COMPARE_DISTANCES,
        UPDATE_DISTANCE,
        REJECT_DISTANCE,
        END_ITERATION,
        SHOW_ITERATION_SUMMARY,
        EARLY_TERMINATION,
        START_NEGATIVE_CYCLE_CHECK,
        CHECK_NEGATIVE_CYCLE_SELECT,
        CHECK_NEGATIVE_CYCLE_CALC,
        CHECK_NEGATIVE_CYCLE_COMPARE,
        NEG_CHECK_SKIP,
        NEGATIVE_CYCLE_FOUND,
        TRACE_NEGATIVE_CYCLE,
        NO_NEGATIVE_CYCLE,
        PATH_ANIMATION_START,
        PATH_ANIMATION_NODE,
        PATH_ANIMATION_EDGE,
        PATH_ANIMATION_END,
        ALL_PATHS_COMPLETE,
        COMPLETE
    }

    public interface StepListener {
        void onStep(StepType type, int u, int v, int distance, Map<String, Object> metadata);
    }

    // Graph structure
    private int n;
    private int[][] edgeList; // [i] = {u, v, weight}
    private List<List<Edge>> adj;
    private int source;

    // Algorithm state
    private int[] dist;
    private int[] prev;
    private int currentIteration;
    private int currentEdgeIndex;
    private boolean changedInIteration;

    // Step tracking
    private StepType lastStep;
    private boolean done;
    private StepListener listener;

    // Path reconstruction / animation
    private List<List<Integer>> allPaths;
    private int currentAnimatingPath;
    private int currentAnimatingStep;
    private boolean hasNegativeCycle;
    private int negCycleEdgeIndex;

    public static class Edge {
        public int to;
        public int weight;

        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    public static class State {
        public int n;
        public int[][] edgeList;
        public List<List<Edge>> adj;
        public int source;
        public int[] dist;
        public int[] prev;
        public int currentIteration;
        public int currentEdgeIndex;
        public boolean changedInIteration;
        public StepType lastStep;
        public boolean done;
        public List<List<Integer>> allPaths;
        public int currentAnimatingPath;
        public int currentAnimatingStep;
        public boolean hasNegativeCycle;
        public int negCycleEdgeIndex;
    }

    public void setGraph(int n, int[][] edges, int source) {
        this.n = n;
        this.source = source;
        this.edgeList = edges;

        // Build adjacency list (for path rendering, undirected)
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int weight = edge[2];
            adj.get(u).add(new Edge(v, weight));
            adj.get(v).add(new Edge(u, weight));
        }

        reset();
    }

    public void reset() {
        dist = new int[n];
        prev = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);

        currentIteration = 0;
        currentEdgeIndex = 0;
        changedInIteration = false;

        lastStep = null;
        done = false;
        allPaths = null;
        currentAnimatingPath = 0;
        currentAnimatingStep = 0;
        hasNegativeCycle = false;
        negCycleEdgeIndex = -1;
    }

    public void setStepListener(StepListener listener) {
        this.listener = listener;
    }

    public void step() {
        if (done)
            return;

        Map<String, Object> meta = new HashMap<>();

        // ---- INIT ----
        if (lastStep == null) {
            lastStep = StepType.INIT;
            notifyListener(StepType.INIT, source, -1, 0, null);
            return;
        }

        // ---- HIGHLIGHT_SOURCE ----
        if (lastStep == StepType.INIT) {
            lastStep = StepType.HIGHLIGHT_SOURCE;
            notifyListener(StepType.HIGHLIGHT_SOURCE, source, -1, 0, null);
            return;
        }

        // ---- INITIALIZE_DISTANCES ----
        if (lastStep == StepType.HIGHLIGHT_SOURCE) {
            dist[source] = 0;
            lastStep = StepType.INITIALIZE_DISTANCES;
            notifyListener(StepType.INITIALIZE_DISTANCES, source, -1, 0, null);
            return;
        }

        // ---- SHOW_ITERATION_SUMMARY (after END_ITERATION) ----
        if (lastStep == StepType.END_ITERATION) {
            lastStep = StepType.SHOW_ITERATION_SUMMARY;
            meta.put("iteration", currentIteration);
            meta.put("changed", changedInIteration);
            meta.put("distances", dist.clone());
            notifyListener(StepType.SHOW_ITERATION_SUMMARY, -1, -1, currentIteration, meta);
            return;
        }

        // ---- START_ITERATION or EARLY_TERMINATION ----
        if (lastStep == StepType.INITIALIZE_DISTANCES ||
                lastStep == StepType.SHOW_ITERATION_SUMMARY) {

            // Check for early termination: no changes in last iteration
            if (lastStep == StepType.SHOW_ITERATION_SUMMARY && !changedInIteration) {
                lastStep = StepType.EARLY_TERMINATION;
                meta.put("iteration", currentIteration);
                notifyListener(StepType.EARLY_TERMINATION, -1, -1, currentIteration, meta);
                return;
            }

            if (currentIteration >= n - 1) {
                // All V-1 iterations done — start negative cycle check
                negCycleEdgeIndex = 0;
                lastStep = StepType.START_NEGATIVE_CYCLE_CHECK;
                notifyListener(StepType.START_NEGATIVE_CYCLE_CHECK, -1, -1, currentIteration, null);
                return;
            }

            currentIteration++;
            currentEdgeIndex = 0;
            changedInIteration = false;
            lastStep = StepType.START_ITERATION;
            meta.put("iteration", currentIteration);
            meta.put("totalIterations", n - 1);
            notifyListener(StepType.START_ITERATION, -1, -1, currentIteration, meta);
            return;
        }

        // ---- EARLY_TERMINATION → negative cycle check ----
        if (lastStep == StepType.EARLY_TERMINATION) {
            negCycleEdgeIndex = 0;
            lastStep = StepType.START_NEGATIVE_CYCLE_CHECK;
            notifyListener(StepType.START_NEGATIVE_CYCLE_CHECK, -1, -1, currentIteration, null);
            return;
        }

        // ---- RESET_EDGES_FOR_ITERATION (after START_ITERATION) ----
        if (lastStep == StepType.START_ITERATION) {
            lastStep = StepType.RESET_EDGES_FOR_ITERATION;
            meta.put("iteration", currentIteration);
            notifyListener(StepType.RESET_EDGES_FOR_ITERATION, -1, -1, currentIteration, meta);
            return;
        }

        // ---- SELECT_EDGE ----
        if (lastStep == StepType.RESET_EDGES_FOR_ITERATION ||
                lastStep == StepType.UPDATE_DISTANCE ||
                lastStep == StepType.REJECT_DISTANCE) {

            if (currentEdgeIndex >= edgeList.length) {
                // Done with this iteration
                lastStep = StepType.END_ITERATION;
                meta.put("iteration", currentIteration);
                meta.put("changed", changedInIteration);
                notifyListener(StepType.END_ITERATION, -1, -1, currentIteration, meta);
                return;
            }

            int[] edge = edgeList[currentEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];

            lastStep = StepType.SELECT_EDGE;
            meta.put("edgeIndex", currentEdgeIndex);
            meta.put("totalEdges", edgeList.length);
            meta.put("edgeWeight", w);
            meta.put("iteration", currentIteration);
            notifyListener(StepType.SELECT_EDGE, u, v, w, meta);
            return;
        }

        // ---- CALCULATE_DISTANCE ----
        if (lastStep == StepType.SELECT_EDGE) {
            int[] edge = edgeList[currentEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];

            if (dist[u] == Integer.MAX_VALUE) {
                // Source side unreachable, skip
                currentEdgeIndex++;
                lastStep = StepType.REJECT_DISTANCE;
                meta.put("reason", "source unreachable");
                notifyListener(StepType.REJECT_DISTANCE, u, v, 0, meta);
                return;
            }

            int tentative = dist[u] + w;
            lastStep = StepType.CALCULATE_DISTANCE;
            meta.put("currentDist", dist[u]);
            meta.put("edgeWeight", w);
            meta.put("tentativeDist", tentative);
            notifyListener(StepType.CALCULATE_DISTANCE, u, v, tentative, meta);
            return;
        }

        // ---- COMPARE_DISTANCES ----
        if (lastStep == StepType.CALCULATE_DISTANCE) {
            int[] edge = edgeList[currentEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];
            int tentative = dist[u] + w;

            lastStep = StepType.COMPARE_DISTANCES;
            meta.put("oldDist", dist[v]);
            meta.put("newDist", tentative);
            meta.put("improved", tentative < dist[v]);
            notifyListener(StepType.COMPARE_DISTANCES, u, v, tentative, meta);
            return;
        }

        // ---- UPDATE / REJECT ----
        if (lastStep == StepType.COMPARE_DISTANCES) {
            int[] edge = edgeList[currentEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];
            int tentative = dist[u] + w;

            if (tentative < dist[v]) {
                dist[v] = tentative;
                prev[v] = u;
                changedInIteration = true;

                lastStep = StepType.UPDATE_DISTANCE;
                meta.put("newDist", tentative);
                meta.put("prevVertex", u);
                notifyListener(StepType.UPDATE_DISTANCE, u, v, tentative, meta);
            } else {
                lastStep = StepType.REJECT_DISTANCE;
                notifyListener(StepType.REJECT_DISTANCE, u, v, dist[v], null);
            }

            currentEdgeIndex++;
            return;
        }

        // ---- NEGATIVE CYCLE CHECK: SELECT ----
        if (lastStep == StepType.START_NEGATIVE_CYCLE_CHECK ||
                lastStep == StepType.NEG_CHECK_SKIP) {

            if (negCycleEdgeIndex >= edgeList.length) {
                lastStep = StepType.NO_NEGATIVE_CYCLE;
                notifyListener(StepType.NO_NEGATIVE_CYCLE, -1, -1, 0, null);
                return;
            }

            int[] edge = edgeList[negCycleEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];

            lastStep = StepType.CHECK_NEGATIVE_CYCLE_SELECT;
            meta.put("edgeIndex", negCycleEdgeIndex);
            meta.put("totalEdges", edgeList.length);
            notifyListener(StepType.CHECK_NEGATIVE_CYCLE_SELECT, u, v, w, meta);
            return;
        }

        // ---- NEGATIVE CYCLE CHECK: CALC ----
        if (lastStep == StepType.CHECK_NEGATIVE_CYCLE_SELECT) {
            int[] edge = edgeList[negCycleEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];

            if (dist[u] == Integer.MAX_VALUE) {
                lastStep = StepType.NEG_CHECK_SKIP;
                meta.put("reason", "source unreachable");
                notifyListener(StepType.NEG_CHECK_SKIP, u, v, 0, meta);
                negCycleEdgeIndex++;
                return;
            }

            int tentative = dist[u] + w;
            lastStep = StepType.CHECK_NEGATIVE_CYCLE_CALC;
            meta.put("currentDist", dist[u]);
            meta.put("edgeWeight", w);
            meta.put("tentativeDist", tentative);
            notifyListener(StepType.CHECK_NEGATIVE_CYCLE_CALC, u, v, tentative, meta);
            return;
        }

        // ---- NEGATIVE CYCLE CHECK: COMPARE ----
        if (lastStep == StepType.CHECK_NEGATIVE_CYCLE_CALC) {
            int[] edge = edgeList[negCycleEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];
            int tentative = dist[u] + w;

            lastStep = StepType.CHECK_NEGATIVE_CYCLE_COMPARE;
            meta.put("oldDist", dist[v]);
            meta.put("newDist", tentative);
            meta.put("currentDist", dist[u]);
            meta.put("edgeWeight", w);
            meta.put("improved", tentative < dist[v]);
            notifyListener(StepType.CHECK_NEGATIVE_CYCLE_COMPARE, u, v, tentative, meta);
            return;
        }

        // ---- NEGATIVE CYCLE CHECK: RESULT ----
        if (lastStep == StepType.CHECK_NEGATIVE_CYCLE_COMPARE) {
            int[] edge = edgeList[negCycleEdgeIndex];
            int u = edge[0], v = edge[1], w = edge[2];
            int tentative = dist[u] + w;

            if (tentative < dist[v]) {
                hasNegativeCycle = true;
                lastStep = StepType.NEGATIVE_CYCLE_FOUND;
                meta.put("edgeU", u);
                meta.put("edgeV", v);
                meta.put("edgeWeight", w);
                notifyListener(StepType.NEGATIVE_CYCLE_FOUND, u, v, w, meta);
                return;
            } else {
                lastStep = StepType.NEG_CHECK_SKIP;
                notifyListener(StepType.NEG_CHECK_SKIP, u, v, dist[v], null);
                negCycleEdgeIndex++;
                return;
            }
        }

        // ---- TRACE NEGATIVE CYCLE ----
        if (lastStep == StepType.NEGATIVE_CYCLE_FOUND) {
            int[] edge = edgeList[negCycleEdgeIndex];
            int v = edge[1];

            // To find the actual cycle, go back N times from v
            int curr = v;
            for (int i = 0; i < n; i++) {
                if (prev[curr] != -1)
                    curr = prev[curr];
            }

            // Now curr is guaranteed to be in the cycle. Trace it.
            List<Integer> cycleNodes = new ArrayList<>();
            List<int[]> cycleEdges = new ArrayList<>();
            int start = curr;
            cycleNodes.add(start);
            int explorer = prev[start];
            while (explorer != start && explorer != -1 && cycleNodes.size() < n + 1) {
                cycleEdges.add(new int[] { explorer, cycleNodes.get(cycleNodes.size() - 1) });
                cycleNodes.add(explorer);
                explorer = prev[explorer];
            }
            if (explorer != -1) {
                cycleEdges.add(new int[] { explorer, cycleNodes.get(cycleNodes.size() - 1) });
            }

            Collections.reverse(cycleNodes);
            Collections.reverse(cycleEdges);

            lastStep = StepType.TRACE_NEGATIVE_CYCLE;
            meta.put("cycleNodes", cycleNodes);
            meta.put("cycleEdges", cycleEdges);
            notifyListener(StepType.TRACE_NEGATIVE_CYCLE, -1, -1, 0, meta);
            return;
        }

        // ---- COMPLETE AFTER TRACE ----
        if (lastStep == StepType.TRACE_NEGATIVE_CYCLE) {
            lastStep = StepType.COMPLETE;
            done = true;
            notifyListener(StepType.COMPLETE, -1, -1, 0, null);
            return;
        }

        // ---- After negative cycle check → start path animation or complete ----
        if (lastStep == StepType.NEGATIVE_CYCLE_FOUND) {
            // End immediately on negative cycle
            lastStep = StepType.COMPLETE;
            done = true;
            notifyListener(StepType.COMPLETE, -1, -1, 0, null);
            return;
        }

        if (lastStep == StepType.NO_NEGATIVE_CYCLE) {
            // Build all paths from source to each reachable vertex
            allPaths = new ArrayList<>();
            for (int t = 0; t < n; t++) {
                if (t != source && dist[t] != Integer.MAX_VALUE) {
                    allPaths.add(reconstructPath(t));
                }
            }

            if (!allPaths.isEmpty()) {
                currentAnimatingPath = 0;
                currentAnimatingStep = 0;
                lastStep = StepType.PATH_ANIMATION_START;
                int targetVertex = allPaths.get(0).get(allPaths.get(0).size() - 1);
                meta.put("targetVertex", targetVertex);
                meta.put("pathNumber", 1);
                meta.put("totalPaths", allPaths.size());
                notifyListener(StepType.PATH_ANIMATION_START, -1, -1, 0, meta);
                return;
            }

            // No reachable vertices
            lastStep = StepType.COMPLETE;
            done = true;
            notifyListener(StepType.COMPLETE, -1, -1, 0, null);
            return;
        }

        // ---- PATH ANIMATION (mirrors Dijkstra exactly) ----
        if (lastStep == StepType.PATH_ANIMATION_START) {
            List<Integer> path = allPaths.get(currentAnimatingPath);
            currentAnimatingStep = 0;
            int targetVertex = path.get(path.size() - 1);

            lastStep = StepType.PATH_ANIMATION_NODE;
            meta.put("nodeIndex", 0);
            meta.put("totalNodes", path.size());
            meta.put("targetVertex", targetVertex);
            meta.put("pathNumber", currentAnimatingPath + 1);
            meta.put("totalPaths", allPaths.size());
            notifyListener(StepType.PATH_ANIMATION_NODE, path.get(0), -1, dist[targetVertex], meta);
            return;
        }

        if (lastStep == StepType.PATH_ANIMATION_NODE) {
            List<Integer> path = allPaths.get(currentAnimatingPath);
            int targetVertex = path.get(path.size() - 1);

            if (currentAnimatingStep < path.size() - 1) {
                lastStep = StepType.PATH_ANIMATION_EDGE;
                int u = path.get(currentAnimatingStep);
                int v = path.get(currentAnimatingStep + 1);
                meta.put("edgeIndex", currentAnimatingStep);
                meta.put("totalEdges", path.size() - 1);
                meta.put("targetVertex", targetVertex);
                meta.put("pathNumber", currentAnimatingPath + 1);
                meta.put("totalPaths", allPaths.size());
                notifyListener(StepType.PATH_ANIMATION_EDGE, u, v, dist[targetVertex], meta);
                return;
            } else {
                lastStep = StepType.PATH_ANIMATION_END;
                meta.put("targetVertex", targetVertex);
                meta.put("pathNumber", currentAnimatingPath + 1);
                meta.put("totalPaths", allPaths.size());
                meta.put("distance", dist[targetVertex]);
                notifyListener(StepType.PATH_ANIMATION_END, -1, -1, dist[targetVertex], meta);
                return;
            }
        }

        if (lastStep == StepType.PATH_ANIMATION_EDGE) {
            List<Integer> path = allPaths.get(currentAnimatingPath);
            int targetVertex = path.get(path.size() - 1);

            currentAnimatingStep++;
            lastStep = StepType.PATH_ANIMATION_NODE;
            meta.put("nodeIndex", currentAnimatingStep);
            meta.put("totalNodes", path.size());
            meta.put("targetVertex", targetVertex);
            meta.put("pathNumber", currentAnimatingPath + 1);
            meta.put("totalPaths", allPaths.size());
            notifyListener(StepType.PATH_ANIMATION_NODE, path.get(currentAnimatingStep), -1, dist[targetVertex], meta);
            return;
        }

        if (lastStep == StepType.PATH_ANIMATION_END) {
            currentAnimatingPath++;

            if (currentAnimatingPath >= allPaths.size()) {
                lastStep = StepType.ALL_PATHS_COMPLETE;
                done = true;
                notifyListener(StepType.ALL_PATHS_COMPLETE, -1, -1, 0, null);
                return;
            }

            currentAnimatingStep = 0;
            lastStep = StepType.PATH_ANIMATION_START;
            int targetVertex = allPaths.get(currentAnimatingPath).get(allPaths.get(currentAnimatingPath).size() - 1);
            meta.put("targetVertex", targetVertex);
            meta.put("pathNumber", currentAnimatingPath + 1);
            meta.put("totalPaths", allPaths.size());
            notifyListener(StepType.PATH_ANIMATION_START, -1, -1, 0, meta);
            return;
        }
    }

    private List<Integer> reconstructPath(int target) {
        List<Integer> path = new ArrayList<>();
        int current = target;
        Set<Integer> visited = new HashSet<>();
        while (current != -1 && !visited.contains(current)) {
            visited.add(current);
            path.add(current);
            current = prev[current];
        }
        Collections.reverse(path);
        return path;
    }

    private void notifyListener(StepType type, int u, int v, int distance, Map<String, Object> meta) {
        if (listener != null) {
            listener.onStep(type, u, v, distance, meta != null ? meta : new HashMap<>());
        }
    }

    public State snapshot() {
        State s = new State();
        s.n = n;
        s.edgeList = edgeList; // Immutable after init
        s.adj = adj; // Immutable after init
        s.source = source;
        s.dist = dist.clone();
        s.prev = prev.clone();
        s.currentIteration = currentIteration;
        s.currentEdgeIndex = currentEdgeIndex;
        s.changedInIteration = changedInIteration;
        s.lastStep = lastStep;
        s.done = done;
        s.hasNegativeCycle = hasNegativeCycle;
        s.negCycleEdgeIndex = negCycleEdgeIndex;

        if (allPaths != null) {
            s.allPaths = new ArrayList<>();
            for (List<Integer> p : allPaths) {
                s.allPaths.add(new ArrayList<>(p));
            }
        }
        s.currentAnimatingPath = currentAnimatingPath;
        s.currentAnimatingStep = currentAnimatingStep;

        return s;
    }

    public void restore(State s) {
        this.n = s.n;
        this.edgeList = s.edgeList;
        this.adj = s.adj;
        this.source = s.source;
        this.dist = s.dist.clone();
        this.prev = s.prev.clone();
        this.currentIteration = s.currentIteration;
        this.currentEdgeIndex = s.currentEdgeIndex;
        this.changedInIteration = s.changedInIteration;
        this.lastStep = s.lastStep;
        this.done = s.done;
        this.hasNegativeCycle = s.hasNegativeCycle;
        this.negCycleEdgeIndex = s.negCycleEdgeIndex;

        if (s.allPaths != null) {
            this.allPaths = new ArrayList<>();
            for (List<Integer> p : s.allPaths) {
                this.allPaths.add(new ArrayList<>(p));
            }
        } else {
            this.allPaths = null;
        }
        this.currentAnimatingPath = s.currentAnimatingPath;
        this.currentAnimatingStep = s.currentAnimatingStep;
    }

    public boolean isDone() {
        return done;
    }

    public int[] getDistances() {
        return dist;
    }

    public int[] getPrevious() {
        return prev;
    }

    public int getSource() {
        return source;
    }

    public int getNodeCount() {
        return n;
    }

    public int[][] getEdgeList() {
        return edgeList;
    }

    public List<List<Integer>> getAllPaths() {
        return allPaths;
    }

    public boolean hasNegativeCycle() {
        return hasNegativeCycle;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }
}
