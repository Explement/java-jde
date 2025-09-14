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

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        Font.loadFont(Main.class.getResourceAsStream("JetBrainsMono-Regular.ttf"), 12);

        // Load controller via fxml
        MainController controller = fxmlLoader.getController();

        stage.setOnCloseRequest(event -> { // On app close
            if (controller.dirty) { // If there are unsaved changes
                // Make new alert with type CONFIRMATION
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                // Alert config
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes.");
                alert.setContentText("Do you want to save before exiting?");

                // Instantiate buttons
                ButtonType saveAndExit = new ButtonType("Save & Exit");
                ButtonType exitWithoutSaving = new ButtonType("Exit Without Saving");
                ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                // Add buttons to alert
                alert.getButtonTypes().setAll(saveAndExit, exitWithoutSaving, cancel);

                // Wait for results
                alert.showAndWait().ifPresent(type -> {
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
