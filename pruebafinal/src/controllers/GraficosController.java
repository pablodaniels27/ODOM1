package controllers;

import DAO.BaseDAO;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GraficosController {

    private MonitoreoController monitoreoController;

    // Método para inyectar el MonitoreoController
    public void setMonitoreoController(MonitoreoController monitoreoController) {
        this.monitoreoController = monitoreoController;
    }


    public BarChart<String, Number> createBarChart(Pane chartPane, String fechaInicio, String fechaFin, String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados) {
        // Limpiar el Pane del gráfico
        chartPane.getChildren().clear();

        // Crear los ejes del gráfico
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Departamento");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cantidad");

        // Crear el gráfico de barras
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        // Cambiar el título dinámicamente según el departamento seleccionado
        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            barChart.setTitle("Conteo de Tipos de Asistencia para: " + departamentoSeleccionado);
        } else {
            barChart.setTitle("Conteo de Tipos de Asistencia por Departamento");
        }

        // Cargar el archivo CSS usando ClassLoader
        try {
            String cssPath = getClass().getResource("/resources/style.css").toExternalForm();
            barChart.getStylesheets().add(cssPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Crear un mapa para almacenar las series por tipo de asistencia y por departamento
        Map<String, Map<String, Integer>> asistenciaPorDepartamento = new HashMap<>();

        // Lista de tipos de asistencia esperados
        List<String> tiposAsistenciaEsperados = Arrays.asList("Asistencia", "No Asistencia", "Retardo", "Justificación");

        try {
            // Obtener los datos desde el DAO
            List<Map<String, Object>> resultados = BaseDAO.obtenerConteoTiposAsistencia(fechaInicio, fechaFin, departamentoSeleccionado, searchQuery, incluirSupervisores, incluirEmpleados);

            // Procesar los resultados
            for (Map<String, Object> resultado : resultados) {
                String departamento = (String) resultado.get("departamento");
                String tipoAsistencia = (String) resultado.get("tipo_asistencia");
                int cantidad = (int) resultado.get("cantidad");

                asistenciaPorDepartamento.putIfAbsent(departamento, new HashMap<>());
                asistenciaPorDepartamento.get(departamento).put(tipoAsistencia, cantidad);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Crear series de datos para cada tipo de asistencia
        for (String tipoAsistencia : tiposAsistenciaEsperados) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(tipoAsistencia);

            // Agregar datos por departamento, rellenando con 0 si no hay datos
            for (String departamento : asistenciaPorDepartamento.keySet()) {
                int cantidad = asistenciaPorDepartamento.get(departamento).getOrDefault(tipoAsistencia, 0);
                series.getData().add(new XYChart.Data<>(departamento, cantidad));
            }

            // Agregar la serie al gráfico
            barChart.getData().add(series);
        }

        // Asegurarse de que los nodos de las barras ya se han creado antes de añadir labels y eventos
        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> series : barChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        // Crear el label con el valor de la cantidad
                        Label label = new Label(String.valueOf(data.getYValue()));
                        label.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");
                        StackPane stackPane = (StackPane) node;
                        stackPane.getChildren().add(label);
                        StackPane.setAlignment(label, Pos.CENTER);
                        label.setTranslateY(-30);  // Ajustar esto según el tamaño de la barra

                        // Aplicar estilo y comportamiento
                        node.setStyle("-fx-cursor: hand;");
                        node.toFront();
                        node.setPickOnBounds(true);
                        node.setMouseTransparent(false);

                        // Agregar el evento de clic
                        node.setOnMouseClicked(event -> {
                            String departamento = data.getXValue();
                            String tipoAsistencia = series.getName();
                            String nombreFilter = monitoreoController.getSearchFieldText();

                            System.out.println("Clic en la barra. Departamento: " + departamento + ", Tipo de asistencia: " + tipoAsistencia);
                            // Actualizar el tipo de asistencia seleccionado en el MonitoreoController
                            monitoreoController.setTipoAsistenciaSeleccionado(tipoAsistencia);
                            // Actualizar el Label en la interfaz con el tipo de asistencia
                            monitoreoController.updateAsistenciaLabel(tipoAsistencia);
                            // Limpiar las fechas al seleccionar una nueva columna de la gráfica
                            monitoreoController.clearDateTableView();
                            // Llamar al método en MonitoreoController para mostrar los nombres y fechas
                            monitoreoController.mostrarNombresPorAsistencia(departamento, tipoAsistencia, nombreFilter);
                        });
                    }
                }
            }
        });

        // Finalmente, añadir el gráfico al Pane
        chartPane.getChildren().add(barChart);

        return barChart;
    }




}
