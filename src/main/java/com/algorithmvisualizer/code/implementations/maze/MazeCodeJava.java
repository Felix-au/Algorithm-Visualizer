package com.algorithmvisualizer.code.implementations.maze;

/**
 * Java code generation for Maze Generation and Pathfinding
 */
public class MazeCodeJava {
    
    public static String generateCode(int rows, int cols, int startR, int startC, int goalR, int goalC, 
                                     String genAlgo, String pathAlgo) {
        StringBuilder sb = new StringBuilder();
        
        // Imports and class header
        sb.append("import java.util.*;\n\n");
        sb.append("public class Main {\n");
        sb.append("    static final int ROWS = ").append(rows).append(";\n");
        sb.append("    static final int COLS = ").append(cols).append(";\n");
        sb.append("    static final int START_R = ").append(startR).append(";\n");
        sb.append("    static final int START_C = ").append(startC).append(";\n");
        sb.append("    static final int GOAL_R = ").append(goalR).append(";\n");
        sb.append("    static final int GOAL_C = ").append(goalC).append(";\n\n");
        
        // Main method
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        boolean[][][] walls = generate").append(genAlgo).append("(ROWS, COLS);\n");
        sb.append("        List<int[]> path = ").append(pathAlgo.toLowerCase()).append("(walls, START_R, START_C, GOAL_R, GOAL_C);\n");
        sb.append("        if (path != null) {\n");
        sb.append("            System.out.println(\"Path found! Length: \" + path.size());\n");
        sb.append("            for (int[] cell : path) {\n");
        sb.append("                System.out.println(\"(\" + cell[0] + \", \" + cell[1] + \")\");\n");
        sb.append("            }\n");
        sb.append("        } else {\n");
        sb.append("            System.out.println(\"No path found.\");\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
        
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
        
        // Helper method (always included)
        sb.append(getBuildPath());
        
        sb.append("}\n");
        return sb.toString();
    }
    
    private static String getGenDFS() {
        return "    static boolean[][][] generateDFS(int R, int C) {\n" +
               "        boolean[][][] walls = new boolean[R][C][4];\n" +
               "        for (int i = 0; i < R; i++) {\n" +
               "            for (int j = 0; j < C; j++) {\n" +
               "                Arrays.fill(walls[i][j], true);\n" +
               "            }\n" +
               "        }\n" +
               "        boolean[][] visited = new boolean[R][C];\n" +
               "        Deque<int[]> stack = new ArrayDeque<>();\n" +
               "        stack.push(new int[]{0, 0});\n" +
               "        visited[0][0] = true;\n" +
               "        Random rnd = new Random();\n" +
               "        int[] dr = {-1, 0, 1, 0}, dc = {0, 1, 0, -1};\n" +
               "        while (!stack.isEmpty()) {\n" +
               "            int[] cur = stack.peek();\n" +
               "            int r = cur[0], c = cur[1];\n" +
               "            List<Integer> dirs = Arrays.asList(0, 1, 2, 3);\n" +
               "            Collections.shuffle(dirs, rnd);\n" +
               "            boolean moved = false;\n" +
               "            for (int d : dirs) {\n" +
               "                int nr = r + dr[d], nc = c + dc[d];\n" +
               "                if (nr < 0 || nc < 0 || nr >= R || nc >= C || visited[nr][nc]) continue;\n" +
               "                walls[r][c][d] = false;\n" +
               "                walls[nr][nc][(d + 2) % 4] = false;\n" +
               "                visited[nr][nc] = true;\n" +
               "                stack.push(new int[]{nr, nc});\n" +
               "                moved = true;\n" +
               "                break;\n" +
               "            }\n" +
               "            if (!moved) stack.pop();\n" +
               "        }\n" +
               "        return walls;\n" +
               "    }\n\n";
    }
    
    private static String getGenPrim() {
        return "    static boolean[][][] generatePrim(int R, int C) {\n" +
               "        boolean[][][] walls = new boolean[R][C][4];\n" +
               "        for (int i = 0; i < R; i++) {\n" +
               "            for (int j = 0; j < C; j++) {\n" +
               "                Arrays.fill(walls[i][j], true);\n" +
               "            }\n" +
               "        }\n" +
               "        boolean[][] inMaze = new boolean[R][C];\n" +
               "        List<int[]> frontier = new ArrayList<>();\n" +
               "        int[] dr = {-1, 0, 1, 0}, dc = {0, 1, 0, -1};\n" +
               "        Random rnd = new Random();\n" +
               "        inMaze[0][0] = true;\n" +
               "        for (int d = 0; d < 4; d++) {\n" +
               "            int nr = 0 + dr[d], nc = 0 + dc[d];\n" +
               "            if (nr >= 0 && nc >= 0 && nr < R && nc < C) {\n" +
               "                frontier.add(new int[]{0, 0, d});\n" +
               "            }\n" +
               "        }\n" +
               "        while (!frontier.isEmpty()) {\n" +
               "            int idx = rnd.nextInt(frontier.size());\n" +
               "            int[] edge = frontier.remove(idx);\n" +
               "            int r = edge[0], c = edge[1], d = edge[2];\n" +
               "            int nr = r + dr[d], nc = c + dc[d];\n" +
               "            if (nr < 0 || nc < 0 || nr >= R || nc >= C) continue;\n" +
               "            if (!inMaze[nr][nc]) {\n" +
               "                walls[r][c][d] = false;\n" +
               "                walls[nr][nc][(d + 2) % 4] = false;\n" +
               "                inMaze[nr][nc] = true;\n" +
               "                for (int dd = 0; dd < 4; dd++) {\n" +
               "                    int nr2 = nr + dr[dd], nc2 = nc + dc[dd];\n" +
               "                    if (nr2 >= 0 && nc2 >= 0 && nr2 < R && nc2 < C && !inMaze[nr2][nc2]) {\n" +
               "                        frontier.add(new int[]{nr, nc, dd});\n" +
               "                    }\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "        return walls;\n" +
               "    }\n\n";
    }
    
    private static String getGenKruskal() {
        return "    static boolean[][][] generateKruskal(int R, int C) {\n" +
               "        boolean[][][] walls = new boolean[R][C][4];\n" +
               "        for (int i = 0; i < R; i++) {\n" +
               "            for (int j = 0; j < C; j++) {\n" +
               "                Arrays.fill(walls[i][j], true);\n" +
               "            }\n" +
               "        }\n" +
               "        int N = R * C;\n" +
               "        int[] parent = new int[N], rank = new int[N];\n" +
               "        for (int i = 0; i < N; i++) {\n" +
               "            parent[i] = i;\n" +
               "            rank[i] = 0;\n" +
               "        }\n" +
               "        List<int[]> edges = new ArrayList<>();\n" +
               "        for (int r = 0; r < R; r++) {\n" +
               "            for (int c = 0; c < C; c++) {\n" +
               "                if (c + 1 < C) edges.add(new int[]{r, c, 1});\n" +
               "                if (r + 1 < R) edges.add(new int[]{r, c, 2});\n" +
               "            }\n" +
               "        }\n" +
               "        Collections.shuffle(edges, new Random());\n" +
               "        int[] dr = {-1, 0, 1, 0}, dc = {0, 1, 0, -1};\n" +
               "        for (int[] edge : edges) {\n" +
               "            int r = edge[0], c = edge[1], d = edge[2];\n" +
               "            int nr = r + dr[d], nc = c + dc[d];\n" +
               "            int a = r * C + c, b = nr * C + nc;\n" +
               "            if (find(parent, a) != find(parent, b)) {\n" +
               "                unite(parent, rank, a, b);\n" +
               "                walls[r][c][d] = false;\n" +
               "                walls[nr][nc][(d + 2) % 4] = false;\n" +
               "            }\n" +
               "        }\n" +
               "        return walls;\n" +
               "    }\n\n";
    }
    
    private static String getUnionFind() {
        return "    static int find(int[] parent, int x) {\n" +
               "        if (parent[x] != x) {\n" +
               "            parent[x] = find(parent, parent[x]);\n" +
               "        }\n" +
               "        return parent[x];\n" +
               "    }\n\n" +
               "    static void unite(int[] parent, int[] rank, int a, int b) {\n" +
               "        a = find(parent, a);\n" +
               "        b = find(parent, b);\n" +
               "        if (a == b) return;\n" +
               "        if (rank[a] < rank[b]) {\n" +
               "            parent[a] = b;\n" +
               "        } else if (rank[a] > rank[b]) {\n" +
               "            parent[b] = a;\n" +
               "        } else {\n" +
               "            parent[b] = a;\n" +
               "            rank[a]++;\n" +
               "        }\n" +
               "    }\n\n";
    }
    
    private static String getPathBFS() {
        return "    static List<int[]> bfs(boolean[][][] walls, int sr, int sc, int gr, int gc) {\n" +
               "        int R = walls.length, C = walls[0].length;\n" +
               "        boolean[][] visited = new boolean[R][C];\n" +
               "        int[][] parentR = new int[R][C], parentC = new int[R][C];\n" +
               "        for (int i = 0; i < R; i++) {\n" +
               "            Arrays.fill(parentR[i], -1);\n" +
               "            Arrays.fill(parentC[i], -1);\n" +
               "        }\n" +
               "        Deque<int[]> queue = new ArrayDeque<>();\n" +
               "        queue.add(new int[]{sr, sc});\n" +
               "        visited[sr][sc] = true;\n" +
               "        int[] dr = {-1, 0, 1, 0}, dc = {0, 1, 0, -1};\n" +
               "        while (!queue.isEmpty()) {\n" +
               "            int[] curr = queue.poll();\n" +
               "            int r = curr[0], c = curr[1];\n" +
               "            if (r == gr && c == gc) break;\n" +
               "            for (int d = 0; d < 4; d++) {\n" +
               "                if (walls[r][c][d]) continue;\n" +
               "                int nr = r + dr[d], nc = c + dc[d];\n" +
               "                if (nr < 0 || nc < 0 || nr >= R || nc >= C) continue;\n" +
               "                if (!visited[nr][nc]) {\n" +
               "                    visited[nr][nc] = true;\n" +
               "                    parentR[nr][nc] = r;\n" +
               "                    parentC[nr][nc] = c;\n" +
               "                    queue.add(new int[]{nr, nc});\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "        return buildPath(parentR, parentC, sr, sc, gr, gc);\n" +
               "    }\n\n";
    }
    
    private static String getPathDFS() {
        return "    static List<int[]> dfs(boolean[][][] walls, int sr, int sc, int gr, int gc) {\n" +
               "        int R = walls.length, C = walls[0].length;\n" +
               "        boolean[][] visited = new boolean[R][C];\n" +
               "        int[][] parentR = new int[R][C], parentC = new int[R][C];\n" +
               "        for (int i = 0; i < R; i++) {\n" +
               "            Arrays.fill(parentR[i], -1);\n" +
               "            Arrays.fill(parentC[i], -1);\n" +
               "        }\n" +
               "        Deque<int[]> stack = new ArrayDeque<>();\n" +
               "        stack.push(new int[]{sr, sc, 0});\n" +
               "        int[] dr = {-1, 0, 1, 0}, dc = {0, 1, 0, -1};\n" +
               "        while (!stack.isEmpty()) {\n" +
               "            int[] top = stack.peek();\n" +
               "            int r = top[0], c = top[1];\n" +
               "            if (!visited[r][c]) {\n" +
               "                visited[r][c] = true;\n" +
               "                if (r == gr && c == gc) break;\n" +
               "            }\n" +
               "            if (top[2] >= 4) {\n" +
               "                stack.pop();\n" +
               "                continue;\n" +
               "            }\n" +
               "            int d = top[2]++;\n" +
               "            if (walls[r][c][d]) continue;\n" +
               "            int nr = r + dr[d], nc = c + dc[d];\n" +
               "            if (nr < 0 || nc < 0 || nr >= R || nc >= C) continue;\n" +
               "            if (!visited[nr][nc] && parentR[nr][nc] == -1) {\n" +
               "                parentR[nr][nc] = r;\n" +
               "                parentC[nr][nc] = c;\n" +
               "                stack.push(new int[]{nr, nc, 0});\n" +
               "            }\n" +
               "        }\n" +
               "        return buildPath(parentR, parentC, sr, sc, gr, gc);\n" +
               "    }\n\n";
    }
    
    private static String getBuildPath() {
        return "    static List<int[]> buildPath(int[][] parentR, int[][] parentC, int sr, int sc, int gr, int gc) {\n" +
               "        List<int[]> path = new ArrayList<>();\n" +
               "        int r = gr, c = gc;\n" +
               "        if (parentR[r][c] == -1 && !(r == sr && c == sc)) return null;\n" +
               "        path.add(new int[]{r, c});\n" +
               "        while (!(r == sr && c == sc)) {\n" +
               "            int pr = parentR[r][c], pc = parentC[r][c];\n" +
               "            if (pr == -1) break;\n" +
               "            r = pr;\n" +
               "            c = pc;\n" +
               "            path.add(new int[]{r, c});\n" +
               "        }\n" +
               "        Collections.reverse(path);\n" +
               "        return path;\n" +
               "    }\n\n";
    }
}
