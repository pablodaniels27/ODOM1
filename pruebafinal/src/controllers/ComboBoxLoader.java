package controllers;
import controllers.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComboBoxLoader {

    public static void loadDepartments(ComboBox<String> comboBox) {
        new Thread(() -> {
            ObservableList<String> departamentos = FXCollections.observableArrayList();
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT nombre FROM departamentos";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    departamentos.add(resultSet.getString("nombre"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                comboBox.setItems(departamentos);
            });
        }).start();
    }

    public static void loadPositions(ComboBox<String> comboBox) {
        new Thread(() -> {
            ObservableList<String> puestos = FXCollections.observableArrayList();
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT nombre FROM jerarquias";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    puestos.add(resultSet.getString("nombre"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                comboBox.setItems(puestos);
            });
        }).start();
    }
}
