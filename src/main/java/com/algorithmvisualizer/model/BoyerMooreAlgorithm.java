package com.algorithmvisualizer.model;

/**
 * Boyer-Moore string search algorithm definition for the visualizer.
 * Uses Bad Character and Good Suffix heuristics to efficiently find
 * pattern occurrences in text.
 *
 * Time Complexity: O(n/m) best case, O(n*m) worst case
 * Space Complexity: O(m + σ) where σ is the alphabet size
 */
public class BoyerMooreAlgorithm extends Algorithm {

    public BoyerMooreAlgorithm() {
        super(
            "Boyer-Moore Search",
            "Efficient string search using Bad Character and Good Suffix heuristics to skip comparisons",
            "Searching"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/boyermoore-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.BoyerMooreController.class;
    }
}
