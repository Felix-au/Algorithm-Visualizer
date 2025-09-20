package com.algorithmvisualizer.model;

public class MazeAlgorithm extends Algorithm {
    public MazeAlgorithm() {
        super(
            "Maze: Generate + Pathfind",
            "Generate a maze (DFS) and find the shortest path (BFS) on a grid.",
            "Pathfinding"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/maze-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.MazeController.class;
    }
}
