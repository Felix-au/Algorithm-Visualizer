package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.model.Algorithm;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Base controller for algorithm visualization views
 */
public class AlgorithmViewController {
    
    @FXML
    protected Button backButton;
    
    @FXML
    protected Label algorithmTitle;
    
    @FXML
    protected VBox visualSection;
    
    @FXML
    protected StackPane chessboardContainer;
    
    @FXML
    protected StackPane backtrackContainer;
    
    @FXML
    protected VBox controlPane;
    
    @FXML
    protected VBox parameterControls;
    
    @FXML
    protected Button stepBackButton;
    
    @FXML
    protected Button playButton;
    
    @FXML
    protected Button pauseButton;
    
    @FXML
    protected Button stepForwardButton;
    
    @FXML
    protected Button resetButton;
    
    @FXML
    protected Slider speedSlider;
    
    @FXML
    protected Label speedLabel;
    
    @FXML
    protected ListView<String> variableList;
    
    @FXML
    protected TextArea progressArea;
    
    @FXML
    protected TextArea codeArea;
    
    @FXML
    protected TextArea stepDescription;
    
    @FXML
    protected Spinner<Integer> paramBoardSizeSpinner;
    
    @FXML
    protected Spinner<Integer> paramNumQueensSpinner;
    
    @FXML
    protected Button paramApplyButton;
    
    protected Algorithm currentAlgorithm;
    protected AlgorithmSpecificController algorithmController;
    
    @FXML
    private void initialize() {
        // Initialize speed slider
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText(String.format("%.0fx", newVal.doubleValue()));
        });
        
        // Initialize step description
        stepDescription.setText("Ready to start algorithm visualization. Click 'Play' or 'Step Forward' to begin.");
        
        // Initialize variable and loop lists
        variableList.getItems().add("Variables will appear here");
        // loopList.getItems().add("Loops will appear here"); // Removed as per edit hint
        
        // Initialize code area
        codeArea.setText("Algorithm code will appear here");
    }
    
    public void setAlgorithm(Algorithm algorithm) {
        this.currentAlgorithm = algorithm;
        algorithmTitle.setText(algorithm.getName());
        
        // Load algorithm-specific visualization
        loadAlgorithmVisualization();
    }
    
    private void loadAlgorithmVisualization() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(currentAlgorithm.getVisualizationFxml()));
            Parent algorithmView = loader.load();
            
            // Set the algorithm-specific controller
            Object controller = loader.getController();
            if (controller instanceof AlgorithmSpecificController) {
                ((AlgorithmSpecificController) controller).setParentController(this);
                this.algorithmController = (AlgorithmSpecificController) controller;
            }
            
            // Store controller reference for later use
            algorithmView.setUserData(controller);
            
            // The new layout handles visual components directly
            // No need to add to a container since they're already in the main layout
            
        } catch (IOException e) {
            e.printStackTrace();
            // Show error in step description
            stepDescription.setText("Error loading algorithm visualization: " + e.getMessage());
        }
    }
    
    @FXML
    private void onBackToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Scene mainScene = new Scene(loader.load(), 800, 600);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(mainScene);
            stage.setTitle("Algorithm Visualizer");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onStepBack() {
        if (algorithmController instanceof com.algorithmvisualizer.ui.NQueensController) {
            ((com.algorithmvisualizer.ui.NQueensController) algorithmController).onStepBack();
        }
    }
    
    @FXML
    protected void onPlay() {
        if (algorithmController instanceof com.algorithmvisualizer.ui.NQueensController) {
            ((com.algorithmvisualizer.ui.NQueensController) algorithmController).onPlay();
        }
    }
    
    @FXML
    protected void onPause() {
        if (algorithmController instanceof com.algorithmvisualizer.ui.NQueensController) {
            ((com.algorithmvisualizer.ui.NQueensController) algorithmController).onPause();
        }
    }
    
    @FXML
    protected void onStepForward() {
        if (algorithmController instanceof com.algorithmvisualizer.ui.NQueensController) {
            ((com.algorithmvisualizer.ui.NQueensController) algorithmController).onStepForward();
        }
    }
    
    @FXML
    protected void onReset() {
        if (algorithmController instanceof com.algorithmvisualizer.ui.NQueensController) {
            ((com.algorithmvisualizer.ui.NQueensController) algorithmController).onReset();
        }
    }
    
    // Interface for algorithm-specific controllers
    public interface AlgorithmSpecificController {
        void setParentController(AlgorithmViewController parent);
    }
}
