package Usuarios;

public class Usuario {
    private int id;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoElectronico;

    // Constructor
    public Usuario(int id, String nombres, String apellidoPaterno, String apellidoMaterno, String correoElectronico) {
        this.id = id;
        this.nombres = nombres;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.correoElectronico = correoElectronico;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public String getCorreoElectronico() { return correoElectronico; }
}
