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
    GREEDY("Greedy Algorithms", "Make locally optimal choices for global optimization", FontAwesomeSolid.COINS),
    DYNAMIC_PROGRAMMING("Dynamic Programming", "Solve problems by breaking them into overlapping subproblems",
            FontAwesomeSolid.TABLE),
    BACKTRACKING("Backtracking", "Solve constraint satisfaction problems", FontAwesomeSolid.UNDO),
    PATHFINDING("Pathfinding", "Find optimal paths in grids and graphs", MaterialDesignM.MAP_MARKER_PATH),
    REAL_WORLD("Real-World Problems", "Practical applications and puzzles", FontAwesomeSolid.PUZZLE_PIECE),
    PRACTICAL("Practical Applications", "Real-world problem solving and applications", MaterialDesignC.COGS),
    CONCEPTS("Concepts", "Data structure fundamentals: Stack, Queue, Linked List", FontAwesomeSolid.LAYER_GROUP),
    MATHEMATICAL("Mathematical Algorithms", "Mathematical and numerical algorithms", FontAwesomeSolid.CALCULATOR);

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
        if (category == null)
            return ALL;

        String lower = category.toLowerCase().trim();
        switch (lower) {
            case "searching":
                return SEARCHING;
            case "sorting":
                return SORTING;
            case "graph traversal":
            case "graph algorithms":
            case "graph":
                return GRAPH;
            case "greedy":
            case "greedy algorithms":
                return GREEDY;
            case "dynamic programming":
            case "dp":
                return DYNAMIC_PROGRAMMING;
            case "backtracking":
                return BACKTRACKING;
            case "pathfinding":
                return PATHFINDING;
            case "real-world":
            case "real-world problems":
                return REAL_WORLD;
            case "practical":
            case "practical applications":
                return PRACTICAL;
            case "concepts":
            case "data structures":
            case "data structure concepts":
                return CONCEPTS;
            case "mathematical":
            case "mathematical algorithms":
                return MATHEMATICAL;
            default:
                return ALL;
        }
    }
}
