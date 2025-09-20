package com.algorithmvisualizer.algorithm;

import java.util.*;

public class PathSolverBFS {
    public enum StepType { INIT, VISIT, FRONTIER, FOUND, RECON_PATH, NO_PATH, DONE }
    public interface StepListener { void onStep(StepType type, int r, int c); }

    private int rows, cols;
    private boolean[][][] walls; // [r][c][4] N,E,S,W true=wall
    private int startR=0, startC=0, goalR, goalC;

    private boolean[][] visited;
    private Deque<int[]> queue = new ArrayDeque<>();
    private int[][] parentR, parentC;

    // per-head neighbor stepping
    private boolean hasCur = false;
    private int curR, curC, curDirIdx = 0; // 0..3

    private boolean found = false;
    private boolean reconstructing = false;
    private List<int[]> path = new ArrayList<>();
    private int pathIdx = -1;
    private boolean done = false;

    private StepListener listener;

    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};

    public static class State {
        public int rows, cols;
        public boolean[][][] walls;
        public int startR, startC, goalR, goalC;
        public boolean[][] visited;
        public Deque<int[]> queue;
        public int[][] parentR, parentC;
        public boolean hasCur; public int curR, curC, curDirIdx;
        public boolean found; public boolean reconstructing; public List<int[]> path; public int pathIdx;
        public boolean done;
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public PathSolverBFS(int rows, int cols, boolean[][][] walls, int sR, int sC, int gR, int gC) {
        setMaze(rows, cols, walls);
        setStartGoal(sR, sC, gR, gC);
    }

    public void setMaze(int r, int c, boolean[][][] w) {
        this.rows = r; this.cols = c;
        this.walls = copyWalls(w);
        resetInternal(false);
    }

    public void setStartGoal(int sR, int sC, int gR, int gC) {
        this.startR = sR; this.startC = sC; this.goalR = gR; this.goalC = gC;
        resetInternal(false);
    }

    public void reset() { resetInternal(true); }

    private void resetInternal(boolean emitInit) {
        visited = new boolean[rows][cols];
        parentR = new int[rows][cols];
        parentC = new int[rows][cols];
        for (int i=0;i<rows;i++) Arrays.fill(parentR[i], -1);
        for (int i=0;i<rows;i++) Arrays.fill(parentC[i], -1);
        queue.clear();
        hasCur = false; curDirIdx = 0; curR = curC = 0;
        found = false; reconstructing = false; path.clear(); pathIdx = -1;
        done = (rows <= 0 || cols <= 0);
        if (!done) {
            queue.add(new int[]{startR, startC});
            if (emitInit && listener != null) listener.onStep(StepType.INIT, startR, startC);
            else emit(StepType.INIT, startR, startC);
        } else emit(StepType.DONE, -1, -1);
    }

    public boolean isDone() { return done; }
    public boolean isReconstructing() { return reconstructing; }
    public boolean[][] getVisited() { return copyVisited(visited); }
    public List<int[]> getQueueSnapshot() { return new ArrayList<>(queue); }
    public int[] getCurrentHead() { return hasCur ? new int[]{curR, curC} : (queue.peek()!=null? new int[]{queue.peek()[0], queue.peek()[1]}: null); }
    public boolean[][][] getWalls() { return copyWalls(walls); }

    public void step() {
        if (done) return;
        if (reconstructing) {
            if (pathIdx >= 0) {
                int[] p = path.get(pathIdx--);
                emit(StepType.RECON_PATH, p[0], p[1]);
                if (pathIdx < 0) { done = true; emit(StepType.DONE, -1, -1); }
            } else { done = true; emit(StepType.DONE, -1, -1); }
            return;
        }
        if (queue.isEmpty()) {
            done = true; emit(StepType.NO_PATH, -1, -1); emit(StepType.DONE, -1, -1); return;
        }
        if (!hasCur) {
            int[] head = queue.peek();
            curR = head[0]; curC = head[1]; curDirIdx = 0; hasCur = true;
            if (!visited[curR][curC]) {
                visited[curR][curC] = true;
                emit(StepType.VISIT, curR, curC);
                return;
            }
        }
        // Try one neighbor per step
        while (curDirIdx < 4) {
            int dir = curDirIdx++;
            if (walls[curR][curC][dir]) continue; // wall blocks
            int nr = curR + DR[dir];
            int nc = curC + DC[dir];
            if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
            if (!visited[nr][nc] && parentR[nr][nc] == -1) {
                parentR[nr][nc] = curR; parentC[nr][nc] = curC;
                queue.add(new int[]{nr, nc});
                if (nr == goalR && nc == goalC) {
                    // build path
                    buildPath(); reconstructing = true;
                    emit(StepType.FOUND, nr, nc);
                } else {
                    emit(StepType.FRONTIER, nr, nc);
                }
                return;
            }
        }
        // finished neighbors for current head
        queue.poll();
        hasCur = false;
    }

    private void buildPath() {
        path.clear();
        int r = goalR, c = goalC;
        path.add(new int[]{r, c});
        while (!(r == startR && c == startC)) {
            int pr = parentR[r][c];
            int pc = parentC[r][c];
            if (pr == -1) break; // no path safety
            r = pr; c = pc;
            path.add(new int[]{r, c});
        }
        pathIdx = path.size() - 1; // start from start -> goal in RECON_PATH steps
    }

    public State snapshot() {
        State s = new State();
        s.rows = rows; s.cols = cols; s.walls = copyWalls(walls);
        s.startR = startR; s.startC = startC; s.goalR = goalR; s.goalC = goalC;
        s.visited = copyVisited(visited);
        s.queue = new ArrayDeque<>(); for (int[] p : queue) s.queue.addLast(new int[]{p[0], p[1]});
        s.parentR = copy2D(parentR); s.parentC = copy2D(parentC);
        s.hasCur = hasCur; s.curR = curR; s.curC = curC; s.curDirIdx = curDirIdx;
        s.found = found; s.reconstructing = reconstructing; s.path = new ArrayList<>(); for (int[] p : path) s.path.add(new int[]{p[0],p[1]}); s.pathIdx = pathIdx;
        s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        rows = s.rows; cols = s.cols; walls = copyWalls(s.walls);
        startR = s.startR; startC = s.startC; goalR = s.goalR; goalC = s.goalC;
        visited = copyVisited(s.visited);
        queue = new ArrayDeque<>(); for (int[] p : s.queue) queue.addLast(new int[]{p[0],p[1]});
        parentR = copy2D(s.parentR); parentC = copy2D(s.parentC);
        hasCur = s.hasCur; curR = s.curR; curC = s.curC; curDirIdx = s.curDirIdx;
        found = s.found; reconstructing = s.reconstructing; path = new ArrayList<>(); for (int[] p : s.path) path.add(new int[]{p[0],p[1]}); pathIdx = s.pathIdx;
        done = s.done;
    }

    private void emit(StepType type, int r, int c) { if (listener != null) listener.onStep(type, r, c); }

    private static boolean[][][] copyWalls(boolean[][][] src) { int r=src.length, c=src[0].length; boolean[][][] w=new boolean[r][c][4]; for(int i=0;i<r;i++) for(int j=0;j<c;j++) System.arraycopy(src[i][j],0,w[i][j],0,4); return w; }
    private static boolean[][] copyVisited(boolean[][] src){ int r=src.length,c=src[0].length; boolean[][] v=new boolean[r][c]; for(int i=0;i<r;i++) System.arraycopy(src[i],0,v[i],0,c); return v; }
    private static int[][] copy2D(int[][] src){ int r=src.length,c=src[0].length; int[][] a=new int[r][c]; for(int i=0;i<r;i++) System.arraycopy(src[i],0,a[i],0,c); return a; }
}
