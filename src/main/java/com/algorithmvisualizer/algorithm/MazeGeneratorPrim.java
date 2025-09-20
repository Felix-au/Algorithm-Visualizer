package com.algorithmvisualizer.algorithm;

import java.util.*;

public class MazeGeneratorPrim {
    public enum StepType { INIT, CARVE, DONE }
    public interface StepListener { void onStep(StepType type, int r, int c, int nr, int nc); }

    private int rows, cols;
    private boolean[][][] walls; // [r][c][4] N,E,S,W true=wall
    private boolean[][] inMaze;
    private List<int[]> frontier = new ArrayList<>(); // {r,c,dir}
    private boolean started = false;
    private boolean done = false;

    private StepListener listener;
    private Random rnd = new Random();

    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};

    public static class State {
        public int rows, cols;
        public boolean[][][] walls;
        public boolean[][] inMaze;
        public List<int[]> frontier;
        public boolean started;
        public boolean done;
    }

    public MazeGeneratorPrim(int rows, int cols) { setSize(rows, cols); }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void setSize(int r, int c) {
        rows = Math.max(1, r); cols = Math.max(1, c);
        walls = new boolean[rows][cols][4];
        for (int i=0;i<rows;i++) for (int j=0;j<cols;j++) Arrays.fill(walls[i][j], true);
        inMaze = new boolean[rows][cols];
        frontier.clear();
        started = false; done = false;
        emit(StepType.INIT, 0, 0, -1, -1);
    }

    public void reset() { setSize(rows, cols); }

    public boolean isDone() { return done; }

    public boolean[][][] getWalls() { return copyWalls(walls); }

    public void step() {
        if (done) return;
        if (!started) {
            addCell(0, 0);
            started = true;
            emit(StepType.INIT, 0, 0, -1, -1);
            return;
        }
        if (frontier.isEmpty()) {
            done = true; emit(StepType.DONE, -1, -1, -1, -1); return;
        }
        int idx = rnd.nextInt(frontier.size());
        int[] e = frontier.remove(idx);
        int r = e[0], c = e[1], dir = e[2];
        int nr = r + DR[dir];
        int nc = c + DC[dir];
        if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) return; // skip invalid
        if (!inMaze[nr][nc]) {
            // carve
            walls[r][c][dir] = false;
            walls[nr][nc][(dir + 2) % 4] = false;
            addCell(nr, nc);
            emit(StepType.CARVE, r, c, nr, nc);
        }
    }

    private void addCell(int r, int c) {
        inMaze[r][c] = true;
        for (int dir = 0; dir < 4; dir++) {
            int nr = r + DR[dir], nc = c + DC[dir];
            if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
            if (!inMaze[nr][nc]) frontier.add(new int[]{r, c, dir});
        }
    }

    private void emit(StepType t, int r, int c, int nr, int nc) { if (listener != null) listener.onStep(t, r, c, nr, nc); }

    public State snapshot() {
        State s = new State();
        s.rows = rows; s.cols = cols; s.walls = copyWalls(walls);
        s.inMaze = new boolean[rows][cols]; for (int i=0;i<rows;i++) System.arraycopy(inMaze[i],0,s.inMaze[i],0,cols);
        s.frontier = new ArrayList<>(); for (int[] f : frontier) s.frontier.add(new int[]{f[0],f[1],f[2]});
        s.started = started; s.done = done;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        rows = s.rows; cols = s.cols; walls = copyWalls(s.walls);
        inMaze = new boolean[rows][cols]; for (int i=0;i<rows;i++) System.arraycopy(s.inMaze[i],0,inMaze[i],0,cols);
        frontier = new ArrayList<>(); for (int[] f : s.frontier) frontier.add(new int[]{f[0],f[1],f[2]});
        started = s.started; done = s.done;
    }

    private static boolean[][][] copyWalls(boolean[][][] src) { int r=src.length,c=src[0].length; boolean[][][] w=new boolean[r][c][4]; for(int i=0;i<r;i++) for(int j=0;j<c;j++) System.arraycopy(src[i][j],0,w[i][j],0,4); return w; }
}
