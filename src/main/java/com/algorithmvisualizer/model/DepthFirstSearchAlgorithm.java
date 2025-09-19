package com.algorithmvisualizer.model;

/**
 * Depth-First Search algorithm definition for the visualizer
 */
public class DepthFirstSearchAlgorithm extends Algorithm {

    public DepthFirstSearchAlgorithm() {
        super(
            "Depth-First Search (DFS)",
            "Traverse a graph by exploring as far as possible along each branch before backtracking",
            "Graph Traversal"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/dfs-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.DFSController.class;
    }
}
