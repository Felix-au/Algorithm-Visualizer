package com.algorithmvisualizer.model;

public class QueueConceptAlgorithm extends Algorithm {
    public QueueConceptAlgorithm() {
        super(
            "Queue (Concept)",
            "Interactive queue with Enqueue, Dequeue, Search, Search & Dequeue, and Reverse",
            "Concepts"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/queue-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.QueueController.class;
    }
}
