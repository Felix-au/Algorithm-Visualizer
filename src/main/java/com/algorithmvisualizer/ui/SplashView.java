package com.algorithmvisualizer.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Handles the display of the video splash screen upon application launch.
 */
public class SplashView {

    private final Stage stage;
    private final Runnable onFinished;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private boolean finishedCalled = false;

    public SplashView(Stage stage, Runnable onFinished) {
        this.stage = stage;
        this.onFinished = onFinished;
    }

    public void show() {
        try {
            URL videoUrl = getClass().getResource("/AlgoBuddy_logo_reveal_animation.mp4");
            if (videoUrl == null) {
                System.err.println("Splash screen video resource not found.");
                finish();
                return;
            }

            // Create media objects
            Media media = new Media(videoUrl.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);

            // Configure layout
            StackPane root = new StackPane(mediaView);
            root.setStyle("-fx-background-color: #ffffff;");

            Scene scene = new Scene(root);
            
            // Bind size dynamically to fit (60% of screen size) instead of fill
            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(scene.widthProperty().multiply(0.6));
            mediaView.fitHeightProperty().bind(scene.heightProperty().multiply(0.6));

            // Handle media lifecycle
            mediaPlayer.setOnEndOfMedia(this::finish);
            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer error: " + mediaPlayer.getError());
                finish();
            });

            // Handle skips on clicks or keys
            root.setOnMouseClicked(event -> finish());
            scene.setOnKeyPressed(event -> finish());

            // Show stage and start playback
            stage.setScene(scene);
            mediaPlayer.play();

        } catch (Throwable e) {
            System.err.println("Failed to initialize splash video: " + e.getMessage());
            e.printStackTrace();
            finish();
        }
    }

    private synchronized void finish() {
        if (finishedCalled) {
            return;
        }
        finishedCalled = true;

        if (mediaView != null) {
            // Fade out the mediaView to white (since parent StackPane background is white)
            Platform.runLater(() -> {
                try {
                    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(300), mediaView
                    );
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(evt -> {
                        stopAndDisposePlayer();
                        
                        // Wait a brief moment on solid white, then transition
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                            javafx.util.Duration.millis(200)
                        );
                        pause.setOnFinished(pe -> onFinished.run());
                        pause.play();
                    });
                    fadeOut.play();
                } catch (Exception e) {
                    // Fallback on transition error
                    stopAndDisposePlayer();
                    onFinished.run();
                }
            });
        } else {
            stopAndDisposePlayer();
            Platform.runLater(onFinished);
        }
    }

    private void stopAndDisposePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception ignore) {}
        }
    }
}
