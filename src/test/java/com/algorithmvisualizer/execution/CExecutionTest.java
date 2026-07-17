package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for C code execution
 */
public class CExecutionTest {
    
    private CodeExecutor executor;
    
    @BeforeEach
    public void setUp() throws ExecutionException {
        executor = new CodeExecutor();
    }
    
    @Test
    public void testSimpleHelloWorld() throws ExecutionException {
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    printf(\"Hello, World!\\n\");\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Hello, World!"), 
                  "Output should contain 'Hello, World!'");
        assertEquals(0, result.getExitCode(), "Exit code should be 0");
        assertFalse(result.isCompilationError(), "Should not have compilation error");
        assertFalse(result.isTimedOut(), "Should not timeout");
    }
    
    @Test
    public void testSimpleArithmetic() throws ExecutionException {
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    int a = 5, b = 3;\n" +
                     "    printf(\"Sum: %d\\n\", a + b);\n" +
                     "    printf(\"Product: %d\\n\", a * b);\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Sum: 8"), "Output should contain 'Sum: 8'");
        assertTrue(result.getOutput().contains("Product: 15"), "Output should contain 'Product: 15'");
    }
    
    @Test
    public void testCompilationError() throws ExecutionException {
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    printf(\"Missing semicolon\")\n" +  // Missing semicolon
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C);
        
        assertFalse(result.isSuccess(), "Execution should fail");
        assertTrue(result.isCompilationError(), "Should have compilation error");
        assertFalse(result.getError().isEmpty(), "Error message should not be empty");
    }
    
    @Test
    public void testRuntimeError() throws ExecutionException {
        String code = "#include <stdio.h>\n" +
                     "#include <stdlib.h>\n" +
                     "int main() {\n" +
                     "    printf(\"Before exit\\n\");\n" +
                     "    exit(1);\n" +  // Non-zero exit
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C);
        
        assertFalse(result.isSuccess(), "Execution should fail with non-zero exit");
        assertEquals(1, result.getExitCode(), "Exit code should be 1");
        assertTrue(result.getOutput().contains("Before exit"), "Should print before exit");
    }
    
    @Test
    public void testInfiniteLoopTimeout() throws ExecutionException {
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    while(1) {}\n" +  // Infinite loop
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C, 2); // 2 second timeout
        
        assertFalse(result.isSuccess(), "Execution should fail due to timeout");
        assertTrue(result.isTimedOut(), "Should be marked as timed out");
        assertTrue(result.getError().contains("timed out"), "Error should mention timeout");
    }
    
    @Test
    public void testMultiLineOutput() throws ExecutionException {
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    for(int i = 1; i <= 5; i++) {\n" +
                     "        printf(\"Line %d\\n\", i);\n" +
                     "    }\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        String output = result.getOutput();
        assertTrue(output.contains("Line 1"), "Should contain Line 1");
        assertTrue(output.contains("Line 5"), "Should contain Line 5");
    }
    
    @Test
    public void testStderrOutput() throws ExecutionException {
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    fprintf(stderr, \"Error message\\n\");\n" +
                     "    printf(\"Normal output\\n\");\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Normal output"), "Stdout should be captured");
        assertTrue(result.getError().contains("Error message"), "Stderr should be captured");
    }
}
