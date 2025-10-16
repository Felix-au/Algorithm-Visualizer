package com.algorithmvisualizer.model;

/**
 * Linear Search algorithm definition for the visualizer
 */
public class LinearSearchAlgorithm extends Algorithm {

    public LinearSearchAlgorithm() {
        super(
            "Linear Search",
            "Search an array by checking each element sequentially until the target is found",
            "Searching"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/linearsearch-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.LinearSearchController.class;
    }
}
