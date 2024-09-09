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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraficosController {

    public BarChart<String, Number> createBarChart(Pane chartPane, String fechaInicio, String fechaFin, String departamentoSeleccionado, boolean incluirSupervisores, boolean incluirEmpleados) {
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
            // Cambiar el método de obtención del recurso CSS
            String cssPath = getClass().getResource("/resources/style.css").toExternalForm();
            System.out.println("Ruta del archivo CSS: " + cssPath); // Verificación
            barChart.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Error cargando el archivo CSS: " + e.getMessage());
            e.printStackTrace();
        }

        // Crear un mapa para almacenar las series por tipo de asistencia y por departamento
        Map<String, Map<String, Integer>> asistenciaPorDepartamento = new HashMap<>();

        // Lista de tipos de asistencia esperados
        List<String> tiposAsistenciaEsperados = Arrays.asList("Asistencia", "No Asistencia", "Retardo", "Justificación");

        // Consulta SQL para obtener los datos desde la base de datos
        try {
            DatabaseConnection connectNow = new DatabaseConnection();
            Connection connectDB = connectNow.getConnection();

            String query = "SELECT d.nombre AS departamento, t.nombre AS tipo_asistencia, COUNT(*) AS cantidad " +
                    "FROM entradas_salidas en " +
                    "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                    "JOIN empleados e ON en.empleado_id = e.id " +
                    "JOIN departamentos d ON e.departamento_id = d.id " +
                    "JOIN dias di ON en.dia_id = di.id " +
                    "WHERE di.fecha BETWEEN '" + fechaInicio + "' AND '" + fechaFin + "' ";

            if (!departamentoSeleccionado.equals("Todos los departamentos")) {
                query += "AND d.nombre = '" + departamentoSeleccionado + "' ";
            }

            if (incluirSupervisores && !incluirEmpleados) {
                query += "AND e.jerarquia_id = 2 ";  // Solo supervisores
            } else if (incluirEmpleados && !incluirSupervisores) {
                query += "AND e.jerarquia_id = 3 ";  // Solo empleados
            }

            query += "GROUP BY d.nombre, t.nombre";

            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Procesar los resultados
            while (resultSet.next()) {
                String departamento = resultSet.getString("departamento");
                String tipoAsistencia = resultSet.getString("tipo_asistencia");
                int cantidad = resultSet.getInt("cantidad");

                // Inicializar el mapa para el departamento si no existe
                asistenciaPorDepartamento.putIfAbsent(departamento, new HashMap<>());

                // Almacenar la cantidad por tipo de asistencia
                asistenciaPorDepartamento.get(departamento).put(tipoAsistencia, cantidad);
            }

            connectDB.close();
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

        // Añadir el gráfico al Pane
        chartPane.getChildren().add(barChart);

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

        return barChart;
    }
}