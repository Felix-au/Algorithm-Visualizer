package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.implementations.maze.*;

/**
 * Maze Generation and Pathfinding algorithm code in multiple languages
 * Generates code containing ONLY the selected generation and pathfinding algorithms
 */
public class MazePathfindingCode implements AlgorithmCode {
    
    private int rows;
    private int cols;
    private int startR;
    private int startC;
    private int goalR;
    private int goalC;
    private String genAlgo; // "DFS", "Prim", or "Kruskal"
    private String pathAlgo; // "BFS" or "DFS"
    
    /**
     * Constructor with default values
     */
    public MazePathfindingCode() {
        this.rows = 15;
        this.cols = 15;
        this.startR = 0;
        this.startC = 0;
        this.goalR = 14;
        this.goalC = 14;
        this.genAlgo = "DFS";
        this.pathAlgo = "BFS";
    }
    
    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int rows, int cols, int startR, int startC, int goalR, int goalC,
                                 String genAlgo, String pathAlgo) {
        this.rows = rows;
        this.cols = cols;
        this.startR = startR;
        this.startC = startC;
        this.goalR = goalR;
        this.goalC = goalC;
        this.genAlgo = genAlgo != null ? genAlgo : "DFS";
        this.pathAlgo = pathAlgo != null ? pathAlgo : "BFS";
    }
    
    @Override
    public String getAlgorithmName() {
        return "Maze Pathfinding";
    }
    
    @Override
    public String getJavaCode() {
        return MazeCodeJava.generateCode(rows, cols, startR, startC, goalR, goalC, genAlgo, pathAlgo);
    }
    
    @Override
    public String getCCode() {
        return MazeCodeC.generateCode(rows, cols, startR, startC, goalR, goalC, genAlgo, pathAlgo);
    }
    
    @Override
    public String getCppCode() {
        return MazeCodeCpp.generateCode(rows, cols, startR, startC, goalR, goalC, genAlgo, pathAlgo);
    }
    
    @Override
    public String getPythonCode() {
        return MazeCodePython.generateCode(rows, cols, startR, startC, goalR, goalC, genAlgo, pathAlgo);
    }
}
