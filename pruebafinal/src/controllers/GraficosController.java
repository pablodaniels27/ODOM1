package controllers;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class GraficosController {

    public BarChart<String, Number> createBarChart(Pane chartPane, String fechaInicio, String fechaFin) {
        // Limpiar el Pane del gráfico
        chartPane.getChildren().clear();

        // Crear los ejes del gráfico
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Departamento");

        // Ajustar el rango del eje Y y establecerlo como logarítmico
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cantidad");

        // Establecer el formateador para que tenga un comportamiento logarítmico sin repetición de valores
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                double value = object.doubleValue();
                if (value == 0) {
                    return "0";
                } else {
                    return String.format("%.0f", Math.pow(10, value));
                }
            }
        });

        // Ajuste de la escala logarítmica para evitar la repetición del valor "10"
        yAxis.setTickUnit(1); // Este valor asegura que solo se muestren 10, 100, 1000, etc.

        // Crear el gráfico de barras
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Conteo de Tipos de Asistencia por Departamento");
        barChart.setPrefWidth(600);  // Ajustar el ancho si es necesario
        barChart.setPrefHeight(700); // Ajustar la altura si es necesario

        // Crear un mapa para almacenar las series por tipo de asistencia
        Map<String, XYChart.Series<String, Number>> asistenciaSeriesMap = new HashMap<>();

        try {
            DatabaseConnection connectNow = new DatabaseConnection();
            Connection connectDB = connectNow.getConnection();

            // Modificar la consulta para obtener datos agrupados por departamento y tipo de asistencia
            String query = "SELECT d.nombre AS departamento, t.nombre AS tipo_asistencia, COUNT(*) AS cantidad " +
                    "FROM entradas_salidas en " +
                    "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                    "JOIN empleados e ON en.empleado_id = e.id " +
                    "JOIN departamentos d ON e.departamento_id = d.id " +
                    "JOIN dias di ON en.dia_id = di.id " +
                    "WHERE di.fecha BETWEEN '" + fechaInicio + "' AND '" + fechaFin + "' " +
                    "GROUP BY d.nombre, t.nombre";

            Statement statement = connectDB.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Recorrer los resultados y llenar las series correspondientes
            while (resultSet.next()) {
                String departamento = resultSet.getString("departamento");
                String tipoAsistencia = resultSet.getString("tipo_asistencia");
                int cantidad = resultSet.getInt("cantidad");

                // Crear o actualizar la serie para el tipo de asistencia actual
                XYChart.Series<String, Number> series = asistenciaSeriesMap.computeIfAbsent(tipoAsistencia, k -> {
                    XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                    newSeries.setName(k);  // El nombre de la serie será el tipo de asistencia
                    barChart.getData().add(newSeries);
                    return newSeries;
                });

                // Calcular el valor en el eje Y usando escala logarítmica
                double yValue;
                if (cantidad == 0) {
                    yValue = 0;
                } else {
                    yValue = Math.log10(cantidad);
                }

                XYChart.Data<String, Number> data = new XYChart.Data<>(departamento, yValue);
                series.getData().add(data);

                // Asegurarse de que el nodo esté disponible antes de trabajar con él
                data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        // Añadir la etiqueta dentro de la barra
                        Tooltip.install(newValue, new Tooltip(String.valueOf(cantidad)));
                        Label label = new Label(String.valueOf(cantidad));
                        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                        StackPane stackPane = (StackPane) newValue;
                        stackPane.getChildren().add(label);

                        // Añadir evento de clic en la barra
                        newValue.setOnMouseClicked(event -> {
                            // Lógica para mostrar detalles al hacer clic en la barra
                        });
                    }
                });
            }

            connectDB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Añadir el gráfico al Pane y retornarlo
        chartPane.getChildren().add(barChart);
        return barChart;
    }
}
