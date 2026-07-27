package com.algorithmvisualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import com.algorithmvisualizer.ui.SplashView;

import java.io.IOException;

/**
 * Main class for the AlgoBuddy application
 */
public class Main extends Application {
    
    @Override
    public void start(Stage stage) {
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
        
        stage.setTitle("AlgoBuddy");
        try { 
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/Logo.png"))); 
        } catch (Exception ignore) {}
        stage.setResizable(true);

        // Load and play the logo reveal animation splash screen first
        SplashView splashView = new SplashView(stage, () -> loadNextScene(stage));
        splashView.show();
        
        stage.show();
    }

    private void loadNextScene(Stage stage) {
        try {
            // Check if all required environment runtimes are available
            com.algorithmvisualizer.execution.ExecutionEnvironment env = new com.algorithmvisualizer.execution.ExecutionEnvironment();
            boolean envsAvailable = env.isLanguageAvailable(com.algorithmvisualizer.execution.LanguageType.JAVA) &&
                                    env.isLanguageAvailable(com.algorithmvisualizer.execution.LanguageType.C) &&
                                    env.isLanguageAvailable(com.algorithmvisualizer.execution.LanguageType.CPP) &&
                                    env.isLanguageAvailable(com.algorithmvisualizer.execution.LanguageType.PYTHON);

            String nextFxml = envsAvailable ? "/fxml/main-view.fxml" : "/fxml/env-installer-view.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(nextFxml));
            javafx.scene.Parent root = fxmlLoader.load();

            // Create a premium fade-in transition from solid white using a StackPane wrapper
            javafx.scene.layout.StackPane rootWrapper = new javafx.scene.layout.StackPane(root);
            javafx.scene.layout.StackPane whiteOverlay = new javafx.scene.layout.StackPane();
            whiteOverlay.setStyle("-fx-background-color: #ffffff;");
            rootWrapper.getChildren().add(whiteOverlay);

            Scene scene = new Scene(rootWrapper);
            stage.setScene(scene);

            // Fade out the white overlay over 700ms to reveal the dashboard contents
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(700), whiteOverlay
            );
            fadeIn.setFromValue(1.0);
            fadeIn.setToValue(0.0);
            fadeIn.setOnFinished(evt -> {
                rootWrapper.getChildren().clear(); // Free root from the wrapper first
                scene.setRoot(root); // Restore the original FXML root directly
            });
            fadeIn.play();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load main or installer view: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
