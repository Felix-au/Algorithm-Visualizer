package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;
import com.algorithmvisualizer.algorithm.ZeroOneKnapsackSolver.Item;

/**
 * Code implementations for 0/1 Knapsack in C, C++, Java, and Python.
 */
public class ZeroOneKnapsackCode implements AlgorithmCode {

    private Item[] items;
    private int capacity;

    public ZeroOneKnapsackCode() {
        this.items = new Item[] {
                new Item(1, 2, 12),
                new Item(2, 1, 10),
                new Item(3, 3, 20),
                new Item(4, 2, 15)
        };
        this.capacity = 10;
        CodeRepository.register(this);
    }

    public void updateParameters(Item[] items, int capacity) {
        this.items = items;
        this.capacity = capacity;
    }

    @Override
    public String getAlgorithmName() {
        return "0/1 Knapsack";
    }

    @Override
    public String getCCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// 0/1 Knapsack - Dynamic Programming\n");
        sb.append("// Time: O(n*W), Space: O(n*W)\n\n");
        sb.append("#include <stdio.h>\n\n");
        sb.append("int max(int a, int b) { return a > b ? a : b; }\n\n");
        sb.append("int knapsack(int weights[], int values[], int n, int W) {\n");
        sb.append("    int dp[n + 1][W + 1];\n");
        sb.append("    \n");
        sb.append("    // Initialize base cases\n");
        sb.append("    for (int i = 0; i <= n; i++) {\n");
        sb.append("        for (int w = 0; w <= W; w++) {\n");
        sb.append("            if (i == 0 || w == 0) {\n");
        sb.append("                dp[i][w] = 0;\n");
        sb.append("            }\n");
        sb.append("            else if (weights[i-1] <= w) {\n");
        sb.append("                // Item fits: max of include or exclude\n");
        sb.append("                dp[i][w] = max(\n");
        sb.append("                    values[i-1] + dp[i-1][w - weights[i-1]],\n");
        sb.append("                    dp[i-1][w]\n");
        sb.append("                );\n");
        sb.append("            } else {\n");
        sb.append("                // Item too heavy: exclude\n");
        sb.append("                dp[i][w] = dp[i-1][w];\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    // Traceback to find selected items\n");
        sb.append("    printf(\"Selected items: \");\n");
        sb.append("    int w = W;\n");
        sb.append("    for (int i = n; i > 0; i--) {\n");
        sb.append("        if (dp[i][w] != dp[i-1][w]) {\n");
        sb.append("            printf(\"Item %d (w=%d, v=%d) \", i, weights[i-1], values[i-1]);\n");
        sb.append("            w -= weights[i-1];\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    printf(\"\\n\");\n");
        sb.append("    \n");
        sb.append("    return dp[n][W];\n");
        sb.append("}\n\n");
        sb.append("int main() {\n");

        sb.append("    int weights[] = {");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].weight);
        }
        sb.append("};\n");

        sb.append("    int values[] = {");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].value);
        }
        sb.append("};\n");

        sb.append("    int n = ").append(items.length).append(";\n");
        sb.append("    int W = ").append(capacity).append(";\n");
        sb.append("    \n");
        sb.append("    int max_value = knapsack(weights, values, n, W);\n");
        sb.append("    printf(\"Maximum value: %d\\n\", max_value);\n");
        sb.append("    \n");
        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String getCppCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// 0/1 Knapsack - Dynamic Programming\n");
        sb.append("// Time: O(n*W), Space: O(n*W)\n\n");
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <algorithm>\n");
        sb.append("using namespace std;\n\n");
        sb.append("int knapsack(vector<int>& weights, vector<int>& values, int W) {\n");
        sb.append("    int n = weights.size();\n");
        sb.append("    vector<vector<int>> dp(n + 1, vector<int>(W + 1, 0));\n");
        sb.append("    \n");
        sb.append("    // Build DP table\n");
        sb.append("    for (int i = 1; i <= n; i++) {\n");
        sb.append("        for (int w = 0; w <= W; w++) {\n");
        sb.append("            if (weights[i-1] <= w) {\n");
        sb.append("                // Item fits: max of include or exclude\n");
        sb.append("                dp[i][w] = max(\n");
        sb.append("                    values[i-1] + dp[i-1][w - weights[i-1]],\n");
        sb.append("                    dp[i-1][w]\n");
        sb.append("                );\n");
        sb.append("            } else {\n");
        sb.append("                // Item too heavy\n");
        sb.append("                dp[i][w] = dp[i-1][w];\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    // Traceback\n");
        sb.append("    cout << \"Selected items: \";\n");
        sb.append("    int w = W;\n");
        sb.append("    for (int i = n; i > 0; i--) {\n");
        sb.append("        if (dp[i][w] != dp[i-1][w]) {\n");
        sb.append("            cout << \"Item \" << i << \" (w=\" << weights[i-1]\n");
        sb.append("                 << \", v=\" << values[i-1] << \") \";\n");
        sb.append("            w -= weights[i-1];\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    cout << endl;\n");
        sb.append("    \n");
        sb.append("    return dp[n][W];\n");
        sb.append("}\n\n");
        sb.append("int main() {\n");

        sb.append("    vector<int> weights = {");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].weight);
        }
        sb.append("};\n");

        sb.append("    vector<int> values = {");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].value);
        }
        sb.append("};\n");

        sb.append("    int W = ").append(capacity).append(";\n");
        sb.append("    \n");
        sb.append("    int maxValue = knapsack(weights, values, W);\n");
        sb.append("    cout << \"Maximum value: \" << maxValue << endl;\n");
        sb.append("    \n");
        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String getJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// 0/1 Knapsack - Dynamic Programming\n");
        sb.append("// Time: O(n*W), Space: O(n*W)\n\n");
        sb.append("public class Main {\n");
        sb.append("    \n");
        sb.append("    public static int knapsack(int[] weights, int[] values, int W) {\n");
        sb.append("        int n = weights.length;\n");
        sb.append("        int[][] dp = new int[n + 1][W + 1];\n");
        sb.append("        \n");
        sb.append("        // Build DP table\n");
        sb.append("        for (int i = 1; i <= n; i++) {\n");
        sb.append("            for (int w = 0; w <= W; w++) {\n");
        sb.append("                if (weights[i-1] <= w) {\n");
        sb.append("                    // Item fits: max of include or exclude\n");
        sb.append("                    dp[i][w] = Math.max(\n");
        sb.append("                        values[i-1] + dp[i-1][w - weights[i-1]],\n");
        sb.append("                        dp[i-1][w]\n");
        sb.append("                    );\n");
        sb.append("                } else {\n");
        sb.append("                    // Item too heavy\n");
        sb.append("                    dp[i][w] = dp[i-1][w];\n");
        sb.append("                }\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        // Traceback\n");
        sb.append("        System.out.print(\"Selected items: \");\n");
        sb.append("        int w = W;\n");
        sb.append("        for (int i = n; i > 0; i--) {\n");
        sb.append("            if (dp[i][w] != dp[i-1][w]) {\n");
        sb.append("                System.out.printf(\"Item %d (w=%d, v=%d) \",\n");
        sb.append("                    i, weights[i-1], values[i-1]);\n");
        sb.append("                w -= weights[i-1];\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        System.out.println();\n");
        sb.append("        \n");
        sb.append("        return dp[n][W];\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    public static void main(String[] args) {\n");

        sb.append("        int[] weights = {");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].weight);
        }
        sb.append("};\n");

        sb.append("        int[] values = {");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].value);
        }
        sb.append("};\n");

        sb.append("        int W = ").append(capacity).append(";\n");
        sb.append("        \n");
        sb.append("        int maxValue = knapsack(weights, values, W);\n");
        sb.append("        System.out.println(\"Maximum value: \" + maxValue);\n");
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    @Override
    public String getPythonCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("# 0/1 Knapsack - Dynamic Programming\n");
        sb.append("# Time: O(n*W), Space: O(n*W)\n\n");
        sb.append("def knapsack(weights, values, W):\n");
        sb.append("    n = len(weights)\n");
        sb.append("    \n");
        sb.append("    # Create DP table\n");
        sb.append("    dp = [[0] * (W + 1) for _ in range(n + 1)]\n");
        sb.append("    \n");
        sb.append("    # Build DP table\n");
        sb.append("    for i in range(1, n + 1):\n");
        sb.append("        for w in range(W + 1):\n");
        sb.append("            if weights[i-1] <= w:\n");
        sb.append("                # Item fits: max of include or exclude\n");
        sb.append("                dp[i][w] = max(\n");
        sb.append("                    values[i-1] + dp[i-1][w - weights[i-1]],\n");
        sb.append("                    dp[i-1][w]\n");
        sb.append("                )\n");
        sb.append("            else:\n");
        sb.append("                # Item too heavy\n");
        sb.append("                dp[i][w] = dp[i-1][w]\n");
        sb.append("    \n");
        sb.append("    # Traceback to find selected items\n");
        sb.append("    selected = []\n");
        sb.append("    w = W\n");
        sb.append("    for i in range(n, 0, -1):\n");
        sb.append("        if dp[i][w] != dp[i-1][w]:\n");
        sb.append("            selected.append(i)\n");
        sb.append("            w -= weights[i-1]\n");
        sb.append("    \n");
        sb.append("    print(f\"Selected items: {selected}\")\n");
        sb.append("    return dp[n][W]\n\n");
        sb.append("if __name__ == \"__main__\":\n");

        sb.append("    weights = [");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].weight);
        }
        sb.append("]\n");

        sb.append("    values = [");
        for (int i = 0; i < items.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(items[i].value);
        }
        sb.append("]\n");

        sb.append("    W = ").append(capacity).append("\n");
        sb.append("    \n");
        sb.append("    max_value = knapsack(weights, values, W)\n");
        sb.append("    print(f\"Maximum value: {max_value}\")\n");

        return sb.toString();
    }
}
