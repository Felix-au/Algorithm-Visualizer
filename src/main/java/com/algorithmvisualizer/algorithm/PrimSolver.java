package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Prim's algorithm for finding Minimum Spanning Tree.
 * Uses a priority queue to select minimum weight edges.
 */
public class PrimSolver implements MSTSolver {
    
    private int n;
    private List<List<Edge>> adj; // Adjacency list with weights
    private Set<String> mstEdges;
    private boolean[] inMST;
    private Map<Integer, Integer> minWeight; // Min weight edge to each vertex
    private Map<Integer, Integer> parentVertex; // Parent in MST
    private PriorityQueue<Edge> frontier;
    private int startVertex;
    private int currentVertex;
    private int totalWeight;
    private boolean done;
    private StepListener listener;
    private StepType lastStep;
    private List<Integer> edgesToExplore;
    private int exploreIndex;
    private Edge currentExploringEdge;
    private List<Edge> frontierList;

    public PrimSolver() {
        this.mstEdges = new LinkedHashSet<>();
        this.minWeight = new HashMap<>();
        this.parentVertex = new HashMap<>();
        this.done = false;
        this.startVertex = 0;
    }

    @Override
    public void setGraph(int n, int[][] edgeArray) {
        this.n = n;
        this.adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        
        for (int[] e : edgeArray) {
            int u = e[0], v = e[1], w = e[2];
            adj.get(u).add(new Edge(u, v, w));
            adj.get(v).add(new Edge(v, u, w));
        }
        reset();
    }

    @Override
    public void setStartVertex(int start) {
        this.startVertex = Math.max(0, Math.min(start, n - 1));
    }

    @Override
    public void setStepListener(StepListener l) {
        this.listener = l;
    }

    @Override
    public void reset() {
        mstEdges.clear();
        inMST = new boolean[n];
        minWeight.clear();
        parentVertex.clear();
        frontier = new PriorityQueue<>();
        currentVertex = -1;
        totalWeight = 0;
        done = false;
        lastStep = null;
        edgesToExplore = new ArrayList<>();
        exploreIndex = 0;
        currentExploringEdge = null;
        frontierList = new ArrayList<>();
        
        // Initialize min weights to infinity
        for (int i = 0; i < n; i++) {
            minWeight.put(i, Integer.MAX_VALUE);
        }
    }

    @Override
    public void step() {
        if (done) return;

        if (lastStep == null) {
            // INIT: Start with the start vertex
            currentVertex = startVertex;
            inMST[currentVertex] = true;
            minWeight.put(currentVertex, 0);
            lastStep = StepType.INIT;
            notifyListener(StepType.INIT, currentVertex, -1, 0, null);
            return;
        }

        if (lastStep == StepType.INIT || lastStep == StepType.MST_EDGE_ADDED) {
            // HIGHLIGHT_CURRENT_VERTEX
            lastStep = StepType.HIGHLIGHT_CURRENT_VERTEX;
            notifyListener(StepType.HIGHLIGHT_CURRENT_VERTEX, currentVertex, -1, 0, null);
            return;
        }

        if (lastStep == StepType.HIGHLIGHT_CURRENT_VERTEX) {
            // ADD_VERTEX_TO_MST
            lastStep = StepType.ADD_VERTEX_TO_MST;
            Map<String, Object> meta = new HashMap<>();
            meta.put("mstSize", countMSTVertices());
            notifyListener(StepType.ADD_VERTEX_TO_MST, currentVertex, -1, 0, meta);
            return;
        }

        if (lastStep == StepType.ADD_VERTEX_TO_MST) {
            // Prepare to explore edges
            edgesToExplore.clear();
            for (Edge e : adj.get(currentVertex)) {
                if (!inMST[e.v]) {
                    edgesToExplore.add(e.v);
                }
            }
            exploreIndex = 0;
            
            lastStep = StepType.EXPLORE_EDGES;
            notifyListener(StepType.EXPLORE_EDGES, currentVertex, -1, 0, null);
            return;
        }

        if (lastStep == StepType.EXPLORE_EDGES) {
            if (exploreIndex < edgesToExplore.size()) {
                // Start exploring next edge
                int neighbor = edgesToExplore.get(exploreIndex);
                currentExploringEdge = findEdge(currentVertex, neighbor);
                
                lastStep = StepType.EXPLORE_EDGE_START;
                notifyListener(StepType.EXPLORE_EDGE_START, currentVertex, neighbor, 
                    currentExploringEdge != null ? currentExploringEdge.weight : 0, null);
                return;
            } else {
                // Done exploring, scan frontier
                lastStep = StepType.SCAN_FRONTIER_START;
                return;
            }
        }

        if (lastStep == StepType.EXPLORE_EDGE_START) {
            // Compare edge weight
            if (currentExploringEdge != null && !inMST[currentExploringEdge.v]) {
                int oldWeight = minWeight.getOrDefault(currentExploringEdge.v, Integer.MAX_VALUE);
                
                Map<String, Object> meta = new HashMap<>();
                meta.put("oldWeight", oldWeight == Integer.MAX_VALUE ? -1 : oldWeight);
                meta.put("isBetter", currentExploringEdge.weight < oldWeight);
                
                lastStep = StepType.COMPARE_EDGE_WEIGHT;
                notifyListener(StepType.COMPARE_EDGE_WEIGHT, currentExploringEdge.u, 
                    currentExploringEdge.v, currentExploringEdge.weight, meta);
                return;
            } else {
                exploreIndex++;
                lastStep = StepType.EXPLORE_EDGES;
                return;
            }
        }

        if (lastStep == StepType.COMPARE_EDGE_WEIGHT) {
            Edge edge = currentExploringEdge;
            if (edge != null && !inMST[edge.v]) {
                int oldWeight = minWeight.getOrDefault(edge.v, Integer.MAX_VALUE);
                
                if (edge.weight < oldWeight) {
                    // Update priority
                    minWeight.put(edge.v, edge.weight);
                    parentVertex.put(edge.v, edge.u);
                    frontier.offer(edge);
                    
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("oldWeight", oldWeight == Integer.MAX_VALUE ? -1 : oldWeight);
                    meta.put("isUpdate", oldWeight != Integer.MAX_VALUE);
                    
                    if (oldWeight == Integer.MAX_VALUE) {
                        lastStep = StepType.ADD_TO_FRONTIER;
                        notifyListener(StepType.ADD_TO_FRONTIER, edge.u, edge.v, edge.weight, meta);
                    } else {
                        lastStep = StepType.UPDATE_PRIORITY;
                        notifyListener(StepType.UPDATE_PRIORITY, edge.u, edge.v, edge.weight, meta);
                    }
                    return;
                }
            }
            
            // Move to next edge
            exploreIndex++;
            lastStep = StepType.EXPLORE_EDGE_END;
            notifyListener(StepType.EXPLORE_EDGE_END, edge.u, edge.v, edge.weight, null);
            return;
        }

        if (lastStep == StepType.ADD_TO_FRONTIER || lastStep == StepType.UPDATE_PRIORITY || lastStep == StepType.EXPLORE_EDGE_END) {
            // Continue exploring edges
            exploreIndex++;
            lastStep = StepType.EXPLORE_EDGES;
            return;
        }

        if (lastStep == StepType.SCAN_FRONTIER_START) {
            // Build frontier list for scanning
            frontierList.clear();
            PriorityQueue<Edge> tempQueue = new PriorityQueue<>(frontier);
            while (!tempQueue.isEmpty()) {
                Edge e = tempQueue.poll();
                if (!inMST[e.v] && minWeight.get(e.v) == e.weight) {
                    frontierList.add(e);
                }
            }
            
            if (frontierList.isEmpty()) {
                // MST complete
                done = true;
                lastStep = StepType.COMPLETE;
                notifyListener(StepType.COMPLETE, -1, -1, totalWeight, null);
                return;
            }
            
            lastStep = StepType.SCAN_FRONTIER_COMPARE;
            notifyListener(StepType.SCAN_FRONTIER_START, -1, -1, 0, null);
            return;
        }

        if (lastStep == StepType.SCAN_FRONTIER_COMPARE) {
            // Show comparison of frontier edges
            if (!frontierList.isEmpty()) {
                Edge minEdge = frontierList.get(0);
                Map<String, Object> meta = new HashMap<>();
                meta.put("frontierSize", frontierList.size());
                
                lastStep = StepType.SCAN_FRONTIER_FOUND;
                notifyListener(StepType.SCAN_FRONTIER_COMPARE, minEdge.u, minEdge.v, minEdge.weight, meta);
                return;
            }
        }

        if (lastStep == StepType.SCAN_FRONTIER_FOUND) {
            // Highlight the minimum edge found
            Edge minEdge = frontierList.get(0);
            lastStep = StepType.SELECT_MIN_EDGE;
            notifyListener(StepType.SCAN_FRONTIER_FOUND, minEdge.u, minEdge.v, minEdge.weight, null);
            return;
        }

        if (lastStep == StepType.SELECT_MIN_EDGE) {
            // Find minimum edge from frontier
            Edge minEdge = null;
            while (!frontier.isEmpty()) {
                Edge e = frontier.poll();
                if (!inMST[e.v] && minWeight.get(e.v) == e.weight) {
                    minEdge = e;
                    break;
                }
            }
            
            if (minEdge == null) {
                // MST complete
                done = true;
                lastStep = StepType.COMPLETE;
                notifyListener(StepType.COMPLETE, -1, -1, totalWeight, null);
                return;
            }
            
            currentVertex = minEdge.v;
            mstEdges.add(minEdge.key());
            inMST[currentVertex] = true;
            totalWeight += minEdge.weight;
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("totalWeight", totalWeight);
            meta.put("mstSize", mstEdges.size());
            
            lastStep = StepType.MST_EDGE_ADDED;
            notifyListener(StepType.MST_EDGE_ADDED, minEdge.u, minEdge.v, minEdge.weight, meta);
            return;
        }
    }

    private Edge findEdge(int u, int v) {
        for (Edge e : adj.get(u)) {
            if (e.v == v) return e;
        }
        return null;
    }

    private int countMSTVertices() {
        int count = 0;
        for (boolean b : inMST) if (b) count++;
        return count;
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
        return new int[0]; // Not used in Prim
    }

    @Override
    public Map<Integer, Integer> getMinWeight() {
        return new HashMap<>(minWeight);
    }

    @Override
    public boolean[] getInMST() {
        return inMST != null ? inMST.clone() : new boolean[0];
    }

    @Override
    public State snapshot() {
        State s = new State();
        s.n = n;
        s.mstEdges = new LinkedHashSet<>(mstEdges);
        s.inMST = inMST.clone();
        s.currentVertex = currentVertex;
        s.minWeight = new HashMap<>(minWeight);
        s.totalWeight = totalWeight;
        s.done = done;
        s.frontier = new PriorityQueue<>(frontier);
        return s;
    }

    @Override
    public void restore(State s) {
        this.n = s.n;
        this.mstEdges = new LinkedHashSet<>(s.mstEdges);
        this.inMST = s.inMST.clone();
        this.currentVertex = s.currentVertex;
        this.minWeight = new HashMap<>(s.minWeight);
        this.totalWeight = s.totalWeight;
        this.done = s.done;
        this.frontier = new PriorityQueue<>(s.frontier);
    }

    private void notifyListener(StepType type, int u, int v, int weight, Map<String, Object> meta) {
        if (listener != null) {
            listener.onStep(type, u, v, weight, meta != null ? meta : new HashMap<>());
        }
    }
}
