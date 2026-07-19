package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.model.ColoringMode;

/**
 * Graph Coloring CSP algorithm code in multiple languages with dynamic parameter syncing
 */
public class GraphColoringCSPCode implements AlgorithmCode {
    
    private int numNodes;
    private int[][] edges;
    private ColoringMode mode;
    private int k;
    
    /**
     * Constructor with default values
     */
    public GraphColoringCSPCode() {
        this.numNodes = 8;
        this.edges = new int[][] {
            {0, 1}, {1, 2}, {2, 3}, {3, 0}, {1, 4}, {4, 5}
        };
        this.mode = ColoringMode.FIND_MINIMUM;
        this.k = 3;
    }
    
    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int numNodes, int[][] edges, ColoringMode mode, int k) {
        this.numNodes = numNodes;
        this.edges = edges != null ? edges : new int[0][];
        this.mode = mode != null ? mode : ColoringMode.FIND_MINIMUM;
        this.k = k;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Graph Coloring CSP";
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
               "    static List<List<Integer>> adj;\n" +
               "    static int[] coloring;\n" +
               "    static List<Set<Integer>> domains;\n" +
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
               "            initDomains(k);\n" +
               "            if (solveCSP()) {\n" +
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
               "        initDomains(K);\n" +
               "        if (solveCSP()) {\n" +
               "            System.out.println(\"\\nSuccessfully colored with \" + K + \" colors!\");\n" +
               "            printSolution();\n" +
               "        } else {\n" +
               "            System.out.println(\"\\nCannot color graph with \" + K + \" colors\");\n" +
               "        }\n"
               ) +
               "    }\n" +
               "\n" +
               "    static void initDomains(int k) {\n" +
               "        domains = new ArrayList<>();\n" +
               "        for (int i = 0; i < N; i++) {\n" +
               "            Set<Integer> domain = new HashSet<>();\n" +
               "            for (int c = 0; c < k; c++) domain.add(c);\n" +
               "            domains.add(domain);\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    static boolean solveCSP() {\n" +
               "        int node = selectNode();\n" +
               "        if (node == -1) return true; // All assigned\n" +
               "\n" +
               "        Set<Integer> domain = new HashSet<>(domains.get(node));\n" +
               "        for (int color : domain) {\n" +
               "            coloring[node] = color;\n" +
               "            List<Set<Integer>> savedDomains = saveDomains();\n" +
               "            if (reduceDomains(node, color) && solveCSP()) {\n" +
               "                return true;\n" +
               "            }\n" +
               "            coloring[node] = -1;\n" +
               "            restoreDomains(savedDomains);\n" +
               "        }\n" +
               "        return false;\n" +
               "    }\n" +
               "\n" +
               "    static int selectNode() {\n" +
               "        int minNode = -1, minSize = Integer.MAX_VALUE;\n" +
               "        for (int i = 0; i < N; i++) {\n" +
               "            if (coloring[i] == -1 && domains.get(i).size() < minSize) {\n" +
               "                minSize = domains.get(i).size();\n" +
               "                minNode = i;\n" +
               "            }\n" +
               "        }\n" +
               "        return minNode;\n" +
               "    }\n" +
               "\n" +
               "    static boolean reduceDomains(int node, int color) {\n" +
               "        for (int neighbor : adj.get(node)) {\n" +
               "            if (coloring[neighbor] == -1) {\n" +
               "                domains.get(neighbor).remove(color);\n" +
               "                if (domains.get(neighbor).isEmpty()) return false;\n" +
               "            }\n" +
               "        }\n" +
               "        return true;\n" +
               "    }\n" +
               "\n" +
               "    static List<Set<Integer>> saveDomains() {\n" +
               "        List<Set<Integer>> saved = new ArrayList<>();\n" +
               "        for (Set<Integer> d : domains) saved.add(new HashSet<>(d));\n" +
               "        return saved;\n" +
               "    }\n" +
               "\n" +
               "    static void restoreDomains(List<Set<Integer>> saved) {\n" +
               "        domains = saved;\n" +
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
               "#define MAX_COLORS 20\n" +
               "\n" +
               "int adj[N][N];\n" +
               "int degree[N];\n" +
               "int coloring[N];\n" +
               "bool domains[N][MAX_COLORS];\n" +
               "int domainSize[N];\n" +
               "\n" +
               "void addEdge(int u, int v) {\n" +
               "    adj[u][degree[u]++] = v;\n" +
               "    adj[v][degree[v]++] = u;\n" +
               "}\n" +
               "\n" +
               "void initDomains(int k) {\n" +
               "    for (int i = 0; i < N; i++) {\n" +
               "        domainSize[i] = k;\n" +
               "        for (int c = 0; c < k; c++) domains[i][c] = true;\n" +
               "        for (int c = k; c < MAX_COLORS; c++) domains[i][c] = false;\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int selectNode() {\n" +
               "    int minNode = -1, minSize = MAX_COLORS + 1;\n" +
               "    for (int i = 0; i < N; i++) {\n" +
               "        if (coloring[i] == -1 && domainSize[i] < minSize) {\n" +
               "            minSize = domainSize[i];\n" +
               "            minNode = i;\n" +
               "        }\n" +
               "    }\n" +
               "    return minNode;\n" +
               "}\n" +
               "\n" +
               "bool reduceDomains(int node, int color, bool savedDomains[N][MAX_COLORS], int savedSizes[N]) {\n" +
               "    memcpy(savedDomains, domains, sizeof(domains));\n" +
               "    memcpy(savedSizes, domainSize, sizeof(domainSize));\n" +
               "    for (int i = 0; i < degree[node]; i++) {\n" +
               "        int neighbor = adj[node][i];\n" +
               "        if (coloring[neighbor] == -1 && domains[neighbor][color]) {\n" +
               "            domains[neighbor][color] = false;\n" +
               "            domainSize[neighbor]--;\n" +
               "            if (domainSize[neighbor] == 0) return false;\n" +
               "        }\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "bool solveCSP() {\n" +
               "    int node = selectNode();\n" +
               "    if (node == -1) return true;\n" +
               "\n" +
               "    for (int c = 0; c < MAX_COLORS; c++) {\n" +
               "        if (domains[node][c]) {\n" +
               "            coloring[node] = c;\n" +
               "            bool savedDomains[N][MAX_COLORS];\n" +
               "            int savedSizes[N];\n" +
               "            if (reduceDomains(node, c, savedDomains, savedSizes) && solveCSP()) {\n" +
               "                return true;\n" +
               "            }\n" +
               "            coloring[node] = -1;\n" +
               "            memcpy(domains, savedDomains, sizeof(domains));\n" +
               "            memcpy(domainSize, savedSizes, sizeof(domainSize));\n" +
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
               "        initDomains(k);\n" +
               "        if (solveCSP()) {\n" +
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
               "    initDomains(K);\n" +
               "    if (solveCSP()) {\n" +
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
               "#include <set>\n" +
               "#include <algorithm>\n" +
               "using namespace std;\n" +
               "\n" +
               "const int N = " + numNodes + ";\n" +
               (isFindMin ? "" : "const int K = " + k + ";\n") +
               "int EDGES[][2] = { " + edgesStr + " };\n" +
               "vector<vector<int>> adj;\n" +
               "vector<int> coloring;\n" +
               "vector<set<int>> domains;\n" +
               "\n" +
               "void initDomains(int k) {\n" +
               "    domains.clear();\n" +
               "    for (int i = 0; i < N; i++) {\n" +
               "        set<int> domain;\n" +
               "        for (int c = 0; c < k; c++) domain.insert(c);\n" +
               "        domains.push_back(domain);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int selectNode() {\n" +
               "    int minNode = -1, minSize = 1000;\n" +
               "    for (int i = 0; i < N; i++) {\n" +
               "        if (coloring[i] == -1 && (int)domains[i].size() < minSize) {\n" +
               "            minSize = domains[i].size();\n" +
               "            minNode = i;\n" +
               "        }\n" +
               "    }\n" +
               "    return minNode;\n" +
               "}\n" +
               "\n" +
               "bool reduceDomains(int node, int color) {\n" +
               "    for (int neighbor : adj[node]) {\n" +
               "        if (coloring[neighbor] == -1) {\n" +
               "            domains[neighbor].erase(color);\n" +
               "            if (domains[neighbor].empty()) return false;\n" +
               "        }\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "vector<set<int>> saveDomains() {\n" +
               "    return domains;\n" +
               "}\n" +
               "\n" +
               "void restoreDomains(const vector<set<int>>& saved) {\n" +
               "    domains = saved;\n" +
               "}\n" +
               "\n" +
               "bool solveCSP() {\n" +
               "    int node = selectNode();\n" +
               "    if (node == -1) return true;\n" +
               "\n" +
               "    set<int> domain = domains[node];\n" +
               "    for (int color : domain) {\n" +
               "        coloring[node] = color;\n" +
               "        auto savedDomains = saveDomains();\n" +
               "        if (reduceDomains(node, color) && solveCSP()) {\n" +
               "            return true;\n" +
               "        }\n" +
               "        coloring[node] = -1;\n" +
               "        restoreDomains(savedDomains);\n" +
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
               "        initDomains(k);\n" +
               "        if (solveCSP()) {\n" +
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
               "    initDomains(K);\n" +
               "    if (solveCSP()) {\n" +
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
               "EDGES = [" + edgesStr + "]\n" +
               "\n" +
               "adj = [[] for _ in range(N)]\n" +
               "for u, v in EDGES:\n" +
               "    adj[u].append(v)\n" +
               "    adj[v].append(u)\n" +
               "\n" +
               "coloring = [-1] * N\n" +
               "domains = []\n" +
               "\n" +
               "def init_domains(k):\n" +
               "    global domains\n" +
               "    domains = [set(range(k)) for _ in range(N)]\n" +
               "\n" +
               "def select_node():\n" +
               "    min_node, min_size = -1, float('inf')\n" +
               "    for i in range(N):\n" +
               "        if coloring[i] == -1 and len(domains[i]) < min_size:\n" +
               "            min_size = len(domains[i])\n" +
               "            min_node = i\n" +
               "    return min_node\n" +
               "\n" +
               "def reduce_domains(node, color):\n" +
               "    for neighbor in adj[node]:\n" +
               "        if coloring[neighbor] == -1:\n" +
               "            domains[neighbor].discard(color)\n" +
               "            if not domains[neighbor]:\n" +
               "                return False\n" +
               "    return True\n" +
               "\n" +
               "def save_domains():\n" +
               "    return [d.copy() for d in domains]\n" +
               "\n" +
               "def restore_domains(saved):\n" +
               "    global domains\n" +
               "    domains = saved\n" +
               "\n" +
               "def solve_csp():\n" +
               "    node = select_node()\n" +
               "    if node == -1:\n" +
               "        return True\n" +
               "\n" +
               "    domain = list(domains[node])\n" +
               "    for color in domain:\n" +
               "        coloring[node] = color\n" +
               "        saved_domains = save_domains()\n" +
               "        if reduce_domains(node, color) and solve_csp():\n" +
               "            return True\n" +
               "        coloring[node] = -1\n" +
               "        restore_domains(saved_domains)\n" +
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
               "    init_domains(k)\n" +
               "    if solve_csp():\n" +
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
               "init_domains(K)\n" +
               "if solve_csp():\n" +
               "    print(f\"\\nSuccessfully colored with {K} colors!\")\n" +
               "    print_solution()\n" +
               "else:\n" +
               "    print(f\"\\nCannot color graph with {K} colors\")\n"
               );
    }
}
