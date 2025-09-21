package org.explement.jde.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.explement.jde.model.FileState;
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
import java.util.HashMap;
import java.util.Map;
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
    public TextArea output;
    @FXML private VBox mainVBox;
    @FXML private HBox navigationBar;

    // File being edited absolute path
    private String editedFile;
    // Compile WHITESPACE_Pattern from RegexUtils
    private static final Pattern whiteSpace = Pattern.compile(RegexUtils.WHITESPACE_PATTERN);
    // Enable highlight check
    private final boolean highlightChecker = true;
    // Undo manager for CodeArea
    private UndoManager<?> undoManager;
    // File cache (avoid loading each time)
    Map<String, FileState> fileCache = new HashMap<>();
    // Number counter for default name (unnamed_file.java)
    private int unnamedIndex = 0;
    // Current edited file's content
    private String currentContent;
    // Tracker for currentContent
    private boolean currentContentTracker = true;
    // All the navigation buttons
    private final Map<String, Button> navButtons = new HashMap<>();

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

        newJavaFile(); // Create before the listener

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            FileState fileState = fileCache.get(editedFile);
            if (fileState == null) {
                return;
            }

            if (currentContentTracker) {
                currentContent = newText;
                fileState.setContent(newText);
            }

            Button button = navButtons.get(editedFile);
            dirtyMarkerCheck(fileState, button);

            if (highlightChecker) {
                updateSyntaxHighlighting(newText);
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

    }

    @FXML
    protected void onNewJavaFile() {
        FileState fileState = fileCache.get(editedFile);
        if (fileState.isDirty() && !codeArea.getText().isEmpty()) {
            promptUserChoice promptSave = promptSaveFile();
            if (promptSave == promptUserChoice.SAVE) {
                saveFile();
            } else if (promptSave == promptUserChoice.CANCEL) {
                return;
            }
        }
        newJavaFile();
    }

    private void newJavaFile() {
        if (unnamedIndex == 0) {
            editedFile = "unnamed_file.java";
            unnamedIndex=1;
        } else {
            editedFile = "unnamed_file" + unnamedIndex + ".java";
            unnamedIndex++;
        }

        codeArea.clear();
        newNavBarButton(editedFile);

        fileCache.put(editedFile, new FileState("", ""));
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
            if (type == saveAndExit) { // Save and exit
                userChoice[0] = promptUserChoice.SAVE;
            } else if (type == cancel) { // Cancel
                userChoice[0] = promptUserChoice.CANCEL;
            }
            // Exit without saving (no exceptions)
        });

        return userChoice[0];
    }

    @FXML
    public void saveFile() {
        FileState fileState = fileCache.get(editedFile);
        if (!fileState.isDirty()) return;

        if (editedFile == null || !new File(editedFile).isAbsolute()) {
            saveFileAs();
            if (editedFile == null) return;
        }

        File file = new File(editedFile);
        fileIOService.saveFile(codeArea.getText(), file);

        String savedContent = codeArea.getText();
        undoManager.mark();

        fileCache.put(editedFile, new FileState(currentContent, savedContent));
        updateNavButtonMark(editedFile);

        printOutput("Saved file: " + file.getAbsolutePath());
        appendOutput("Saved file: " + file.getAbsolutePath());
    }

    @FXML
    protected void saveFileAs() {
        FileChooser fileChooser = FileChooserUtils.createSaveJavaFileChooser();
        File file = fileChooser.showSaveDialog(mainVBox.getScene().getWindow());
        if (file != null) {
            updateNavButtonPath(editedFile, file.getAbsolutePath()); // old path, and new path
            editedFile = file.getAbsolutePath();
            fileIOService.saveFile(codeArea.getText(), file);
            String savedContent = codeArea.getText();
            fileCache.put(editedFile, new FileState(currentContent, savedContent));
            updateNavButtonMark(editedFile);
            printOutput("File saved: " + editedFile);
            appendOutput("File saved: " + editedFile);
        }
    }

    @FXML
    protected void loadFile() {
        FileState fileState = fileCache.get(editedFile);
        if (fileState.isDirty()) {
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


        String savedContent = codeArea.getText();
        currentContent = savedContent;

        fileCache.put(editedFile, new FileState(currentContent, savedContent));

        updateSyntaxHighlighting(currentContent);
        newNavBarButton(editedFile);
    }

    @FXML
    protected void runJavaFile() throws IOException, InterruptedException {
        FileState fileState = fileCache.get(editedFile);
        if (fileState.isDirty() || editedFile == null) {
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

    @FXML
    protected void debugDirty() {
        FileState fileState = fileCache.get(editedFile);
        if (fileState == null) return;
        System.out.println(fileState.isDirty());
    }

    private void newNavBarButton(String path) {
        Button button = new Button();

        button.setText(new File(path).getName());

        // Store real path
        button.setUserData(path);

        button.setOnMouseClicked(event -> {
            loadFileFromCache((String) button.getUserData());
        });

        navigationBar.getChildren().add(button);
        navButtons.put(path, button);
    }

    public void updateNavButtonPath(String oldPath, String newPath) { // TODO: Finish navbar
        Button button = navButtons.remove(oldPath); // (old key)
        if (button == null) {
            printOutput("No navigation button selected");
            appendOutput("No navigation button selected");
            return;
        }
        button.setText(new File(newPath).getName());
        button.setUserData(newPath);
        navButtons.put(newPath, button);
    }

    private void updateNavButtonMark(String path) {
        FileState fileState = fileCache.get(path);
        Button button = navButtons.get(path);

        if (fileState == null) return;

        dirtyMarkerCheck(fileState, button);

    }

    private void dirtyMarkerCheck(FileState fileState, Button button) {
        if (button == null) return;

        if (fileState.isDirty()) {
            if (!button.getText().contains("(*)")) { // Dirty (*)
                button.setText(button.getText() + "(*)");
            }
        } else if (button.getText().contains("(*)")) {
            button.setText(new File((String) button.getUserData()).getName()); // Create a file and get its name
        }
    }

    private void updateSyntaxHighlighting(String newText) {
        codeArea.setStyleSpans(0, highlighterService.computeHighlighting(newText));
    }

    private void loadFileFromCache(String path) {
        FileState fileState = fileCache.get(path);

        currentContentTracker = false;
        codeArea.clear();
        editedFile = path;
        printOutput("Loaded cache: " + editedFile);
        appendOutput("Loaded cache: " + editedFile);
        codeArea.replaceText(fileState.getContent());
        currentContentTracker = true;
    }

    public boolean editedFileIsDirty() {
        FileState fileState = fileCache.get(editedFile);
        return fileState != null && fileState.isDirty();
    }
}
