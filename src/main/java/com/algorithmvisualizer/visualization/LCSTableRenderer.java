package com.algorithmvisualizer.visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Renderer for LCS DP table visualization.
 * Displays a 2D grid with cell values, colors, and arrows.
 */
public class LCSTableRenderer {
    
    private Canvas canvas;
    private String str1;
    private String str2;
    private int rows, cols;
    private int[][] table;
    
    // Cell highlighting
    private Map<String, Color> cellColors;
    private Map<String, String> cellArrows;  // Arrow direction: "↖", "↑", "←"
    private Set<String> pathCells;
    
    // Visual constants
    private static final double CELL_SIZE = 50;
    private static final double HEADER_SIZE = 40;
    private static final double PADDING = 20;
    
    // Colors
    private static final Color COLOR_UNPROCESSED = Color.WHITE;
    private static final Color COLOR_CURRENT = Color.rgb(255, 235, 59);  // Yellow
    private static final Color COLOR_COMPARING = Color.rgb(144, 202, 249);  // Light blue
    private static final Color COLOR_MATCH = Color.rgb(76, 175, 80);  // Green
    private static final Color COLOR_DEPENDENCY = Color.rgb(179, 229, 252);  // Sky blue
    private static final Color COLOR_PATH = Color.rgb(255, 183, 77);  // Gold
    private static final Color COLOR_COMPLETE = Color.rgb(165, 214, 167);  // Pale green
    private static final Color COLOR_GRID = Color.rgb(189, 189, 189);
    private static final Color COLOR_TEXT = Color.rgb(33, 33, 33);
    
    public LCSTableRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.cellColors = new HashMap<>();
        this.cellArrows = new HashMap<>();
    }
    
    public void setStrings(String s1, String s2) {
        this.str1 = s1;
        this.str2 = s2;
        this.rows = s1.length() + 1;
        this.cols = s2.length() + 1;
        this.table = new int[rows][cols];
        
        // Resize canvas
        double width = PADDING * 2 + HEADER_SIZE + cols * CELL_SIZE;
        double height = PADDING * 2 + HEADER_SIZE + rows * CELL_SIZE;
        canvas.setWidth(width);
        canvas.setHeight(height);
        
        clearHighlights();
    }
    
    public void setTable(int[][] dp) {
        this.table = dp;
    }
    
    public void highlightCell(int i, int j, Color color) {
        cellColors.put(i + "," + j, color);
    }
    
    public void setArrow(int i, int j, String direction) {
        cellArrows.put(i + "," + j, direction);
    }
    
    public void setPathCells(Set<String> path) {
        this.pathCells = path;
    }
    
    public void clearHighlights() {
        cellColors.clear();
        cellArrows.clear();
    }
    
    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Clear canvas
        gc.setFill(Color.rgb(250, 250, 250));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw grid
        drawGrid(gc);
        
        // Draw headers
        drawHeaders(gc);
        
        // Draw cell values
        drawCellValues(gc);
        
        // Draw arrows
        drawArrows(gc);
    }
    
    private void drawGrid(GraphicsContext gc) {
        if (table == null || rows == 0 || cols == 0) return;
        
        gc.setStroke(COLOR_GRID);
        gc.setLineWidth(1.0);
        
        double startX = PADDING + HEADER_SIZE;
        double startY = PADDING + HEADER_SIZE;
        
        // Draw cells with colors
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = startX + j * CELL_SIZE;
                double y = startY + i * CELL_SIZE;
                
                // Determine cell color
                String key = i + "," + j;
                Color fillColor = COLOR_UNPROCESSED;
                
                if (pathCells != null && pathCells.contains(key)) {
                    fillColor = COLOR_PATH;
                } else if (cellColors.containsKey(key)) {
                    fillColor = cellColors.get(key);
                } else if (table[i][j] > 0) {
                    // Gradient based on value
                    int maxVal = Math.max(str1.length(), str2.length());
                    double intensity = (double) table[i][j] / maxVal;
                    fillColor = Color.rgb(
                        (int)(200 + 55 * (1 - intensity)),
                        (int)(230 - 30 * intensity),
                        (int)(201 - 40 * intensity)
                    );
                }
                
                gc.setFill(fillColor);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }
    
    private void drawHeaders(GraphicsContext gc) {
        if (str1 == null || str2 == null) return;
        
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setFill(COLOR_TEXT);
        gc.setTextAlign(TextAlignment.CENTER);
        
        double startX = PADDING + HEADER_SIZE;
        double startY = PADDING + HEADER_SIZE;
        
        // Top header (str2)
        gc.setFill(Color.rgb(230, 230, 230));
        gc.fillRect(startX, PADDING, cols * CELL_SIZE, HEADER_SIZE);
        gc.setFill(COLOR_TEXT);
        
        // Empty string header
        gc.fillText("\"\"", startX + CELL_SIZE / 2, PADDING + HEADER_SIZE / 2 + 5);
        
        for (int j = 0; j < str2.length(); j++) {
            double x = startX + (j + 1) * CELL_SIZE + CELL_SIZE / 2;
            gc.fillText(String.valueOf(str2.charAt(j)), x, PADDING + HEADER_SIZE / 2 + 5);
        }
        
        // Left header (str1)
        gc.setFill(Color.rgb(230, 230, 230));
        gc.fillRect(PADDING, startY, HEADER_SIZE, rows * CELL_SIZE);
        gc.setFill(COLOR_TEXT);
        
        // Empty string header
        gc.fillText("\"\"", PADDING + HEADER_SIZE / 2, startY + CELL_SIZE / 2 + 5);
        
        for (int i = 0; i < str1.length(); i++) {
            double y = startY + (i + 1) * CELL_SIZE + CELL_SIZE / 2 + 5;
            gc.fillText(String.valueOf(str1.charAt(i)), PADDING + HEADER_SIZE / 2, y);
        }
    }
    
    private void drawCellValues(GraphicsContext gc) {
        if (table == null || rows == 0 || cols == 0) return;
        
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(COLOR_TEXT);
        gc.setTextAlign(TextAlignment.CENTER);
        
        double startX = PADDING + HEADER_SIZE;
        double startY = PADDING + HEADER_SIZE;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = startX + j * CELL_SIZE + CELL_SIZE / 2;
                double y = startY + i * CELL_SIZE + CELL_SIZE / 2 + 6;
                
                gc.fillText(String.valueOf(table[i][j]), x, y);
            }
        }
    }
    
    private void drawArrows(GraphicsContext gc) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(Color.rgb(100, 100, 100));
        gc.setTextAlign(TextAlignment.CENTER);
        
        double startX = PADDING + HEADER_SIZE;
        double startY = PADDING + HEADER_SIZE;
        
        for (Map.Entry<String, String> entry : cellArrows.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int i = Integer.parseInt(parts[0]);
            int j = Integer.parseInt(parts[1]);
            String arrow = entry.getValue();
            
            double x = startX + j * CELL_SIZE + CELL_SIZE / 2;
            double y = startY + i * CELL_SIZE + CELL_SIZE * 0.75;
            
            gc.fillText(arrow, x, y);
        }
    }
    
    public Canvas getCanvas() {
        return canvas;
    }
}
