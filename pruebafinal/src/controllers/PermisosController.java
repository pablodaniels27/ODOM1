package controllers;

import DAO.BaseDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;

public class PermisosController {

    @FXML
    private Accordion accordion;

    @FXML
    private TitledPane titledPaneRegistro, titledPaneGestion, titledPaneMonitoreo;

    @FXML
    private CheckBox checkBoxRegistro, checkBoxGestion, checkBoxMonitoreo;


    @FXML
    private CheckBox registroPermiso1;
    @FXML
    private CheckBox gestionPermiso1, gestionPermiso2, gestionPermiso3, gestionPermiso4;
    @FXML
    private CheckBox monitoreoPermiso1, monitoreoPermiso2;



    @FXML
    public void initialize() {
        // Inicializamos los permisos deshabilitados
        toggleRegistro();
        toggleGestion();
        toggleMonitoreo();

    }
    @FXML
    private TextField searchField;

    private ContextMenu suggestionsMenu = new ContextMenu();

    @FXML
    private void searchForSupervisors(KeyEvent event) {
        String searchQuery = searchField.getText().trim();

        // Si el texto tiene menos de 3 caracteres, no buscar
        if (searchQuery.length() < 3) {
            suggestionsMenu.hide();
            return;
        }

        try {
            // Obtener la lista de supervisores desde el DAO
            ObservableList<String> results = BaseDAO.buscarSupervisores(searchQuery);

            if (!results.isEmpty()) {
                populateSuggestions(results);
            } else {
                suggestionsMenu.hide();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateSuggestions(ObservableList<String> suggestions) {
        suggestionsMenu.getItems().clear();

        VBox suggestionBox = new VBox();
        suggestionBox.setFillWidth(true);
        suggestionBox.setStyle("-fx-background-color: white;");

        for (String suggestion : suggestions) {
            Label item = new Label(suggestion);
            item.setStyle("-fx-padding: 5px; -fx-background-color: white;");
            item.setOnMouseEntered(event -> item.setStyle("-fx-padding: 5px; -fx-background-color: #0078d7; -fx-text-fill: white;"));
            item.setOnMouseExited(event -> item.setStyle("-fx-padding: 5px; -fx-background-color: white; -fx-text-fill: black;"));

            item.setOnMouseClicked(event -> {
                searchField.setText(suggestion);
                suggestionsMenu.hide();
            });

            suggestionBox.getChildren().add(item);
        }

        ScrollPane scrollPane = new ScrollPane(suggestionBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        CustomMenuItem scrollableMenuItem = new CustomMenuItem(scrollPane, false);

        suggestionsMenu.getItems().add(scrollableMenuItem);

        if (!suggestionsMenu.isShowing()) {
            Bounds boundsInScreen = searchField.localToScreen(searchField.getBoundsInLocal());
            suggestionsMenu.show(searchField, boundsInScreen.getMinX(), boundsInScreen.getMaxY());
        }
    }


    @FXML
    public void preventToggle(javafx.scene.input.MouseEvent event) {
        // Consumimos el evento para evitar que el TitledPane se expanda o colapse cuando se haga clic
        event.consume();
    }

    // Método para alternar el estado de los TitledPane cuando el CheckBox cambia
    @FXML
    public void toggleTitledPane() {
        // Expandir o colapsar los TitledPane según el estado de los CheckBox
        titledPaneRegistro.setExpanded(checkBoxRegistro.isSelected());
        titledPaneGestion.setExpanded(checkBoxGestion.isSelected());
        titledPaneMonitoreo.setExpanded(checkBoxMonitoreo.isSelected());
    }
    @FXML
    public void toggleRegistro() {
        boolean enabled = checkBoxRegistro.isSelected();
        registroPermiso1.setDisable(!enabled);

        // Si deshabilitamos, también desmarcamos los CheckBox
        if (!enabled) {
            registroPermiso1.setSelected(false);
        }
    }

    @FXML
    public void toggleGestion() {
        boolean enabled = checkBoxGestion.isSelected();
        gestionPermiso1.setDisable(!enabled);
        gestionPermiso2.setDisable(!enabled);
        gestionPermiso3.setDisable(!enabled);
        gestionPermiso4.setDisable(!enabled);

        // Si deshabilitamos, también desmarcamos los CheckBox
        if (!enabled) {
            gestionPermiso1.setSelected(false);
            gestionPermiso2.setSelected(false);
            gestionPermiso3.setSelected(false);
            gestionPermiso4.setSelected(false);
        }
    }

    @FXML
    public void toggleMonitoreo() {
        boolean enabled = checkBoxMonitoreo.isSelected();
        monitoreoPermiso1.setDisable(!enabled);
        monitoreoPermiso2.setDisable(!enabled);

        // Si deshabilitamos, también desmarcamos los CheckBox
        if (!enabled) {
            monitoreoPermiso1.setSelected(false);
            monitoreoPermiso2.setSelected(false);
        }
    }

    @FXML
    public void handleSearch() {
        // Lógica para manejar la búsqueda
    }

    @FXML
    public void applyChanges() {
        // Lógica para aplicar los cambios en los permisos
    }

}