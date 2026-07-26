package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.algorithm.FractionalKnapsackSolver.Item;

/**
 * Fractional Knapsack code in multiple languages
 */
public class FractionalKnapsackCode implements AlgorithmCode {

    private Item[] items;
    private double capacity;

    public FractionalKnapsackCode() {
        this.items = new Item[]{
            new Item(1, 10, 60),
            new Item(2, 20, 100),
            new Item(3, 30, 120)
        };
        this.capacity = 50.0;
    }

    public void updateParameters(Item[] items, double capacity) {
        this.items = items;
        this.capacity = capacity;
    }

    @Override
    public String getAlgorithmName() {
        return "Fractional Knapsack";
    }

    @Override
    public String getCodeForLanguage(String language) {
        switch (language.toLowerCase()) {
            case "python":
                return getPythonCode();
            case "java":
                return getJavaCode();
            case "c":
                return getCCode();
            case "c++":
                return getCppCode();
            default:
                return getPythonCode();
        }
    }

    public String getPythonCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Fractional Knapsack - Greedy Algorithm\n");
        sb.append("# Time: O(n log n), Space: O(1)\n\n");
        
        sb.append("class Item:\n");
        sb.append("    def __init__(self, weight, value):\n");
        sb.append("        self.weight = weight\n");
        sb.append("        self.value = value\n");
        sb.append("        self.ratio = value / weight\n\n");
        
        sb.append("def fractional_knapsack(items, capacity):\n");
        sb.append("    \"\"\"\n");
        sb.append("    Solve fractional knapsack using greedy approach.\n");
        sb.append("    Items can be broken into fractions.\n");
        sb.append("    \"\"\"\n");
        sb.append("    # Calculate value/weight ratios\n");
        sb.append("    for item in items:\n");
        sb.append("        item.ratio = item.value / item.weight\n");
        sb.append("    \n");
        sb.append("    # Sort by ratio in descending order\n");
        sb.append("    items.sort(key=lambda x: x.ratio, reverse=True)\n");
        sb.append("    \n");
        sb.append("    total_value = 0.0\n");
        sb.append("    remaining = capacity\n");
        sb.append("    \n");
        sb.append("    # Greedily select items\n");
        sb.append("    for item in items:\n");
        sb.append("        if remaining == 0:\n");
        sb.append("            break\n");
        sb.append("        \n");
        sb.append("        if item.weight <= remaining:\n");
        sb.append("            # Take full item\n");
        sb.append("            total_value += item.value\n");
        sb.append("            remaining -= item.weight\n");
        sb.append("        else:\n");
        sb.append("            # Take fraction of item\n");
        sb.append("            fraction = remaining / item.weight\n");
        sb.append("            total_value += item.value * fraction\n");
        sb.append("            remaining = 0\n");
        sb.append("    \n");
        sb.append("    return total_value\n\n");
        
        sb.append("# Example usage\n");
        sb.append("items = [\n");
        for (int i = 0; i < items.length; i++) {
            sb.append(String.format("    Item(%.1f, %.1f)", items[i].weight, items[i].value));
            if (i < items.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        sb.append(String.format("capacity = %.1f\n\n", capacity));
        sb.append("max_value = fractional_knapsack(items, capacity)\n");
        sb.append("print(f\"Maximum value: ${max_value:.2f}\")\n");
        
        return sb.toString();
    }

    public String getJavaCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Fractional Knapsack - Greedy Algorithm\n");
        sb.append("// Time: O(n log n), Space: O(1)\n\n");
        
        sb.append("import java.util.Arrays;\n\n");
        
        sb.append("class Item {\n");
        sb.append("    double weight;\n");
        sb.append("    double value;\n");
        sb.append("    double ratio;\n");
        sb.append("    \n");
        sb.append("    Item(double weight, double value) {\n");
        sb.append("        this.weight = weight;\n");
        sb.append("        this.value = value;\n");
        sb.append("        this.ratio = value / weight;\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("public class Main {\n");
        sb.append("    \n");
        sb.append("    public static double solve(Item[] items, double capacity) {\n");
        sb.append("        // Calculate ratios\n");
        sb.append("        for (Item item : items) {\n");
        sb.append("            item.ratio = item.value / item.weight;\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        // Sort by ratio (descending)\n");
        sb.append("        Arrays.sort(items, (a, b) -> Double.compare(b.ratio, a.ratio));\n");
        sb.append("        \n");
        sb.append("        double totalValue = 0.0;\n");
        sb.append("        double remaining = capacity;\n");
        sb.append("        \n");
        sb.append("        // Greedily select items\n");
        sb.append("        for (Item item : items) {\n");
        sb.append("            if (remaining == 0) break;\n");
        sb.append("            \n");
        sb.append("            if (item.weight <= remaining) {\n");
        sb.append("                // Take full item\n");
        sb.append("                totalValue += item.value;\n");
        sb.append("                remaining -= item.weight;\n");
        sb.append("            } else {\n");
        sb.append("                // Take fraction\n");
        sb.append("                double fraction = remaining / item.weight;\n");
        sb.append("                totalValue += item.value * fraction;\n");
        sb.append("                remaining = 0;\n");
        sb.append("            }\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        return totalValue;\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    public static void main(String[] args) {\n");
        sb.append("        Item[] items = {\n");
        for (int i = 0; i < items.length; i++) {
            sb.append(String.format("            new Item(%.1f, %.1f)", items[i].weight, items[i].value));
            if (i < items.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("        };\n");
        sb.append(String.format("        double capacity = %.1f;\n", capacity));
        sb.append("        \n");
        sb.append("        double maxValue = solve(items, capacity);\n");
        sb.append("        System.out.printf(\"Maximum value: $%.2f\\n\", maxValue);\n");
        sb.append("    }\n");
        sb.append("}\n");
        
        return sb.toString();
    }

    public String getCCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Fractional Knapsack - Greedy Algorithm\n");
        sb.append("// Time: O(n log n), Space: O(1)\n\n");
        
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n\n");
        
        sb.append("typedef struct {\n");
        sb.append("    double weight;\n");
        sb.append("    double value;\n");
        sb.append("    double ratio;\n");
        sb.append("} Item;\n\n");
        
        sb.append("int compare(const void *a, const void *b) {\n");
        sb.append("    Item *itemA = (Item *)a;\n");
        sb.append("    Item *itemB = (Item *)b;\n");
        sb.append("    if (itemB->ratio > itemA->ratio) return 1;\n");
        sb.append("    if (itemB->ratio < itemA->ratio) return -1;\n");
        sb.append("    return 0;\n");
        sb.append("}\n\n");
        
        sb.append("double fractional_knapsack(Item items[], int n, double capacity) {\n");
        sb.append("    // Calculate ratios\n");
        sb.append("    for (int i = 0; i < n; i++) {\n");
        sb.append("        items[i].ratio = items[i].value / items[i].weight;\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    // Sort by ratio (descending)\n");
        sb.append("    qsort(items, n, sizeof(Item), compare);\n");
        sb.append("    \n");
        sb.append("    double total_value = 0.0;\n");
        sb.append("    double remaining = capacity;\n");
        sb.append("    \n");
        sb.append("    // Greedily select items\n");
        sb.append("    for (int i = 0; i < n; i++) {\n");
        sb.append("        if (remaining == 0) break;\n");
        sb.append("        \n");
        sb.append("        if (items[i].weight <= remaining) {\n");
        sb.append("            // Take full item\n");
        sb.append("            total_value += items[i].value;\n");
        sb.append("            remaining -= items[i].weight;\n");
        sb.append("        } else {\n");
        sb.append("            // Take fraction\n");
        sb.append("            double fraction = remaining / items[i].weight;\n");
        sb.append("            total_value += items[i].value * fraction;\n");
        sb.append("            remaining = 0;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    return total_value;\n");
        sb.append("}\n\n");
        
        sb.append("int main() {\n");
        sb.append(String.format("    Item items[] = {\n"));
        for (int i = 0; i < items.length; i++) {
            sb.append(String.format("        {%.1f, %.1f, 0.0}", items[i].weight, items[i].value));
            if (i < items.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    };\n");
        sb.append(String.format("    int n = %d;\n", items.length));
        sb.append(String.format("    double capacity = %.1f;\n", capacity));
        sb.append("    \n");
        sb.append("    double max_value = fractional_knapsack(items, n, capacity);\n");
        sb.append("    printf(\"Maximum value: $%.2f\\n\", max_value);\n");
        sb.append("    \n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }

    public String getCppCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// Fractional Knapsack - Greedy Algorithm\n");
        sb.append("// Time: O(n log n), Space: O(1)\n\n");
        
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <algorithm>\n");
        sb.append("#include <iomanip>\n");
        sb.append("using namespace std;\n\n");
        
        sb.append("struct Item {\n");
        sb.append("    double weight;\n");
        sb.append("    double value;\n");
        sb.append("    double ratio;\n");
        sb.append("    \n");
        sb.append("    Item(double w, double v) : weight(w), value(v) {\n");
        sb.append("        ratio = value / weight;\n");
        sb.append("    }\n");
        sb.append("};\n\n");
        
        sb.append("double fractionalKnapsack(vector<Item>& items, double capacity) {\n");
        sb.append("    // Calculate ratios\n");
        sb.append("    for (auto& item : items) {\n");
        sb.append("        item.ratio = item.value / item.weight;\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    // Sort by ratio (descending)\n");
        sb.append("    sort(items.begin(), items.end(), \n");
        sb.append("         [](const Item& a, const Item& b) { return a.ratio > b.ratio; });\n");
        sb.append("    \n");
        sb.append("    double totalValue = 0.0;\n");
        sb.append("    double remaining = capacity;\n");
        sb.append("    \n");
        sb.append("    // Greedily select items\n");
        sb.append("    for (const auto& item : items) {\n");
        sb.append("        if (remaining == 0) break;\n");
        sb.append("        \n");
        sb.append("        if (item.weight <= remaining) {\n");
        sb.append("            // Take full item\n");
        sb.append("            totalValue += item.value;\n");
        sb.append("            remaining -= item.weight;\n");
        sb.append("        } else {\n");
        sb.append("            // Take fraction\n");
        sb.append("            double fraction = remaining / item.weight;\n");
        sb.append("            totalValue += item.value * fraction;\n");
        sb.append("            remaining = 0;\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    return totalValue;\n");
        sb.append("}\n\n");
        
        sb.append("int main() {\n");
        sb.append("    vector<Item> items = {\n");
        for (int i = 0; i < items.length; i++) {
            sb.append(String.format("        Item(%.1f, %.1f)", items[i].weight, items[i].value));
            if (i < items.length - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    };\n");
        sb.append(String.format("    double capacity = %.1f;\n", capacity));
        sb.append("    \n");
        sb.append("    double maxValue = fractionalKnapsack(items, capacity);\n");
        sb.append("    cout << fixed << setprecision(2);\n");
        sb.append("    cout << \"Maximum value: $\" << maxValue << endl;\n");
        sb.append("    \n");
        sb.append("    return 0;\n");
        sb.append("}\n");
        
        return sb.toString();
    }
}
