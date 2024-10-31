package DAO;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Usuarios.*;
import controllers.AuthResult;
import controllers.DatabaseConnection;
import javafx.scene.control.CheckBox;
import org.mindrot.jbcrypt.BCrypt;

import static controllers.DatabaseConnection.getConnection;

public class UsuariosDAO {

    private Connection conexion;

    public UsuariosDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public AuthResult autenticar(String correo, String contrasena) {
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
                        String nombres = rs.getString("empleado_nombres");
                        String apellidoPaterno = rs.getString("apellido_paterno");
                        String apellidoMaterno = rs.getString("apellido_materno");
                        String tipoUsuario = rs.getString("tipo_usuario");
                        String correoElectronico = rs.getString("correo_electronico");
                        String departamentoNombre = rs.getString("departamento_nombre");

                        Usuario usuario = null;
                        switch (tipoUsuario) {
                            case "Empleado":
                                usuario = new Empleado(id, nombres, apellidoPaterno, apellidoMaterno, correoElectronico, departamentoNombre);
                                break;
                            case "Supervisor":
                                int departamentoId = rs.getInt("departamento_id");
                                Set<Permisos> permisos = obtenerPermisos(id);
                                usuario = new Supervisor(id, nombres, apellidoPaterno, apellidoMaterno, correoElectronico, departamentoId, departamentoNombre, permisos, this);
                                break;
                            case "Líder":
                                Set<Permisos> permisosLider = Lider.obtenerTodosLosPermisos();
                                usuario = new Lider(id, nombres, apellidoPaterno, apellidoMaterno, correoElectronico);
                                break;
                        }

                        SessionManager.setCurrentUser(usuario);
                        return new AuthResult(true, null, usuario);
                    } else {
                        return new AuthResult(false, "Contraseña incorrecta.", null);
                    }
                } else {
                    return new AuthResult(false, "Correo no encontrado.", null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new AuthResult(false, "Error de base de datos.", null);
        }
    }



    // Nuevo método para obtener un supervisor junto con sus permisos
    public Supervisor obtenerSupervisorConPermisos(String nombreCompleto) throws SQLException {
        Supervisor supervisor = null;

        // Dividimos el nombre completo en posibles partes (nombres, apellido paterno, apellido materno)
        String[] nombrePartes = nombreCompleto.split(" ");
        String nombres = nombrePartes.length > 0 ? nombrePartes[0] : "";
        String apellidoPaterno = nombrePartes.length > 1 ? nombrePartes[1] : "";
        String apellidoMaterno = nombrePartes.length > 2 ? nombrePartes[2] : "";

        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, e.correo_electronico, e.departamento_id, d.nombre AS departamento_nombre " +
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
                String nombre = rs.getString("nombres");
                String apellidoPaternoDB = rs.getString("apellido_paterno");
                String apellidoMaternoDB = rs.getString("apellido_materno");
                String correo = rs.getString("correo_electronico");
                int departamentoId = rs.getInt("departamento_id");
                String departamentoNombre = rs.getString("departamento_nombre");

                // Obtener permisos del supervisor
                Set<Permisos> permisos = obtenerPermisos(id);
                supervisor = new Supervisor(id, nombre, apellidoPaternoDB, apellidoMaternoDB, correo, departamentoId, departamentoNombre, permisos, this);

                System.out.println("Supervisor encontrado en base de datos: " + supervisor.getNombres() + " " + supervisor.getApellidoPaterno() + " " + supervisor.getApellidoMaterno());
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

    public static boolean verificarUsuario(String username) {
        String query = "SELECT COUNT(*) FROM usuarios WHERE nombre_usuario = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Retorna true si el usuario existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Retorna false si el usuario no existe o en caso de error
    }

    // Método para obtener la contraseña hasheada de un usuario por su correo electrónico
    public String obtenerContrasenaHashPorCorreo(String correo) {
        String query = "SELECT u.contrasena_hash FROM usuarios u " +
                "JOIN empleados e ON u.empleado_id = e.id " +
                "WHERE e.correo_electronico = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("contrasena_hash");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retorna null si no se encuentra la contraseña
    }

    // Método para obtener un objeto Usuario por su correo electrónico
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, e.correo_electronico, j.nombre AS tipo_usuario, " +
                "e.departamento_id, d.nombre AS departamento_nombre " +
                "FROM empleados e " +
                "JOIN jerarquias j ON e.jerarquia_id = j.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "WHERE e.correo_electronico = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String nombres = rs.getString("nombres");
                String apellidoPaterno = rs.getString("apellido_paterno");
                String apellidoMaterno = rs.getString("apellido_materno");
                String correoDB = rs.getString("correo_electronico");
                String tipoUsuario = rs.getString("tipo_usuario");
                String departamentoNombre = rs.getString("departamento_nombre");

                switch (tipoUsuario) {
                    case "Empleado":
                        return new Empleado(id, nombres, apellidoPaterno, apellidoMaterno, correoDB, departamentoNombre);
                    case "Supervisor":
                        int departamentoId = rs.getInt("departamento_id");
                        Set<Permisos> permisos = obtenerPermisos(id);
                        return new Supervisor(id, nombres, apellidoPaterno, apellidoMaterno, correoDB, departamentoId, departamentoNombre, permisos, this);
                    case "Líder":
                        return new Lider(id, nombres, apellidoPaterno, apellidoMaterno, correoDB);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retorna null si no se encuentra el usuario
    }



}
