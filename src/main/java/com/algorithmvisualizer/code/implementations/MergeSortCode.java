package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;

/**
 * Merge Sort algorithm code in multiple languages with dynamic parameter syncing.
 * Default language: Python
 */
public class MergeSortCode implements AlgorithmCode {
    
    private int[] array;
    
    /**
     * Constructor with default values
     */
    public MergeSortCode() {
        this.array = new int[]{5, 3, 8, 4, 2, 7, 1, 6};
    }
    
    /**
     * Update the array for dynamic code generation
     */
    public void updateParameters(int[] array) {
        this.array = array != null ? array.clone() : new int[]{5, 3, 8, 4, 2, 7, 1, 6};
    }
    
    @Override
    public String getAlgorithmName() {
        return "Merge Sort";
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
               "def merge(arr, left, mid, right):\n" +
               "    \"\"\"Merge two sorted subarrays arr[left..mid] and arr[mid+1..right]\"\"\"\n" +
               "    # Create temporary arrays\n" +
               "    left_arr = arr[left:mid + 1]\n" +
               "    right_arr = arr[mid + 1:right + 1]\n" +
               "    \n" +
               "    i = j = 0  # Initial indexes for left and right subarrays\n" +
               "    k = left   # Initial index for merged array\n" +
               "    \n" +
               "    print(f\"  Merging {left_arr} and {right_arr}\")\n" +
               "    \n" +
               "    # Merge the temp arrays back\n" +
               "    while i < len(left_arr) and j < len(right_arr):\n" +
               "        if left_arr[i] <= right_arr[j]:\n" +
               "            arr[k] = left_arr[i]\n" +
               "            i += 1\n" +
               "        else:\n" +
               "            arr[k] = right_arr[j]\n" +
               "            j += 1\n" +
               "        k += 1\n" +
               "    \n" +
               "    # Copy remaining elements\n" +
               "    while i < len(left_arr):\n" +
               "        arr[k] = left_arr[i]\n" +
               "        i += 1\n" +
               "        k += 1\n" +
               "    \n" +
               "    while j < len(right_arr):\n" +
               "        arr[k] = right_arr[j]\n" +
               "        j += 1\n" +
               "        k += 1\n" +
               "    \n" +
               "    print(f\"  Result: {arr[left:right + 1]}\")\n" +
               "\n" +
               "def merge_sort(arr, left, right):\n" +
               "    \"\"\"Recursively sort array using merge sort\"\"\"\n" +
               "    if left < right:\n" +
               "        mid = left + (right - left) // 2\n" +
               "        \n" +
               "        # Sort first and second halves\n" +
               "        merge_sort(arr, left, mid)\n" +
               "        merge_sort(arr, mid + 1, right)\n" +
               "        \n" +
               "        # Merge the sorted halves\n" +
               "        merge(arr, left, mid, right)\n" +
               "\n" +
               "print(f\"Merge Sort for array of size {SIZE}\")\n" +
               "print(\"=====================================\")\n" +
               "\n" +
               "print(\"Original Array: \", end=\"\")\n" +
               "print_array(arr)\n" +
               "print()\n" +
               "\n" +
               "print(\"Sorting process:\")\n" +
               "merge_sort(arr, 0, len(arr) - 1)\n" +
               "print()\n" +
               "\n" +
               "print(\"=====================================\")\n" +
               "print(\"Sorted Array:   \", end=\"\")\n" +
               "print_array(arr)\n" +
               "print()\n" +
               "print(\"Time Complexity: O(n log n)\")\n" +
               "print(\"Space Complexity: O(n)\")\n";
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
               "        System.out.println(\"Merge Sort for array of size \" + SIZE);\n" +
               "        System.out.println(\"=====================================\");\n" +
               "\n" +
               "        System.out.print(\"Original Array: \");\n" +
               "        printArray(arr);\n" +
               "        System.out.println();\n" +
               "\n" +
               "        System.out.println(\"Sorting process:\");\n" +
               "        mergeSort(arr, 0, arr.length - 1);\n" +
               "        System.out.println();\n" +
               "\n" +
               "        System.out.println(\"=====================================\");\n" +
               "        System.out.print(\"Sorted Array:   \");\n" +
               "        printArray(arr);\n" +
               "        System.out.println();\n" +
               "        System.out.println(\"Time Complexity: O(n log n)\");\n" +
               "        System.out.println(\"Space Complexity: O(n)\");\n" +
               "    }\n" +
               "\n" +
               "    static void merge(int[] arr, int left, int mid, int right) {\n" +
               "        // Find sizes of two subarrays to be merged\n" +
               "        int n1 = mid - left + 1;\n" +
               "        int n2 = right - mid;\n" +
               "\n" +
               "        // Create temp arrays\n" +
               "        int[] leftArr = new int[n1];\n" +
               "        int[] rightArr = new int[n2];\n" +
               "\n" +
               "        // Copy data to temp arrays\n" +
               "        for (int i = 0; i < n1; i++)\n" +
               "            leftArr[i] = arr[left + i];\n" +
               "        for (int j = 0; j < n2; j++)\n" +
               "            rightArr[j] = arr[mid + 1 + j];\n" +
               "\n" +
               "        System.out.print(\"  Merging \");\n" +
               "        printSubArray(leftArr);\n" +
               "        System.out.print(\" and \");\n" +
               "        printSubArray(rightArr);\n" +
               "        System.out.println();\n" +
               "\n" +
               "        // Merge the temp arrays\n" +
               "        int i = 0, j = 0;\n" +
               "        int k = left;\n" +
               "        while (i < n1 && j < n2) {\n" +
               "            if (leftArr[i] <= rightArr[j]) {\n" +
               "                arr[k] = leftArr[i];\n" +
               "                i++;\n" +
               "            } else {\n" +
               "                arr[k] = rightArr[j];\n" +
               "                j++;\n" +
               "            }\n" +
               "            k++;\n" +
               "        }\n" +
               "\n" +
               "        // Copy remaining elements\n" +
               "        while (i < n1) {\n" +
               "            arr[k] = leftArr[i];\n" +
               "            i++;\n" +
               "            k++;\n" +
               "        }\n" +
               "        while (j < n2) {\n" +
               "            arr[k] = rightArr[j];\n" +
               "            j++;\n" +
               "            k++;\n" +
               "        }\n" +
               "\n" +
               "        System.out.print(\"  Result: \");\n" +
               "        printRange(arr, left, right);\n" +
               "        System.out.println();\n" +
               "    }\n" +
               "\n" +
               "    static void mergeSort(int[] arr, int left, int right) {\n" +
               "        if (left < right) {\n" +
               "            int mid = left + (right - left) / 2;\n" +
               "\n" +
               "            // Sort first and second halves\n" +
               "            mergeSort(arr, left, mid);\n" +
               "            mergeSort(arr, mid + 1, right);\n" +
               "\n" +
               "            // Merge the sorted halves\n" +
               "            merge(arr, left, mid, right);\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    static void printArray(int[] arr) {\n" +
               "        for (int num : arr) {\n" +
               "            System.out.print(num + \" \");\n" +
               "        }\n" +
               "        System.out.println();\n" +
               "    }\n" +
               "\n" +
               "    static void printSubArray(int[] arr) {\n" +
               "        System.out.print(\"[\");\n" +
               "        for (int i = 0; i < arr.length; i++) {\n" +
               "            System.out.print(arr[i]);\n" +
               "            if (i < arr.length - 1) System.out.print(\", \");\n" +
               "        }\n" +
               "        System.out.print(\"]\");\n" +
               "    }\n" +
               "\n" +
               "    static void printRange(int[] arr, int left, int right) {\n" +
               "        System.out.print(\"[\");\n" +
               "        for (int i = left; i <= right; i++) {\n" +
               "            System.out.print(arr[i]);\n" +
               "            if (i < right) System.out.print(\", \");\n" +
               "        }\n" +
               "        System.out.print(\"]\");\n" +
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
               "void printRange(int arr[], int left, int right) {\n" +
               "    printf(\"[\");\n" +
               "    for (int i = left; i <= right; i++) {\n" +
               "        printf(\"%d\", arr[i]);\n" +
               "        if (i < right) printf(\", \");\n" +
               "    }\n" +
               "    printf(\"]\");\n" +
               "}\n" +
               "\n" +
               "void merge(int arr[], int left, int mid, int right) {\n" +
               "    int n1 = mid - left + 1;\n" +
               "    int n2 = right - mid;\n" +
               "\n" +
               "    // Create temp arrays\n" +
               "    int leftArr[n1], rightArr[n2];\n" +
               "\n" +
               "    // Copy data to temp arrays\n" +
               "    for (int i = 0; i < n1; i++)\n" +
               "        leftArr[i] = arr[left + i];\n" +
               "    for (int j = 0; j < n2; j++)\n" +
               "        rightArr[j] = arr[mid + 1 + j];\n" +
               "\n" +
               "    printf(\"  Merging \");\n" +
               "    printRange(leftArr, 0, n1 - 1);\n" +
               "    printf(\" and \");\n" +
               "    printRange(rightArr, 0, n2 - 1);\n" +
               "    printf(\"\\n\");\n" +
               "\n" +
               "    // Merge the temp arrays back\n" +
               "    int i = 0, j = 0, k = left;\n" +
               "    while (i < n1 && j < n2) {\n" +
               "        if (leftArr[i] <= rightArr[j]) {\n" +
               "            arr[k] = leftArr[i];\n" +
               "            i++;\n" +
               "        } else {\n" +
               "            arr[k] = rightArr[j];\n" +
               "            j++;\n" +
               "        }\n" +
               "        k++;\n" +
               "    }\n" +
               "\n" +
               "    // Copy remaining elements\n" +
               "    while (i < n1) {\n" +
               "        arr[k] = leftArr[i];\n" +
               "        i++;\n" +
               "        k++;\n" +
               "    }\n" +
               "    while (j < n2) {\n" +
               "        arr[k] = rightArr[j];\n" +
               "        j++;\n" +
               "        k++;\n" +
               "    }\n" +
               "\n" +
               "    printf(\"  Result: \");\n" +
               "    printRange(arr, left, right);\n" +
               "    printf(\"\\n\");\n" +
               "}\n" +
               "\n" +
               "void mergeSort(int arr[], int left, int right) {\n" +
               "    if (left < right) {\n" +
               "        int mid = left + (right - left) / 2;\n" +
               "\n" +
               "        // Sort first and second halves\n" +
               "        mergeSort(arr, left, mid);\n" +
               "        mergeSort(arr, mid + 1, right);\n" +
               "\n" +
               "        // Merge the sorted halves\n" +
               "        merge(arr, left, mid, right);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    printf(\"Merge Sort for array of size %d\\n\", SIZE);\n" +
               "    printf(\"=====================================\\n\");\n" +
               "\n" +
               "    printf(\"Original Array: \");\n" +
               "    printArray(arr, SIZE);\n" +
               "    printf(\"\\n\");\n" +
               "\n" +
               "    printf(\"Sorting process:\\n\");\n" +
               "    mergeSort(arr, 0, SIZE - 1);\n" +
               "    printf(\"\\n\");\n" +
               "\n" +
               "    printf(\"=====================================\\n\");\n" +
               "    printf(\"Sorted Array:   \");\n" +
               "    printArray(arr, SIZE);\n" +
               "    printf(\"\\n\");\n" +
               "    printf(\"Time Complexity: O(n log n)\\n\");\n" +
               "    printf(\"Space Complexity: O(n)\\n\");\n" +
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
               "void printRange(int arr[], int left, int right) {\n" +
               "    cout << \"[\";\n" +
               "    for (int i = left; i <= right; i++) {\n" +
               "        cout << arr[i];\n" +
               "        if (i < right) cout << \", \";\n" +
               "    }\n" +
               "    cout << \"]\";\n" +
               "}\n" +
               "\n" +
               "void merge(int arr[], int left, int mid, int right) {\n" +
               "    int n1 = mid - left + 1;\n" +
               "    int n2 = right - mid;\n" +
               "\n" +
               "    // Create temp arrays\n" +
               "    int leftArr[n1], rightArr[n2];\n" +
               "\n" +
               "    // Copy data to temp arrays\n" +
               "    for (int i = 0; i < n1; i++)\n" +
               "        leftArr[i] = arr[left + i];\n" +
               "    for (int j = 0; j < n2; j++)\n" +
               "        rightArr[j] = arr[mid + 1 + j];\n" +
               "\n" +
               "    cout << \"  Merging \";\n" +
               "    printRange(leftArr, 0, n1 - 1);\n" +
               "    cout << \" and \";\n" +
               "    printRange(rightArr, 0, n2 - 1);\n" +
               "    cout << endl;\n" +
               "\n" +
               "    // Merge the temp arrays back\n" +
               "    int i = 0, j = 0, k = left;\n" +
               "    while (i < n1 && j < n2) {\n" +
               "        if (leftArr[i] <= rightArr[j]) {\n" +
               "            arr[k] = leftArr[i];\n" +
               "            i++;\n" +
               "        } else {\n" +
               "            arr[k] = rightArr[j];\n" +
               "            j++;\n" +
               "        }\n" +
               "        k++;\n" +
               "    }\n" +
               "\n" +
               "    // Copy remaining elements\n" +
               "    while (i < n1) {\n" +
               "        arr[k] = leftArr[i];\n" +
               "        i++;\n" +
               "        k++;\n" +
               "    }\n" +
               "    while (j < n2) {\n" +
               "        arr[k] = rightArr[j];\n" +
               "        j++;\n" +
               "        k++;\n" +
               "    }\n" +
               "\n" +
               "    cout << \"  Result: \";\n" +
               "    printRange(arr, left, right);\n" +
               "    cout << endl;\n" +
               "}\n" +
               "\n" +
               "void mergeSort(int arr[], int left, int right) {\n" +
               "    if (left < right) {\n" +
               "        int mid = left + (right - left) / 2;\n" +
               "\n" +
               "        // Sort first and second halves\n" +
               "        mergeSort(arr, left, mid);\n" +
               "        mergeSort(arr, mid + 1, right);\n" +
               "\n" +
               "        // Merge the sorted halves\n" +
               "        merge(arr, left, mid, right);\n" +
               "    }\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    cout << \"Merge Sort for array of size \" << SIZE << endl;\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "\n" +
               "    cout << \"Original Array: \";\n" +
               "    printArray(arr, SIZE);\n" +
               "    cout << endl;\n" +
               "\n" +
               "    cout << \"Sorting process:\" << endl;\n" +
               "    mergeSort(arr, 0, SIZE - 1);\n" +
               "    cout << endl;\n" +
               "\n" +
               "    cout << \"=====================================\" << endl;\n" +
               "    cout << \"Sorted Array:   \";\n" +
               "    printArray(arr, SIZE);\n" +
               "    cout << endl;\n" +
               "    cout << \"Time Complexity: O(n log n)\" << endl;\n" +
               "    cout << \"Space Complexity: O(n)\" << endl;\n" +
               "\n" +
               "    return 0;\n" +
               "}\n";
    }
}
