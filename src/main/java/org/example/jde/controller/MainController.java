package org.example.jde.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.jde.service.CompilerService;
import org.example.jde.service.FileIOService;
import org.example.jde.service.SyntaxHighlighterService;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;
import java.util.Optional;

public class MainController {

    // Services
    private final SyntaxHighlighterService highlighterService = new SyntaxHighlighterService();
    private final FileIOService fileIOService = new FileIOService();
    private final CompilerService compilerService = new CompilerService();

    // JavaFX Objects
    private CodeArea codeArea;
    @FXML private  VBox mainVBox;

    // Tracks file changes w/o being edited
    private boolean dirty;
    // File being edited absolute path
    private String editedFile;

    @FXML
    private void initialize() { // Self-explanatory
        // Initialize the new CodeArea
        codeArea = new CodeArea();

        // Set a LineNumberFactory and its FXML ID
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setId("codeArea");

        // Create a new ScrollPane for the CodeArea
        VirtualizedScrollPane<CodeArea> scrollPane =  new VirtualizedScrollPane<>(codeArea);

        // Add it to the VBox and make sure it takes up space
        mainVBox.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Add a listener for changes to apply syntax highlighting and dirty check
        codeArea.textProperty().addListener((obs, oldText, newText) -> { // Everytime code is changed
            dirty = true;
            codeArea.setStyleSpans(0, highlighterService.computeHighlighting(newText));
        });
    }

    @FXML
    protected void onNewJavaFile() { // Triggers when user clicks 'New Java File' under the MenuBar
        if (dirty && !codeArea.getText().isEmpty()) { // Prompt a save if user is currently on a dirty file
            promptSaveFile();
        }

        // Default name (not absolute)
        editedFile = "unnamed_file.java";

        // Clear codeArea for the new file
        codeArea.clear();
    }

    private void promptSaveFile() { // Prompts user to save via the 'dirty' variable
        // Create a new 'Alert' with the confirmation UI
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        // Set header and context text for the UI
        alert.setHeaderText("Would you like to save the file?");
        alert.setContentText("unnamedfile.java");

        // Wait for alert to show
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) { // Save and set dirty to false
            saveFile();
            dirty = false;
        } else { // Don't save and move on
            System.out.println("Not saving");
        }
    }

    @FXML
    protected void saveFile() { // Triggers when user clicks 'Save File' under the MenuBar
        if (editedFile == null ||!new File(editedFile).isAbsolute()) { // Make user save file under proper directory if file is not absolute
            saveFileAs();
        }

        // Save the file using FileIOService
        File file = new File(editedFile);
        fileIOService.saveFile(codeArea.getText(), file);

        // Print file's absolute path and set dirty to false
        System.out.println("Saved file: " + file.getAbsolutePath());
        dirty = false;
    }

    @FXML
    protected void saveFileAs() { // Triggers when user clicks 'Save File As' under the MenuBar
        // Create a new FileChooser and set it's title
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");

        // Suggest default name and extension (e.g. a .java file)
        fileChooser.setInitialFileName("unnamed_file.java");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Java Files", "*.java")
        );

        // Create a new variable with the chosen file
        File file = fileChooser.showSaveDialog(mainVBox.getScene().getWindow());
        if (file != null) { // If user pressed on a valid file/folder
            // Store editedFile as an absolute path and use FileIOService to save the file
            editedFile = file.getAbsolutePath();
            fileIOService.saveFile(codeArea.getText(), file);

            // Print file's absolute path via editedFile and set dirty to false
            System.out.println("File saved: " + editedFile);
            dirty = false;
        }
    }

    @FXML
    protected void loadFile() { // Triggers when user clicks 'Open File' under the MenuBar
        // Create a new FileChooser and set it's title
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load File");

        // Add extension filter
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Java Files", "*.java")
        );

        // Create a new variable with the chosen file
        File file = fileChooser.showOpenDialog(mainVBox.getScene().getWindow());
        if  (file == null) { // If user didn't press on a valid file/folder
            System.out.println("No file selected");
            return;
        }

        // Clear CodeArea and set editedFile to the selected file path (absolute)
        codeArea.clear();
        editedFile = file.getAbsolutePath();

        // Set dirty to false and replace the codeArea text with the new File's text
        dirty = false;
        codeArea.replaceText(fileIOService.loadFile(file));
    }

    @FXML
    protected void runJavaFile() throws IOException, InterruptedException { // Triggers when user clicks 'Compile' under the MenuBar
        if (dirty || editedFile == null) { // Triggers if file is dirty or there is no saved file being edited
            saveFile();
            if (editedFile == null) {
                // Cancelled save
                System.out.println("No file selected");
                return;
            }
        }

        // Create a new file via the editedFile variable
        File javaFile = new  File(editedFile);

        // Set up directory, non-absolute File name, and the File's class name
        String dir = javaFile.getParent();
        String fileName =  javaFile.getName();
        String className = fileName.substring(0, fileName.lastIndexOf('.'));

        StringBuilder output =  compilerService.compileAndRun(javaFile, dir, fileName, className, editedFile);

        // System.out.println("Captured Output: " + output.toString());
        for (String s :  output.toString().split("\n")) { // For every String (line) in StringBuilder
            System.out.println("> " + s);
        }
    }
}
