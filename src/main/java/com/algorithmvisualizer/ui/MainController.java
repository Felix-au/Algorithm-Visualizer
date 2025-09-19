package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.model.Algorithm;
import com.algorithmvisualizer.model.NQueensAlgorithm;
import com.algorithmvisualizer.model.SelectionSortAlgorithm;
import com.algorithmvisualizer.model.BubbleSortAlgorithm;
import com.algorithmvisualizer.model.BinarySearchAlgorithm;
import com.algorithmvisualizer.model.DepthFirstSearchAlgorithm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the main application view
 */
public class MainController {
    
    @FXML
    private ListView<Algorithm> algorithmList;
    
    @FXML
    private Button selectButton;
    
    private ObservableList<Algorithm> algorithms;
    
    @FXML
    private void initialize() {
        // Initialize the list of available algorithms
        algorithms = FXCollections.observableArrayList();
        algorithms.add(new NQueensAlgorithm());
        algorithms.add(new SelectionSortAlgorithm());
        algorithms.add(new BubbleSortAlgorithm());
        algorithms.add(new BinarySearchAlgorithm());
        algorithms.add(new DepthFirstSearchAlgorithm());
        
        algorithmList.setItems(algorithms);
        algorithmList.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void onSelectAlgorithm() {
        Algorithm selectedAlgorithm = algorithmList.getSelectionModel().getSelectedItem();
        if (selectedAlgorithm != null) {
        try {
            // Load the algorithm visualization view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/algorithm-view-new.fxml"));
            Scene algorithmScene = new Scene(loader.load(), 1400, 900);
                
                // Pass the selected algorithm to the controller
                AlgorithmViewController controller = loader.getController();
                controller.setAlgorithm(selectedAlgorithm);
                
                Stage stage = (Stage) selectButton.getScene().getWindow();
                stage.setScene(algorithmScene);
                stage.setTitle("Algorithm Visualizer - " + selectedAlgorithm.getName());
                
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: Show error dialog
            }
        }
    }
}
