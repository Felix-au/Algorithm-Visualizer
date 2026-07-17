package com.algorithmvisualizer.execution;

/**
 * Enumeration of supported programming languages for code execution
 */
public enum LanguageType {
    C("c", ".c", "gcc"),
    CPP("cpp", ".cpp", "g++"),
    JAVA("java", ".java", "javac"),
    PYTHON("python", ".py", "python");
    
    private final String name;
    private final String extension;
    private final String compiler;
    
    LanguageType(String name, String extension, String compiler) {
        this.name = name;
        this.extension = extension;
        this.compiler = compiler;
    }
    
    public String getName() {
        return name;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getCompiler() {
        return compiler;
    }
    
    public boolean isCompiled() {
        return this == C || this == CPP || this == JAVA;
    }
    
    public boolean isInterpreted() {
        return this == PYTHON;
    }
    
    public static LanguageType fromString(String lang) {
        if (lang == null) return null;
        String lower = lang.toLowerCase().trim();
        switch (lower) {
            case "c": return C;
            case "cpp":
            case "c++": return CPP;
            case "java": return JAVA;
            case "python":
            case "py": return PYTHON;
            default: return null;
        }
    }
}
