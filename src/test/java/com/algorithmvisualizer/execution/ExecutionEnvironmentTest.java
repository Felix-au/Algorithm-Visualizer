package com.algorithmvisualizer.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Files;

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

    @Test
    public void testDynamicDetectionWithMockFolder() throws Exception {
        Path tempRoot = Files.createTempDirectory("algobuddy_mock_root");
        try {
            // Mock MinGW (nested deep, e.g. level 3)
            Path mockMinGWBins = tempRoot.resolve("custom-compilers/gcc-v15/bin");
            Files.createDirectories(mockMinGWBins);
            Files.createFile(mockMinGWBins.resolve("gcc.exe"));
            Files.createFile(mockMinGWBins.resolve("g++.exe"));

            // Mock JDK (nested level 2)
            Path mockJDKBins = tempRoot.resolve("my-custom-jdk/bin");
            Files.createDirectories(mockJDKBins);
            Files.createFile(mockJDKBins.resolve("javac.exe"));
            Files.createFile(mockJDKBins.resolve("java.exe"));

            // Mock Python (nested level 1)
            Path mockPython = tempRoot.resolve("python-embed-dist");
            Files.createDirectories(mockPython);
            Files.createFile(mockPython.resolve("python.exe"));

            // Instantiate ExecutionEnvironment pointed to this temp root
            ExecutionEnvironment mockEnv = new ExecutionEnvironment(tempRoot, true);

            // Verifications
            assertNotNull(mockEnv.getGccPath(), "Should find gcc.exe dynamically");
            assertTrue(mockEnv.getGccPath().contains("custom-compilers"), "Path should contain dynamic folder");
            
            assertNotNull(mockEnv.getGppPath(), "Should find g++.exe dynamically");
            assertTrue(mockEnv.getGppPath().contains("custom-compilers"), "Path should contain dynamic folder");

            assertNotNull(mockEnv.getJavacPath(), "Should find javac.exe dynamically");
            assertTrue(mockEnv.getJavacPath().contains("my-custom-jdk"), "Path should contain dynamic folder");

            assertNotNull(mockEnv.getJavaPath(), "Should find java.exe dynamically");
            assertTrue(mockEnv.getJavaPath().contains("my-custom-jdk"), "Path should contain dynamic folder");

            assertNotNull(mockEnv.getPythonPath(), "Should find python.exe dynamically");
            assertTrue(mockEnv.getPythonPath().contains("python-embed-dist"), "Path should contain dynamic folder");

        } finally {
            // Clean up mock files
            Files.walk(tempRoot)
                 .sorted((a, b) -> -a.compareTo(b))
                 .forEach(path -> {
                     try { Files.delete(path); } catch (Exception ignored) {}
                 });
        }
    }

    @Test
    public void testDynamicEnvironmentDetection() throws Exception {
        Path root = environment.getApplicationRoot();
        Path mingwPath = root.resolve("mingw64");
        Path jdkPath = root.resolve("jdk-17.0.18");
        Path pythonPath = root.resolve("python-3.11.8");

        Path tempMingw = root.resolve("mingw64_temp_test_rename");
        Path tempJdk = root.resolve("jdk-17.0.18_temp_test_rename");
        Path tempPython = root.resolve("python-3.11.8_temp_test_rename");

        boolean movedMingw = false;
        boolean movedJdk = false;
        boolean movedPython = false;

        try {
            // Rename to simulate different directory names
            if (Files.exists(mingwPath)) {
                Files.move(mingwPath, tempMingw);
                movedMingw = true;
            }
            if (Files.exists(jdkPath)) {
                Files.move(jdkPath, tempJdk);
                movedJdk = true;
            }
            if (Files.exists(pythonPath)) {
                Files.move(pythonPath, tempPython);
                movedPython = true;
            }

            // If we didn't have any of the local environments, skip this test
            if (!movedMingw && !movedJdk && !movedPython) {
                System.out.println("No local compilers to temporarily rename. Skipping integration test.");
                return;
            }

            // Create new environment and run checks
            ExecutionEnvironment dynamicEnv = new ExecutionEnvironment(root, true);

            if (movedMingw) {
                String gcc = dynamicEnv.getGccPath();
                assertTrue(gcc.contains("mingw64_temp_test_rename"), "Gcc should be found in renamed folder: " + gcc);
                String gpp = dynamicEnv.getGppPath();
                assertTrue(gpp.contains("mingw64_temp_test_rename"), "Gpp should be found in renamed folder: " + gpp);
            }
            if (movedJdk) {
                String javac = dynamicEnv.getJavacPath();
                assertTrue(javac.contains("jdk-17.0.18_temp_test_rename"), "Javac should be found in renamed folder: " + javac);
                String java = dynamicEnv.getJavaPath();
                assertTrue(java.contains("jdk-17.0.18_temp_test_rename"), "Java should be found in renamed folder: " + java);
            }
            if (movedPython) {
                String python = dynamicEnv.getPythonPath();
                assertTrue(python.contains("python-3.11.8_temp_test_rename"), "Python should be found in renamed folder: " + python);
            }

        } finally {
            // Restore original directory names
            if (movedMingw) {
                try { Files.move(tempMingw, mingwPath); } catch (Exception e) { System.err.println("Failed to restore mingw: " + e.getMessage()); }
            }
            if (movedJdk) {
                try { Files.move(tempJdk, jdkPath); } catch (Exception e) { System.err.println("Failed to restore jdk: " + e.getMessage()); }
            }
            if (movedPython) {
                try { Files.move(tempPython, pythonPath); } catch (Exception e) { System.err.println("Failed to restore python: " + e.getMessage()); }
            }
        }
    }
}
