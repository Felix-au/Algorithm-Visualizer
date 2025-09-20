package com.algorithmvisualizer.algorithm;

import java.util.*;

public class PathSolverAStar {
    public interface StepListener { void onStep(PathSolverBFS.StepType type, int r, int c); }

    public enum Heuristic { MANHATTAN, EUCLIDEAN }

    private int rows, cols;
    private boolean[][][] walls; // [r][c][4] N,E,S,W true=wall
    private int startR=0, startC=0, goalR, goalC;
    private Heuristic heuristic = Heuristic.MANHATTAN;

    private boolean[][] visited;
    private int[][] parentR, parentC;
    private double[][] g;

    private PriorityQueue<Node> open;
    private boolean reconstructing = false;
    private List<int[]> path = new ArrayList<>();
    private int pathIdx = -1;
    private boolean done = false;

    private StepListener listener;

    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};

    private static class Node { int r,c; double f; Node(int r,int c,double f){this.r=r;this.c=c;this.f=f;} }

    public static class State {
        public int rows, cols;
        public boolean[][][] walls;
        public int startR, startC, goalR, goalC;
        public Heuristic heuristic;
        public boolean[][] visited;
        public int[][] parentR, parentC;
        public double[][] g;
        public PriorityQueue<Node> open;
        public boolean reconstructing; public List<int[]> path; public int pathIdx;
        public boolean done;
    }

    public PathSolverAStar(int rows, int cols, boolean[][][] walls, int sR, int sC, int gR, int gC, Heuristic h) {
        this.heuristic = h;
        setMaze(rows, cols, walls);
        setStartGoal(sR, sC, gR, gC);
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void setMaze(int r, int c, boolean[][][] w) { this.rows=r; this.cols=c; this.walls=copyWalls(w); resetInternal(false); }
    public void setStartGoal(int sR, int sC, int gR, int gC) { this.startR=sR; this.startC=sC; this.goalR=gR; this.goalC=gC; resetInternal(false); }
    public void setHeuristic(Heuristic h) { this.heuristic = h; resetInternal(true); }

    public void reset() { resetInternal(true); }

    private void resetInternal(boolean emitInit) {
        visited = new boolean[rows][cols];
        parentR = new int[rows][cols]; parentC = new int[rows][cols];
        g = new double[rows][cols];
        for (int i=0;i<rows;i++) { Arrays.fill(parentR[i], -1); Arrays.fill(parentC[i], -1); Arrays.fill(g[i], Double.POSITIVE_INFINITY); }
        open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        reconstructing = false; path.clear(); pathIdx = -1; done = false;
        if (rows<=0||cols<=0) { done = true; emit(PathSolverBFS.StepType.DONE, -1, -1); return; }
        g[startR][startC] = 0.0;
        open.add(new Node(startR, startC, h(startR, startC)));
        emit(PathSolverBFS.StepType.INIT, startR, startC);
    }

    public boolean isDone() { return done; }

    public State snapshot() {
        State s = new State();
        s.rows=rows; s.cols=cols; s.walls=copyWalls(walls);
        s.startR=startR; s.startC=startC; s.goalR=goalR; s.goalC=goalC;
        s.heuristic=heuristic;
        s.visited=copyVisited(visited); s.parentR=copy2D(parentR); s.parentC=copy2D(parentC);
        s.g=copy2D(g);
        s.open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f)); s.open.addAll(open);
        s.reconstructing=reconstructing; s.path=new ArrayList<>(); for (int[] p:path) s.path.add(new int[]{p[0],p[1]}); s.pathIdx=pathIdx;
        s.done=done; return s;
    }

    public void restore(State s) {
        if (s==null) return;
        rows=s.rows; cols=s.cols; walls=copyWalls(s.walls);
        startR=s.startR; startC=s.startC; goalR=s.goalR; goalC=s.goalC;
        heuristic=s.heuristic;
        visited=copyVisited(s.visited); parentR=copy2D(s.parentR); parentC=copy2D(s.parentC);
        g=copy2D(s.g);
        open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f)); if (s.open!=null) open.addAll(s.open);
        reconstructing=s.reconstructing; path=new ArrayList<>(); for (int[] p:s.path) path.add(new int[]{p[0],p[1]}); pathIdx=s.pathIdx;
        done=s.done;
    }

    public void step() {
        if (done) return;
        if (reconstructing) {
            if (pathIdx >= 0) { int[] p = path.get(pathIdx--); emit(PathSolverBFS.StepType.RECON_PATH, p[0], p[1]); if (pathIdx < 0) { done = true; emit(PathSolverBFS.StepType.DONE, -1, -1); } }
            else { done = true; emit(PathSolverBFS.StepType.DONE, -1, -1); }
            return;
        }
        if (open.isEmpty()) { done = true; emit(PathSolverBFS.StepType.NO_PATH, -1, -1); emit(PathSolverBFS.StepType.DONE, -1, -1); return; }
        Node cur = open.poll();
        if (visited[cur.r][cur.c]) return; // skip stale
        visited[cur.r][cur.c] = true; emit(PathSolverBFS.StepType.VISIT, cur.r, cur.c);
        if (cur.r == goalR && cur.c == goalC) { buildPath(); reconstructing = true; emit(PathSolverBFS.StepType.FOUND, cur.r, cur.c); return; }
        for (int dir=0; dir<4; dir++) {
            if (walls[cur.r][cur.c][dir]) continue;
            int nr = cur.r + DR[dir], nc = cur.c + DC[dir];
            if (nr<0||nc<0||nr>=rows||nc>=cols) continue;
            double tentativeG = g[cur.r][cur.c] + 1.0;
            if (tentativeG < g[nr][nc]) {
                g[nr][nc] = tentativeG; parentR[nr][nc] = cur.r; parentC[nr][nc] = cur.c;
                open.add(new Node(nr, nc, tentativeG + h(nr, nc)));
                emit(PathSolverBFS.StepType.FRONTIER, nr, nc);
                return; // one neighbor per step for animation
            }
        }
    }

    private double h(int r, int c) {
        if (heuristic == Heuristic.EUCLIDEAN) {
            double dr = r - goalR, dc = c - goalC; return Math.sqrt(dr*dr + dc*dc);
        } else {
            return Math.abs(r - goalR) + Math.abs(c - goalC);
        }
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
    private static double[][] copy2D(double[][] src){ int r=src.length,c=src[0].length; double[][] a=new double[r][c]; for(int i=0;i<r;i++) System.arraycopy(src[i],0,a[i],0,c); return a; }
}
