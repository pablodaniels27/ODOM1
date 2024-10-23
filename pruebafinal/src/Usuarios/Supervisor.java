package Usuarios;

import DAO.UsuariosDAO;
import java.util.Set;

public class Supervisor extends Usuario {
    private int departamentoId;  // Almacena el ID del departamento del supervisor
    private String departamentoNombre;  // Almacena el nombre del departamento del supervisor
    private Set<String> permisos;  // Almacena los permisos asociados al supervisor


    // Constructor modificado para aceptar el departamentoId, departamentoNombre y permisos
    public Supervisor(int id, String nombre, String correo, int departamentoId, String departamentoNombre, Set<String> permisos, UsuariosDAO usuariosDAO) {
        super(id, nombre, correo, "Supervisor");
        this.departamentoId = departamentoId;  // Asignar el departamento ID
        this.departamentoNombre = departamentoNombre;  // Asignar el nombre del departamento
        this.permisos = permisos;  // Asignar los permisos

    }

    // Obtener el ID del departamento del supervisor
    public int getDepartamentoId() {
        return departamentoId;
    }

    // Obtener el nombre del departamento del supervisor
    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    public boolean tienePermiso(String permiso) {
        // Verificar si el supervisor tiene el permiso requerido
        return permisos.contains(permiso);
    }


}
