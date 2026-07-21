package com.algorithmvisualizer.model;

/**
 * Huffman Coding algorithm entry for the visualizer.
 * Greedy compression algorithm that builds an optimal prefix-free binary tree.
 */
public class HuffmanCodingAlgorithm extends Algorithm {

    public HuffmanCodingAlgorithm() {
        super(
                "Huffman Coding",
                "Greedy compression algorithm: build optimal prefix-free codes from character frequencies.",
                "Greedy Algorithms");
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/huffmancoding-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.HuffmanCodingController.class;
    }
}
