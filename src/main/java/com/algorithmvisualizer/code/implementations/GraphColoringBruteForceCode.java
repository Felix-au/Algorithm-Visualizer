package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.model.ColoringMode;

/**
 * Graph Coloring Brute Force algorithm code in multiple languages with dynamic parameter syncing
 */
public class GraphColoringBruteForceCode implements AlgorithmCode {
    
    private int numNodes;
    private int[][] edges;
    private ColoringMode mode;
    private int k;
    private boolean anchorV0;
    
    /**
     * Constructor with default values
     */
    public GraphColoringBruteForceCode() {
        this.numNodes = 8;
        this.edges = new int[][] {
            {0, 1}, {1, 2}, {2, 3}, {3, 0}, {1, 4}, {4, 5}
        };
        this.mode = ColoringMode.FIND_MINIMUM;
        this.k = 3;
        this.anchorV0 = true;
    }
    
    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int numNodes, int[][] edges, ColoringMode mode, int k, boolean anchorV0) {
        this.numNodes = numNodes;
        this.edges = edges != null ? edges : new int[0][];
        this.mode = mode != null ? mode : ColoringMode.FIND_MINIMUM;
        this.k = k;
        this.anchorV0 = anchorV0;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Graph Coloring Brute Force";
    }
    
    @Override
    public String getJavaCode() {
        StringBuilder edgesStr = new StringBuilder();
        for (int i = 0; i < edges.length; i++) {
            edgesStr.append("{").append(edges[i][0]).append(", ").append(edges[i][1]).append("}");
            if (i < edges.length - 1) edgesStr.append(", ");
        }
        
        boolean isFindMin = (mode == ColoringMode.FIND_MINIMUM);
        
        return "import java.util.*;\n" +
               "\n" +
               "public class Main {\n" +
               "    static final int N = " + numNodes + ";\n" +
               "    static final int[][] EDGES = { " + edgesStr + " };\n" +
               (isFindMin ? "" : "    static final int K = " + k + ";\n") +
               "    static final boolean ANCHOR_V0 = " + anchorV0 + ";\n" +
               "    static List<List<Integer>> adj;\n" +
               "    static int[] coloring;\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        adj = new ArrayList<>();\n" +
               "        for (int i = 0; i < N; i++) adj.add(new ArrayList<>());\n" +
               "        for (int[] e : EDGES) {\n" +
               "            adj.get(e[0]).add(e[1]);\n" +
               "            adj.get(e[1]).add(e[0]);\n" +
               "        }\n" +
               "\n" +
               (isFindMin ? 
               "        // Find minimum colors needed\n" +
               "        for (int k = 1; k <= N; k++) {\n" +
               "            System.out.println(\"Trying k = \" + k + \" colors...\");\n" +
               "            coloring = new int[N];\n" +
               "            Arrays.fill(coloring, -1);\n" +
               "            if (ANCHOR_V0 && k > 0) coloring[0] = 0;\n" +
               "            if (solve(ANCHOR_V0 ? 1 : 0, k)) {\n" +
               "                System.out.println(\"\\nMinimum colors needed: \" + k);\n" +
               "                printSolution();\n" +
               "                return;\n" +
               "            }\n" +
               "        }\n" +
               "        System.out.println(\"No solution found\");\n"
               :
               "        // Try to color with K colors\n" +
               "        System.out.println(\"Attempting to color graph with \" + K + \" colors...\");\n" +
               "        coloring = new int[N];\n" +
               "        Arrays.fill(coloring, -1);\n" +
               "        if (ANCHOR_V0 && K > 0) coloring[0] = 0;\n" +
               "        if (solve(ANCHOR_V0 ? 1 : 0, K)) {\n" +
               "            System.out.println(\"\\nSuccessfully colored with \" + K + \" colors!\");\n" +
               "            printSolution();\n" +
               "        } else {\n" +
               "            System.out.println(\"\\nCannot color graph with \" + K + \" colors\");\n" +
               "        }\n"
               ) +
               "    }\n" +
               "\n" +
               "    static boolean solve(int node, int k) {\n" +
               "        if (node == N) return true;\n" +
               "\n" +
               "        for (int c = 0; c < k; c++) {\n" +
               "            if (isValid(node, c)) {\n" +
               "                coloring[node] = c;\n" +
               "                if (solve(node + 1, k)) return true;\n" +
               "                coloring[node] = -1;\n" +
               "            }\n" +
               "        }\n" +
               "        return false;\n" +
               "    }\n" +
               "\n" +
               "    static boolean isValid(int node, int color) {\n" +
               "        for (int neighbor : adj.get(node)) {\n" +
               "            if (coloring[neighbor] == color) return false;\n" +
               "        }\n" +
               "        return true;\n" +
               "    }\n" +
               "\n" +
               "    static void printSolution() {\n" +
               "        System.out.print(\"Coloring: \");\n" +
               "        for (int i = 0; i < N; i++) {\n" +
               "            System.out.print(\"v\" + i + \"=\" + coloring[i] + \" \");\n" +
               "        }\n" +
               "        System.out.println();\n" +
               "    }\n" +
               "}\n";
    }
    
    @Override
    public String getCCode() {
        StringBuilder edgesStr = new StringBuilder();
        for (int i = 0; i < edges.length; i++) {
            edgesStr.append("{").append(edges[i][0]).append(", ").append(edges[i][1]).append("}");
            if (i < edges.length - 1) edgesStr.append(", ");
        }
        
        boolean isFindMin = (mode == ColoringMode.FIND_MINIMUM);
        
        return "#include <stdio.h>\n" +
               "#include <stdbool.h>\n" +
               "#include <string.h>\n" +
               "\n" +
               "#define N " + numNodes + "\n" +
               (isFindMin ? "" : "#define K " + k + "\n") +
               "#define ANCHOR_V0 " + (anchorV0 ? "1" : "0") + "\n" +
               "\n" +
               "int adj[N][N];\n" +
               "int degree[N];\n" +
               "int coloring[N];\n" +
               "\n" +
               "void addEdge(int u, int v) {\n" +
               "    adj[u][degree[u]++] = v;\n" +
               "    adj[v][degree[v]++] = u;\n" +
               "}\n" +
               "\n" +
               "bool isValid(int node, int color) {\n" +
               "    for (int i = 0; i < degree[node]; i++) {\n" +
               "        int neighbor = adj[node][i];\n" +
               "        if (coloring[neighbor] == color) return false;\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "bool solve(int node, int k) {\n" +
               "    if (node == N) return true;\n" +
               "\n" +
               "    for (int c = 0; c < k; c++) {\n" +
               "        if (isValid(node, c)) {\n" +
               "            coloring[node] = c;\n" +
               "            if (solve(node + 1, k)) return true;\n" +
               "            coloring[node] = -1;\n" +
               "        }\n" +
               "    }\n" +
               "    return false;\n" +
               "}\n" +
               "\n" +
               "void printSolution() {\n" +
               "    printf(\"Coloring: \");\n" +
               "    for (int i = 0; i < N; i++) {\n" +
               "        printf(\"v%d=%d \", i, coloring[i]);\n" +
               "    }\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    memset(degree, 0, sizeof(degree));\n" +
               "    int edges[][2] = { " + edgesStr + " };\n" +
               "    int numEdges = sizeof(edges) / sizeof(edges[0]);\n" +
               "    for (int i = 0; i < numEdges; i++)\n" +
               "        addEdge(edges[i][0], edges[i][1]);\n" +
               "\n" +
               (isFindMin ?
               "    // Find minimum colors needed\n" +
               "    for (int k = 1; k <= N; k++) {\n" +
               "        printf(\"Trying k = %d colors...\\n\", k);\n" +
               "        memset(coloring, -1, sizeof(coloring));\n" +
               "        if (ANCHOR_V0 && k > 0) coloring[0] = 0;\n" +
               "        if (solve(ANCHOR_V0 ? 1 : 0, k)) {\n" +
               "            printf(\"\\nMinimum colors needed: %d\\n\", k);\n" +
               "            printSolution();\n" +
               "            return 0;\n" +
               "        }\n" +
               "    }\n" +
               "    printf(\"No solution found\\n\");\n"
               :
               "    // Try to color with K colors\n" +
               "    printf(\"Attempting to color graph with %d colors...\\n\", K);\n" +
               "    memset(coloring, -1, sizeof(coloring));\n" +
               "    if (ANCHOR_V0 && K > 0) coloring[0] = 0;\n" +
               "    if (solve(ANCHOR_V0 ? 1 : 0, K)) {\n" +
               "        printf(\"\\nSuccessfully colored with %d colors!\\n\", K);\n" +
               "        printSolution();\n" +
               "    } else {\n" +
               "        printf(\"\\nCannot color graph with %d colors\\n\", K);\n" +
               "    }\n"
               ) +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getCppCode() {
        StringBuilder edgesStr = new StringBuilder();
        for (int i = 0; i < edges.length; i++) {
            edgesStr.append("{").append(edges[i][0]).append(", ").append(edges[i][1]).append("}");
            if (i < edges.length - 1) edgesStr.append(", ");
        }
        
        boolean isFindMin = (mode == ColoringMode.FIND_MINIMUM);
        
        return "#include <iostream>\n" +
               "#include <vector>\n" +
               "#include <algorithm>\n" +
               "using namespace std;\n" +
               "\n" +
               "const int N = " + numNodes + ";\n" +
               (isFindMin ? "" : "const int K = " + k + ";\n") +
               "const bool ANCHOR_V0 = " + (anchorV0 ? "true" : "false") + ";\n" +
               "int EDGES[][2] = { " + edgesStr + " };\n" +
               "vector<vector<int>> adj;\n" +
               "vector<int> coloring;\n" +
               "\n" +
               "bool isValid(int node, int color) {\n" +
               "    for (int neighbor : adj[node]) {\n" +
               "        if (coloring[neighbor] == color) return false;\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "bool solve(int node, int k) {\n" +
               "    if (node == N) return true;\n" +
               "\n" +
               "    for (int c = 0; c < k; c++) {\n" +
               "        if (isValid(node, c)) {\n" +
               "            coloring[node] = c;\n" +
               "            if (solve(node + 1, k)) return true;\n" +
               "            coloring[node] = -1;\n" +
               "        }\n" +
               "    }\n" +
               "    return false;\n" +
               "}\n" +
               "\n" +
               "void printSolution() {\n" +
               "    cout << \"Coloring: \";\n" +
               "    for (int i = 0; i < N; i++) {\n" +
               "        cout << \"v\" << i << \"=\" << coloring[i] << \" \";\n" +
               "    }\n" +
               "    cout << endl;\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    adj.resize(N);\n" +
               "    for (auto& e : EDGES) {\n" +
               "        adj[e[0]].push_back(e[1]);\n" +
               "        adj[e[1]].push_back(e[0]);\n" +
               "    }\n" +
               "\n" +
               (isFindMin ?
               "    // Find minimum colors needed\n" +
               "    for (int k = 1; k <= N; k++) {\n" +
               "        cout << \"Trying k = \" << k << \" colors...\" << endl;\n" +
               "        coloring.assign(N, -1);\n" +
               "        if (ANCHOR_V0 && k > 0) coloring[0] = 0;\n" +
               "        if (solve(ANCHOR_V0 ? 1 : 0, k)) {\n" +
               "            cout << \"\\nMinimum colors needed: \" << k << endl;\n" +
               "            printSolution();\n" +
               "            return 0;\n" +
               "        }\n" +
               "    }\n" +
               "    cout << \"No solution found\" << endl;\n"
               :
               "    // Try to color with K colors\n" +
               "    cout << \"Attempting to color graph with \" << K << \" colors...\" << endl;\n" +
               "    coloring.assign(N, -1);\n" +
               "    if (ANCHOR_V0 && K > 0) coloring[0] = 0;\n" +
               "    if (solve(ANCHOR_V0 ? 1 : 0, K)) {\n" +
               "        cout << \"\\nSuccessfully colored with \" << K << \" colors!\" << endl;\n" +
               "        printSolution();\n" +
               "    } else {\n" +
               "        cout << \"\\nCannot color graph with \" << K << \" colors\" << endl;\n" +
               "    }\n"
               ) +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getPythonCode() {
        StringBuilder edgesStr = new StringBuilder();
        for (int i = 0; i < edges.length; i++) {
            edgesStr.append("[").append(edges[i][0]).append(", ").append(edges[i][1]).append("]");
            if (i < edges.length - 1) edgesStr.append(", ");
        }
        
        boolean isFindMin = (mode == ColoringMode.FIND_MINIMUM);
        
        return "N = " + numNodes + "\n" +
               (isFindMin ? "" : "K = " + k + "\n") +
               "ANCHOR_V0 = " + (anchorV0 ? "True" : "False") + "\n" +
               "EDGES = [" + edgesStr + "]\n" +
               "\n" +
               "adj = [[] for _ in range(N)]\n" +
               "for u, v in EDGES:\n" +
               "    adj[u].append(v)\n" +
               "    adj[v].append(u)\n" +
               "\n" +
               "coloring = [-1] * N\n" +
               "\n" +
               "def is_valid(node, color):\n" +
               "    for neighbor in adj[node]:\n" +
               "        if coloring[neighbor] == color:\n" +
               "            return False\n" +
               "    return True\n" +
               "\n" +
               "def solve(node, k):\n" +
               "    if node == N:\n" +
               "        return True\n" +
               "\n" +
               "    for c in range(k):\n" +
               "        if is_valid(node, c):\n" +
               "            coloring[node] = c\n" +
               "            if solve(node + 1, k):\n" +
               "                return True\n" +
               "            coloring[node] = -1\n" +
               "    return False\n" +
               "\n" +
               "def print_solution():\n" +
               "    print(\"Coloring:\", end=\" \")\n" +
               "    for i in range(N):\n" +
               "        print(f\"v{i}={coloring[i]}\", end=\" \")\n" +
               "    print()\n" +
               "\n" +
               (isFindMin ?
               "# Find minimum colors needed\n" +
               "found = False\n" +
               "for k in range(1, N + 1):\n" +
               "    print(f\"Trying k = {k} colors...\")\n" +
               "    coloring = [-1] * N\n" +
               "    if ANCHOR_V0 and k > 0:\n" +
               "        coloring[0] = 0\n" +
               "    if solve(1 if ANCHOR_V0 else 0, k):\n" +
               "        print(f\"\\nMinimum colors needed: {k}\")\n" +
               "        print_solution()\n" +
               "        found = True\n" +
               "        break\n" +
               "if not found:\n" +
               "    print(\"No solution found\")\n"
               :
               "# Try to color with K colors\n" +
               "print(f\"Attempting to color graph with {K} colors...\")\n" +
               "coloring = [-1] * N\n" +
               "if ANCHOR_V0 and K > 0:\n" +
               "    coloring[0] = 0\n" +
               "if solve(1 if ANCHOR_V0 else 0, K):\n" +
               "    print(f\"\\nSuccessfully colored with {K} colors!\")\n" +
               "    print_solution()\n" +
               "else:\n" +
               "    print(f\"\\nCannot color graph with {K} colors\")\n"
               );
    }
}
