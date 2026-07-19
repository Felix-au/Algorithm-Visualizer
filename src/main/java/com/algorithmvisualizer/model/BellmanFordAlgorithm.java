package com.algorithmvisualizer.model;

public class BellmanFordAlgorithm extends Algorithm {

    public BellmanFordAlgorithm() {
        super(
                "Bellman-Ford Shortest Path",
                "Find shortest paths from source vertex using Bellman-Ford algorithm. Supports negative edge weights and detects negative cycles.",
                "Graph Algorithms",
                "Pathfinding",
                "Dynamic Programming");
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/bellmanford-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.BellmanFordController.class;
    }
}
