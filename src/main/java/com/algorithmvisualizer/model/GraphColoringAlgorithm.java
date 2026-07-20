package com.algorithmvisualizer.model;

public class GraphColoringAlgorithm extends Algorithm {
    public GraphColoringAlgorithm() {
        super(
            "Graph Coloring",
            "Color graph nodes so adjacent nodes have different colors using brute-force or CSP.",
            "Graph Algorithms"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/graphcoloring-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.GraphColoringController.class;
    }
}
