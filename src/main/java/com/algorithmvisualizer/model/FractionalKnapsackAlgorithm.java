package com.algorithmvisualizer.model;

/**
 * Fractional Knapsack algorithm definition for the visualizer
 */
public class FractionalKnapsackAlgorithm extends Algorithm {

    public FractionalKnapsackAlgorithm() {
        super("Fractional Knapsack",
            "Greedy algorithm: maximize value by taking items (or fractions) based on value/weight ratio",
            "Greedy", "Practical", "Real-World");
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/fractionalknapsack-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.FractionalKnapsackController.class;
    }
}
