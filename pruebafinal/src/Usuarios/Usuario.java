package Usuarios;

public class Usuario {
    private int id;
    private String nombres;
    private String correo;
    private String tipoUsuario;  // "Empleado", "Supervisor", "Lider"

    public Usuario(int id, String nombre, String correo, String tipoUsuario) {
        this.id = id;
        this.nombres = nombre;
        this.correo = correo;
        this.tipoUsuario = tipoUsuario;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombres;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    // Este método puede ser sobrescrito por las subclases si es necesario
    public boolean tienePermiso(String permiso) {
        return false; // Usuario base no tiene permisos especiales
    }
}
