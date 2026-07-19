package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Sudoku Solver algorithm code in multiple languages with dynamic parameter syncing
 */
public class SudokuSolverCode implements AlgorithmCode {
    
    private int[][] board;
    
    /**
     * Constructor with default values
     */
    public SudokuSolverCode() {
        this.board = new int[][] {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };
    }
    
    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int[][] board) {
        if (board != null && board.length == 9) {
            this.board = board;
        }
    }
    
    @Override
    public String getAlgorithmName() {
        return "Sudoku Solver";
    }
    
    @Override
    public String getJavaCode() {
        StringBuilder boardStr = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            boardStr.append("        {");
            for (int j = 0; j < 9; j++) {
                boardStr.append(board[i][j]);
                if (j < 8) boardStr.append(", ");
            }
            boardStr.append("}");
            if (i < 8) boardStr.append(",\n");
        }
        
        return "import java.util.*;\n" +
               "\n" +
               "public class Main {\n" +
               "    static final int[][] BOARD = {\n" +
               boardStr.toString() + "\n" +
               "    };\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        solve(BOARD);\n" +
               "        print(BOARD);\n" +
               "    }\n" +
               "\n" +
               "    static boolean solve(int[][] g) {\n" +
               "        int r = -1, c = -1;\n" +
               "        for (int i = 0; i < 9; i++) {\n" +
               "            for (int j = 0; j < 9; j++) {\n" +
               "                if (g[i][j] == 0) {\n" +
               "                    r = i;\n" +
               "                    c = j;\n" +
               "                    i = 9;\n" +
               "                    break;\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "        if (r == -1) return true;\n" +
               "\n" +
               "        for (int v = 1; v <= 9; v++) {\n" +
               "            if (can(g, r, c, v)) {\n" +
               "                g[r][c] = v;\n" +
               "                if (solve(g)) return true;\n" +
               "                g[r][c] = 0;\n" +
               "            }\n" +
               "        }\n" +
               "        return false;\n" +
               "    }\n" +
               "\n" +
               "    static boolean can(int[][] g, int r, int c, int v) {\n" +
               "        for (int k = 0; k < 9; k++) {\n" +
               "            if (g[r][k] == v || g[k][c] == v) return false;\n" +
               "        }\n" +
               "        int br = (r / 3) * 3, bc = (c / 3) * 3;\n" +
               "        for (int i = 0; i < 3; i++) {\n" +
               "            for (int j = 0; j < 3; j++) {\n" +
               "                if (g[br + i][bc + j] == v) return false;\n" +
               "            }\n" +
               "        }\n" +
               "        return true;\n" +
               "    }\n" +
               "\n" +
               "    static void print(int[][] g) {\n" +
               "        for (int i = 0; i < 9; i++) {\n" +
               "            for (int j = 0; j < 9; j++) {\n" +
               "                System.out.print(g[i][j] + \" \");\n" +
               "            }\n" +
               "            System.out.println();\n" +
               "        }\n" +
               "    }\n" +
               "}\n";
    }
    
    @Override
    public String getCCode() {
        StringBuilder boardStr = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            boardStr.append("    {");
            for (int j = 0; j < 9; j++) {
                boardStr.append(board[i][j]);
                if (j < 8) boardStr.append(", ");
            }
            boardStr.append("}");
            if (i < 8) boardStr.append(",\n");
        }
        
        return "#include <stdio.h>\n" +
               "#include <stdbool.h>\n" +
               "#include <stdlib.h>\n" +
               "\n" +
               "int board[9][9] = {\n" +
               boardStr.toString() + "\n" +
               "};\n" +
               "\n" +
               "bool can(int r, int c, int v) {\n" +
               "    for (int k = 0; k < 9; k++) {\n" +
               "        if (board[r][k] == v || board[k][c] == v) return false;\n" +
               "    }\n" +
               "    int br = (r / 3) * 3, bc = (c / 3) * 3;\n" +
               "    for (int i = 0; i < 3; i++) {\n" +
               "        for (int j = 0; j < 3; j++) {\n" +
               "            if (board[br + i][bc + j] == v) return false;\n" +
               "        }\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "bool solve() {\n" +
               "    int r = -1, c = -1;\n" +
               "    for (int i = 0; i < 9; i++) {\n" +
               "        for (int j = 0; j < 9; j++) {\n" +
               "            if (board[i][j] == 0) {\n" +
               "                r = i;\n" +
               "                c = j;\n" +
               "                i = 9;\n" +
               "                break;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    if (r == -1) return true;\n" +
               "\n" +
               "    for (int v = 1; v <= 9; v++) {\n" +
               "        if (can(r, c, v)) {\n" +
               "            board[r][c] = v;\n" +
               "            if (solve()) return true;\n" +
               "            board[r][c] = 0;\n" +
               "        }\n" +
               "    }\n" +
               "    return false;\n" +
               "}\n" +
               "\n" +
               "void print_board() {\n" +
               "    for (int i = 0; i < 9; i++) {\n" +
               "        for (int j = 0; j < 9; j++) {\n" +
               "            printf(\"%d \", board[i][j]);\n" +
               "        }\n" +
               "        printf(\"\\n\");\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    solve();\n" +
               "    print_board();\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getCppCode() {
        StringBuilder boardStr = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            boardStr.append("    {");
            for (int j = 0; j < 9; j++) {
                boardStr.append(board[i][j]);
                if (j < 8) boardStr.append(", ");
            }
            boardStr.append("}");
            if (i < 8) boardStr.append(",\n");
        }
        
        return "#include <iostream>\n" +
               "using namespace std;\n" +
               "\n" +
               "int board[9][9] = {\n" +
               boardStr.toString() + "\n" +
               "};\n" +
               "\n" +
               "bool can(int r, int c, int v) {\n" +
               "    for (int k = 0; k < 9; k++) {\n" +
               "        if (board[r][k] == v || board[k][c] == v) return false;\n" +
               "    }\n" +
               "    int br = (r / 3) * 3, bc = (c / 3) * 3;\n" +
               "    for (int i = 0; i < 3; i++) {\n" +
               "        for (int j = 0; j < 3; j++) {\n" +
               "            if (board[br + i][bc + j] == v) return false;\n" +
               "        }\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "bool solve() {\n" +
               "    int r = -1, c = -1;\n" +
               "    for (int i = 0; i < 9; i++) {\n" +
               "        for (int j = 0; j < 9; j++) {\n" +
               "            if (board[i][j] == 0) {\n" +
               "                r = i;\n" +
               "                c = j;\n" +
               "                i = 9;\n" +
               "                break;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    if (r == -1) return true;\n" +
               "\n" +
               "    for (int v = 1; v <= 9; v++) {\n" +
               "        if (can(r, c, v)) {\n" +
               "            board[r][c] = v;\n" +
               "            if (solve()) return true;\n" +
               "            board[r][c] = 0;\n" +
               "        }\n" +
               "    }\n" +
               "    return false;\n" +
               "}\n" +
               "\n" +
               "void print_board() {\n" +
               "    for (int i = 0; i < 9; i++) {\n" +
               "        for (int j = 0; j < 9; j++) {\n" +
               "            cout << board[i][j] << \" \";\n" +
               "        }\n" +
               "        cout << endl;\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    solve();\n" +
               "    print_board();\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getPythonCode() {
        StringBuilder boardStr = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            boardStr.append("    [");
            for (int j = 0; j < 9; j++) {
                boardStr.append(board[i][j]);
                if (j < 8) boardStr.append(", ");
            }
            boardStr.append("]");
            if (i < 8) boardStr.append(",\n");
        }
        
        return "board = [\n" +
               boardStr.toString() + "\n" +
               "]\n" +
               "\n" +
               "def can(r, c, v):\n" +
               "    for k in range(9):\n" +
               "        if board[r][k] == v or board[k][c] == v:\n" +
               "            return False\n" +
               "    br, bc = (r // 3) * 3, (c // 3) * 3\n" +
               "    for i in range(3):\n" +
               "        for j in range(3):\n" +
               "            if board[br + i][bc + j] == v:\n" +
               "                return False\n" +
               "    return True\n" +
               "\n" +
               "def solve():\n" +
               "    r, c = -1, -1\n" +
               "    for i in range(9):\n" +
               "        for j in range(9):\n" +
               "            if board[i][j] == 0:\n" +
               "                r, c = i, j\n" +
               "                break\n" +
               "        if r != -1:\n" +
               "            break\n" +
               "    if r == -1:\n" +
               "        return True\n" +
               "\n" +
               "    for v in range(1, 10):\n" +
               "        if can(r, c, v):\n" +
               "            board[r][c] = v\n" +
               "            if solve():\n" +
               "                return True\n" +
               "            board[r][c] = 0\n" +
               "    return False\n" +
               "\n" +
               "def print_board():\n" +
               "    for i in range(9):\n" +
               "        for j in range(9):\n" +
               "            print(board[i][j], end=\" \")\n" +
               "        print()\n" +
               "\n" +
               "solve()\n" +
               "print_board()\n";
    }
}
