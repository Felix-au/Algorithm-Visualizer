package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Python code execution
 */
public class PythonExecutionTest {
    
    private CodeExecutor executor;
    
    @BeforeEach
    public void setUp() throws ExecutionException {
        executor = new CodeExecutor();
    }
    
    @Test
    public void testSimpleHelloWorld() throws ExecutionException {
        String code = "print('Hello, Python!')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Hello, Python!"), 
                  "Output should contain 'Hello, Python!'");
        assertEquals(0, result.getExitCode(), "Exit code should be 0");
    }
    
    @Test
    public void testArithmeticOperations() throws ExecutionException {
        String code = "a = 10\n" +
                     "b = 5\n" +
                     "print(f'Sum: {a + b}')\n" +
                     "print(f'Product: {a * b}')\n" +
                     "print(f'Division: {a / b}')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Sum: 15"), "Should contain Sum: 15");
        assertTrue(result.getOutput().contains("Product: 50"), "Should contain Product: 50");
        assertTrue(result.getOutput().contains("Division: 2"), "Should contain Division: 2");
    }
    
    @Test
    public void testListAndLoop() throws ExecutionException {
        String code = "numbers = [1, 2, 3, 4, 5]\n" +
                     "total = sum(numbers)\n" +
                     "print(f'Sum: {total}')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Sum: 15"), "Output should contain 'Sum: 15'");
    }
    
    @Test
    public void testSyntaxError() throws ExecutionException {
        String code = "print('Missing closing quote)";  // Syntax error
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertFalse(result.isSuccess(), "Execution should fail");
        assertFalse(result.getError().isEmpty(), "Error message should not be empty");
        assertTrue(result.getError().contains("SyntaxError") || 
                  result.getError().contains("unterminated"), 
                  "Error should mention syntax error");
    }
    
    @Test
    public void testRuntimeError() throws ExecutionException {
        String code = "print('Before error')\n" +
                     "x = 1 / 0\n" +  // Division by zero
                     "print('After error')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertFalse(result.isSuccess(), "Execution should fail");
        assertTrue(result.getOutput().contains("Before error"), 
                  "Should print before error");
        assertTrue(result.getError().contains("ZeroDivisionError") || 
                  result.getError().contains("division"), 
                  "Error should mention division by zero");
    }
    
    @Test
    public void testMultiLineOutput() throws ExecutionException {
        String code = "for i in range(1, 6):\n" +
                     "    print(f'Line {i}')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        String output = result.getOutput();
        assertTrue(output.contains("Line 1"), "Should contain Line 1");
        assertTrue(output.contains("Line 5"), "Should contain Line 5");
    }
    
    @Test
    public void testDictionaryUsage() throws ExecutionException {
        String code = "person = {'name': 'Alice', 'age': 30}\n" +
                     "print(f\"Name: {person['name']}\")\n" +
                     "print(f\"Age: {person['age']}\")";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Name: Alice"), "Should contain Name: Alice");
        assertTrue(result.getOutput().contains("Age: 30"), "Should contain Age: 30");
    }
    
    @Test
    public void testFunctionDefinition() throws ExecutionException {
        String code = "def greet(name):\n" +
                     "    return f'Hello, {name}!'\n" +
                     "\n" +
                     "print(greet('World'))";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Hello, World!"), 
                  "Output should contain 'Hello, World!'");
    }
    
    @Test
    public void testInfiniteLoopTimeout() throws ExecutionException {
        String code = "while True:\n" +
                     "    pass";  // Infinite loop
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON, 2); // 2 second timeout
        
        assertFalse(result.isSuccess(), "Execution should fail due to timeout");
        assertTrue(result.isTimedOut(), "Should be marked as timed out");
        assertTrue(result.getError().contains("timed out"), "Error should mention timeout");
    }
    
    @Test
    public void testImportStatement() throws ExecutionException {
        String code = "import math\n" +
                     "print(f'Pi: {math.pi:.2f}')\n" +
                     "print(f'Square root of 16: {math.sqrt(16)}')";
        
        ExecutionResult result = executor.execute(code, LanguageType.PYTHON);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Pi: 3.14"), "Should contain Pi value");
        assertTrue(result.getOutput().contains("Square root of 16: 4"), 
                  "Should contain square root result");
    }
}
