package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.execution.CodeExecutor;
import com.algorithmvisualizer.execution.ExecutionException;
import com.algorithmvisualizer.execution.ExecutionResult;
import com.algorithmvisualizer.execution.LanguageType;
import com.algorithmvisualizer.model.Algorithm;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

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
    protected ScrollPane solutionsContainer;
    
    @FXML
    protected FlowPane solutionsContent;
    
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
    protected StackPane codeAreaContainer;
    
    protected CodeArea codeArea; // RichTextFX CodeArea for syntax highlighting
    
    @FXML
    protected TextArea stepDescription;
    
    @FXML
    protected ComboBox<String> languageSelector;
    
    @FXML
    protected Button executeButton;
    
    @FXML
    protected Button openEditorButton;
    
    @FXML
    protected Button themeToggleButton;
    
    @FXML
    protected org.kordamp.ikonli.javafx.FontIcon themeToggleIcon;
    
    @FXML
    protected Spinner<Integer> paramBoardSizeSpinner;
    
    @FXML
    protected Spinner<Integer> paramNumQueensSpinner;
    
    @FXML
    protected Button paramApplyButton;
    
    @FXML
    protected TextField paramElementsField;
    
    @FXML
    protected Button paramRandomizeButton;
    
    @FXML
    protected Label paramSizeLabel;
    
    @FXML
    protected Label paramTargetLabel;
    
    @FXML
    protected javafx.scene.layout.FlowPane paramElementsBox;

    @FXML
    protected Label paramElementsLabel;
    
    @FXML
    protected HBox chessboardHeaderBox;
    
    @FXML
    protected VBox chessboardLegendBox;

    // Solutions section headers (to be hidden for Selection Sort)
    @FXML
    protected HBox solutionsHeaderBox;
    @FXML
    protected Label solutionsHeaderLabel;
    @FXML
    protected VBox solutionsSubHeaderBox;
    @FXML
    protected Label solutionsSubHeaderLabel;
    
    protected Algorithm currentAlgorithm;
    protected AlgorithmSpecificController algorithmController;
    private CodeExecutor codeExecutor;
    private String currentAlgorithmName;
    protected boolean isUpdatingCode = false; // Flag to prevent recursive updates
    private boolean isLightMode = true; // Default to light mode
    
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
        
        // Initialize CodeArea with syntax highlighting
        if (codeAreaContainer != null) {
            codeArea = CodeHighlighter.createCodeArea("Java");
            codeArea.setPrefHeight(520);
            codeArea.setPrefWidth(300);
            codeArea.setEditable(true);
            codeArea.replaceText("Algorithm code will appear here");
            
            // Apply light mode by default
            codeArea.getStyleClass().add("light-mode");
            
            // Apply stylesheet
            try {
                codeArea.getStylesheets().add(CodeHighlighter.getStylesheet());
            } catch (Exception e) {
                System.err.println("Could not load syntax highlighting stylesheet: " + e.getMessage());
            }
            
            codeAreaContainer.getChildren().clear();
            codeAreaContainer.getChildren().add(codeArea);
        }
        
        // Initialize theme toggle button
        if (themeToggleButton != null) {
            themeToggleButton.setOnAction(e -> toggleTheme());
        }
        
        // Initialize open editor button
        if (openEditorButton != null) {
            openEditorButton.setOnAction(e -> openFullScreenEditor());
        }
        
        // Initialize code executor
        try {
            codeExecutor = new CodeExecutor();
        } catch (ExecutionException e) {
            System.err.println("Failed to initialize code executor: " + e.getMessage());
            if (executeButton != null) {
                executeButton.setDisable(true);
                executeButton.setTooltip(new Tooltip("Code executor not available"));
            }
        }
        
        // Initialize language selector
        if (languageSelector != null) {
            languageSelector.getItems().addAll("Java", "Python", "C", "C++");
            languageSelector.setValue("Java"); // Default to Java
            
            // Language change handler - fixed to prevent race condition
            languageSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (!isUpdatingCode && newVal != null && !newVal.equals(oldVal)) {
                    isUpdatingCode = true;
                    try {
                        // Load code first
                        loadCodeForCurrentLanguage();
                        // Then apply highlighting after a short delay to ensure code is loaded
                        if (codeArea != null) {
                            javafx.application.Platform.runLater(() -> {
                                CodeHighlighter.applyHighlighting(codeArea, newVal);
                            });
                        }
                    } finally {
                        isUpdatingCode = false;
                    }
                }
            });
        }
        
        // Execute button handler
        if (executeButton != null) {
            executeButton.setOnAction(e -> executeCode());
        }
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
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            
            System.out.println("=== BACK TO MAIN DEBUG ===");
            System.out.println("Stage dimensions: " + stage.getWidth() + "x" + stage.getHeight());
            System.out.println("Maximized: " + stage.isMaximized());
            
            // Get actual stage dimensions
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            // Create scene matching current stage size
            Scene mainScene = new Scene(root, width, height);
            
            System.out.println("Scene created: " + mainScene.getWidth() + "x" + mainScene.getHeight());
            
            // Temporarily un-maximize to force layout recalculation
            boolean wasMaximized = stage.isMaximized();
            if (wasMaximized) {
                stage.setMaximized(false);
            }
            
            stage.setScene(mainScene);
            stage.setTitle("AlgoBuddy");
            
            // Re-maximize after scene is set
            if (wasMaximized) {
                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                    System.out.println("After re-maximize: " + stage.getScene().getWidth() + "x" + stage.getScene().getHeight());
                });
            }
            
            System.out.println("=========================");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onStepBack() {
        if (algorithmController != null) {
            algorithmController.onStepBack();
        }
    }
    
    @FXML
    protected void onPlay() {
        if (algorithmController != null) {
            algorithmController.onPlay();
        }
    }
    
    @FXML
    protected void onPause() {
        if (algorithmController != null) {
            algorithmController.onPause();
        }
    }
    
    @FXML
    protected void onStepForward() {
        if (algorithmController != null) {
            algorithmController.onStepForward();
        }
    }
    
    @FXML
    protected void onReset() {
        if (algorithmController != null) {
            algorithmController.onReset();
        }
    }
    
    /**
     * Toggle between light and dark mode for code editor
     */
    private void toggleTheme() {
        if (codeArea == null || themeToggleIcon == null) {
            return;
        }
        
        isLightMode = !isLightMode;
        
        // Remove both classes first
        codeArea.getStyleClass().removeAll("light-mode", "dark-mode");
        
        // Add the appropriate class and update icon
        if (isLightMode) {
            codeArea.getStyleClass().add("light-mode");
            themeToggleIcon.setIconLiteral("fas-sun");
        } else {
            codeArea.getStyleClass().add("dark-mode");
            themeToggleIcon.setIconLiteral("fas-moon");
        }
        
        // Re-apply syntax highlighting to refresh colors
        String currentLanguage = languageSelector != null ? languageSelector.getValue() : "Java";
        if (currentLanguage != null) {
            CodeHighlighter.applyHighlighting(codeArea, currentLanguage);
        }
    }
    
    /**
     * Open full-screen code editor window
     */
    private void openFullScreenEditor() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/code-editor-view.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            // Get controller and set algorithm context
            CodeEditorViewController controller = loader.getController();
            if (currentAlgorithmName != null) {
                controller.setAlgorithm(currentAlgorithmName);
            }
            
            // Create new stage
            javafx.stage.Stage editorStage = new javafx.stage.Stage();
            editorStage.setTitle("AlgoBuddy - Code Editor");
            editorStage.setScene(new javafx.scene.Scene(root, 1200, 700));
            editorStage.initModality(javafx.stage.Modality.NONE);
            editorStage.show();
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open code editor: " + e.getMessage());
        }
    }
    
    /**
     * Load code for the currently selected language from the repository
     */
    protected void loadCodeForCurrentLanguage() {
        if (currentAlgorithmName == null || languageSelector == null || codeArea == null) {
            return;
        }
        
        AlgorithmCode code = CodeRepository.getCode(currentAlgorithmName);
        if (code == null) {
            // No code in repository, keep existing code
            return;
        }
        
        String selectedLanguage = languageSelector.getValue();
        if (selectedLanguage == null) {
            return;
        }
        
        isUpdatingCode = true;
        try {
            String codeText = code.getCodeForLanguage(selectedLanguage);
            if (codeText != null && !codeText.isEmpty()) {
                codeArea.replaceText(codeText);
            }
        } finally {
            isUpdatingCode = false;
        }
        
        // Reapply highlighting after flag is cleared to ensure formatting is preserved
        javafx.application.Platform.runLater(() -> {
            if (codeArea != null && selectedLanguage != null) {
                CodeHighlighter.applyHighlighting(codeArea, selectedLanguage);
            }
        });
    }
    
    /**
     * Show execution result window with interactive console
     */
    private void showExecutionResultWindow(ExecutionResult result, String languageName) {
        Stage executionStage = new Stage();
        executionStage.setTitle("Execution Output - " + languageName);
        
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(15));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(result.isSuccess() ? "✓ Execution Successful" : "✗ Execution Failed");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + 
            (result.isSuccess() ? "#4caf50" : "#f44336") + ";");
        
        Label langLabel = new Label("Language: " + languageName);
        langLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        
        Label timeLabel = new Label("Time: " + result.getExecutionTimeMs() + " ms");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(titleLabel, spacer, langLabel, timeLabel);
        
        // Interactive console pane (replaces TextArea)
        InteractiveConsolePane consolePane = new InteractiveConsolePane(true); // Dark mode
        VBox.setVgrow(consolePane, javafx.scene.layout.Priority.ALWAYS);
        
        // Build output text
        StringBuilder output = new StringBuilder();
        output.append("═══════════════════════════════════════════════════\n");
        output.append("OUTPUT:\n");
        output.append("═══════════════════════════════════════════════════\n\n");
        
        if (result.getOutput() != null && !result.getOutput().isEmpty()) {
            output.append(result.getOutput());
        } else {
            output.append("(no output)\n");
        }
        
        if (!result.isSuccess() && result.getError() != null && !result.getError().isEmpty()) {
            output.append("\n\n═══════════════════════════════════════════════════\n");
            output.append("ERROR:\n");
            output.append("═══════════════════════════════════════════════════\n\n");
            output.append(result.getError());
        }
        
        consolePane.setText(output.toString());
        consolePane.setInputEnabled(false); // Disable input for completed execution
        
        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 20; " +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        closeButton.setOnAction(e -> executionStage.close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        root.getChildren().addAll(header, new Separator(), consolePane, buttonBox);
        
        Scene scene = new Scene(root, 700, 500);
        executionStage.setScene(scene);
        executionStage.show();
    }
    
    /**
     * Execute code and show result
     */
    protected void executeCode() {
        if (codeExecutor == null) {
            showExecutionError("Code executor not initialized");
            return;
        }
        
        String code = codeArea.getText();
        if (code == null || code.trim().isEmpty()) {
            showExecutionError("No code to execute");
            return;
        }
        
        String selectedLanguage = languageSelector.getValue();
        if (selectedLanguage == null) {
            showExecutionError("No language selected");
            return;
        }
        
        LanguageType langType = LanguageType.fromString(selectedLanguage);
        if (langType == null) {
            showExecutionError("Unsupported language: " + selectedLanguage);
            return;
        }
        
        // Create interactive execution window
        showInteractiveExecutionWindow(code, langType, selectedLanguage);
    }
    
    /**
     * Show interactive execution window with live output and input support
     */
    private void showInteractiveExecutionWindow(String code, LanguageType langType, String languageName) {
        Stage executionStage = new Stage();
        executionStage.setTitle("Executing - " + languageName);
        
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(15));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("⚡ Executing Code...");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffc107;");
        
        Label langLabel = new Label("Language: " + languageName);
        langLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(titleLabel, spacer, langLabel);
        
        // Interactive console pane
        InteractiveConsolePane consolePane = new InteractiveConsolePane(true); // Dark mode
        VBox.setVgrow(consolePane, javafx.scene.layout.Priority.ALWAYS);
        
        consolePane.setText("═══════════════════════════════════════════════════\n" +
                           "EXECUTING CODE...\n" +
                           "═══════════════════════════════════════════════════\n\n");
        consolePane.setInputEnabled(true); // Enable input during execution
        
        // Close button
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 20; " +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        closeButton.setOnAction(e -> executionStage.close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        root.getChildren().addAll(header, new Separator(), consolePane, buttonBox);
        
        Scene scene = new Scene(root, 700, 500);
        executionStage.setScene(scene);
        executionStage.show();
        
        // Execute in background thread
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                ExecutionResult result = codeExecutor.execute(code, langType, 60, consolePane.getInputQueue());
                long executionTime = System.currentTimeMillis() - startTime;
                
                javafx.application.Platform.runLater(() -> {
                    consolePane.setInputEnabled(false);
                    
                    // Update header
                    titleLabel.setText(result.isSuccess() ? "✓ Execution Complete" : "✗ Execution Failed");
                    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + 
                        (result.isSuccess() ? "#4caf50" : "#f44336") + ";");
                    
                    // Append result
                    StringBuilder output = new StringBuilder();
                    
                    if (result.getOutput() != null && !result.getOutput().isEmpty()) {
                        output.append(result.getOutput());
                    }
                    
                    if (!result.isSuccess() && result.getError() != null && !result.getError().isEmpty()) {
                        output.append("\n\n═══════════════════════════════════════════════════\n");
                        output.append("ERROR:\n");
                        output.append("═══════════════════════════════════════════════════\n\n");
                        output.append(result.getError());
                    }
                    
                    output.append("\n\n───────────────────────────────────────────────────\n");
                    output.append(result.isSuccess() ? "✓ Execution Complete\n" : "✗ Execution Failed\n");
                    output.append("Time: ").append(executionTime).append(" ms\n");
                    output.append("───────────────────────────────────────────────────\n");
                    
                    consolePane.appendOutput(output.toString());
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    consolePane.setInputEnabled(false);
                    titleLabel.setText("✗ Execution Error");
                    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f44336;");
                    consolePane.appendOutput("\n\nExecution failed: " + e.getMessage() + "\n");
                });
            }
        }).start();
    }
    
    /**
     * Display execution result in the output area (deprecated - now shows in window)
     */
    private void displayExecutionResult(ExecutionResult result) {
        // This method is no longer used - results shown in window
        String selectedLanguage = languageSelector != null ? languageSelector.getValue() : "Java";
        showExecutionResultWindow(result, selectedLanguage);
    }
    
    /**
     * Show execution error message
     */
    private void showExecutionError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Execution Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Set the current algorithm name (called by child controllers)
     */
    public void setCurrentAlgorithmName(String algorithmName) {
        this.currentAlgorithmName = algorithmName;
        loadCodeForCurrentLanguage();
    }
    
    /**
     * Set the current algorithm name with a specific default language
     */
    public void setCurrentAlgorithmName(String algorithmName, String defaultLanguage) {
        this.currentAlgorithmName = algorithmName;
        if (languageSelector != null && defaultLanguage != null) {
            languageSelector.setValue(defaultLanguage);
        }
        loadCodeForCurrentLanguage();
    }
    
    // Interface for algorithm-specific controllers
    public interface AlgorithmSpecificController {
        void setParentController(AlgorithmViewController parent);
        // Common control hooks
        default void onStepBack() {}
        default void onPlay() {}
        default void onPause() {}
        default void onStepForward() {}
        default void onReset() {}
    }
}
