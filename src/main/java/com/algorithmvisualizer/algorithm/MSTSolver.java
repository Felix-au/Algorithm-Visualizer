package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Interface for Minimum Spanning Tree solvers.
 */
public interface MSTSolver {
    enum StepType {
        INIT,
        SORT_EDGES,           // Kruskal: sorting edges
        SELECT_EDGE,          // Kruskal: considering an edge
        CHECK_CYCLE,          // Kruskal: checking if edge creates cycle
        FIND_SET_U,           // Kruskal: finding set of u
        FIND_SET_V,           // Kruskal: finding set of v
        COMPARE_ROOTS,        // Kruskal: comparing root vertices
        UNION_SETS,           // Kruskal: union operation
        MST_EDGE_ADDED,       // Edge added to MST
        MST_EDGE_REJECTED,    // Edge rejected (creates cycle)
        HIGHLIGHT_CURRENT_VERTEX,  // Prim: highlight current vertex
        EXPLORE_EDGE_START,   // Prim: start exploring an edge
        COMPARE_EDGE_WEIGHT,  // Prim: compare edge weight
        EXPLORE_EDGE_END,     // Prim: end exploring an edge
        ADD_VERTEX_TO_MST,    // Prim: adding vertex to MST
        EXPLORE_EDGES,        // Prim: exploring edges from current vertex
        ADD_TO_FRONTIER,      // Prim: adding edge to frontier
        UPDATE_PRIORITY,      // Prim: updating edge priority
        SCAN_FRONTIER_START,  // Prim: start scanning frontier
        SCAN_FRONTIER_COMPARE,// Prim: comparing frontier edges
        SCAN_FRONTIER_FOUND,  // Prim: found minimum edge
        SELECT_MIN_EDGE,      // Prim: selecting minimum edge from frontier
        COMPLETE,
        DONE
    }

    interface StepListener {
        void onStep(StepType type, int u, int v, int weight, Map<String, Object> meta);
    }

    void setGraph(int n, int[][] edges); // edges: [u, v, weight]
    void setStartVertex(int start); // For Prim only
    void setStepListener(StepListener l);

    void reset();
    void step();
    boolean isDone();

    Set<String> getMSTEdges(); // Returns edges as "u-v" strings
    int getTotalWeight();
    int[] getParent(); // Union-Find parent array (Kruskal)
    Map<Integer, Integer> getMinWeight(); // Min weight to each vertex (Prim)
    boolean[] getInMST(); // Vertices in MST

    State snapshot();
    void restore(State s);

    class State {
        public int n;
        public int[][] edges;
        public Set<String> mstEdges;
        public boolean[] inMST;
        public int currentEdgeIndex;
        public int currentVertex;
        public int[] parent;
        public int[] rank;
        public Map<Integer, Integer> minWeight;
        public int totalWeight;
        public boolean done;
        public PriorityQueue<Edge> frontier; // For Prim
    }

    class Edge implements Comparable<Edge> {
        public int u, v, weight;
        
        public Edge(int u, int v, int weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }

        public String key() {
            return Math.min(u, v) + "-" + Math.max(u, v);
        }
    }
}
