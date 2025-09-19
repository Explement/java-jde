package org.explement.jde.service;

import java.io.*;
import java.util.Scanner;

public class FileIOService {
    public void saveFile(String content, File file) { // Save file using BufferedWriter
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public String loadFile(File file) { // Load File using StringBuilder
        StringBuilder sb = new StringBuilder();
        try (Scanner fileReader = new Scanner(file)) {
            while (fileReader.hasNextLine()) {
                sb.append(fileReader.nextLine()).append("\n");
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }
}
