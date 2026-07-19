package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * N-Queens algorithm code in multiple languages with dynamic parameter syncing
 */
public class NQueensCode implements AlgorithmCode {
    
    private int n;
    
    /**
     * Constructor with default values
     */
    public NQueensCode() {
        this.n = 6;
    }
    
    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int n) {
        this.n = n;
    }
    
    @Override
    public String getAlgorithmName() {
        return "N-Queens";
    }
    
    @Override
    public String getJavaCode() {
        return "public class Main {\n" +
               "    static final int N = " + n + ";\n" +
               "    static int solutionCount = 0;\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        System.out.println(\"Solving N-Queens problem for \" + N + \"x\" + N + \" board:\");\n" +
               "        System.out.println(\"=====================================\");\n" +
               "        int[] queens = new int[N]; // queens[i] = column of queen at row i\n" +
               "        solve(0, queens);\n" +
               "        System.out.println(\"=====================================\");\n" +
               "        System.out.println(\"Total solutions found: \" + solutionCount);\n" +
               "    }\n" +
               "\n" +
               "    static void solve(int row, int[] queens) {\n" +
               "        if (row == N) {\n" +
               "            solutionCount++;\n" +
               "            printSolution(queens);\n" +
               "            return;\n" +
               "        }\n" +
               "        for (int col = 0; col < N; col++) {\n" +
               "            if (isSafe(row, col, queens)) {\n" +
               "                queens[row] = col;\n" +
               "                solve(row + 1, queens);\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    static boolean isSafe(int row, int col, int[] queens) {\n" +
               "        for (int i = 0; i < row; i++) {\n" +
               "            int otherCol = queens[i];\n" +
               "            if (otherCol == col || Math.abs(otherCol - col) == Math.abs(i - row)) {\n" +
               "                return false;\n" +
               "            }\n" +
               "        }\n" +
               "        return true;\n" +
               "    }\n" +
               "\n" +
               "    static void printSolution(int[] queens) {\n" +
               "        System.out.println(\"\\nSolution #\" + solutionCount + \":\");\n" +
               "        for (int row = 0; row < N; row++) {\n" +
               "            for (int col = 0; col < N; col++) {\n" +
               "                if (queens[row] == col) {\n" +
               "                    System.out.print(\"Q \");\n" +
               "                } else {\n" +
               "                    System.out.print(\". \");\n" +
               "                }\n" +
               "            }\n" +
               "            System.out.println();\n" +
               "        }\n" +
               "        System.out.print(\"Queen positions:\");\n" +
               "        for (int row = 0; row < N; row++) {\n" +
               "            System.out.print(\" (\" + row + \",\" + queens[row] + \")\");\n" +
               "        }\n" +
               "        System.out.println();\n" +
               "    }\n" +
               "}\n";
    }
    
    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
               "#include <stdlib.h>\n" +
               "#include <stdbool.h>\n" +
               "\n" +
               "#define N " + n + "\n" +
               "\n" +
               "int solutionCount = 0;\n" +
               "\n" +
               "bool isSafe(int row, int col, int queens[]) {\n" +
               "    for (int i = 0; i < row; i++) {\n" +
               "        int otherCol = queens[i];\n" +
               "        if (otherCol == col || abs(otherCol - col) == abs(i - row)) {\n" +
               "            return false;\n" +
               "        }\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "void printSolution(int queens[]) {\n" +
               "    printf(\"\\nSolution #%d:\\n\", solutionCount);\n" +
               "    for (int row = 0; row < N; row++) {\n" +
               "        for (int col = 0; col < N; col++) {\n" +
               "            if (queens[row] == col) {\n" +
               "                printf(\"Q \");\n" +
               "            } else {\n" +
               "                printf(\". \");\n" +
               "            }\n" +
               "        }\n" +
               "        printf(\"\\n\");\n" +
               "    }\n" +
               "    printf(\"Queen positions:\");\n" +
               "    for (int row = 0; row < N; row++) {\n" +
               "        printf(\" (%d,%d)\", row, queens[row]);\n" +
               "    }\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "void solve(int row, int queens[]) {\n" +
               "    if (row == N) {\n" +
               "        solutionCount++;\n" +
               "        printSolution(queens);\n" +
               "        return;\n" +
               "    }\n" +
               "    for (int col = 0; col < N; col++) {\n" +
               "        if (isSafe(row, col, queens)) {\n" +
               "            queens[row] = col;\n" +
               "            solve(row + 1, queens);\n" +
               "        }\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    printf(\"Solving N-Queens problem for %dx%d board:\\n\", N, N);\n" +
               "    printf(\"=====================================\\n\");\n" +
               "    int queens[N];\n" +
               "    solve(0, queens);\n" +
               "    printf(\"=====================================\\n\");\n" +
               "    printf(\"Total solutions found: %d\\n\", solutionCount);\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getCppCode() {
        return "#include <iostream>\n" +
               "#include <cmath>\n" +
               "using namespace std;\n" +
               "\n" +
               "const int N = " + n + ";\n" +
               "int solutionCount = 0;\n" +
               "\n" +
               "bool isSafe(int row, int col, int queens[]) {\n" +
               "    for (int i = 0; i < row; i++) {\n" +
               "        int otherCol = queens[i];\n" +
               "        if (otherCol == col || abs(otherCol - col) == abs(i - row)) {\n" +
               "            return false;\n" +
               "        }\n" +
               "    }\n" +
               "    return true;\n" +
               "}\n" +
               "\n" +
               "void printSolution(int queens[]) {\n" +
               "    cout << \"\\nSolution #\" << solutionCount << \":\" << endl;\n" +
               "    for (int row = 0; row < N; row++) {\n" +
               "        for (int col = 0; col < N; col++) {\n" +
               "            if (queens[row] == col) {\n" +
               "                cout << \"Q \";\n" +
               "            } else {\n" +
               "                cout << \". \";\n" +
               "            }\n" +
               "        }\n" +
               "        cout << endl;\n" +
               "    }\n" +
               "    cout << \"Queen positions:\";\n" +
               "    for (int row = 0; row < N; row++) {\n" +
               "        cout << \" (\" << row << \",\" << queens[row] << \")\";\n" +
               "    }\n" +
               "    cout << endl;\n" +
               "}\n" +
               "\n" +
               "void solve(int row, int queens[]) {\n" +
               "    if (row == N) {\n" +
               "        solutionCount++;\n" +
               "        printSolution(queens);\n" +
               "        return;\n" +
               "    }\n" +
               "    for (int col = 0; col < N; col++) {\n" +
               "        if (isSafe(row, col, queens)) {\n" +
               "            queens[row] = col;\n" +
               "            solve(row + 1, queens);\n" +
               "        }\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    cout << \"Solving N-Queens problem for \" << N << \"x\" << N << \" board:\" << endl;\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "    int queens[N];\n" +
               "    solve(0, queens);\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "    cout << \"Total solutions found: \" << solutionCount << endl;\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getPythonCode() {
        return "N = " + n + "\n" +
               "solution_count = 0\n" +
               "\n" +
               "def is_safe(row, col, queens):\n" +
               "    for i in range(row):\n" +
               "        other_col = queens[i]\n" +
               "        if other_col == col or abs(other_col - col) == abs(i - row):\n" +
               "            return False\n" +
               "    return True\n" +
               "\n" +
               "def print_solution(queens):\n" +
               "    global solution_count\n" +
               "    print(f\"\\nSolution #{solution_count}:\")\n" +
               "    for row in range(N):\n" +
               "        for col in range(N):\n" +
               "            if queens[row] == col:\n" +
               "                print(\"Q \", end=\"\")\n" +
               "            else:\n" +
               "                print(\". \", end=\"\")\n" +
               "        print()\n" +
               "    print(\"Queen positions:\", end=\"\")\n" +
               "    for row in range(N):\n" +
               "        print(f\" ({row},{queens[row]})\", end=\"\")\n" +
               "    print()\n" +
               "\n" +
               "def solve(row, queens):\n" +
               "    global solution_count\n" +
               "    if row == N:\n" +
               "        solution_count += 1\n" +
               "        print_solution(queens)\n" +
               "        return\n" +
               "    for col in range(N):\n" +
               "        if is_safe(row, col, queens):\n" +
               "            queens[row] = col\n" +
               "            solve(row + 1, queens)\n" +
               "\n" +
               "print(f\"Solving N-Queens problem for {N}x{N} board:\")\n" +
               "print(\"=====================================\")\n" +
               "queens = [0] * N\n" +
               "solve(0, queens)\n" +
               "print(\"=====================================\")\n" +
               "print(f\"Total solutions found: {solution_count}\")\n";
    }
}
