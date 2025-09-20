package com.algorithmvisualizer.algorithm;

import java.util.*;

public class MazeGeneratorKruskal {
    public enum StepType { INIT, CARVE, DONE }
    public interface StepListener { void onStep(StepType type, int r, int c, int nr, int nc); }

    private int rows, cols;
    private boolean[][][] walls; // [r][c][4] N,E,S,W true=wall

    private int[] parent;
    private int[] rank;
    private List<int[]> edges; // each = {r, c, dir}
    private int edgeIdx = 0;
    private boolean done = false;

    private StepListener listener;

    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};

    public static class State {
        public int rows, cols;
        public boolean[][][] walls;
        public int[] parent;
        public int[] rank;
        public List<int[]> edges;
        public int edgeIdx;
        public boolean done;
    }

    public MazeGeneratorKruskal(int rows, int cols) { setSize(rows, cols); }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void setSize(int r, int c) {
        rows = Math.max(1, r); cols = Math.max(1, c);
        walls = new boolean[rows][cols][4];
        for (int i=0;i<rows;i++) for (int j=0;j<cols;j++) Arrays.fill(walls[i][j], true);
        parent = new int[rows*cols]; rank = new int[rows*cols];
        for (int i=0;i<rows*cols;i++){ parent[i]=i; rank[i]=0; }
        edges = new ArrayList<>();
        // add all cell edges (only E and S to avoid duplicates)
        for (int r0=0;r0<rows;r0++) for (int c0=0;c0<cols;c0++) {
            if (c0+1 < cols) edges.add(new int[]{r0,c0,1});
            if (r0+1 < rows) edges.add(new int[]{r0,c0,2});
        }
        Collections.shuffle(edges, new Random());
        edgeIdx = 0; done = false;
        emit(StepType.INIT, 0, 0, -1, -1);
    }

    public void reset() { setSize(rows, cols); }

    public boolean isDone() { return done; }

    public boolean[][][] getWalls() { return copyWalls(walls); }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public void step() {
        if (done) return;
        if (edgeIdx >= edges.size()) { done = true; emit(StepType.DONE, -1, -1, -1, -1); return; }
        int[] e = edges.get(edgeIdx++);
        int r = e[0], c = e[1], dir = e[2];
        int nr = r + DR[dir]; int nc = c + DC[dir];
        int a = id(r, c), b = id(nr, nc);
        if (find(a) != find(b)) {
            unite(a, b);
            // carve wall
            walls[r][c][dir] = false;
            walls[nr][nc][(dir+2)%4] = false;
            emit(StepType.CARVE, r, c, nr, nc);
        }
    }

    private int id(int r, int c) { return r*cols + c; }
    private int find(int x) { if (parent[x]==x) return x; parent[x]=find(parent[x]); return parent[x]; }
    private void unite(int a, int b) { a=find(a); b=find(b); if (a==b) return; if (rank[a]<rank[b]) parent[a]=b; else if(rank[a]>rank[b]) parent[b]=a; else { parent[b]=a; rank[a]++; } }

    private void emit(StepType t, int r, int c, int nr, int nc) { if (listener!=null) listener.onStep(t, r, c, nr, nc); }

    public State snapshot() {
        State s = new State();
        s.rows=rows; s.cols=cols; s.walls=copyWalls(walls);
        s.parent = Arrays.copyOf(parent, parent.length);
        s.rank = Arrays.copyOf(rank, rank.length);
        s.edges = new ArrayList<>(); for (int[] e : edges) s.edges.add(new int[]{e[0],e[1],e[2]});
        s.edgeIdx = edgeIdx; s.done = done; return s;
    }

    public void restore(State s) {
        if (s==null) return;
        rows=s.rows; cols=s.cols; walls=copyWalls(s.walls);
        parent = Arrays.copyOf(s.parent, s.parent.length);
        rank = Arrays.copyOf(s.rank, s.rank.length);
        edges = new ArrayList<>(); for (int[] e : s.edges) edges.add(new int[]{e[0],e[1],e[2]});
        edgeIdx = s.edgeIdx; done = s.done;
    }

    private static boolean[][][] copyWalls(boolean[][][] src) { int r=src.length,c=src[0].length; boolean[][][] w=new boolean[r][c][4]; for(int i=0;i<r;i++) for(int j=0;j<c;j++) System.arraycopy(src[i][j],0,w[i][j],0,4); return w; }
}
