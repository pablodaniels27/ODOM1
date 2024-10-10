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


    public void createBarChart(Pane chartPane, String fechaInicio, String fechaFin, String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados) {
        // Limpiar el Pane del gráfico
        chartPane.getChildren().clear();

        // Crear el gráfico de barras
        BarChart<String, Number> barChart = configurarEjesYGrafico(departamentoSeleccionado);

        // Aplicar el estilo CSS
        aplicarEstiloCSS(barChart);

        // Crear el mapa con los datos procesados
        Map<String, Map<String, Integer>> asistenciaPorDepartamento = obtenerDatosProcesados(fechaInicio, fechaFin, departamentoSeleccionado, searchQuery, incluirSupervisores, incluirEmpleados);

        // Crear las series de datos para el gráfico
        crearSeriesDeDatos(barChart, asistenciaPorDepartamento);

        // Añadir eventos y etiquetas a las barras
        agregarEventosYLabels(barChart);

        // Finalmente, añadir el gráfico al Pane
        chartPane.getChildren().add(barChart);
    }

    private BarChart<String, Number> configurarEjesYGrafico(String departamentoSeleccionado) {
        // Crear los ejes del gráfico
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Departamento");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cantidad");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        // Cambiar el título dinámicamente según el departamento seleccionado
        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            barChart.setTitle("Conteo de Tipos de Asistencia para: " + departamentoSeleccionado);
        } else {
            barChart.setTitle("Conteo de Tipos de Asistencia por Departamento");
        }

        return barChart;
    }

    private void aplicarEstiloCSS(BarChart<String, Number> barChart) {
        try {
            String cssPath = getClass().getResource("/resources/style.css").toExternalForm();
            barChart.getStylesheets().add(cssPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Map<String, Integer>> obtenerDatosProcesados(String fechaInicio, String fechaFin, String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados) {
        Map<String, Map<String, Integer>> asistenciaPorDepartamento = new HashMap<>();

        try {
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

        return asistenciaPorDepartamento;
    }

    private void crearSeriesDeDatos(BarChart<String, Number> barChart, Map<String, Map<String, Integer>> asistenciaPorDepartamento) {
        List<String> tiposAsistenciaEsperados = Arrays.asList("Asistencia", "No Asistencia", "Retardo", "Justificación");

        // Crear series de datos para cada tipo de asistencia
        for (String tipoAsistencia : tiposAsistenciaEsperados) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(tipoAsistencia);

            // Agregar datos por departamento, rellenando con 0 si no hay datos
            for (Map.Entry<String, Map<String, Integer>> entry : asistenciaPorDepartamento.entrySet()) {
                String departamento = entry.getKey();
                Map<String, Integer> asistencia = entry.getValue();
                int cantidad = asistencia.getOrDefault(tipoAsistencia, 0);
                series.getData().add(new XYChart.Data<>(departamento, cantidad));
            }


            // Agregar la serie al gráfico
            barChart.getData().add(series);
        }
    }

    private void agregarEventosYLabels(BarChart<String, Number> barChart) {
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
                            monitoreoController.setTipoAsistenciaSeleccionado(tipoAsistencia);
                            monitoreoController.updateAsistenciaLabel(tipoAsistencia);
                            monitoreoController.clearDateTableView();
                            monitoreoController.mostrarNombresPorAsistencia(departamento, tipoAsistencia, nombreFilter);
                        });
                    }
                }
            }
        });
    }





}
