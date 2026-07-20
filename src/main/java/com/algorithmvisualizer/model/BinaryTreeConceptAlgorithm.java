package com.algorithmvisualizer.model;

/**
 * Binary Tree (Concept) entry for the visualizer.
 */
public class BinaryTreeConceptAlgorithm extends Algorithm {

    public BinaryTreeConceptAlgorithm() {
        super(
            "Binary Tree (Concept)",
            "Build and operate on a Binary Search Tree: insert/delete/traverse/height.",
            "Concepts"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/binary-tree-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.BinaryTreeController.class;
    }
}
