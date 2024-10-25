package controllers;

public class Auditoria {
    private String nombreCompletoEmpleado;
    private String departamentoNombre;
    private String accion;
    private String nombreSupervisor;
    private String timestamp;
    private String cambios;  // Aquí está la propiedad 'cambios'

    // Constructor
    public Auditoria(String nombreCompletoEmpleado, String departamentoNombre, String accion, String nombreSupervisor, String timestamp, String cambios) {
        this.nombreCompletoEmpleado = nombreCompletoEmpleado;
        this.departamentoNombre = departamentoNombre;
        this.accion = accion;
        this.nombreSupervisor = nombreSupervisor;
        this.timestamp = timestamp;
        this.cambios = cambios;  // Aquí estamos asignando 'cambios'
    }

    // Getters
    public String getNombreCompletoEmpleado() {
        return nombreCompletoEmpleado;
    }

    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    public String getAccion() {
        return accion;
    }

    public String getNombreSupervisor() {
        return nombreSupervisor;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCambios() {
        return cambios;  // Aquí el getter de 'cambios'
    }
    }


