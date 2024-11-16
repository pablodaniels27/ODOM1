package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GeneradorSemana {

    public static void generarSemana() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Genera los registros de los próximos 7 días en la tabla "dias"
            LocalDate fechaInicio = LocalDate.now();
            LocalDate fechaFin = fechaInicio.plusDays(6);  // Generar hasta 7 días desde hoy

            for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
                agregarDiaSiNoExiste(conn, fecha);
            }
            System.out.println("Días de la semana generados en la tabla 'dias'.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para verificar o insertar un día en la tabla "dias" si no existe
    private static void agregarDiaSiNoExiste(Connection conn, LocalDate fecha) throws SQLException {
        String diaId = fecha.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // Consulta para verificar si el día ya existe en la tabla "dias"
        String selectSql = "SELECT id FROM dias WHERE fecha = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setDate(1, java.sql.Date.valueOf(fecha));
            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                // Si no existe, inserta el día en la tabla "dias" con el ID formateado como YYMMDD
                String insertSql = "INSERT INTO dias (id, fecha) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, diaId);  // Establece el ID en formato YYMMDD
                    insertStmt.setDate(2, java.sql.Date.valueOf(fecha));
                    insertStmt.executeUpdate();
                    System.out.println("Día " + diaId + " agregado a la tabla 'dias'.");
                }
            }
        }
    }

    public static void main(String[] args) {
        generarSemana();
    }
}