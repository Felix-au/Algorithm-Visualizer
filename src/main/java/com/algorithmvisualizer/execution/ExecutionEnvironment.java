package com.algorithmvisualizer.execution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages paths to bundled compilers and interpreters.
 * Provides fallback to system-installed tools if bundled ones aren't found.
 */
public class ExecutionEnvironment {
    
    private static final String MINGW_PATH = "mingw64/bin";
    private static final String JDK_PATH = "jdk-17.0.18/bin";
    private static final String PYTHON_PATH = "python-3.11.8";
    
    private final Path applicationRoot;
    private final boolean useBundled;
    
    public ExecutionEnvironment() {
        this(detectApplicationRoot(), true);
    }
    
    public ExecutionEnvironment(Path applicationRoot, boolean useBundled) {
        this.applicationRoot = applicationRoot;
        this.useBundled = useBundled;
    }
    
    /**
     * Detect the application root directory
     */
    private static Path detectApplicationRoot() {
        // Try to get the directory where the application is running
        String userDir = System.getProperty("user.dir");
        Path root = Paths.get(userDir);
        
        // Verify this looks like our application root by checking for bundled directories
        if (Files.exists(root.resolve(MINGW_PATH)) || 
            Files.exists(root.resolve(JDK_PATH)) || 
            Files.exists(root.resolve(PYTHON_PATH))) {
            return root;
        }
        
        return root;
    }
    
    /**
     * Get path to GCC compiler (C)
     */
    public String getGccPath() throws ExecutionException {
        if (useBundled) {
            Path gccPath = applicationRoot.resolve(MINGW_PATH).resolve("gcc.exe");
            if (Files.exists(gccPath)) {
                return gccPath.toAbsolutePath().toString();
            }
        }
        
        // Fallback to system GCC
        String systemGcc = findInSystemPath("gcc.exe");
        if (systemGcc != null) {
            return systemGcc;
        }
        
        throw new ExecutionException("GCC compiler not found. Please ensure mingw64 is bundled or GCC is installed on the system.");
    }
    
    /**
     * Get path to G++ compiler (C++)
     */
    public String getGppPath() throws ExecutionException {
        if (useBundled) {
            Path gppPath = applicationRoot.resolve(MINGW_PATH).resolve("g++.exe");
            if (Files.exists(gppPath)) {
                return gppPath.toAbsolutePath().toString();
            }
        }
        
        // Fallback to system G++
        String systemGpp = findInSystemPath("g++.exe");
        if (systemGpp != null) {
            return systemGpp;
        }
        
        throw new ExecutionException("G++ compiler not found. Please ensure mingw64 is bundled or G++ is installed on the system.");
    }
    
    /**
     * Get path to Java compiler (javac)
     */
    public String getJavacPath() throws ExecutionException {
        if (useBundled) {
            Path javacPath = applicationRoot.resolve(JDK_PATH).resolve("javac.exe");
            if (Files.exists(javacPath)) {
                return javacPath.toAbsolutePath().toString();
            }
        }
        
        // Fallback to system javac
        String systemJavac = findInSystemPath("javac.exe");
        if (systemJavac != null) {
            return systemJavac;
        }
        
        throw new ExecutionException("Java compiler (javac) not found. Please ensure JDK is bundled or installed on the system.");
    }
    
    /**
     * Get path to Java runtime (java)
     */
    public String getJavaPath() throws ExecutionException {
        if (useBundled) {
            Path javaPath = applicationRoot.resolve(JDK_PATH).resolve("java.exe");
            if (Files.exists(javaPath)) {
                return javaPath.toAbsolutePath().toString();
            }
        }
        
        // Fallback to system java
        String systemJava = findInSystemPath("java.exe");
        if (systemJava != null) {
            return systemJava;
        }
        
        throw new ExecutionException("Java runtime not found. Please ensure JDK is bundled or installed on the system.");
    }
    
    /**
     * Get path to Python interpreter
     */
    public String getPythonPath() throws ExecutionException {
        if (useBundled) {
            Path pythonPath = applicationRoot.resolve(PYTHON_PATH).resolve("python.exe");
            if (Files.exists(pythonPath)) {
                return pythonPath.toAbsolutePath().toString();
            }
        }
        
        // Fallback to system Python
        String systemPython = findInSystemPath("python.exe");
        if (systemPython != null) {
            return systemPython;
        }
        
        throw new ExecutionException("Python interpreter not found. Please ensure Python is bundled or installed on the system.");
    }
    
    /**
     * Get compiler/interpreter path for a specific language
     */
    public String getCompilerPath(LanguageType language) throws ExecutionException {
        switch (language) {
            case C:
                return getGccPath();
            case CPP:
                return getGppPath();
            case JAVA:
                return getJavacPath();
            case PYTHON:
                return getPythonPath();
            default:
                throw new ExecutionException("Unsupported language: " + language);
        }
    }
    
    /**
     * Check if all bundled environments are available
     */
    public boolean areBundledEnvironmentsAvailable() {
        try {
            getGccPath();
            getGppPath();
            getJavacPath();
            getJavaPath();
            getPythonPath();
            return true;
        } catch (ExecutionException e) {
            return false;
        }
    }
    
    /**
     * Check if a specific language environment is available
     */
    public boolean isLanguageAvailable(LanguageType language) {
        try {
            getCompilerPath(language);
            if (language == LanguageType.JAVA) {
                getJavaPath(); // Also need runtime for Java
            }
            return true;
        } catch (ExecutionException e) {
            return false;
        }
    }
    
    /**
     * Find executable in system PATH
     */
    private String findInSystemPath(String executable) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return null;
        
        String[] paths = pathEnv.split(File.pathSeparator);
        for (String path : paths) {
            File file = new File(path, executable);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
    
    public Path getApplicationRoot() {
        return applicationRoot;
    }
}
