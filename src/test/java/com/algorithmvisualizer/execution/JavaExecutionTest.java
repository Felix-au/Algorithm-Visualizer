package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Java code execution
 */
public class JavaExecutionTest {
    
    private CodeExecutor executor;
    
    @BeforeEach
    public void setUp() throws ExecutionException {
        executor = new CodeExecutor();
    }
    
    @Test
    public void testSimpleHelloWorld() throws ExecutionException {
        String code = "public class Main {\n" +
                     "    public static void main(String[] args) {\n" +
                     "        System.out.println(\"Hello, Java!\");\n" +
                     "    }\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.JAVA);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Hello, Java!"), 
                  "Output should contain 'Hello, Java!'");
        assertEquals(0, result.getExitCode(), "Exit code should be 0");
    }
    
    @Test
    public void testArithmeticOperations() throws ExecutionException {
        String code = "public class Main {\n" +
                     "    public static void main(String[] args) {\n" +
                     "        int a = 10, b = 5;\n" +
                     "        System.out.println(\"Sum: \" + (a + b));\n" +
                     "        System.out.println(\"Difference: \" + (a - b));\n" +
                     "        System.out.println(\"Product: \" + (a * b));\n" +
                     "    }\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.JAVA);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Sum: 15"), "Should contain Sum: 15");
        assertTrue(result.getOutput().contains("Difference: 5"), "Should contain Difference: 5");
        assertTrue(result.getOutput().contains("Product: 50"), "Should contain Product: 50");
    }
    
    @Test
    public void testArrayAndLoop() throws ExecutionException {
        String code = "public class Main {\n" +
                     "    public static void main(String[] args) {\n" +
                     "        int[] numbers = {1, 2, 3, 4, 5};\n" +
                     "        int sum = 0;\n" +
                     "        for (int num : numbers) {\n" +
                     "            sum += num;\n" +
                     "        }\n" +
                     "        System.out.println(\"Sum: \" + sum);\n" +
                     "    }\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.JAVA);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Sum: 15"), "Output should contain 'Sum: 15'");
    }
    
    @Test
    public void testCompilationError() throws ExecutionException {
        String code = "public class Main {\n" +
                     "    public static void main(String[] args) {\n" +
                     "        System.out.println(\"Missing semicolon\")\n" +  // Missing semicolon
                     "    }\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.JAVA);
        
        assertFalse(result.isSuccess(), "Execution should fail");
        assertTrue(result.isCompilationError(), "Should have compilation error");
        assertFalse(result.getError().isEmpty(), "Error message should not be empty");
    }
    
    @Test
    public void testRuntimeException() throws ExecutionException {
        String code = "public class Main {\n" +
                     "    public static void main(String[] args) {\n" +
                     "        System.out.println(\"Before exception\");\n" +
                     "        throw new RuntimeException(\"Test exception\");\n" +
                     "    }\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.JAVA);
        
        assertFalse(result.isSuccess(), "Execution should fail");
        assertTrue(result.getOutput().contains("Before exception"), 
                  "Should print before exception");
        assertTrue(result.getError().contains("RuntimeException") || 
                  result.getError().contains("Test exception"), 
                  "Error should mention exception");
    }
    
    @Test
    public void testStringManipulation() throws ExecutionException {
        String code = "public class Main {\n" +
                     "    public static void main(String[] args) {\n" +
                     "        String str = \"Hello\";\n" +
                     "        str = str + \" World\";\n" +
                     "        System.out.println(str.toUpperCase());\n" +
                     "    }\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.JAVA);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("HELLO WORLD"), 
                  "Output should contain 'HELLO WORLD'");
    }
    
    @Test
    public void testInfiniteLoopTimeout() throws ExecutionException {
        String code = "public class Main {\n" +
                     "    public static void main(String[] args) {\n" +
                     "        while(true) {}\n" +  // Infinite loop
                     "    }\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.JAVA, 2); // 2 second timeout
        
        assertFalse(result.isSuccess(), "Execution should fail due to timeout");
        assertTrue(result.isTimedOut(), "Should be marked as timed out");
    }
}
