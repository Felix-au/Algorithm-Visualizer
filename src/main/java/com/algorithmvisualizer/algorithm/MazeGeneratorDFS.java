package com.algorithmvisualizer.algorithm;

import java.util.*;

public class MazeGeneratorDFS {
    public enum StepType { INIT, CARVE, BACKTRACK, DONE }
    public interface StepListener { void onStep(StepType type, int r, int c, int nr, int nc); }

    private int rows, cols;
    private boolean[][][] walls; // [r][c][4] => N,E,S,W
    private boolean[][] visited;
    private Deque<int[]> stack = new ArrayDeque<>();
    private Random rnd = new Random();
    private boolean done = false;

    private StepListener listener;

    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};

    public static class State {
        public int rows, cols;
        public boolean[][][] walls;
        public boolean[][] visited;
        public Deque<int[]> stack;
        public boolean done;
    }

    public MazeGeneratorDFS(int rows, int cols) {
        setSize(rows, cols);
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void setSize(int r, int c) {
        this.rows = Math.max(1, r);
        this.cols = Math.max(1, c);
        this.walls = new boolean[rows][cols][4];
        for (int i=0;i<rows;i++) for (int j=0;j<cols;j++) Arrays.fill(this.walls[i][j], true);
        this.visited = new boolean[rows][cols];
        this.stack.clear();
        this.done = false;
        emit(StepType.INIT, 0, 0, -1, -1);
    }

    public void reset() { setSize(rows, cols); }

    public boolean isDone() { return done; }

    public boolean[][][] getWalls() { return copyWalls(walls); }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public void step() {
        if (done) return;
        if (stack.isEmpty()) {
            // start
            stack.push(new int[]{0,0});
            visited[0][0] = true;
            emit(StepType.INIT, 0, 0, -1, -1);
            return;
        }
        int[] cur = stack.peek();
        int r = cur[0], c = cur[1];
        List<Integer> dirs = new ArrayList<>(Arrays.asList(0,1,2,3));
        Collections.shuffle(dirs, rnd);
        for (int dir : dirs) {
            int nr = r + DR[dir];
            int nc = c + DC[dir];
            if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
            if (!visited[nr][nc]) {
                // carve wall between (r,c) and (nr,nc)
                removeWall(r, c, dir);
                removeWall(nr, nc, (dir + 2) % 4);
                visited[nr][nc] = true;
                stack.push(new int[]{nr,nc});
                emit(StepType.CARVE, r, c, nr, nc);
                return;
            }
        }
        // no neighbors -> backtrack
        stack.pop();
        emit(StepType.BACKTRACK, r, c, -1, -1);
        if (stack.isEmpty()) {
            done = true;
            emit(StepType.DONE, -1, -1, -1, -1);
        }
    }

    private void removeWall(int r, int c, int dir) { walls[r][c][dir] = false; }

    private void emit(StepType t, int r, int c, int nr, int nc) { if (listener != null) listener.onStep(t, r, c, nr, nc); }

    public State snapshot() {
        State s = new State();
        s.rows = rows; s.cols = cols; s.walls = copyWalls(walls);
        s.visited = new boolean[rows][cols];
        for (int i=0;i<rows;i++) System.arraycopy(visited[i], 0, s.visited[i], 0, cols);
        s.stack = new ArrayDeque<>();
        for (int[] p : stack) s.stack.addLast(new int[]{p[0],p[1]});
        s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        rows = s.rows; cols = s.cols; walls = copyWalls(s.walls);
        visited = new boolean[rows][cols];
        for (int i=0;i<rows;i++) System.arraycopy(s.visited[i], 0, visited[i], 0, cols);
        stack = new ArrayDeque<>();
        for (int[] p : s.stack) stack.addLast(new int[]{p[0],p[1]});
        done = s.done;
    }

    private static boolean[][][] copyWalls(boolean[][][] src) {
        int r = src.length, c = src[0].length;
        boolean[][][] w = new boolean[r][c][4];
        for (int i=0;i<r;i++) for (int j=0;j<c;j++) System.arraycopy(src[i][j], 0, w[i][j], 0, 4);
        return w;
    }
}
