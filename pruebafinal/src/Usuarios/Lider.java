package Usuarios;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import controllers.DatabaseConnection;

public class Lider extends Usuario {

    public Lider(int id, String nombre, String correo) {
        super(id, nombre, correo, "Lider");
    }

    @Override
    public boolean tienePermiso(String permiso) {
        return true;  // El líder tiene acceso a todos los permisos
    }

    // Obtener todos los permisos disponibles de la base de datos
    public static Set<Permisos> obtenerTodosLosPermisos() {
        Set<Permisos> permisos = new HashSet<>();
        String query = "SELECT nombre FROM permisos";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombrePermiso = rs.getString("nombre");
                Permisos permiso = new Permisos(nombrePermiso);
                permisos.add(permiso);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return permisos;
    }
}
