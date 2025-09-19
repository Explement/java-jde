package org.explement.jde.util;

import javafx.stage.FileChooser;

import java.io.File;

public class FileChooserUtils {
    // TODO: Fix 'desktop' not being shown at all

    private FileChooserUtils(){} // Create private constructor

    public static FileChooser createSaveJavaFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");

        // Set default directory
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        if (desktop.exists() && desktop.isDirectory()) { // If desktop exists and is a valid directory
            fileChooser.setInitialDirectory(desktop);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); // Fallback to user.home (C:/Users/User)
        }

        fileChooser.setInitialFileName("unnamed_file.java");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Java Files", "*.java")
        );

        return fileChooser;
    }

    public static FileChooser createLoadJavaFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load File");

        // Set default directory
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        if (desktop.exists() && desktop.isDirectory()) { // If desktop exists and is a valid directory
            fileChooser.setInitialDirectory(desktop);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))); // Fallback to user.home (C:/Users/User)
        }

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Java Files", "*.java")
        );

        return fileChooser;
    }
}
