package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;

/**
 * Debug test to see what's happening
 */
public class DebugTest {
    
    @Test
    public void debugCExecution() throws ExecutionException {
        CodeExecutor executor = new CodeExecutor();
        
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    printf(\"Hello, World!\\n\");\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C);
        
        System.out.println("=== EXECUTION RESULT ===");
        System.out.println("Success: " + result.isSuccess());
        System.out.println("Exit Code: " + result.getExitCode());
        System.out.println("Compilation Error: " + result.isCompilationError());
        System.out.println("Timed Out: " + result.isTimedOut());
        System.out.println("Execution Time: " + result.getExecutionTimeMs() + "ms");
        System.out.println("\n=== OUTPUT ===");
        System.out.println(result.getOutput());
        System.out.println("\n=== ERROR ===");
        System.out.println(result.getError());
    }
}
