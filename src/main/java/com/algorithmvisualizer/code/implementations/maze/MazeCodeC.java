package com.algorithmvisualizer.code.implementations.maze;

/**
 * C code generation for Maze Generation and Pathfinding
 */
public class MazeCodeC {
    
    public static String generateCode(int rows, int cols, int startR, int startC, int goalR, int goalC, 
                                     String genAlgo, String pathAlgo) {
        StringBuilder sb = new StringBuilder();
        
        // Headers and constants
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <stdbool.h>\n");
        sb.append("#include <time.h>\n\n");
        sb.append("#define ROWS ").append(rows).append("\n");
        sb.append("#define COLS ").append(cols).append("\n");
        sb.append("#define START_R ").append(startR).append("\n");
        sb.append("#define START_C ").append(startC).append("\n");
        sb.append("#define GOAL_R ").append(goalR).append("\n");
        sb.append("#define GOAL_C ").append(goalC).append("\n\n");
        
        // Type definitions
        sb.append("typedef struct { int r, c; } Cell;\n");
        sb.append("typedef struct { Cell* data; int size, capacity; } CellList;\n\n");
        
        // Helper functions
        sb.append(getHelpers());
        
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
        
        // Main function
        sb.append("int main() {\n");
        sb.append("    srand(time(NULL));\n");
        sb.append("    bool walls[ROWS][COLS][4];\n");
        sb.append("    generate").append(genAlgo).append("(walls);\n");
        sb.append("    CellList* path = ").append(pathAlgo.toLowerCase()).append("(walls);\n");
        sb.append("    if (path) {\n");
        sb.append("        printf(\"Path found! Length: %d\\n\", path->size);\n");
        sb.append("        for (int i = 0; i < path->size; i++) {\n");
        sb.append("            printf(\"(%d, %d)\\n\", path->data[i].r, path->data[i].c);\n");
        sb.append("        }\n");
        sb.append("        free(path->data);\n");
        sb.append("        free(path);\n");
        sb.append("    } else {\n");
        sb.append("        printf(\"No path found.\\n\");\n");
        sb.append("    }\n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    private static String getHelpers() {
        return "void initList(CellList* list) {\n" +
               "    list->capacity = 100;\n" +
               "    list->size = 0;\n" +
               "    list->data = malloc(list->capacity * sizeof(Cell));\n" +
               "}\n\n" +
               "void addCell(CellList* list, int r, int c) {\n" +
               "    if (list->size >= list->capacity) {\n" +
               "        list->capacity *= 2;\n" +
               "        list->data = realloc(list->data, list->capacity * sizeof(Cell));\n" +
               "    }\n" +
               "    list->data[list->size].r = r;\n" +
               "    list->data[list->size].c = c;\n" +
               "    list->size++;\n" +
               "}\n\n";
    }
    
    private static String getGenDFS() {
        return "void generateDFS(bool walls[ROWS][COLS][4]) {\n" +
               "    for (int i = 0; i < ROWS; i++)\n" +
               "        for (int j = 0; j < COLS; j++)\n" +
               "            for (int k = 0; k < 4; k++) walls[i][j][k] = true;\n" +
               "    bool visited[ROWS][COLS] = {false};\n" +
               "    Cell stack[ROWS * COLS];\n" +
               "    int top = 0;\n" +
               "    stack[top++] = (Cell){0, 0};\n" +
               "    visited[0][0] = true;\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    while (top > 0) {\n" +
               "        Cell cur = stack[top - 1];\n" +
               "        int dirs[4] = {0, 1, 2, 3};\n" +
               "        for (int i = 3; i > 0; i--) {\n" +
               "            int j = rand() % (i + 1);\n" +
               "            int temp = dirs[i]; dirs[i] = dirs[j]; dirs[j] = temp;\n" +
               "        }\n" +
               "        bool moved = false;\n" +
               "        for (int i = 0; i < 4; i++) {\n" +
               "            int d = dirs[i];\n" +
               "            int nr = cur.r + dr[d], nc = cur.c + dc[d];\n" +
               "            if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS || visited[nr][nc]) continue;\n" +
               "            walls[cur.r][cur.c][d] = false;\n" +
               "            walls[nr][nc][(d + 2) % 4] = false;\n" +
               "            visited[nr][nc] = true;\n" +
               "            stack[top++] = (Cell){nr, nc};\n" +
               "            moved = true;\n" +
               "            break;\n" +
               "        }\n" +
               "        if (!moved) top--;\n" +
               "    }\n" +
               "}\n\n";
    }
    
    private static String getGenPrim() {
        return "void generatePrim(bool walls[ROWS][COLS][4]) {\n" +
               "    for (int i = 0; i < ROWS; i++)\n" +
               "        for (int j = 0; j < COLS; j++)\n" +
               "            for (int k = 0; k < 4; k++) walls[i][j][k] = true;\n" +
               "    bool inMaze[ROWS][COLS] = {false};\n" +
               "    CellList frontier; initList(&frontier);\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    inMaze[0][0] = true;\n" +
               "    for (int d = 0; d < 4; d++) {\n" +
               "        int nr = dr[d], nc = dc[d];\n" +
               "        if (nr >= 0 && nc >= 0 && nr < ROWS && nc < COLS)\n" +
               "            addCell(&frontier, 0 * 10 + d, 0);\n" +
               "    }\n" +
               "    while (frontier.size > 0) {\n" +
               "        int idx = rand() % frontier.size;\n" +
               "        Cell edge = frontier.data[idx];\n" +
               "        frontier.data[idx] = frontier.data[--frontier.size];\n" +
               "        int r = edge.r / 10, d = edge.r % 10, c = edge.c;\n" +
               "        int nr = r + dr[d], nc = c + dc[d];\n" +
               "        if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS) continue;\n" +
               "        if (!inMaze[nr][nc]) {\n" +
               "            walls[r][c][d] = false;\n" +
               "            walls[nr][nc][(d + 2) % 4] = false;\n" +
               "            inMaze[nr][nc] = true;\n" +
               "            for (int dd = 0; dd < 4; dd++) {\n" +
               "                int nr2 = nr + dr[dd], nc2 = nc + dc[dd];\n" +
               "                if (nr2 >= 0 && nc2 >= 0 && nr2 < ROWS && nc2 < COLS && !inMaze[nr2][nc2])\n" +
               "                    addCell(&frontier, nr * 10 + dd, nc);\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    free(frontier.data);\n" +
               "}\n\n";
    }
    
    private static String getGenKruskal() {
        return "void generateKruskal(bool walls[ROWS][COLS][4]) {\n" +
               "    for (int i = 0; i < ROWS; i++)\n" +
               "        for (int j = 0; j < COLS; j++)\n" +
               "            for (int k = 0; k < 4; k++) walls[i][j][k] = true;\n" +
               "    int N = ROWS * COLS;\n" +
               "    int parent[N], rank[N];\n" +
               "    for (int i = 0; i < N; i++) { parent[i] = i; rank[i] = 0; }\n" +
               "    CellList edges; initList(&edges);\n" +
               "    for (int r = 0; r < ROWS; r++) {\n" +
               "        for (int c = 0; c < COLS; c++) {\n" +
               "            if (c + 1 < COLS) addCell(&edges, r * 10 + 1, c);\n" +
               "            if (r + 1 < ROWS) addCell(&edges, r * 10 + 2, c);\n" +
               "        }\n" +
               "    }\n" +
               "    for (int i = edges.size - 1; i > 0; i--) {\n" +
               "        int j = rand() % (i + 1);\n" +
               "        Cell temp = edges.data[i]; edges.data[i] = edges.data[j]; edges.data[j] = temp;\n" +
               "    }\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    for (int i = 0; i < edges.size; i++) {\n" +
               "        int r = edges.data[i].r / 10, d = edges.data[i].r % 10, c = edges.data[i].c;\n" +
               "        int nr = r + dr[d], nc = c + dc[d];\n" +
               "        int a = r * COLS + c, b = nr * COLS + nc;\n" +
               "        if (find(parent, a) != find(parent, b)) {\n" +
               "            unite(parent, rank, a, b);\n" +
               "            walls[r][c][d] = false;\n" +
               "            walls[nr][nc][(d + 2) % 4] = false;\n" +
               "        }\n" +
               "    }\n" +
               "    free(edges.data);\n" +
               "}\n\n";
    }
    
    private static String getUnionFind() {
        return "int find(int parent[], int x) {\n" +
               "    if (parent[x] != x) parent[x] = find(parent, parent[x]);\n" +
               "    return parent[x];\n" +
               "}\n\n" +
               "void unite(int parent[], int rank[], int a, int b) {\n" +
               "    a = find(parent, a); b = find(parent, b);\n" +
               "    if (a == b) return;\n" +
               "    if (rank[a] < rank[b]) parent[a] = b;\n" +
               "    else if (rank[a] > rank[b]) parent[b] = a;\n" +
               "    else { parent[b] = a; rank[a]++; }\n" +
               "}\n\n";
    }
    
    private static String getPathBFS() {
        return "CellList* bfs(bool walls[ROWS][COLS][4]) {\n" +
               "    bool visited[ROWS][COLS] = {false};\n" +
               "    int parentR[ROWS][COLS], parentC[ROWS][COLS];\n" +
               "    for (int i = 0; i < ROWS; i++) {\n" +
               "        for (int j = 0; j < COLS; j++) {\n" +
               "            parentR[i][j] = -1; parentC[i][j] = -1;\n" +
               "        }\n" +
               "    }\n" +
               "    Cell queue[ROWS * COLS];\n" +
               "    int front = 0, rear = 0;\n" +
               "    queue[rear++] = (Cell){START_R, START_C};\n" +
               "    visited[START_R][START_C] = true;\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    while (front < rear) {\n" +
               "        Cell cur = queue[front++];\n" +
               "        if (cur.r == GOAL_R && cur.c == GOAL_C) break;\n" +
               "        for (int d = 0; d < 4; d++) {\n" +
               "            if (walls[cur.r][cur.c][d]) continue;\n" +
               "            int nr = cur.r + dr[d], nc = cur.c + dc[d];\n" +
               "            if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS) continue;\n" +
               "            if (!visited[nr][nc]) {\n" +
               "                visited[nr][nc] = true;\n" +
               "                parentR[nr][nc] = cur.r; parentC[nr][nc] = cur.c;\n" +
               "                queue[rear++] = (Cell){nr, nc};\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    if (parentR[GOAL_R][GOAL_C] == -1 && !(GOAL_R == START_R && GOAL_C == START_C)) return NULL;\n" +
               "    CellList* path = malloc(sizeof(CellList)); initList(path);\n" +
               "    int r = GOAL_R, c = GOAL_C;\n" +
               "    while (!(r == START_R && c == START_C)) {\n" +
               "        addCell(path, r, c);\n" +
               "        int pr = parentR[r][c], pc = parentC[r][c];\n" +
               "        if (pr == -1) break;\n" +
               "        r = pr; c = pc;\n" +
               "    }\n" +
               "    addCell(path, START_R, START_C);\n" +
               "    for (int i = 0; i < path->size / 2; i++) {\n" +
               "        Cell temp = path->data[i];\n" +
               "        path->data[i] = path->data[path->size - 1 - i];\n" +
               "        path->data[path->size - 1 - i] = temp;\n" +
               "    }\n" +
               "    return path;\n" +
               "}\n\n";
    }
    
    private static String getPathDFS() {
        return "CellList* dfs(bool walls[ROWS][COLS][4]) {\n" +
               "    bool visited[ROWS][COLS] = {false};\n" +
               "    int parentR[ROWS][COLS], parentC[ROWS][COLS];\n" +
               "    for (int i = 0; i < ROWS; i++) {\n" +
               "        for (int j = 0; j < COLS; j++) {\n" +
               "            parentR[i][j] = -1; parentC[i][j] = -1;\n" +
               "        }\n" +
               "    }\n" +
               "    typedef struct { int r, c, d; } State;\n" +
               "    State stack[ROWS * COLS * 4];\n" +
               "    int top = 0;\n" +
               "    stack[top++] = (State){START_R, START_C, 0};\n" +
               "    int dr[] = {-1, 0, 1, 0}, dc[] = {0, 1, 0, -1};\n" +
               "    while (top > 0) {\n" +
               "        State* s = &stack[top - 1];\n" +
               "        if (!visited[s->r][s->c]) {\n" +
               "            visited[s->r][s->c] = true;\n" +
               "            if (s->r == GOAL_R && s->c == GOAL_C) break;\n" +
               "        }\n" +
               "        if (s->d >= 4) { top--; continue; }\n" +
               "        int d = s->d++;\n" +
               "        if (walls[s->r][s->c][d]) continue;\n" +
               "        int nr = s->r + dr[d], nc = s->c + dc[d];\n" +
               "        if (nr < 0 || nc < 0 || nr >= ROWS || nc >= COLS) continue;\n" +
               "        if (!visited[nr][nc] && parentR[nr][nc] == -1) {\n" +
               "            parentR[nr][nc] = s->r; parentC[nr][nc] = s->c;\n" +
               "            stack[top++] = (State){nr, nc, 0};\n" +
               "        }\n" +
               "    }\n" +
               "    if (parentR[GOAL_R][GOAL_C] == -1 && !(GOAL_R == START_R && GOAL_C == START_C)) return NULL;\n" +
               "    CellList* path = malloc(sizeof(CellList)); initList(path);\n" +
               "    int r = GOAL_R, c = GOAL_C;\n" +
               "    while (!(r == START_R && c == START_C)) {\n" +
               "        addCell(path, r, c);\n" +
               "        int pr = parentR[r][c], pc = parentC[r][c];\n" +
               "        if (pr == -1) break;\n" +
               "        r = pr; c = pc;\n" +
               "    }\n" +
               "    addCell(path, START_R, START_C);\n" +
               "    for (int i = 0; i < path->size / 2; i++) {\n" +
               "        Cell temp = path->data[i];\n" +
               "        path->data[i] = path->data[path->size - 1 - i];\n" +
               "        path->data[path->size - 1 - i] = temp;\n" +
               "    }\n" +
               "    return path;\n" +
               "}\n\n";
    }
}
