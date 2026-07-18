package com.algorithmvisualizer.execution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manual test to debug compilation issues
 */
public class ManualTest {
    
    public static void main(String[] args) throws Exception {
        // Create a simple C file
        Path tempDir = Files.createTempDirectory("test_gcc_");
        Path sourceFile = tempDir.resolve("test.c");
        
        String code = "#include <stdio.h>\n" +
                     "int main() {\n" +
                     "    printf(\"Hello!\\n\");\n" +
                     "    return 0;\n" +
                     "}";
        
        Files.write(sourceFile, code.getBytes());
        
        System.out.println("Source file: " + sourceFile);
        System.out.println("File exists: " + Files.exists(sourceFile));
        System.out.println("File content:");
        System.out.println(new String(Files.readAllBytes(sourceFile)));
        
        // Try to compile
        String gccPath = "D:\\Users\\gg\\Desktop\\Algorithm Visualizer\\mingw64\\bin\\gcc.exe";
        Path exePath = tempDir.resolve("test.exe");
        
        ProcessBuilder pb = new ProcessBuilder(
            gccPath,
            sourceFile.toAbsolutePath().toString(),
            "-o",
            exePath.toAbsolutePath().toString()
        );
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);
        
        System.out.println("\nRunning: " + String.join(" ", pb.command()));
        
        Process process = pb.start();
        
        // Read output
        byte[] output = process.getInputStream().readAllBytes();
        int exitCode = process.waitFor();
        
        System.out.println("Exit code: " + exitCode);
        System.out.println("Output: " + new String(output));
        System.out.println("Executable exists: " + Files.exists(exePath));
        
        if (Files.exists(exePath)) {
            // Try to run it
            ProcessBuilder runPb = new ProcessBuilder(exePath.toAbsolutePath().toString());
            runPb.directory(tempDir.toFile());
            Process runProcess = runPb.start();
            byte[] runOutput = runProcess.getInputStream().readAllBytes();
            int runExitCode = runProcess.waitFor();
            
            System.out.println("\nRun exit code: " + runExitCode);
            System.out.println("Run output: " + new String(runOutput));
        }
        
        // Cleanup
        Files.deleteIfExists(sourceFile);
        Files.deleteIfExists(exePath);
        Files.deleteIfExists(tempDir);
    }
}
