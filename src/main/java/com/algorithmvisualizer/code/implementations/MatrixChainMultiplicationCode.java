package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;

/**
 * Code implementations for Matrix Chain Multiplication in C, C++, Java, Python.
 * Generates compilable, runnable code with rich step-by-step output.
 */
public class MatrixChainMultiplicationCode implements AlgorithmCode {

    private int[] dimensions;

    public MatrixChainMultiplicationCode() {
        this.dimensions = new int[]{30, 35, 15, 5, 10, 20, 25};
        CodeRepository.register(this);
    }

    public void updateParameters(int[] dimensions) {
        this.dimensions = dimensions.clone();
    }

    @Override
    public String getAlgorithmName() {
        return "Matrix Chain Multiplication";
    }

    @Override
    public String getCCode() {
        int n = dimensions.length - 1;
        return "#include <stdio.h>\n" +
            "#include <limits.h>\n" +
            "\n" +
            "// Dimensions from AlgoBuddy UI\n" +
            "int p[] = {" + formatArray() + "};\n" +
            "int n = " + n + "; // number of matrices\n" +
            "\n" +
            "int m[20][20]; // cost table\n" +
            "int s[20][20]; // split table\n" +
            "\n" +
            "void printParenthesization(int i, int j) {\n" +
            "    if (i == j) {\n" +
            "        printf(\"A%d\", i);\n" +
            "        return;\n" +
            "    }\n" +
            "    printf(\"(\");\n" +
            "    printParenthesization(i, s[i][j]);\n" +
            "    printf(\" x \");\n" +
            "    printParenthesization(s[i][j] + 1, j);\n" +
            "    printf(\")\");\n" +
            "}\n" +
            "\n" +
            "int main() {\n" +
            "    printf(\"=== Matrix Chain Multiplication (DP) ===\\n\");\n" +
            "    printf(\"Matrices: %d\\n\", n);\n" +
            "    printf(\"Dimensions: \");\n" +
            "    for (int i = 0; i <= n; i++) printf(\"%d \", p[i]);\n" +
            "    printf(\"\\n\\n\");\n" +
            "\n" +
            "    // Base case: single matrices cost 0\n" +
            "    for (int i = 1; i <= n; i++) m[i][i] = 0;\n" +
            "\n" +
            "    // Fill table by chain length\n" +
            "    for (int l = 2; l <= n; l++) {\n" +
            "        printf(\"--- Chain length %d ---\\n\", l);\n" +
            "        for (int i = 1; i <= n - l + 1; i++) {\n" +
            "            int j = i + l - 1;\n" +
            "            m[i][j] = INT_MAX;\n" +
            "\n" +
            "            for (int k = i; k < j; k++) {\n" +
            "                int cost = m[i][k] + m[k+1][j] + p[i-1]*p[k]*p[j];\n" +
            "                printf(\"  m[%d][%d]: split k=%d, cost = %d + %d + %d*%d*%d = %d\",\n" +
            "                       i, j, k, m[i][k], m[k+1][j], p[i-1], p[k], p[j], cost);\n" +
            "\n" +
            "                if (cost < m[i][j]) {\n" +
            "                    m[i][j] = cost;\n" +
            "                    s[i][j] = k;\n" +
            "                    printf(\"  <-- new min!\\n\");\n" +
            "                } else {\n" +
            "                    printf(\"\\n\");\n" +
            "                }\n" +
            "            }\n" +
            "            printf(\"  => m[%d][%d] = %d (split at k=%d)\\n\\n\", i, j, m[i][j], s[i][j]);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    printf(\"\\n=== Result ===\\n\");\n" +
            "    printf(\"Minimum scalar multiplications: %d\\n\", m[1][n]);\n" +
            "    printf(\"Optimal parenthesization: \");\n" +
            "    printParenthesization(1, n);\n" +
            "    printf(\"\\n\");\n" +
            "\n" +
            "    return 0;\n" +
            "}\n";
    }

    @Override
    public String getCppCode() {
        int n = dimensions.length - 1;
        return "#include <iostream>\n" +
            "#include <climits>\n" +
            "#include <vector>\n" +
            "using namespace std;\n" +
            "\n" +
            "// Dimensions from AlgoBuddy UI\n" +
            "vector<int> p = {" + formatArray() + "};\n" +
            "int n = " + n + ";\n" +
            "\n" +
            "int m[20][20];\n" +
            "int s[20][20];\n" +
            "\n" +
            "void printParen(int i, int j) {\n" +
            "    if (i == j) { cout << \"A\" << i; return; }\n" +
            "    cout << \"(\";\n" +
            "    printParen(i, s[i][j]);\n" +
            "    cout << \" x \";\n" +
            "    printParen(s[i][j] + 1, j);\n" +
            "    cout << \")\";\n" +
            "}\n" +
            "\n" +
            "int main() {\n" +
            "    cout << \"=== Matrix Chain Multiplication (DP) ===\" << endl;\n" +
            "    cout << \"Matrices: \" << n << endl;\n" +
            "    cout << \"Dimensions: \";\n" +
            "    for (int x : p) cout << x << \" \";\n" +
            "    cout << endl << endl;\n" +
            "\n" +
            "    for (int i = 1; i <= n; i++) m[i][i] = 0;\n" +
            "\n" +
            "    for (int l = 2; l <= n; l++) {\n" +
            "        cout << \"--- Chain length \" << l << \" ---\" << endl;\n" +
            "        for (int i = 1; i <= n - l + 1; i++) {\n" +
            "            int j = i + l - 1;\n" +
            "            m[i][j] = INT_MAX;\n" +
            "\n" +
            "            for (int k = i; k < j; k++) {\n" +
            "                int cost = m[i][k] + m[k+1][j] + p[i-1]*p[k]*p[j];\n" +
            "                cout << \"  m[\" << i << \"][\" << j << \"]: split k=\" << k\n" +
            "                     << \", cost=\" << cost;\n" +
            "\n" +
            "                if (cost < m[i][j]) {\n" +
            "                    m[i][j] = cost;\n" +
            "                    s[i][j] = k;\n" +
            "                    cout << \" <-- new min!\" << endl;\n" +
            "                } else {\n" +
            "                    cout << endl;\n" +
            "                }\n" +
            "            }\n" +
            "            cout << \"  => m[\" << i << \"][\" << j << \"] = \" << m[i][j]\n" +
            "                 << \" (split at k=\" << s[i][j] << \")\" << endl << endl;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    cout << endl << \"=== Result ===\" << endl;\n" +
            "    cout << \"Minimum scalar multiplications: \" << m[1][n] << endl;\n" +
            "    cout << \"Optimal parenthesization: \";\n" +
            "    printParen(1, n);\n" +
            "    cout << endl;\n" +
            "\n" +
            "    return 0;\n" +
            "}\n";
    }

    @Override
    public String getJavaCode() {
        int n = dimensions.length - 1;
        return "public class Main {\n" +
            "    // Dimensions from AlgoBuddy UI\n" +
            "    static int[] p = {" + formatArray() + "};\n" +
            "    static int n = " + n + ";\n" +
            "\n" +
            "    static int[][] m = new int[n + 1][n + 1];\n" +
            "    static int[][] s = new int[n + 1][n + 1];\n" +
            "\n" +
            "    static void printParen(int i, int j, StringBuilder sb) {\n" +
            "        if (i == j) { sb.append(\"A\").append(i); return; }\n" +
            "        sb.append(\"(\");\n" +
            "        printParen(i, s[i][j], sb);\n" +
            "        sb.append(\" x \");\n" +
            "        printParen(s[i][j] + 1, j, sb);\n" +
            "        sb.append(\")\");\n" +
            "    }\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"=== Matrix Chain Multiplication (DP) ===\");\n" +
            "        System.out.println(\"Matrices: \" + n);\n" +
            "        System.out.print(\"Dimensions: \");\n" +
            "        for (int x : p) System.out.print(x + \" \");\n" +
            "        System.out.println(\"\\n\");\n" +
            "\n" +
            "        for (int i = 1; i <= n; i++) m[i][i] = 0;\n" +
            "\n" +
            "        for (int l = 2; l <= n; l++) {\n" +
            "            System.out.println(\"--- Chain length \" + l + \" ---\");\n" +
            "            for (int i = 1; i <= n - l + 1; i++) {\n" +
            "                int j = i + l - 1;\n" +
            "                m[i][j] = Integer.MAX_VALUE;\n" +
            "\n" +
            "                for (int k = i; k < j; k++) {\n" +
            "                    int cost = m[i][k] + m[k+1][j] + p[i-1]*p[k]*p[j];\n" +
            "                    System.out.print(\"  m[\" + i + \"][\" + j + \"]: split k=\" + k\n" +
            "                        + \", cost=\" + cost);\n" +
            "\n" +
            "                    if (cost < m[i][j]) {\n" +
            "                        m[i][j] = cost;\n" +
            "                        s[i][j] = k;\n" +
            "                        System.out.println(\" <-- new min!\");\n" +
            "                    } else {\n" +
            "                        System.out.println();\n" +
            "                    }\n" +
            "                }\n" +
            "                System.out.println(\"  => m[\" + i + \"][\" + j + \"] = \" + m[i][j]\n" +
            "                    + \" (split at k=\" + s[i][j] + \")\\n\");\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        System.out.println(\"\\n=== Result ===\");\n" +
            "        System.out.println(\"Minimum scalar multiplications: \" + m[1][n]);\n" +
            "        StringBuilder sb = new StringBuilder();\n" +
            "        printParen(1, n, sb);\n" +
            "        System.out.println(\"Optimal parenthesization: \" + sb);\n" +
            "    }\n" +
            "}\n";
    }

    @Override
    public String getPythonCode() {
        int n = dimensions.length - 1;
        return "import sys\n" +
            "\n" +
            "# Dimensions from AlgoBuddy UI\n" +
            "p = [" + formatArray() + "]\n" +
            "n = " + n + "  # number of matrices\n" +
            "\n" +
            "m = [[0] * (n + 1) for _ in range(n + 1)]\n" +
            "s = [[0] * (n + 1) for _ in range(n + 1)]\n" +
            "\n" +
            "def print_paren(i, j):\n" +
            "    if i == j:\n" +
            "        return f\"A{i}\"\n" +
            "    left = print_paren(i, s[i][j])\n" +
            "    right = print_paren(s[i][j] + 1, j)\n" +
            "    return f\"({left} x {right})\"\n" +
            "\n" +
            "def matrix_chain_order():\n" +
            "    print(\"=== Matrix Chain Multiplication (DP) ===\")\n" +
            "    print(f\"Matrices: {n}\")\n" +
            "    print(f\"Dimensions: {p}\")\n" +
            "    print()\n" +
            "\n" +
            "    for l in range(2, n + 1):\n" +
            "        print(f\"--- Chain length {l} ---\")\n" +
            "        for i in range(1, n - l + 2):\n" +
            "            j = i + l - 1\n" +
            "            m[i][j] = sys.maxsize\n" +
            "\n" +
            "            for k in range(i, j):\n" +
            "                cost = m[i][k] + m[k+1][j] + p[i-1] * p[k] * p[j]\n" +
            "                marker = \"\"\n" +
            "\n" +
            "                if cost < m[i][j]:\n" +
            "                    m[i][j] = cost\n" +
            "                    s[i][j] = k\n" +
            "                    marker = \" <-- new min!\"\n" +
            "\n" +
            "                print(f\"  m[{i}][{j}]: split k={k}, cost={cost}{marker}\")\n" +
            "\n" +
            "            print(f\"  => m[{i}][{j}] = {m[i][j]} (split at k={s[i][j]})\")\n" +
            "            print()\n" +
            "\n" +
            "    print(\"\\n=== Result ===\")\n" +
            "    print(f\"Minimum scalar multiplications: {m[1][n]}\")\n" +
            "    print(f\"Optimal parenthesization: {print_paren(1, n)}\")\n" +
            "\n" +
            "if __name__ == \"__main__\":\n" +
            "    matrix_chain_order()\n";
    }

    private String formatArray() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimensions.length; i++) {
            sb.append(dimensions[i]);
            if (i < dimensions.length - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
