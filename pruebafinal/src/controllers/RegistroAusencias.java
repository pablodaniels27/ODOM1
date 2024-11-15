package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RegistroAusencias {

    public static void registrarAusencias() {
        LocalTime horaActual = LocalTime.now();
        LocalTime horaLimiteSalida = LocalTime.of(18, 0); // Hora de salida estándar: 6:00 p.m.

        // Verifica si la hora actual es después de las 6:00 p.m.
        if (horaActual.isAfter(horaLimiteSalida)) {
            try (Connection conn = DatabaseConnection.getConnection()) {

                // Asegura que existe una entrada para el día actual en la tabla "dias"
                String diaId = obtenerDiaId(conn);

                // SQL para registrar "No Asistencia" solo si el empleado no tiene registro de entrada en el día actual
                String sql = "INSERT INTO entradas_salidas (empleado_id, dia_id, tipo_asistencia_id) " +
                        "SELECT e.id, ?, 2 FROM empleados e " +  // 2 corresponde a "No Asistencia"
                        "WHERE NOT EXISTS ( " +
                        "    SELECT 1 FROM entradas_salidas es " +
                        "    WHERE es.empleado_id = e.id " +
                        "    AND es.dia_id = ? " +
                        ")";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, diaId); // Para el día actual
                    pstmt.setString(2, diaId); // Para verificar el día actual en la subconsulta
                    pstmt.executeUpdate();
                    System.out.println("Ausencias registradas correctamente para empleados sin entrada.");
                }

                // Actualizar el tipo de salida de los empleados en función de la hora de salida
                actualizarTipoSalida(conn, diaId);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No es la hora adecuada para registrar ausencias.");
        }
    }

    // Método para obtener el ID del día actual de la tabla "dias" o insertarlo si no existe
    private static String obtenerDiaId(Connection conn) throws SQLException {
        String diaId;
        LocalDate fechaActual = LocalDate.now();

        // Formatear la fecha actual como YYMMDD para el ID
        diaId = fechaActual.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // Primero intenta obtener el ID del día actual
        String selectSql = "SELECT id FROM dias WHERE fecha = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setDate(1, java.sql.Date.valueOf(fechaActual));
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                diaId = rs.getString("id");  // Obtener el ID formateado como YYMMDD
            } else {
                // Si no existe, inserta el día en la tabla "dias" con el ID formateado como YYMMDD
                String insertSql = "INSERT INTO dias (id, fecha) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, diaId);  // Establece el ID en formato YYMMDD
                    insertStmt.setDate(2, java.sql.Date.valueOf(fechaActual));
                    insertStmt.executeUpdate();
                }
            }
        }
        return diaId;
    }

    // Método para actualizar el tipo de salida según la hora de salida y asignar retardo si se registra después de las 9:15 a.m.
    private static void actualizarTipoSalida(Connection conn, String diaId) throws SQLException {
        LocalTime horaEntradaLimite = LocalTime.of(9, 15); // Hora límite para registrar "Retardo"
        LocalTime horaSalidaProgramada = LocalTime.of(18, 0); // Hora de salida programada

        // Consulta SQL para actualizar el tipo de asistencia a "Retardo" si la hora de entrada es después de las 9:15 a.m.
        String actualizarRetardoSql = "UPDATE entradas_salidas es " +
                "SET es.tipo_asistencia_id = 3 " + // 3 corresponde a "Retardo"
                "WHERE es.dia_id = ? AND es.hora_entrada > ? AND es.tipo_asistencia_id = 1"; // 1 es "Asistencia"

        try (PreparedStatement pstmt = conn.prepareStatement(actualizarRetardoSql)) {
            pstmt.setString(1, diaId);
            pstmt.setTime(2, java.sql.Time.valueOf(horaEntradaLimite));
            pstmt.executeUpdate();
            System.out.println("Registro de retardos actualizado correctamente.");
        }

        // Consulta SQL para actualizar el tipo de salida según la hora de salida
        String actualizarTipoSalidaSql = "UPDATE entradas_salidas es " +
                "SET es.tipo_salida_id = CASE " +
                "    WHEN es.hora_salida IS NULL THEN 1 " +        // 1 = "No registró salida"
                "    WHEN es.hora_salida < ? THEN 2 " +            // 2 = "Salió antes"
                "    WHEN es.hora_salida = ? THEN 4 " +            // 4 = "Salió a tiempo"
                "    WHEN es.hora_salida > ? THEN 3 " +            // 3 = "Salió después"
                "END " +
                "WHERE es.dia_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(actualizarTipoSalidaSql)) {
            pstmt.setTime(1, java.sql.Time.valueOf(horaSalidaProgramada));
            pstmt.setTime(2, java.sql.Time.valueOf(horaSalidaProgramada));
            pstmt.setTime(3, java.sql.Time.valueOf(horaSalidaProgramada));
            pstmt.setString(4, diaId);
            pstmt.executeUpdate();
            System.out.println("Tipo de salida actualizado correctamente para el día actual.");
        }
    }

    public static void main(String[] args)  {
        registrarAusencias();
    }
}
