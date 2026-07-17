package com.algorithmvisualizer.execution;

/**
 * Custom exception for code execution errors
 */
public class ExecutionException extends Exception {
    
    public ExecutionException(String message) {
        super(message);
    }
    
    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
