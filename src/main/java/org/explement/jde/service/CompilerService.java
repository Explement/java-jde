package org.explement.jde.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CompilerService {
    public StringBuilder compileAndRun(File javaFile, String editedFile) throws IOException, InterruptedException {
        // Directory, file name, and class name of the Java file
        String dir = javaFile.getParent();
        String fileName = javaFile.getName();
        String className = fileName.substring(0, fileName.lastIndexOf('.'));

        // Compile the Java file
        Process compile = new ProcessBuilder("javac", editedFile)
                .directory(new File(dir))
                .start();
        int compileResult = compile.waitFor();
        if (compileResult != 0) { // Compilation failed
            System.out.println("Compilation failed");
            return new StringBuilder("Compilation failed");
        }

        // Run the compiled class
        Process run = new ProcessBuilder("java", "-cp", dir, className)
                .redirectErrorStream(true)
                .start();

        // Capture output from the process
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(run.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        run.waitFor(); // Wait for the process to finish

        return output;
    }
}
