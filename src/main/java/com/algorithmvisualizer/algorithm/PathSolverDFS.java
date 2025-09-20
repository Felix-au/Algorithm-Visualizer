package com.algorithmvisualizer.algorithm;

import java.util.*;

public class PathSolverDFS {
    public interface StepListener { void onStep(PathSolverBFS.StepType type, int r, int c); }

    private int rows, cols;
    private boolean[][][] walls; // [r][c][4] N,E,S,W true=wall
    private int startR=0, startC=0, goalR, goalC;

    private boolean[][] visited;
    private int[][] parentR, parentC;

    private Deque<Node> stack = new ArrayDeque<>();
    private boolean reconstructing = false;
    private List<int[]> path = new ArrayList<>();
    private int pathIdx = -1;
    private boolean done = false;

    private StepListener listener;

    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};

    private static class Node { int r, c, di; Node(int r,int c){ this.r=r; this.c=c; this.di=0; } }

    public static class State {
        public int rows, cols;
        public boolean[][][] walls;
        public int startR, startC, goalR, goalC;
        public boolean[][] visited;
        public int[][] parentR, parentC;
        public Deque<Node> stack;
        public boolean reconstructing; public List<int[]> path; public int pathIdx;
        public boolean done;
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public PathSolverDFS(int rows, int cols, boolean[][][] walls, int sR, int sC, int gR, int gC) {
        setMaze(rows, cols, walls);
        setStartGoal(sR, sC, gR, gC);
    }

    public void setMaze(int r, int c, boolean[][][] w) {
        this.rows = r; this.cols = c; this.walls = copyWalls(w);
        resetInternal(false);
    }

    public void setStartGoal(int sR, int sC, int gR, int gC) {
        this.startR = sR; this.startC = sC; this.goalR = gR; this.goalC = gC;
        resetInternal(false);
    }

    public void reset() { resetInternal(true); }

    private void resetInternal(boolean emitInit) {
        visited = new boolean[rows][cols];
        parentR = new int[rows][cols]; parentC = new int[rows][cols];
        for (int i=0;i<rows;i++) Arrays.fill(parentR[i], -1);
        for (int i=0;i<rows;i++) Arrays.fill(parentC[i], -1);
        stack.clear();
        reconstructing = false; path.clear(); pathIdx = -1; done = false;
        if (rows<=0||cols<=0) { done = true; emit(PathSolverBFS.StepType.DONE, -1, -1); return; }
        stack.push(new Node(startR, startC));
        emit(PathSolverBFS.StepType.INIT, startR, startC);
    }

    public boolean isDone() { return done; }

    public PathSolverBFS.State snapshotAsBFSStateForCompat() { return null; }

    public State snapshot() {
        State s = new State();
        s.rows = rows; s.cols = cols; s.walls = copyWalls(walls);
        s.startR=startR; s.startC=startC; s.goalR=goalR; s.goalC=goalC;
        s.visited = copyVisited(visited);
        s.parentR = copy2D(parentR); s.parentC = copy2D(parentC);
        s.stack = new ArrayDeque<>();
        for (Node n : stack) { Node m = new Node(n.r, n.c); m.di = n.di; s.stack.addLast(m); }
        s.reconstructing = reconstructing; s.path = new ArrayList<>(); for (int[] p : path) s.path.add(new int[]{p[0],p[1]}); s.pathIdx = pathIdx;
        s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        rows = s.rows; cols = s.cols; walls = copyWalls(s.walls);
        startR = s.startR; startC = s.startC; goalR = s.goalR; goalC = s.goalC;
        visited = copyVisited(s.visited);
        parentR = copy2D(s.parentR); parentC = copy2D(s.parentC);
        stack = new ArrayDeque<>(); for (Node n : s.stack) { Node m = new Node(n.r,n.c); m.di = n.di; stack.addLast(m); }
        reconstructing = s.reconstructing; path = new ArrayList<>(); for (int[] p : s.path) path.add(new int[]{p[0],p[1]}); pathIdx = s.pathIdx;
        done = s.done;
    }

    public void step() {
        if (done) return;
        if (reconstructing) {
            if (pathIdx >= 0) {
                int[] p = path.get(pathIdx--);
                emit(PathSolverBFS.StepType.RECON_PATH, p[0], p[1]);
                if (pathIdx < 0) { done = true; emit(PathSolverBFS.StepType.DONE, -1, -1); }
            } else { done = true; emit(PathSolverBFS.StepType.DONE, -1, -1); }
            return;
        }
        if (stack.isEmpty()) { done = true; emit(PathSolverBFS.StepType.NO_PATH, -1, -1); emit(PathSolverBFS.StepType.DONE, -1, -1); return; }
        Node top = stack.peek();
        int r = top.r, c = top.c;
        if (!visited[r][c]) { visited[r][c] = true; emit(PathSolverBFS.StepType.VISIT, r, c); return; }
        while (top.di < 4) {
            int dir = top.di++;
            if (walls[r][c][dir]) continue;
            int nr = r + DR[dir], nc = c + DC[dir];
            if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
            if (!visited[nr][nc] && parentR[nr][nc] == -1) {
                parentR[nr][nc] = r; parentC[nr][nc] = c;
                stack.push(new Node(nr, nc));
                if (nr == goalR && nc == goalC) { buildPath(); reconstructing = true; emit(PathSolverBFS.StepType.FOUND, nr, nc); }
                else emit(PathSolverBFS.StepType.FRONTIER, nr, nc);
                return;
            }
        }
        stack.pop();
    }

    private void buildPath() {
        path.clear(); int r = goalR, c = goalC; path.add(new int[]{r,c});
        while (!(r == startR && c == startC)) { int pr = parentR[r][c], pc = parentC[r][c]; if (pr == -1) break; r = pr; c = pc; path.add(new int[]{r,c}); }
        pathIdx = path.size() - 1;
    }

    private void emit(PathSolverBFS.StepType t, int r, int c) { if (listener != null) listener.onStep(t, r, c); }

    private static boolean[][][] copyWalls(boolean[][][] src) { int r=src.length,c=src[0].length; boolean[][][] w=new boolean[r][c][4]; for(int i=0;i<r;i++) for(int j=0;j<c;j++) System.arraycopy(src[i][j],0,w[i][j],0,4); return w; }
    private static boolean[][] copyVisited(boolean[][] src){ int r=src.length,c=src[0].length; boolean[][] v=new boolean[r][c]; for(int i=0;i<r;i++) System.arraycopy(src[i],0,v[i],0,c); return v; }
    private static int[][] copy2D(int[][] src){ int r=src.length,c=src[0].length; int[][] a=new int[r][c]; for(int i=0;i<r;i++) System.arraycopy(src[i],0,a[i],0,c); return a; }
}
