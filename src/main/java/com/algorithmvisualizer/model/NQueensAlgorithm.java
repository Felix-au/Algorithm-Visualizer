package com.algorithmvisualizer.model;

/**
 * N-Queens algorithm implementation
 */
public class NQueensAlgorithm extends Algorithm {
    
    public NQueensAlgorithm() {
        super("N-Queens Problem", 
              "Place N queens on an N×N chessboard so that no two queens attack each other", 
              "Backtracking");
    }
    
    @Override
    public String getVisualizationFxml() {
        return "/fxml/nqueens-view-new.fxml";
    }
    
    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.NQueensController.class;
    }
}
