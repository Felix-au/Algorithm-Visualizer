package com.algorithmvisualizer.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Interactive console component that supports both output display and user input.
 * Designed to replace standard TextArea for code execution output.
 * Includes visual indicators when program is waiting for input.
 */
public class InteractiveConsolePane extends VBox {
    
    private final TextArea outputArea;
    private final TextField inputField;
    private final Label hintLabel;
    private final BlockingQueue<String> inputQueue;
    private boolean inputEnabled;
    private boolean isDarkMode;
    
    // Input waiting detection
    private Timeline idleTimer;
    private Timeline pulseAnimation;
    private long lastOutputTime;
    private static final long IDLE_THRESHOLD_MS = 3000; // 3 seconds
    
    // Placeholder texts
    private static final String PLACEHOLDER_NORMAL = "Enter input here and press Enter...";
    private static final String PLACEHOLDER_WAITING = "⌨️ Program is waiting for your input - type here and press Enter";
    
    public InteractiveConsolePane() {
        this(false);
    }
    
    public InteractiveConsolePane(boolean darkMode) {
        super(0); // No spacing between components for seamless look
        this.isDarkMode = darkMode;
        this.inputQueue = new LinkedBlockingQueue<>();
        this.inputEnabled = false;
        this.lastOutputTime = System.currentTimeMillis();
        
        // Initialize output area
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        
        // Initialize hint label (hidden by default)
        hintLabel = new Label();
        hintLabel.setWrapText(true);
        hintLabel.setVisible(false);
        hintLabel.setManaged(false);
        hintLabel.setMaxWidth(Double.MAX_VALUE);
        hintLabel.setAlignment(Pos.CENTER_LEFT);
        hintLabel.setPadding(new Insets(8, 10, 8, 10));
        
        // Initialize input field
        inputField = new TextField();
        inputField.setPromptText(PLACEHOLDER_NORMAL);
        inputField.setDisable(true);
        
        // Handle input submission
        inputField.setOnAction(e -> {
            String input = inputField.getText();
            if (!input.isEmpty() && inputEnabled) {
                // Echo input to output area
                appendOutput("> " + input + "\n");
                
                // Add to queue for process
                inputQueue.offer(input);
                
                // Clear input field
                inputField.clear();
                
                // Reset idle detection
                resetIdleDetection();
            }
        });
        
        // Handle typing - hide hint when user starts typing
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                hideWaitingIndicators();
            }
        });
        
        // Apply initial theme
        applyTheme(darkMode);
        
        // Add components
        getChildren().addAll(outputArea, hintLabel, inputField);
        
        // Initialize idle detection timer
        initializeIdleDetection();
    }
    
    /**
     * Append text to the output area
     */
    public void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText(text);
            outputArea.setScrollTop(Double.MAX_VALUE);
            
            // Reset idle detection when new output arrives
            resetIdleDetection();
        });
    }
    
    /**
     * Clear all output
     */
    public void clear() {
        Platform.runLater(() -> {
            outputArea.clear();
            inputField.clear();
        });
    }
    
    /**
     * Set the output text (replaces all content)
     */
    public void setText(String text) {
        Platform.runLater(() -> {
            outputArea.setText(text);
        });
    }
    
    /**
     * Get current output text
     */
    public String getText() {
        return outputArea.getText();
    }
    
    /**
     * Enable or disable input field
     */
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
        Platform.runLater(() -> {
            inputField.setDisable(!enabled);
            if (enabled) {
                inputField.setPromptText(PLACEHOLDER_NORMAL);
                inputField.requestFocus();
                startIdleDetection();
            } else {
                stopIdleDetection();
                hideWaitingIndicators();
                inputField.setPromptText(PLACEHOLDER_NORMAL);
            }
        });
    }
    
    /**
     * Check if input is enabled
     */
    public boolean isInputEnabled() {
        return inputEnabled;
    }
    
    /**
     * Get the input queue for process communication
     */
    public BlockingQueue<String> getInputQueue() {
        return inputQueue;
    }
    
    /**
     * Apply theme (light or dark mode)
     */
    public void applyTheme(boolean darkMode) {
        this.isDarkMode = darkMode;
        
        if (darkMode) {
            // Dark mode styling
            outputArea.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 12px; " +
                "-fx-control-inner-background: #1e1e1e; " +
                "-fx-text-fill: #d4d4d4; " +
                "-fx-background-color: #1e1e1e;"
            );
            
            inputField.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 12px; " +
                "-fx-background-color: #2d2d2d; " +
                "-fx-text-fill: #d4d4d4; " +
                "-fx-prompt-text-fill: #808080; " +
                "-fx-border-color: #3e3e3e; " +
                "-fx-border-width: 1 0 0 0; " +
                "-fx-padding: 8 10;"
            );
            
            hintLabel.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 11px; " +
                "-fx-background-color: #3a3a00; " +
                "-fx-text-fill: #ffd700; " +
                "-fx-border-color: #ffc107; " +
                "-fx-border-width: 1 0 1 0;"
            );
        } else {
            // Light mode styling
            outputArea.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 12px; " +
                "-fx-control-inner-background: #ffffff; " +
                "-fx-text-fill: #000000; " +
                "-fx-background-color: #ffffff;"
            );
            
            inputField.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 12px; " +
                "-fx-background-color: #f8f9fa; " +
                "-fx-text-fill: #000000; " +
                "-fx-prompt-text-fill: #6c757d; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1 0 0 0; " +
                "-fx-padding: 8 10;"
            );
            
            hintLabel.setStyle(
                "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
                "-fx-font-size: 11px; " +
                "-fx-background-color: #fffef0; " +
                "-fx-text-fill: #856404; " +
                "-fx-border-color: #ffc107; " +
                "-fx-border-width: 1 0 1 0;"
            );
        }
    }
    
    /**
     * Toggle between light and dark mode
     */
    public void toggleTheme() {
        applyTheme(!isDarkMode);
    }
    
    /**
     * Check if dark mode is active
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    /**
     * Set scroll position to bottom
     */
    public void scrollToBottom() {
        Platform.runLater(() -> {
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    /**
     * Focus the input field
     */
    public void focusInput() {
        Platform.runLater(() -> {
            if (!inputField.isDisabled()) {
                inputField.requestFocus();
            }
        });
    }
    
    /**
     * Initialize idle detection timer
     */
    private void initializeIdleDetection() {
        // Check every second if we should show waiting indicators
        idleTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> checkIdleState()));
        idleTimer.setCycleCount(Timeline.INDEFINITE);
    }
    
    /**
     * Start idle detection
     */
    private void startIdleDetection() {
        if (idleTimer != null) {
            lastOutputTime = System.currentTimeMillis();
            idleTimer.play();
        }
    }
    
    /**
     * Stop idle detection
     */
    private void stopIdleDetection() {
        if (idleTimer != null) {
            idleTimer.stop();
        }
        hideWaitingIndicators();
    }
    
    /**
     * Reset idle detection (called when new output arrives or input is sent)
     */
    private void resetIdleDetection() {
        lastOutputTime = System.currentTimeMillis();
        hideWaitingIndicators();
    }
    
    /**
     * Check if program appears to be waiting for input
     */
    private void checkIdleState() {
        if (!inputEnabled || inputField.isDisabled()) {
            return;
        }
        
        long idleTime = System.currentTimeMillis() - lastOutputTime;
        
        if (idleTime >= IDLE_THRESHOLD_MS) {
            showWaitingIndicators();
        }
    }
    
    /**
     * Show visual indicators that program is waiting for input
     */
    private void showWaitingIndicators() {
        Platform.runLater(() -> {
            // Change placeholder text
            inputField.setPromptText(PLACEHOLDER_WAITING);
            
            // Show hint label
            hintLabel.setText("💡 Program is waiting for input. Type below and press Enter to continue.");
            hintLabel.setVisible(true);
            hintLabel.setManaged(true);
            
            // Start pulsing border animation
            startPulseAnimation();
        });
    }
    
    /**
     * Hide waiting indicators
     */
    private void hideWaitingIndicators() {
        Platform.runLater(() -> {
            // Reset placeholder text
            if (inputEnabled) {
                inputField.setPromptText(PLACEHOLDER_NORMAL);
            }
            
            // Hide hint label
            hintLabel.setVisible(false);
            hintLabel.setManaged(false);
            
            // Stop pulsing animation
            stopPulseAnimation();
        });
    }
    
    /**
     * Start pulsing border animation on input field
     */
    private void startPulseAnimation() {
        if (pulseAnimation != null) {
            pulseAnimation.stop();
        }
        
        pulseAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                String baseStyle = inputField.getStyle();
                String newStyle = baseStyle.replaceAll("-fx-border-color: [^;]+;", "-fx-border-color: #ffc107;")
                                          .replaceAll("-fx-border-width: [^;]+;", "-fx-border-width: 2 0 0 0;");
                inputField.setStyle(newStyle);
            }),
            new KeyFrame(Duration.seconds(1), e -> {
                String baseStyle = inputField.getStyle();
                String newStyle = baseStyle.replaceAll("-fx-border-color: [^;]+;", "-fx-border-color: #ff9800;")
                                          .replaceAll("-fx-border-width: [^;]+;", "-fx-border-width: 2 0 0 0;");
                inputField.setStyle(newStyle);
            })
        );
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.setAutoReverse(true);
        pulseAnimation.play();
    }
    
    /**
     * Stop pulsing border animation
     */
    private void stopPulseAnimation() {
        if (pulseAnimation != null) {
            pulseAnimation.stop();
            // Reset to normal border
            applyTheme(isDarkMode);
        }
    }
}
