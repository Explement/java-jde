package org.explement.jde.service;

import java.io.*;
import java.util.Scanner;

public class FileIOService {
    public void saveFile(String content, File file) { // Save file using BufferedWriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) { // Create new BufferedWriter with FileWriter
            // Write the String into the FileWriter
            writer.write(content);
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public String loadFile(File file) { // Load File using StringBuilder
        // Instantiate the StringBuilder
        StringBuilder sb = new StringBuilder();
        try (Scanner fileReader = new Scanner(file)) { // Create a new scanner for the File
            while (fileReader.hasNextLine()) { // If fileReader has next line
                // Save and append the read line
                String line = fileReader.nextLine();
                sb.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        // Return the StringBuilder
        return sb.toString();
    }
}
