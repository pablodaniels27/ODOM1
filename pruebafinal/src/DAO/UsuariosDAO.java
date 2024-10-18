package DAO;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import Usuarios.*;
import org.mindrot.jbcrypt.BCrypt;

public class UsuariosDAO {

    private Connection conexion;

    public UsuariosDAO(Connection conexion) {
        this.conexion = conexion;
    }

    // Método para autenticar el usuario usando correo y contraseña
    public Usuario autenticar(String correo, String contrasena) {
        Usuario usuario = null;

        // Consulta para obtener el hash de la contraseña almacenada y los datos del empleado
        String query = "SELECT e.id AS empleado_id, e.nombres AS empleado_nombres, e.correo_electronico, " +
                "u.contrasena_hash, j.nombre AS tipo_usuario, e.departamento_id, d.nombre AS departamento_nombre " + // Agregar departamento_nombre
                "FROM empleados e " +
                "JOIN usuarios u ON e.id = u.empleado_id " +
                "JOIN jerarquias j ON e.jerarquia_id = j.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +  // Unir con la tabla de departamentos
                "WHERE e.correo_electronico = ?";

        try (PreparedStatement stmt = conexion.prepareStatement(query)) {
            stmt.setString(1, correo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("contrasena_hash");

                    // Verificar la contraseña usando BCrypt
                    if (BCrypt.checkpw(contrasena, storedHash)) {
                        int id = rs.getInt("empleado_id");
                        String nombre = rs.getString("empleado_nombres");
                        String tipoUsuario = rs.getString("tipo_usuario");
                        int departamentoId = rs.getInt("departamento_id");  // Obtener el departamento del supervisor
                        String departamentoNombre = rs.getString("departamento_nombre");  // Obtener el nombre del departamento

                        switch (tipoUsuario) {
                            case "Empleado":
                                usuario = new Empleado(id, nombre, correo);
                                break;
                            case "Supervisor":
                                usuario = new Supervisor(id, nombre, correo, departamentoId, departamentoNombre, this);  // Pasar el departamentoId y el departamentoNombre
                                break;
                            case "Líder":
                                usuario = new Lider(id, nombre, correo);
                                break;
                        }

                        // Establecer el usuario autenticado en el SessionManager
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




    // Cargar permisos asignados a un supervisor desde la tabla usuarios_permisos
    public Permisos cargarPermisos(int supervisorId) {
        Set<String> permisos = new HashSet<>();

        try {
            // Ajustamos la consulta para cargar permisos desde la tabla 'usuarios_permisos'
            String query = "SELECT p.nombre " +
                    "FROM permisos p " +
                    "JOIN usuarios_permisos up ON p.id = up.permiso_id " +
                    "WHERE up.supervisor_id = ?";
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setInt(1, supervisorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                permisos.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Permisos(permisos);
    }

    // Método para asignar un permiso a un supervisor
    public void asignarPermiso(int supervisorId, int permisoId) {
        try {
            // Consulta para insertar un permiso en la tabla 'usuarios_permisos'
            String query = "INSERT INTO usuarios_permisos (supervisor_id, permiso_id) VALUES (?, ?)";
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setInt(1, supervisorId);
            stmt.setInt(2, permisoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para eliminar un permiso de un supervisor
    public void eliminarPermiso(int supervisorId, int permisoId) {
        try {
            // Consulta para eliminar un permiso en la tabla 'usuarios_permisos'
            String query = "DELETE FROM usuarios_permisos WHERE supervisor_id = ? AND permiso_id = ?";
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setInt(1, supervisorId);
            stmt.setInt(2, permisoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
