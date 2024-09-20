package Usuarios;

public class Usuario {
    private int id;
    private String nombres;
    private String apellidoMaterno;
    private String apellidoPaterno;
    private String fechaNacimiento;
    private String pais;
    private String ciudad;
    private String correoElectronico;
    private String lada;
    private String telefono;
    private String rfc;
    private String curp;
    private String profesion;
    private int departamentoId;
    private int jerarquiaId;
    private int estatusId;

    // Constructor con todos los campos
    public Usuario(int id, String nombres, String apellidoMaterno, String apellidoPaterno, String fechaNacimiento,
                   String pais, String ciudad, String correoElectronico, String lada, String telefono, String rfc,
                   String curp, String profesion, int departamentoId, int jerarquiaId, int estatusId) {
        this.id = id;
        this.nombres = nombres;
        this.apellidoMaterno = apellidoMaterno;
        this.apellidoPaterno = apellidoPaterno;
        this.fechaNacimiento = fechaNacimiento;
        this.pais = pais;
        this.ciudad = ciudad;
        this.correoElectronico = correoElectronico;
        this.lada = lada;
        this.telefono = telefono;
        this.rfc = rfc;
        this.curp = curp;
        this.profesion = profesion;
        this.departamentoId = departamentoId;
        this.jerarquiaId = jerarquiaId;
        this.estatusId = estatusId;
    }

    // Getters y Setters para cada atributo
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getLada() {
        return lada;
    }

    public void setLada(String lada) {
        this.lada = lada;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public String getProfesion() {
        return profesion;
    }

    public void setProfesion(String profesion) {
        this.profesion = profesion;
    }

    public int getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(int departamentoId) {
        this.departamentoId = departamentoId;
    }

    public int getJerarquiaId() {
        return jerarquiaId;
    }

    public void setJerarquiaId(int jerarquiaId) {
        this.jerarquiaId = jerarquiaId;
    }

    public int getEstatusId() {
        return estatusId;
    }

    public void setEstatusId(int estatusId) {
        this.estatusId = estatusId;
    }

    // Métodos adicionales
    public String getNombreCompleto() {
        return nombres + " " + apellidoPaterno + " " + apellidoMaterno;
    }

    public boolean esActivo() {
        return estatusId == 1; // Supone que el estatus '1' es 'Activo'
    }
}
