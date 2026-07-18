package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Quick Sort algorithm code in multiple languages with dynamic parameter syncing.
 * Default language: Python
 */
public class QuickSortCode implements AlgorithmCode {
    
    private int[] array;
    private String pivotStrategy;
    
    /**
     * Constructor with default values
     */
    public QuickSortCode() {
        this.array = new int[]{5, 3, 8, 4, 2, 7, 1, 6};
        this.pivotStrategy = "Last Element";
    }
    
    /**
     * Update the array and pivot strategy for dynamic code generation
     */
    public void updateParameters(int[] array, String pivotStrategy) {
        this.array = array != null ? array.clone() : new int[]{5, 3, 8, 4, 2, 7, 1, 6};
        this.pivotStrategy = pivotStrategy != null ? pivotStrategy : "Last Element";
    }
    
    /**
     * Update only the array (keep current pivot strategy)
     */
    public void updateParameters(int[] array) {
        this.array = array != null ? array.clone() : new int[]{5, 3, 8, 4, 2, 7, 1, 6};
    }
    
    @Override
    public String getAlgorithmName() {
        return "Quick Sort";
    }
    
    @Override
    public String getPythonCode() {
        StringBuilder arrayStr = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            arrayStr.append(array[i]);
            if (i < array.length - 1) arrayStr.append(", ");
        }
        
        String pivotComment = getPivotStrategyComment();
        
        return "SIZE = " + array.length + "\n" +
               "arr = [" + arrayStr + "]\n" +
               "PIVOT_STRATEGY = \"" + pivotStrategy + "\"\n" +
               "\n" +
               "def print_array(arr):\n" +
               "    for num in arr:\n" +
               "        print(num, end=\" \")\n" +
               "    print()\n" +
               "\n" +
               "def select_pivot(arr, low, high, strategy):\n" +
               "    \"\"\"" + pivotComment + "\"\"\"\n" +
               "    if strategy == \"First Element\":\n" +
               "        return low\n" +
               "    elif strategy == \"Middle Element\":\n" +
               "        return low + (high - low) // 2\n" +
               "    elif strategy == \"Random\":\n" +
               "        import random\n" +
               "        return random.randint(low, high)\n" +
               "    elif strategy == \"Median of Three\":\n" +
               "        mid = low + (high - low) // 2\n" +
               "        a, b, c = arr[low], arr[mid], arr[high]\n" +
               "        if (a <= b <= c) or (c <= b <= a):\n" +
               "            return mid\n" +
               "        elif (b <= a <= c) or (c <= a <= b):\n" +
               "            return low\n" +
               "        return high\n" +
               "    else:  # Last Element (default)\n" +
               "        return high\n" +
               "\n" +
               "def partition(arr, low, high, strategy):\n" +
               "    \"\"\"Partition array around pivot using Lomuto scheme\"\"\"\n" +
               "    pivot_idx = select_pivot(arr, low, high, strategy)\n" +
               "    \n" +
               "    # Move pivot to end\n" +
               "    arr[pivot_idx], arr[high] = arr[high], arr[pivot_idx]\n" +
               "    pivot = arr[high]\n" +
               "    \n" +
               "    print(f\"  Partitioning [{low}..{high}], pivot = {pivot}\")\n" +
               "    \n" +
               "    i = low - 1\n" +
               "    \n" +
               "    for j in range(low, high):\n" +
               "        if arr[j] <= pivot:\n" +
               "            i += 1\n" +
               "            arr[i], arr[j] = arr[j], arr[i]\n" +
               "    \n" +
               "    # Place pivot in final position\n" +
               "    arr[i + 1], arr[high] = arr[high], arr[i + 1]\n" +
               "    \n" +
               "    print(f\"  Pivot {pivot} placed at index {i + 1}\")\n" +
               "    print(f\"  Array: {arr}\")\n" +
               "    \n" +
               "    return i + 1\n" +
               "\n" +
               "def quick_sort(arr, low, high, strategy):\n" +
               "    \"\"\"Recursively sort array using quick sort\"\"\"\n" +
               "    if low < high:\n" +
               "        # Partition and get pivot index\n" +
               "        pi = partition(arr, low, high, strategy)\n" +
               "        \n" +
               "        # Recursively sort left and right partitions\n" +
               "        quick_sort(arr, low, pi - 1, strategy)\n" +
               "        quick_sort(arr, pi + 1, high, strategy)\n" +
               "\n" +
               "print(f\"Quick Sort for array of size {SIZE}\")\n" +
               "print(f\"Pivot Strategy: {PIVOT_STRATEGY}\")\n" +
               "print(\"=====================================\")\n" +
               "\n" +
               "print(\"Original Array: \", end=\"\")\n" +
               "print_array(arr)\n" +
               "print()\n" +
               "\n" +
               "print(\"Sorting process:\")\n" +
               "quick_sort(arr, 0, len(arr) - 1, PIVOT_STRATEGY)\n" +
               "print()\n" +
               "\n" +
               "print(\"=====================================\")\n" +
               "print(\"Sorted Array:   \", end=\"\")\n" +
               "print_array(arr)\n" +
               "print()\n" +
               "print(\"Time Complexity: O(n log n) average, O(n²) worst\")\n" +
               "print(\"Space Complexity: O(log n)\")\n";
    }
    
    @Override
    public String getJavaCode() {
        StringBuilder arrayStr = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            arrayStr.append(array[i]);
            if (i < array.length - 1) arrayStr.append(", ");
        }
        
        String pivotComment = getPivotStrategyComment();
        
        return "import java.util.Random;\n" +
               "\n" +
               "public class Main {\n" +
               "    static final int SIZE = " + array.length + ";\n" +
               "    static int[] arr = {" + arrayStr + "};\n" +
               "    static final String PIVOT_STRATEGY = \"" + pivotStrategy + "\";\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        System.out.println(\"Quick Sort for array of size \" + SIZE);\n" +
               "        System.out.println(\"Pivot Strategy: \" + PIVOT_STRATEGY);\n" +
               "        System.out.println(\"=====================================\");\n" +
               "\n" +
               "        System.out.print(\"Original Array: \");\n" +
               "        printArray(arr);\n" +
               "        System.out.println();\n" +
               "\n" +
               "        System.out.println(\"Sorting process:\");\n" +
               "        quickSort(arr, 0, arr.length - 1, PIVOT_STRATEGY);\n" +
               "        System.out.println();\n" +
               "\n" +
               "        System.out.println(\"=====================================\");\n" +
               "        System.out.print(\"Sorted Array:   \");\n" +
               "        printArray(arr);\n" +
               "        System.out.println();\n" +
               "        System.out.println(\"Time Complexity: O(n log n) average, O(n²) worst\");\n" +
               "        System.out.println(\"Space Complexity: O(log n)\");\n" +
               "    }\n" +
               "\n" +
               "    /**\n" +
               "     * " + pivotComment + "\n" +
               "     */\n" +
               "    static int selectPivot(int[] arr, int low, int high, String strategy) {\n" +
               "        switch (strategy) {\n" +
               "            case \"First Element\":\n" +
               "                return low;\n" +
               "            case \"Middle Element\":\n" +
               "                return low + (high - low) / 2;\n" +
               "            case \"Random\":\n" +
               "                return low + new Random().nextInt(high - low + 1);\n" +
               "            case \"Median of Three\":\n" +
               "                int mid = low + (high - low) / 2;\n" +
               "                int a = arr[low], b = arr[mid], c = arr[high];\n" +
               "                if ((a <= b && b <= c) || (c <= b && b <= a)) return mid;\n" +
               "                if ((b <= a && a <= c) || (c <= a && a <= b)) return low;\n" +
               "                return high;\n" +
               "            default: // Last Element\n" +
               "                return high;\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    static int partition(int[] arr, int low, int high, String strategy) {\n" +
               "        int pivotIdx = selectPivot(arr, low, high, strategy);\n" +
               "        \n" +
               "        // Move pivot to end\n" +
               "        swap(arr, pivotIdx, high);\n" +
               "        int pivot = arr[high];\n" +
               "        \n" +
               "        System.out.println(\"  Partitioning [\" + low + \"..\" + high + \"], pivot = \" + pivot);\n" +
               "        \n" +
               "        int i = low - 1;\n" +
               "        \n" +
               "        for (int j = low; j < high; j++) {\n" +
               "            if (arr[j] <= pivot) {\n" +
               "                i++;\n" +
               "                swap(arr, i, j);\n" +
               "            }\n" +
               "        }\n" +
               "        \n" +
               "        // Place pivot in final position\n" +
               "        swap(arr, i + 1, high);\n" +
               "        \n" +
               "        System.out.println(\"  Pivot \" + pivot + \" placed at index \" + (i + 1));\n" +
               "        System.out.print(\"  Array: \");\n" +
               "        printArray(arr);\n" +
               "        \n" +
               "        return i + 1;\n" +
               "    }\n" +
               "\n" +
               "    static void quickSort(int[] arr, int low, int high, String strategy) {\n" +
               "        if (low < high) {\n" +
               "            int pi = partition(arr, low, high, strategy);\n" +
               "            \n" +
               "            quickSort(arr, low, pi - 1, strategy);\n" +
               "            quickSort(arr, pi + 1, high, strategy);\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    static void swap(int[] arr, int i, int j) {\n" +
               "        int temp = arr[i];\n" +
               "        arr[i] = arr[j];\n" +
               "        arr[j] = temp;\n" +
               "    }\n" +
               "\n" +
               "    static void printArray(int[] arr) {\n" +
               "        for (int num : arr) {\n" +
               "            System.out.print(num + \" \");\n" +
               "        }\n" +
               "        System.out.println();\n" +
               "    }\n" +
               "}\n";
    }
    
    @Override
    public String getCCode() {
        StringBuilder arrayStr = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            arrayStr.append(array[i]);
            if (i < array.length - 1) arrayStr.append(", ");
        }
        
        String pivotComment = getPivotStrategyComment();
        
        return "#include <stdio.h>\n" +
               "#include <stdlib.h>\n" +
               "#include <time.h>\n" +
               "\n" +
               "#define SIZE " + array.length + "\n" +
               "\n" +
               "int arr[SIZE] = {" + arrayStr + "};\n" +
               "const char* PIVOT_STRATEGY = \"" + pivotStrategy + "\";\n" +
               "\n" +
               "void printArray(int arr[], int size) {\n" +
               "    for (int i = 0; i < size; i++)\n" +
               "        printf(\"%d \", arr[i]);\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "void swap(int* a, int* b) {\n" +
               "    int temp = *a;\n" +
               "    *a = *b;\n" +
               "    *b = temp;\n" +
               "}\n" +
               "\n" +
               "/* " + pivotComment + " */\n" +
               "int selectPivot(int arr[], int low, int high, const char* strategy) {\n" +
               "    if (strcmp(strategy, \"First Element\") == 0) {\n" +
               "        return low;\n" +
               "    } else if (strcmp(strategy, \"Middle Element\") == 0) {\n" +
               "        return low + (high - low) / 2;\n" +
               "    } else if (strcmp(strategy, \"Random\") == 0) {\n" +
               "        return low + rand() % (high - low + 1);\n" +
               "    } else if (strcmp(strategy, \"Median of Three\") == 0) {\n" +
               "        int mid = low + (high - low) / 2;\n" +
               "        int a = arr[low], b = arr[mid], c = arr[high];\n" +
               "        if ((a <= b && b <= c) || (c <= b && b <= a)) return mid;\n" +
               "        if ((b <= a && a <= c) || (c <= a && a <= b)) return low;\n" +
               "        return high;\n" +
               "    }\n" +
               "    return high; // Last Element (default)\n" +
               "}\n" +
               "\n" +
               "int partition(int arr[], int low, int high, const char* strategy) {\n" +
               "    int pivotIdx = selectPivot(arr, low, high, strategy);\n" +
               "    \n" +
               "    // Move pivot to end\n" +
               "    swap(&arr[pivotIdx], &arr[high]);\n" +
               "    int pivot = arr[high];\n" +
               "    \n" +
               "    printf(\"  Partitioning [%d..%d], pivot = %d\\n\", low, high, pivot);\n" +
               "    \n" +
               "    int i = low - 1;\n" +
               "    \n" +
               "    for (int j = low; j < high; j++) {\n" +
               "        if (arr[j] <= pivot) {\n" +
               "            i++;\n" +
               "            swap(&arr[i], &arr[j]);\n" +
               "        }\n" +
               "    }\n" +
               "    \n" +
               "    // Place pivot in final position\n" +
               "    swap(&arr[i + 1], &arr[high]);\n" +
               "    \n" +
               "    printf(\"  Pivot %d placed at index %d\\n\", pivot, i + 1);\n" +
               "    printf(\"  Array: \");\n" +
               "    printArray(arr, SIZE);\n" +
               "    \n" +
               "    return i + 1;\n" +
               "}\n" +
               "\n" +
               "void quickSort(int arr[], int low, int high, const char* strategy) {\n" +
               "    if (low < high) {\n" +
               "        int pi = partition(arr, low, high, strategy);\n" +
               "        \n" +
               "        quickSort(arr, low, pi - 1, strategy);\n" +
               "        quickSort(arr, pi + 1, high, strategy);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    srand(time(NULL));\n" +
               "    \n" +
               "    printf(\"Quick Sort for array of size %d\\n\", SIZE);\n" +
               "    printf(\"Pivot Strategy: %s\\n\", PIVOT_STRATEGY);\n" +
               "    printf(\"=====================================\\n\");\n" +
               "\n" +
               "    printf(\"Original Array: \");\n" +
               "    printArray(arr, SIZE);\n" +
               "    printf(\"\\n\");\n" +
               "\n" +
               "    printf(\"Sorting process:\\n\");\n" +
               "    quickSort(arr, 0, SIZE - 1, PIVOT_STRATEGY);\n" +
               "    printf(\"\\n\");\n" +
               "\n" +
               "    printf(\"=====================================\\n\");\n" +
               "    printf(\"Sorted Array:   \");\n" +
               "    printArray(arr, SIZE);\n" +
               "    printf(\"\\n\");\n" +
               "    printf(\"Time Complexity: O(n log n) average, O(n²) worst\\n\");\n" +
               "    printf(\"Space Complexity: O(log n)\\n\");\n" +
               "\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getCppCode() {
        StringBuilder arrayStr = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            arrayStr.append(array[i]);
            if (i < array.length - 1) arrayStr.append(", ");
        }
        
        String pivotComment = getPivotStrategyComment();
        
        return "#include <iostream>\n" +
               "#include <cstdlib>\n" +
               "#include <ctime>\n" +
               "#include <string>\n" +
               "using namespace std;\n" +
               "\n" +
               "const int SIZE = " + array.length + ";\n" +
               "int arr[SIZE] = {" + arrayStr + "};\n" +
               "const string PIVOT_STRATEGY = \"" + pivotStrategy + "\";\n" +
               "\n" +
               "void printArray(int arr[], int size) {\n" +
               "    for (int i = 0; i < size; i++)\n" +
               "        cout << arr[i] << \" \";\n" +
               "    cout << endl;\n" +
               "}\n" +
               "\n" +
               "void swap(int& a, int& b) {\n" +
               "    int temp = a;\n" +
               "    a = b;\n" +
               "    b = temp;\n" +
               "}\n" +
               "\n" +
               "// " + pivotComment + "\n" +
               "int selectPivot(int arr[], int low, int high, const string& strategy) {\n" +
               "    if (strategy == \"First Element\") {\n" +
               "        return low;\n" +
               "    } else if (strategy == \"Middle Element\") {\n" +
               "        return low + (high - low) / 2;\n" +
               "    } else if (strategy == \"Random\") {\n" +
               "        return low + rand() % (high - low + 1);\n" +
               "    } else if (strategy == \"Median of Three\") {\n" +
               "        int mid = low + (high - low) / 2;\n" +
               "        int a = arr[low], b = arr[mid], c = arr[high];\n" +
               "        if ((a <= b && b <= c) || (c <= b && b <= a)) return mid;\n" +
               "        if ((b <= a && a <= c) || (c <= a && a <= b)) return low;\n" +
               "        return high;\n" +
               "    }\n" +
               "    return high; // Last Element (default)\n" +
               "}\n" +
               "\n" +
               "int partition(int arr[], int low, int high, const string& strategy) {\n" +
               "    int pivotIdx = selectPivot(arr, low, high, strategy);\n" +
               "    \n" +
               "    // Move pivot to end\n" +
               "    swap(arr[pivotIdx], arr[high]);\n" +
               "    int pivot = arr[high];\n" +
               "    \n" +
               "    cout << \"  Partitioning [\" << low << \"..\" << high << \"], pivot = \" << pivot << endl;\n" +
               "    \n" +
               "    int i = low - 1;\n" +
               "    \n" +
               "    for (int j = low; j < high; j++) {\n" +
               "        if (arr[j] <= pivot) {\n" +
               "            i++;\n" +
               "            swap(arr[i], arr[j]);\n" +
               "        }\n" +
               "    }\n" +
               "    \n" +
               "    // Place pivot in final position\n" +
               "    swap(arr[i + 1], arr[high]);\n" +
               "    \n" +
               "    cout << \"  Pivot \" << pivot << \" placed at index \" << (i + 1) << endl;\n" +
               "    cout << \"  Array: \";\n" +
               "    printArray(arr, SIZE);\n" +
               "    \n" +
               "    return i + 1;\n" +
               "}\n" +
               "\n" +
               "void quickSort(int arr[], int low, int high, const string& strategy) {\n" +
               "    if (low < high) {\n" +
               "        int pi = partition(arr, low, high, strategy);\n" +
               "        \n" +
               "        quickSort(arr, low, pi - 1, strategy);\n" +
               "        quickSort(arr, pi + 1, high, strategy);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    srand(time(NULL));\n" +
               "    \n" +
               "    cout << \"Quick Sort for array of size \" << SIZE << endl;\n" +
               "    cout << \"Pivot Strategy: \" << PIVOT_STRATEGY << endl;\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "\n" +
               "    cout << \"Original Array: \";\n" +
               "    printArray(arr, SIZE);\n" +
               "    cout << endl;\n" +
               "\n" +
               "    cout << \"Sorting process:\" << endl;\n" +
               "    quickSort(arr, 0, SIZE - 1, PIVOT_STRATEGY);\n" +
               "    cout << endl;\n" +
               "\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "    cout << \"Sorted Array:   \";\n" +
               "    printArray(arr, SIZE);\n" +
               "    cout << endl;\n" +
               "    cout << \"Time Complexity: O(n log n) average, O(n²) worst\" << endl;\n" +
               "    cout << \"Space Complexity: O(log n)\" << endl;\n" +
               "\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    private String getPivotStrategyComment() {
        switch (pivotStrategy) {
            case "First Element":
                return "Select first element as pivot";
            case "Middle Element":
                return "Select middle element as pivot";
            case "Random":
                return "Select random element as pivot";
            case "Median of Three":
                return "Select median of first, middle, and last elements as pivot";
            case "Last Element":
            default:
                return "Select last element as pivot (Lomuto scheme)";
        }
    }
}
