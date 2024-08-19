package Usuarios;

public class Supervisor extends Usuario {
    // Constructor que llama al constructor de la clase base
    public Supervisor(int id, String nombres, String apellidoMaterno, String apellidoPaterno, String fechaNacimiento, String pais, String ciudad, String correoElectronico, String lada, String telefono, String rfc, String curp, String profesion, int departamentoId, int jerarquiaId, int huellaId) {
        super(id, nombres, apellidoMaterno, apellidoPaterno, fechaNacimiento, pais, ciudad, correoElectronico, lada, telefono, rfc, curp, profesion, departamentoId, jerarquiaId, huellaId);
    }

    // Métodos específicos de la clase Supervisor
}

