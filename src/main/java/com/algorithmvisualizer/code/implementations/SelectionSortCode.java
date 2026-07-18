package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Selection Sort algorithm code in multiple languages with dynamic parameter syncing
 */
public class SelectionSortCode implements AlgorithmCode {
    
    private int[] array;
    
    /**
     * Constructor with default values
     */
    public SelectionSortCode() {
        this.array = new int[]{5, 3, 8, 4, 2};
    }
    
    /**
     * Update the array for dynamic code generation
     */
    public void updateParameters(int[] array) {
        this.array = array != null ? array.clone() : new int[]{5, 3, 8, 4, 2};
    }
    
    @Override
    public String getAlgorithmName() {
        return "Selection Sort";
    }
    
    @Override
    public String getJavaCode() {
        StringBuilder arrayStr = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            arrayStr.append(array[i]);
            if (i < array.length - 1) arrayStr.append(", ");
        }
        
        return "public class Main {\n" +
               "    static final int SIZE = " + array.length + ";\n" +
               "    static int[] arr = {" + arrayStr + "};\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        System.out.println(\"Selection Sort for array of size \" + SIZE);\n" +
               "        System.out.println(\"=====================================\");\n" +
               "\n" +
               "        System.out.print(\"Original Array: \");\n" +
               "        printArray(arr);\n" +
               "\n" +
               "        selectionSort(arr);\n" +
               "\n" +
               "        System.out.println(\"=====================================\");\n" +
               "        System.out.print(\"Sorted Array:   \");\n" +
               "        printArray(arr);\n" +
               "    }\n" +
               "\n" +
               "    static void selectionSort(int[] arr) {\n" +
               "        int n = arr.length;\n" +
               "        for (int i = 0; i < n - 1; i++) {\n" +
               "            int minIdx = i;\n" +
               "            for (int j = i + 1; j < n; j++) {\n" +
               "                if (arr[j] < arr[minIdx]) {\n" +
               "                    minIdx = j;\n" +
               "                }\n" +
               "            }\n" +
               "            int temp = arr[minIdx];\n" +
               "            arr[minIdx] = arr[i];\n" +
               "            arr[i] = temp;\n" +
               "\n" +
               "            System.out.print(\"After pass \" + (i + 1) + \": \");\n" +
               "            printArray(arr);\n" +
               "        }\n" +
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
        
        return "#include <stdio.h>\n" +
               "\n" +
               "#define SIZE " + array.length + "\n" +
               "\n" +
               "int arr[SIZE] = {" + arrayStr + "};\n" +
               "\n" +
               "void printArray(int arr[], int size) {\n" +
               "    for (int i = 0; i < size; i++)\n" +
               "        printf(\"%d \", arr[i]);\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "void selectionSort(int arr[], int size) {\n" +
               "    for (int i = 0; i < size - 1; i++) {\n" +
               "        int minIdx = i;\n" +
               "        for (int j = i + 1; j < size; j++) {\n" +
               "            if (arr[j] < arr[minIdx]) {\n" +
               "                minIdx = j;\n" +
               "            }\n" +
               "        }\n" +
               "        int temp = arr[minIdx];\n" +
               "        arr[minIdx] = arr[i];\n" +
               "        arr[i] = temp;\n" +
               "\n" +
               "        printf(\"After pass %d: \", i + 1);\n" +
               "        printArray(arr, size);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    printf(\"Selection Sort for array of size %d\\n\", SIZE);\n" +
               "    printf(\"=====================================\\n\");\n" +
               "\n" +
               "    printf(\"Original Array: \");\n" +
               "    printArray(arr, SIZE);\n" +
               "\n" +
               "    selectionSort(arr, SIZE);\n" +
               "\n" +
               "    printf(\"=====================================\\n\");\n" +
               "\n" +
               "    printf(\"Sorted Array:   \");\n" +
               "    printArray(arr, SIZE);\n" +
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
        
        return "#include <iostream>\n" +
               "using namespace std;\n" +
               "\n" +
               "const int SIZE = " + array.length + ";\n" +
               "int arr[SIZE] = {" + arrayStr + "};\n" +
               "\n" +
               "void printArray(int arr[], int size) {\n" +
               "    for (int i = 0; i < size; i++)\n" +
               "        cout << arr[i] << \" \";\n" +
               "    cout << endl;\n" +
               "}\n" +
               "\n" +
               "void selectionSort(int arr[], int size) {\n" +
               "    for (int i = 0; i < size - 1; i++) {\n" +
               "        int minIdx = i;\n" +
               "        for (int j = i + 1; j < size; j++) {\n" +
               "            if (arr[j] < arr[minIdx]) {\n" +
               "                minIdx = j;\n" +
               "            }\n" +
               "        }\n" +
               "        int temp = arr[minIdx];\n" +
               "        arr[minIdx] = arr[i];\n" +
               "        arr[i] = temp;\n" +
               "\n" +
               "        cout << \"After pass \" << i + 1 << \": \";\n" +
               "        printArray(arr, size);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    cout << \"Selection Sort for array of size \" << SIZE << endl;\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "\n" +
               "    cout << \"Original Array: \";\n" +
               "    printArray(arr, SIZE);\n" +
               "\n" +
               "    selectionSort(arr, SIZE);\n" +
               "\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "\n" +
               "    cout << \"Sorted Array:   \";\n" +
               "    printArray(arr, SIZE);\n" +
               "\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getPythonCode() {
        StringBuilder arrayStr = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            arrayStr.append(array[i]);
            if (i < array.length - 1) arrayStr.append(", ");
        }
        
        return "SIZE = " + array.length + "\n" +
               "arr = [" + arrayStr + "]\n" +
               "\n" +
               "def print_array(arr):\n" +
               "    for num in arr:\n" +
               "        print(num, end=\" \")\n" +
               "    print()\n" +
               "\n" +
               "def selection_sort(arr):\n" +
               "    n = len(arr)\n" +
               "    for i in range(n - 1):\n" +
               "        min_idx = i\n" +
               "        for j in range(i + 1, n):\n" +
               "            if arr[j] < arr[min_idx]:\n" +
               "                min_idx = j\n" +
               "        arr[i], arr[min_idx] = arr[min_idx], arr[i]\n" +
               "\n" +
               "        print(f\"After pass {i + 1}: \", end=\"\")\n" +
               "        print_array(arr)\n" +
               "\n" +
               "print(f\"Selection Sort for array of size {SIZE}\")\n" +
               "print(\"=====================================\")\n" +
               "\n" +
               "print(\"Original Array: \", end=\"\")\n" +
               "print_array(arr)\n" +
               "\n" +
               "selection_sort(arr)\n" +
               "\n" +
               "print(\"=====================================\")\n" +
               "\n" +
               "print(\"Sorted Array:   \", end=\"\")\n" +
               "print_array(arr)\n";
    }
}
