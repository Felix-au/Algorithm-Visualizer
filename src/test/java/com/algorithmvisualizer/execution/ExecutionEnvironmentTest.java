package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExecutionEnvironment - verifies bundled compilers/interpreters are found
 */
public class ExecutionEnvironmentTest {
    
    private ExecutionEnvironment environment;
    
    @BeforeEach
    public void setUp() {
        environment = new ExecutionEnvironment();
    }
    
    @Test
    public void testApplicationRootDetection() {
        assertNotNull(environment.getApplicationRoot(), "Application root should be detected");
        assertTrue(environment.getApplicationRoot().toFile().exists(), "Application root should exist");
    }
    
    @Test
    public void testGccPathAvailable() {
        try {
            String gccPath = environment.getGccPath();
            assertNotNull(gccPath, "GCC path should not be null");
            assertFalse(gccPath.isEmpty(), "GCC path should not be empty");
            System.out.println("GCC found at: " + gccPath);
        } catch (ExecutionException e) {
            fail("GCC should be available: " + e.getMessage());
        }
    }
    
    @Test
    public void testGppPathAvailable() {
        try {
            String gppPath = environment.getGppPath();
            assertNotNull(gppPath, "G++ path should not be null");
            assertFalse(gppPath.isEmpty(), "G++ path should not be empty");
            System.out.println("G++ found at: " + gppPath);
        } catch (ExecutionException e) {
            fail("G++ should be available: " + e.getMessage());
        }
    }
    
    @Test
    public void testJavacPathAvailable() {
        try {
            String javacPath = environment.getJavacPath();
            assertNotNull(javacPath, "Javac path should not be null");
            assertFalse(javacPath.isEmpty(), "Javac path should not be empty");
            System.out.println("Javac found at: " + javacPath);
        } catch (ExecutionException e) {
            fail("Javac should be available: " + e.getMessage());
        }
    }
    
    @Test
    public void testJavaPathAvailable() {
        try {
            String javaPath = environment.getJavaPath();
            assertNotNull(javaPath, "Java path should not be null");
            assertFalse(javaPath.isEmpty(), "Java path should not be empty");
            System.out.println("Java found at: " + javaPath);
        } catch (ExecutionException e) {
            fail("Java should be available: " + e.getMessage());
        }
    }
    
    @Test
    public void testPythonPathAvailable() {
        try {
            String pythonPath = environment.getPythonPath();
            assertNotNull(pythonPath, "Python path should not be null");
            assertFalse(pythonPath.isEmpty(), "Python path should not be empty");
            System.out.println("Python found at: " + pythonPath);
        } catch (ExecutionException e) {
            fail("Python should be available: " + e.getMessage());
        }
    }
    
    @Test
    public void testCLanguageAvailable() {
        assertTrue(environment.isLanguageAvailable(LanguageType.C), 
                  "C language should be available");
    }
    
    @Test
    public void testCppLanguageAvailable() {
        assertTrue(environment.isLanguageAvailable(LanguageType.CPP), 
                  "C++ language should be available");
    }
    
    @Test
    public void testJavaLanguageAvailable() {
        assertTrue(environment.isLanguageAvailable(LanguageType.JAVA), 
                  "Java language should be available");
    }
    
    @Test
    public void testPythonLanguageAvailable() {
        assertTrue(environment.isLanguageAvailable(LanguageType.PYTHON), 
                  "Python language should be available");
    }
    
    @Test
    public void testGetCompilerPathForEachLanguage() {
        for (LanguageType language : LanguageType.values()) {
            try {
                String path = environment.getCompilerPath(language);
                assertNotNull(path, "Compiler path for " + language + " should not be null");
                System.out.println(language + " compiler: " + path);
            } catch (ExecutionException e) {
                fail("Compiler for " + language + " should be available: " + e.getMessage());
            }
        }
    }
}
