package controllers;

import DAO.BaseDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class AuditoriaController {

    @FXML
    private TableColumn<Auditoria, String> nombreCompletoEmpleado;

    @FXML
    private TableColumn<Auditoria, String> nombreCompletoSupervisor;

    @FXML
    private TableColumn<Auditoria, String> departamentoColumn;

    @FXML
    private DatePicker fechaInicioPicker;

    @FXML
    private DatePicker fechaFinPicker;

    @FXML
    private TextField userFilterField;

    @FXML
    private ChoiceBox<String> actionFilterChoiceBox; // Cambiar ComboBox por ChoiceBox


    @FXML
    private TableView<Auditoria> auditoriaTable;

    @FXML
    private TableColumn<Auditoria, String> accionColumn;

    @FXML
    private TableColumn<Auditoria, String> timestampColumn;

    @FXML
    private TableColumn<Auditoria, String> detallesColumn;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button searchButton;

    @FXML
    private ChoiceBox<Integer> itemsPerPageChoiceBox;

    @FXML
    private HBox paginationBox;

    private ObservableList<Auditoria> auditoriaData = FXCollections.observableArrayList();



    private ObservableList<Auditoria> filteredData = FXCollections.observableArrayList();


    private int itemsPerPage = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    @FXML
    private TextField searchField;

    private ContextMenu suggestionsMenu = new ContextMenu();


    @FXML
    public void initialize() {
        // Configurar las opciones de itemsPerPageChoiceBox
        itemsPerPageChoiceBox.getItems().addAll(10, 20, 50, 100); // Añadir las opciones
        itemsPerPageChoiceBox.setValue(itemsPerPage); // Valor inicial

        // Agregar listener para actualizar itemsPerPage cuando el usuario cambie la selección
        itemsPerPageChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                itemsPerPage = newValue;
                totalPages = (int) Math.ceil((double) filteredData.size() / itemsPerPage);
                currentPage = 1; // Reiniciar a la primera página
                showPage(currentPage);
            }
        });

        // Configurar las columnas con las propiedades correctas
        nombreCompletoSupervisor.setCellValueFactory(new PropertyValueFactory<>("nombreSupervisor"));
        nombreCompletoEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreCompletoEmpleado"));
        departamentoColumn.setCellValueFactory(new PropertyValueFactory<>("departamentoNombre"));
        accionColumn.setCellValueFactory(new PropertyValueFactory<>("accion"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        detallesColumn.setCellValueFactory(new PropertyValueFactory<>("cambios")); // Ahora muestra la columna 'cambios'

        // Configurar la columna detalles con estilo de enlace y mostrar un popup
        detallesColumn.setCellFactory(tc -> new TableCell<Auditoria, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setOnMouseClicked(null);
                    setStyle("");
                } else {
                    setText("ver detalles");
                    setStyle("-fx-text-fill: blue; -fx-underline: true;");
                    setOnMouseClicked(event -> {
                        if (!isEmpty()) {
                            showDetailsPopup(item);  // Mostramos un popup con los detalles
                        }
                    });
                }
            }
        });

        restablecerFiltros();
    }

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
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage);
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            showPage(currentPage);
        }
    }

    @FXML
    private void validateAndSearch() {
        if (fechaInicioPicker.getValue() == null || fechaFinPicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Faltan fechas");
            alert.setContentText("Por favor, selecciona una fecha de inicio y una fecha final.");
            alert.showAndWait();
            return;
        }

        if (fechaInicioPicker.getValue().isAfter(fechaFinPicker.getValue())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Rango de fechas inválido");
            alert.setContentText("La fecha de inicio no puede ser posterior a la fecha final.");
            alert.showAndWait();
            return;
        }

        handleSearch();
    }

    @FXML
    private void handleSearch() {
        String supervisorFilter = searchField.getText().trim().toLowerCase();
        String actionFilter = actionFilterChoiceBox.getValue(); // ChoiceBox
        LocalDate fechaInicio = fechaInicioPicker.getValue();
        LocalDate fechaFin = fechaFinPicker.getValue();

        ObservableList<Auditoria> filteredList = FXCollections.observableArrayList();

        for (Auditoria auditoria : auditoriaData) {
            boolean matchesSupervisor = supervisorFilter.isEmpty() ||
                    auditoria.getNombreSupervisor().toLowerCase().contains(supervisorFilter);

            // Verifica si la acción coincide o si se ha seleccionado "Todos"
            boolean matchesAction = actionFilter == null || actionFilter.equals("Todos") ||
                    auditoria.getAccion().equalsIgnoreCase(actionFilter);

            boolean matchesDateRange = true;
            if (fechaInicio != null && fechaFin != null) {
                LocalDate auditDate = LocalDate.parse(auditoria.getTimestamp().substring(0, 10));
                matchesDateRange = !auditDate.isBefore(fechaInicio) && !auditDate.isAfter(fechaFin);
            }

            // Si todos los filtros coinciden, añadir a la lista filtrada
            if (matchesSupervisor && matchesAction && matchesDateRange) {
                filteredList.add(auditoria);
            }
        }

        // Actualizar la lista filtrada y la paginación
        filteredData = filteredList;
        totalPages = (int) Math.ceil((double) filteredData.size() / itemsPerPage);
        currentPage = 1;
        showPage(currentPage);
    }


    private void cargarDatos() {
        auditoriaData.clear();

        try {
            List<Auditoria> datos = BaseDAO.obtenerDatosAuditoria();  // Asegúrate de que estén en orden descendente por fecha
            auditoriaData.addAll(datos);  // Añadir los datos a la tabla
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private void showPage(int pageNumber) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageNumber > totalPages) {
            pageNumber = totalPages;
        }
        currentPage = pageNumber;

        int fromIndex = (pageNumber - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredData.size());

        if (fromIndex > filteredData.size()) {
            fromIndex = filteredData.size() - itemsPerPage;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        List<Auditoria> pageData = filteredData.subList(fromIndex, toIndex);
        auditoriaTable.setItems(FXCollections.observableArrayList(pageData));

        // Actualizar botones de paginación
        updatePaginationButtons();

        // Deshabilitar botones si es necesario
        previousButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages || totalPages == 0);
    }

    private void updatePaginationButtons() {
        paginationBox.getChildren().clear();
        int visiblePages = 5;

        int startPage = Math.max(2, currentPage - visiblePages / 2);
        int endPage = Math.min(startPage + visiblePages - 1, totalPages - 1);

        if (endPage - startPage + 1 < visiblePages) {
            startPage = Math.max(2, endPage - visiblePages + 1);
        }

        Button firstPageButton = new Button("1");
        firstPageButton.setOnAction(event -> showPage(1));
        if (currentPage == 1) {
            firstPageButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        } else {
            firstPageButton.setStyle("");
        }
        paginationBox.getChildren().add(firstPageButton);

        if (startPage > 2) {
            paginationBox.getChildren().add(new Label("..."));
        }

        for (int i = startPage; i <= endPage; i++) {
            final int pageIndex = i;
            Button pageButton = new Button(String.valueOf(i));
            pageButton.setOnAction(event -> showPage(pageIndex));
            if (i == currentPage) {
                pageButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
            } else {
                pageButton.setStyle("");
            }
            paginationBox.getChildren().add(pageButton);
        }

        if (endPage < totalPages - 1) {
            paginationBox.getChildren().add(new Label("..."));
        }

        if (totalPages > 1) {
            Button lastPageButton = new Button(String.valueOf(totalPages));
            lastPageButton.setOnAction(event -> showPage(totalPages));
            if (currentPage == totalPages) {
                lastPageButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
            } else {
                lastPageButton.setStyle("");
            }
            paginationBox.getChildren().add(lastPageButton);
        }
    }

    private void showDetailsPopup(String detalle) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles");
        alert.setHeaderText("Detalles de la Acción:");

        // Usamos un TextArea para mostrar el texto con saltos de línea
        TextArea textArea = new TextArea(detalle);
        textArea.setEditable(false);  // No permitimos edición
        textArea.setWrapText(true);   // Permitir que el texto se ajuste a la línea
        textArea.setPrefWidth(400);   // Ajusta el ancho según sea necesario
        textArea.setPrefHeight(200);  // Ajusta la altura según sea necesario

        // Añadir el TextArea al contenido del Alert
        alert.getDialogPane().setContent(textArea);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }




    @FXML
    private void copyToClipboard() {
        // Construir el contenido a copiar con encabezados de columnas
        StringBuilder clipboardContent = new StringBuilder();
        clipboardContent.append("Supervisor\tDepartamento\tAcción\tEmpleado Afectado\tHorario\tDetalles\n");

        // Recorrer todos los datos de la lista 'filteredData' para copiar al portapapeles
        for (Auditoria auditoria : filteredData) {
            String detalles = auditoria.getCambios().replace("\n", " ").replace("\r", " "); // Reemplazar saltos de línea con un espacio

            clipboardContent.append(auditoria.getNombreSupervisor()).append("\t")
                    .append(auditoria.getDepartamentoNombre()).append("\t")
                    .append(auditoria.getAccion()).append("\t")
                    .append(auditoria.getNombreCompletoEmpleado()).append("\t")
                    .append(auditoria.getTimestamp()).append("\t")
                    .append(detalles).append("\n");
        }

        // Copiar el contenido al portapapeles
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(clipboardContent.toString());
        clipboard.setContent(content);

        // Mostrar mensaje de confirmación
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Portapapeles");
        alert.setHeaderText(null);
        alert.setContentText("Los detalles han sido copiados al portapapeles.");
        alert.showAndWait();
    }


    @FXML
    private void restablecerFiltros() {
        // Restablecer los campos de texto y controles a sus valores iniciales
        searchField.clear();
        fechaInicioPicker.setValue(null);
        fechaFinPicker.setValue(null);
        actionFilterChoiceBox.getSelectionModel().clearSelection();
        actionFilterChoiceBox.getSelectionModel().selectFirst(); // Seleccionar "Todos" si está disponible

        // Volver a cargar los datos iniciales
        cargarDatos(); // Cargar todos los registros desde la base de datos
        filteredData.setAll(auditoriaData); // Copiar los datos cargados a la lista filtrada

        // Calcular total de páginas y mostrar la primera página
        totalPages = (int) Math.ceil((double) filteredData.size() / itemsPerPage);
        currentPage = 1;
        showPage(currentPage);
    }

}