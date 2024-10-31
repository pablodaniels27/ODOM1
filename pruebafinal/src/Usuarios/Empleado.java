package Usuarios;

public class Empleado extends Usuario {
    private String departamentoNombre;

    public Empleado(int id, String nombres, String apellidoPaterno, String apellidoMaterno, String correo, String departamentoNombre) {
        super(id, nombres, apellidoPaterno, apellidoMaterno, correo, "Empleado");
        this.departamentoNombre = departamentoNombre;
    }

    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    @Override
    public boolean tienePermiso(String permiso) {
        return false; // El empleado no tiene permisos adicionales
    }

    public String getNombreCompleto() {
        return this.getNombres() + " " + this.getApellidoPaterno() + " " + this.getApellidoMaterno();
    }
}
