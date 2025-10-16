package com.algorithmvisualizer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for all algorithms that can be visualized
 * Supports multiple categories/tags per algorithm
 */
public abstract class Algorithm {
    
    protected String name;
    protected String description;
    protected String category;  // Primary category (for backward compatibility)
    protected List<String> tags; // Multiple categories/tags
    
    public Algorithm(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.tags = new ArrayList<>();
        this.tags.add(category);
    }
    
    public Algorithm(String name, String description, String... categories) {
        this.name = name;
        this.description = description;
        this.category = categories.length > 0 ? categories[0] : "General";
        this.tags = new ArrayList<>(Arrays.asList(categories));
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
    
    /**
     * Get all category tags for this algorithm
     */
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }
    
    /**
     * Check if this algorithm belongs to a specific category
     */
    public boolean hasCategory(String categoryName) {
        return tags.stream()
            .anyMatch(tag -> tag.equalsIgnoreCase(categoryName.trim()));
    }
    
    /**
     * Check if this algorithm belongs to a specific category enum
     */
    public boolean hasCategory(AlgorithmCategory category) {
        return hasCategory(category.getDisplayName()) || 
               tags.stream().anyMatch(tag -> 
                   AlgorithmCategory.fromString(tag) == category
               );
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
