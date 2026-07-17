package com.algorithmvisualizer.execution;

/**
 * Encapsulates the result of code execution
 */
public class ExecutionResult {
    private final boolean success;
    private final String output;
    private final String error;
    private final int exitCode;
    private final long executionTimeMs;
    private final boolean compilationError;
    private final boolean timedOut;
    
    private ExecutionResult(Builder builder) {
        this.success = builder.success;
        this.output = builder.output;
        this.error = builder.error;
        this.exitCode = builder.exitCode;
        this.executionTimeMs = builder.executionTimeMs;
        this.compilationError = builder.compilationError;
        this.timedOut = builder.timedOut;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getOutput() {
        return output;
    }
    
    public String getError() {
        return error;
    }
    
    public int getExitCode() {
        return exitCode;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public boolean isCompilationError() {
        return compilationError;
    }
    
    public boolean isTimedOut() {
        return timedOut;
    }
    
    @Override
    public String toString() {
        return "ExecutionResult{" +
                "success=" + success +
                ", output='" + output + '\'' +
                ", error='" + error + '\'' +
                ", exitCode=" + exitCode +
                ", executionTimeMs=" + executionTimeMs +
                ", compilationError=" + compilationError +
                ", timedOut=" + timedOut +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean success = false;
        private String output = "";
        private String error = "";
        private int exitCode = -1;
        private long executionTimeMs = 0;
        private boolean compilationError = false;
        private boolean timedOut = false;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder output(String output) {
            this.output = output != null ? output : "";
            return this;
        }
        
        public Builder error(String error) {
            this.error = error != null ? error : "";
            return this;
        }
        
        public Builder exitCode(int exitCode) {
            this.exitCode = exitCode;
            return this;
        }
        
        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }
        
        public Builder compilationError(boolean compilationError) {
            this.compilationError = compilationError;
            return this;
        }
        
        public Builder timedOut(boolean timedOut) {
            this.timedOut = timedOut;
            return this;
        }
        
        public ExecutionResult build() {
            return new ExecutionResult(this);
        }
    }
}
