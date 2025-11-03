package com.algorithmvisualizer.model;

/**
 * Breadth-First Search algorithm definition for the visualizer
 */
public class BreadthFirstSearchAlgorithm extends Algorithm {

    public BreadthFirstSearchAlgorithm() {
        super(
            "Breadth-First Search (BFS)",
            "Traverse a graph level by level, exploring all neighbors before moving to the next level",
            "Graph Algorithms"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/bfs-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.BFSController.class;
    }
}
