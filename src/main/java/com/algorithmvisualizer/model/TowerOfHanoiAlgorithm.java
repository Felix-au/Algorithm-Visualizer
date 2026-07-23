package com.algorithmvisualizer.model;

public class TowerOfHanoiAlgorithm extends Algorithm {
    public TowerOfHanoiAlgorithm() {
        super(
            "Tower of Hanoi (Recursion)",
            "Move N disks from A to C using B following recursion",
            "Practical Applications",
            "Backtracking"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/toh-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.TowerOfHanoiController.class;
    }
}
