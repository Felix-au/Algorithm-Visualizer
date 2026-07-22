package com.algorithmvisualizer.model;

public class LinkedListConceptAlgorithmV2 extends Algorithm {
    public LinkedListConceptAlgorithmV2() {
        super(
            "Linked List",
            "Node-based, step-driven SLL with value/address visualization",
            "Concepts"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/linkedlist-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.LinkedListControllerV2.class;
    }
}
