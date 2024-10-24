package Usuarios;

import DAO.UsuariosDAO;
import controllers.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class Supervisor extends Usuario {
    private int departamentoId;
    private String departamentoNombre;
    private Set<Permisos> permisos;  // Usar la clase Permisos en lugar de String
    private UsuariosDAO usuariosDAO;

    public Supervisor(int id, String nombre, String correo, int departamentoId, String departamentoNombre, Set<Permisos> permisos, UsuariosDAO usuariosDAO) {
        super(id, nombre, correo, "Supervisor");
        this.departamentoId = departamentoId;
        this.departamentoNombre = departamentoNombre;
        this.permisos = permisos;
        this.usuariosDAO = usuariosDAO;
    }

    public int getDepartamentoId() {
        return departamentoId;
    }

    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    public boolean tienePermiso(String permiso) {
        // Verificar si el supervisor tiene el permiso requerido utilizando la clase Permisos
        return permisos.stream().anyMatch(p -> p.getNombre().equals(permiso));
    }

    public Set<Permisos> getPermisos() {
        return permisos;
    }


}
