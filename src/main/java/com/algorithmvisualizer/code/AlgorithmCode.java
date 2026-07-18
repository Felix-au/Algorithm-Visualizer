package com.algorithmvisualizer.code;

import com.algorithmvisualizer.execution.LanguageType;

/**
 * Interface for algorithm code implementations in multiple languages.
 * Each algorithm should implement this interface to provide code in C, C++, Java, and Python.
 */
public interface AlgorithmCode {
    
    /**
     * Get the algorithm name (must match Algorithm.getName())
     */
    String getAlgorithmName();
    
    /**
     * Get C implementation
     */
    String getCCode();
    
    /**
     * Get C++ implementation
     */
    String getCppCode();
    
    /**
     * Get Java implementation
     */
    String getJavaCode();
    
    /**
     * Get Python implementation
     */
    String getPythonCode();
    
    /**
     * Get code for specific language
     */
    default String getCodeForLanguage(LanguageType language) {
        if (language == null) {
            return getJavaCode(); // Default to Java
        }
        
        switch (language) {
            case C:
                return getCCode();
            case CPP:
                return getCppCode();
            case JAVA:
                return getJavaCode();
            case PYTHON:
                return getPythonCode();
            default:
                return "// Code not available for this language";
        }
    }
    
    /**
     * Get code for language by name
     */
    default String getCodeForLanguage(String languageName) {
        LanguageType type = LanguageType.fromString(languageName);
        return getCodeForLanguage(type);
    }
}
