package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * DFS (Depth-First Search) algorithm code in multiple languages with dynamic parameter syncing
 */
public class DFSCode implements AlgorithmCode {
    
    private int numNodes;
    private int[][] edges;
    private int startNode;
    
    /**
     * Constructor with default values
     */
    public DFSCode() {
        this.numNodes = 9;
        this.startNode = 0;
        this.edges = new int[][] {
            {0, 2}, {0, 5}, {0, 7}, {1, 3}, {1, 5}, {1, 8},
            {2, 4}, {2, 8}, {3, 4}, {3, 5}, {3, 6}, {4, 8}, {5, 6}, {5, 8}
        };
    }
    
    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int numNodes, int[][] edges, int startNode) {
        this.numNodes = numNodes;
        this.edges = edges != null ? edges : new int[0][];
        this.startNode = startNode;
    }
    
    @Override
    public String getAlgorithmName() {
        return "DFS";
    }
    
    @Override
    public String getJavaCode() {
        StringBuilder edgesStr = new StringBuilder();
        for (int i = 0; i < edges.length; i++) {
            edgesStr.append("{").append(edges[i][0]).append(", ").append(edges[i][1]).append("}");
            if (i < edges.length - 1) edgesStr.append(", ");
        }
        
        return "import java.util.*;\n" +
               "\n" +
               "public class Main {\n" +
               "    static final int N = " + numNodes + ";\n" +
               "    static final int START = " + startNode + ";\n" +
               "    static final int[][] EDGES = { " + edgesStr + " };\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        List<List<Integer>> adj = new ArrayList<>();\n" +
               "        for (int i = 0; i < N; i++) adj.add(new ArrayList<>());\n" +
               "        for (int[] e : EDGES) { int u = e[0], v = e[1]; adj.get(u).add(v); adj.get(v).add(u); }\n" +
               "        for (int i = 0; i < N; i++) Collections.sort(adj.get(i));\n" +
               "\n" +
               "        long startTime = System.currentTimeMillis();\n" +
               "        List<Integer> order = dfsIterative(START, adj);\n" +
               "        long endTime = System.currentTimeMillis();\n" +
               "\n" +
               "        System.out.println(\"DFS traversal order: \" + order);\n" +
               "        System.out.println(\"Execution time: \" + (endTime - startTime) + \" ms\");\n" +
               "    }\n" +
               "\n" +
               "    static List<Integer> dfsIterative(int start, List<List<Integer>> adj) {\n" +
               "        boolean[] visited = new boolean[adj.size()];\n" +
               "        Deque<Integer> stack = new ArrayDeque<>();\n" +
               "        List<Integer> order = new ArrayList<>();\n" +
               "        stack.push(start);\n" +
               "        while (!stack.isEmpty()) {\n" +
               "            int u = stack.peek();\n" +
               "            if (!visited[u]) {\n" +
               "                visited[u] = true;\n" +
               "                order.add(u);\n" +
               "            }\n" +
               "            boolean advanced = false;\n" +
               "            for (int v : adj.get(u)) {\n" +
               "                if (!visited[v]) {\n" +
               "                    stack.push(v);\n" +
               "                    advanced = true;\n" +
               "                    break;\n" +
               "                }\n" +
               "            }\n" +
               "            if (!advanced) stack.pop();\n" +
               "        }\n" +
               "        return order;\n" +
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
        
        return "#include <stdio.h>\n" +
               "#include <stdlib.h>\n" +
               "\n" +
               "#define N " + numNodes + "\n" +
               "#define START " + startNode + "\n" +
               "\n" +
               "typedef struct Node {\n" +
               "    int val;\n" +
               "    struct Node* next;\n" +
               "} Node;\n" +
               "\n" +
               "Node* adj[N];\n" +
               "\n" +
               "void addEdge(int u, int v) {\n" +
               "    Node* nodeU = (Node*)malloc(sizeof(Node));\n" +
               "    nodeU->val = v;\n" +
               "    nodeU->next = adj[u];\n" +
               "    adj[u] = nodeU;\n" +
               "\n" +
               "    Node* nodeV = (Node*)malloc(sizeof(Node));\n" +
               "    nodeV->val = u;\n" +
               "    nodeV->next = adj[v];\n" +
               "    adj[v] = nodeV;\n" +
               "}\n" +
               "\n" +
               "void freeGraph() {\n" +
               "    for (int i = 0; i < N; i++) {\n" +
               "        Node* curr = adj[i];\n" +
               "        while (curr) {\n" +
               "            Node* tmp = curr;\n" +
               "            curr = curr->next;\n" +
               "            free(tmp);\n" +
               "        }\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int cmpfunc(const void* a, const void* b) {\n" +
               "    return (*(int*)a - *(int*)b);\n" +
               "}\n" +
               "\n" +
               "void dfsIterative(int start) {\n" +
               "    int visited[N] = {0};\n" +
               "    int stack[N], top = -1;\n" +
               "    int order[N], orderIdx = 0;\n" +
               "\n" +
               "    stack[++top] = start;\n" +
               "\n" +
               "    while (top >= 0) {\n" +
               "        int u = stack[top];\n" +
               "\n" +
               "        if (!visited[u]) {\n" +
               "            visited[u] = 1;\n" +
               "            order[orderIdx++] = u;\n" +
               "        }\n" +
               "\n" +
               "        int advanced = 0;\n" +
               "        // Push neighbors in order\n" +
               "        int neighbors[N], count = 0;\n" +
               "        Node* curr = adj[u];\n" +
               "        while (curr) {\n" +
               "            neighbors[count++] = curr->val;\n" +
               "            curr = curr->next;\n" +
               "        }\n" +
               "        qsort(neighbors, count, sizeof(int), cmpfunc);\n" +
               "\n" +
               "        for (int i = 0; i < count; i++) {\n" +
               "            if (!visited[neighbors[i]]) {\n" +
               "                stack[++top] = neighbors[i];\n" +
               "                advanced = 1;\n" +
               "                break;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        if (!advanced) top--;\n" +
               "    }\n" +
               "\n" +
               "    printf(\"DFS traversal order: \");\n" +
               "    for (int i = 0; i < orderIdx; i++)\n" +
               "        printf(\"%d \", order[i]);\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    for (int i = 0; i < N; i++) adj[i] = NULL;\n" +
               "\n" +
               "    int edges[][2] = { " + edgesStr + " };\n" +
               "    int numEdges = sizeof(edges)/sizeof(edges[0]);\n" +
               "    for (int i = 0; i < numEdges; i++)\n" +
               "        addEdge(edges[i][0], edges[i][1]);\n" +
               "\n" +
               "    dfsIterative(START);\n" +
               "\n" +
               "    freeGraph();\n" +
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
        
        return "#include <iostream>\n" +
               "#include <vector>\n" +
               "#include <stack>\n" +
               "#include <algorithm>\n" +
               "using namespace std;\n" +
               "\n" +
               "const int N = " + numNodes + ";\n" +
               "const int START = " + startNode + ";\n" +
               "int EDGES[][2] = { " + edgesStr + " };\n" +
               "\n" +
               "int main() {\n" +
               "    vector<vector<int>> adj(N);\n" +
               "    for (auto& e : EDGES) {\n" +
               "        int u = e[0], v = e[1];\n" +
               "        adj[u].push_back(v);\n" +
               "        adj[v].push_back(u);\n" +
               "    }\n" +
               "    for (int i = 0; i < N; i++) sort(adj[i].begin(), adj[i].end());\n" +
               "\n" +
               "    vector<int> order;\n" +
               "    vector<bool> visited(N, false);\n" +
               "    stack<int> st;\n" +
               "    st.push(START);\n" +
               "\n" +
               "    while (!st.empty()) {\n" +
               "        int u = st.top();\n" +
               "        if (!visited[u]) {\n" +
               "            visited[u] = true;\n" +
               "            order.push_back(u);\n" +
               "        }\n" +
               "        bool advanced = false;\n" +
               "        for (int v : adj[u]) {\n" +
               "            if (!visited[v]) {\n" +
               "                st.push(v);\n" +
               "                advanced = true;\n" +
               "                break;\n" +
               "            }\n" +
               "        }\n" +
               "        if (!advanced) st.pop();\n" +
               "    }\n" +
               "\n" +
               "    cout << \"DFS traversal order: \";\n" +
               "    for (int v : order) cout << v << \" \";\n" +
               "    cout << endl;\n" +
               "\n" +
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
        
        return "N = " + numNodes + "\n" +
               "START = " + startNode + "\n" +
               "EDGES = [ " + edgesStr + " ]\n" +
               "\n" +
               "adj = [[] for _ in range(N)]\n" +
               "for u, v in EDGES:\n" +
               "    adj[u].append(v)\n" +
               "    adj[v].append(u)\n" +
               "for neighbors in adj:\n" +
               "    neighbors.sort()\n" +
               "\n" +
               "def dfs_iterative(start, adj):\n" +
               "    visited = [False] * len(adj)\n" +
               "    stack = [start]\n" +
               "    order = []\n" +
               "\n" +
               "    while stack:\n" +
               "        u = stack[-1]\n" +
               "        if not visited[u]:\n" +
               "            visited[u] = True\n" +
               "            order.append(u)\n" +
               "\n" +
               "        advanced = False\n" +
               "        for v in adj[u]:\n" +
               "            if not visited[v]:\n" +
               "                stack.append(v)\n" +
               "                advanced = True\n" +
               "                break\n" +
               "        if not advanced:\n" +
               "            stack.pop()\n" +
               "    return order\n" +
               "\n" +
               "order = dfs_iterative(START, adj)\n" +
               "print(\"DFS traversal order:\", order)\n";
    }
}
