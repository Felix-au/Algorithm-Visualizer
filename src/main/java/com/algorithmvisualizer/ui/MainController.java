package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.model.Algorithm;
import com.algorithmvisualizer.model.NQueensAlgorithm;
import com.algorithmvisualizer.model.SelectionSortAlgorithm;
import com.algorithmvisualizer.model.BubbleSortAlgorithm;
import com.algorithmvisualizer.model.BinarySearchAlgorithm;
import com.algorithmvisualizer.model.DepthFirstSearchAlgorithm;
import com.algorithmvisualizer.model.SudokuAlgorithm;
import com.algorithmvisualizer.model.MazeAlgorithm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    
    @FXML
    private TextField searchField;
    
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
        algorithms.add(new SudokuAlgorithm());
        algorithms.add(new MazeAlgorithm());
        
        // Filter support
        FilteredList<Algorithm> filtered = new FilteredList<>(algorithms, a -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, ov, nv) -> {
                String q = nv == null ? "" : nv.trim().toLowerCase();
                filtered.setPredicate(a -> {
                    if (q.isEmpty()) return true;
                    String name = a.getName() == null ? "" : a.getName().toLowerCase();
                    String desc = a.getDescription() == null ? "" : a.getDescription().toLowerCase();
                    String cat  = a.getCategory() == null ? "" : a.getCategory().toLowerCase();
                    return name.contains(q) || desc.contains(q) || cat.contains(q);
                });
            });
        }
        algorithmList.setItems(filtered);

        // Pretty list cells
        algorithmList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Algorithm item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label title = new Label(item.getName());
                title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                Label cat = new Label(item.getCategory());
                cat.setStyle("-fx-background-color:#e8eefb; -fx-text-fill:#2b4c7e; -fx-padding:2 6; -fx-background-radius:8; -fx-font-size:11px;");
                HBox meta = new HBox(6.0, cat);
                Label desc = new Label(item.getDescription());
                desc.setWrapText(true);
                desc.setStyle("-fx-text-fill:#555;");
                desc.setMaxWidth(360);
                VBox box = new VBox(2.0, title, meta, desc);
                box.setStyle("-fx-padding:8;");
                setGraphic(box);
            }
        });
        algorithmList.getSelectionModel().selectFirst();
        // Wire interactions
        initializeListInteractions();
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
                stage.setTitle("AlgoQuest - " + selectedAlgorithm.getName());
                
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: Show error dialog
            }
        }
    }

    @FXML
    private void initializeListInteractions() {
        // Open on double click
        algorithmList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                onSelectAlgorithm();
            }
        });
        // Open on Enter
        algorithmList.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER: onSelectAlgorithm(); break;
            }
        });
    }
}
