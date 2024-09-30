package Usuarios;

import controllers.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Supervisor {
    private int id;
    private String nombre;
    private String apellido;

    // Constructor
    public Supervisor(int id, String nombre, String apellido) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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

    // Métodos específicos de la clase Supervisor


