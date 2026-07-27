package com.algorithmvisualizer.code.implementations.maze;

/**
 * Python code generation for Maze Generation and Pathfinding
 */
public class MazeCodePython {
    
    public static String generateCode(int rows, int cols, int startR, int startC, int goalR, int goalC, 
                                     String genAlgo, String pathAlgo) {
        StringBuilder sb = new StringBuilder();
        
        // Imports and constants
        sb.append("import random\n");
        sb.append("from collections import deque\n\n");
        sb.append("ROWS = ").append(rows).append("\n");
        sb.append("COLS = ").append(cols).append("\n");
        sb.append("START_R = ").append(startR).append("\n");
        sb.append("START_C = ").append(startC).append("\n");
        sb.append("GOAL_R = ").append(goalR).append("\n");
        sb.append("GOAL_C = ").append(goalC).append("\n\n");
        
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
        
        // Main execution
        sb.append("walls = generate_").append(genAlgo.toLowerCase()).append("()\n");
        sb.append("path = ").append(pathAlgo.toLowerCase()).append("(walls)\n");
        sb.append("if path:\n");
        sb.append("    print(f\"Path found! Length: {len(path)}\")\n");
        sb.append("    for cell in path:\n");
        sb.append("        print(f\"({cell[0]}, {cell[1]})\")\n");
        sb.append("else:\n");
        sb.append("    print(\"No path found.\")\n");
        
        return sb.toString();
    }
    
    private static String getGenDFS() {
        return "def generate_dfs():\n" +
               "    walls = [[[True] * 4 for _ in range(COLS)] for _ in range(ROWS)]\n" +
               "    visited = [[False] * COLS for _ in range(ROWS)]\n" +
               "    stack = [(0, 0)]\n" +
               "    visited[0][0] = True\n" +
               "    dr, dc = [-1, 0, 1, 0], [0, 1, 0, -1]\n" +
               "    while stack:\n" +
               "        r, c = stack[-1]\n" +
               "        dirs = [0, 1, 2, 3]\n" +
               "        random.shuffle(dirs)\n" +
               "        moved = False\n" +
               "        for d in dirs:\n" +
               "            nr, nc = r + dr[d], c + dc[d]\n" +
               "            if nr < 0 or nc < 0 or nr >= ROWS or nc >= COLS or visited[nr][nc]:\n" +
               "                continue\n" +
               "            walls[r][c][d] = False\n" +
               "            walls[nr][nc][(d + 2) % 4] = False\n" +
               "            visited[nr][nc] = True\n" +
               "            stack.append((nr, nc))\n" +
               "            moved = True\n" +
               "            break\n" +
               "        if not moved:\n" +
               "            stack.pop()\n" +
               "    return walls\n\n";
    }
    
    private static String getGenPrim() {
        return "def generate_prim():\n" +
               "    walls = [[[True] * 4 for _ in range(COLS)] for _ in range(ROWS)]\n" +
               "    in_maze = [[False] * COLS for _ in range(ROWS)]\n" +
               "    frontier = []\n" +
               "    dr, dc = [-1, 0, 1, 0], [0, 1, 0, -1]\n" +
               "    in_maze[0][0] = True\n" +
               "    for d in range(4):\n" +
               "        nr, nc = dr[d], dc[d]\n" +
               "        if 0 <= nr < ROWS and 0 <= nc < COLS:\n" +
               "            frontier.append((0, 0, d))\n" +
               "    while frontier:\n" +
               "        idx = random.randint(0, len(frontier) - 1)\n" +
               "        r, c, d = frontier.pop(idx)\n" +
               "        nr, nc = r + dr[d], c + dc[d]\n" +
               "        if nr < 0 or nc < 0 or nr >= ROWS or nc >= COLS:\n" +
               "            continue\n" +
               "        if not in_maze[nr][nc]:\n" +
               "            walls[r][c][d] = False\n" +
               "            walls[nr][nc][(d + 2) % 4] = False\n" +
               "            in_maze[nr][nc] = True\n" +
               "            for dd in range(4):\n" +
               "                nr2, nc2 = nr + dr[dd], nc + dc[dd]\n" +
               "                if 0 <= nr2 < ROWS and 0 <= nc2 < COLS and not in_maze[nr2][nc2]:\n" +
               "                    frontier.append((nr, nc, dd))\n" +
               "    return walls\n\n";
    }
    
    private static String getGenKruskal() {
        return "def generate_kruskal():\n" +
               "    walls = [[[True] * 4 for _ in range(COLS)] for _ in range(ROWS)]\n" +
               "    N = ROWS * COLS\n" +
               "    parent = list(range(N))\n" +
               "    rank = [0] * N\n" +
               "    edges = []\n" +
               "    for r in range(ROWS):\n" +
               "        for c in range(COLS):\n" +
               "            if c + 1 < COLS:\n" +
               "                edges.append((r, c, 1))\n" +
               "            if r + 1 < ROWS:\n" +
               "                edges.append((r, c, 2))\n" +
               "    random.shuffle(edges)\n" +
               "    dr, dc = [-1, 0, 1, 0], [0, 1, 0, -1]\n" +
               "    for r, c, d in edges:\n" +
               "        nr, nc = r + dr[d], c + dc[d]\n" +
               "        a, b = r * COLS + c, nr * COLS + nc\n" +
               "        if find(parent, a) != find(parent, b):\n" +
               "            unite(parent, rank, a, b)\n" +
               "            walls[r][c][d] = False\n" +
               "            walls[nr][nc][(d + 2) % 4] = False\n" +
               "    return walls\n\n";
    }
    
    private static String getUnionFind() {
        return "def find(parent, x):\n" +
               "    if parent[x] != x:\n" +
               "        parent[x] = find(parent, parent[x])\n" +
               "    return parent[x]\n\n" +
               "def unite(parent, rank, a, b):\n" +
               "    a, b = find(parent, a), find(parent, b)\n" +
               "    if a == b:\n" +
               "        return\n" +
               "    if rank[a] < rank[b]:\n" +
               "        parent[a] = b\n" +
               "    elif rank[a] > rank[b]:\n" +
               "        parent[b] = a\n" +
               "    else:\n" +
               "        parent[b] = a\n" +
               "        rank[a] += 1\n\n";
    }
    
    private static String getPathBFS() {
        return "def bfs(walls):\n" +
               "    visited = [[False] * COLS for _ in range(ROWS)]\n" +
               "    parent_r = [[-1] * COLS for _ in range(ROWS)]\n" +
               "    parent_c = [[-1] * COLS for _ in range(ROWS)]\n" +
               "    queue = deque([(START_R, START_C)])\n" +
               "    visited[START_R][START_C] = True\n" +
               "    dr, dc = [-1, 0, 1, 0], [0, 1, 0, -1]\n" +
               "    while queue:\n" +
               "        r, c = queue.popleft()\n" +
               "        if r == GOAL_R and c == GOAL_C:\n" +
               "            break\n" +
               "        for d in range(4):\n" +
               "            if walls[r][c][d]:\n" +
               "                continue\n" +
               "            nr, nc = r + dr[d], c + dc[d]\n" +
               "            if nr < 0 or nc < 0 or nr >= ROWS or nc >= COLS:\n" +
               "                continue\n" +
               "            if not visited[nr][nc]:\n" +
               "                visited[nr][nc] = True\n" +
               "                parent_r[nr][nc] = r\n" +
               "                parent_c[nr][nc] = c\n" +
               "                queue.append((nr, nc))\n" +
               "    return build_path(parent_r, parent_c)\n\n";
    }
    
    private static String getPathDFS() {
        return "def dfs(walls):\n" +
               "    visited = [[False] * COLS for _ in range(ROWS)]\n" +
               "    parent_r = [[-1] * COLS for _ in range(ROWS)]\n" +
               "    parent_c = [[-1] * COLS for _ in range(ROWS)]\n" +
               "    stack = [(START_R, START_C, 0)]\n" +
               "    dr, dc = [-1, 0, 1, 0], [0, 1, 0, -1]\n" +
               "    while stack:\n" +
               "        r, c, d = stack[-1]\n" +
               "        if not visited[r][c]:\n" +
               "            visited[r][c] = True\n" +
               "            if r == GOAL_R and c == GOAL_C:\n" +
               "                break\n" +
               "        if d >= 4:\n" +
               "            stack.pop()\n" +
               "            continue\n" +
               "        stack[-1] = (r, c, d + 1)\n" +
               "        if walls[r][c][d]:\n" +
               "            continue\n" +
               "        nr, nc = r + dr[d], c + dc[d]\n" +
               "        if nr < 0 or nc < 0 or nr >= ROWS or nc >= COLS:\n" +
               "            continue\n" +
               "        if not visited[nr][nc] and parent_r[nr][nc] == -1:\n" +
               "            parent_r[nr][nc] = r\n" +
               "            parent_c[nr][nc] = c\n" +
               "            stack.append((nr, nc, 0))\n" +
               "    return build_path(parent_r, parent_c)\n\n";
    }
    
    private static String getBuildPath() {
        return "def build_path(parent_r, parent_c):\n" +
               "    path = []\n" +
               "    r, c = GOAL_R, GOAL_C\n" +
               "    if parent_r[r][c] == -1 and not (r == START_R and c == START_C):\n" +
               "        return None\n" +
               "    path.append((r, c))\n" +
               "    while not (r == START_R and c == START_C):\n" +
               "        pr, pc = parent_r[r][c], parent_c[r][c]\n" +
               "        if pr == -1:\n" +
               "            break\n" +
               "        r, c = pr, pc\n" +
               "        path.append((r, c))\n" +
               "    path.reverse()\n" +
               "    return path\n\n";
    }
}
