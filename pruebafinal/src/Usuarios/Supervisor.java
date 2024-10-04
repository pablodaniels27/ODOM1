package Usuarios;

import controllers.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Supervisor extends Empleado {
    // Constructor
    public Supervisor(int id, String nombres, String apellidoPaterno, String apellidoMaterno, String correoElectronico, String departamento) {
        super(id, nombres, apellidoPaterno, apellidoMaterno, correoElectronico, departamento);
    }

    // Funcionalidades de un Supervisor
    public void gestionarEmpleados() {
        System.out.println("Supervisor puede gestionar empleados: aprobar asistencias, justificar faltas, etc.");
    }

    // Restricciones
    public void accesoRestringidoALider() {
        System.out.println("No puede realizar tareas de líder como asignar supervisores.");
    }


    // Método para obtener el ID del supervisor actual
    // Obtener el ID de un supervisor específico desde la base de datos
    public static int getCurrentSupervisorId() {
        // Hacer la consulta directamente para obtener un supervisor de la base de datos
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT id, nombres, apellido_paterno, apellido_materno FROM empleados WHERE nombres = ? AND apellido_paterno = ? AND apellido_materno = ? AND jerarquia_id = 2";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, "Pablo");
            statement.setString(2, "Moran");
            statement.setString(3, "López");

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int supervisorId = resultSet.getInt("id");
                String nombres = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String apellidoMaterno = resultSet.getString("apellido_materno");

                // Imprimir los datos del supervisor
                System.out.println("Supervisor encontrado:");
                System.out.println("ID: " + supervisorId);
                System.out.println("Nombres: " + nombres);
                System.out.println("Apellido Paterno: " + apellidoPaterno);
                System.out.println("Apellido Materno: " + apellidoMaterno);

                return supervisorId;
            } else {
                throw new IllegalStateException("No se encontró un supervisor con los datos proporcionados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener el ID del supervisor.", e);
        }
    }

}




