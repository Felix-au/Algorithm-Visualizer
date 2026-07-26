package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Boyer-Moore string search algorithm implementation in multiple languages.
 * Dynamically updated with actual text and pattern values from the UI.
 */
public class BoyerMooreCode implements AlgorithmCode {

    private String text;
    private String pattern;

    public BoyerMooreCode() {
        this.text = "ABAAABCDABABCDABCDABDE";
        this.pattern = "ABCDABD";
    }

    /**
     * Update text and pattern for dynamic code generation
     */
    public void updateParameters(String text, String pattern) {
        this.text = text != null ? text : "ABAAABCDABABCDABCDABDE";
        this.pattern = pattern != null ? pattern : "ABCDABD";
    }

    @Override
    public String getAlgorithmName() {
        return "Boyer-Moore Search";
    }

    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
               "#include <string.h>\n" +
               "\n" +
               "#define ALPHABET_SIZE 256\n" +
               "\n" +
               "/* Build Bad Character Table */\n" +
               "void buildBadCharTable(const char *pattern, int patLen, int badChar[ALPHABET_SIZE]) {\n" +
               "    int i;\n" +
               "    for (i = 0; i < ALPHABET_SIZE; i++)\n" +
               "        badChar[i] = -1;\n" +
               "    for (i = 0; i < patLen; i++)\n" +
               "        badChar[(unsigned char)pattern[i]] = i;\n" +
               "}\n" +
               "\n" +
               "/* Boyer-Moore Search */\n" +
               "void boyerMooreSearch(const char *text, const char *pattern) {\n" +
               "    int n = strlen(text);\n" +
               "    int m = strlen(pattern);\n" +
               "    int badChar[ALPHABET_SIZE];\n" +
               "    int comparisons = 0, shifts = 0;\n" +
               "\n" +
               "    buildBadCharTable(pattern, m, badChar);\n" +
               "\n" +
               "    printf(\"=== Boyer-Moore String Search ===\\n\");\n" +
               "    printf(\"Text:    \\\"%s\\\" (length %d)\\n\", text, n);\n" +
               "    printf(\"Pattern: \\\"%s\\\" (length %d)\\n\\n\", pattern, m);\n" +
               "\n" +
               "    int s = 0; /* alignment of pattern in text */\n" +
               "    while (s <= n - m) {\n" +
               "        printf(\"Alignment at position %d\\n\", s);\n" +
               "        int j = m - 1;\n" +
               "\n" +
               "        while (j >= 0 && pattern[j] == text[s + j]) {\n" +
               "            comparisons++;\n" +
               "            printf(\"  Compare text[%d]='%c' == pattern[%d]='%c' -> MATCH\\n\",\n" +
               "                   s + j, text[s + j], j, pattern[j]);\n" +
               "            j--;\n" +
               "        }\n" +
               "\n" +
               "        if (j < 0) {\n" +
               "            printf(\"  >>> Pattern FOUND at index %d <<<\\n\", s);\n" +
               "            s += 1;\n" +
               "            shifts++;\n" +
               "        } else {\n" +
               "            comparisons++;\n" +
               "            int shift = j - badChar[(unsigned char)text[s + j]];\n" +
               "            if (shift < 1) shift = 1;\n" +
               "            printf(\"  Compare text[%d]='%c' != pattern[%d]='%c' -> MISMATCH\\n\",\n" +
               "                   s + j, text[s + j], j, pattern[j]);\n" +
               "            printf(\"  Bad char '%c' last at %d in pattern, shift by %d\\n\",\n" +
               "                   text[s + j], badChar[(unsigned char)text[s + j]], shift);\n" +
               "            s += shift;\n" +
               "            shifts++;\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    printf(\"\\n=== Search Complete ===\\n\");\n" +
               "    printf(\"Comparisons: %d\\n\", comparisons);\n" +
               "    printf(\"Shifts: %d\\n\", shifts);\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    const char *text = \"" + escapeC(text) + "\";\n" +
               "    const char *pattern = \"" + escapeC(pattern) + "\";\n" +
               "    boyerMooreSearch(text, pattern);\n" +
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
               "const int ALPHABET_SIZE = 256;\n" +
               "\n" +
               "/* Build Bad Character Table */\n" +
               "vector<int> buildBadCharTable(const string& pattern) {\n" +
               "    vector<int> badChar(ALPHABET_SIZE, -1);\n" +
               "    for (int i = 0; i < (int)pattern.size(); i++)\n" +
               "        badChar[(unsigned char)pattern[i]] = i;\n" +
               "    return badChar;\n" +
               "}\n" +
               "\n" +
               "/* Boyer-Moore Search */\n" +
               "void boyerMooreSearch(const string& text, const string& pattern) {\n" +
               "    int n = text.size();\n" +
               "    int m = pattern.size();\n" +
               "    vector<int> badChar = buildBadCharTable(pattern);\n" +
               "    int comparisons = 0, shifts = 0;\n" +
               "\n" +
               "    cout << \"=== Boyer-Moore String Search ===\" << endl;\n" +
               "    cout << \"Text:    \\\"\" << text << \"\\\" (length \" << n << \")\" << endl;\n" +
               "    cout << \"Pattern: \\\"\" << pattern << \"\\\" (length \" << m << \")\" << endl << endl;\n" +
               "\n" +
               "    int s = 0;\n" +
               "    while (s <= n - m) {\n" +
               "        cout << \"Alignment at position \" << s << endl;\n" +
               "        int j = m - 1;\n" +
               "\n" +
               "        while (j >= 0 && pattern[j] == text[s + j]) {\n" +
               "            comparisons++;\n" +
               "            cout << \"  Compare text[\" << s+j << \"]='\" << text[s+j]\n" +
               "                 << \"' == pattern[\" << j << \"]='\" << pattern[j] << \"' -> MATCH\" << endl;\n" +
               "            j--;\n" +
               "        }\n" +
               "\n" +
               "        if (j < 0) {\n" +
               "            cout << \"  >>> Pattern FOUND at index \" << s << \" <<<\" << endl;\n" +
               "            s += 1;\n" +
               "            shifts++;\n" +
               "        } else {\n" +
               "            comparisons++;\n" +
               "            int shift = max(1, j - badChar[(unsigned char)text[s + j]]);\n" +
               "            cout << \"  Compare text[\" << s+j << \"]='\" << text[s+j]\n" +
               "                 << \"' != pattern[\" << j << \"]='\" << pattern[j] << \"' -> MISMATCH\" << endl;\n" +
               "            cout << \"  Bad char '\" << text[s+j] << \"' last at \"\n" +
               "                 << badChar[(unsigned char)text[s+j]] << \" in pattern, shift by \" << shift << endl;\n" +
               "            s += shift;\n" +
               "            shifts++;\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    cout << endl << \"=== Search Complete ===\" << endl;\n" +
               "    cout << \"Comparisons: \" << comparisons << endl;\n" +
               "    cout << \"Shifts: \" << shifts << endl;\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    string text = \"" + escapeC(text) + "\";\n" +
               "    string pattern = \"" + escapeC(pattern) + "\";\n" +
               "    boyerMooreSearch(text, pattern);\n" +
               "    return 0;\n" +
               "}\n";
    }

    @Override
    public String getJavaCode() {
        return "import java.util.Arrays;\n" +
               "\n" +
               "public class Main {\n" +
               "\n" +
               "    static final int ALPHABET_SIZE = 256;\n" +
               "\n" +
               "    /* Build Bad Character Table */\n" +
               "    static int[] buildBadCharTable(String pattern) {\n" +
               "        int[] badChar = new int[ALPHABET_SIZE];\n" +
               "        Arrays.fill(badChar, -1);\n" +
               "        for (int i = 0; i < pattern.length(); i++)\n" +
               "            badChar[pattern.charAt(i)] = i;\n" +
               "        return badChar;\n" +
               "    }\n" +
               "\n" +
               "    /* Boyer-Moore Search */\n" +
               "    static void boyerMooreSearch(String text, String pattern) {\n" +
               "        int n = text.length();\n" +
               "        int m = pattern.length();\n" +
               "        int[] badChar = buildBadCharTable(pattern);\n" +
               "        int comparisons = 0, shifts = 0;\n" +
               "\n" +
               "        System.out.println(\"=== Boyer-Moore String Search ===\");\n" +
               "        System.out.println(\"Text:    \\\"\" + text + \"\\\" (length \" + n + \")\");\n" +
               "        System.out.println(\"Pattern: \\\"\" + pattern + \"\\\" (length \" + m + \")\\n\");\n" +
               "\n" +
               "        int s = 0;\n" +
               "        while (s <= n - m) {\n" +
               "            System.out.println(\"Alignment at position \" + s);\n" +
               "            int j = m - 1;\n" +
               "\n" +
               "            while (j >= 0 && pattern.charAt(j) == text.charAt(s + j)) {\n" +
               "                comparisons++;\n" +
               "                System.out.println(\"  Compare text[\" + (s+j) + \"]='\" + text.charAt(s+j)\n" +
               "                    + \"' == pattern[\" + j + \"]='\" + pattern.charAt(j) + \"' -> MATCH\");\n" +
               "                j--;\n" +
               "            }\n" +
               "\n" +
               "            if (j < 0) {\n" +
               "                System.out.println(\"  >>> Pattern FOUND at index \" + s + \" <<<\");\n" +
               "                s += 1;\n" +
               "                shifts++;\n" +
               "            } else {\n" +
               "                comparisons++;\n" +
               "                int shift = Math.max(1, j - badChar[text.charAt(s + j)]);\n" +
               "                System.out.println(\"  Compare text[\" + (s+j) + \"]='\" + text.charAt(s+j)\n" +
               "                    + \"' != pattern[\" + j + \"]='\" + pattern.charAt(j) + \"' -> MISMATCH\");\n" +
               "                System.out.println(\"  Bad char '\" + text.charAt(s+j) + \"' last at \"\n" +
               "                    + badChar[text.charAt(s+j)] + \" in pattern, shift by \" + shift);\n" +
               "                s += shift;\n" +
               "                shifts++;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        System.out.println();\n" +
               "        System.out.println(\"=== Search Complete ===\");\n" +
               "        System.out.println(\"Comparisons: \" + comparisons);\n" +
               "        System.out.println(\"Shifts: \" + shifts);\n" +
               "    }\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        String text = \"" + escapeJava(text) + "\";\n" +
               "        String pattern = \"" + escapeJava(pattern) + "\";\n" +
               "        boyerMooreSearch(text, pattern);\n" +
               "    }\n" +
               "}\n";
    }

    @Override
    public String getPythonCode() {
        return "def build_bad_char_table(pattern):\n" +
               "    \"\"\"Build the Bad Character Table.\"\"\"\n" +
               "    bad_char = {}\n" +
               "    for i, ch in enumerate(pattern):\n" +
               "        bad_char[ch] = i\n" +
               "    return bad_char\n" +
               "\n" +
               "def boyer_moore_search(text, pattern):\n" +
               "    \"\"\"Boyer-Moore String Search using Bad Character heuristic.\"\"\"\n" +
               "    n, m = len(text), len(pattern)\n" +
               "    bad_char = build_bad_char_table(pattern)\n" +
               "    comparisons, shifts = 0, 0\n" +
               "\n" +
               "    print(\"=== Boyer-Moore String Search ===\")\n" +
               "    print(f'Text:    \"{text}\" (length {n})')\n" +
               "    print(f'Pattern: \"{pattern}\" (length {m})')\n" +
               "    print()\n" +
               "\n" +
               "    s = 0\n" +
               "    while s <= n - m:\n" +
               "        print(f\"Alignment at position {s}\")\n" +
               "        j = m - 1\n" +
               "\n" +
               "        while j >= 0 and pattern[j] == text[s + j]:\n" +
               "            comparisons += 1\n" +
               "            print(f\"  Compare text[{s+j}]='{text[s+j]}' == pattern[{j}]='{pattern[j]}' -> MATCH\")\n" +
               "            j -= 1\n" +
               "\n" +
               "        if j < 0:\n" +
               "            print(f\"  >>> Pattern FOUND at index {s} <<<\")\n" +
               "            s += 1\n" +
               "            shifts += 1\n" +
               "        else:\n" +
               "            comparisons += 1\n" +
               "            last_occ = bad_char.get(text[s + j], -1)\n" +
               "            shift = max(1, j - last_occ)\n" +
               "            print(f\"  Compare text[{s+j}]='{text[s+j]}' != pattern[{j}]='{pattern[j]}' -> MISMATCH\")\n" +
               "            print(f\"  Bad char '{text[s+j]}' last at {last_occ} in pattern, shift by {shift}\")\n" +
               "            s += shift\n" +
               "            shifts += 1\n" +
               "\n" +
               "    print()\n" +
               "    print(\"=== Search Complete ===\")\n" +
               "    print(f\"Comparisons: {comparisons}\")\n" +
               "    print(f\"Shifts: {shifts}\")\n" +
               "\n" +
               "text = \"" + escapePython(text) + "\"\n" +
               "pattern = \"" + escapePython(pattern) + "\"\n" +
               "boyer_moore_search(text, pattern)\n";
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
