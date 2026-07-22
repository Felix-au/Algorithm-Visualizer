package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Kruskal's algorithm for finding Minimum Spanning Tree.
 * Uses Union-Find data structure to detect cycles.
 */
public class KruskalSolver implements MSTSolver {
    
    private int n;
    private Edge[] edges;
    private Set<String> mstEdges;
    private boolean[] inMST;
    private int[] parent;
    private int[] rank;
    private int currentEdgeIndex;
    private int totalWeight;
    private boolean done;
    private StepListener listener;
    private StepType lastStep;
    private int rootU, rootV;

    public KruskalSolver() {
        this.mstEdges = new LinkedHashSet<>();
        this.done = false;
    }

    @Override
    public void setGraph(int n, int[][] edgeArray) {
        this.n = n;
        this.edges = new Edge[edgeArray.length];
        for (int i = 0; i < edgeArray.length; i++) {
            edges[i] = new Edge(edgeArray[i][0], edgeArray[i][1], edgeArray[i][2]);
        }
        reset();
    }

    @Override
    public void setStartVertex(int start) {
        // Not used in Kruskal
    }

    @Override
    public void setStepListener(StepListener l) {
        this.listener = l;
    }

    @Override
    public void reset() {
        mstEdges.clear();
        inMST = new boolean[n];
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
        currentEdgeIndex = -1;
        totalWeight = 0;
        done = false;
        lastStep = null;
    }

    @Override
    public void step() {
        if (done) return;

        if (lastStep == null) {
            // INIT step
            lastStep = StepType.INIT;
            notifyListener(StepType.INIT, -1, -1, 0, null);
            return;
        }

        if (lastStep == StepType.INIT) {
            // Sort edges
            Arrays.sort(edges);
            lastStep = StepType.SORT_EDGES;
            notifyListener(StepType.SORT_EDGES, -1, -1, 0, null);
            return;
        }

        // Check if MST is complete
        if (mstEdges.size() >= n - 1) {
            done = true;
            lastStep = StepType.COMPLETE;
            notifyListener(StepType.COMPLETE, -1, -1, totalWeight, null);
            return;
        }

        // Process next edge
        if (lastStep == StepType.SORT_EDGES || lastStep == StepType.MST_EDGE_ADDED || lastStep == StepType.MST_EDGE_REJECTED) {
            currentEdgeIndex++;
            if (currentEdgeIndex >= edges.length) {
                done = true;
                lastStep = StepType.COMPLETE;
                notifyListener(StepType.COMPLETE, -1, -1, totalWeight, null);
                return;
            }
            Edge e = edges[currentEdgeIndex];
            lastStep = StepType.SELECT_EDGE;
            notifyListener(StepType.SELECT_EDGE, e.u, e.v, e.weight, null);
            return;
        }

        if (lastStep == StepType.SELECT_EDGE) {
            Edge e = edges[currentEdgeIndex];
            rootU = find(e.u);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("rootU", rootU);
            
            lastStep = StepType.FIND_SET_U;
            notifyListener(StepType.FIND_SET_U, e.u, e.v, e.weight, meta);
            return;
        }

        if (lastStep == StepType.FIND_SET_U) {
            Edge e = edges[currentEdgeIndex];
            rootV = find(e.v);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("rootV", rootV);
            
            lastStep = StepType.FIND_SET_V;
            notifyListener(StepType.FIND_SET_V, e.u, e.v, e.weight, meta);
            return;
        }

        if (lastStep == StepType.FIND_SET_V) {
            Edge e = edges[currentEdgeIndex];
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("rootU", rootU);
            meta.put("rootV", rootV);
            meta.put("sameset", rootU == rootV);
            
            lastStep = StepType.COMPARE_ROOTS;
            notifyListener(StepType.COMPARE_ROOTS, e.u, e.v, e.weight, meta);
            return;
        }

        if (lastStep == StepType.COMPARE_ROOTS) {
            Edge e = edges[currentEdgeIndex];
            
            if (rootU == rootV) {
                // Reject edge - creates cycle
                lastStep = StepType.MST_EDGE_REJECTED;
                notifyListener(StepType.MST_EDGE_REJECTED, e.u, e.v, e.weight, null);
            } else {
                // Union the sets
                lastStep = StepType.UNION_SETS;
                Map<String, Object> meta = new HashMap<>();
                meta.put("rootU", rootU);
                meta.put("rootV", rootV);
                notifyListener(StepType.UNION_SETS, e.u, e.v, e.weight, meta);
            }
            return;
        }

        if (lastStep == StepType.UNION_SETS) {
            Edge e = edges[currentEdgeIndex];
            union(e.u, e.v);
            mstEdges.add(e.key());
            inMST[e.u] = true;
            inMST[e.v] = true;
            totalWeight += e.weight;
            
            lastStep = StepType.MST_EDGE_ADDED;
            Map<String, Object> meta = new HashMap<>();
            meta.put("mstSize", mstEdges.size());
            meta.put("totalWeight", totalWeight);
            notifyListener(StepType.MST_EDGE_ADDED, e.u, e.v, e.weight, meta);
            return;
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public Set<String> getMSTEdges() {
        return new LinkedHashSet<>(mstEdges);
    }

    @Override
    public int getTotalWeight() {
        return totalWeight;
    }

    @Override
    public int[] getParent() {
        return parent != null ? parent.clone() : new int[0];
    }

    @Override
    public Map<Integer, Integer> getMinWeight() {
        return new HashMap<>(); // Not used in Kruskal
    }

    @Override
    public boolean[] getInMST() {
        return inMST != null ? inMST.clone() : new boolean[0];
    }

    // Union-Find operations
    private int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Path compression
        }
        return parent[x];
    }

    private void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        
        if (rootX == rootY) return;
        
        // Union by rank
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
    }

    @Override
    public State snapshot() {
        State s = new State();
        s.n = n;
        s.edges = new int[edges.length][3];
        for (int i = 0; i < edges.length; i++) {
            s.edges[i][0] = edges[i].u;
            s.edges[i][1] = edges[i].v;
            s.edges[i][2] = edges[i].weight;
        }
        s.mstEdges = new LinkedHashSet<>(mstEdges);
        s.inMST = inMST.clone();
        s.currentEdgeIndex = currentEdgeIndex;
        s.parent = parent.clone();
        s.rank = rank.clone();
        s.totalWeight = totalWeight;
        s.done = done;
        return s;
    }

    @Override
    public void restore(State s) {
        this.n = s.n;
        this.edges = new Edge[s.edges.length];
        for (int i = 0; i < s.edges.length; i++) {
            edges[i] = new Edge(s.edges[i][0], s.edges[i][1], s.edges[i][2]);
        }
        this.mstEdges = new LinkedHashSet<>(s.mstEdges);
        this.inMST = s.inMST.clone();
        this.currentEdgeIndex = s.currentEdgeIndex;
        this.parent = s.parent.clone();
        this.rank = s.rank.clone();
        this.totalWeight = s.totalWeight;
        this.done = s.done;
    }

    private void notifyListener(StepType type, int u, int v, int weight, Map<String, Object> meta) {
        if (listener != null) {
            listener.onStep(type, u, v, weight, meta != null ? meta : new HashMap<>());
        }
    }
}
