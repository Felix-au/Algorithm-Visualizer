package com.algorithmvisualizer.model;

public class SudokuAlgorithm extends Algorithm {

    public SudokuAlgorithm() {
        super(
            "Sudoku Solver",
            "Solve a 9x9 Sudoku puzzle using backtracking with constraint checking",
            "Real-World Problems",
            "Backtracking",
            "Practical Applications"
        );
    }

    @Override
    public String getVisualizationFxml() {
        return "/fxml/sudoku-view.fxml";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.algorithmvisualizer.ui.SudokuController.class;
    }
}
