package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.execution.CodeExecutor;
import com.algorithmvisualizer.execution.ExecutionException;
import com.algorithmvisualizer.execution.ExecutionResult;
import com.algorithmvisualizer.execution.LanguageType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller for the full-screen code editor window
 */
public class CodeEditorViewController {
    
    @FXML
    private Label algorithmNameLabel;
    
    @FXML
    private ComboBox<String> languageSelector;
    
    @FXML
    private Button themeToggleButton;
    
    @FXML
    private FontIcon themeToggleIcon;
    
    @FXML
    private Button executeButton;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private StackPane codeAreaContainer;
    
    @FXML
    private StackPane outputPanelContainer;
    
    @FXML
    private Label lineCountLabel;
    
    @FXML
    private Label executionStatusLabel;
    
    private CodeArea codeArea;
    private CodeExecutor codeExecutor;
    private InteractiveConsolePane consolePane;
    private String algorithmName;
    private boolean isLightMode = false; // Default to dark mode
    private boolean isUpdatingCode = false;
    
    @FXML
    private void initialize() {
        // Initialize CodeArea with syntax highlighting
        codeArea = CodeHighlighter.createCodeArea("Java");
        codeArea.setEditable(true);
        codeArea.getStyleClass().add("dark-mode"); // Default to dark mode
        
        // Apply stylesheet
        try {
            codeArea.getStylesheets().add(CodeHighlighter.getStylesheet());
        } catch (Exception e) {
            System.err.println("Could not load syntax highlighting stylesheet: " + e.getMessage());
        }
        
        codeAreaContainer.getChildren().add(codeArea);
        
        // Initialize interactive console pane (replaces outputArea)
        consolePane = new InteractiveConsolePane(true); // Start with dark mode
        consolePane.setText("═══════════════════════════════════════════════════════════\n" +
                          "  CODE EXECUTION OUTPUT\n" +
                          "═══════════════════════════════════════════════════════════\n\n" +
                          "Ready to execute code.\n" +
                          "Click 'Execute Code' button to run your code.\n");
        outputPanelContainer.getChildren().add(consolePane);
        
        // Update line count when text changes
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            int lineCount = newText.split("\n", -1).length;
            lineCountLabel.setText("Lines: " + lineCount);
        });
        
        // Initialize code executor (kept for compatibility, but not used directly)
        try {
            codeExecutor = new CodeExecutor();
        } catch (ExecutionException e) {
            System.err.println("Failed to initialize code executor: " + e.getMessage());
            executeButton.setDisable(true);
            executeButton.setTooltip(new Tooltip("Code executor not available"));
        }
        
        // Initialize language selector
        languageSelector.getItems().addAll("Java", "Python", "C", "C++");
        languageSelector.setValue("Java");
        
        // Language change handler - fixed to prevent flickering
        languageSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingCode && newVal != null && !newVal.equals(oldVal)) {
                isUpdatingCode = true;
                try {
                    // Load code for new language first
                    loadCodeForLanguage(newVal);
                    // Then apply highlighting once
                    CodeHighlighter.applyHighlighting(codeArea, newVal);
                } finally {
                    isUpdatingCode = false;
                }
            }
        });
        
        // Theme toggle handler
        themeToggleButton.setOnAction(e -> toggleTheme());
        
        // Execute button handler
        executeButton.setOnAction(e -> executeCode());
        
        // Close button handler
        closeButton.setOnAction(e -> closeWindow());
        
        // Set initial theme icon to moon (since we're in dark mode)
        if (themeToggleIcon != null) {
            themeToggleIcon.setIconLiteral("fas-moon");
        }
    }
    
    /**
     * Set the algorithm context for this editor
     */
    public void setAlgorithm(String algorithmName) {
        this.algorithmName = algorithmName;
        algorithmNameLabel.setText(algorithmName + " - Code Editor");
        
        // Set Python as default for Merge Sort and Quick Sort
        if ("Merge Sort".equals(algorithmName) || "Quick Sort".equals(algorithmName)) {
            languageSelector.setValue("Python");
        }
        
        // Load initial code
        loadCodeForLanguage(languageSelector.getValue());
    }
    
    /**
     * Load code for the specified language
     */
    private void loadCodeForLanguage(String language) {
        if (algorithmName == null || language == null) {
            return;
        }
        
        AlgorithmCode code = CodeRepository.getCode(algorithmName);
        if (code == null) {
            codeArea.replaceText("// No code available for this algorithm");
            return;
        }
        
        // Don't set isUpdatingCode here since it's already set by the caller
        String codeText = code.getCodeForLanguage(language);
        if (codeText != null && !codeText.isEmpty()) {
            codeArea.replaceText(codeText);
        } else {
            codeArea.replaceText("// Code not available for " + language);
        }
    }
    
    /**
     * Toggle between light and dark mode
     */
    private void toggleTheme() {
        isLightMode = !isLightMode;
        
        // Remove both classes
        codeArea.getStyleClass().removeAll("light-mode", "dark-mode");
        
        // Add appropriate class and update icon
        if (isLightMode) {
            codeArea.getStyleClass().add("light-mode");
            themeToggleIcon.setIconLiteral("fas-sun");
            consolePane.applyTheme(false); // Light mode
        } else {
            codeArea.getStyleClass().add("dark-mode");
            themeToggleIcon.setIconLiteral("fas-moon");
            consolePane.applyTheme(true); // Dark mode
        }
        
        // Re-apply syntax highlighting
        String currentLanguage = languageSelector.getValue();
        if (currentLanguage != null) {
            CodeHighlighter.applyHighlighting(codeArea, currentLanguage);
        }
    }
    
    /**
     * Execute the code
     */
    private void executeCode() {
        String code = codeArea.getText();
        String selectedLanguage = languageSelector.getValue();
        LanguageType langType = LanguageType.fromString(selectedLanguage);
        
        if (langType == null) {
            executionStatusLabel.setText("Error: Invalid language");
            executionStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        
        // Clear previous output and show running status
        consolePane.clear();
        consolePane.appendOutput("═══════════════════════════════════════════════════════════\n");
        consolePane.appendOutput("  EXECUTING CODE\n");
        consolePane.appendOutput("═══════════════════════════════════════════════════════════\n");
        consolePane.appendOutput("Language: " + selectedLanguage + "\n");
        consolePane.appendOutput("Status: Running...\n");
        consolePane.appendOutput("───────────────────────────────────────────────────────────\n\n");
        
        // Enable input for interactive programs
        consolePane.setInputEnabled(true);
        
        // Update status
        executionStatusLabel.setText("Running...");
        executionStatusLabel.setStyle("-fx-text-fill: #ffc107;");
        
        // Execute in background thread
        new Thread(() -> {
            try {
                ExecutionResult result = codeExecutor.execute(code, langType, 60, consolePane.getInputQueue());
                Platform.runLater(() -> {
                    consolePane.setInputEnabled(false);
                    displayExecutionResult(result, selectedLanguage);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    consolePane.setInputEnabled(false);
                    executionStatusLabel.setText("Error: " + e.getMessage());
                    executionStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    consolePane.appendOutput("\n\n───────────────────────────────────────────────────────────\n");
                    consolePane.appendOutput("✗ Execution Failed\n");
                    consolePane.appendOutput("───────────────────────────────────────────────────────────\n");
                    consolePane.appendOutput("Error: " + e.getMessage() + "\n");
                });
            }
        }).start();
    }
    
    /**
     * Display execution result in the output panel
     */
    private void displayExecutionResult(ExecutionResult result, String languageName) {
        // Update status label
        executionStatusLabel.setText(result.isSuccess() ? "Completed" : "Failed");
        executionStatusLabel.setStyle("-fx-text-fill: " + (result.isSuccess() ? "#28a745" : "#e74c3c") + ";");
        
        // Build output text
        StringBuilder output = new StringBuilder();
        
        // Output section
        if (result.getOutput() != null && !result.getOutput().isEmpty()) {
            output.append(result.getOutput());
        } else {
            output.append("(no output)\n");
        }
        
        // Error section
        if (!result.isSuccess() && result.getError() != null && !result.getError().isEmpty()) {
            output.append("\n\n═══════════════════════════════════════════════════════════\n");
            output.append("  ERROR OUTPUT\n");
            output.append("═══════════════════════════════════════════════════════════\n\n");
            output.append(result.getError());
        }
        
        // Footer
        output.append("\n\n───────────────────────────────────────────────────────────\n");
        output.append(result.isSuccess() ? "✓ Execution Complete\n" : "✗ Execution Failed\n");
        output.append("Time: ").append(result.getExecutionTimeMs()).append(" ms\n");
        output.append("───────────────────────────────────────────────────────────\n");
        
        consolePane.appendOutput(output.toString());
        consolePane.scrollToBottom();
    }
    
    /**
     * Close the editor window
     */
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
