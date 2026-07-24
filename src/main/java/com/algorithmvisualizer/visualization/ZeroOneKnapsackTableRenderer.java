package com.algorithmvisualizer.visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Renderer for 0/1 Knapsack DP table visualization.
 * Displays a 2D grid where:
 * - Rows = items (0..n)
 * - Columns = capacities (0..W)
 * Supports cell highlighting, arrows for traceback, and path marking.
 */
public class ZeroOneKnapsackTableRenderer {

    private Canvas canvas;
    private int numItems; // n
    private int capacity; // W
    private int[][] table;
    private int[] weights; // item weights for header
    private int[] values; // item values for header

    // Cell highlighting
    private Map<String, Color> cellColors;
    private Map<String, String> cellArrows;
    private Map<String, String> pathCells; // key="i,w", value="start"|"checking"|"selected"|"skipped"
    private Set<Integer> highlightedRows; // Row header highlights for selected items

    // Visual constants
    private static final double CELL_SIZE = 48;
    private static final double HEADER_WIDTH = 80;
    private static final double HEADER_HEIGHT = 35;
    private static final double PADDING = 15;

    // Colors
    private static final Color COLOR_UNPROCESSED = Color.WHITE;
    private static final Color COLOR_GRID = Color.rgb(189, 189, 189);
    private static final Color COLOR_TEXT = Color.rgb(33, 33, 33);

    public ZeroOneKnapsackTableRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.cellColors = new HashMap<>();
        this.cellArrows = new HashMap<>();
        this.highlightedRows = new HashSet<>();
    }

    public void setParameters(int numItems, int capacity, int[] weights, int[] values) {
        this.numItems = numItems;
        this.capacity = capacity;
        this.weights = weights;
        this.values = values;
        this.table = new int[numItems + 1][capacity + 1];

        // Resize canvas
        double width = PADDING * 2 + HEADER_WIDTH + (capacity + 1) * CELL_SIZE;
        double height = PADDING * 2 + HEADER_HEIGHT + (numItems + 1) * CELL_SIZE;
        canvas.setWidth(Math.max(width, 400));
        canvas.setHeight(Math.max(height, 300));

        clearHighlights();
    }

    public void setTable(int[][] dp) {
        this.table = dp;
    }

    public void highlightCell(int i, int w, Color color) {
        cellColors.put(i + "," + w, color);
    }

    public void setArrow(int i, int w, String direction) {
        cellArrows.put(i + "," + w, direction);
    }

    public void setPathCells(Map<String, String> path) {
        this.pathCells = path;
    }

    public void highlightRowHeader(int row) {
        highlightedRows.add(row);
    }

    public void clearHighlights() {
        cellColors.clear();
        cellArrows.clear();
    }

    public void clearAll() {
        clearHighlights();
        highlightedRows.clear();
    }

    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear canvas
        gc.setFill(Color.rgb(250, 250, 250));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (table == null || numItems == 0)
            return;

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
        int rows = numItems + 1;
        int cols = capacity + 1;

        gc.setStroke(COLOR_GRID);
        gc.setLineWidth(1.0);

        double startX = PADDING + HEADER_WIDTH;
        double startY = PADDING + HEADER_HEIGHT;

        // Draw cells with colors
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = startX + j * CELL_SIZE;
                double y = startY + i * CELL_SIZE;

                // Determine cell color
                String key = i + "," + j;
                Color fillColor = COLOR_UNPROCESSED;

                if (pathCells != null && pathCells.containsKey(key)) {
                    String tag = pathCells.get(key);
                    switch (tag) {
                        case "selected":
                            fillColor = Color.rgb(0, 200, 83); // Bright green for selected
                            break;
                        case "skipped":
                            fillColor = Color.rgb(244, 67, 54); // Bright red for skipped
                            break;
                        default: // "start", "checking"
                            fillColor = Color.rgb(255, 235, 59); // Yellow
                            break;
                    }
                } else if (cellColors.containsKey(key)) {
                    fillColor = cellColors.get(key);
                } else if (table != null && i < table.length && j < table[i].length && table[i][j] > 0) {
                    // Gradient based on value
                    int maxVal = 1;
                    for (int[] row : table) {
                        for (int val : row) {
                            maxVal = Math.max(maxVal, val);
                        }
                    }
                    double intensity = (double) table[i][j] / maxVal;
                    fillColor = Color.rgb(
                            (int) (200 + 55 * (1 - intensity)),
                            (int) (230 - 30 * intensity),
                            (int) (201 - 40 * intensity));
                }

                gc.setFill(fillColor);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawHeaders(GraphicsContext gc) {
        int cols = capacity + 1;

        double startX = PADDING + HEADER_WIDTH;
        double startY = PADDING + HEADER_HEIGHT;

        // Top header — capacity values (0..W)
        gc.setFill(Color.rgb(230, 230, 230));
        gc.fillRect(startX, PADDING, cols * CELL_SIZE, HEADER_HEIGHT);
        gc.setFill(COLOR_TEXT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.CENTER);

        for (int j = 0; j <= capacity; j++) {
            double x = startX + j * CELL_SIZE + CELL_SIZE / 2;
            gc.fillText(String.valueOf(j), x, PADDING + HEADER_HEIGHT / 2 + 5);
        }

        // "w→" label
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setFill(Color.rgb(100, 100, 100));
        gc.fillText("w →", PADDING + HEADER_WIDTH / 2, PADDING + HEADER_HEIGHT / 2 + 5);

        // Left header — item info
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        // Row 0 = "∅" (no items) — default header color
        gc.setFill(Color.rgb(230, 230, 230));
        gc.fillRect(PADDING, startY, HEADER_WIDTH, CELL_SIZE);
        gc.setStroke(COLOR_GRID);
        gc.strokeRect(PADDING, startY, HEADER_WIDTH, CELL_SIZE);
        gc.setFill(COLOR_TEXT);
        gc.fillText("∅", PADDING + HEADER_WIDTH / 2, startY + CELL_SIZE / 2 + 4);

        // Item rows — highlight if selected
        for (int i = 0; i < numItems; i++) {
            double hy = startY + (i + 1) * CELL_SIZE;
            boolean isHighlighted = highlightedRows.contains(i + 1);

            if (isHighlighted) {
                gc.setFill(Color.rgb(76, 175, 80, 0.3)); // Green tint for selected
                gc.fillRect(PADDING, hy, HEADER_WIDTH, CELL_SIZE);
                gc.setStroke(Color.rgb(76, 175, 80));
                gc.setLineWidth(2.5);
                gc.strokeRect(PADDING, hy, HEADER_WIDTH, CELL_SIZE);
                gc.setLineWidth(1.0);
            } else {
                gc.setFill(Color.rgb(230, 230, 230));
                gc.fillRect(PADDING, hy, HEADER_WIDTH, CELL_SIZE);
                gc.setStroke(COLOR_GRID);
                gc.strokeRect(PADDING, hy, HEADER_WIDTH, CELL_SIZE);
            }

            double y = hy + CELL_SIZE / 2;
            String label;
            if (weights != null && values != null && i < weights.length && i < values.length) {
                label = "I" + (i + 1) + " w=" + weights[i] + " v=" + values[i];
            } else {
                label = "Item " + (i + 1);
            }
            gc.setFill(isHighlighted ? Color.rgb(27, 94, 32) : COLOR_TEXT);
            gc.setFont(Font.font("Arial", isHighlighted ? FontWeight.BOLD : FontWeight.NORMAL, 10));
            gc.fillText(label, PADDING + HEADER_WIDTH / 2, y + 4);
        }
    }

    private void drawCellValues(GraphicsContext gc) {
        if (table == null)
            return;

        int rows = numItems + 1;
        int cols = capacity + 1;

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setFill(COLOR_TEXT);
        gc.setTextAlign(TextAlignment.CENTER);

        double startX = PADDING + HEADER_WIDTH;
        double startY = PADDING + HEADER_HEIGHT;

        for (int i = 0; i < rows && i < table.length; i++) {
            for (int j = 0; j < cols && j < table[i].length; j++) {
                double x = startX + j * CELL_SIZE + CELL_SIZE / 2;
                double y = startY + i * CELL_SIZE + CELL_SIZE / 2 + 6;

                gc.fillText(String.valueOf(table[i][j]), x, y);
            }
        }
    }

    private void drawArrows(GraphicsContext gc) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setFill(Color.rgb(100, 100, 100));
        gc.setTextAlign(TextAlignment.CENTER);

        double startX = PADDING + HEADER_WIDTH;
        double startY = PADDING + HEADER_HEIGHT;

        for (Map.Entry<String, String> entry : cellArrows.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int i = Integer.parseInt(parts[0]);
            int j = Integer.parseInt(parts[1]);
            String arrow = entry.getValue();

            double x = startX + j * CELL_SIZE + CELL_SIZE / 2;
            double y = startY + i * CELL_SIZE + CELL_SIZE * 0.8;

            gc.fillText(arrow, x, y);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
