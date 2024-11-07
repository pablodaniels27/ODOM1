package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;

public class RegistroAusencias {

    public static void registrarAusencias() {
        LocalTime horaActual = LocalTime.now();

        // Verifica si la hora actual es después de las 6pm
        if (horaActual.isAfter(LocalTime.of(18, 0))) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO entradas_salidas (empleado_id, dia_id, tipo_asistencia_id) " +
                        "SELECT empleado_id, CURDATE(), 2 FROM empleados " +
                        "WHERE NOT EXISTS (" +
                        "    SELECT 1 FROM entradas_salidas " +
                        "    WHERE empleados.empleado_id = entradas_salidas.empleado_id " +
                        "    AND DATE(entradas_salidas.dia_id) = CURDATE()" +
                        ")";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.executeUpdate();
                    System.out.println("Ausencias registradas correctamente para empleados sin salida.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No es la hora adecuada para registrar ausencias.");
        }
    }

    public static void main(String[] args) {
        registrarAusencias();
    }
}
