module grafo.view {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.opencsv;
    requires gs.core;
    requires OpenXES;
    requires java.logging;

    opens grafo.view to javafx.fxml;
    exports grafo.view;
}