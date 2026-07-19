package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * BFS (Breadth-First Search) algorithm code in multiple languages with dynamic parameter syncing
 */
public class BFSCode implements AlgorithmCode {
    
    private int numNodes;
    private int[][] edges;
    private int startNode;
    
    /**
     * Constructor with default values
     */
    public BFSCode() {
        this.numNodes = 6;
        this.startNode = 0;
        this.edges = new int[][] {
            {0, 1}, {0, 3}, {1, 2}, {1, 4}, {2, 3}, {4, 5}
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
        return "BFS";
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
               "        for (int[] e : EDGES) {\n" +
               "            int u = e[0], v = e[1];\n" +
               "            adj.get(u).add(v);\n" +
               "            adj.get(v).add(u);\n" +
               "        }\n" +
               "        for (int i = 0; i < N; i++) Collections.sort(adj.get(i));\n" +
               "\n" +
               "        List<Integer> order = bfs(START, adj);\n" +
               "\n" +
               "        System.out.println(\"BFS traversal order: \" + order);\n" +
               "    }\n" +
               "\n" +
               "    static List<Integer> bfs(int start, List<List<Integer>> adj) {\n" +
               "        boolean[] visited = new boolean[adj.size()];\n" +
               "        Queue<Integer> queue = new LinkedList<>();\n" +
               "        List<Integer> order = new ArrayList<>();\n" +
               "        queue.offer(start);\n" +
               "\n" +
               "        while (!queue.isEmpty()) {\n" +
               "            int u = queue.poll();\n" +
               "            if (!visited[u]) {\n" +
               "                visited[u] = true;\n" +
               "                order.add(u);\n" +
               "                for (int v : adj.get(u)) {\n" +
               "                    if (!visited[v]) {\n" +
               "                        queue.offer(v);\n" +
               "                    }\n" +
               "                }\n" +
               "            }\n" +
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
               "void bfs(int start) {\n" +
               "    int visited[N] = {0};\n" +
               "    int queue[N], front = 0, rear = 0;\n" +
               "\n" +
               "    queue[rear++] = start;\n" +
               "\n" +
               "    printf(\"BFS traversal order: \");\n" +
               "\n" +
               "    while (front < rear) {\n" +
               "        int u = queue[front++];\n" +
               "        if (!visited[u]) {\n" +
               "            visited[u] = 1;\n" +
               "            printf(\"%d \", u);\n" +
               "\n" +
               "            Node* curr = adj[u];\n" +
               "            while (curr) {\n" +
               "                if (!visited[curr->val]) {\n" +
               "                    queue[rear++] = curr->val;\n" +
               "                }\n" +
               "                curr = curr->next;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    for (int i = 0; i < N; i++) adj[i] = NULL;\n" +
               "\n" +
               "    int edges[][2] = { " + edgesStr + " };\n" +
               "    int numEdges = sizeof(edges)/sizeof(edges[0]);\n" +
               "\n" +
               "    for (int i = 0; i < numEdges; i++)\n" +
               "        addEdge(edges[i][0], edges[i][1]);\n" +
               "\n" +
               "    bfs(START);\n" +
               "\n" +
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
               "#include <queue>\n" +
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
               "    vector<bool> visited(N, false);\n" +
               "    queue<int> q;\n" +
               "    q.push(START);\n" +
               "\n" +
               "    cout << \"BFS traversal order: \";\n" +
               "\n" +
               "    while (!q.empty()) {\n" +
               "        int u = q.front();\n" +
               "        q.pop();\n" +
               "\n" +
               "        if (!visited[u]) {\n" +
               "            visited[u] = true;\n" +
               "            cout << u << \" \";\n" +
               "\n" +
               "            for (int v : adj[u]) {\n" +
               "                if (!visited[v]) {\n" +
               "                    q.push(v);\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
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
        
        return "from collections import deque\n" +
               "\n" +
               "N = " + numNodes + "\n" +
               "START = " + startNode + "\n" +
               "EDGES = [" + edgesStr + "]\n" +
               "\n" +
               "adj = [[] for _ in range(N)]\n" +
               "for u, v in EDGES:\n" +
               "    adj[u].append(v)\n" +
               "    adj[v].append(u)\n" +
               "\n" +
               "for neighbors in adj:\n" +
               "    neighbors.sort()\n" +
               "\n" +
               "def bfs(start, adj):\n" +
               "    visited = [False] * len(adj)\n" +
               "    queue = deque([start])\n" +
               "    order = []\n" +
               "\n" +
               "    while queue:\n" +
               "        u = queue.popleft()\n" +
               "        if not visited[u]:\n" +
               "            visited[u] = True\n" +
               "            order.append(u)\n" +
               "            for v in adj[u]:\n" +
               "                if not visited[v]:\n" +
               "                    queue.append(v)\n" +
               "    return order\n" +
               "\n" +
               "order = bfs(START, adj)\n" +
               "print(\"BFS traversal order:\", order)\n";
    }
}
