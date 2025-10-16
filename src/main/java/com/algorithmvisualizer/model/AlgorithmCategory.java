package com.algorithmvisualizer.model;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignG;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

/**
 * Enumeration of algorithm categories for better organization
 */
public enum AlgorithmCategory {
    ALL("All Algorithms", "View all available algorithms", FontAwesomeSolid.TH_LARGE),
    SEARCHING("Searching", "Find elements in data structures", FontAwesomeSolid.SEARCH),
    SORTING("Sorting", "Arrange elements in order", MaterialDesignS.SORT_ASCENDING),
    GRAPH("Graph Algorithms", "Traverse and analyze graphs", MaterialDesignG.GRAPH_OUTLINE),
    BACKTRACKING("Backtracking", "Solve constraint satisfaction problems", FontAwesomeSolid.UNDO),
    PATHFINDING("Pathfinding", "Find optimal paths in grids and graphs", MaterialDesignM.MAP_MARKER_PATH),
    REAL_WORLD("Real-World Problems", "Practical applications and puzzles", FontAwesomeSolid.PUZZLE_PIECE),
    PRACTICAL("Practical Applications", "Real-world problem solving and applications", MaterialDesignC.COGS);
    
    private final String displayName;
    private final String description;
    private final Ikon icon;
    
    AlgorithmCategory(String displayName, String description, Ikon icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Ikon getIcon() {
        return icon;
    }
    
    /**
     * Get category from string name (case-insensitive)
     */
    public static AlgorithmCategory fromString(String category) {
        if (category == null) return ALL;
        
        String lower = category.toLowerCase().trim();
        switch (lower) {
            case "searching": return SEARCHING;
            case "sorting": return SORTING;
            case "graph traversal":
            case "graph algorithms":
            case "graph": return GRAPH;
            case "backtracking": return BACKTRACKING;
            case "pathfinding": return PATHFINDING;
            case "real-world":
            case "real-world problems": return REAL_WORLD;
            case "practical":
            case "practical applications": return PRACTICAL;
            default: return ALL;
        }
    }
}
