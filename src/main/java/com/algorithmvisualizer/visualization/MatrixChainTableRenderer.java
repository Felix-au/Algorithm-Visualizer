package com.algorithmvisualizer.visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;

/**
 * Canvas-based renderer for the Matrix Chain Multiplication DP tables.
 * Draws the cost table m[i][j] (upper-triangular), the dimension array p[],
 * and optionally the split table s[i][j].
 */
public class MatrixChainTableRenderer {

    private Canvas canvas;
    private int n; // number of matrices (1-indexed in tables up to n)
    private int[] dimensions; // p[0..n]
    private int[][] mTable;   // cost table m[1..n][1..n]
    private int[][] sTable;   // split table s[1..n][1..n]

    // Cell highlighting
    private Map<String, Color> cellColors;
    private Map<String, Color> dimColors; // dimension index -> color

    // Visual constants
    private static final double CELL_SIZE = 56;
    private static final double HEADER_SIZE = 36;
    private static final double PADDING = 20;
    private static final double DIM_BOX_SIZE = 40;
    private static final double DIM_SPACING = 6;
    private static final double SECTION_GAP = 30;

    // Colors
    private static final Color COLOR_UNPROCESSED = Color.rgb(245, 245, 245);
    private static final Color COLOR_DIAGONAL = Color.rgb(224, 224, 224);
    private static final Color COLOR_CURRENT = Color.rgb(255, 235, 59);      // Yellow
    private static final Color COLOR_DEPENDENCY = Color.rgb(128, 222, 234);  // Cyan
    private static final Color COLOR_TRYING_SPLIT = Color.rgb(255, 183, 77); // Orange
    private static final Color COLOR_NEW_MIN = Color.rgb(129, 199, 132);     // Light green
    private static final Color COLOR_COMPLETED = Color.rgb(200, 230, 201);   // Pale green
    private static final Color COLOR_TRACEBACK = Color.rgb(186, 104, 200);   // Purple
    private static final Color COLOR_GRID = Color.rgb(189, 189, 189);
    private static final Color COLOR_TEXT = Color.rgb(33, 33, 33);
    private static final Color COLOR_DIM_DEFAULT = Color.rgb(227, 242, 253); // Light blue
    private static final Color COLOR_DIM_ACTIVE = Color.rgb(255, 152, 0);    // Orange

    // Formula text
    private String formulaText = "";

    public MatrixChainTableRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.cellColors = new HashMap<>();
        this.dimColors = new HashMap<>();
    }

    public void setData(int numMatrices, int[] dimensions) {
        this.n = numMatrices;
        this.dimensions = dimensions;
        this.mTable = new int[n + 1][n + 1];
        this.sTable = new int[n + 1][n + 1];

        // Resize canvas
        double tableWidth = HEADER_SIZE + (n + 1) * CELL_SIZE;
        double tableHeight = HEADER_SIZE + (n + 1) * CELL_SIZE;
        double dimRowHeight = DIM_BOX_SIZE + 40; // label + box
        double formulaHeight = 50;
        double totalWidth = PADDING * 2 + tableWidth;
        double totalHeight = PADDING * 2 + tableHeight + SECTION_GAP + dimRowHeight + formulaHeight;
        canvas.setWidth(Math.max(totalWidth, 500));
        canvas.setHeight(Math.max(totalHeight, 400));

        clearHighlights();
    }

    public void setMTable(int[][] m) {
        this.mTable = m;
    }

    public void setSTable(int[][] s) {
        this.sTable = s;
    }

    public void highlightCell(int i, int j, Color color) {
        cellColors.put(i + "," + j, color);
    }

    public void highlightDimension(int index, Color color) {
        dimColors.put(String.valueOf(index), color);
    }

    public void setFormulaText(String text) {
        this.formulaText = text;
    }

    public void clearHighlights() {
        cellColors.clear();
        dimColors.clear();
        formulaText = "";
    }

    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear canvas
        gc.setFill(Color.rgb(250, 250, 250));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (n <= 0) return;

        // Draw cost table title
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(COLOR_TEXT);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Cost Table m[i][j]", PADDING, PADDING - 4);

        // Draw cost table
        drawCostTable(gc);

        // Draw dimension array below the table
        double tableBottom = PADDING + HEADER_SIZE + (n + 1) * CELL_SIZE;
        drawDimensionArray(gc, tableBottom + SECTION_GAP);

        // Draw formula
        if (formulaText != null && !formulaText.isEmpty()) {
            double formulaY = tableBottom + SECTION_GAP + DIM_BOX_SIZE + 30;
            drawFormula(gc, formulaY);
        }
    }

    private void drawCostTable(GraphicsContext gc) {
        double startX = PADDING + HEADER_SIZE;
        double startY = PADDING + HEADER_SIZE;

        gc.setStroke(COLOR_GRID);
        gc.setLineWidth(1.0);

        // Draw headers (A1, A2, ...)
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.CENTER);

        // Column headers
        for (int j = 1; j <= n; j++) {
            double x = startX + (j - 1) * CELL_SIZE + CELL_SIZE / 2;
            gc.setFill(Color.rgb(63, 81, 181)); // Indigo
            gc.fillText("A" + j, x, startY - 8);
        }

        // Row headers
        for (int i = 1; i <= n; i++) {
            double y = startY + (i - 1) * CELL_SIZE + CELL_SIZE / 2 + 5;
            gc.setFill(Color.rgb(63, 81, 181));
            gc.fillText("A" + i, PADDING + HEADER_SIZE / 2, y);
        }

        // Draw cells
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                double x = startX + (j - 1) * CELL_SIZE;
                double y = startY + (i - 1) * CELL_SIZE;

                Color fillColor;
                String key = i + "," + j;

                if (i > j) {
                    // Below diagonal: unused
                    fillColor = Color.rgb(238, 238, 238);
                } else if (i == j) {
                    // Diagonal: always 0
                    fillColor = cellColors.containsKey(key) ? cellColors.get(key) : COLOR_DIAGONAL;
                } else if (cellColors.containsKey(key)) {
                    fillColor = cellColors.get(key);
                } else if (mTable[i][j] > 0) {
                    fillColor = COLOR_COMPLETED;
                } else {
                    fillColor = COLOR_UNPROCESSED;
                }

                // Draw cell background
                gc.setFill(fillColor);
                gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Draw border
                gc.setStroke(COLOR_GRID);
                gc.setLineWidth(1.0);
                gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);

                // Draw value
                if (i <= j) {
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.setFill(COLOR_TEXT);
                    gc.setTextAlign(TextAlignment.CENTER);

                    String value;
                    if (i == j) {
                        value = "0";
                    } else if (mTable[i][j] > 0) {
                        value = formatNumber(mTable[i][j]);
                    } else {
                        value = "—";
                    }
                    gc.fillText(value, x + CELL_SIZE / 2, y + CELL_SIZE / 2 + 5);
                }
            }
        }
    }

    private void drawDimensionArray(GraphicsContext gc, double topY) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setFill(COLOR_TEXT);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Dimensions p[]:", PADDING, topY);

        if (dimensions == null) return;

        double startX = PADDING;
        double boxY = topY + 8;

        for (int i = 0; i < dimensions.length; i++) {
            double x = startX + i * (DIM_BOX_SIZE + DIM_SPACING);
            String key = String.valueOf(i);

            // Box color
            Color boxColor = dimColors.containsKey(key) ? dimColors.get(key) : COLOR_DIM_DEFAULT;
            gc.setFill(boxColor);
            gc.fillRoundRect(x, boxY, DIM_BOX_SIZE, DIM_BOX_SIZE, 8, 8);

            // Border
            gc.setStroke(Color.rgb(144, 164, 174));
            gc.setLineWidth(1.0);
            gc.strokeRoundRect(x, boxY, DIM_BOX_SIZE, DIM_BOX_SIZE, 8, 8);

            // Value
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.setFill(COLOR_TEXT);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(dimensions[i]), x + DIM_BOX_SIZE / 2, boxY + DIM_BOX_SIZE / 2 + 5);

            // Index label
            gc.setFont(Font.font("Arial", 10));
            gc.setFill(Color.rgb(117, 117, 117));
            gc.fillText("p" + i, x + DIM_BOX_SIZE / 2, boxY + DIM_BOX_SIZE + 14);
        }
    }

    private void drawFormula(GraphicsContext gc, double y) {
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        gc.setFill(Color.rgb(21, 101, 192)); // Dark blue
        gc.setTextAlign(TextAlignment.LEFT);

        // Background box
        double textWidth = formulaText.length() * 8.0;
        gc.setFill(Color.rgb(232, 245, 233)); // Very light green background
        gc.fillRoundRect(PADDING - 4, y - 16, Math.max(textWidth + 16, 300), 28, 8, 8);
        gc.setStroke(Color.rgb(165, 214, 167));
        gc.strokeRoundRect(PADDING - 4, y - 16, Math.max(textWidth + 16, 300), 28, 8, 8);

        gc.setFill(Color.rgb(21, 101, 192));
        gc.fillText(formulaText, PADDING + 4, y);
    }

    /**
     * Format large numbers with commas for readability.
     */
    private String formatNumber(int num) {
        if (num < 1000) return String.valueOf(num);
        if (num < 10000) return String.format("%,d", num);
        return String.format("%dk", num / 1000);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
