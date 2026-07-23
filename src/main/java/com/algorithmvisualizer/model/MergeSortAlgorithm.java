package com.algorithmvisualizer.model;

/**
 * Merge Sort algorithm metadata
 */
public class MergeSortAlgorithm extends Algorithm {
    
    public MergeSortAlgorithm() {
        super(
            "Merge Sort",
            "Divide-and-conquer sorting algorithm with O(n log n) time complexity. " +
            "Recursively divides the array into halves, sorts them, and merges them back together. " +
            "Stable sort with guaranteed O(n log n) performance.",
            "Sorting"
        );
    }
    
    @Override
    public String getVisualizationFxml() {
        return "/fxml/mergesort-view.fxml";
    }
    
    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.MergeSortController.class;
    }
}
