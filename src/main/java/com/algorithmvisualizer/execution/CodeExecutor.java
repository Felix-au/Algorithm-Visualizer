package com.algorithmvisualizer.execution;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Main orchestrator for compiling and executing code in multiple languages.
 * Thread-safe and handles cleanup automatically.
 */
public class CodeExecutor {
    
    private final ExecutionEnvironment environment;
    private final long defaultTimeoutSeconds;
    private final Path tempDirectory;
    
    public CodeExecutor() throws ExecutionException {
        this(new ExecutionEnvironment(), 60);
    }
    
    public CodeExecutor(ExecutionEnvironment environment, long defaultTimeoutSeconds) throws ExecutionException {
        this.environment = environment;
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        
        try {
            this.tempDirectory = Files.createTempDirectory("algobuddy_exec_");
            // Register shutdown hook to clean up temp directory
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    deleteDirectory(tempDirectory);
                } catch (IOException e) {
                    System.err.println("Failed to clean up temp directory: " + e.getMessage());
                }
            }));
        } catch (IOException e) {
            throw new ExecutionException("Failed to create temporary directory", e);
        }
    }
    
    /**
     * Execute code with default timeout
     */
    public ExecutionResult execute(String code, LanguageType language) throws ExecutionException {
        return execute(code, language, defaultTimeoutSeconds, null);
    }
    
    /**
     * Execute code with custom timeout
     */
    public ExecutionResult execute(String code, LanguageType language, long timeoutSeconds) throws ExecutionException {
        return execute(code, language, timeoutSeconds, null);
    }
    
    /**
     * Execute code with input queue support for interactive programs
     */
    public ExecutionResult execute(String code, LanguageType language, long timeoutSeconds, 
                                   java.util.concurrent.BlockingQueue<String> inputQueue) throws ExecutionException {
        // Validate inputs
        if (code == null || code.trim().isEmpty()) {
            throw new ExecutionException("Code cannot be null or empty");
        }
        if (language == null) {
            throw new ExecutionException("Language cannot be null");
        }
        
        // Create unique execution directory
        Path executionDir = createExecutionDirectory();
        
        try {
            if (language.isCompiled()) {
                return executeCompiledLanguage(code, language, timeoutSeconds, executionDir, inputQueue);
            } else {
                return executeInterpretedLanguage(code, language, timeoutSeconds, executionDir, inputQueue);
            }
        } finally {
            // Clean up execution directory
            try {
                deleteDirectory(executionDir);
            } catch (IOException e) {
                System.err.println("Failed to clean up execution directory: " + e.getMessage());
            }
        }
    }
    
    /**
     * Execute compiled language (C, C++, Java)
     */
    private ExecutionResult executeCompiledLanguage(String code, LanguageType language, 
                                                   long timeoutSeconds, Path executionDir,
                                                   java.util.concurrent.BlockingQueue<String> inputQueue) throws ExecutionException {
        long startTime = System.currentTimeMillis();
        
        // Step 1: Write source file
        Path sourceFile = writeSourceFile(code, language, executionDir);
        
        // Step 2: Compile
        ExecutionResult compileResult = compile(sourceFile, language, executionDir, timeoutSeconds);
        if (!compileResult.isSuccess()) {
            return ExecutionResult.builder()
                    .success(false)
                    .error(compileResult.getError())
                    .output(compileResult.getOutput())
                    .exitCode(compileResult.getExitCode())
                    .compilationError(true)
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
        
        // Step 3: Execute
        Path executable = getExecutablePath(sourceFile, language, executionDir);
        ExecutionResult runResult = runExecutable(executable, language, executionDir, timeoutSeconds, inputQueue);
        
        return ExecutionResult.builder()
                .success(runResult.isSuccess())
                .output(runResult.getOutput())
                .error(runResult.getError())
                .exitCode(runResult.getExitCode())
                .timedOut(runResult.isTimedOut())
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }
    
    /**
     * Execute interpreted language (Python)
     */
    private ExecutionResult executeInterpretedLanguage(String code, LanguageType language,
                                                      long timeoutSeconds, Path executionDir,
                                                      java.util.concurrent.BlockingQueue<String> inputQueue) throws ExecutionException {
        long startTime = System.currentTimeMillis();
        
        // Write source file
        Path sourceFile = writeSourceFile(code, language, executionDir);
        
        // Execute directly
        String interpreterPath = environment.getCompilerPath(language);
        List<String> command = new ArrayList<>();
        command.add(interpreterPath);
        command.add(sourceFile.toAbsolutePath().toString());
        
        ProcessResult result = runProcess(command, executionDir, timeoutSeconds, language, inputQueue);
        
        return ExecutionResult.builder()
                .success(result.exitCode == 0 && !result.timedOut)
                .output(result.output)
                .error(result.error)
                .exitCode(result.exitCode)
                .timedOut(result.timedOut)
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }
    
    /**
     * Compile source code
     */
    private ExecutionResult compile(Path sourceFile, LanguageType language, 
                                   Path executionDir, long timeoutSeconds) throws ExecutionException {
        List<String> command = buildCompileCommand(sourceFile, language, executionDir);
        ProcessResult result = runProcess(command, executionDir, timeoutSeconds, null, null);
        
        return ExecutionResult.builder()
                .success(result.exitCode == 0)
                .output(result.output)
                .error(result.error)
                .exitCode(result.exitCode)
                .timedOut(result.timedOut)
                .build();
    }
    
    /**
     * Build compilation command
     */
    private List<String> buildCompileCommand(Path sourceFile, LanguageType language, 
                                            Path executionDir) throws ExecutionException {
        List<String> command = new ArrayList<>();
        
        switch (language) {
            case C:
                command.add(environment.getGccPath());
                // Use relative path if possible to avoid issues with spaces
                command.add(sourceFile.getFileName().toString());
                command.add("-o");
                command.add("program.exe");
                break;
                
            case CPP:
                command.add(environment.getGppPath());
                command.add(sourceFile.getFileName().toString());
                command.add("-o");
                command.add("program.exe");
                break;
                
            case JAVA:
                command.add(environment.getJavacPath());
                command.add("-d");
                command.add(".");
                command.add(sourceFile.getFileName().toString());
                break;
                
            default:
                throw new ExecutionException("Unsupported compiled language: " + language);
        }
        
        return command;
    }
    
    /**
     * Run compiled executable
     */
    private ExecutionResult runExecutable(Path executable, LanguageType language,
                                         Path executionDir, long timeoutSeconds,
                                         java.util.concurrent.BlockingQueue<String> inputQueue) throws ExecutionException {
        List<String> command = new ArrayList<>();
        
        if (language == LanguageType.JAVA) {
            command.add(environment.getJavaPath());
            command.add("-cp");
            command.add(".");
            command.add("Main"); // Assume class name is Main
        } else {
            // Use absolute path for C/C++ executables
            command.add(executionDir.resolve("program.exe").toAbsolutePath().toString());
        }
        
        ProcessResult result = runProcess(command, executionDir, timeoutSeconds, null, inputQueue);
        
        return ExecutionResult.builder()
                .success(result.exitCode == 0 && !result.timedOut)
                .output(result.output)
                .error(result.error)
                .exitCode(result.exitCode)
                .timedOut(result.timedOut)
                .build();
    }
    
    /**
     * Run a process with timeout and optional input support
     */
    private ProcessResult runProcess(List<String> command, Path workingDir, long timeoutSeconds, 
                                    LanguageType language, java.util.concurrent.BlockingQueue<String> inputQueue) {
        // Debug: print command
        System.out.println("DEBUG: Running command: " + String.join(" ", command));
        System.out.println("DEBUG: Working directory: " + workingDir);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(false);
        
        // Add MinGW bin directory to PATH for GCC/G++ to find its libraries
        Path mingwBinPath = environment.getApplicationRoot().resolve("mingw64/bin");
        if (Files.exists(mingwBinPath)) {
            String currentPath = pb.environment().get("PATH");
            String newPath = mingwBinPath.toAbsolutePath().toString() + File.pathSeparator + currentPath;
            pb.environment().put("PATH", newPath);
        }
        
        // Set UTF-8 encoding for Python to handle Unicode characters properly
        if (language == LanguageType.PYTHON) {
            pb.environment().put("PYTHONIOENCODING", "utf-8");
        }
        
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        int exitCode = -1;
        boolean timedOut = false;
        
        try {
            Process process = pb.start();
            
            // Create threads to read output and error streams
            ExecutorService executor = Executors.newFixedThreadPool(inputQueue != null ? 3 : 2);
            Future<String> outputFuture = executor.submit(() -> readStream(process.getInputStream()));
            Future<String> errorFuture = executor.submit(() -> readStream(process.getErrorStream()));
            
            // If input queue provided, create input writer thread
            Future<?> inputFuture = null;
            if (inputQueue != null) {
                inputFuture = executor.submit(() -> {
                    try (OutputStream os = process.getOutputStream();
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                        while (process.isAlive()) {
                            try {
                                // Poll for input with timeout to check if process is still alive
                                String input = inputQueue.poll(100, TimeUnit.MILLISECONDS);
                                if (input != null) {
                                    writer.write(input);
                                    writer.newLine();
                                    writer.flush();
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        // Process ended or stream closed - this is normal
                    }
                });
            }
            
            // Wait for process with timeout
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                timedOut = true;
                error.append("Execution timed out after ").append(timeoutSeconds).append(" seconds");
                // Try to get partial output
                try {
                    output.append(outputFuture.get(100, TimeUnit.MILLISECONDS));
                } catch (Exception ignored) {}
                try {
                    error.append(errorFuture.get(100, TimeUnit.MILLISECONDS));
                } catch (Exception ignored) {}
            } else {
                exitCode = process.exitValue();
                System.out.println("DEBUG: Process exit code: " + exitCode);
                try {
                    output.append(outputFuture.get(2, TimeUnit.SECONDS));
                    System.out.println("DEBUG: Output captured: " + output.length() + " chars");
                    if (output.length() > 0) {
                        System.out.println("DEBUG: Output content: " + output.toString());
                    }
                } catch (TimeoutException e) {
                    output.append("[Output stream read timeout]");
                } catch (Exception e) {
                    output.append("[Error reading output: ").append(e.getMessage()).append("]");
                }
                try {
                    error.append(errorFuture.get(2, TimeUnit.SECONDS));
                    System.out.println("DEBUG: Error captured: " + error.length() + " chars");
                    if (error.length() > 0) {
                        System.out.println("DEBUG: Error content: " + error.toString());
                    }
                } catch (TimeoutException e) {
                    error.append("[Error stream read timeout]");
                } catch (Exception e) {
                    error.append("[Error reading error stream: ").append(e.getMessage()).append("]");
                }
            }
            
            executor.shutdownNow();
            
        } catch (IOException e) {
            error.append("Failed to start process: ").append(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error.append("Process interrupted: ").append(e.getMessage());
        } catch (Exception e) {
            error.append("Execution error: ").append(e.getMessage());
        }
        
        return new ProcessResult(output.toString(), error.toString(), exitCode, timedOut);
    }
    
    /**
     * Read input stream to string
     */
    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
    
    /**
     * Write source code to file
     */
    private Path writeSourceFile(String code, LanguageType language, Path executionDir) throws ExecutionException {
        String filename = getSourceFileName(language);
        Path sourceFile = executionDir.resolve(filename);
        
        try {
            Files.write(sourceFile, code.getBytes(StandardCharsets.UTF_8));
            return sourceFile;
        } catch (IOException e) {
            throw new ExecutionException("Failed to write source file", e);
        }
    }
    
    /**
     * Get source file name based on language
     */
    private String getSourceFileName(LanguageType language) {
        if (language == LanguageType.JAVA) {
            return "Main.java"; // Java requires class name to match filename
        }
        return "program" + language.getExtension();
    }
    
    /**
     * Get executable path after compilation
     */
    private Path getExecutablePath(Path sourceFile, LanguageType language, Path executionDir) {
        if (language == LanguageType.JAVA) {
            return executionDir.resolve("Main.class");
        }
        return executionDir.resolve("program.exe");
    }
    
    /**
     * Create unique execution directory
     */
    private Path createExecutionDirectory() throws ExecutionException {
        try {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            Path execDir = tempDirectory.resolve("exec_" + uniqueId);
            Files.createDirectories(execDir);
            return execDir;
        } catch (IOException e) {
            throw new ExecutionException("Failed to create execution directory", e);
        }
    }
    
    /**
     * Recursively delete directory
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) return;
        
        Files.walk(directory)
            .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    // Ignore deletion errors
                }
            });
    }
    
    /**
     * Internal class to hold process execution results
     */
    private static class ProcessResult {
        final String output;
        final String error;
        final int exitCode;
        final boolean timedOut;
        
        ProcessResult(String output, String error, int exitCode, boolean timedOut) {
            this.output = output;
            this.error = error;
            this.exitCode = exitCode;
            this.timedOut = timedOut;
        }
    }
    
    public ExecutionEnvironment getEnvironment() {
        return environment;
    }
}
