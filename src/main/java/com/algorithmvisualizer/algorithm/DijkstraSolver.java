package com.algorithmvisualizer.algorithm;

import java.util.*;

public class DijkstraSolver {
    
    public enum StepType {
        INIT,
        HIGHLIGHT_SOURCE,
        INITIALIZE_DISTANCES,
        SELECT_CURRENT,
        MARK_VISITED,
        EXPLORE_NEIGHBOR_START,
        CALCULATE_DISTANCE,
        COMPARE_DISTANCES,
        UPDATE_DISTANCE,
        REJECT_DISTANCE,
        EXPLORE_NEIGHBOR_END,
        SCAN_UNVISITED,
        PATH_RECONSTRUCTION,
        PATH_HIGHLIGHT_STEP,  // For granular path highlighting
        PATH_ANIMATION_START,  // Start animating a specific path
        PATH_ANIMATION_NODE,   // Highlight a node in path
        PATH_ANIMATION_EDGE,   // Highlight an edge in path
        PATH_ANIMATION_END,    // End of one path animation
        ALL_PATHS_COMPLETE,    // All paths animated
        COMPLETE
    }
    
    public interface StepListener {
        void onStep(StepType type, int u, int v, int distance, Map<String, Object> metadata);
    }
    
    // Graph structure
    private int n;
    private List<List<Edge>> adj;
    private int source;
    private Integer target;
    
    // Algorithm state
    private int[] dist;
    private int[] prev;
    private boolean[] visited;
    private PriorityQueue<Node> pq;
    private int currentVertex;
    private int currentNeighbor;
    private int currentNeighborIndex;
    private List<Integer> currentNeighbors;
    private int tentativeDistance;
    
    // Step tracking
    private StepType lastStep;
    private boolean done;
    private StepListener listener;
    
    // Path reconstruction
    private List<Integer> shortestPath;
    private int pathIndex;
    private boolean pathReconstructionStarted;
    
    // Multi-path animation (for target=all)
    private List<List<Integer>> allPaths;
    private int currentAnimatingPath;  // Which path (target) we're animating
    private int currentAnimatingStep;  // Which step in that path
    
    public static class Edge {
        public int to;
        public int weight;
        
        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }
    
    private static class Node implements Comparable<Node> {
        int vertex;
        int distance;
        
        Node(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
    
    public static class State {
        public int n;
        public List<List<Edge>> adj;
        public int source;
        public Integer target;
        public int[] dist;
        public int[] prev;
        public boolean[] visited;
        public PriorityQueue<Node> pq;
        public int currentVertex;
        public int currentNeighbor;
        public int currentNeighborIndex;
        public List<Integer> currentNeighbors;
        public int tentativeDistance;
        public StepType lastStep;
        public boolean done;
        public List<Integer> shortestPath;
        public int pathIndex;
        public boolean pathReconstructionStarted;
        public List<List<Integer>> allPaths;
        public int currentAnimatingPath;
        public int currentAnimatingStep;
    }
    
    public void setGraph(int n, int[][] edges, int source, Integer target) {
        this.n = n;
        this.source = source;
        this.target = target;
        
        // Build adjacency list
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int weight = edge[2];
            adj.get(u).add(new Edge(v, weight));
            // For undirected graph
            adj.get(v).add(new Edge(u, weight));
        }
        
        reset();
    }
    
    public void reset() {
        dist = new int[n];
        prev = new int[n];
        visited = new boolean[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        
        pq = new PriorityQueue<>();
        currentVertex = -1;
        currentNeighbor = -1;
        currentNeighborIndex = -1;
        currentNeighbors = null;
        tentativeDistance = 0;
        
        lastStep = null;
        done = false;
        shortestPath = null;
        pathIndex = 0;
        pathReconstructionStarted = false;
        allPaths = null;
        currentAnimatingPath = 0;
        currentAnimatingStep = 0;
    }
    
    public void setStepListener(StepListener listener) {
        this.listener = listener;
    }
    
    public void step() {
        if (done) return;
        
        Map<String, Object> meta = new HashMap<>();
        
        if (lastStep == null) {
            // INIT
            lastStep = StepType.INIT;
            notifyListener(StepType.INIT, source, -1, 0, null);
            return;
        }
        
        if (lastStep == StepType.INIT) {
            // HIGHLIGHT_SOURCE
            lastStep = StepType.HIGHLIGHT_SOURCE;
            notifyListener(StepType.HIGHLIGHT_SOURCE, source, -1, 0, null);
            return;
        }
        
        if (lastStep == StepType.HIGHLIGHT_SOURCE) {
            // INITIALIZE_DISTANCES
            dist[source] = 0;
            pq.add(new Node(source, 0));
            lastStep = StepType.INITIALIZE_DISTANCES;
            notifyListener(StepType.INITIALIZE_DISTANCES, source, -1, 0, null);
            return;
        }
        
        if (lastStep == StepType.INITIALIZE_DISTANCES || lastStep == StepType.SCAN_UNVISITED) {
            // SELECT_CURRENT - get vertex with minimum distance
            if (pq.isEmpty()) {
                // No more vertices to process - start multi-path animation
                if (target == null) {
                    // Build all paths from source to all reachable vertices
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
                        meta.put("targetVertex", allPaths.get(0).get(allPaths.get(0).size() - 1));
                        meta.put("pathNumber", 1);
                        meta.put("totalPaths", allPaths.size());
                        notifyListener(StepType.PATH_ANIMATION_START, -1, -1, 0, meta);
                        return;
                    }
                } else {
                    // Single target - start path reconstruction
                    if (dist[target] != Integer.MAX_VALUE) {
                        shortestPath = reconstructPath(target);
                        pathIndex = 0;
                        pathReconstructionStarted = true;
                        lastStep = StepType.PATH_RECONSTRUCTION;
                        meta.put("pathLength", shortestPath.size());
                        meta.put("totalDistance", dist[target]);
                        notifyListener(StepType.PATH_RECONSTRUCTION, -1, -1, dist[target], meta);
                        return;
                    }
                }
                // Complete
                lastStep = StepType.COMPLETE;
                done = true;
                notifyListener(StepType.COMPLETE, -1, -1, 0, null);
                return;
            }
            
            Node current = pq.poll();
            currentVertex = current.vertex;
            
            // Skip if already visited
            if (visited[currentVertex]) {
                lastStep = StepType.SCAN_UNVISITED;
                notifyListener(StepType.SCAN_UNVISITED, currentVertex, -1, dist[currentVertex], null);
                return;
            }
            
            lastStep = StepType.SELECT_CURRENT;
            meta.put("distance", dist[currentVertex]);
            notifyListener(StepType.SELECT_CURRENT, currentVertex, -1, dist[currentVertex], meta);
            return;
        }
        
        if (lastStep == StepType.SELECT_CURRENT) {
            // MARK_VISITED
            visited[currentVertex] = true;
            lastStep = StepType.MARK_VISITED;
            notifyListener(StepType.MARK_VISITED, currentVertex, -1, dist[currentVertex], null);
            return;
        }
        
        if (lastStep == StepType.MARK_VISITED || lastStep == StepType.EXPLORE_NEIGHBOR_END) {
            // Start exploring neighbors
            if (currentNeighbors == null) {
                currentNeighbors = new ArrayList<>();
                for (Edge e : adj.get(currentVertex)) {
                    currentNeighbors.add(e.to);
                }
                currentNeighborIndex = 0;
            }
            
            if (currentNeighborIndex >= currentNeighbors.size()) {
                // Done with all neighbors, scan for next vertex
                currentNeighbors = null;
                currentNeighborIndex = -1;
                lastStep = StepType.SCAN_UNVISITED;
                notifyListener(StepType.SCAN_UNVISITED, currentVertex, -1, dist[currentVertex], null);
                return;
            }
            
            // EXPLORE_NEIGHBOR_START
            currentNeighbor = currentNeighbors.get(currentNeighborIndex);
            
            // Find edge weight
            int edgeWeight = 0;
            for (Edge e : adj.get(currentVertex)) {
                if (e.to == currentNeighbor) {
                    edgeWeight = e.weight;
                    break;
                }
            }
            
            lastStep = StepType.EXPLORE_NEIGHBOR_START;
            meta.put("edgeWeight", edgeWeight);
            meta.put("visited", visited[currentNeighbor]);
            notifyListener(StepType.EXPLORE_NEIGHBOR_START, currentVertex, currentNeighbor, edgeWeight, meta);
            return;
        }
        
        if (lastStep == StepType.EXPLORE_NEIGHBOR_START) {
            // Skip if neighbor already visited
            if (visited[currentNeighbor]) {
                currentNeighborIndex++;
                lastStep = StepType.EXPLORE_NEIGHBOR_END;
                notifyListener(StepType.EXPLORE_NEIGHBOR_END, currentVertex, currentNeighbor, 0, null);
                return;
            }
            
            // CALCULATE_DISTANCE
            int edgeWeight = 0;
            for (Edge e : adj.get(currentVertex)) {
                if (e.to == currentNeighbor) {
                    edgeWeight = e.weight;
                    break;
                }
            }
            
            tentativeDistance = dist[currentVertex] + edgeWeight;
            lastStep = StepType.CALCULATE_DISTANCE;
            meta.put("currentDist", dist[currentVertex]);
            meta.put("edgeWeight", edgeWeight);
            meta.put("tentativeDist", tentativeDistance);
            notifyListener(StepType.CALCULATE_DISTANCE, currentVertex, currentNeighbor, tentativeDistance, meta);
            return;
        }
        
        if (lastStep == StepType.CALCULATE_DISTANCE) {
            // COMPARE_DISTANCES
            lastStep = StepType.COMPARE_DISTANCES;
            meta.put("oldDist", dist[currentNeighbor]);
            meta.put("newDist", tentativeDistance);
            meta.put("improved", tentativeDistance < dist[currentNeighbor]);
            notifyListener(StepType.COMPARE_DISTANCES, currentVertex, currentNeighbor, tentativeDistance, meta);
            return;
        }
        
        if (lastStep == StepType.COMPARE_DISTANCES) {
            if (tentativeDistance < dist[currentNeighbor]) {
                // UPDATE_DISTANCE
                dist[currentNeighbor] = tentativeDistance;
                prev[currentNeighbor] = currentVertex;
                pq.add(new Node(currentNeighbor, tentativeDistance));
                
                lastStep = StepType.UPDATE_DISTANCE;
                meta.put("newDist", tentativeDistance);
                meta.put("prevVertex", currentVertex);
                notifyListener(StepType.UPDATE_DISTANCE, currentVertex, currentNeighbor, tentativeDistance, meta);
            } else {
                // REJECT_DISTANCE
                lastStep = StepType.REJECT_DISTANCE;
                notifyListener(StepType.REJECT_DISTANCE, currentVertex, currentNeighbor, dist[currentNeighbor], null);
            }
            return;
        }
        
        if (lastStep == StepType.UPDATE_DISTANCE || lastStep == StepType.REJECT_DISTANCE) {
            // EXPLORE_NEIGHBOR_END
            currentNeighborIndex++;
            lastStep = StepType.EXPLORE_NEIGHBOR_END;
            notifyListener(StepType.EXPLORE_NEIGHBOR_END, currentVertex, currentNeighbor, 0, null);
            return;
        }
        
        // Multi-path animation steps
        if (lastStep == StepType.PATH_ANIMATION_START) {
            // Start animating current path
            List<Integer> path = allPaths.get(currentAnimatingPath);
            currentAnimatingStep = 0;
            
            // Highlight first node (source)
            lastStep = StepType.PATH_ANIMATION_NODE;
            int targetVertex = path.get(path.size() - 1);
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
            
            // Check if we need to highlight an edge next
            if (currentAnimatingStep < path.size() - 1) {
                // Highlight edge to next node
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
                // Path complete - move to next path or finish
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
            
            // Move to next node
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
            // Move to next path
            currentAnimatingPath++;
            
            if (currentAnimatingPath >= allPaths.size()) {
                // All paths animated
                lastStep = StepType.ALL_PATHS_COMPLETE;
                done = true;
                notifyListener(StepType.ALL_PATHS_COMPLETE, -1, -1, 0, null);
                return;
            }
            
            // Start next path
            currentAnimatingStep = 0;
            lastStep = StepType.PATH_ANIMATION_START;
            int targetVertex = allPaths.get(currentAnimatingPath).get(allPaths.get(currentAnimatingPath).size() - 1);
            meta.put("targetVertex", targetVertex);
            meta.put("pathNumber", currentAnimatingPath + 1);
            meta.put("totalPaths", allPaths.size());
            notifyListener(StepType.PATH_ANIMATION_START, -1, -1, 0, meta);
            return;
        }
        
        // Single-target path reconstruction (old logic)
        if (lastStep == StepType.PATH_RECONSTRUCTION) {
            // Start highlighting path step by step
            if (pathIndex < shortestPath.size()) {
                int u = shortestPath.get(pathIndex);
                int v = pathIndex + 1 < shortestPath.size() ? shortestPath.get(pathIndex + 1) : -1;
                
                lastStep = StepType.PATH_HIGHLIGHT_STEP;
                meta.put("pathIndex", pathIndex);
                meta.put("pathLength", shortestPath.size());
                meta.put("totalDistance", dist[target]);
                notifyListener(StepType.PATH_HIGHLIGHT_STEP, u, v, dist[target], meta);
                return;
            }
        }
        
        if (lastStep == StepType.PATH_HIGHLIGHT_STEP) {
            pathIndex++;
            
            if (pathIndex >= shortestPath.size()) {
                // Path highlighting complete
                lastStep = StepType.COMPLETE;
                done = true;
                meta.put("totalDistance", dist[target]);
                notifyListener(StepType.COMPLETE, -1, -1, dist[target], meta);
                return;
            }
            
            // Continue highlighting next step
            int u = shortestPath.get(pathIndex);
            int v = pathIndex + 1 < shortestPath.size() ? shortestPath.get(pathIndex + 1) : -1;
            
            meta.put("pathIndex", pathIndex);
            meta.put("pathLength", shortestPath.size());
            meta.put("totalDistance", dist[target]);
            notifyListener(StepType.PATH_HIGHLIGHT_STEP, u, v, dist[target], meta);
            return;
        }
    }
    
    private List<Integer> reconstructPath(int target) {
        List<Integer> path = new ArrayList<>();
        int current = target;
        while (current != -1) {
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
        s.adj = adj; // Immutable after initialization
        s.source = source;
        s.target = target;
        s.dist = dist.clone();
        s.prev = prev.clone();
        s.visited = visited.clone();
        
        // Deep copy priority queue
        s.pq = new PriorityQueue<>();
        for (Node node : pq) {
            s.pq.add(new Node(node.vertex, node.distance));
        }
        
        s.currentVertex = currentVertex;
        s.currentNeighbor = currentNeighbor;
        s.currentNeighborIndex = currentNeighborIndex;
        s.currentNeighbors = currentNeighbors != null ? new ArrayList<>(currentNeighbors) : null;
        s.tentativeDistance = tentativeDistance;
        s.lastStep = lastStep;
        s.done = done;
        s.shortestPath = shortestPath != null ? new ArrayList<>(shortestPath) : null;
        s.pathIndex = pathIndex;
        s.pathReconstructionStarted = pathReconstructionStarted;
        s.allPaths = allPaths != null ? new ArrayList<>(allPaths) : null;
        s.currentAnimatingPath = currentAnimatingPath;
        s.currentAnimatingStep = currentAnimatingStep;
        
        return s;
    }
    
    public void restore(State s) {
        this.n = s.n;
        this.adj = s.adj;
        this.source = s.source;
        this.target = s.target;
        this.dist = s.dist.clone();
        this.prev = s.prev.clone();
        this.visited = s.visited.clone();
        
        this.pq = new PriorityQueue<>();
        for (Node node : s.pq) {
            this.pq.add(new Node(node.vertex, node.distance));
        }
        
        this.currentVertex = s.currentVertex;
        this.currentNeighbor = s.currentNeighbor;
        this.currentNeighborIndex = s.currentNeighborIndex;
        this.currentNeighbors = s.currentNeighbors != null ? new ArrayList<>(s.currentNeighbors) : null;
        this.tentativeDistance = s.tentativeDistance;
        this.lastStep = s.lastStep;
        this.done = s.done;
        this.shortestPath = s.shortestPath != null ? new ArrayList<>(s.shortestPath) : null;
        this.pathIndex = s.pathIndex;
        this.pathReconstructionStarted = s.pathReconstructionStarted;
        this.allPaths = s.allPaths != null ? new ArrayList<>(s.allPaths) : null;
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
    
    public boolean[] getVisited() {
        return visited;
    }
    
    public List<Integer> getShortestPath() {
        return shortestPath;
    }
    
    public List<List<Integer>> getAllPaths() {
        return allPaths;
    }
}
