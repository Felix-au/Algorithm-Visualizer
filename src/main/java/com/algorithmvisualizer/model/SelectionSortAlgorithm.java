package com.algorithmvisualizer.model;

/**
 * Selection Sort algorithm definition for the visualizer
 */
public class SelectionSortAlgorithm extends Algorithm {

    public SelectionSortAlgorithm() {
        super("Selection Sort",
            "Sort an array by repeatedly selecting the minimum element from the unsorted part",
            "Sorting");
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/selectionsort-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.SelectionSortController.class;
    }
}


