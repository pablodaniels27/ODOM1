package Usuarios;
import java.util.Set;

public class Permisos {
    private Set<String> permisosAsignados;

    public Permisos(Set<String> permisosAsignados) {
        this.permisosAsignados = permisosAsignados;
    }

    // Verificar si el supervisor tiene un permiso en particular
    public boolean tienePermiso(String permiso) {
        return permisosAsignados.contains(permiso);
    }
}
