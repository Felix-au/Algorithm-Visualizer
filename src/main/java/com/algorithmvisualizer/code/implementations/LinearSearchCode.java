package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Linear Search algorithm implementation in multiple languages.
 * This code can be dynamically updated with actual array values and target.
 */
public class LinearSearchCode implements AlgorithmCode {
    
    private int[] array;
    private int target;
    
    public LinearSearchCode() {
        // Default values
        this.array = new int[]{5, 2, 8, 12, 1, 9, 3};
        this.target = 9;
    }
    
    /**
     * Update the array and target for dynamic code generation
     */
    public void updateParameters(int[] array, int target) {
        this.array = array != null ? array : new int[]{5, 2, 8, 12, 1, 9, 3};
        this.target = target;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Linear Search";
    }
    
    @Override
    public String getCCode() {
        String arrayStr = arrayToString(array, ", ");
        return "#include <stdio.h>\n" +
               "\n" +
               "int linearSearch(int arr[], int size, int target) {\n" +
               "    for (int i = 0; i < size; i++) {\n" +
               "        printf(\"Step %d: Checking index %d (value: %d)\\n\", i + 1, i, arr[i]);\n" +
               "        \n" +
               "        if (arr[i] == target) {\n" +
               "            printf(\"Match found at index %d\\n\", i);\n" +
               "            return i;\n" +
               "        }\n" +
               "    }\n" +
               "    \n" +
               "    printf(\"Element not found\\n\");\n" +
               "    return -1;\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    int arr[] = {" + arrayStr + "};\n" +
               "    int size = " + array.length + ";\n" +
               "    int target = " + target + ";\n" +
               "    \n" +
               "    printf(\"Linear Search\\n\");\n" +
               "    printf(\"Array: \");\n" +
               "    for (int i = 0; i < size; i++) {\n" +
               "        printf(\"%d \", arr[i]);\n" +
               "    }\n" +
               "    printf(\"\\nTarget: %d\\n\\n\", target);\n" +
               "    \n" +
               "    int result = linearSearch(arr, size, target);\n" +
               "    \n" +
               "    if (result != -1) {\n" +
               "        printf(\"\\nElement %d found at index %d\\n\", target, result);\n" +
               "    } else {\n" +
               "        printf(\"\\nElement %d not found\\n\", target);\n" +
               "    }\n" +
               "    \n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getCppCode() {
        String arrayStr = arrayToString(array, ", ");
        return "#include <iostream>\n" +
               "#include <vector>\n" +
               "using namespace std;\n" +
               "\n" +
               "int linearSearch(const vector<int>& arr, int target) {\n" +
               "    for (int i = 0; i < arr.size(); i++) {\n" +
               "        cout << \"Step \" << (i + 1) << \": Checking index \" << i \n" +
               "             << \" (value: \" << arr[i] << \")\" << endl;\n" +
               "        \n" +
               "        if (arr[i] == target) {\n" +
               "            cout << \"Match found at index \" << i << endl;\n" +
               "            return i;\n" +
               "        }\n" +
               "    }\n" +
               "    \n" +
               "    cout << \"Element not found\" << endl;\n" +
               "    return -1;\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    vector<int> arr = {" + arrayStr + "};\n" +
               "    int target = " + target + ";\n" +
               "    \n" +
               "    cout << \"Linear Search\" << endl;\n" +
               "    cout << \"Array: \";\n" +
               "    for (int num : arr) {\n" +
               "        cout << num << \" \";\n" +
               "    }\n" +
               "    cout << endl << \"Target: \" << target << endl << endl;\n" +
               "    \n" +
               "    int result = linearSearch(arr, target);\n" +
               "    \n" +
               "    if (result != -1) {\n" +
               "        cout << endl << \"Element \" << target << \" found at index \" << result << endl;\n" +
               "    } else {\n" +
               "        cout << endl << \"Element \" << target << \" not found\" << endl;\n" +
               "    }\n" +
               "    \n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getJavaCode() {
        String arrayStr = arrayToString(array, ", ");
        return "public class Main {\n" +
               "    \n" +
               "    public static int linearSearch(int[] arr, int target) {\n" +
               "        for (int i = 0; i < arr.length; i++) {\n" +
               "            System.out.println(\"Step \" + (i + 1) + \": Checking index \" + i \n" +
               "                             + \" (value: \" + arr[i] + \")\");\n" +
               "            \n" +
               "            if (arr[i] == target) {\n" +
               "                System.out.println(\"Match found at index \" + i);\n" +
               "                return i;\n" +
               "            }\n" +
               "        }\n" +
               "        \n" +
               "        System.out.println(\"Element not found\");\n" +
               "        return -1;\n" +
               "    }\n" +
               "    \n" +
               "    public static void main(String[] args) {\n" +
               "        int[] arr = {" + arrayStr + "};\n" +
               "        int target = " + target + ";\n" +
               "        \n" +
               "        System.out.println(\"Linear Search\");\n" +
               "        System.out.print(\"Array: \");\n" +
               "        for (int num : arr) {\n" +
               "            System.out.print(num + \" \");\n" +
               "        }\n" +
               "        System.out.println(\"\\nTarget: \" + target + \"\\n\");\n" +
               "        \n" +
               "        int result = linearSearch(arr, target);\n" +
               "        \n" +
               "        if (result != -1) {\n" +
               "            System.out.println(\"\\nElement \" + target + \" found at index \" + result);\n" +
               "        } else {\n" +
               "            System.out.println(\"\\nElement \" + target + \" not found\");\n" +
               "        }\n" +
               "    }\n" +
               "}\n";
    }
    
    @Override
    public String getPythonCode() {
        String arrayStr = arrayToString(array, ", ");
        return "def linear_search(arr, target):\n" +
               "    \"\"\"Linear search implementation\"\"\"\n" +
               "    for i in range(len(arr)):\n" +
               "        print(f\"Step {i + 1}: Checking index {i} (value: {arr[i]})\")\n" +
               "        \n" +
               "        if arr[i] == target:\n" +
               "            print(f\"Match found at index {i}\")\n" +
               "            return i\n" +
               "    \n" +
               "    print(\"Element not found\")\n" +
               "    return -1\n" +
               "\n" +
               "arr = [" + arrayStr + "]\n" +
               "target = " + target + "\n" +
               "\n" +
               "print(\"Linear Search\")\n" +
               "print(f\"Array: {arr}\")\n" +
               "print(f\"Target: {target}\\n\")\n" +
               "\n" +
               "result = linear_search(arr, target)\n" +
               "\n" +
               "if result != -1:\n" +
               "    print(f\"\\nElement {target} found at index {result}\")\n" +
               "else:\n" +
               "    print(f\"\\nElement {target} not found\")\n";
    }
    
    /**
     * Helper method to convert array to string
     */
    private String arrayToString(int[] arr, String delimiter) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
