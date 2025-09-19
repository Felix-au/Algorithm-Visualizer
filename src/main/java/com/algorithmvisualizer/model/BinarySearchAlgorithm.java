package com.algorithmvisualizer.model;

/**
 * Binary Search algorithm definition for the visualizer
 */
public class BinarySearchAlgorithm extends Algorithm {

    public BinarySearchAlgorithm() {
        super(
            "Binary Search",
            "Search a sorted array by repeatedly dividing the search interval in half",
            "Searching"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/binarysearch-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.BinarySearchController.class;
    }
}
