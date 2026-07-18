package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * General tests for CodeExecutor functionality
 */
public class CodeExecutorTest {
    
    private CodeExecutor executor;
    
    @BeforeEach
    public void setUp() throws ExecutionException {
        executor = new CodeExecutor();
    }
    
    @Test
    public void testNullCodeThrowsException() {
        assertThrows(ExecutionException.class, () -> {
            executor.execute(null, LanguageType.C);
        }, "Null code should throw ExecutionException");
    }
    
    @Test
    public void testEmptyCodeThrowsException() {
        assertThrows(ExecutionException.class, () -> {
            executor.execute("", LanguageType.C);
        }, "Empty code should throw ExecutionException");
    }
    
    @Test
    public void testWhitespaceOnlyCodeThrowsException() {
        assertThrows(ExecutionException.class, () -> {
            executor.execute("   \n\t  ", LanguageType.C);
        }, "Whitespace-only code should throw ExecutionException");
    }
    
    @Test
    public void testNullLanguageThrowsException() {
        assertThrows(ExecutionException.class, () -> {
            executor.execute("print('test')", null);
        }, "Null language should throw ExecutionException");
    }
    
    @Test
    public void testExecutionEnvironmentAccessible() {
        assertNotNull(executor.getEnvironment(), "Execution environment should be accessible");
    }
    
    @Test
    public void testCustomTimeout() throws ExecutionException {
        // Test that custom timeout is respected
        String code = "#include <stdio.h>\n" +
                     "#include <unistd.h>\n" +
                     "int main() {\n" +
                     "    printf(\"Starting...\\n\");\n" +
                     "    sleep(5);\n" +  // Sleep for 5 seconds
                     "    printf(\"Done\\n\");\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.C, 2); // 2 second timeout
        
        assertTrue(result.isTimedOut(), "Should timeout with 2 second limit");
    }
    
    @Test
    public void testSpecialCharactersInOutput() throws ExecutionException {
        String code = "print('Special chars: !@#$%^&*()_+-={}[]|:;<>?,./~`')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Special chars:"), 
                  "Output should contain special characters");
    }
    
    @Test
    public void testUnicodeCharacters() throws ExecutionException {
        String code = "print('Unicode: 你好世界 🌍 αβγδ')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        // Note: Unicode support depends on terminal/console encoding
    }
    
    @Test
    public void testExecutionTimeRecorded() throws ExecutionException {
        String code = "print('Quick execution')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.getExecutionTimeMs() >= 0, 
                  "Execution time should be non-negative");
        assertTrue(result.getExecutionTimeMs() < 10000, 
                  "Simple execution should complete in under 10 seconds");
    }
    
    @Test
    public void testLanguageTypeFromString() {
        assertEquals(LanguageType.C, LanguageType.fromString("c"));
        assertEquals(LanguageType.C, LanguageType.fromString("C"));
        assertEquals(LanguageType.CPP, LanguageType.fromString("cpp"));
        assertEquals(LanguageType.CPP, LanguageType.fromString("c++"));
        assertEquals(LanguageType.JAVA, LanguageType.fromString("java"));
        assertEquals(LanguageType.PYTHON, LanguageType.fromString("python"));
        assertEquals(LanguageType.PYTHON, LanguageType.fromString("py"));
        assertNull(LanguageType.fromString("invalid"));
        assertNull(LanguageType.fromString(null));
    }
    
    @Test
    public void testLanguageTypeProperties() {
        assertEquals(".c", LanguageType.C.getExtension());
        assertEquals(".cpp", LanguageType.CPP.getExtension());
        assertEquals(".java", LanguageType.JAVA.getExtension());
        assertEquals(".py", LanguageType.PYTHON.getExtension());
        
        assertTrue(LanguageType.C.isCompiled());
        assertTrue(LanguageType.CPP.isCompiled());
        assertTrue(LanguageType.JAVA.isCompiled());
        assertFalse(LanguageType.PYTHON.isCompiled());
        
        assertFalse(LanguageType.C.isInterpreted());
        assertTrue(LanguageType.PYTHON.isInterpreted());
    }
    
    @Test
    public void testExecutionResultBuilder() {
        ExecutionResult result = ExecutionResult.builder()
                .success(true)
                .output("Test output")
                .error("Test error")
                .exitCode(0)
                .executionTimeMs(100)
                .compilationError(false)
                .timedOut(false)
                .build();
        
        assertTrue(result.isSuccess());
        assertEquals("Test output", result.getOutput());
        assertEquals("Test error", result.getError());
        assertEquals(0, result.getExitCode());
        assertEquals(100, result.getExecutionTimeMs());
        assertFalse(result.isCompilationError());
        assertFalse(result.isTimedOut());
    }
    
    @Test
    public void testExecutionResultToString() {
        ExecutionResult result = ExecutionResult.builder()
                .success(true)
                .output("Output")
                .build();
        
        String str = result.toString();
        assertNotNull(str);
        assertTrue(str.contains("success=true"));
        assertTrue(str.contains("output='Output'"));
    }
}
