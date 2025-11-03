package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Breadth-First Search solver that emits fine-grained steps to drive visualization.
 * Iterative BFS using a queue for step-wise execution.
 */
public class BFSSolver {

    public enum StepType { INIT, DISCOVER, EXPLORE_EDGE, ENQUEUE, DEQUEUE, DONE }

    public interface StepListener {
        void onStep(StepType type, int u, int v);
    }

    public static class State {
        public int n;
        public List<List<Integer>> adj;
        public boolean[] visited;
        public Queue<Integer> queue;
        public List<Integer> traversal;
        public int start;
        public boolean done;
        public int currentNode;
        public int currentNeighborIndex;
    }

    private int n = 0;
    private List<List<Integer>> adj = new ArrayList<>();
    private boolean[] visited = new boolean[0];
    private Queue<Integer> queue = new LinkedList<>();
    private List<Integer> traversal = new ArrayList<>();
    private int start = 0;
    private boolean done = false;
    private int currentNode = -1;
    private int currentNeighborIndex = 0;

    private StepListener listener;

    public BFSSolver(int n, List<List<Integer>> adj, int start) {
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
        queue.clear();
        traversal.clear();
        currentNode = -1;
        currentNeighborIndex = 0;
        done = (n == 0);
        if (done) {
            emit(StepType.DONE, -1, -1);
            return;
        }
        queue.offer(start);
        if (emitInit) emit(StepType.INIT, start, -1);
        else emit(StepType.INIT, start, -1);
    }

    public boolean isDone() { return done; }
    public int getN() { return n; }
    public List<List<Integer>> getAdj() { return copyAdj(adj); }
    public boolean[] getVisited() { return Arrays.copyOf(visited, visited.length); }
    public List<Integer> getTraversal() { return new ArrayList<>(traversal); }
    public Queue<Integer> getQueue() { return new LinkedList<>(queue); }
    public int getStart() { return start; }

    public void step() {
        if (done) return;
        
        // If we're not currently processing a node, dequeue the next one
        if (currentNode == -1) {
            if (queue.isEmpty()) {
                // Try to find next unvisited component
                int next = -1;
                for (int i = 0; i < n; i++) {
                    if (!visited[i]) {
                        next = i;
                        break;
                    }
                }
                if (next == -1) {
                    done = true;
                    emit(StepType.DONE, -1, -1);
                    return;
                }
                queue.offer(next);
                emit(StepType.INIT, next, -1);
                return;
            }
            
            currentNode = queue.poll();
            currentNeighborIndex = 0;
            emit(StepType.DEQUEUE, currentNode, -1);
            return;
        }

        // Mark as visited if not already
        if (!visited[currentNode]) {
            visited[currentNode] = true;
            traversal.add(currentNode);
            emit(StepType.DISCOVER, currentNode, -1);
            return;
        }

        // Explore neighbors
        if (currentNeighborIndex < adj.get(currentNode).size()) {
            int neighbor = adj.get(currentNode).get(currentNeighborIndex);
            currentNeighborIndex++;
            
            emit(StepType.EXPLORE_EDGE, currentNode, neighbor);
            
            if (!visited[neighbor] && !queue.contains(neighbor)) {
                queue.offer(neighbor);
                emit(StepType.ENQUEUE, neighbor, -1);
            }
            return;
        }

        // Done with this node, move to next
        currentNode = -1;
        currentNeighborIndex = 0;
    }

    public State snapshot() {
        State s = new State();
        s.n = n;
        s.adj = copyAdj(adj);
        s.visited = Arrays.copyOf(visited, visited.length);
        s.queue = new LinkedList<>(queue);
        s.traversal = new ArrayList<>(traversal);
        s.start = start;
        s.done = done;
        s.currentNode = currentNode;
        s.currentNeighborIndex = currentNeighborIndex;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        n = s.n;
        adj = copyAdj(s.adj);
        visited = Arrays.copyOf(s.visited, s.visited.length);
        queue = new LinkedList<>(s.queue);
        traversal = new ArrayList<>(s.traversal);
        start = s.start;
        done = s.done;
        currentNode = s.currentNode;
        currentNeighborIndex = s.currentNeighborIndex;
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
        if (done) return "BFS complete. Traversal size=" + traversal.size();
        if (currentNode == -1) return "Ready to dequeue next node";
        return "Processing node " + currentNode + ", neighbor index=" + currentNeighborIndex + ", queue size=" + queue.size();
    }
}
