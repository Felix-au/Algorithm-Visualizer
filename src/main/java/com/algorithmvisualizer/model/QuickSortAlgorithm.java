package com.algorithmvisualizer.model;

/**
 * Quick Sort algorithm metadata
 */
public class QuickSortAlgorithm extends Algorithm {
    
    public QuickSortAlgorithm() {
        super(
            "Quick Sort",
            "Divide-and-conquer sorting using partitioning around a pivot element. " +
            "Watch how pivot choice affects tree balance! " +
            "O(n log n) average, O(n²) worst case. In-place sorting.",
            "Sorting"
        );
    }
    
    @Override
    public String getVisualizationFxml() {
        return "/fxml/quicksort-view.fxml";
    }
    
    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.QuickSortController.class;
    }
}
