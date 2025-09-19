package com.algorithmvisualizer.model;

/**
 * Bubble Sort algorithm definition for the visualizer
 */
public class BubbleSortAlgorithm extends Algorithm {

    public BubbleSortAlgorithm() {
        super("Bubble Sort",
            "Repeatedly swap adjacent out-of-order elements; largest bubbles to the end",
            "Sorting");
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/bubblesort-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        // Note: Controller class is resolved via FXML; this value is not used by loader.
        // Return an existing controller class to satisfy compile-time availability.
        return com.algorithmvisualizer.ui.SelectionSortController.class;
    }
}


