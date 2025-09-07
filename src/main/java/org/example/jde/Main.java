package org.example.jde;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        Font.loadFont(Main.class.getResourceAsStream("JetBrainsMono-Regular.ttf"), 12);

        stage.setScene(scene);
        stage.show();
    }
}
