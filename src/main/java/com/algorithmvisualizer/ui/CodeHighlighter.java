package com.algorithmvisualizer.ui;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for applying syntax highlighting to code editors.
 * Supports C, C++, Java, and Python syntax highlighting.
 */
public class CodeHighlighter {
    
    // Store subscription to allow cleanup
    private static Subscription currentSubscription = null;
    
    // Java/C/C++ Keywords
    private static final String[] JAVA_KEYWORDS = new String[] {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while", "true", "false", "null",
        // C/C++ specific
        "auto", "extern", "register", "signed", "sizeof", "struct", "typedef",
        "union", "unsigned", "using", "namespace", "template", "typename",
        "virtual", "explicit", "inline", "operator", "friend", "mutable",
        "constexpr", "nullptr", "decltype", "noexcept"
    };
    
    // Python Keywords
    private static final String[] PYTHON_KEYWORDS = new String[] {
        "False", "None", "True", "and", "as", "assert", "async", "await",
        "break", "class", "continue", "def", "del", "elif", "else", "except",
        "finally", "for", "from", "global", "if", "import", "in", "is",
        "lambda", "nonlocal", "not", "or", "pass", "raise", "return",
        "try", "while", "with", "yield", "print", "range", "len", "str",
        "int", "float", "list", "dict", "set", "tuple"
    };
    
    // Build patterns
    private static final String JAVA_KEYWORD_PATTERN = "\\b(" + String.join("|", JAVA_KEYWORDS) + ")\\b";
    private static final String PYTHON_KEYWORD_PATTERN = "\\b(" + String.join("|", PYTHON_KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String CHAR_PATTERN = "'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String PYTHON_COMMENT_PATTERN = "#[^\n]*";
    private static final String PYTHON_STRING_PATTERN = "\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";
    
    private static Pattern JAVA_PATTERN;
    private static Pattern PYTHON_PATTERN;
    
    static {
        JAVA_PATTERN = Pattern.compile(
            "(?<KEYWORD>" + JAVA_KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<CHAR>" + CHAR_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
            + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
        );
        
        PYTHON_PATTERN = Pattern.compile(
            "(?<KEYWORD>" + PYTHON_KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<STRING>" + PYTHON_STRING_PATTERN + ")"
            + "|(?<COMMENT>" + PYTHON_COMMENT_PATTERN + ")"
            + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
        );
    }
    
    /**
     * Create a CodeArea with syntax highlighting for the specified language
     */
    public static CodeArea createCodeArea(String language) {
        CodeArea codeArea = new CodeArea();
        
        // Add line numbers
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        
        // Set font
        codeArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        
        // Store language in user data for later reference
        codeArea.setUserData(language);
        
        // Apply syntax highlighting based on language
        setupHighlighting(codeArea, language);
        
        return codeArea;
    }
    
    /**
     * Setup highlighting subscription for a CodeArea
     */
    private static void setupHighlighting(CodeArea codeArea, String language) {
        if (language == null) return;
        
        // Set up subscription that checks userData to determine which highlighting to apply
        // This allows dynamic language switching
        codeArea.richChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
            .successionEnds(Duration.ofMillis(50))
            .subscribe(ignore -> {
                String currentLang = (String) codeArea.getUserData();
                if (currentLang != null) {
                    if (currentLang.equalsIgnoreCase("Python")) {
                        codeArea.setStyleSpans(0, computePythonHighlighting(codeArea.getText()));
                    } else {
                        // Java, C, C++
                        codeArea.setStyleSpans(0, computeJavaHighlighting(codeArea.getText()));
                    }
                }
            });
    }
    
    /**
     * Apply syntax highlighting to existing CodeArea
     */
    public static void applyHighlighting(CodeArea codeArea, String language) {
        if (language == null || codeArea == null) return;
        
        // Update the language stored in user data
        codeArea.setUserData(language);
        
        // Apply highlighting to current text
        if (language.equalsIgnoreCase("Python")) {
            codeArea.setStyleSpans(0, computePythonHighlighting(codeArea.getText()));
        } else {
            // Java, C, C++
            codeArea.setStyleSpans(0, computeJavaHighlighting(codeArea.getText()));
        }
    }
    
    /**
     * Compute syntax highlighting for Java/C/C++
     */
    private static StyleSpans<Collection<String>> computeJavaHighlighting(String text) {
        Matcher matcher = JAVA_PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        while (matcher.find()) {
            String styleClass =
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("PAREN") != null ? "paren" :
                matcher.group("BRACE") != null ? "brace" :
                matcher.group("BRACKET") != null ? "bracket" :
                matcher.group("SEMICOLON") != null ? "semicolon" :
                matcher.group("STRING") != null ? "string" :
                matcher.group("CHAR") != null ? "string" :
                matcher.group("COMMENT") != null ? "comment" :
                matcher.group("NUMBER") != null ? "number" :
                null;
            
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
    /**
     * Compute syntax highlighting for Python
     */
    private static StyleSpans<Collection<String>> computePythonHighlighting(String text) {
        Matcher matcher = PYTHON_PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        while (matcher.find()) {
            String styleClass =
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("PAREN") != null ? "paren" :
                matcher.group("BRACKET") != null ? "bracket" :
                matcher.group("STRING") != null ? "string" :
                matcher.group("COMMENT") != null ? "comment" :
                matcher.group("NUMBER") != null ? "number" :
                null;
            
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
    /**
     * Get CSS stylesheet for syntax highlighting
     */
    public static String getStylesheet() {
        return CodeHighlighter.class.getResource("/css/code-highlighting.css").toExternalForm();
    }
}
