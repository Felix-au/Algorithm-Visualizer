package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class BellmanFordCode implements AlgorithmCode {

    private int nodeCount = 6;
    private int[][] edges = new int[0][];
    private int source = 0;

    @Override
    public String getAlgorithmName() {
        return "Bellman-Ford Shortest Path";
    }

    public void updateParameters(int nodeCount, int[][] edges, int source) {
        this.nodeCount = nodeCount;
        this.edges = edges;
        this.source = source;
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

    // ──────────────────── Python ────────────────────
    private String generatePythonCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Bellman-Ford Shortest Path Algorithm\n\n");

        sb.append("def bellman_ford(n, edges, source):\n");
        sb.append("    print(\"=== Bellman-Ford Shortest Path ===\")\n");
        sb.append("    print(f\"Vertices: {n}, Edges: {len(edges)}, Source: {source}\")\n");
        sb.append("    print()\n\n");

        sb.append("    # Initialize distances\n");
        sb.append("    dist = [float('inf')] * n\n");
        sb.append("    prev = [-1] * n\n");
        sb.append("    dist[source] = 0\n\n");

        sb.append("    # Relax all edges V-1 times\n");
        sb.append("    for i in range(1, n):\n");
        sb.append("        print(f\"--- Iteration {i}/{n-1} ---\")\n");
        sb.append("        changed = False\n");
        sb.append("        for u, v, w in edges:\n");
        sb.append("            if dist[u] != float('inf') and dist[u] + w < dist[v]:\n");
        sb.append("                dist[v] = dist[u] + w\n");
        sb.append("                prev[v] = u\n");
        sb.append("                changed = True\n");
        sb.append("                print(f\"  Edge {u} -> {v} (w={w}): Updated dist[{v}] = {dist[v]}\")\n");
        sb.append("        if not changed:\n");
        sb.append("            print(\"  No changes — early termination\")\n");
        sb.append("            break\n");
        sb.append("        print()\n\n");

        sb.append("    # Check for negative cycles\n");
        sb.append("    print(\"Checking for negative cycles...\")\n");
        sb.append("    for u, v, w in edges:\n");
        sb.append("        if dist[u] != float('inf') and dist[u] + w < dist[v]:\n");
        sb.append("            print(f\"  [!] Negative cycle detected at edge {u} -> {v}!\")\n");
        sb.append("            return dist, prev, True\n");
        sb.append("    print(\"  [OK] No negative cycle.\")\n");
        sb.append("    print()\n\n");

        sb.append("    # Print results\n");
        sb.append("    print(\"Shortest distances from source:\")\n");
        sb.append("    for i in range(n):\n");
        sb.append("        if dist[i] == float('inf'):\n");
        sb.append("            print(f\"  Vertex {i}: unreachable\")\n");
        sb.append("        else:\n");
        sb.append("            # Reconstruct path\n");
        sb.append("            path = []\n");
        sb.append("            curr = i\n");
        sb.append("            while curr != -1:\n");
        sb.append("                path.append(curr)\n");
        sb.append("                curr = prev[curr]\n");
        sb.append("            path.reverse()\n");
        sb.append("            print(f\"  Vertex {i}: dist={dist[i]}, path={' -> '.join(map(str, path))}\")\n\n");

        sb.append("    return dist, prev, False\n\n");

        sb.append("# Graph data\n");
        sb.append("n = ").append(nodeCount).append("\n");
        sb.append("edges = ").append(formatEdgesForPython()).append("\n");
        sb.append("source = ").append(source).append("\n");
        sb.append("bellman_ford(n, edges, source)\n");

        return sb.toString();
    }

    // ──────────────────── Java ────────────────────
    private String generateJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\n\n");
        sb.append("public class Main {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        int n = ").append(nodeCount).append(";\n");
        sb.append("        int[][] edges = ").append(formatEdgesForJava()).append(";\n");
        sb.append("        int source = ").append(source).append(";\n\n");

        sb.append("        System.out.println(\"=== Bellman-Ford Shortest Path ===\");\n");
        sb.append(
                "        System.out.println(\"Vertices: \" + n + \", Edges: \" + edges.length + \", Source: \" + source);\n");
        sb.append("        System.out.println();\n\n");

        sb.append("        // Initialize distances\n");
        sb.append("        int[] dist = new int[n];\n");
        sb.append("        int[] prev = new int[n];\n");
        sb.append("        Arrays.fill(dist, Integer.MAX_VALUE);\n");
        sb.append("        Arrays.fill(prev, -1);\n");
        sb.append("        dist[source] = 0;\n\n");

        sb.append("        // Relax all edges V-1 times\n");
        sb.append("        for (int i = 1; i < n; i++) {\n");
        sb.append("            System.out.println(\"--- Iteration \" + i + \"/\" + (n-1) + \" ---\");\n");
        sb.append("            boolean changed = false;\n");
        sb.append("            for (int[] e : edges) {\n");
        sb.append("                int u = e[0], v = e[1], w = e[2];\n");
        sb.append("                if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {\n");
        sb.append("                    dist[v] = dist[u] + w;\n");
        sb.append("                    prev[v] = u;\n");
        sb.append("                    changed = true;\n");
        sb.append(
                "                    System.out.println(\"  Edge \" + u + \" -> \" + v + \" (w=\" + w + \"): Updated dist[\" + v + \"] = \" + dist[v]);\n");
        sb.append("                }\n");
        sb.append("            }\n");
        sb.append("            if (!changed) {\n");
        sb.append("                System.out.println(\"  No changes - early termination\");\n");
        sb.append("                break;\n");
        sb.append("            }\n");
        sb.append("            System.out.println();\n");
        sb.append("        }\n\n");

        sb.append("        // Check for negative cycles\n");
        sb.append("        System.out.println(\"Checking for negative cycles...\");\n");
        sb.append("        boolean negativeCycle = false;\n");
        sb.append("        for (int[] e : edges) {\n");
        sb.append("            int u = e[0], v = e[1], w = e[2];\n");
        sb.append("            if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {\n");
        sb.append(
                "                System.out.println(\"  [!] Negative cycle detected at edge \" + u + \" -> \" + v + \"!\");\n");
        sb.append("                negativeCycle = true;\n");
        sb.append("                break;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        if (!negativeCycle) {\n");
        sb.append("            System.out.println(\"  [OK] No negative cycle.\");\n");
        sb.append("            System.out.println();\n");
        sb.append("            System.out.println(\"Shortest distances from source:\");\n");
        sb.append("            for (int i = 0; i < n; i++) {\n");
        sb.append("                if (dist[i] == Integer.MAX_VALUE) {\n");
        sb.append("                    System.out.println(\"  Vertex \" + i + \": unreachable\");\n");
        sb.append("                } else {\n");
        sb.append("                    System.out.println(\"  Vertex \" + i + \": \" + dist[i]);\n");
        sb.append("                }\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    // ──────────────────── C++ ────────────────────
    private String generateCppCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <climits>\n");
        sb.append("using namespace std;\n\n");

        sb.append("int main() {\n");
        sb.append("    int n = ").append(nodeCount).append(";\n");
        sb.append("    int source = ").append(source).append(";\n");
        sb.append("    vector<vector<int>> edges = ").append(formatEdgesForCpp()).append(";\n\n");

        sb.append("    cout << \"=== Bellman-Ford Shortest Path ===\" << endl;\n");
        sb.append(
                "    cout << \"Vertices: \" << n << \", Edges: \" << edges.size() << \", Source: \" << source << endl << endl;\n\n");

        sb.append("    // Initialize distances\n");
        sb.append("    vector<int> dist(n, INT_MAX);\n");
        sb.append("    vector<int> prev(n, -1);\n");
        sb.append("    dist[source] = 0;\n\n");

        sb.append("    // Relax all edges V-1 times\n");
        sb.append("    for (int i = 1; i < n; i++) {\n");
        sb.append("        cout << \"--- Iteration \" << i << \"/\" << (n-1) << \" ---\" << endl;\n");
        sb.append("        bool changed = false;\n");
        sb.append("        for (auto& e : edges) {\n");
        sb.append("            int u = e[0], v = e[1], w = e[2];\n");
        sb.append("            if (dist[u] != INT_MAX && dist[u] + w < dist[v]) {\n");
        sb.append("                dist[v] = dist[u] + w;\n");
        sb.append("                prev[v] = u;\n");
        sb.append("                changed = true;\n");
        sb.append(
                "                cout << \"  Edge \" << u << \" -> \" << v << \" (w=\" << w << \"): Updated dist[\" << v << \"] = \" << dist[v] << endl;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        if (!changed) {\n");
        sb.append("            cout << \"  No changes - early termination\" << endl;\n");
        sb.append("            break;\n");
        sb.append("        }\n");
        sb.append("        cout << endl;\n");
        sb.append("    }\n\n");

        sb.append("    // Check for negative cycles\n");
        sb.append("    cout << \"Checking for negative cycles...\" << endl;\n");
        sb.append("    bool negativeCycle = false;\n");
        sb.append("    for (auto& e : edges) {\n");
        sb.append("        int u = e[0], v = e[1], w = e[2];\n");
        sb.append("        if (dist[u] != INT_MAX && dist[u] + w < dist[v]) {\n");
        sb.append(
                "            cout << \"  Negative cycle detected at edge \" << u << \" -> \" << v << \"!\" << endl;\n");
        sb.append("            negativeCycle = true;\n");
        sb.append("            break;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    if (!negativeCycle) {\n");
        sb.append("        cout << \"  No negative cycle.\" << endl << endl;\n");
        sb.append("        cout << \"Shortest distances from source:\" << endl;\n");
        sb.append("        for (int i = 0; i < n; i++) {\n");
        sb.append("            if (dist[i] == INT_MAX) {\n");
        sb.append("                cout << \"  Vertex \" << i << \": unreachable\" << endl;\n");
        sb.append("            } else {\n");
        sb.append("                cout << \"  Vertex \" << i << \": \" << dist[i] << endl;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n\n");

        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    // ──────────────────── C ────────────────────
    private String generateCCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Bellman-Ford Shortest Path Algorithm in C\n");
        sb.append("#include <stdio.h>\n");
        sb.append("#include <limits.h>\n");
        sb.append("#include <stdbool.h>\n\n");

        sb.append("#define V ").append(nodeCount).append("\n");
        sb.append("#define E ").append(edges.length > 0 ? edges.length : 10).append("\n");
        sb.append("#define INF INT_MAX\n\n");

        sb.append("typedef struct {\n");
        sb.append("    int u, v, weight;\n");
        sb.append("} Edge;\n\n");

        sb.append("void bellmanFord(Edge edges[], int edgeCount, int src) {\n");
        sb.append("    int dist[V];\n");
        sb.append("    int prev[V];\n\n");

        sb.append("    // Initialize\n");
        sb.append("    for (int i = 0; i < V; i++) {\n");
        sb.append("        dist[i] = INF;\n");
        sb.append("        prev[i] = -1;\n");
        sb.append("    }\n");
        sb.append("    dist[src] = 0;\n\n");

        sb.append("    printf(\"=== Bellman-Ford Algorithm ===\\n\");\n");
        sb.append("    printf(\"Source: %d\\n\\n\", src);\n\n");

        sb.append("    // Relax all edges V-1 times\n");
        sb.append("    for (int i = 1; i < V; i++) {\n");
        sb.append("        printf(\"--- Iteration %d/%d ---\\n\", i, V - 1);\n");
        sb.append("        bool changed = false;\n");
        sb.append("        for (int j = 0; j < edgeCount; j++) {\n");
        sb.append("            int u = edges[j].u;\n");
        sb.append("            int v = edges[j].v;\n");
        sb.append("            int w = edges[j].weight;\n");
        sb.append("            if (dist[u] != INF && dist[u] + w < dist[v]) {\n");
        sb.append("                dist[v] = dist[u] + w;\n");
        sb.append("                prev[v] = u;\n");
        sb.append("                changed = true;\n");
        sb.append("                printf(\"  Edge %d -> %d (w=%d): dist[%d] = %d\\n\", u, v, w, v, dist[v]);\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        if (!changed) {\n");
        sb.append("            printf(\"  No changes - early termination\\n\");\n");
        sb.append("            break;\n");
        sb.append("        }\n");
        sb.append("        printf(\"\\n\");\n");
        sb.append("    }\n\n");

        sb.append("    // Check for negative cycles\n");
        sb.append("    printf(\"Checking for negative cycles...\\n\");\n");
        sb.append("    for (int j = 0; j < edgeCount; j++) {\n");
        sb.append("        if (dist[edges[j].u] != INF && \n");
        sb.append("            dist[edges[j].u] + edges[j].weight < dist[edges[j].v]) {\n");
        sb.append("            printf(\"  Negative cycle detected!\\n\");\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    printf(\"  No negative cycle.\\n\\n\");\n\n");

        sb.append("    // Print results\n");
        sb.append("    printf(\"Shortest Distances:\\n\");\n");
        sb.append("    for (int i = 0; i < V; i++) {\n");
        sb.append("        if (dist[i] == INF) {\n");
        sb.append("            printf(\"  Vertex %d: unreachable\\n\", i);\n");
        sb.append("        } else {\n");
        sb.append("            printf(\"  Vertex %d: %d\\n\", i, dist[i]);\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("}\n\n");

        sb.append("int main() {\n");
        sb.append("    Edge edges[] = {\n");
        if (edges.length > 0) {
            for (int i = 0; i < edges.length; i++) {
                sb.append("        {").append(edges[i][0]).append(", ")
                        .append(edges[i][1]).append(", ").append(edges[i][2]).append("}");
                if (i < edges.length - 1)
                    sb.append(",");
                sb.append("\n");
            }
        } else {
            sb.append("        {0, 1, 6}, {0, 2, 7}, {1, 2, 8}, {1, 3, 5}, {1, 4, -4},\n");
            sb.append("        {2, 3, -3}, {2, 4, 9}, {3, 1, -2}, {4, 0, 2}, {4, 3, 7}\n");
        }
        sb.append("    };\n");
        sb.append("    int edgeCount = sizeof(edges) / sizeof(edges[0]);\n\n");
        sb.append("    bellmanFord(edges, edgeCount, ").append(source).append(");\n");
        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    // ──────────────────── Formatters ────────────────────
    private String formatEdgesForPython() {
        if (edges.length == 0) {
            return "[(0, 1, 6), (0, 2, 7), (1, 2, 8), (1, 3, 5), (1, 4, -4), (2, 3, -3), (2, 4, 9), (3, 1, -2), (4, 0, 2), (4, 3, 7)]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < edges.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append("(").append(edges[i][0]).append(", ")
                    .append(edges[i][1]).append(", ")
                    .append(edges[i][2]).append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatEdgesForJava() {
        if (edges.length == 0) {
            return "{{0,1,6}, {0,2,7}, {1,2,8}, {1,3,5}, {1,4,-4}, {2,3,-3}, {2,4,9}, {3,1,-2}, {4,0,2}, {4,3,7}}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < edges.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append("{").append(edges[i][0]).append(",")
                    .append(edges[i][1]).append(",")
                    .append(edges[i][2]).append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private String formatEdgesForCpp() {
        if (edges.length == 0) {
            return "{{0,1,6}, {0,2,7}, {1,2,8}, {1,3,5}, {1,4,-4}, {2,3,-3}, {2,4,9}, {3,1,-2}, {4,0,2}, {4,3,7}}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < edges.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append("{").append(edges[i][0]).append(",")
                    .append(edges[i][1]).append(",")
                    .append(edges[i][2]).append("}");
        }
        sb.append("}");
        return sb.toString();
    }
}
