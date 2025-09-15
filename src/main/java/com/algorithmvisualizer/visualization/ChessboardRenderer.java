package com.algorithmvisualizer.visualization;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Renders a chessboard for the N-Queens visualization
 */
public class ChessboardRenderer {
    
    private int boardSize;
    private GridPane chessboard;
    private Rectangle[][] squares;
    private Circle[][] queens;
    private static final int SQUARE_SIZE = 40;
    private static final Color LIGHT_COLOR = Color.WHITE;
    private static final Color DARK_COLOR = Color.LIGHTGRAY;
    private static final Color QUEEN_COLOR = Color.DARKRED;
    private static final Color CONFLICT_COLOR = Color.RED;
    
    public ChessboardRenderer(int boardSize) {
        this.boardSize = boardSize;
        createChessboard();
    }
    
    private void createChessboard() {
        chessboard = new GridPane();
        squares = new Rectangle[boardSize][boardSize];
        queens = new Circle[boardSize][boardSize];
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Create square
                Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                square.setFill((row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
                square.setStroke(Color.BLACK);
                square.setStrokeWidth(1);
                
                squares[row][col] = square;
                chessboard.add(square, col, row);
                
                // Create queen placeholder (initially invisible)
                Circle queen = new Circle(SQUARE_SIZE / 3);
                queen.setFill(QUEEN_COLOR);
                queen.setStroke(Color.BLACK);
                queen.setStrokeWidth(2);
                queen.setVisible(false);
                
                queens[row][col] = queen;
                chessboard.add(queen, col, row);
            }
        }
        // Labels removed: GridPane does not support negative indices for headers
    }
    
    // Header labels are intentionally omitted to avoid invalid GridPane indices.
    
    public void placeQueen(int row, int col) {
        if (isValidPosition(row, col)) {
            queens[row][col].setVisible(true);
            highlightConflicts(row, col);
        }
    }
    
    public void removeQueen(int row, int col) {
        if (isValidPosition(row, col)) {
            queens[row][col].setVisible(false);
            clearConflicts();
        }
    }
    
    public void clearBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                queens[row][col].setVisible(false);
                squares[row][col].setFill((row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
            }
        }
    }
    
    public void highlightConflicts(int queenRow, int queenCol) {
        // Reset all squares to original colors
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                squares[row][col].setFill((row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
            }
        }
        
        // Highlight conflicts for the placed queen
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (isInConflict(queenRow, queenCol, row, col)) {
                    squares[row][col].setFill(CONFLICT_COLOR);
                }
            }
        }
        
        // Restore the queen's square
        squares[queenRow][queenCol].setFill((queenRow + queenCol) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
    }
    
    public void clearConflicts() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                squares[row][col].setFill((row + col) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
            }
        }
    }
    
    public void highlightCurrentPosition(int row, int col) {
        // Reset all squares to original colors first
        clearConflicts();
        
        // Highlight current position in yellow
        if (isValidPosition(row, col)) {
            squares[row][col].setFill(Color.YELLOW);
        }
    }
    
    private boolean isInConflict(int queenRow, int queenCol, int row, int col) {
        // Same row
        if (queenRow == row) return true;
        
        // Same column
        if (queenCol == col) return true;
        
        // Same diagonal
        if (Math.abs(queenRow - row) == Math.abs(queenCol - col)) return true;
        
        return false;
    }
    
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
    }
    
    public GridPane getChessboard() {
        return chessboard;
    }
}
