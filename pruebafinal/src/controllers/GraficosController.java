package controllers;

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
        barChart.setTitle("Conteo de Tipos de Asistencia por Departamento");

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

        // Consulta SQL para obtener los datos desde la base de datos
        String query = "SELECT d.nombre AS departamento, t.nombre AS tipo_asistencia, COUNT(*) AS cantidad " +
                "FROM entradas_salidas en " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "JOIN dias di ON en.dia_id = di.id " +
                "WHERE di.fecha BETWEEN ? AND ? ";

        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            query += "AND d.nombre = ? ";
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            query += "AND (e.nombres LIKE ? OR e.apellido_paterno LIKE ? OR e.apellido_materno LIKE ?) ";
        }

        if (incluirSupervisores && !incluirEmpleados) {
            query += "AND e.jerarquia_id = 2 ";  // Solo supervisores
        } else if (incluirEmpleados && !incluirSupervisores) {
            query += "AND e.jerarquia_id = 3 ";  // Solo empleados
        }

        query += "GROUP BY d.nombre, t.nombre";

        try (Connection connectDB = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {

            // Asignar parámetros
            preparedStatement.setString(1, fechaInicio);
            preparedStatement.setString(2, fechaFin);

            int paramIndex = 3;
            if (!departamentoSeleccionado.equals("Todos los departamentos")) {
                preparedStatement.setString(paramIndex++, departamentoSeleccionado);
            }

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String searchPattern = "%" + searchQuery.trim() + "%";
                preparedStatement.setString(paramIndex++, searchPattern);
                preparedStatement.setString(paramIndex++, searchPattern);
                preparedStatement.setString(paramIndex++, searchPattern);
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            // Procesar los resultados
            while (resultSet.next()) {
                String departamento = resultSet.getString("departamento");
                String tipoAsistencia = resultSet.getString("tipo_asistencia");
                int cantidad = resultSet.getInt("cantidad");

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

        // Usar Platform.runLater para asegurarnos de que el gráfico se haya renderizado antes de agregar los labels
        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> series : barChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        // Crear el label con el valor de la cantidad
                        Label label = new Label(String.valueOf(data.getYValue()));

                        // Ajustar el estilo del label para hacerlo visible
                        label.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");

                        // Añadir el label a la barra (StackPane)
                        StackPane stackPane = (StackPane) node;
                        stackPane.getChildren().add(label);

                        // Alinear el label en el centro de la barra
                        StackPane.setAlignment(label, Pos.CENTER);

                        // Ajustar la posición del label más arriba dentro de la barra
                        label.setTranslateY(-30);  // Ajustar esto según el tamaño de la barra
                    }
                }
            }
        });

        // Asegurarse de que los nodos de las barras ya se han creado antes de añadir labels y eventos
        // Asegurarse de que los nodos de las barras ya se han creado antes de añadir labels y eventos
        Platform.runLater(() -> {
            System.out.println("Verificando si los nodos de las barras se están creando...");
            for (XYChart.Series<String, Number> series : barChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        System.out.println("Nodo creado para el departamento: " + data.getXValue() + " - Tipo de asistencia: " + series.getName());

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

                            // Llamar al método en MonitoreoController para mostrar los nombres
                            monitoreoController.mostrarNombresPorAsistencia(departamento, tipoAsistencia, nombreFilter);
                        });

                    } else {
                        System.out.println("El nodo no está creado aún para el departamento: " + data.getXValue());
                    }
                }
            }
        });


        System.out.println("chartPane bounds: " + chartPane.getBoundsInParent());
        System.out.println("BarChart bounds: " + barChart.getBoundsInParent());


        // Finalmente, añadir el gráfico al Pane después de todas las manipulaciones
        chartPane.getChildren().add(barChart);

        return barChart;
    }

}
