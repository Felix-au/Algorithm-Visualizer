package com.algorithmvisualizer.model;

/**
 * Algorithm model for 0/1 Knapsack Problem.
 * Classic dynamic programming problem where items must be taken wholly or not
 * at all.
 */
public class ZeroOneKnapsackAlgorithm extends Algorithm {

    public ZeroOneKnapsackAlgorithm() {
        super(
                "0/1 Knapsack",
                "Solve the 0/1 knapsack problem using dynamic programming to maximize value without exceeding capacity",
                "Dynamic Programming",
                "Practical");
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/zerooneknapsack-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.ZeroOneKnapsackController.class;
    }
}
