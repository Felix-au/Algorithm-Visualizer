package com.algorithmvisualizer.model;

public class DijkstraAlgorithm extends Algorithm {
    
    public DijkstraAlgorithm() {
        super(
            "Dijkstra's Shortest Path",
            "Find shortest paths from source vertex to all other vertices using Dijkstra's algorithm",
            "Graph Algorithms",
            "Pathfinding",
            "Greedy Algorithms"
        );
    }
    
    @Override
    public String getVisualizationFxml() {
        return "/fxml/dijkstra-view.fxml";
    }
    
    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.DijkstraController.class;
    }
}
