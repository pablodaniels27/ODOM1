package Usuarios;

public class Empleado extends Usuario {

    public Empleado(int id, String nombres, String apellidoMaterno, String apellidoPaterno, String fechaNacimiento,
                    String pais, String ciudad, String correoElectronico, String lada, String telefono, String rfc,
                    String curp, String profesion, int departamentoId, int jerarquiaId, int estatusId) {
        super(id, nombres, apellidoMaterno, apellidoPaterno, fechaNacimiento, pais, ciudad, correoElectronico, lada, telefono, rfc, curp, profesion, departamentoId, jerarquiaId, estatusId);
    }

    // Métodos específicos de Empleado
}


