package com.algorithmvisualizer.model;

/**
 * Abstract base class for all algorithms that can be visualized
 */
public abstract class Algorithm {
    
    protected String name;
    protected String description;
    protected String category;
    
    public Algorithm(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Get the FXML file path for this algorithm's visualization
     */
    public abstract String getVisualizationFxml();
    
    /**
     * Get the controller class for this algorithm's visualization
     */
    public abstract Class<?> getControllerClass();
}
