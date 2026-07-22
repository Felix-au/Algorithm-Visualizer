package com.algorithmvisualizer.model;

/**
 * Algorithm model for Matrix Chain Multiplication.
 * Classic dynamic programming problem that finds the optimal
 * parenthesization of a chain of matrices to minimize scalar multiplications.
 *
 * Time Complexity: O(n³) where n = number of matrices
 * Space Complexity: O(n²) for the DP and split tables
 */
public class MatrixChainMultiplicationAlgorithm extends Algorithm {

    public MatrixChainMultiplicationAlgorithm() {
        super(
            "Matrix Chain Multiplication",
            "Find optimal parenthesization of matrix chain to minimize multiplications using DP",
            "Dynamic Programming",
            "Mathematical Algorithms"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/matrixchainmultiplication-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.MatrixChainMultiplicationController.class;
    }
}
