module org.example.jde {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;


    opens org.explement.jde to javafx.fxml;
    opens org.explement.jde.controller to javafx.fxml;
    exports org.explement.jde;
}