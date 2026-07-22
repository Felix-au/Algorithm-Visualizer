package com.algorithmvisualizer.model;

public class MSTAlgorithm extends Algorithm {
    public MSTAlgorithm() {
        super(
            "Minimum Spanning Tree",
            "Find minimum spanning tree using Kruskal's or Prim's algorithm.",
            "Graph Algorithms"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/mst-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.MSTController.class;
    }
}
