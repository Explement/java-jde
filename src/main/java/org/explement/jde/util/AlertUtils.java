package org.explement.jde.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertUtils {

    private AlertUtils(){} // Create private constructor

    public static Optional<ButtonType> createConfirmation(String title, String header, String content, ButtonType... buttons) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (buttons != null && buttons.length > 0) { // Make sure buttons are not null and exist
            alert.getButtonTypes().setAll(buttons);
        }

        return alert.showAndWait();
    }
}
