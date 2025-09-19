package org.explement.jde;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.explement.jde.controller.MainController;
import org.explement.jde.util.AlertUtils;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        Font.loadFont(Main.class.getResourceAsStream("JetBrainsMono-Regular.ttf"), 12);

        MainController controller = fxmlLoader.getController();

        stage.setOnCloseRequest(event -> {
            if (controller.isDirty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                String title = "Unsaved Changes";
                String header = "You have unsaved changes.";
                String content = "Do you want to save before exiting?";

                ButtonType saveAndExit = new ButtonType("Save & Exit");
                ButtonType exitWithoutSaving = new ButtonType("Exit Without Saving");
                ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                Optional<ButtonType> result = AlertUtils.createConfirmation(title, header, content, saveAndExit, exitWithoutSaving, cancel);

                result.ifPresent(type -> {
                    if (type == saveAndExit) { // Save and exit
                        controller.saveFile();
                    } else if (type == cancel) { // Cancel
                        event.consume();
                    }
                    // Exit without saving (no exceptions)
                });
            }
        });

        stage.setScene(scene);
        stage.show();
    }
}
