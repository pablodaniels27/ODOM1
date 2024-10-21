package Usuarios;

public class Empleado extends Usuario {

    public Empleado(int id, String nombre, String correo) {
        super(id, nombre, correo, "Empleado");
    }

    @Override
    public boolean tienePermiso(String permiso) {
        return false; // El empleado no tiene permisos adicionales
    }
}
