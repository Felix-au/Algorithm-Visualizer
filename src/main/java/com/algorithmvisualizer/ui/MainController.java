package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.model.*;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Modern categorized controller for the main application view
 */
public class MainController {
    
    @FXML private TextField searchField;
    @FXML private FlowPane categoryGrid;
    @FXML private VBox algorithmSection;
    @FXML private FlowPane algorithmGrid;
    @FXML private Label categoryTitle;
    @FXML private Button backButton;
    @FXML private VBox mainContent;
    
    private ObservableList<Algorithm> allAlgorithms;
    private Map<AlgorithmCategory, List<Algorithm>> algorithmsByCategory;
    private AlgorithmCategory currentCategory = null;
    
    @FXML
    private void initialize() {
        // Initialize algorithms
        initializeAlgorithms();
        
        // Setup search
        setupSearch();
        
        // Create category cards
        createCategoryCards();
        
        // Set window size to 90% screen size and center
        configureWindowSize();
    }
    
    private void initializeAlgorithms() {
        allAlgorithms = FXCollections.observableArrayList(
            new BinarySearchAlgorithm(),
            new LinearSearchAlgorithm(),
            new BubbleSortAlgorithm(),
            new SelectionSortAlgorithm(),
            new DepthFirstSearchAlgorithm(),
            new NQueensAlgorithm(),
            new SudokuAlgorithm(),
            new MazeAlgorithm()
        );
        
        // Group algorithms by category
        algorithmsByCategory = new HashMap<>();
        for (AlgorithmCategory category : AlgorithmCategory.values()) {
            algorithmsByCategory.put(category, new ArrayList<>());
        }
        
        // Add to "All" and specific categories (supports multiple tags)
        for (Algorithm algo : allAlgorithms) {
            algorithmsByCategory.get(AlgorithmCategory.ALL).add(algo);
            
            // Add to each category the algorithm belongs to
            for (String tag : algo.getTags()) {
                AlgorithmCategory category = AlgorithmCategory.fromString(tag);
                if (category != AlgorithmCategory.ALL && !algorithmsByCategory.get(category).contains(algo)) {
                    algorithmsByCategory.get(category).add(algo);
                }
            }
        }
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal == null ? "" : newVal.trim().toLowerCase();
            
            if (query.isEmpty()) {
                // Show categories if no search query
                if (currentCategory == null) {
                    showCategories();
                } else {
                    showCategory(currentCategory);
                }
            } else {
                // Show filtered results
                showSearchResults(query);
            }
        });
    }
    
    private void createCategoryCards() {
        categoryGrid.getChildren().clear();
        
        // Define category order
        AlgorithmCategory[] orderedCategories = {
            AlgorithmCategory.ALL,
            AlgorithmCategory.SEARCHING,
            AlgorithmCategory.SORTING,
            AlgorithmCategory.GRAPH,
            AlgorithmCategory.BACKTRACKING,
            AlgorithmCategory.PATHFINDING,
            AlgorithmCategory.PRACTICAL,
            AlgorithmCategory.REAL_WORLD
        };
        
        // Color schemes for categories
        Map<AlgorithmCategory, String> categoryColors = new HashMap<>();
        categoryColors.put(AlgorithmCategory.ALL, "#4a90e2");
        categoryColors.put(AlgorithmCategory.SEARCHING, "#7b68ee");
        categoryColors.put(AlgorithmCategory.SORTING, "#ff6b6b");
        categoryColors.put(AlgorithmCategory.GRAPH, "#4ecdc4");
        categoryColors.put(AlgorithmCategory.BACKTRACKING, "#f39c12");
        categoryColors.put(AlgorithmCategory.PATHFINDING, "#2ecc71");
        categoryColors.put(AlgorithmCategory.PRACTICAL, "#9b59b6");
        categoryColors.put(AlgorithmCategory.REAL_WORLD, "#e74c3c");
        
        for (AlgorithmCategory category : orderedCategories) {
            int count = algorithmsByCategory.get(category).size();
            if (count > 0 || category == AlgorithmCategory.ALL) {
                VBox card = createCategoryCard(category, count, categoryColors.get(category));
                categoryGrid.getChildren().add(card);
            }
        }
    }
    
    private VBox createCategoryCard(AlgorithmCategory category, int count, String color) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(220, 180);
        card.setMaxSize(220, 180);
        card.setStyle(String.format(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3); " +
            "-fx-padding: 25; " +
            "-fx-cursor: hand;"
        ));
        
        // Icon using Ikonli
        FontIcon icon = new FontIcon(category.getIcon());
        icon.setIconSize(42);
        icon.setIconColor(javafx.scene.paint.Color.web(color));
        
        // Category name
        Label name = new Label(category.getDisplayName());
        name.setFont(Font.font("System", FontWeight.BOLD, 16));
        name.setStyle("-fx-text-fill: #2b4c7e;");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);
        name.setMaxWidth(180);
        
        // Count badge
        Label countLabel = new Label(count + " algorithm" + (count != 1 ? "s" : ""));
        countLabel.setFont(Font.font("System", 12));
        countLabel.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 4 12; " +
            "-fx-background-radius: 12;",
            color
        ));
        
        // Description
        Label desc = new Label(category.getDescription());
        desc.setFont(Font.font("System", 11));
        desc.setStyle("-fx-text-fill: #6c757d;");
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);
        desc.setMaxWidth(180);
        
        card.getChildren().addAll(icon, name, countLabel);
        
        // Hover effect
        addHoverEffect(card);
        
        // Click handler
        card.setOnMouseClicked(e -> showCategory(category));
        
        return card;
    }
    
    private void addHoverEffect(VBox card) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        card.setOnMouseEntered(e -> {
            scaleUp.playFromStart();
            card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5);");
        });
        
        card.setOnMouseExited(e -> {
            scaleDown.playFromStart();
            card.setStyle(card.getStyle().replace(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5);",
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);"
            ));
        });
    }
    
    @FXML
    private void showCategories() {
        currentCategory = null;
        searchField.clear();
        categoryGrid.setVisible(true);
        categoryGrid.setManaged(true);
        algorithmSection.setVisible(false);
        algorithmSection.setManaged(false);
    }
    
    private void showCategory(AlgorithmCategory category) {
        currentCategory = category;
        categoryTitle.setText(category.getDisplayName());
        
        categoryGrid.setVisible(false);
        categoryGrid.setManaged(false);
        algorithmSection.setVisible(true);
        algorithmSection.setManaged(true);
        
        // Populate algorithm cards
        List<Algorithm> algorithms = algorithmsByCategory.get(category);
        displayAlgorithmCards(algorithms);
    }
    
    private void showSearchResults(String query) {
        List<Algorithm> results = allAlgorithms.stream()
            .filter(a -> {
                String name = a.getName().toLowerCase();
                String desc = a.getDescription().toLowerCase();
                String cat = a.getCategory().toLowerCase();
                return name.contains(query) || desc.contains(query) || cat.contains(query);
            })
            .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            categoryTitle.setText("🔍 No results found for \"" + query + "\"");
            algorithmGrid.getChildren().clear();
        } else {
            categoryTitle.setText("🔍 Search Results (" + results.size() + ")");
            displayAlgorithmCards(results);
        }
        
        categoryGrid.setVisible(false);
        categoryGrid.setManaged(false);
        algorithmSection.setVisible(true);
        algorithmSection.setManaged(true);
    }
    
    private void displayAlgorithmCards(List<Algorithm> algorithms) {
        algorithmGrid.getChildren().clear();
        
        for (Algorithm algo : algorithms) {
            VBox card = createAlgorithmCard(algo);
            algorithmGrid.getChildren().add(card);
        }
    }
    
    private VBox createAlgorithmCard(Algorithm algo) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefSize(280, 160);
        card.setMaxSize(280, 160);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
            "-fx-padding: 20; " +
            "-fx-cursor: hand;"
        );
        
        // Title
        Label title = new Label(algo.getName());
        title.setFont(Font.font("System", FontWeight.BOLD, 15));
        title.setStyle("-fx-text-fill: #2b4c7e;");
        title.setWrapText(true);
        title.setMaxWidth(240);
        
        // Category badge
        Label categoryBadge = new Label(algo.getCategory());
        categoryBadge.setFont(Font.font("System", 11));
        categoryBadge.setStyle(
            "-fx-background-color: #e8eefb; " +
            "-fx-text-fill: #2b4c7e; " +
            "-fx-padding: 3 10; " +
            "-fx-background-radius: 10;"
        );
        
        // Description
        Label description = new Label(algo.getDescription());
        description.setFont(Font.font("System", 12));
        description.setStyle("-fx-text-fill: #6c757d;");
        description.setWrapText(true);
        description.setMaxWidth(240);
        description.setMaxHeight(60);
        
        VBox.setVgrow(description, Priority.ALWAYS);
        
        card.getChildren().addAll(title, categoryBadge, description);
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + 
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0, 0, 4); " +
                "-fx-background-color: #f8f9fa;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
                "-fx-padding: 20; " +
                "-fx-cursor: hand;"
            );
        });
        
        // Click handler
        card.setOnMouseClicked(e -> openAlgorithm(algo));
        
        return card;
    }
    
    private void openAlgorithm(Algorithm algorithm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/algorithm-view-new.fxml"));
            Scene algorithmScene = new Scene(loader.load(), 1400, 900);
            
            AlgorithmViewController controller = loader.getController();
            controller.setAlgorithm(algorithm);
            
            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(algorithmScene);
            stage.setTitle("AlgoQuest - " + algorithm.getName());
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load algorithm visualization");
        }
    }
    
    private void configureWindowSize() {
        mainContent.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                    if (newWin instanceof Stage) {
                        Stage stage = (Stage) newWin;
                        
                        // Get primary screen bounds
                        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                        
                        // Set to 90% of screen size
                        double width = screenBounds.getWidth() * 0.9;
                        double height = screenBounds.getHeight() * 0.9;
                        
                        stage.setWidth(width);
                        stage.setHeight(height);
                        
                        // Center on screen
                        stage.setX((screenBounds.getWidth() - width) / 2);
                        stage.setY((screenBounds.getHeight() - height) / 2);
                    }
                });
            }
        });
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
