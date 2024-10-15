package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;

public class PermisosController {

    @FXML
    private CheckBox checkBoxRegistro;
    @FXML
    private TitledPane titledPaneRegistro;

    @FXML
    private CheckBox checkBoxGestion;
    @FXML
    private TitledPane titledPaneGestion;

    @FXML
    private CheckBox checkBoxMonitoreo;
    @FXML
    private TitledPane titledPaneMonitoreo;

    @FXML
    public void initialize() {
        // Inicializar deshabilitados
        titledPaneRegistro.setDisable(true);
        titledPaneGestion.setDisable(true);
        titledPaneMonitoreo.setDisable(true);

        // Escucha para habilitar/deshabilitar y expandir los TitledPane según los CheckBox de activación
        checkBoxRegistro.selectedProperty().addListener((observable, oldValue, newValue) -> {
            titledPaneRegistro.setDisable(!newValue);
            if (newValue) {
                titledPaneRegistro.setExpanded(true); // Se abre automáticamente
            } else {
                titledPaneRegistro.setExpanded(false); // Se colapsa si se desmarca
            }
        });

        checkBoxGestion.selectedProperty().addListener((observable, oldValue, newValue) -> {
            titledPaneGestion.setDisable(!newValue);
            if (newValue) {
                titledPaneGestion.setExpanded(true); // Se abre automáticamente
            } else {
                titledPaneGestion.setExpanded(false); // Se colapsa si se desmarca
            }
        });

        checkBoxMonitoreo.selectedProperty().addListener((observable, oldValue, newValue) -> {
            titledPaneMonitoreo.setDisable(!newValue);
            if (newValue) {
                titledPaneMonitoreo.setExpanded(true); // Se abre automáticamente
            } else {
                titledPaneMonitoreo.setExpanded(false); // Se colapsa si se desmarca
            }
        });
    }
}
