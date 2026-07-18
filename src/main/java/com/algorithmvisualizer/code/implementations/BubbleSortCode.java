package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Bubble Sort algorithm code in multiple languages with dynamic parameter syncing
 */
public class BubbleSortCode implements AlgorithmCode {
    
    private int[] array;
    
    /**
     * Constructor with default values
     */
    public BubbleSortCode() {
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
        return "Bubble Sort";
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
               "        System.out.println(\"Bubble Sort for array of size \" + SIZE);\n" +
               "        System.out.println(\"=====================================\");\n" +
               "\n" +
               "        System.out.print(\"Original Array: \");\n" +
               "        printArray(arr);\n" +
               "\n" +
               "        bubbleSort(arr);\n" +
               "\n" +
               "        System.out.println(\"=====================================\");\n" +
               "        System.out.print(\"Sorted Array:   \");\n" +
               "        printArray(arr);\n" +
               "    }\n" +
               "\n" +
               "    static void bubbleSort(int[] arr) {\n" +
               "        int n = arr.length;\n" +
               "        for (int i = 0; i < n - 1; i++) {\n" +
               "            for (int j = 0; j < n - 1 - i; j++) {\n" +
               "                if (arr[j] > arr[j + 1]) {\n" +
               "                    int temp = arr[j];\n" +
               "                    arr[j] = arr[j + 1];\n" +
               "                    arr[j + 1] = temp;\n" +
               "                }\n" +
               "            }\n" +
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
               "void bubbleSort(int arr[], int size) {\n" +
               "    for (int i = 0; i < size - 1; i++) {\n" +
               "        for (int j = 0; j < size - 1 - i; j++) {\n" +
               "            if (arr[j] > arr[j + 1]) {\n" +
               "                int temp = arr[j];\n" +
               "                arr[j] = arr[j + 1];\n" +
               "                arr[j + 1] = temp;\n" +
               "            }\n" +
               "        }\n" +
               "        printf(\"After pass %d: \", i + 1);\n" +
               "        printArray(arr, size);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    printf(\"Bubble Sort for array of size %d\\n\", SIZE);\n" +
               "    printf(\"=====================================\\n\");\n" +
               "\n" +
               "    printf(\"Original Array: \");\n" +
               "    printArray(arr, SIZE);\n" +
               "\n" +
               "    bubbleSort(arr, SIZE);\n" +
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
               "void bubbleSort(int arr[], int size) {\n" +
               "    for (int i = 0; i < size - 1; i++) {\n" +
               "        for (int j = 0; j < size - 1 - i; j++) {\n" +
               "            if (arr[j] > arr[j + 1]) {\n" +
               "                int temp = arr[j];\n" +
               "                arr[j] = arr[j + 1];\n" +
               "                arr[j + 1] = temp;\n" +
               "            }\n" +
               "        }\n" +
               "        cout << \"After pass \" << i + 1 << \": \";\n" +
               "        printArray(arr, size);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    cout << \"Bubble Sort for array of size \" << SIZE << endl;\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "\n" +
               "    cout << \"Original Array: \";\n" +
               "    printArray(arr, SIZE);\n" +
               "\n" +
               "    bubbleSort(arr, SIZE);\n" +
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
               "def bubble_sort(arr):\n" +
               "    n = len(arr)\n" +
               "    for i in range(n - 1):\n" +
               "        for j in range(n - 1 - i):\n" +
               "            if arr[j] > arr[j + 1]:\n" +
               "                arr[j], arr[j + 1] = arr[j + 1], arr[j]\n" +
               "        print(f\"After pass {i + 1}: \", end=\"\")\n" +
               "        print_array(arr)\n" +
               "\n" +
               "print(f\"Bubble Sort for array of size {SIZE}\")\n" +
               "print(\"=====================================\")\n" +
               "\n" +
               "print(\"Original Array: \", end=\"\")\n" +
               "print_array(arr)\n" +
               "\n" +
               "bubble_sort(arr)\n" +
               "\n" +
               "print(\"=====================================\")\n" +
               "\n" +
               "print(\"Sorted Array:   \", end=\"\")\n" +
               "print_array(arr)\n";
    }
}
