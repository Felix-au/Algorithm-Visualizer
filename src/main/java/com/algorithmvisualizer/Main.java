package com.algorithmvisualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

/**
 * Main class for the AlgoQuest application
 */
public class Main extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        
        stage.setTitle("AlgoQuest");
        try { stage.getIcons().add(new Image(Main.class.getResourceAsStream("/Logo.png"))); } catch (Exception ignore) {}
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
