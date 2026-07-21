package com.algorithmvisualizer.model;

/**
 * Algorithm model for Longest Common Subsequence (LCS).
 * Classic dynamic programming problem that finds the longest subsequence
 * common to two sequences.
 */
public class LCSAlgorithm extends Algorithm {
    
    public LCSAlgorithm() {
        super(
            "Longest Common Subsequence",
            "Find the longest subsequence common to two strings using dynamic programming",
            "Dynamic Programming",
            "String Algorithms"
        );
    }
    
    @Override
    public String getVisualizationFxml() {
        return "/fxml/lcs-view.fxml";
    }
    
    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.LCSController.class;
    }
}
