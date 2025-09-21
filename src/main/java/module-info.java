module org.explement.jde {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires java.desktop;
    requires org.fxmisc.undo;
    requires javafx.graphics;


    opens org.explement.jde to javafx.fxml;
    opens org.explement.jde.controller to javafx.fxml;
    exports org.explement.jde;
}