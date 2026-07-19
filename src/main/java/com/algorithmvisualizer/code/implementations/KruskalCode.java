package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class KruskalCode implements AlgorithmCode {
    
    private int nodeCount = 6;
    private int[][] edges = new int[0][];

    @Override
    public String getAlgorithmName() {
        return "MST Kruskal";
    }

    @Override
    public String getCCode() {
        return generateCCode();
    }

    public void updateParameters(int n, int[][] edges) {
        this.nodeCount = n;
        this.edges = edges;
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
        sb.append("# Kruskal's Algorithm for Minimum Spanning Tree\n");
        sb.append("# Uses Union-Find data structure\n\n");
        
        sb.append("class UnionFind:\n");
        sb.append("    def __init__(self, n):\n");
        sb.append("        self.parent = list(range(n))\n");
        sb.append("        self.rank = [0] * n\n\n");
        
        sb.append("    def find(self, x):\n");
        sb.append("        if self.parent[x] != x:\n");
        sb.append("            self.parent[x] = self.find(self.parent[x])  # Path compression\n");
        sb.append("        return self.parent[x]\n\n");
        
        sb.append("    def union(self, x, y):\n");
        sb.append("        root_x = self.find(x)\n");
        sb.append("        root_y = self.find(y)\n");
        sb.append("        if root_x == root_y:\n");
        sb.append("            return False\n");
        sb.append("        # Union by rank\n");
        sb.append("        if self.rank[root_x] < self.rank[root_y]:\n");
        sb.append("            self.parent[root_x] = root_y\n");
        sb.append("        elif self.rank[root_x] > self.rank[root_y]:\n");
        sb.append("            self.parent[root_y] = root_x\n");
        sb.append("        else:\n");
        sb.append("            self.parent[root_y] = root_x\n");
        sb.append("            self.rank[root_x] += 1\n");
        sb.append("        return True\n\n");
        
        sb.append("def kruskal_mst(n, edges):\n");
        sb.append("    print(\"=== Kruskal's MST Algorithm ===\")\n");
        sb.append("    print(f\"Graph: {n} vertices, {len(edges)} edges\")\n");
        sb.append("    print()\n\n");
        
        sb.append("    # Sort edges by weight\n");
        sb.append("    edges.sort(key=lambda e: e[2])\n");
        sb.append("    print(\"Sorted edges by weight:\")\n");
        sb.append("    for u, v, w in edges:\n");
        sb.append("        print(f\"  {u}-{v} (weight: {w})\")\n");
        sb.append("    print()\n\n");
        
        sb.append("    uf = UnionFind(n)\n");
        sb.append("    mst = []\n");
        sb.append("    total_weight = 0\n\n");
        
        sb.append("    print(\"Building MST:\")\n");
        sb.append("    for u, v, weight in edges:\n");
        sb.append("        # Check if adding edge creates cycle\n");
        sb.append("        if uf.find(u) != uf.find(v):\n");
        sb.append("            uf.union(u, v)\n");
        sb.append("            mst.append((u, v, weight))\n");
        sb.append("            total_weight += weight\n");
        sb.append("            print(f\"  ✓ Added: {u}-{v} (weight: {weight}) | Total: {total_weight}\")\n");
        sb.append("            if len(mst) == n - 1:\n");
        sb.append("                break\n");
        sb.append("        else:\n");
        sb.append("            print(f\"  ✗ Rejected: {u}-{v} (weight: {weight}) - creates cycle\")\n\n");
        
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
        sb.append("edges = ").append(formatEdgesPython()).append("\n\n");
        
        sb.append("# Find MST\n");
        sb.append("mst, total_weight = kruskal_mst(n, edges)\n");
        
        return sb.toString();
    }

    private String generateJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Kruskal's Algorithm for Minimum Spanning Tree\n");
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
        
        sb.append("class UnionFind {\n");
        sb.append("    int[] parent, rank;\n\n");
        
        sb.append("    UnionFind(int n) {\n");
        sb.append("        parent = new int[n];\n");
        sb.append("        rank = new int[n];\n");
        sb.append("        for (int i = 0; i < n; i++) parent[i] = i;\n");
        sb.append("    }\n\n");
        
        sb.append("    int find(int x) {\n");
        sb.append("        if (parent[x] != x)\n");
        sb.append("            parent[x] = find(parent[x]); // Path compression\n");
        sb.append("        return parent[x];\n");
        sb.append("    }\n\n");
        
        sb.append("    boolean union(int x, int y) {\n");
        sb.append("        int rootX = find(x), rootY = find(y);\n");
        sb.append("        if (rootX == rootY) return false;\n");
        sb.append("        // Union by rank\n");
        sb.append("        if (rank[rootX] < rank[rootY]) parent[rootX] = rootY;\n");
        sb.append("        else if (rank[rootX] > rank[rootY]) parent[rootY] = rootX;\n");
        sb.append("        else { parent[rootY] = rootX; rank[rootX]++; }\n");
        sb.append("        return true;\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("public class Main {\n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        int n = ").append(nodeCount).append(";\n");
        sb.append("        Edge[] edges = ").append(formatEdgesJava()).append(";\n\n");
        
        sb.append("        System.out.println(\"=== Kruskal's MST Algorithm ===\"");
sb.append(");\n");
        sb.append("        System.out.println(\"Graph: \" + n + \" vertices, \" + edges.length + \" edges\");\n");
        sb.append("        System.out.println();\n\n");
        
        sb.append("        // Sort edges by weight\n");
        sb.append("        Arrays.sort(edges);\n");
        sb.append("        System.out.println(\"Sorted edges by weight:\");\n");
        sb.append("        for (Edge e : edges) System.out.println(\"  \" + e);\n");
        sb.append("        System.out.println();\n\n");
        
        sb.append("        UnionFind uf = new UnionFind(n);\n");
        sb.append("        List<Edge> mst = new ArrayList<>();\n");
        sb.append("        int totalWeight = 0;\n\n");
        
        sb.append("        System.out.println(\"Building MST:\");\n");
        sb.append("        for (Edge e : edges) {\n");
        sb.append("            if (uf.find(e.u) != uf.find(e.v)) {\n");
        sb.append("                uf.union(e.u, e.v);\n");
        sb.append("                mst.add(e);\n");
        sb.append("                totalWeight += e.weight;\n");
        sb.append("                System.out.println(\"  ✓ Added: \" + e + \" | Total: \" + totalWeight);\n");
        sb.append("                if (mst.size() == n - 1) break;\n");
        sb.append("            } else {\n");
        sb.append("                System.out.println(\"  ✗ Rejected: \" + e + \" (creates cycle)\");\n");
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
        sb.append("// Kruskal's Algorithm for Minimum Spanning Tree\n");
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <algorithm>\n");
        sb.append("using namespace std;\n\n");
        
        sb.append("struct Edge {\n");
        sb.append("    int u, v, weight;\n");
        sb.append("    bool operator<(const Edge& other) const {\n");
        sb.append("        return weight < other.weight;\n");
        sb.append("    }\n");
        sb.append("};\n\n");
        
        sb.append("class UnionFind {\n");
        sb.append("    vector<int> parent, rank;\n");
        sb.append("public:\n");
        sb.append("    UnionFind(int n) : parent(n), rank(n, 0) {\n");
        sb.append("        for (int i = 0; i < n; i++) parent[i] = i;\n");
        sb.append("    }\n\n");
        
        sb.append("    int find(int x) {\n");
        sb.append("        if (parent[x] != x)\n");
        sb.append("            parent[x] = find(parent[x]); // Path compression\n");
        sb.append("        return parent[x];\n");
        sb.append("    }\n\n");
        
        sb.append("    bool unite(int x, int y) {\n");
        sb.append("        int rootX = find(x), rootY = find(y);\n");
        sb.append("        if (rootX == rootY) return false;\n");
        sb.append("        // Union by rank\n");
        sb.append("        if (rank[rootX] < rank[rootY]) parent[rootX] = rootY;\n");
        sb.append("        else if (rank[rootX] > rank[rootY]) parent[rootY] = rootX;\n");
        sb.append("        else { parent[rootY] = rootX; rank[rootX]++; }\n");
        sb.append("        return true;\n");
        sb.append("    }\n");
        sb.append("};\n\n");
        
        sb.append("int main() {\n");
        sb.append("    int n = ").append(nodeCount).append(";\n");
        sb.append("    vector<Edge> edges = ").append(formatEdgesCpp()).append(";\n\n");
        
        sb.append("    cout << \"=== Kruskal's MST Algorithm ===\" << endl;\n");
        sb.append("    cout << \"Graph: \" << n << \" vertices, \" << edges.size() << \" edges\" << endl;\n");
        sb.append("    cout << endl;\n\n");
        
        sb.append("    // Sort edges by weight\n");
        sb.append("    sort(edges.begin(), edges.end());\n");
        sb.append("    cout << \"Sorted edges by weight:\" << endl;\n");
        sb.append("    for (const Edge& e : edges)\n");
        sb.append("        cout << \"  \" << e.u << \"-\" << e.v << \" (weight: \" << e.weight << \")\" << endl;\n");
        sb.append("    cout << endl;\n\n");
        
        sb.append("    UnionFind uf(n);\n");
        sb.append("    vector<Edge> mst;\n");
        sb.append("    int totalWeight = 0;\n\n");
        
        sb.append("    cout << \"Building MST:\" << endl;\n");
        sb.append("    for (const Edge& e : edges) {\n");
        sb.append("        if (uf.find(e.u) != uf.find(e.v)) {\n");
        sb.append("            uf.unite(e.u, e.v);\n");
        sb.append("            mst.push_back(e);\n");
        sb.append("            totalWeight += e.weight;\n");
        sb.append("            cout << \"  ✓ Added: \" << e.u << \"-\" << e.v << \" (weight: \" << e.weight << \") | Total: \" << totalWeight << endl;\n");
        sb.append("            if (mst.size() == n - 1) break;\n");
        sb.append("        } else {\n");
        sb.append("            cout << \"  ✗ Rejected: \" << e.u << \"-\" << e.v << \" (weight: \" << e.weight << \") - creates cycle\" << endl;\n");
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

    private String formatEdgesJava() {
        StringBuilder sb = new StringBuilder("{\n");
        for (int i = 0; i < edges.length; i++) {
            sb.append("            new Edge(")
              .append(edges[i][0]).append(", ")
              .append(edges[i][1]).append(", ")
              .append(edges[i][2]).append(")");
            if (i < edges.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("        }");
        return sb.toString();
    }

    private String formatEdgesCpp() {
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
    
    private String generateCCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Kruskal's MST Algorithm in C\n");
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n\n");
        
        sb.append("#define V ").append(nodeCount).append("\n");
        sb.append("#define E ").append(edges.length).append("\n\n");
        
        sb.append("typedef struct {\n");
        sb.append("    int u, v, weight;\n");
        sb.append("} Edge;\n\n");
        
        sb.append("int parent[V];\n");
        sb.append("int rank_arr[V];\n\n");
        
        sb.append("void makeSet(int v) {\n");
        sb.append("    parent[v] = v;\n");
        sb.append("    rank_arr[v] = 0;\n");
        sb.append("}\n\n");
        
        sb.append("int find(int v) {\n");
        sb.append("    if (parent[v] != v)\n");
        sb.append("        parent[v] = find(parent[v]);\n");
        sb.append("    return parent[v];\n");
        sb.append("}\n\n");
        
        sb.append("void unionSets(int a, int b) {\n");
        sb.append("    a = find(a);\n");
        sb.append("    b = find(b);\n");
        sb.append("    if (a != b) {\n");
        sb.append("        if (rank_arr[a] < rank_arr[b]) {\n");
        sb.append("            int temp = a; a = b; b = temp;\n");
        sb.append("        }\n");
        sb.append("        parent[b] = a;\n");
        sb.append("        if (rank_arr[a] == rank_arr[b])\n");
        sb.append("            rank_arr[a]++;\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("int compareEdges(const void *a, const void *b) {\n");
        sb.append("    return ((Edge*)a)->weight - ((Edge*)b)->weight;\n");
        sb.append("}\n\n");
        
        sb.append("int main() {\n");
        sb.append("    Edge edges[E] = ").append(formatEdgesC()).append(";\n\n");
        
        sb.append("    // Initialize union-find\n");
        sb.append("    for (int i = 0; i < V; i++)\n");
        sb.append("        makeSet(i);\n\n");
        
        sb.append("    // Sort edges by weight\n");
        sb.append("    qsort(edges, E, sizeof(Edge), compareEdges);\n\n");
        
        sb.append("    printf(\"=== Kruskal's MST ===\\n\");\n");
        sb.append("    printf(\"Vertices: %d, Edges: %d\\n\\n\", V, E);\n\n");
        
        sb.append("    Edge mst[V-1];\n");
        sb.append("    int mstSize = 0, totalWeight = 0;\n\n");
        
        sb.append("    for (int i = 0; i < E && mstSize < V-1; i++) {\n");
        sb.append("        int u = edges[i].u, v = edges[i].v;\n");
        sb.append("        int setU = find(u), setV = find(v);\n\n");
        
        sb.append("        if (setU != setV) {\n");
        sb.append("            mst[mstSize++] = edges[i];\n");
        sb.append("            totalWeight += edges[i].weight;\n");
        sb.append("            unionSets(setU, setV);\n");
        sb.append("            printf(\"Added: %d-%d (weight: %d)\\n\", u, v, edges[i].weight);\n");
        sb.append("        } else {\n");
        sb.append("            printf(\"Rejected: %d-%d (creates cycle)\\n\", u, v);\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
        sb.append("    printf(\"\\n=== MST Complete ===\\n\");\n");
        sb.append("    printf(\"Total weight: %d\\n\", totalWeight);\n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    private String formatEdgesC() {
        if (edges.length == 0) {
            return "{{0,1,4}, {0,2,2}, {1,2,1}, {1,3,5}, {2,3,8}, {2,4,10}, {3,4,2}, {3,5,6}, {4,5,3}}";
        }
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
