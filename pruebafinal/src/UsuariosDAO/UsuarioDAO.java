package UsuariosDAO;

import Usuarios.Usuario;
import controllers.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public List<Usuario> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM empleados";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Usuario usuario = new Usuario(
                        resultSet.getInt("id"),
                        resultSet.getString("nombres"),
                        resultSet.getString("apellido_materno"),
                        resultSet.getString("apellido_paterno"),
                        resultSet.getString("fecha_nacimiento"),
                        resultSet.getString("pais"),
                        resultSet.getString("ciudad"),
                        resultSet.getString("correo_electronico"),
                        resultSet.getString("lada"),
                        resultSet.getString("telefono"),
                        resultSet.getString("rfc"),
                        resultSet.getString("curp"),
                        resultSet.getString("profesion"),
                        resultSet.getInt("departamento_id"),
                        resultSet.getInt("jerarquia_id"),
                        resultSet.getInt("estatus_id")
                );
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuarios;
    }
}
