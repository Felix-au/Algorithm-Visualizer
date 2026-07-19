package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class PrimCode implements AlgorithmCode {
    
    private int nodeCount = 6;
    private int[][] edges = new int[0][];
    private int startVertex = 0;

    @Override
    public String getAlgorithmName() {
        return "MST Prim";
    }

    @Override
    public String getCCode() {
        return generateCCode();
    }
    
    private String generateCCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Prim's Algorithm for Minimum Spanning Tree\n");
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdbool.h>\n");
        sb.append("#include <limits.h>\n\n");
        
        sb.append("#define V ").append(nodeCount).append("\n");
        sb.append("#define INF INT_MAX\n\n");
        
        sb.append("// Find minimum key vertex not in MST\n");
        sb.append("int minKey(int key[], bool inMST[]) {\n");
        sb.append("    int min = INF, minIndex = -1;\n");
        sb.append("    for (int v = 0; v < V; v++) {\n");
        sb.append("        if (!inMST[v] && key[v] < min) {\n");
        sb.append("            min = key[v];\n");
        sb.append("            minIndex = v;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    return minIndex;\n");
        sb.append("}\n\n");
        
        sb.append("void primMST(int graph[V][V]) {\n");
        sb.append("    int parent[V];     // MST structure\n");
        sb.append("    int key[V];        // Minimum weight to reach vertex\n");
        sb.append("    bool inMST[V];     // Vertices included in MST\n");
        sb.append("    int totalWeight = 0;\n\n");
        
        sb.append("    // Initialize\n");
        sb.append("    for (int i = 0; i < V; i++) {\n");
        sb.append("        key[i] = INF;\n");
        sb.append("        inMST[i] = false;\n");
        sb.append("    }\n\n");
        
        sb.append("    // Start from vertex 0\n");
        sb.append("    key[0] = 0;\n");
        sb.append("    parent[0] = -1;\n\n");
        
        sb.append("    printf(\"=== Prim's MST Algorithm ===\\n\");\n");
        sb.append("    printf(\"Building MST:\\n\");\n\n");
        
        sb.append("    // Build MST with V-1 edges\n");
        sb.append("    for (int count = 0; count < V - 1; count++) {\n");
        sb.append("        // Pick minimum key vertex not in MST\n");
        sb.append("        int u = minKey(key, inMST);\n");
        sb.append("        inMST[u] = true;\n\n");
        
        sb.append("        // Update keys of adjacent vertices\n");
        sb.append("        for (int v = 0; v < V; v++) {\n");
        sb.append("            if (graph[u][v] && !inMST[v] && graph[u][v] < key[v]) {\n");
        sb.append("                parent[v] = u;\n");
        sb.append("                key[v] = graph[u][v];\n");
        sb.append("            }\n");
        sb.append("        }\n\n");
        
        sb.append("        if (count > 0) {\n");
        sb.append("            totalWeight += key[u];\n");
        sb.append("            printf(\"  Added: %d-%d (weight: %d) | Total: %d\\n\", parent[u], u, key[u], totalWeight);\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        sb.append("    printf(\"\\n=== MST Complete ===\\n\");\n");
        sb.append("    printf(\"Total weight: %d\\n\", totalWeight);\n");
        sb.append("    printf(\"\\nMST edges:\\n\");\n");
        sb.append("    for (int i = 1; i < V; i++) {\n");
        sb.append("        printf(\"  %d-%d (weight: %d)\\n\", parent[i], i, key[i]);\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("int main() {\n");
        sb.append("    // Adjacency matrix representation\n");
        sb.append("    int graph[V][V] = ").append(formatEdgesCMatrix()).append(";\n\n");
        
        sb.append("    primMST(graph);\n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    private String formatEdgesCMatrix() {
        // Build adjacency matrix
        int[][] matrix = new int[nodeCount][nodeCount];
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], w = edge[2];
            if (u < nodeCount && v < nodeCount) {
                matrix[u][v] = w;
                matrix[v][u] = w;
            }
        }
        
        StringBuilder sb = new StringBuilder("{\n");
        for (int i = 0; i < nodeCount; i++) {
            sb.append("        {");
            for (int j = 0; j < nodeCount; j++) {
                sb.append(matrix[i][j]);
                if (j < nodeCount - 1) sb.append(", ");
            }
            sb.append("}");
            if (i < nodeCount - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    }");
        return sb.toString();
    }

    public void updateParameters(int n, int[][] edges, int start) {
        this.nodeCount = n;
        this.edges = edges;
        this.startVertex = start;
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
        sb.append("# Prim's Algorithm for Minimum Spanning Tree\n");
        sb.append("# Uses priority queue for efficient edge selection\n\n");
        
        sb.append("import heapq\n");
        sb.append("from collections import defaultdict\n\n");
        
        sb.append("def prim_mst(n, edges, start=0):\n");
        sb.append("    print(\"=== Prim's MST Algorithm ===\")\n");
        sb.append("    print(f\"Graph: {n} vertices, {len(edges)} edges\")\n");
        sb.append("    print(f\"Starting from vertex {start}\")\n");
        sb.append("    print()\n\n");
        
        sb.append("    # Build adjacency list\n");
        sb.append("    graph = defaultdict(list)\n");
        sb.append("    for u, v, weight in edges:\n");
        sb.append("        graph[u].append((v, weight))\n");
        sb.append("        graph[v].append((u, weight))\n\n");
        
        sb.append("    in_mst = [False] * n\n");
        sb.append("    mst = []\n");
        sb.append("    total_weight = 0\n");
        sb.append("    frontier = []  # Min heap: (weight, u, v)\n\n");
        
        sb.append("    # Start from vertex 0\n");
        sb.append("    in_mst[start] = True\n");
        sb.append("    print(\"Building MST:\")\n");
        sb.append("    print(f\"  Added vertex {start} to MST\")\n");
        sb.append("    for neighbor, weight in graph[start]:\n");
        sb.append("        heapq.heappush(frontier, (weight, start, neighbor))\n\n");
        
        sb.append("    while frontier and len(mst) < n - 1:\n");
        sb.append("        weight, u, v = heapq.heappop(frontier)\n");
        sb.append("        if in_mst[v]:\n");
        sb.append("            continue\n\n");
        
        sb.append("        # Add edge to MST\n");
        sb.append("        in_mst[v] = True\n");
        sb.append("        mst.append((u, v, weight))\n");
        sb.append("        total_weight += weight\n");
        sb.append("        print(f\"  ✓ Added: {u}-{v} (weight: {weight}) | Total: {total_weight}\")\n\n");
        
        sb.append("        # Add new edges to frontier\n");
        sb.append("        for neighbor, w in graph[v]:\n");
        sb.append("            if not in_mst[neighbor]:\n");
        sb.append("                heapq.heappush(frontier, (w, v, neighbor))\n\n");
        
        sb.append("    print()\n");
        sb.append("    print(\"=== MST Complete ===\")\n");
        sb.append("    print(f\"Edges in MST: {len(mst)}\")\n");
        sb.append("    print(f\"Total weight: {total_weight}\")\n");
        sb.append("    print(\"\\nMST edges:\")\n");
        sb.append("    for u, v, w in mst:\n");
        sb.append("        print(f\"  {u}-{v} (weight: {w})\")\n");
        sb.append("    return mst, total_weight\n\n");
        
        sb.append("# Graph data\n");
        sb.append("n = ").append(nodeCount).append("\n");
        sb.append("edges = ").append(formatEdgesPython()).append("\n");
        sb.append("start = 0\n\n");
        
        sb.append("# Find MST\n");
        sb.append("mst, total_weight = prim_mst(n, edges, start)\n");
        
        return sb.toString();
    }

    private String generateJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Prim's Algorithm for Minimum Spanning Tree\n");
        sb.append("import java.util.*;\n\n");
        
        sb.append("class Edge implements Comparable<Edge> {\n");
        sb.append("    int u, v, weight;\n");
        sb.append("    Edge(int u, int v, int weight) {\n");
        sb.append("        this.u = u; this.v = v; this.weight = weight;\n");
        sb.append("    }\n");
        sb.append("    public int compareTo(Edge other) {\n");
        sb.append("        return Integer.compare(this.weight, other.weight);\n");
        sb.append("    }\n");
        sb.append("    public String toString() {\n");
        sb.append("        return u + \"-\" + v + \" (weight: \" + weight + \")\";\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("public class Main {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        int n = ").append(nodeCount).append(";\n");
        sb.append("        int[][] edgeArray = ").append(formatEdgesJavaArray()).append(";\n\n");
        
        sb.append("        System.out.println(\"=== Prim's MST Algorithm ===\");\n");
        sb.append("        System.out.println(\"Graph: \" + n + \" vertices, \" + edgeArray.length + \" edges\");\n");
        sb.append("        System.out.println(\"Starting from vertex 0\");\n");
        sb.append("        System.out.println();\n\n");
        
        sb.append("        // Build adjacency list\n");
        sb.append("        List<List<Edge>> graph = new ArrayList<>();\n");
        sb.append("        for (int i = 0; i < n; i++) graph.add(new ArrayList<>());\n");
        sb.append("        for (int[] e : edgeArray) {\n");
        sb.append("            graph.get(e[0]).add(new Edge(e[0], e[1], e[2]));\n");
        sb.append("            graph.get(e[1]).add(new Edge(e[1], e[0], e[2]));\n");
        sb.append("        }\n\n");
        
        sb.append("        boolean[] inMST = new boolean[n];\n");
        sb.append("        List<Edge> mst = new ArrayList<>();\n");
        sb.append("        int totalWeight = 0;\n");
        sb.append("        PriorityQueue<Edge> frontier = new PriorityQueue<>();\n\n");
        
        sb.append("        // Start from vertex 0\n");
        sb.append("        int start = 0;\n");
        sb.append("        inMST[start] = true;\n");
        sb.append("        System.out.println(\"Building MST:\");\n");
        sb.append("        System.out.println(\"  Added vertex 0 to MST\");\n");
        sb.append("        for (Edge e : graph.get(start)) {\n");
        sb.append("            frontier.offer(e);\n");
        sb.append("        }\n\n");
        
        sb.append("        while (!frontier.isEmpty() && mst.size() < n - 1) {\n");
        sb.append("            Edge e = frontier.poll();\n");
        sb.append("            if (inMST[e.v]) continue;\n\n");
        
        sb.append("            // Add edge to MST\n");
        sb.append("            inMST[e.v] = true;\n");
        sb.append("            mst.add(e);\n");
        sb.append("            totalWeight += e.weight;\n");
        sb.append("            System.out.println(\"  ✓ Added: \" + e + \" | Total: \" + totalWeight);\n\n");
        
        sb.append("            // Add new edges to frontier\n");
        sb.append("            for (Edge next : graph.get(e.v)) {\n");
        sb.append("                if (!inMST[next.v]) {\n");
        sb.append("                    frontier.offer(next);\n");
        sb.append("                }\n");
        sb.append("            }\n");
        sb.append("        }\n\n");
        
        sb.append("        System.out.println();\n");
        sb.append("        System.out.println(\"=== MST Complete ===\");\n");
        sb.append("        System.out.println(\"Edges in MST: \" + mst.size());\n");
        sb.append("        System.out.println(\"Total weight: \" + totalWeight);\n");
        sb.append("        System.out.println(\"\\nMST edges:\");\n");
        sb.append("        for (Edge e : mst) System.out.println(\"  \" + e);\n");
        sb.append("    }\n");
        sb.append("}\n");
        
        return sb.toString();
    }

    private String generateCppCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Prim's Algorithm for Minimum Spanning Tree\n");
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <queue>\n");
        sb.append("using namespace std;\n\n");
        
        sb.append("struct Edge {\n");
        sb.append("    int u, v, weight;\n");
        sb.append("    bool operator>(const Edge& other) const {\n");
        sb.append("        return weight > other.weight;\n");
        sb.append("    }\n");
        sb.append("};\n\n");
        
        sb.append("int main() {\n");
        sb.append("    int n = ").append(nodeCount).append(";\n");
        sb.append("    vector<vector<int>> edgeArray = ").append(formatEdgesCppArray()).append(";\n\n");
        
        sb.append("    cout << \"=== Prim's MST Algorithm ===\" << endl;\n");
        sb.append("    cout << \"Graph: \" << n << \" vertices, \" << edgeArray.size() << \" edges\" << endl;\n");
        sb.append("    cout << \"Starting from vertex 0\" << endl;\n");
        sb.append("    cout << endl;\n\n");
        
        sb.append("    // Build adjacency list\n");
        sb.append("    vector<vector<Edge>> graph(n);\n");
        sb.append("    for (const auto& e : edgeArray) {\n");
        sb.append("        graph[e[0]].push_back({e[0], e[1], e[2]});\n");
        sb.append("        graph[e[1]].push_back({e[1], e[0], e[2]});\n");
        sb.append("    }\n\n");
        
        sb.append("    vector<bool> inMST(n, false);\n");
        sb.append("    vector<Edge> mst;\n");
        sb.append("    int totalWeight = 0;\n");
        sb.append("    priority_queue<Edge, vector<Edge>, greater<Edge>> frontier;\n\n");
        
        sb.append("    // Start from vertex 0\n");
        sb.append("    int start = 0;\n");
        sb.append("    inMST[start] = true;\n");
        sb.append("    cout << \"Building MST:\" << endl;\n");
        sb.append("    cout << \"  Added vertex 0 to MST\" << endl;\n");
        sb.append("    for (const Edge& e : graph[start]) {\n");
        sb.append("        frontier.push(e);\n");
        sb.append("    }\n\n");
        
        sb.append("    while (!frontier.empty() && mst.size() < n - 1) {\n");
        sb.append("        Edge e = frontier.top();\n");
        sb.append("        frontier.pop();\n");
        sb.append("        if (inMST[e.v]) continue;\n\n");
        
        sb.append("        // Add edge to MST\n");
        sb.append("        inMST[e.v] = true;\n");
        sb.append("        mst.push_back(e);\n");
        sb.append("        totalWeight += e.weight;\n");
        sb.append("        cout << \"  ✓ Added: \" << e.u << \"-\" << e.v << \" (weight: \" << e.weight << \") | Total: \" << totalWeight << endl;\n\n");
        
        sb.append("        // Add new edges to frontier\n");
        sb.append("        for (const Edge& next : graph[e.v]) {\n");
        sb.append("            if (!inMST[next.v]) {\n");
        sb.append("                frontier.push(next);\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        sb.append("    cout << endl;\n");
        sb.append("    cout << \"=== MST Complete ===\" << endl;\n");
        sb.append("    cout << \"Edges in MST: \" << mst.size() << endl;\n");
        sb.append("    cout << \"Total weight: \" << totalWeight << endl;\n");
        sb.append("    cout << \"\\nMST edges:\" << endl;\n");
        sb.append("    for (const Edge& e : mst)\n");
        sb.append("        cout << \"  \" << e.u << \"-\" << e.v << \" (weight: \" << e.weight << \")\" << endl;\n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }

    private String formatEdgesPython() {
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

    private String formatEdgesJavaArray() {
        StringBuilder sb = new StringBuilder("{\n");
        for (int i = 0; i < edges.length; i++) {
            sb.append("            {")
              .append(edges[i][0]).append(", ")
              .append(edges[i][1]).append(", ")
              .append(edges[i][2]).append("}");
            if (i < edges.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("        }");
        return sb.toString();
    }

    private String formatEdgesCppArray() {
        StringBuilder sb = new StringBuilder("{\n");
        for (int i = 0; i < edges.length; i++) {
            sb.append("        {")
              .append(edges[i][0]).append(", ")
              .append(edges[i][1]).append(", ")
              .append(edges[i][2]).append("}");
            if (i < edges.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    }");
        return sb.toString();
    }
}
