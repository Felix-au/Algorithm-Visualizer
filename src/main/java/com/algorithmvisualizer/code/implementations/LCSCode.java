package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;
import com.algorithmvisualizer.code.CodeRepository;

/**
 * Code implementations for Longest Common Subsequence in C, C++, Java, and Python.
 */
public class LCSCode implements AlgorithmCode {
    
    private String str1;
    private String str2;
    
    public LCSCode() {
        this.str1 = "ABCDGH";
        this.str2 = "AEDFHR";
        CodeRepository.register(this);
    }
    
    /**
     * Update the strings for dynamic code generation
     */
    public void updateParameters(String str1, String str2) {
        this.str1 = (str1 != null && !str1.isEmpty()) ? str1 : "ABCDGH";
        this.str2 = (str2 != null && !str2.isEmpty()) ? str2 : "AEDFHR";
    }
    
    @Override
    public String getAlgorithmName() {
        return "Longest Common Subsequence";
    }
    
    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
            "#include <string.h>\n" +
            "\n" +
            "#define MAX 100\n" +
            "\n" +
            "// Function to find length of LCS\n" +
            "int lcs(char *str1, char *str2, int m, int n) {\n" +
            "    int dp[MAX][MAX];\n" +
            "    \n" +
            "    // Initialize first row and column to 0\n" +
            "    for (int i = 0; i <= m; i++) {\n" +
            "        for (int j = 0; j <= n; j++) {\n" +
            "            if (i == 0 || j == 0) {\n" +
            "                dp[i][j] = 0;\n" +
            "            }\n" +
            "            else if (str1[i-1] == str2[j-1]) {\n" +
            "                // Characters match: take diagonal + 1\n" +
            "                dp[i][j] = dp[i-1][j-1] + 1;\n" +
            "            }\n" +
            "            else {\n" +
            "                // Characters differ: take max of top and left\n" +
            "                dp[i][j] = (dp[i-1][j] > dp[i][j-1]) ? \n" +
            "                           dp[i-1][j] : dp[i][j-1];\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    return dp[m][n];\n" +
            "}\n" +
            "\n" +
            "// Function to print LCS\n" +
            "void printLCS(char *str1, char *str2, int m, int n) {\n" +
            "    int dp[MAX][MAX];\n" +
            "    \n" +
            "    // Build DP table\n" +
            "    for (int i = 0; i <= m; i++) {\n" +
            "        for (int j = 0; j <= n; j++) {\n" +
            "            if (i == 0 || j == 0) {\n" +
            "                dp[i][j] = 0;\n" +
            "            }\n" +
            "            else if (str1[i-1] == str2[j-1]) {\n" +
            "                dp[i][j] = dp[i-1][j-1] + 1;\n" +
            "            }\n" +
            "            else {\n" +
            "                dp[i][j] = (dp[i-1][j] > dp[i][j-1]) ? \n" +
            "                           dp[i-1][j] : dp[i][j-1];\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    // Traceback to find LCS\n" +
            "    int i = m, j = n;\n" +
            "    char lcs[MAX];\n" +
            "    int index = dp[m][n];\n" +
            "    lcs[index] = '\\0';\n" +
            "    \n" +
            "    while (i > 0 && j > 0) {\n" +
            "        if (str1[i-1] == str2[j-1]) {\n" +
            "            lcs[index-1] = str1[i-1];\n" +
            "            i--;\n" +
            "            j--;\n" +
            "            index--;\n" +
            "        }\n" +
            "        else if (dp[i-1][j] > dp[i][j-1]) {\n" +
            "            i--;\n" +
            "        }\n" +
            "        else {\n" +
            "            j--;\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    printf(\"LCS: %s\\n\", lcs);\n" +
            "}\n" +
            "\n" +
            "int main() {\n" +
            "    char str1[] = \"" + str1 + "\";\n" +
            "    char str2[] = \"" + str2 + "\";\n" +
            "    \n" +
            "    int m = strlen(str1);\n" +
            "    int n = strlen(str2);\n" +
            "    \n" +
            "    printf(\"String 1: %s\\n\", str1);\n" +
            "    printf(\"String 2: %s\\n\", str2);\n" +
            "    \n" +
            "    int length = lcs(str1, str2, m, n);\n" +
            "    printf(\"LCS Length: %d\\n\", length);\n" +
            "    \n" +
            "    printLCS(str1, str2, m, n);\n" +
            "    \n" +
            "    return 0;\n" +
            "}\n";
    }
    
    @Override
    public String getCppCode() {
        return "#include <iostream>\n" +
            "#include <string>\n" +
            "#include <vector>\n" +
            "#include <algorithm>\n" +
            "using namespace std;\n" +
            "\n" +
            "// Function to find LCS length\n" +
            "int lcsLength(const string& str1, const string& str2) {\n" +
            "    int m = str1.length();\n" +
            "    int n = str2.length();\n" +
            "    \n" +
            "    vector<vector<int>> dp(m + 1, vector<int>(n + 1, 0));\n" +
            "    \n" +
            "    // Build DP table\n" +
            "    for (int i = 1; i <= m; i++) {\n" +
            "        for (int j = 1; j <= n; j++) {\n" +
            "            if (str1[i-1] == str2[j-1]) {\n" +
            "                // Characters match: take diagonal + 1\n" +
            "                dp[i][j] = dp[i-1][j-1] + 1;\n" +
            "            } else {\n" +
            "                // Characters differ: take max of top and left\n" +
            "                dp[i][j] = max(dp[i-1][j], dp[i][j-1]);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    return dp[m][n];\n" +
            "}\n" +
            "\n" +
            "// Function to find and return LCS string\n" +
            "string findLCS(const string& str1, const string& str2) {\n" +
            "    int m = str1.length();\n" +
            "    int n = str2.length();\n" +
            "    \n" +
            "    vector<vector<int>> dp(m + 1, vector<int>(n + 1, 0));\n" +
            "    \n" +
            "    // Build DP table\n" +
            "    for (int i = 1; i <= m; i++) {\n" +
            "        for (int j = 1; j <= n; j++) {\n" +
            "            if (str1[i-1] == str2[j-1]) {\n" +
            "                dp[i][j] = dp[i-1][j-1] + 1;\n" +
            "            } else {\n" +
            "                dp[i][j] = max(dp[i-1][j], dp[i][j-1]);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    // Traceback to construct LCS\n" +
            "    string lcs;\n" +
            "    int i = m, j = n;\n" +
            "    \n" +
            "    while (i > 0 && j > 0) {\n" +
            "        if (str1[i-1] == str2[j-1]) {\n" +
            "            lcs += str1[i-1];\n" +
            "            i--;\n" +
            "            j--;\n" +
            "        } else if (dp[i-1][j] > dp[i][j-1]) {\n" +
            "            i--;\n" +
            "        } else {\n" +
            "            j--;\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    reverse(lcs.begin(), lcs.end());\n" +
            "    return lcs;\n" +
            "}\n" +
            "\n" +
            "int main() {\n" +
            "    string str1 = \"" + str1 + "\";\n" +
            "    string str2 = \"" + str2 + "\";\n" +
            "    \n" +
            "    cout << \"String 1: \" << str1 << endl;\n" +
            "    cout << \"String 2: \" << str2 << endl;\n" +
            "    \n" +
            "    int length = lcsLength(str1, str2);\n" +
            "    cout << \"LCS Length: \" << length << endl;\n" +
            "    \n" +
            "    string lcs = findLCS(str1, str2);\n" +
            "    cout << \"LCS: \" << lcs << endl;\n" +
            "    \n" +
            "    return 0;\n" +
            "}\n";
    }
    
    @Override
    public String getJavaCode() {
        return "public class Main {\n" +
            "    \n" +
            "    // Function to find LCS length\n" +
            "    public static int lcsLength(String str1, String str2) {\n" +
            "        int m = str1.length();\n" +
            "        int n = str2.length();\n" +
            "        \n" +
            "        int[][] dp = new int[m + 1][n + 1];\n" +
            "        \n" +
            "        // Build DP table\n" +
            "        for (int i = 1; i <= m; i++) {\n" +
            "            for (int j = 1; j <= n; j++) {\n" +
            "                if (str1.charAt(i-1) == str2.charAt(j-1)) {\n" +
            "                    // Characters match: take diagonal + 1\n" +
            "                    dp[i][j] = dp[i-1][j-1] + 1;\n" +
            "                } else {\n" +
            "                    // Characters differ: take max of top and left\n" +
            "                    dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        return dp[m][n];\n" +
            "    }\n" +
            "    \n" +
            "    // Function to find and return LCS string\n" +
            "    public static String findLCS(String str1, String str2) {\n" +
            "        int m = str1.length();\n" +
            "        int n = str2.length();\n" +
            "        \n" +
            "        int[][] dp = new int[m + 1][n + 1];\n" +
            "        \n" +
            "        // Build DP table\n" +
            "        for (int i = 1; i <= m; i++) {\n" +
            "            for (int j = 1; j <= n; j++) {\n" +
            "                if (str1.charAt(i-1) == str2.charAt(j-1)) {\n" +
            "                    dp[i][j] = dp[i-1][j-1] + 1;\n" +
            "                } else {\n" +
            "                    dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        // Traceback to construct LCS\n" +
            "        StringBuilder lcs = new StringBuilder();\n" +
            "        int i = m, j = n;\n" +
            "        \n" +
            "        while (i > 0 && j > 0) {\n" +
            "            if (str1.charAt(i-1) == str2.charAt(j-1)) {\n" +
            "                lcs.append(str1.charAt(i-1));\n" +
            "                i--;\n" +
            "                j--;\n" +
            "            } else if (dp[i-1][j] > dp[i][j-1]) {\n" +
            "                i--;\n" +
            "            } else {\n" +
            "                j--;\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        return lcs.reverse().toString();\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        String str1 = \"" + str1 + "\";\n" +
            "        String str2 = \"" + str2 + "\";\n" +
            "        \n" +
            "        System.out.println(\"String 1: \" + str1);\n" +
            "        System.out.println(\"String 2: \" + str2);\n" +
            "        \n" +
            "        int length = lcsLength(str1, str2);\n" +
            "        System.out.println(\"LCS Length: \" + length);\n" +
            "        \n" +
            "        String lcs = findLCS(str1, str2);\n" +
            "        System.out.println(\"LCS: \" + lcs);\n" +
            "    }\n" +
            "}\n";
    }
    
    @Override
    public String getPythonCode() {
        return "def lcs_length(str1, str2):\n" +
            "    \"\"\"Find the length of LCS\"\"\"\n" +
            "    m = len(str1)\n" +
            "    n = len(str2)\n" +
            "    \n" +
            "    # Create DP table\n" +
            "    dp = [[0] * (n + 1) for _ in range(m + 1)]\n" +
            "    \n" +
            "    # Build DP table\n" +
            "    for i in range(1, m + 1):\n" +
            "        for j in range(1, n + 1):\n" +
            "            if str1[i-1] == str2[j-1]:\n" +
            "                # Characters match: take diagonal + 1\n" +
            "                dp[i][j] = dp[i-1][j-1] + 1\n" +
            "            else:\n" +
            "                # Characters differ: take max of top and left\n" +
            "                dp[i][j] = max(dp[i-1][j], dp[i][j-1])\n" +
            "    \n" +
            "    return dp[m][n]\n" +
            "\n" +
            "def find_lcs(str1, str2):\n" +
            "    \"\"\"Find the actual LCS string\"\"\"\n" +
            "    m = len(str1)\n" +
            "    n = len(str2)\n" +
            "    \n" +
            "    # Create DP table\n" +
            "    dp = [[0] * (n + 1) for _ in range(m + 1)]\n" +
            "    \n" +
            "    # Build DP table\n" +
            "    for i in range(1, m + 1):\n" +
            "        for j in range(1, n + 1):\n" +
            "            if str1[i-1] == str2[j-1]:\n" +
            "                dp[i][j] = dp[i-1][j-1] + 1\n" +
            "            else:\n" +
            "                dp[i][j] = max(dp[i-1][j], dp[i][j-1])\n" +
            "    \n" +
            "    # Traceback to construct LCS\n" +
            "    lcs = []\n" +
            "    i, j = m, n\n" +
            "    \n" +
            "    while i > 0 and j > 0:\n" +
            "        if str1[i-1] == str2[j-1]:\n" +
            "            lcs.append(str1[i-1])\n" +
            "            i -= 1\n" +
            "            j -= 1\n" +
            "        elif dp[i-1][j] > dp[i][j-1]:\n" +
            "            i -= 1\n" +
            "        else:\n" +
            "            j -= 1\n" +
            "    \n" +
            "    return ''.join(reversed(lcs))\n" +
            "\n" +
            "if __name__ == \"__main__\":\n" +
            "    str1 = \"" + str1 + "\"\n" +
            "    str2 = \"" + str2 + "\"\n" +
            "    \n" +
            "    print(f\"String 1: {str1}\")\n" +
            "    print(f\"String 2: {str2}\")\n" +
            "    \n" +
            "    length = lcs_length(str1, str2)\n" +
            "    print(f\"LCS Length: {length}\")\n" +
            "    \n" +
            "    lcs = find_lcs(str1, str2)\n" +
            "    print(f\"LCS: {lcs}\")\n";
    }
}
