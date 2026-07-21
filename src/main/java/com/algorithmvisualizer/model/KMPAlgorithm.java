package com.algorithmvisualizer.model;

/**
 * KMP (Knuth-Morris-Pratt) string search algorithm definition for the visualizer.
 * Uses a failure/LPS function to skip redundant comparisons when a mismatch occurs.
 *
 * Time Complexity: O(n + m) where n = text length, m = pattern length
 * Space Complexity: O(m) for the failure function
 */
public class KMPAlgorithm extends Algorithm {

    public KMPAlgorithm() {
        super(
            "KMP Search",
            "Efficient string search using the Knuth-Morris-Pratt failure function to avoid redundant comparisons",
            "Searching"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/kmp-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.KMPController.class;
    }
}
