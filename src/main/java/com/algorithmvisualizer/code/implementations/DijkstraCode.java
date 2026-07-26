package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class DijkstraCode implements AlgorithmCode {
    
    private int nodeCount = 6;
    private int[][] edges = new int[0][];
    private int source = 0;
    private Integer target = null;

    @Override
    public String getAlgorithmName() {
        return "Dijkstra's Shortest Path";
    }

    public void updateParameters(int nodeCount, int[][] edges, int source, Integer target) {
        this.nodeCount = nodeCount;
        this.edges = edges;
        this.source = source;
        this.target = target;
    }

    @Override
    public String getCCode() {
        return generateCCode();
    }

    @Override
    public String getPythonCode() {
        return generatePythonCode();
    }

    @Override
    public String getJavaCode() {
        return generateJavaCode();
    }

    @Override
    public String getCppCode() {
        return generateCppCode();
    }

    private String generatePythonCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Dijkstra's Shortest Path Algorithm\n");
        sb.append("import heapq\n\n");
        
        sb.append("def dijkstra(n, edges, source");
        if (target != null) {
            sb.append(", target");
        }
        sb.append("):\n");
        sb.append("    print(\"=== Dijkstra's Shortest Path ===\")\n");
        sb.append("    print(f\"Vertices: {n}, Edges: {len(edges)}\")\n");
        sb.append("    print(f\"Source: {source}");
        if (target != null) {
            sb.append(", Target: {target}");
        }
        sb.append("\")\n");
        sb.append("    print()\n\n");
        
        sb.append("    # Build adjacency list\n");
        sb.append("    adj = [[] for _ in range(n)]\n");
        sb.append("    for u, v, w in edges:\n");
        sb.append("        adj[u].append((v, w))\n");
        sb.append("        adj[v].append((u, w))  # Undirected\n\n");
        
        sb.append("    # Initialize distances\n");
        sb.append("    dist = [float('inf')] * n\n");
        sb.append("    prev = [-1] * n\n");
        sb.append("    dist[source] = 0\n\n");
        
        sb.append("    # Priority queue: (distance, vertex)\n");
        sb.append("    pq = [(0, source)]\n");
        sb.append("    visited = set()\n\n");
        
        sb.append("    print(\"Processing vertices:\")\n");
        sb.append("    while pq:\n");
        sb.append("        d, u = heapq.heappop(pq)\n");
        sb.append("        if u in visited:\n");
        sb.append("            continue\n");
        sb.append("        visited.add(u)\n");
        sb.append("        print(f\"  Vertex {u} (distance: {d})\")\n\n");
        
        sb.append("        for v, weight in adj[u]:\n");
        sb.append("            if v not in visited:\n");
        sb.append("                new_dist = dist[u] + weight\n");
        sb.append("                if new_dist < dist[v]:\n");
        sb.append("                    dist[v] = new_dist\n");
        sb.append("                    prev[v] = u\n");
        sb.append("                    heapq.heappush(pq, (new_dist, v))\n");
        sb.append("                    print(f\"    → Updated {v}: {new_dist}\")\n\n");
        
        if (target != null) {
            sb.append("    # Reconstruct path\n");
            sb.append("    if dist[target] != float('inf'):\n");
            sb.append("        path = []\n");
            sb.append("        curr = target\n");
            sb.append("        while curr != -1:\n");
            sb.append("            path.append(curr)\n");
            sb.append("            curr = prev[curr]\n");
            sb.append("        path.reverse()\n");
            sb.append("        print()\n");
            sb.append("        print(f\"Shortest path to {target}: {' → '.join(map(str, path))}\")\n");
            sb.append("        print(f\"Total distance: {dist[target]}\")\n");
        } else {
            sb.append("    print()\n");
            sb.append("    print(\"Shortest distances from source:\")\n");
            sb.append("    for i in range(n):\n");
            sb.append("        if dist[i] == float('inf'):\n");
            sb.append("            print(f\"  Vertex {i}: unreachable\")\n");
            sb.append("        else:\n");
            sb.append("            print(f\"  Vertex {i}: {dist[i]}\")\n");
        }
        
        sb.append("\n# Graph data\n");
        sb.append("n = ").append(nodeCount).append("\n");
        sb.append("edges = ").append(formatEdgesForPython()).append("\n");
        sb.append("source = ").append(source).append("\n");
        if (target != null) {
            sb.append("target = ").append(target).append("\n");
            sb.append("dijkstra(n, edges, source, target)\n");
        } else {
            sb.append("dijkstra(n, edges, source)\n");
        }
        
        return sb.toString();
    }

    private String generateJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\n\n");
        sb.append("public class Main {\n");
        sb.append("    static class Edge {\n");
        sb.append("        int to, weight;\n");
        sb.append("        Edge(int to, int weight) {\n");
        sb.append("            this.to = to;\n");
        sb.append("            this.weight = weight;\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        sb.append("    static class State implements Comparable<State> {\n");
        sb.append("        int vertex, distance;\n");
        sb.append("        State(int vertex, int distance) {\n");
        sb.append("            this.vertex = vertex;\n");
        sb.append("            this.distance = distance;\n");
        sb.append("        }\n");
        sb.append("        public int compareTo(State other) {\n");
        sb.append("            return Integer.compare(this.distance, other.distance);\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        int n = ").append(nodeCount).append(";\n");
        sb.append("        int[][] edgeData = ").append(formatEdgesForJava()).append(";\n");
        sb.append("        int source = ").append(source).append(";\n");
        if (target != null) {
            sb.append("        int target = ").append(target).append(";\n");
        }
        sb.append("\n");
        sb.append("        System.out.println(\"=== Dijkstra's Shortest Path ===\");\n");
        sb.append("        System.out.println(\"Vertices: \" + n + \", Edges: \" + edgeData.length);\n");
        sb.append("        System.out.println(\"Source: \" + source");
        if (target != null) {
            sb.append(" + \", Target: \" + target");
        }
        sb.append(");\n");
        sb.append("        System.out.println();\n\n");
        
        sb.append("        // Build adjacency list\n");
        sb.append("        List<List<Edge>> adj = new ArrayList<>();\n");
        sb.append("        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());\n");
        sb.append("        for (int[] e : edgeData) {\n");
        sb.append("            adj.get(e[0]).add(new Edge(e[1], e[2]));\n");
        sb.append("            adj.get(e[1]).add(new Edge(e[0], e[2])); // Undirected\n");
        sb.append("        }\n\n");
        
        sb.append("        // Dijkstra's algorithm\n");
        sb.append("        int[] dist = new int[n];\n");
        sb.append("        int[] prev = new int[n];\n");
        sb.append("        Arrays.fill(dist, Integer.MAX_VALUE);\n");
        sb.append("        Arrays.fill(prev, -1);\n");
        sb.append("        dist[source] = 0;\n\n");
        
        sb.append("        PriorityQueue<State> pq = new PriorityQueue<>();\n");
        sb.append("        pq.offer(new State(source, 0));\n");
        sb.append("        boolean[] visited = new boolean[n];\n\n");
        
        sb.append("        System.out.println(\"Processing vertices:\");\n");
        sb.append("        while (!pq.isEmpty()) {\n");
        sb.append("            State curr = pq.poll();\n");
        sb.append("            int u = curr.vertex;\n");
        sb.append("            if (visited[u]) continue;\n");
        sb.append("            visited[u] = true;\n");
        sb.append("            System.out.println(\"  Vertex \" + u + \" (distance: \" + dist[u] + \")\");\n\n");
        
        sb.append("            for (Edge edge : adj.get(u)) {\n");
        sb.append("                int v = edge.to;\n");
        sb.append("                if (!visited[v]) {\n");
        sb.append("                    int newDist = dist[u] + edge.weight;\n");
        sb.append("                    if (newDist < dist[v]) {\n");
        sb.append("                        dist[v] = newDist;\n");
        sb.append("                        prev[v] = u;\n");
        sb.append("                        pq.offer(new State(v, newDist));\n");
        sb.append("                        System.out.println(\"    → Updated \" + v + \": \" + newDist);\n");
        sb.append("                    }\n");
        sb.append("                }\n");
        sb.append("            }\n");
        sb.append("        }\n\n");
        
        if (target != null) {
            sb.append("        // Reconstruct path\n");
            sb.append("        if (dist[target] != Integer.MAX_VALUE) {\n");
            sb.append("            List<Integer> path = new ArrayList<>();\n");
            sb.append("            for (int curr = target; curr != -1; curr = prev[curr]) {\n");
            sb.append("                path.add(curr);\n");
            sb.append("            }\n");
            sb.append("            Collections.reverse(path);\n");
            sb.append("            System.out.println();\n");
            sb.append("            System.out.println(\"Shortest path to \" + target + \": \" + path);\n");
            sb.append("            System.out.println(\"Total distance: \" + dist[target]);\n");
            sb.append("        }\n");
        } else {
            sb.append("        System.out.println();\n");
            sb.append("        System.out.println(\"Shortest distances from source:\");\n");
            sb.append("        for (int i = 0; i < n; i++) {\n");
            sb.append("            if (dist[i] == Integer.MAX_VALUE) {\n");
            sb.append("                System.out.println(\"  Vertex \" + i + \": unreachable\");\n");
            sb.append("            } else {\n");
            sb.append("                System.out.println(\"  Vertex \" + i + \": \" + dist[i]);\n");
            sb.append("            }\n");
            sb.append("        }\n");
        }
        
        sb.append("    }\n");
        sb.append("}\n");
        
        return sb.toString();
    }

    private String generateCppCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <queue>\n");
        sb.append("#include <limits>\n");
        sb.append("using namespace std;\n\n");
        
        sb.append("struct Edge {\n");
        sb.append("    int to, weight;\n");
        sb.append("};\n\n");
        
        sb.append("struct State {\n");
        sb.append("    int vertex, distance;\n");
        sb.append("    bool operator>(const State& other) const {\n");
        sb.append("        return distance > other.distance;\n");
        sb.append("    }\n");
        sb.append("};\n\n");
        
        sb.append("int main() {\n");
        sb.append("    int n = ").append(nodeCount).append(";\n");
        sb.append("    vector<vector<int>> edgeData = ").append(formatEdgesForCpp()).append(";\n");
        sb.append("    int source = ").append(source).append(";\n");
        if (target != null) {
            sb.append("    int target = ").append(target).append(";\n");
        }
        sb.append("\n");
        sb.append("    cout << \"=== Dijkstra's Shortest Path ===\" << endl;\n");
        sb.append("    cout << \"Vertices: \" << n << \", Edges: \" << edgeData.size() << endl;\n");
        sb.append("    cout << \"Source: \" << source");
        if (target != null) {
            sb.append(" << \", Target: \" << target");
        }
        sb.append(" << endl << endl;\n\n");
        
        sb.append("    // Build adjacency list\n");
        sb.append("    vector<vector<Edge>> adj(n);\n");
        sb.append("    for (auto& e : edgeData) {\n");
        sb.append("        adj[e[0]].push_back({e[1], e[2]});\n");
        sb.append("        adj[e[1]].push_back({e[0], e[2]}); // Undirected\n");
        sb.append("    }\n\n");
        
        sb.append("    // Dijkstra's algorithm\n");
        sb.append("    const int INF = numeric_limits<int>::max();\n");
        sb.append("    vector<int> dist(n, INF);\n");
        sb.append("    vector<int> prev(n, -1);\n");
        sb.append("    dist[source] = 0;\n\n");
        
        sb.append("    priority_queue<State, vector<State>, greater<State>> pq;\n");
        sb.append("    pq.push({source, 0});\n");
        sb.append("    vector<bool> visited(n, false);\n\n");
        
        sb.append("    cout << \"Processing vertices:\" << endl;\n");
        sb.append("    while (!pq.empty()) {\n");
        sb.append("        State curr = pq.top();\n");
        sb.append("        pq.pop();\n");
        sb.append("        int u = curr.vertex;\n");
        sb.append("        if (visited[u]) continue;\n");
        sb.append("        visited[u] = true;\n");
        sb.append("        cout << \"  Vertex \" << u << \" (distance: \" << dist[u] << \")\" << endl;\n\n");
        
        sb.append("        for (Edge& edge : adj[u]) {\n");
        sb.append("            int v = edge.to;\n");
        sb.append("            if (!visited[v]) {\n");
        sb.append("                int newDist = dist[u] + edge.weight;\n");
        sb.append("                if (newDist < dist[v]) {\n");
        sb.append("                    dist[v] = newDist;\n");
        sb.append("                    prev[v] = u;\n");
        sb.append("                    pq.push({v, newDist});\n");
        sb.append("                    cout << \"    → Updated \" << v << \": \" << newDist << endl;\n");
        sb.append("                }\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        if (target != null) {
            sb.append("    // Reconstruct path\n");
            sb.append("    if (dist[target] != INF) {\n");
            sb.append("        vector<int> path;\n");
            sb.append("        for (int curr = target; curr != -1; curr = prev[curr]) {\n");
            sb.append("            path.push_back(curr);\n");
            sb.append("        }\n");
            sb.append("        reverse(path.begin(), path.end());\n");
            sb.append("        cout << endl << \"Shortest path to \" << target << \": \";\n");
            sb.append("        for (int i = 0; i < path.size(); i++) {\n");
            sb.append("            if (i > 0) cout << \" → \";\n");
            sb.append("            cout << path[i];\n");
            sb.append("        }\n");
            sb.append("        cout << endl << \"Total distance: \" << dist[target] << endl;\n");
            sb.append("    }\n");
        } else {
            sb.append("    cout << endl << \"Shortest distances from source:\" << endl;\n");
            sb.append("    for (int i = 0; i < n; i++) {\n");
            sb.append("        if (dist[i] == INF) {\n");
            sb.append("            cout << \"  Vertex \" << i << \": unreachable\" << endl;\n");
            sb.append("        } else {\n");
            sb.append("            cout << \"  Vertex \" << i << \": \" << dist[i] << endl;\n");
            sb.append("        }\n");
            sb.append("    }\n");
        }
        
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }

    private String formatEdgesForPython() {
        if (edges.length == 0) {
            return "[(0, 1, 4), (0, 2, 2), (1, 2, 1), (1, 3, 5), (2, 3, 8), (2, 4, 10), (3, 4, 2), (3, 5, 6), (4, 5, 3)]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < edges.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("(").append(edges[i][0]).append(", ")
              .append(edges[i][1]).append(", ")
              .append(edges[i][2]).append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatEdgesForJava() {
        if (edges.length == 0) {
            return "{{0,1,4}, {0,2,2}, {1,2,1}, {1,3,5}, {2,3,8}, {2,4,10}, {3,4,2}, {3,5,6}, {4,5,3}}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < edges.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("{").append(edges[i][0]).append(",")
              .append(edges[i][1]).append(",")
              .append(edges[i][2]).append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private String formatEdgesForCpp() {
        if (edges.length == 0) {
            return "{{0,1,4}, {0,2,2}, {1,2,1}, {1,3,5}, {2,3,8}, {2,4,10}, {3,4,2}, {3,5,6}, {4,5,3}}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < edges.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("{").append(edges[i][0]).append(",")
              .append(edges[i][1]).append(",")
              .append(edges[i][2]).append("}");
        }
        sb.append("}");
        return sb.toString();
    }
    
    private String generateCCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Dijkstra's Shortest Path Algorithm in C\n");
        sb.append("#include <stdio.h>\n");
        sb.append("#include <limits.h>\n");
        sb.append("#include <stdbool.h>\n\n");
        
        sb.append("#define V ").append(nodeCount).append("\n");
        sb.append("#define INF INT_MAX\n\n");
        
        sb.append("// Find vertex with minimum distance\n");
        sb.append("int minDistance(int dist[], bool visited[]) {\n");
        sb.append("    int min = INF, min_index = -1;\n");
        sb.append("    for (int v = 0; v < V; v++) {\n");
        sb.append("        if (!visited[v] && dist[v] < min) {\n");
        sb.append("            min = dist[v];\n");
        sb.append("            min_index = v;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    return min_index;\n");
        sb.append("}\n\n");
        
        sb.append("// Print shortest path from source to target\n");
        sb.append("void printPath(int parent[], int j) {\n");
        sb.append("    if (parent[j] == -1) {\n");
        sb.append("        printf(\"%d\", j);\n");
        sb.append("        return;\n");
        sb.append("    }\n");
        sb.append("    printPath(parent, parent[j]);\n");
        sb.append("    printf(\" -> %d\", j);\n");
        sb.append("}\n\n");
        
        sb.append("// Dijkstra's algorithm\n");
        sb.append("void dijkstra(int graph[V][V], int src) {\n");
        sb.append("    int dist[V];\n");
        sb.append("    bool visited[V];\n");
        sb.append("    int parent[V];\n\n");
        
        sb.append("    // Initialize\n");
        sb.append("    for (int i = 0; i < V; i++) {\n");
        sb.append("        dist[i] = INF;\n");
        sb.append("        visited[i] = false;\n");
        sb.append("        parent[i] = -1;\n");
        sb.append("    }\n");
        sb.append("    dist[src] = 0;\n\n");
        
        sb.append("    printf(\"=== Dijkstra's Algorithm ===\\n\");\n");
        sb.append("    printf(\"Source: %d\\n\\n\", src);\n\n");
        
        sb.append("    // Find shortest path for all vertices\n");
        sb.append("    for (int count = 0; count < V - 1; count++) {\n");
        sb.append("        int u = minDistance(dist, visited);\n");
        sb.append("        if (u == -1) break;\n");
        sb.append("        visited[u] = true;\n\n");
        
        sb.append("        printf(\"Processing vertex %d (distance: %d)\\n\", u, dist[u]);\n\n");
        
        sb.append("        // Update distances of adjacent vertices\n");
        sb.append("        for (int v = 0; v < V; v++) {\n");
        sb.append("            if (!visited[v] && graph[u][v] != 0 && \n");
        sb.append("                dist[u] != INF && dist[u] + graph[u][v] < dist[v]) {\n");
        sb.append("                dist[v] = dist[u] + graph[u][v];\n");
        sb.append("                parent[v] = u;\n");
        sb.append("                printf(\"  Updated distance to %d: %d\\n\", v, dist[v]);\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        printf(\"\\n\");\n");
        sb.append("    }\n\n");
        
        sb.append("    // Print results\n");
        sb.append("    printf(\"=== Shortest Paths ===\\n\");\n");
        sb.append("    for (int i = 0; i < V; i++) {\n");
        sb.append("        if (i != src) {\n");
        sb.append("            printf(\"To %d: \", i);\n");
        sb.append("            if (dist[i] == INF) {\n");
        sb.append("                printf(\"No path\\n\");\n");
        sb.append("            } else {\n");
        sb.append("                printf(\"Distance = %d, Path: \", dist[i]);\n");
        sb.append("                printPath(parent, i);\n");
        sb.append("                printf(\"\\n\");\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("int main() {\n");
        sb.append("    // Initialize adjacency matrix\n");
        sb.append("    int graph[V][V] = {0};\n\n");
        
        sb.append("    // Add edges\n");
        if (edges.length > 0) {
            for (int[] edge : edges) {
                sb.append("    graph[").append(edge[0]).append("][").append(edge[1])
                  .append("] = ").append(edge[2]).append(";\n");
                sb.append("    graph[").append(edge[1]).append("][").append(edge[0])
                  .append("] = ").append(edge[2]).append(";  // Undirected\n");
            }
        } else {
            sb.append("    graph[0][1] = graph[1][0] = 4;\n");
            sb.append("    graph[0][2] = graph[2][0] = 2;\n");
            sb.append("    graph[1][2] = graph[2][1] = 1;\n");
            sb.append("    graph[1][3] = graph[3][1] = 5;\n");
            sb.append("    graph[2][3] = graph[3][2] = 8;\n");
            sb.append("    graph[2][4] = graph[4][2] = 10;\n");
            sb.append("    graph[3][4] = graph[4][3] = 2;\n");
            sb.append("    graph[3][5] = graph[5][3] = 6;\n");
            sb.append("    graph[4][5] = graph[5][4] = 3;\n");
        }
        
        sb.append("\n    dijkstra(graph, ").append(source).append(");\n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }
}
