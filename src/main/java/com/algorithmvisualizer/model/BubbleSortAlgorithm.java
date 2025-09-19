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
        // Note: Controller class is resolved via FXML at runtime.
        // Returning the matching controller improves clarity for maintainers.
        return com.algorithmvisualizer.ui.BubbleSortController.class;
    }
}


