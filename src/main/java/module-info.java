module org.example.jde {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;


    opens org.example.jde to javafx.fxml;
    opens org.example.jde.controller to javafx.fxml;
    exports org.example.jde;
}