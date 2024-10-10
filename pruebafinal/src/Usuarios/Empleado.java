package Usuarios;

public class Empleado extends Usuario {
    private String departamento;

    // Constructor
    public Empleado(int id, String nombres, String apellidoPaterno, String apellidoMaterno, String correoElectronico, String departamento) {
        super(id, nombres, apellidoPaterno, apellidoMaterno, correoElectronico);
        this.departamento = departamento;
    }

    // Getters y Setters
    public String getDepartamento() { return departamento; }

    // Restricciones
    public void accesoLimitado() {
        System.out.println("Acceso restringido a configuraciones de supervisión o liderazgo.");
    }
}



