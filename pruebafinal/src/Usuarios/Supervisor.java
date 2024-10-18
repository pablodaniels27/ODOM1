package Usuarios;

import DAO.UsuariosDAO;
import controllers.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Supervisor extends Usuario {
    private int departamentoId;  // Almacena el ID del departamento del supervisor
    private String departamentoNombre;  // Almacena el nombre del departamento del supervisor
    private UsuariosDAO usuariosDAO;

    // Constructor modificado para aceptar el departamentoId y el departamentoNombre
    public Supervisor(int id, String nombre, String correo, int departamentoId, String departamentoNombre, UsuariosDAO usuariosDAO) {
        super(id, nombre, correo, "Supervisor");
        this.departamentoId = departamentoId;  // Asignar el departamento
        this.departamentoNombre = departamentoNombre;  // Asignar el nombre del departamento
        this.usuariosDAO = usuariosDAO;
    }

    // Obtener el ID del departamento del supervisor
    public int getDepartamentoId() {
        return departamentoId;
    }

    // Obtener el nombre del departamento del supervisor
    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    @Override
    public boolean tienePermiso(String permiso) {
        // Carga los permisos desde la base de datos usando UsuariosDAO
        Permisos permisos = usuariosDAO.cargarPermisos(getId());
        return permisos.tienePermiso(permiso);
    }

    // Método para obtener el ID y nombre del departamento del supervisor autenticado
    public static Supervisor getSupervisorById(int supervisorId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, e.correo_electronico, d.id AS departamentoId, d.nombre AS departamentoNombre " +
                    "FROM empleados e " +
                    "JOIN departamentos d ON e.departamento_id = d.id " +
                    "WHERE e.id = ? AND e.jerarquia_id = 2";  // Solo supervisores
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, supervisorId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombres = resultSet.getString("nombres");
                String apellidoPaterno = resultSet.getString("apellido_paterno");
                String apellidoMaterno = resultSet.getString("apellido_materno");
                String correo = resultSet.getString("correo_electronico");
                int departamentoId = resultSet.getInt("departamentoId");
                String departamentoNombre = resultSet.getString("departamentoNombre");

                // Pasar la conexión a UsuariosDAO
                return new Supervisor(id, nombres, correo, departamentoId, departamentoNombre, new UsuariosDAO(connection));
            } else {
                throw new IllegalStateException("No se encontró un supervisor con el ID proporcionado.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener el supervisor.", e);
        }
    }
}
