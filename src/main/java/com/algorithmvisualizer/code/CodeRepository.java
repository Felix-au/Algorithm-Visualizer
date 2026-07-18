package com.algorithmvisualizer.code;

import com.algorithmvisualizer.code.implementations.LinearSearchCode;
import com.algorithmvisualizer.code.implementations.BinarySearchCode;
import com.algorithmvisualizer.code.implementations.BubbleSortCode;
import com.algorithmvisualizer.code.implementations.SelectionSortCode;
import com.algorithmvisualizer.code.implementations.MergeSortCode;
import com.algorithmvisualizer.code.implementations.QuickSortCode;
import com.algorithmvisualizer.code.implementations.FractionalKnapsackCode;
import com.algorithmvisualizer.code.implementations.DFSCode;
import com.algorithmvisualizer.code.implementations.BFSCode;
import com.algorithmvisualizer.code.implementations.GraphColoringBruteForceCode;
import com.algorithmvisualizer.code.implementations.GraphColoringCSPCode;
import com.algorithmvisualizer.code.implementations.TowerOfHanoiCode;
import com.algorithmvisualizer.code.implementations.NQueensCode;
import com.algorithmvisualizer.code.implementations.SudokuSolverCode;
import com.algorithmvisualizer.code.implementations.MazePathfindingCode;
import com.algorithmvisualizer.code.implementations.KruskalCode;
import com.algorithmvisualizer.code.implementations.PrimCode;
import com.algorithmvisualizer.code.implementations.DijkstraCode;
import com.algorithmvisualizer.code.implementations.LinkedListCode;
import com.algorithmvisualizer.code.implementations.QueueCode;
import com.algorithmvisualizer.code.implementations.StackCode;
import com.algorithmvisualizer.code.implementations.BinaryTreeCode;
import com.algorithmvisualizer.code.implementations.HuffmanCodingCode;
import com.algorithmvisualizer.code.implementations.BellmanFordCode;
import java.util.HashMap;
import java.util.Map;

/**
 * Central repository for all algorithm code implementations.
 * Manages registration and retrieval of algorithm code in multiple languages.
 */
public class CodeRepository {

    private static final Map<String, AlgorithmCode> codeMap = new HashMap<>();

    static {
        // Register algorithm codes
        register(new LinearSearchCode());
        register(new BinarySearchCode());
        register(new BubbleSortCode());
        register(new SelectionSortCode());
        register(new MergeSortCode());
        register(new QuickSortCode());
        register(new FractionalKnapsackCode());
        register(new DFSCode());
        register(new BFSCode());
        register(new GraphColoringBruteForceCode());
        register(new GraphColoringCSPCode());
        register(new TowerOfHanoiCode());
        register(new NQueensCode());
        register(new SudokuSolverCode());
        register(new MazePathfindingCode());
        register(new LinkedListCode());
        register(new QueueCode());
        register(new StackCode());
        register(new BinaryTreeCode());
        register(new KruskalCode());
        register(new PrimCode());
        register(new DijkstraCode());
        register(new HuffmanCodingCode());
        register(new BellmanFordCode());
        // More algorithms will be added here
    }

    /**
     * Register an algorithm code implementation
     */
    public static void register(AlgorithmCode code) {
        if (code != null && code.getAlgorithmName() != null) {
            codeMap.put(code.getAlgorithmName(), code);
        }
    }

    /**
     * Get code implementation for an algorithm
     */
    public static AlgorithmCode getCode(String algorithmName) {
        return codeMap.get(algorithmName);
    }

    /**
     * Check if code exists for an algorithm
     */
    public static boolean hasCode(String algorithmName) {
        return codeMap.containsKey(algorithmName);
    }

    /**
     * Get all registered algorithm names
     */
    public static String[] getRegisteredAlgorithms() {
        return codeMap.keySet().toArray(new String[0]);
    }
}
