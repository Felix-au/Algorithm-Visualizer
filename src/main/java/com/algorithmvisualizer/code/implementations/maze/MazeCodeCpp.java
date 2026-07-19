package com.algorithmvisualizer.code.implementations.maze;

/**
 * C++ code generation for Maze Generation and Pathfinding
 */
public class MazeCodeCpp {
    
    public static String generateCode(int rows, int cols, int startR, int startC, int goalR, int goalC, 
                                     String genAlgo, String pathAlgo) {
        StringBuilder sb = new StringBuilder();
        
        // Headers and constants
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <queue>\n");
        sb.append("#include <stack>\n");
        sb.append("#include <algorithm>\n");
        sb.append("#include <cstdlib>\n");
        sb.append("#include <ctime>\n");
        sb.append("using namespace std;\n\n");
        sb.append("const int ROWS = ").append(rows).append(";\n");
        sb.append("const int COLS = ").append(cols).append(";\n");
        sb.append("const int START_R = ").append(startR).append(";\n");
        sb.append("const int START_C = ").append(startC).append(";\n");
        sb.append("const int GOAL_R = ").append(goalR).append(";\n");
        sb.append("const int GOAL_C = ").append(goalC).append(";\n\n");
        
        // Type definitions
        sb.append("struct Cell { int r, c; };\n\n");
        
        // Generation algorithm
        if (genAlgo.equals("DFS")) {
            sb.append(getGenDFS());
        } else if (genAlgo.equals("Prim")) {
            sb.append(getGenPrim());
        } else if (genAlgo.equals("Kruskal")) {
            sb.append(getGenKruskal());
            sb.append(getUnionFind());
        }
        
        // Pathfinding algorithm
        if (pathAlgo.equals("BFS")) {
            sb.append(getPathBFS());
        } else {
            sb.append(getPathDFS());
        }
        
        // Helper
        sb.append(getBuildPath());
        
        // Main function
        sb.append("int main() {\n");
        sb.append("    srand(time(0));\n");
        sb.append("    vector<vector<vector<bool>>> walls = generate").append(genAlgo).append("();\n");
        sb.append("    vector<Cell> path = ").append(pathAlgo.toLowerCase()).append("(walls);\n");
        sb.append("    if (!path.empty()) {\n");
        sb.append("        cout << \"Path found! Length: \" << path.size() << endl;\n");
        sb.append("        for (const Cell& cell : path) {\n");
        sb.append("            cout << \"(\" << cell.r << \", \" << cell.c << \")\" << endl;\n");
        sb.append("        }\n");
        sb.append("    } else {\n");
        sb.append("        cout << \"No path found.\" << endl;\n");
        sb.append("    }\n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    private static String getGenDFS() {
        return "vector<vector<vector<bool>>> generateDFS() {\n" +
               "    vector<vector<vector<bool>>> walls(ROWS, vector<vector<bool>>(COLS, vector<bool>(4, true)));\n" +
               "    vector<vector<bool>> visited(ROWS, vector<bool>(COLS, false));\n" +
               "    stack<Cell> st;\n" +
               "    st.push({0, 0});\n" +
               "    visited[0][0] = true;\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    while (!st.empty()) {\n" +
               "        Cell cur = st.top();\n" +
               "        vector<int> dirs = {0, 1, 2, 3};\n" +
               "        random_shuffle(dirs.begin(), dirs.end());\n" +
               "        bool moved = false;\n" +
               "        for (int d : dirs) {\n" +
               "            int nr = cur.r + dr[d], nc = cur.c + dc[d];\n" +
               "            if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS || visited[nr][nc]) continue;\n" +
               "            walls[cur.r][cur.c][d] = false;\n" +
               "            walls[nr][nc][(d + 2) % 4] = false;\n" +
               "            visited[nr][nc] = true;\n" +
               "            st.push({nr, nc});\n" +
               "            moved = true;\n" +
               "            break;\n" +
               "        }\n" +
               "        if (!moved) st.pop();\n" +
               "    }\n" +
               "    return walls;\n" +
               "}\n\n";
    }
    
    private static String getGenPrim() {
        return "vector<vector<vector<bool>>> generatePrim() {\n" +
               "    vector<vector<vector<bool>>> walls(ROWS, vector<vector<bool>>(COLS, vector<bool>(4, true)));\n" +
               "    vector<vector<bool>> inMaze(ROWS, vector<bool>(COLS, false));\n" +
               "    vector<Cell> frontier;\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    inMaze[0][0] = true;\n" +
               "    for (int d = 0; d < 4; d++) {\n" +
               "        int nr = dr[d], nc = dc[d];\n" +
               "        if (nr >= 0 && nc >= 0 && nr < ROWS && nc < COLS) {\n" +
               "            frontier.push_back({d, 0}); // encode: r=direction, c=0\n" +
               "        }\n" +
               "    }\n" +
               "    while (!frontier.empty()) {\n" +
               "        int idx = rand() % frontier.size();\n" +
               "        Cell edge = frontier[idx];\n" +
               "        frontier.erase(frontier.begin() + idx);\n" +
               "        int r = 0, c = 0, d = edge.r;\n" +
               "        int nr = r + dr[d], nc = c + dc[d];\n" +
               "        if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS) continue;\n" +
               "        if (!inMaze[nr][nc]) {\n" +
               "            walls[r][c][d] = false;\n" +
               "            walls[nr][nc][(d + 2) % 4] = false;\n" +
               "            inMaze[nr][nc] = true;\n" +
               "            for (int dd = 0; dd < 4; dd++) {\n" +
               "                int nr2 = nr + dr[dd], nc2 = nc + dc[dd];\n" +
               "                if (nr2 >= 0 && nc2 >= 0 && nr2 < ROWS && nc2 < COLS && !inMaze[nr2][nc2]) {\n" +
               "                    frontier.push_back({dd, 0});\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    return walls;\n" +
               "}\n\n";
    }
    
    private static String getGenKruskal() {
        return "vector<vector<vector<bool>>> generateKruskal() {\n" +
               "    vector<vector<vector<bool>>> walls(ROWS, vector<vector<bool>>(COLS, vector<bool>(4, true)));\n" +
               "    int N = ROWS * COLS;\n" +
               "    vector<int> parent(N), rank(N, 0);\n" +
               "    for (int i = 0; i < N; i++) parent[i] = i;\n" +
               "    vector<Cell> edges;\n" +
               "    for (int r = 0; r < ROWS; r++) {\n" +
               "        for (int c = 0; c < COLS; c++) {\n" +
               "            if (c + 1 < COLS) edges.push_back({r * 10 + 1, c});\n" +
               "            if (r + 1 < ROWS) edges.push_back({r * 10 + 2, c});\n" +
               "        }\n" +
               "    }\n" +
               "    random_shuffle(edges.begin(), edges.end());\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    for (const Cell& edge : edges) {\n" +
               "        int r = edge.r / 10, d = edge.r % 10, c = edge.c;\n" +
               "        int nr = r + dr[d], nc = c + dc[d];\n" +
               "        int a = r * COLS + c, b = nr * COLS + nc;\n" +
               "        if (find(parent, a) != find(parent, b)) {\n" +
               "            unite(parent, rank, a, b);\n" +
               "            walls[r][c][d] = false;\n" +
               "            walls[nr][nc][(d + 2) % 4] = false;\n" +
               "        }\n" +
               "    }\n" +
               "    return walls;\n" +
               "}\n\n";
    }
    
    private static String getUnionFind() {
        return "int find(vector<int>& parent, int x) {\n" +
               "    if (parent[x] != x) parent[x] = find(parent, parent[x]);\n" +
               "    return parent[x];\n" +
               "}\n\n" +
               "void unite(vector<int>& parent, vector<int>& rank, int a, int b) {\n" +
               "    a = find(parent, a); b = find(parent, b);\n" +
               "    if (a == b) return;\n" +
               "    if (rank[a] < rank[b]) parent[a] = b;\n" +
               "    else if (rank[a] > rank[b]) parent[b] = a;\n" +
               "    else { parent[b] = a; rank[a]++; }\n" +
               "}\n\n";
    }
    
    private static String getPathBFS() {
        return "vector<Cell> bfs(const vector<vector<vector<bool>>>& walls) {\n" +
               "    vector<vector<bool>> visited(ROWS, vector<bool>(COLS, false));\n" +
               "    vector<vector<int>> parentR(ROWS, vector<int>(COLS, -1));\n" +
               "    vector<vector<int>> parentC(ROWS, vector<int>(COLS, -1));\n" +
               "    queue<Cell> q;\n" +
               "    q.push({START_R, START_C});\n" +
               "    visited[START_R][START_C] = true;\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    while (!q.empty()) {\n" +
               "        Cell cur = q.front(); q.pop();\n" +
               "        if (cur.r == GOAL_R && cur.c == GOAL_C) break;\n" +
               "        for (int d = 0; d < 4; d++) {\n" +
               "            if (walls[cur.r][cur.c][d]) continue;\n" +
               "            int nr = cur.r + dr[d], nc = cur.c + dc[d];\n" +
               "            if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS) continue;\n" +
               "            if (!visited[nr][nc]) {\n" +
               "                visited[nr][nc] = true;\n" +
               "                parentR[nr][nc] = cur.r;\n" +
               "                parentC[nr][nc] = cur.c;\n" +
               "                q.push({nr, nc});\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    return buildPath(parentR, parentC);\n" +
               "}\n\n";
    }
    
    private static String getPathDFS() {
        return "vector<Cell> dfs(const vector<vector<vector<bool>>>& walls) {\n" +
               "    vector<vector<bool>> visited(ROWS, vector<bool>(COLS, false));\n" +
               "    vector<vector<int>> parentR(ROWS, vector<int>(COLS, -1));\n" +
               "    vector<vector<int>> parentC(ROWS, vector<int>(COLS, -1));\n" +
               "    struct State { int r, c, d; };\n" +
               "    stack<State> st;\n" +
               "    st.push({START_R, START_C, 0});\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    while (!st.empty()) {\n" +
               "        State& s = st.top();\n" +
               "        if (!visited[s.r][s.c]) {\n" +
               "            visited[s.r][s.c] = true;\n" +
               "            if (s.r == GOAL_R && s.c == GOAL_C) break;\n" +
               "        }\n" +
               "        if (s.d >= 4) { st.pop(); continue; }\n" +
               "        int d = s.d++;\n" +
               "        if (walls[s.r][s.c][d]) continue;\n" +
               "        int nr = s.r + dr[d], nc = s.c + dc[d];\n" +
               "        if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS) continue;\n" +
               "        if (!visited[nr][nc] && parentR[nr][nc] == -1) {\n" +
               "            parentR[nr][nc] = s.r;\n" +
               "            parentC[nr][nc] = s.c;\n" +
               "            st.push({nr, nc, 0});\n" +
               "        }\n" +
               "    }\n" +
               "    return buildPath(parentR, parentC);\n" +
               "}\n\n";
    }
    
    private static String getBuildPath() {
        return "vector<Cell> buildPath(const vector<vector<int>>& parentR, const vector<vector<int>>& parentC) {\n" +
               "    vector<Cell> path;\n" +
               "    int r = GOAL_R, c = GOAL_C;\n" +
               "    if (parentR[r][c] == -1 && !(r == START_R && c == START_C)) return path;\n" +
               "    path.push_back({r, c});\n" +
               "    while (!(r == START_R && c == START_C)) {\n" +
               "        int pr = parentR[r][c], pc = parentC[r][c];\n" +
               "        if (pr == -1) break;\n" +
               "        r = pr; c = pc;\n" +
               "        path.push_back({r, c});\n" +
               "    }\n" +
               "    reverse(path.begin(), path.end());\n" +
               "    return path;\n" +
               "}\n\n";
    }
}
