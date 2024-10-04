package Usuarios;

public class Lider extends Supervisor {

    // Constructor
    public Lider(int id, String nombres, String apellidoPaterno, String apellidoMaterno, String correoElectronico, String departamento) {
        super(id, nombres, apellidoPaterno, apellidoMaterno, correoElectronico, departamento);
    }

    // Funcionalidades de un Líder
    public void asignarSupervisores() {
        System.out.println("Líder puede asignar o remover supervisores.");
    }


    // Sin restricciones
    public void accesoCompleto() {
        System.out.println("Líder tiene acceso completo a todas las configuraciones del sistema.");
    }
}

