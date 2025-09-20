package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Generates valid Sudoku puzzles with at least one solution.
 */
public class SudokuGenerator {

    /** Generate a puzzle with roughly targetBlanks blanks (at least one solution guaranteed). */
    public static int[][] generatePuzzle(int targetBlanks) {
        Random rnd = new Random();
        int[][] full = generateSolved(rnd);
        int[][] puzzle = copyGrid(full);
        // positions 0..80 shuffled
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) positions.add(new int[]{r,c});
        Collections.shuffle(positions, rnd);
        int blanks = 0;
        for (int[] pos : positions) {
            int r = pos[0], c = pos[1];
            int backup = puzzle[r][c];
            puzzle[r][c] = 0;
            if (existsSolution(puzzle, 1)) {
                blanks++;
                if (blanks >= targetBlanks) break;
            } else {
                // revert removal to keep at least one solution
                puzzle[r][c] = backup;
            }
        }
        return puzzle;
    }

    /** Generate a fully solved grid via randomized backtracking. */
    public static int[][] generateSolved(Random rnd) {
        int[][] g = new int[9][9];
        // Use MRV heuristic: fill cells with fewest candidates first
        fillMRV(g, rnd);
        return g;
    }

    private static boolean fillMRV(int[][] g, Random rnd) {
        int br = -1, bc = -1, bestCount = 10;
        List<Integer> bestCands = null;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (g[r][c] == 0) {
                    List<Integer> cand = candidates(g, r, c);
                    if (cand.size() < bestCount) {
                        bestCount = cand.size();
                        br = r; bc = c; bestCands = cand;
                        if (bestCount == 1) break; // cannot do better for this cell
                    }
                }
            }
        }
        if (br == -1) return true; // solved
        Collections.shuffle(bestCands, rnd);
        for (int v : bestCands) {
            g[br][bc] = v;
            if (fillMRV(g, rnd)) return true;
            g[br][bc] = 0;
        }
        return false;
    }

    private static List<Integer> candidates(int[][] g, int r, int c) {
        boolean[] used = new boolean[10];
        for (int k = 0; k < 9; k++) { used[g[r][k]] = true; used[g[k][c]] = true; }
        int br = (r/3)*3, bc = (c/3)*3;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) used[g[br+i][bc+j]] = true;
        List<Integer> cand = new ArrayList<>();
        for (int v = 1; v <= 9; v++) if (!used[v]) cand.add(v);
        return cand;
    }

    /** Check if there exists at least one solution (early exit after first). */
    public static boolean existsSolution(int[][] grid, int limit) {
        int[][] g = copyGrid(grid);
        return solveExists(g);
    }

    private static boolean solveExists(int[][] g) {
        int r = -1, c = -1;
        outer: for (int i = 0; i < 9; i++) for (int j = 0; j < 9; j++) if (g[i][j] == 0) { r = i; c = j; break outer; }
        if (r == -1) return true;
        boolean[] used = new boolean[10];
        for (int k = 0; k < 9; k++) { used[g[r][k]] = true; used[g[k][c]] = true; }
        int br = (r/3)*3, bc = (c/3)*3;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) used[g[br+i][bc+j]] = true;
        for (int v = 1; v <= 9; v++) if (!used[v]) { g[r][c] = v; if (solveExists(g)) return true; g[r][c] = 0; }
        return false;
    }

    private static int[][] copyGrid(int[][] src) {
        int[][] g = new int[9][9];
        for (int i=0;i<9;i++) System.arraycopy(src[i], 0, g[i], 0, 9);
        return g;
    }
}
