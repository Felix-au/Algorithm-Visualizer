package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for C++ code execution
 */
public class CppExecutionTest {
    
    private CodeExecutor executor;
    
    @BeforeEach
    public void setUp() throws ExecutionException {
        executor = new CodeExecutor();
    }
    
    @Test
    public void testSimpleHelloWorld() throws ExecutionException {
        String code = "#include <iostream>\n" +
                     "int main() {\n" +
                     "    std::cout << \"Hello, C++!\" << std::endl;\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.CPP);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Hello, C++!"), 
                  "Output should contain 'Hello, C++!'");
        assertEquals(0, result.getExitCode(), "Exit code should be 0");
    }
    
    @Test
    public void testVectorUsage() throws ExecutionException {
        String code = "#include <iostream>\n" +
                     "#include <vector>\n" +
                     "int main() {\n" +
                     "    std::vector<int> nums = {1, 2, 3, 4, 5};\n" +
                     "    int sum = 0;\n" +
                     "    for(int n : nums) sum += n;\n" +
                     "    std::cout << \"Sum: \" << sum << std::endl;\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.CPP);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Sum: 15"), "Output should contain 'Sum: 15'");
    }
    
    @Test
    public void testClassAndObject() throws ExecutionException {
        String code = "#include <iostream>\n" +
                     "#include <string>\n" +
                     "class Person {\n" +
                     "public:\n" +
                     "    std::string name;\n" +
                     "    Person(std::string n) : name(n) {}\n" +
                     "    void greet() { std::cout << \"Hello, \" << name << std::endl; }\n" +
                     "};\n" +
                     "int main() {\n" +
                     "    Person p(\"Alice\");\n" +
                     "    p.greet();\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.CPP);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("Hello, Alice"), 
                  "Output should contain 'Hello, Alice'");
    }
    
    @Test
    public void testCompilationError() throws ExecutionException {
        String code = "#include <iostream>\n" +
                     "int main() {\n" +
                     "    std::cout << \"Missing semicolon\"\n" +  // Missing semicolon
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.CPP);
        
        assertFalse(result.isSuccess(), "Execution should fail");
        assertTrue(result.isCompilationError(), "Should have compilation error");
    }
    
    @Test
    public void testStdString() throws ExecutionException {
        String code = "#include <iostream>\n" +
                     "#include <string>\n" +
                     "int main() {\n" +
                     "    std::string msg = \"C++ Strings\";\n" +
                     "    std::cout << msg << \" are great!\" << std::endl;\n" +
                     "    return 0;\n" +
                     "}";
        
        ExecutionResult result = executor.execute(code, LanguageType.CPP);
        
        assertTrue(result.isSuccess(), "Execution should succeed");
        assertTrue(result.getOutput().contains("C++ Strings are great!"), 
                  "Output should contain full message");
    }
}
