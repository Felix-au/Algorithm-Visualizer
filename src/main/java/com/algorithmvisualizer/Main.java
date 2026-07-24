package com.algorithmvisualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

/**
 * Main class for the AlgoBuddy application
 */
public class Main extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        
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
        try { stage.getIcons().add(new Image(Main.class.getResourceAsStream("/Logo.png"))); } catch (Exception ignore) {}
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
