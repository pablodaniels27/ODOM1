package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/empresaodom";
    private static final String USER = "root"; // Cambia a tu usuario de MySQL
    private static final String PASSWORD = ""; // Cambia a tu contraseña de MySQL

    // Constructor privado para ocultar el constructor público implícito
    private DatabaseConnection() {
        // Evita la instanciación de esta clase
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

