package org.explement.jde.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.explement.jde.service.CompilerService;
import org.explement.jde.service.FileIOService;
import org.explement.jde.service.SyntaxHighlighterService;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.undo.UndoManager;

import java.io.*;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    // Services
    private final SyntaxHighlighterService highlighterService = new SyntaxHighlighterService();
    private final FileIOService fileIOService = new FileIOService();
    private final CompilerService compilerService = new CompilerService();

    // Enum for promptSaveFile()
    enum promptUserChoice {
        SAVE,
        DONT_SAVE,
        CANCEL
    }

    // JavaFX Objects
    private CodeArea codeArea;
    private TextArea output;
    @FXML private  VBox mainVBox;

    // File being edited absolute path
    private String editedFile;
    // Pattern for whitespace (tabs, spaces) at start of line
    private static final Pattern whiteSpace = Pattern.compile( "^\\s+" );
    // Enable highlight check
    private boolean highlightChecker = true;
    // Undo manager for CodeArea
    private UndoManager<?> undoManager;
    // Last saved content
    private String savedContent;

    @FXML
    private void initialize() { // On JavaFX init
        // Initialize the new CodeArea and Output
        codeArea = new CodeArea();
        output = new TextArea();

        // Set codeArea UndoManager
        undoManager = codeArea.getUndoManager();

        // Make output uneditable
        output.setEditable(false);

        // Create a ContextMenu for the output
        ContextMenu contextMenu = new ContextMenu();

        // Clear the output
        MenuItem clearItem = new MenuItem("Clear");
        clearItem.setOnAction(e -> output.clear());

        // Copy selected text
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> output.copy());

        // Separator
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        // Select all text
        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> output.selectAll());

        // Add all items and set the ContextMenu
        contextMenu.getItems().addAll(clearItem, copyItem, separatorMenuItem, selectAllItem);
        output.setContextMenu(contextMenu);

        // Set a LineNumberFactory and a FXML ID for the CodeArea
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setId("codeArea");

        // Create a new ScrollPane for the CodeArea
        VirtualizedScrollPane<CodeArea> scrollPane =  new VirtualizedScrollPane<>(codeArea);

        // Add both to the VBox and make sure it takes up space
        mainVBox.getChildren().addAll(scrollPane, output);

        // Add a listener to split the ratios between the CodeArea and Output
        mainVBox.heightProperty().addListener((obs, oldVal, newVal) -> { // Everytime height is changed in the VBox
            double height = newVal.doubleValue();
            scrollPane.setPrefHeight(height * 0.7);
            output.setPrefHeight(height * 0.3);
        });

        // Add a listener for changes to apply syntax highlighting and dirty check
        codeArea.textProperty().addListener((obs, oldText, newText) -> { // Everytime code is changed
            if (highlightChecker) { // Highlight checker
                codeArea.setStyleSpans(0, highlighterService.computeHighlighting(newText));
            }
        });

        // Add a listener for changes to apply auto indentation
        codeArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) { // Enter key
                // Get caret position and current paragraph
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                // Match to pattern
                Matcher m0 = whiteSpace.matcher( codeArea.getParagraph( currentParagraph-1 ).getSegments().get(0));
                if ( m0.find() ) { // If it gets a match, insert tab to fill in
                    Platform.runLater( () -> codeArea.insertText( caretPosition, m0.group() ) );
                }
            }
        });

        newJavaFile();
    }

    @FXML
    protected void onNewJavaFile() { // Triggers when user clicks 'New Java File' under the MenuBar
        if (isDirty() && !codeArea.getText().isEmpty()) { // Prompt a save if user is currently on a dirty file
            promptUserChoice promptSave = promptSaveFile();
            if (promptSave == promptUserChoice.SAVE) { // Save and create new file
                saveFile();
            } else if (promptSave == promptUserChoice.CANCEL) {
                return;
            }
            newJavaFile();
        }
    }
    private void newJavaFile() {
        // Default name (not absolute)
        editedFile = "unnamed_file.java";

        // Clear codeArea for the new file
        codeArea.clear();

        // Put empty space into savedContent for new Java file
        savedContent = codeArea.getText();
    }
    private promptUserChoice promptSaveFile() { // Prompts user to save
        // Create a new 'Alert' with the confirmation UI
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        // Set config for alert
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes.");
        alert.setContentText("Would you like to save changes?");

        // Instantiate buttons
        ButtonType saveAndExit = new ButtonType("Save & Exit");
        ButtonType exitWithoutSaving = new ButtonType("Exit Without Saving");
        ButtonType cancel = new ButtonType("Cancel");

        // Add buttons to alert
        alert.getButtonTypes().setAll(saveAndExit, exitWithoutSaving, cancel);

        // Final userChoice
        promptUserChoice[] userChoice = new promptUserChoice[1];
        userChoice[0] = promptUserChoice.DONT_SAVE; // Default

        // Wait for results
        alert.showAndWait().ifPresent(type -> {
            if (type == saveAndExit) { // Save and exit
                userChoice[0] = promptUserChoice.SAVE;
            } else if (type == cancel) { // Cancel
                userChoice[0] = promptUserChoice.CANCEL;
            }
            // exitWithoutSaving is already set as Default
        });

        // Return userChoice
        return userChoice[0];
    }

    @FXML
    public void saveFile() { // Triggers when user clicks 'Save File' under the MenuBar
        if (!isDirty()) { // Make sure file needs to save
            return;
        }

        if (editedFile == null ||!new File(editedFile).isAbsolute()) { // Make user save file under proper directory if file is not absolute
            saveFileAs();
            if (editedFile == null) { // If not selected
                return;
            }
        }

        // Save the file using FileIOService
        File file = new File(editedFile);
        fileIOService.saveFile(codeArea.getText(), file);

        // Put saved contents into savedContent
        savedContent = codeArea.getText();

        // Mark undoManager saved position
        undoManager.mark();

        // Print file's absolute path
        printOutput("Saved file: " + file.getAbsolutePath());
        appendOutput("Saved file: " + file.getAbsolutePath());
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

            // Put saved contents into savedContent
            savedContent = codeArea.getText();

            // Print file's absolute path via editedFile
            printOutput("File saved: " + editedFile);
            appendOutput("File saved: " + editedFile);
        }
    }

    @FXML
    protected void loadFile() { // Triggers when user clicks 'Open File' under the MenuBar
        if (isDirty()) { // Check if file needs to be saved
            promptUserChoice userChoice = promptSaveFile();
            if (userChoice == promptUserChoice.SAVE) { // Save file and continue editing
                saveFile();
            } else if (userChoice == promptUserChoice.CANCEL) { // Cancel and don't save
                return;
            } else if (userChoice == promptUserChoice.DONT_SAVE) { // Continue and don't save file
            }
        }

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
            printOutput("No file selected");
            appendOutput("No file selected");
            return;
        }

        // Clear CodeArea and set editedFile to the selected file path (absolute)
        codeArea.clear();
        editedFile = file.getAbsolutePath();

        // Print information
        printOutput("Loaded file: " + editedFile);
        appendOutput("Loaded file: " + editedFile);

        // Replace the codeArea text with the new File's text
        codeArea.replaceText(fileIOService.loadFile(file));

        // Put saved contents into savedContent
        savedContent = codeArea.getText();
    }

    @FXML
    protected void runJavaFile() throws IOException, InterruptedException { // Triggers when user clicks 'Compile' under the MenuBar
        if (isDirty() || editedFile == null) { // Triggers if file is dirty or there is no saved file being edited
            saveFile();
            if (editedFile == null) {
                LocalTime currentTime = getTime();

                // Cancelled save
                printOutput("No file selected");
                appendOutput("No file selected");

                return;
            }
        }

        // Create a new file via the editedFile variable
        File javaFile = new  File(editedFile);

        // Create an output (StringBuilder) for the compiled code
        StringBuilder output =  compilerService.compileAndRun(javaFile, editedFile);

        // Print it to the running command (e.g. CMD)
        for (String s :  output.toString().split("\n")) { // For every String (line) in StringBuilder
            LocalTime currentTime = getTime();

            // Print out the time and line of the provided StringBuilder
            printOutput(s);
            appendOutput(s);

            // Scroll to bottom of output
            this.output.setScrollTop(Double.MAX_VALUE);
        }
    }

    @FXML
    protected void undo() { // Triggers when user clicks 'Undo' under the MenuBar
        codeArea.undo();
    }

    @FXML
    protected void redo() { // Triggers when user clicks 'Redo' under the MenuBar
        codeArea.redo();
    }

    @FXML
    protected void delete() { // Triggers when user clicks 'Delete' under the MenuBar
        codeArea.replaceSelection("");
    }

    @FXML
    protected void clear() { // Triggers when user clicks 'Clear' under the MenuBar
        codeArea.clear();
    }

    @FXML
    protected void copy() { // Triggers when user clicks 'Copy' under the MenuBar
        codeArea.copy();
    }

    @FXML
    protected void paste() { // Triggers when user clicks 'Paste' under the MenuBar
        codeArea.paste();
    }

    private LocalTime getTime() { // Get local time
        return LocalTime.now();
    }

    private void appendOutput(String content) { // Append output with template
        output.appendText(getTime() + " > " +  content + "\n");
    }

    private void printOutput(String content) { // Print output with template
        System.out.println(getTime() + " > " +  content); // (no need for newline because of ln)
    }

    public boolean isDirty() {
        return !codeArea.getText().equals(savedContent);
    }

    @FXML
    protected void debugDirty() {
        System.out.println(isDirty());
    }

}
