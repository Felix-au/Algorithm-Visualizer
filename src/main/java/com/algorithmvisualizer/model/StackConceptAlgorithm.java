package com.algorithmvisualizer.model;

public class StackConceptAlgorithm extends Algorithm {
    public StackConceptAlgorithm() {
        super(
            "Stack (Concept)",
            "Interactive stack with Push, Pop, Peek, Search, and Search & Pop",
            "Concepts"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/stack-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.StackController.class;
    }
}
