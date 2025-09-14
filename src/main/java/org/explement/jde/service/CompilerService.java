package org.explement.jde.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CompilerService {
    public StringBuilder compileAndRun(File javaFile, String editedFile) throws IOException, InterruptedException {
        // Directory of the Java File
        String dir = javaFile.getParent();
        // Name of the Java File
        String fileName =  javaFile.getName();
        // Main class of the Java File
        String className = fileName.substring(0, fileName.lastIndexOf('.'));

        // Compile editedFile and wait for the exit code
        Process compile = new ProcessBuilder("javac", editedFile)
                .directory(new File(dir))
                .start();
        int compileResult = compile.waitFor();
        if (compileResult != 0) { // If the result was not 0 (not successful)
            System.out.println("Compilation failed");
            return new StringBuilder("Compilation failed"); // Return a StringBuilder with a error message
        }

        // Run the compiled file and redirect the error stream to one stream
        Process run = new ProcessBuilder("java", "-cp", dir, className)
                .redirectErrorStream(true)
                .start();

        // Capture the stream from the compiled file that was run and show via output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader( // Try to create a BufferedReader reading the InputStream of the 'run' Process
                new InputStreamReader(run.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) { // While there is still a line to read
                output.append(line).append("\n"); // Append the line to the StringBuilder and a newline
            }
        }

        // Wait for the compiled file to finish running before showing output
        run.waitFor();

        return output;
    }

}
