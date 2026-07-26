package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * KMP (Knuth-Morris-Pratt) string search algorithm implementation in multiple languages.
 * Dynamically updated with actual text and pattern values from the UI.
 */
public class KMPCode implements AlgorithmCode {

    private String text;
    private String pattern;

    public KMPCode() {
        this.text = "BABABABABCABABCABAB";
        this.pattern = "ABABCABAB";
    }

    /**
     * Update text and pattern for dynamic code generation
     */
    public void updateParameters(String text, String pattern) {
        this.text = text != null ? text : "ABABDABACDABABCABAB";
        this.pattern = pattern != null ? pattern : "ABABCABAB";
    }

    @Override
    public String getAlgorithmName() {
        return "KMP Search";
    }

    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
               "#include <string.h>\n" +
               "\n" +
               "/* Build LPS (Longest Proper Prefix which is also Suffix) array */\n" +
               "void buildLPS(const char *pattern, int m, int lps[]) {\n" +
               "    int len = 0;\n" +
               "    lps[0] = 0;\n" +
               "    int i = 1;\n" +
               "\n" +
               "    printf(\"Building LPS (failure function):\\n\");\n" +
               "    while (i < m) {\n" +
               "        if (pattern[i] == pattern[len]) {\n" +
               "            len++;\n" +
               "            lps[i] = len;\n" +
               "            printf(\"  pattern[%d]='%c' == pattern[%d]='%c' -> lps[%d] = %d\\n\",\n" +
               "                   i, pattern[i], len - 1, pattern[len - 1], i, len);\n" +
               "            i++;\n" +
               "        } else {\n" +
               "            if (len != 0) {\n" +
               "                printf(\"  pattern[%d]='%c' != pattern[%d]='%c' -> fallback len %d -> %d\\n\",\n" +
               "                       i, pattern[i], len, pattern[len], len, lps[len - 1]);\n" +
               "                len = lps[len - 1];\n" +
               "            } else {\n" +
               "                lps[i] = 0;\n" +
               "                printf(\"  pattern[%d]='%c' != pattern[0]='%c' -> lps[%d] = 0\\n\",\n" +
               "                       i, pattern[i], pattern[0], i);\n" +
               "                i++;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    printf(\"LPS array: [\");\n" +
               "    for (int k = 0; k < m; k++) {\n" +
               "        printf(\"%d\", lps[k]);\n" +
               "        if (k < m - 1) printf(\", \");\n" +
               "    }\n" +
               "    printf(\"]\\n\\n\");\n" +
               "}\n" +
               "\n" +
               "/* KMP Search */\n" +
               "void kmpSearch(const char *text, const char *pattern) {\n" +
               "    int n = strlen(text);\n" +
               "    int m = strlen(pattern);\n" +
               "    int lps[m];\n" +
               "    int comparisons = 0;\n" +
               "\n" +
               "    printf(\"=== KMP String Search ===\\n\");\n" +
               "    printf(\"Text:    \\\"%s\\\" (length %d)\\n\", text, n);\n" +
               "    printf(\"Pattern: \\\"%s\\\" (length %d)\\n\\n\", pattern, m);\n" +
               "\n" +
               "    buildLPS(pattern, m, lps);\n" +
               "\n" +
               "    int i = 0, j = 0;\n" +
               "    while (i < n) {\n" +
               "        comparisons++;\n" +
               "        if (text[i] == pattern[j]) {\n" +
               "            printf(\"  Compare text[%d]='%c' == pattern[%d]='%c' -> MATCH\\n\",\n" +
               "                   i, text[i], j, pattern[j]);\n" +
               "            i++;\n" +
               "            j++;\n" +
               "        }\n" +
               "\n" +
               "        if (j == m) {\n" +
               "            printf(\"  >>> Pattern FOUND at index %d <<<\\n\", i - j);\n" +
               "            j = lps[j - 1];\n" +
               "        } else if (i < n && text[i] != pattern[j]) {\n" +
               "            comparisons++;\n" +
               "            printf(\"  Compare text[%d]='%c' != pattern[%d]='%c' -> MISMATCH\\n\",\n" +
               "                   i, text[i], j, pattern[j]);\n" +
               "            if (j != 0) {\n" +
               "                printf(\"    Using LPS: j = lps[%d] = %d\\n\", j - 1, lps[j - 1]);\n" +
               "                j = lps[j - 1];\n" +
               "            } else {\n" +
               "                i++;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    printf(\"\\n=== Search Complete ===\\n\");\n" +
               "    printf(\"Comparisons: %d\\n\", comparisons);\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    const char *text = \"" + escapeC(text) + "\";\n" +
               "    const char *pattern = \"" + escapeC(pattern) + "\";\n" +
               "    kmpSearch(text, pattern);\n" +
               "    return 0;\n" +
               "}\n";
    }

    @Override
    public String getCppCode() {
        return "#include <iostream>\n" +
               "#include <string>\n" +
               "#include <vector>\n" +
               "using namespace std;\n" +
               "\n" +
               "/* Build LPS array */\n" +
               "vector<int> buildLPS(const string& pattern) {\n" +
               "    int m = pattern.size();\n" +
               "    vector<int> lps(m, 0);\n" +
               "    int len = 0;\n" +
               "    int i = 1;\n" +
               "\n" +
               "    cout << \"Building LPS (failure function):\" << endl;\n" +
               "    while (i < m) {\n" +
               "        if (pattern[i] == pattern[len]) {\n" +
               "            len++;\n" +
               "            lps[i] = len;\n" +
               "            cout << \"  pattern[\" << i << \"]='\" << pattern[i]\n" +
               "                 << \"' == pattern[\" << len - 1 << \"]='\" << pattern[len - 1]\n" +
               "                 << \"' -> lps[\" << i << \"] = \" << len << endl;\n" +
               "            i++;\n" +
               "        } else {\n" +
               "            if (len != 0) {\n" +
               "                cout << \"  pattern[\" << i << \"]='\" << pattern[i]\n" +
               "                     << \"' != pattern[\" << len << \"]='\" << pattern[len]\n" +
               "                     << \"' -> fallback len \" << len << \" -> \" << lps[len - 1] << endl;\n" +
               "                len = lps[len - 1];\n" +
               "            } else {\n" +
               "                lps[i] = 0;\n" +
               "                cout << \"  pattern[\" << i << \"]='\" << pattern[i]\n" +
               "                     << \"' != pattern[0]='\" << pattern[0]\n" +
               "                     << \"' -> lps[\" << i << \"] = 0\" << endl;\n" +
               "                i++;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    cout << \"LPS array: [\";\n" +
               "    for (int k = 0; k < m; k++) {\n" +
               "        cout << lps[k];\n" +
               "        if (k < m - 1) cout << \", \";\n" +
               "    }\n" +
               "    cout << \"]\" << endl << endl;\n" +
               "    return lps;\n" +
               "}\n" +
               "\n" +
               "/* KMP Search */\n" +
               "void kmpSearch(const string& text, const string& pattern) {\n" +
               "    int n = text.size();\n" +
               "    int m = pattern.size();\n" +
               "    int comparisons = 0;\n" +
               "\n" +
               "    cout << \"=== KMP String Search ===\" << endl;\n" +
               "    cout << \"Text:    \\\"\" << text << \"\\\" (length \" << n << \")\" << endl;\n" +
               "    cout << \"Pattern: \\\"\" << pattern << \"\\\" (length \" << m << \")\" << endl << endl;\n" +
               "\n" +
               "    vector<int> lps = buildLPS(pattern);\n" +
               "\n" +
               "    int i = 0, j = 0;\n" +
               "    while (i < n) {\n" +
               "        comparisons++;\n" +
               "        if (text[i] == pattern[j]) {\n" +
               "            cout << \"  Compare text[\" << i << \"]='\" << text[i]\n" +
               "                 << \"' == pattern[\" << j << \"]='\" << pattern[j] << \"' -> MATCH\" << endl;\n" +
               "            i++;\n" +
               "            j++;\n" +
               "        }\n" +
               "\n" +
               "        if (j == m) {\n" +
               "            cout << \"  >>> Pattern FOUND at index \" << i - j << \" <<<\" << endl;\n" +
               "            j = lps[j - 1];\n" +
               "        } else if (i < n && text[i] != pattern[j]) {\n" +
               "            comparisons++;\n" +
               "            cout << \"  Compare text[\" << i << \"]='\" << text[i]\n" +
               "                 << \"' != pattern[\" << j << \"]='\" << pattern[j] << \"' -> MISMATCH\" << endl;\n" +
               "            if (j != 0) {\n" +
               "                cout << \"    Using LPS: j = lps[\" << j - 1 << \"] = \" << lps[j - 1] << endl;\n" +
               "                j = lps[j - 1];\n" +
               "            } else {\n" +
               "                i++;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    cout << endl << \"=== Search Complete ===\" << endl;\n" +
               "    cout << \"Comparisons: \" << comparisons << endl;\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    string text = \"" + escapeC(text) + "\";\n" +
               "    string pattern = \"" + escapeC(pattern) + "\";\n" +
               "    kmpSearch(text, pattern);\n" +
               "    return 0;\n" +
               "}\n";
    }

    @Override
    public String getJavaCode() {
        return "public class Main {\n" +
               "\n" +
               "    /* Build LPS (failure function) array */\n" +
               "    static int[] buildLPS(String pattern) {\n" +
               "        int m = pattern.length();\n" +
               "        int[] lps = new int[m];\n" +
               "        int len = 0;\n" +
               "        int i = 1;\n" +
               "\n" +
               "        System.out.println(\"Building LPS (failure function):\");\n" +
               "        while (i < m) {\n" +
               "            if (pattern.charAt(i) == pattern.charAt(len)) {\n" +
               "                len++;\n" +
               "                lps[i] = len;\n" +
               "                System.out.println(\"  pattern[\" + i + \"]='\" + pattern.charAt(i)\n" +
               "                    + \"' == pattern[\" + (len - 1) + \"]='\" + pattern.charAt(len - 1)\n" +
               "                    + \"' -> lps[\" + i + \"] = \" + len);\n" +
               "                i++;\n" +
               "            } else {\n" +
               "                if (len != 0) {\n" +
               "                    System.out.println(\"  pattern[\" + i + \"]='\" + pattern.charAt(i)\n" +
               "                        + \"' != pattern[\" + len + \"]='\" + pattern.charAt(len)\n" +
               "                        + \"' -> fallback len \" + len + \" -> \" + lps[len - 1]);\n" +
               "                    len = lps[len - 1];\n" +
               "                } else {\n" +
               "                    lps[i] = 0;\n" +
               "                    System.out.println(\"  pattern[\" + i + \"]='\" + pattern.charAt(i)\n" +
               "                        + \"' != pattern[0]='\" + pattern.charAt(0)\n" +
               "                        + \"' -> lps[\" + i + \"] = 0\");\n" +
               "                    i++;\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        System.out.print(\"LPS array: [\");\n" +
               "        for (int k = 0; k < m; k++) {\n" +
               "            System.out.print(lps[k]);\n" +
               "            if (k < m - 1) System.out.print(\", \");\n" +
               "        }\n" +
               "        System.out.println(\"]\\n\");\n" +
               "        return lps;\n" +
               "    }\n" +
               "\n" +
               "    /* KMP Search */\n" +
               "    static void kmpSearch(String text, String pattern) {\n" +
               "        int n = text.length();\n" +
               "        int m = pattern.length();\n" +
               "        int comparisons = 0;\n" +
               "\n" +
               "        System.out.println(\"=== KMP String Search ===\");\n" +
               "        System.out.println(\"Text:    \\\"\" + text + \"\\\" (length \" + n + \")\");\n" +
               "        System.out.println(\"Pattern: \\\"\" + pattern + \"\\\" (length \" + m + \")\\n\");\n" +
               "\n" +
               "        int[] lps = buildLPS(pattern);\n" +
               "\n" +
               "        int i = 0, j = 0;\n" +
               "        while (i < n) {\n" +
               "            comparisons++;\n" +
               "            if (text.charAt(i) == pattern.charAt(j)) {\n" +
               "                System.out.println(\"  Compare text[\" + i + \"]='\" + text.charAt(i)\n" +
               "                    + \"' == pattern[\" + j + \"]='\" + pattern.charAt(j) + \"' -> MATCH\");\n" +
               "                i++;\n" +
               "                j++;\n" +
               "            }\n" +
               "\n" +
               "            if (j == m) {\n" +
               "                System.out.println(\"  >>> Pattern FOUND at index \" + (i - j) + \" <<<\");\n" +
               "                j = lps[j - 1];\n" +
               "            } else if (i < n && text.charAt(i) != pattern.charAt(j)) {\n" +
               "                comparisons++;\n" +
               "                System.out.println(\"  Compare text[\" + i + \"]='\" + text.charAt(i)\n" +
               "                    + \"' != pattern[\" + j + \"]='\" + pattern.charAt(j) + \"' -> MISMATCH\");\n" +
               "                if (j != 0) {\n" +
               "                    System.out.println(\"    Using LPS: j = lps[\" + (j - 1) + \"] = \" + lps[j - 1]);\n" +
               "                    j = lps[j - 1];\n" +
               "                } else {\n" +
               "                    i++;\n" +
               "                }\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        System.out.println();\n" +
               "        System.out.println(\"=== Search Complete ===\");\n" +
               "        System.out.println(\"Comparisons: \" + comparisons);\n" +
               "    }\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        String text = \"" + escapeJava(text) + "\";\n" +
               "        String pattern = \"" + escapeJava(pattern) + "\";\n" +
               "        kmpSearch(text, pattern);\n" +
               "    }\n" +
               "}\n";
    }

    @Override
    public String getPythonCode() {
        return "def build_lps(pattern):\n" +
               "    \"\"\"Build LPS (failure function) array.\"\"\"\n" +
               "    m = len(pattern)\n" +
               "    lps = [0] * m\n" +
               "    length = 0\n" +
               "    i = 1\n" +
               "\n" +
               "    print(\"Building LPS (failure function):\")\n" +
               "    while i < m:\n" +
               "        if pattern[i] == pattern[length]:\n" +
               "            length += 1\n" +
               "            lps[i] = length\n" +
               "            print(f\"  pattern[{i}]='{pattern[i]}' == pattern[{length - 1}]='{pattern[length - 1]}'\"\n" +
               "                  f\" -> lps[{i}] = {length}\")\n" +
               "            i += 1\n" +
               "        else:\n" +
               "            if length != 0:\n" +
               "                print(f\"  pattern[{i}]='{pattern[i]}' != pattern[{length}]='{pattern[length]}'\"\n" +
               "                      f\" -> fallback len {length} -> {lps[length - 1]}\")\n" +
               "                length = lps[length - 1]\n" +
               "            else:\n" +
               "                lps[i] = 0\n" +
               "                print(f\"  pattern[{i}]='{pattern[i]}' != pattern[0]='{pattern[0]}'\"\n" +
               "                      f\" -> lps[{i}] = 0\")\n" +
               "                i += 1\n" +
               "\n" +
               "    print(f\"LPS array: {lps}\")\n" +
               "    print()\n" +
               "    return lps\n" +
               "\n" +
               "def kmp_search(text, pattern):\n" +
               "    \"\"\"KMP String Search.\"\"\"\n" +
               "    n, m = len(text), len(pattern)\n" +
               "    comparisons = 0\n" +
               "\n" +
               "    print(\"=== KMP String Search ===\")\n" +
               "    print(f'Text:    \"{text}\" (length {n})')\n" +
               "    print(f'Pattern: \"{pattern}\" (length {m})')\n" +
               "    print()\n" +
               "\n" +
               "    lps = build_lps(pattern)\n" +
               "\n" +
               "    i = 0\n" +
               "    j = 0\n" +
               "    while i < n:\n" +
               "        comparisons += 1\n" +
               "        if text[i] == pattern[j]:\n" +
               "            print(f\"  Compare text[{i}]='{text[i]}' == pattern[{j}]='{pattern[j]}' -> MATCH\")\n" +
               "            i += 1\n" +
               "            j += 1\n" +
               "\n" +
               "        if j == m:\n" +
               "            print(f\"  >>> Pattern FOUND at index {i - j} <<<\")\n" +
               "            j = lps[j - 1]\n" +
               "        elif i < n and text[i] != pattern[j]:\n" +
               "            comparisons += 1\n" +
               "            print(f\"  Compare text[{i}]='{text[i]}' != pattern[{j}]='{pattern[j]}' -> MISMATCH\")\n" +
               "            if j != 0:\n" +
               "                print(f\"    Using LPS: j = lps[{j - 1}] = {lps[j - 1]}\")\n" +
               "                j = lps[j - 1]\n" +
               "            else:\n" +
               "                i += 1\n" +
               "\n" +
               "    print()\n" +
               "    print(\"=== Search Complete ===\")\n" +
               "    print(f\"Comparisons: {comparisons}\")\n" +
               "\n" +
               "text = \"" + escapePython(text) + "\"\n" +
               "pattern = \"" + escapePython(pattern) + "\"\n" +
               "kmp_search(text, pattern)\n";
    }

    // ── Escape helpers ──────────────────────────────────────────────
    private String escapeC(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String escapeJava(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String escapePython(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
