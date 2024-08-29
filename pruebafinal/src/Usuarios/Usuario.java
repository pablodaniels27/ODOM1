package Usuarios;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import controllers.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


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
    private int huellaId;  // Atributo para almacenar el ID de la huella

    public String getTipoAsistencia() {
        return tipoAsistencia;
    }

    public void setTipoAsistencia(String tipoAsistencia) {
        this.tipoAsistencia = tipoAsistencia;
    }

    public String getTipoSalida() {
        return tipoSalida;
    }

    public void setTipoSalida(String tipoSalida) {
        this.tipoSalida = tipoSalida;
    }

    public String getEstadoEmpleado() {
        return estadoEmpleado;
    }

    public void setEstadoEmpleado(String estadoEmpleado) {
        this.estadoEmpleado = estadoEmpleado;
    }

    private String tipoAsistencia;  // Nuevo apartado: Tipo de Asistencia
    private String tipoSalida;      // Nuevo apartado: Tipo de Salida
    private String estadoEmpleado;  // Nuevo apartado: Estado del Empleado

    // Constructor completo
    public Usuario(int id, String nombres, String apellidoMaterno, String apellidoPaterno, String fechaNacimiento,
                   String pais, String ciudad, String correoElectronico, String lada, String telefono, String rfc,
                   String curp, String profesion, int departamentoId, int jerarquiaId, int huellaId,
                   String tipoAsistencia, String tipoSalida, String estadoEmpleado) {
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
        this.huellaId = huellaId;
        this.tipoAsistencia = tipoAsistencia;  // Asigna el nuevo apartado
        this.tipoSalida = tipoSalida;          // Asigna el nuevo apartado
        this.estadoEmpleado = estadoEmpleado;  // Asigna el nuevo apartado
    }

    // Getters y setters
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

    public int getHuellaId() {
        return huellaId;
    }

    public void setHuellaId(int huellaId) {
        this.huellaId = huellaId;
    }

    // Método para cargar la huella desde la base de datos utilizando DatabaseConnection
    public byte[] cargarHuellaDesdeBD() throws SQLException {
        String query = "SELECT huella FROM huellas WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection(); // Utiliza la conexión de la clase DatabaseConnection
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, this.huellaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("huella");
            } else {
                throw new SQLException("Huella no encontrada.");
            }
        }
    }



}
