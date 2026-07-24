package com.algorithmvisualizer.visualization;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Overlay component for displaying sliding pointer indicators (i, j, pivot, etc.)
 * above or below visualization elements like bars or array boxes.
 */
public class PointerOverlay {
    
    /**
     * Individual pointer indicator with arrow and label
     */
    public static class PointerIndicator {
        private final Group visual;
        private final Polygon arrow;
        private final StackPane labelBox;
        private final Label label;
        private int currentIndex;
        private final Color color;
        private final boolean positionBelow;
        private TranslateTransition activeTransition;
        
        public PointerIndicator(String name, Color color, boolean positionBelow) {
            this.color = color;
            this.positionBelow = positionBelow;
            this.currentIndex = -1;
            
            // Create arrow (triangle)
            arrow = new Polygon();
            if (positionBelow) {
                // Upward pointing arrow
                arrow.getPoints().addAll(
                    0.0, 0.0,      // Top point
                    -7.0, 12.0,    // Bottom left
                    7.0, 12.0      // Bottom right
                );
            } else {
                // Downward pointing arrow
                arrow.getPoints().addAll(
                    0.0, 15.0,     // Bottom point
                    -7.0, 0.0,     // Top left
                    7.0, 0.0       // Top right
                );
            }
            arrow.setFill(color);
            arrow.setStroke(Color.BLACK);
            arrow.setStrokeWidth(1.5);
            
            // Create label box
            labelBox = new StackPane();
            labelBox.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-border-color: black; " +
                "-fx-border-width: 2; " +
                "-fx-background-radius: 4; " +
                "-fx-border-radius: 4; " +
                "-fx-padding: 3 12;",
                toHexString(color)
            ));
            labelBox.setMinWidth(60);
            labelBox.setMinHeight(26);
            labelBox.setAlignment(Pos.CENTER);
            
            // Create label text
            label = new Label(name);
            label.setStyle(String.format(
                "-fx-font-family: 'Courier New'; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: %s;",
                isLightColor(color) ? "black" : "white"
            ));
            labelBox.getChildren().add(label);
            
            // Position label relative to arrow
            if (positionBelow) {
                labelBox.setTranslateY(15);  // Below arrow
            } else {
                labelBox.setTranslateY(-29); // Above arrow
            }
            
            // Group arrow and label
            visual = new Group(arrow, labelBox);
            visual.setVisible(false);
        }
        
        public Group getVisual() {
            return visual;
        }
        
        public void setPosition(double x, double y, boolean animate) {
            if (animate && visual.isVisible()) {
                // Cancel any active transition
                if (activeTransition != null) {
                    activeTransition.stop();
                }
                
                // Animate to new position
                activeTransition = new TranslateTransition(Duration.millis(400), visual);
                activeTransition.setToX(x);
                activeTransition.setToY(y);
                activeTransition.setInterpolator(Interpolator.EASE_BOTH);
                activeTransition.setOnFinished(e -> activeTransition = null);
                activeTransition.play();
            } else {
                // Instant position change
                visual.setTranslateX(x);
                visual.setTranslateY(y);
            }
        }
        
        public void show() {
            visual.setVisible(true);
        }
        
        public void hide() {
            visual.setVisible(false);
            if (activeTransition != null) {
                activeTransition.stop();
                activeTransition = null;
            }
        }
        
        public boolean isVisible() {
            return visual.isVisible();
        }
        
        public int getCurrentIndex() {
            return currentIndex;
        }
        
        public void setCurrentIndex(int index) {
            this.currentIndex = index;
        }
        
        public void cancelAnimation() {
            if (activeTransition != null) {
                activeTransition.stop();
                activeTransition = null;
            }
        }
        
        private String toHexString(Color color) {
            return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
            );
        }
        
        private boolean isLightColor(Color color) {
            // Calculate perceived brightness
            double brightness = (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000;
            return brightness > 0.5;
        }
    }
    
    private final Pane container;
    private final Map<String, PointerIndicator> pointers;
    private PositionCalculator positionCalculator;
    
    /**
     * Interface for calculating element positions
     */
    public interface PositionCalculator {
        double getElementX(int index);
        double getElementY(int index);
        double getElementWidth(int index);
    }
    
    public PointerOverlay() {
        container = new Pane();
        container.setMouseTransparent(true); // Allow clicks to pass through
        pointers = new HashMap<>();
    }
    
    public Pane getContainer() {
        return container;
    }
    
    public void setPositionCalculator(PositionCalculator calculator) {
        this.positionCalculator = calculator;
    }
    
    /**
     * Create and show a pointer at the specified index
     */
    public void showPointer(String name, int index, Color color, boolean positionBelow) {
        if (positionCalculator == null) {
            System.err.println("PointerOverlay: PositionCalculator not set!");
            return;
        }
        
        PointerIndicator pointer = pointers.get(name);
        if (pointer == null) {
            // Create new pointer
            pointer = new PointerIndicator(name, color, positionBelow);
            pointers.put(name, pointer);
            container.getChildren().add(pointer.getVisual());
        }
        
        // Calculate position
        double x = positionCalculator.getElementX(index) + positionCalculator.getElementWidth(index) / 2;
        double y = positionCalculator.getElementY(index);
        if (positionBelow) {
            y += 10; // Offset below element
        } else {
            y -= 5; // Offset above element
        }
        
        pointer.setCurrentIndex(index);
        pointer.setPosition(x, y, false);
        pointer.show();
    }
    
    /**
     * Move pointer to new index with optional animation
     */
    public void setPointerPosition(String name, int index, boolean animate) {
        if (positionCalculator == null) {
            System.err.println("PointerOverlay: PositionCalculator not set!");
            return;
        }
        
        PointerIndicator pointer = pointers.get(name);
        if (pointer == null || !pointer.isVisible()) {
            return;
        }
        
        // Calculate new position
        double x = positionCalculator.getElementX(index) + positionCalculator.getElementWidth(index) / 2;
        double y = positionCalculator.getElementY(index);
        if (pointer.positionBelow) {
            y += 10;
        } else {
            y -= 5;
        }
        
        pointer.setCurrentIndex(index);
        pointer.setPosition(x, y, animate);
    }
    
    /**
     * Hide a specific pointer
     */
    public void hidePointer(String name) {
        PointerIndicator pointer = pointers.get(name);
        if (pointer != null) {
            pointer.hide();
        }
    }
    
    /**
     * Hide all pointers
     */
    public void clearAllPointers() {
        for (PointerIndicator pointer : pointers.values()) {
            pointer.hide();
        }
    }
    
    /**
     * Check if a pointer is visible
     */
    public boolean isPointerVisible(String name) {
        PointerIndicator pointer = pointers.get(name);
        return pointer != null && pointer.isVisible();
    }
    
    /**
     * Get current index of a pointer
     */
    public int getPointerIndex(String name) {
        PointerIndicator pointer = pointers.get(name);
        return pointer != null ? pointer.getCurrentIndex() : -1;
    }
    
    /**
     * Cancel all active animations
     */
    public void cancelAllAnimations() {
        for (PointerIndicator pointer : pointers.values()) {
            pointer.cancelAnimation();
        }
    }
    
    /**
     * Update all visible pointer positions (call after resize or data change)
     */
    public void updateAllPositions() {
        if (positionCalculator == null) return;
        
        for (PointerIndicator pointer : pointers.values()) {
            if (pointer.isVisible()) {
                int index = pointer.getCurrentIndex();
                if (index >= 0) {
                    double x = positionCalculator.getElementX(index) + positionCalculator.getElementWidth(index) / 2;
                    double y = positionCalculator.getElementY(index);
                    if (pointer.positionBelow) {
                        y += 10;
                    } else {
                        y -= 5;
                    }
                    pointer.setPosition(x, y, false);
                }
            }
        }
    }
}
