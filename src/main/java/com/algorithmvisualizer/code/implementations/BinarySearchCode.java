package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Binary Search algorithm code in multiple languages with dynamic parameter syncing
 */
public class BinarySearchCode implements AlgorithmCode {
    
    private int[] array;
    private int target;
    
    /**
     * Constructor with default values
     */
    public BinarySearchCode() {
        this.array = new int[]{1, 3, 5, 7, 9, 11};
        this.target = 7;
    }
    
    /**
     * Update the array and target for dynamic code generation
     */
    public void updateParameters(int[] array, int target) {
        this.array = array != null ? array.clone() : new int[]{1, 3, 5, 7, 9, 11};
        this.target = target;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Binary Search";
    }
    
    @Override
    public String getJavaCode() {
        StringBuilder arrayStr = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            arrayStr.append(array[i]);
            if (i < array.length - 1) arrayStr.append(", ");
        }
        
        return "public class Main {\n" +
               "    static final int[] arr = {" + arrayStr + "};\n" +
               "    static final int SIZE = " + array.length + ";\n" +
               "    static final int TARGET = " + target + ";\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        System.out.println(\"Binary Search in sorted array of size \" + SIZE);\n" +
               "        System.out.println(\"=====================================\");\n" +
               "        System.out.print(\"Array: \");\n" +
               "        printArray(arr);\n" +
               "        System.out.println(\"Element to search: \" + TARGET);\n" +
               "\n" +
               "        int result = binarySearch(arr, TARGET);\n" +
               "\n" +
               "        System.out.println(\"=====================================\");\n" +
               "\n" +
               "        if (result == -1) {\n" +
               "            System.out.println(\"Element \" + TARGET + \" not found.\");\n" +
               "        } else {\n" +
               "            System.out.println(\"Element \" + TARGET + \" found at index \" + result);\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    static int binarySearch(int[] arr, int target) {\n" +
               "        int left = 0, right = arr.length - 1;\n" +
               "        int step = 1;\n" +
               "\n" +
               "        while (left <= right) {\n" +
               "            int mid = (left + right) / 2;\n" +
               "            System.out.println(\"Step \" + step + \": left=\" + left + \" right=\" + right + \" mid=\" + mid + \" (value=\" + arr[mid] + \")\");\n" +
               "            step++;\n" +
               "\n" +
               "            if (arr[mid] == target) {\n" +
               "                return mid;\n" +
               "            }\n" +
               "            if (arr[mid] < target) {\n" +
               "                left = mid + 1;\n" +
               "            } else {\n" +
               "                right = mid - 1;\n" +
               "            }\n" +
               "        }\n" +
               "        return -1;\n" +
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
               "#define TARGET " + target + "\n" +
               "\n" +
               "int arr[SIZE] = {" + arrayStr + "};\n" +
               "\n" +
               "void printArray(int arr[], int size) {\n" +
               "    for (int i = 0; i < size; i++)\n" +
               "        printf(\"%d \", arr[i]);\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "int binarySearch(int arr[], int size, int target) {\n" +
               "    int left = 0, right = size - 1;\n" +
               "    int step = 1;\n" +
               "\n" +
               "    while (left <= right) {\n" +
               "        int mid = (left + right) / 2;\n" +
               "        printf(\"Step %d: left=%d right=%d mid=%d (value=%d)\\n\",\n" +
               "               step, left, right, mid, arr[mid]);\n" +
               "        step++;\n" +
               "\n" +
               "        if (arr[mid] == target)\n" +
               "            return mid;\n" +
               "        if (arr[mid] < target)\n" +
               "            left = mid + 1;\n" +
               "        else\n" +
               "            right = mid - 1;\n" +
               "    }\n" +
               "    return -1;\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    printf(\"Binary Search in sorted array of size %d\\n\", SIZE);\n" +
               "    printf(\"=====================================\\n\");\n" +
               "    printf(\"Array: \");\n" +
               "    printArray(arr, SIZE);\n" +
               "    printf(\"Element to search: %d\\n\", TARGET);\n" +
               "\n" +
               "    int result = binarySearch(arr, SIZE, TARGET);\n" +
               "\n" +
               "    printf(\"=====================================\\n\");\n" +
               "\n" +
               "    if (result == -1)\n" +
               "        printf(\"Element %d not found.\\n\", TARGET);\n" +
               "    else\n" +
               "        printf(\"Element %d found at index %d\\n\", TARGET, result);\n" +
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
               "const int TARGET = " + target + ";\n" +
               "int arr[SIZE] = {" + arrayStr + "};\n" +
               "\n" +
               "void printArray(int arr[], int size) {\n" +
               "    for (int i = 0; i < size; i++)\n" +
               "        cout << arr[i] << \" \";\n" +
               "    cout << endl;\n" +
               "}\n" +
               "\n" +
               "int binarySearch(int arr[], int size, int target) {\n" +
               "    int left = 0, right = size - 1;\n" +
               "    int step = 1;\n" +
               "\n" +
               "    while (left <= right) {\n" +
               "        int mid = (left + right) / 2;\n" +
               "        cout << \"Step \" << step << \": left=\" << left\n" +
               "             << \" right=\" << right\n" +
               "             << \" mid=\" << mid\n" +
               "             << \" (value=\" << arr[mid] << \")\" << endl;\n" +
               "        step++;\n" +
               "\n" +
               "        if (arr[mid] == target)\n" +
               "            return mid;\n" +
               "        if (arr[mid] < target)\n" +
               "            left = mid + 1;\n" +
               "        else\n" +
               "            right = mid - 1;\n" +
               "    }\n" +
               "    return -1;\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    cout << \"Binary Search in sorted array of size \" << SIZE << endl;\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "    cout << \"Array: \";\n" +
               "    printArray(arr, SIZE);\n" +
               "    cout << \"Element to search: \" << TARGET << endl;\n" +
               "\n" +
               "    int result = binarySearch(arr, SIZE, TARGET);\n" +
               "\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "\n" +
               "    if (result == -1)\n" +
               "        cout << \"Element \" << TARGET << \" not found.\" << endl;\n" +
               "    else\n" +
               "        cout << \"Element \" << TARGET << \" found at index \" << result << endl;\n" +
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
               "TARGET = " + target + "\n" +
               "arr = [" + arrayStr + "]\n" +
               "\n" +
               "def print_array(arr):\n" +
               "    for num in arr:\n" +
               "        print(num, end=\" \")\n" +
               "    print()\n" +
               "\n" +
               "def binary_search(arr, target):\n" +
               "    left, right = 0, len(arr) - 1\n" +
               "    step = 1\n" +
               "\n" +
               "    while left <= right:\n" +
               "        mid = (left + right) // 2\n" +
               "        print(f\"Step {step}: left={left} right={right} mid={mid} (value={arr[mid]})\")\n" +
               "        step += 1\n" +
               "\n" +
               "        if arr[mid] == target:\n" +
               "            return mid\n" +
               "        if arr[mid] < target:\n" +
               "            left = mid + 1\n" +
               "        else:\n" +
               "            right = mid - 1\n" +
               "    return -1\n" +
               "\n" +
               "print(f\"Binary Search in sorted array of size {SIZE}\")\n" +
               "print(\"=====================================\")\n" +
               "print(\"Array: \", end=\"\")\n" +
               "print_array(arr)\n" +
               "print(f\"Element to search: {TARGET}\")\n" +
               "\n" +
               "result = binary_search(arr, TARGET)\n" +
               "\n" +
               "print(\"=====================================\")\n" +
               "\n" +
               "if result == -1:\n" +
               "    print(f\"Element {TARGET} not found.\")\n" +
               "else:\n" +
               "    print(f\"Element {TARGET} found at index {result}\")\n";
    }
}
