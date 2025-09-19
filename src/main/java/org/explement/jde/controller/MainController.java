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
import org.explement.jde.util.AlertUtils;
import org.explement.jde.util.FileChooserUtils;
import org.explement.jde.util.RegexUtils;
import org.explement.jde.util.TimeUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.undo.UndoManager;

import java.io.*;
import java.util.Optional;
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
    @FXML private VBox mainVBox;

    // File being edited absolute path
    private String editedFile;
    // Compile WHITESPACE_Pattern from RegexUtils
    private static final Pattern whiteSpace = Pattern.compile(RegexUtils.WHITESPACE_PATTERN);
    // Enable highlight check
    private boolean highlightChecker = true;
    // Undo manager for CodeArea
    private UndoManager<?> undoManager;
    // Last saved content
    private String savedContent;

    @FXML
    private void initialize() {
        codeArea = new CodeArea();
        output = new TextArea();

        undoManager = codeArea.getUndoManager();
        output.setEditable(false);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem clearItem = new MenuItem("Clear");
        clearItem.setOnAction(e -> output.clear());

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> output.copy());

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> output.selectAll());

        contextMenu.getItems().addAll(clearItem, copyItem, separatorMenuItem, selectAllItem);
        output.setContextMenu(contextMenu);

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setId("codeArea");

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);

        mainVBox.getChildren().addAll(scrollPane, output);

        mainVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            double height = newVal.doubleValue();
            scrollPane.setPrefHeight(height * 0.7);
            output.setPrefHeight(height * 0.3);
        });

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            if (highlightChecker) {
                codeArea.setStyleSpans(0, highlighterService.computeHighlighting(newText));
            }
        });

        codeArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(codeArea.getParagraph(currentParagraph - 1).getSegments().get(0));
                if (m0.find()) {
                    Platform.runLater(() -> codeArea.insertText(caretPosition, m0.group()));
                }
            }
        });

        newJavaFile();
    }

    @FXML
    protected void onNewJavaFile() {
        if (isDirty() && !codeArea.getText().isEmpty()) {
            promptUserChoice promptSave = promptSaveFile();
            if (promptSave == promptUserChoice.SAVE) {
                saveFile();
            } else if (promptSave == promptUserChoice.CANCEL) {
                return;
            }
            newJavaFile();
        }
    }

    private void newJavaFile() {
        editedFile = "unnamed_file.java";
        codeArea.clear();
        savedContent = codeArea.getText();
    }

    private promptUserChoice promptSaveFile() {
        String title = "Unsaved Changes";
        String header = "You have unsaved changes.";
        String content = "Would you like to save changes?";

        ButtonType saveAndExit = new ButtonType("Save & Exit");
        ButtonType exitWithoutSaving = new ButtonType("Exit Without Saving");
        ButtonType cancel = new ButtonType("Cancel");

        Optional<ButtonType> result = AlertUtils.createConfirmation(title, header, content, saveAndExit, exitWithoutSaving, cancel);

        promptUserChoice[] userChoice = new promptUserChoice[1];
        userChoice[0] = promptUserChoice.DONT_SAVE;

        result.ifPresent(type -> {
            if (type == saveAndExit) {
                userChoice[0] = promptUserChoice.SAVE;
            } else if (type == cancel) {
                userChoice[0] = promptUserChoice.CANCEL;
            }
        });

        return userChoice[0];
    }

    @FXML
    public void saveFile() {
        if (!isDirty()) return;

        if (editedFile == null || !new File(editedFile).isAbsolute()) {
            saveFileAs();
            if (editedFile == null) return;
        }

        File file = new File(editedFile);
        fileIOService.saveFile(codeArea.getText(), file);

        savedContent = codeArea.getText();
        undoManager.mark();

        printOutput("Saved file: " + file.getAbsolutePath());
        appendOutput("Saved file: " + file.getAbsolutePath());
    }

    @FXML
    protected void saveFileAs() {
        FileChooser fileChooser = FileChooserUtils.createSaveJavaFileChooser();
        File file = fileChooser.showSaveDialog(mainVBox.getScene().getWindow());
        if (file != null) {
            editedFile = file.getAbsolutePath();
            fileIOService.saveFile(codeArea.getText(), file);
            savedContent = codeArea.getText();
            printOutput("File saved: " + editedFile);
            appendOutput("File saved: " + editedFile);
        }
    }

    @FXML
    protected void loadFile() {
        if (isDirty()) {
            promptUserChoice userChoice = promptSaveFile();
            if (userChoice == promptUserChoice.SAVE) saveFile();
            else if (userChoice == promptUserChoice.CANCEL) return;
        }

        FileChooser fileChooser = FileChooserUtils.createLoadJavaFileChooser();
        File file = fileChooser.showOpenDialog(mainVBox.getScene().getWindow());
        if (file == null) {
            printOutput("No file selected");
            appendOutput("No file selected");
            return;
        }

        codeArea.clear();
        editedFile = file.getAbsolutePath();
        printOutput("Loaded file: " + editedFile);
        appendOutput("Loaded file: " + editedFile);
        codeArea.replaceText(fileIOService.loadFile(file));
        savedContent = codeArea.getText();
    }

    @FXML
    protected void runJavaFile() throws IOException, InterruptedException {
        if (isDirty() || editedFile == null) {
            saveFile();
            if (editedFile == null) {
                printOutput("No file selected");
                appendOutput("No file selected");
                return;
            }
        }

        File javaFile = new File(editedFile);
        StringBuilder output = compilerService.compileAndRun(javaFile, editedFile);

        for (String s : output.toString().split("\n")) {
            printOutput(s);
            appendOutput(s);
            this.output.setScrollTop(Double.MAX_VALUE); // Scroll to bottom
        }
    }

    @FXML
    protected void undo() { codeArea.undo(); }

    @FXML
    protected void redo() { codeArea.redo(); }

    @FXML
    protected void delete() { codeArea.replaceSelection(""); }

    @FXML
    protected void clear() { codeArea.clear(); }

    @FXML
    protected void copy() { codeArea.copy(); }

    @FXML
    protected void paste() { codeArea.paste(); }

    private void appendOutput(String content) {
        output.appendText(TimeUtils.now() + " > " + content + "\n");
    }

    private void printOutput(String content) {
        System.out.println(TimeUtils.now() + " > " + content);
    }

    public boolean isDirty() { return !codeArea.getText().equals(savedContent); }

    @FXML
    protected void debugDirty() { System.out.println(isDirty()); }
}
