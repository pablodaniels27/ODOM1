package Usuarios;

public class Lider extends Usuario {

    public Lider(int id, String nombre, String correo) {
        super(id, nombre, correo, "Lider");
    }

    @Override
    public boolean tienePermiso(String permiso) {
        return true;  // El líder tiene acceso a todos los permisos
    }
}
