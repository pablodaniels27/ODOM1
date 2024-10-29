package DAO;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Usuarios.*;
import controllers.DatabaseConnection;
import javafx.scene.control.CheckBox;
import org.mindrot.jbcrypt.BCrypt;

public class UsuariosDAO {

    private Connection conexion;

    public UsuariosDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public Usuario autenticar(String correo, String contrasena) {
        Usuario usuario = null;

        String query = "SELECT e.id AS empleado_id, e.nombres AS empleado_nombres, e.correo_electronico, " +
                "u.contrasena_hash, j.nombre AS tipo_usuario, e.departamento_id, d.nombre AS departamento_nombre " +
                "FROM empleados e " +
                "JOIN usuarios u ON e.id = u.empleado_id " +
                "JOIN jerarquias j ON e.jerarquia_id = j.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "WHERE e.correo_electronico = ?";

        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setString(1, correo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("contrasena_hash");

                    if (BCrypt.checkpw(contrasena, storedHash)) {
                        int id = rs.getInt("empleado_id");
                        String nombre = rs.getString("empleado_nombres");
                        String tipoUsuario = rs.getString("tipo_usuario");
                        int departamentoId = rs.getInt("departamento_id");
                        String departamentoNombre = rs.getString("departamento_nombre");

                        switch (tipoUsuario) {
                            case "Empleado":
                                usuario = new Empleado(id, nombre, correo);
                                break;
                            case "Supervisor":
                                Set<Permisos> permisos = obtenerPermisos(id);
                                usuario = new Supervisor(id, nombre, correo, departamentoId, departamentoNombre, permisos, this);
                                break;
                            case "Líder":
                                // Obtener todos los permisos para el líder
                                Set<Permisos> permisosLider = Lider.obtenerTodosLosPermisos();
                                usuario = new Lider(id, nombre, correo);
                                break;
                        }

                        SessionManager.setCurrentUser(usuario);
                    } else {
                        System.out.println("Contraseña incorrecta.");
                    }
                } else {
                    System.out.println("Correo no encontrado.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuario;
    }

    // Nuevo método para obtener un supervisor junto con sus permisos
    public Supervisor obtenerSupervisorConPermisos(String nombreCompleto) throws SQLException {
        Supervisor supervisor = null;

        // Dividimos el nombre completo en posibles partes (nombres, apellido paterno, apellido materno)
        String[] nombrePartes = nombreCompleto.split(" ");
        String nombres = nombrePartes.length > 0 ? nombrePartes[0] : "";
        String apellidoPaterno = nombrePartes.length > 1 ? nombrePartes[1] : "";
        String apellidoMaterno = nombrePartes.length > 2 ? nombrePartes[2] : "";

        String query = "SELECT e.id, e.nombres, e.correo_electronico, e.departamento_id, d.nombre AS departamento_nombre " +
                "FROM empleados e " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "WHERE LOWER(e.nombres) LIKE LOWER(?) " +
                "AND LOWER(e.apellido_paterno) LIKE LOWER(?) " +
                "AND LOWER(e.apellido_materno) LIKE LOWER(?) " +
                "AND e.jerarquia_id = 2"; // Suponiendo que jerarquia_id=2 es para supervisores

        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setString(1, "%" + nombres + "%");
            stmt.setString(2, "%" + apellidoPaterno + "%");
            stmt.setString(3, "%" + apellidoMaterno + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String correo = rs.getString("correo_electronico");
                int departamentoId = rs.getInt("departamento_id");
                String departamentoNombre = rs.getString("departamento_nombre");

                // Obtener permisos del supervisor
                Set<Permisos> permisos = obtenerPermisos(id);
                supervisor = new Supervisor(id, nombreCompleto, correo, departamentoId, departamentoNombre, permisos, this);

                System.out.println("Supervisor encontrado en base de datos: " + supervisor.getNombre());
            } else {
                System.out.println("No se encontró el supervisor en la base de datos.");
            }
        }

        return supervisor;
    }

    // Método para obtener permisos de un supervisor específico (generalizable)
    public Set<Permisos> obtenerPermisos(int supervisorId) throws SQLException {
        Set<Permisos> permisos = new HashSet<>();

        String query = "SELECT p.nombre " +
                "FROM permisos p " +
                "JOIN usuarios_permisos up ON p.id = up.permiso_id " +
                "WHERE up.supervisor_id = ?";

        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setInt(1, supervisorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nombrePermiso = rs.getString("nombre");
                    permisos.add(new Permisos(nombrePermiso));
                    System.out.println("Permiso obtenido: " + nombrePermiso); // Línea de depuración
                }
            }
        }

        return permisos;
    }

    public Set<Permisos> obtenerTodosLosPermisos() throws SQLException {
        Set<Permisos> permisos = new HashSet<>();
        String query = "SELECT nombre FROM permisos";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombrePermiso = rs.getString("nombre");
                permisos.add(new Permisos(nombrePermiso));
            }
        }
        return permisos;
    }

    public void asignarPermiso(int supervisorId, String permisoNombre) throws SQLException {
        String query = "INSERT INTO usuarios_permisos (supervisor_id, permiso_id) " +
                "SELECT ?, p.id FROM permisos p WHERE p.nombre = ? " +
                "ON DUPLICATE KEY UPDATE supervisor_id = supervisor_id";

        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setInt(1, supervisorId);
            stmt.setString(2, permisoNombre);
            stmt.executeUpdate();
        }
    }

    public void eliminarPermiso(int supervisorId, String permisoNombre) throws SQLException {
        String query = "DELETE FROM usuarios_permisos " +
                "WHERE supervisor_id = ? AND permiso_id = (SELECT id FROM permisos WHERE nombre = ?)";

        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setInt(1, supervisorId);
            stmt.setString(2, permisoNombre);
            stmt.executeUpdate();
        }
    }

}
