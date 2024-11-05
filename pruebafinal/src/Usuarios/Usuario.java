package Usuarios;

public class Usuario {
    private int id;
    private String nombres;
    private String apellidoPaterno;  // Nuevo campo
    private String apellidoMaterno;  // Nuevo campo
    private String correo;
    private String tipoUsuario;  // "Empleado", "Supervisor", "Lider"

    public Usuario(int id, String nombres, String apellidoPaterno, String apellidoMaterno, String correo, String tipoUsuario) {
        this.id = id;
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.correo = correo;
        this.tipoUsuario = tipoUsuario;
    }

    public int getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public boolean tienePermiso(String permiso) {
        return false; // Usuario base no tiene permisos especiales
    }
}
