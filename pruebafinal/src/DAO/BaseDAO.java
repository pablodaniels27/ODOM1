package DAO;

import Usuarios.Empleado;
import Usuarios.SessionManager;
import Usuarios.Supervisor;
import Usuarios.Usuario;

import controllers.Auditoria;
import controllers.AuditoriaController;
import controllers.DatabaseConnection;
import controllers.InicioController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static controllers.DatabaseConnection.getConnection;

public class BaseDAO {

    private BaseDAO() {
        // Evita que esta clase sea instanciada
    }

    private static final String CAMPO_CORREO = "correo_electronico";
    private static final String CAMPO_DEPARTAMENTO_ID = "departamento_id";
    private static final String CAMPO_FECHA_NACIMIENTO = "fecha_nacimiento";
    private static final String CAMPO_CIUDAD = "ciudad";
    private static final String CAMPO_TELEFONO = "telefono";
    private static final String CAMPO_JERARQUIA_ID = "jerarquia_id";
    private static final String CAMPO_NOMBRE = "nombre";
    private static final String CAMPO_APELLIDOPATERNO = "apellido_paterno";
    private static final String CAMPO_APELLIDOMATERNO = "apellido_materno";
    private static final String CAMPO_ESTATUS_ID = "estatus_id";
    private static final String CAMPO_PROFESION = "profesion";
    private static final String CAMPO_HORASALIDA = "hora_salida";
    private static final String CAMPO_HORAENTRADA = "hora_entrada";
    private static final String CAMPO_DEPARTAMENTO = "departamento";
    private static final String CAMPO_DEPARTAMENTOS = "departamentos";
    private static final String CAMPO_TIPOASISTENCIA = "tipo_asistencia";
    private static final String CAMPO_TODOSLOSDEPARTAMENTOS = "Todos los departamentos";
    private static final String CAMPO_FECHA = "fecha";
    private static final String CAMPO_NOMBRECOMPLETO = "nombreCompleto";
    private static final String CAMPO_NOMBRES = "nombres";
    private static final String CAMPO_ESTADO = "estado";
    private static final String CAMPO_NOTAS = "notas";
    private static final String CAMPO_EMPLEADO_ID = "empleado_id";
    private static final String CAMPO_SUPERVISOR_ID = "supervisor_id";
    private static final String CAMPO_ACCION = "action";
    private static final String CAMPO_EMPLEADO_OBJETIVO = "target_employee_id";
    private static final String CAMPO_DETALLES = "details";
    private static final String CAMPO_TIPOSALIDA = "tipo_salida";
    private static final String CAMPO_TIPOS_ASISTENCIA = "tipos_asistencia";
    // Nuevas constantes para las columnas de la tabla de huellas
    private static final String CAMPO_HUELLA = "huella";
    private static final String CAMPO_HUELLA_IMAGEN = "huella_imagen";
    private static final String CAMPO_CAMBIOS ="cambios";
    private static final String CAMPO_ENTRADA_ID = "entrada_id";



    //Estos metodos controla monitoreo//////

    public static int obtenerIdTipoAsistencia(String tipoAsistencia) throws SQLException {
        String query = "SELECT id FROM " + CAMPO_TIPOS_ASISTENCIA + " WHERE " + CAMPO_NOMBRE +" = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, tipoAsistencia);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return -1; // Devolver -1 si no se encuentra el tipo de asistencia
    }

    public static void actualizarTipoAsistencia(int empleadoId, String fechaEntrada, int tipoAsistenciaId) throws SQLException {
        // Usar las constantes para los nombres de columnas y tablas en la consulta
        String updateQuery = "UPDATE entradas_salidas SET " + CAMPO_TIPOASISTENCIA + "_id = ? " +
                "WHERE " + CAMPO_EMPLEADO_ID + " = ? AND dia_id = (SELECT id FROM dias WHERE " + CAMPO_FECHA + " = ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            preparedStatement.setInt(1, tipoAsistenciaId);
            preparedStatement.setInt(2, empleadoId);
            preparedStatement.setString(3, fechaEntrada);

            preparedStatement.executeUpdate();
        }
    }


    public static void registrarCambioLog(int supervisorId, String accion, int empleadoId, Integer entradaId, String detalles, String cambios) throws SQLException {
        // Consulta SQL para insertar el registro, incluyendo la columna entrada_id
        String insertLogQuery = "INSERT INTO logs (" + CAMPO_SUPERVISOR_ID + ", " + CAMPO_ACCION + ", " + CAMPO_EMPLEADO_OBJETIVO + ", " +
                CAMPO_ENTRADA_ID + ", " + CAMPO_DETALLES + ", " + CAMPO_CAMBIOS + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertLogQuery)) {

            // Configurar los valores en la consulta SQL
            preparedStatement.setInt(1, supervisorId);
            preparedStatement.setString(2, accion);
            preparedStatement.setInt(3, empleadoId);

            // Verificar si entradaId es null y configurarlo adecuadamente
            if (entradaId != null) {
                preparedStatement.setInt(4, entradaId);
            } else {
                preparedStatement.setNull(4, Types.INTEGER);
            }

            preparedStatement.setString(5, detalles); // Notas o detalles del cambio
            preparedStatement.setString(6, cambios);  // Descripción del cambio

            // Ejecutar la actualización
            preparedStatement.executeUpdate();
        }
    }


    public static void registrarCambioLogCambios(int supervisorId, String accion, int empleadoId, String cambios) throws SQLException {
        // Usar las constantes para los nombres de columnas en la consulta
        String insertLogQuery = "INSERT INTO logs (" + CAMPO_SUPERVISOR_ID + ", " + CAMPO_ACCION + ", " + CAMPO_EMPLEADO_OBJETIVO + ", " + CAMPO_CAMBIOS + ") VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertLogQuery)) {

            // Configurar los valores en la consulta SQL
            preparedStatement.setInt(1, supervisorId);
            preparedStatement.setString(2, accion);
            preparedStatement.setInt(3, empleadoId);
            preparedStatement.setString(4, cambios);  // Registrar los cambios

            // Ejecutar la actualización
            preparedStatement.executeUpdate();
        }
    }




    //este metodo tambien se usa en Registro controller
    public static List<String> obtenerDepartamentos() throws SQLException {
        String query = "SELECT " +  CAMPO_NOMBRE + " FROM " + CAMPO_DEPARTAMENTOS;
        List<String> departamentos = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                departamentos.add(resultSet.getString(CAMPO_NOMBRE));
            }
        }

        return departamentos;
    }

    public static List<Map<String, Object>> obtenerTodasLasEntradas() throws SQLException {
        // Obtener el usuario autenticado
        Usuario currentUser = SessionManager.getCurrentUser();

        // Base de la consulta SQL
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, " +
                "es.nombre AS estado, dias.fecha, es.id AS estado_id, " +
                "en.hora_entrada, en.hora_salida, t.nombre AS tipo_asistencia, " +
                "ts.nombre AS tipo_salida, " +
                "(SELECT l2.details FROM logs l2 " +
                " WHERE l2.target_employee_id = e.id " +
                " AND l2.action = 'Cambio de tipo de asistencia' " +
                " AND DATE(l2.timestamp) = dias.fecha " +
                " ORDER BY l2.timestamp DESC LIMIT 1) AS notas " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id ";

        // Verificar si el usuario autenticado es un Empleado o Supervisor y agregar filtros específicos
        if (currentUser instanceof Empleado) {
            // Si es un empleado, agregar el filtro para cargar solo sus registros
            Empleado empleado = (Empleado) currentUser;
            query += "WHERE e.id = ? "; // Filtrar por el ID del empleado

        } else if (currentUser instanceof Supervisor) {
            // Si es un supervisor, agregar el filtro de departamento
            Supervisor supervisor = (Supervisor) currentUser;
            int departamentoId = supervisor.getDepartamentoId();
            query += "WHERE e.departamento_id = ? "; // Filtrar por departamento
        }

        query += "ORDER BY dias.fecha DESC, en.hora_entrada DESC"; // Ordenar por fecha y hora de entrada

        List<Map<String, Object>> entries = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Si es un empleado, establecer el parámetro de su ID
            if (currentUser instanceof Empleado) {
                Empleado empleado = (Empleado) currentUser;
                statement.setInt(1, empleado.getId()); // Asignar el ID del empleado al parámetro

            } else if (currentUser instanceof Supervisor) {
                // Si es un supervisor, establecer el parámetro de departamento
                Supervisor supervisor = (Supervisor) currentUser;
                int departamentoId = supervisor.getDepartamentoId();
                statement.setInt(1, departamentoId); // Asignar el ID del departamento al parámetro
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " +
                        resultSet.getString("apellido_paterno") + " " +
                        resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha"));
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada"));
                employeeData.put("horaSalida", resultSet.getString("hora_salida"));
                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia"));
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida"));
                employeeData.put("estado", resultSet.getString("estado"));
                employeeData.put("notas", resultSet.getString("notas") != null ? resultSet.getString("notas") : ""); // Si no hay notas, mostrar vacío

                entries.add(employeeData);
            }
        }

        return entries;
    }

//desde aqui pa bajo

    public static List<Map<String, Object>> buscarPorFechaYDepartamento(String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados, String fechaInicio, String fechaFin) throws SQLException {
        // Obtener el supervisor actual desde SessionManager
        Usuario currentUser = SessionManager.getCurrentUser();
        int supervisorDepartamentoId = -1;

        if (currentUser instanceof Supervisor) {
            Supervisor supervisor = (Supervisor) currentUser;
            supervisorDepartamentoId = supervisor.getDepartamentoId(); // Obtener el ID del departamento del supervisor actual
        }

        String query = construirConsultaBase(supervisorDepartamentoId);

        query += agregarFiltroDepartamento(departamentoSeleccionado);
        query += agregarFiltroSupervisoresYEmpleados(incluirSupervisores, incluirEmpleados);
        query += agregarFiltroBusquedaNombre(searchQuery);

        query += " ORDER BY dias." + CAMPO_FECHA + " ASC"; // Ordenar por fecha

        List<Map<String, Object>> searchResults = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            asignarParametros(preparedStatement, departamentoSeleccionado, searchQuery, fechaInicio, fechaFin, supervisorDepartamentoId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                searchResults.add(llenarDatosEmpleado(resultSet));
            }
        }

        return searchResults;
    }

    private static String construirConsultaBase(int supervisorDepartamentoId) {
        String baseQuery = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, " +
                "es.nombre AS estado, dias.fecha, es.id AS estado_id, " +
                "en.id AS entradaId, en.hora_entrada, en.hora_salida, t.nombre AS tipo_asistencia, " +
                "ts.nombre AS tipo_salida, " +
                "(SELECT l2.details FROM logs l2 " +
                " WHERE l2.entrada_id = en.id " +
                " AND l2.action = 'Cambio de tipo de asistencia' " +
                " ORDER BY l2.timestamp DESC LIMIT 1) AS notas " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "LEFT JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " + // Cambiado a LEFT JOIN
                "WHERE dias.fecha BETWEEN?AND?";

        if (supervisorDepartamentoId != -1) {
            baseQuery += "AND e." + CAMPO_DEPARTAMENTO_ID + " = " + supervisorDepartamentoId + " ";
        }

        return baseQuery;
    }



    private static String agregarFiltroDepartamento(String departamentoSeleccionado) {
        // Solo aplicamos el filtro de departamento seleccionado si no es "Todos los departamentos"
        if (!departamentoSeleccionado.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
            return "AND e." + CAMPO_DEPARTAMENTO_ID + " IN (SELECT id FROM " + CAMPO_DEPARTAMENTOS + " WHERE " + CAMPO_NOMBRE + " = ?) ";
        }
        return "";
    }

    private static String agregarFiltroSupervisoresYEmpleados(boolean incluirSupervisores, boolean incluirEmpleados) {
        if (incluirSupervisores || incluirEmpleados) {
            StringBuilder filtro = new StringBuilder("AND (");
            if (incluirSupervisores) {
                filtro.append("e." + CAMPO_JERARQUIA_ID + " = 2 ");  // Supervisores
            }
            if (incluirSupervisores && incluirEmpleados) {
                filtro.append("OR ");
            }
            if (incluirEmpleados) {
                filtro.append("e." + CAMPO_JERARQUIA_ID + " = 3 ");  // Empleados
            }
            filtro.append(") ");
            return filtro.toString();
        }
        return "";
    }

    private static String agregarFiltroBusquedaNombre(String searchQuery) {
        if (!searchQuery.isEmpty()) {
            return "AND CONCAT(LOWER(TRIM(e." + CAMPO_NOMBRES + ")), ' ', LOWER(TRIM(e." + CAMPO_APELLIDOPATERNO + ")), ' ', LOWER(TRIM(e." + CAMPO_APELLIDOMATERNO + "))) LIKE ? ";
        }
        return "";
    }

    private static void asignarParametros(PreparedStatement preparedStatement, String departamentoSeleccionado, String searchQuery, String fechaInicio, String fechaFin, int supervisorDepartamentoId) throws SQLException {
        int paramIndex = 1;
        preparedStatement.setString(paramIndex++, fechaInicio);
        preparedStatement.setString(paramIndex++, fechaFin);

        // Si se seleccionó un departamento, agregarlo como parámetro
        if (!departamentoSeleccionado.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
            preparedStatement.setString(paramIndex++, departamentoSeleccionado);
        }

        // Agregar patrón de búsqueda si se proporcionó uno
        if (!searchQuery.isEmpty()) {
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";
            preparedStatement.setString(paramIndex++, searchPattern);
        }

        // No es necesario agregar el parámetro del departamento del supervisor, ya que está en la consulta base
    }

    private static Map<String, Object> llenarDatosEmpleado(ResultSet resultSet) throws SQLException {
        Map<String, Object> employeeData = new HashMap<>();
        employeeData.put("id", String.valueOf(resultSet.getInt("id")));
        employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " +
                resultSet.getString("apellido_paterno") + " " +
                resultSet.getString("apellido_materno"));
        employeeData.put("fechaEntrada", resultSet.getString("fecha"));
        employeeData.put("horaEntrada", resultSet.getString("hora_entrada"));
        employeeData.put("horaSalida", resultSet.getString("hora_salida"));
        employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia"));
        employeeData.put("tipoSalida", resultSet.getString("tipo_salida"));
        employeeData.put("estado", resultSet.getString("estado"));
        employeeData.put("notas", resultSet.getString("notas") != null ? resultSet.getString("notas") : "");

        // Agregar entradaId al mapa
        int entradaId = resultSet.getInt("entradaId");
        if (!resultSet.wasNull()) {
            employeeData.put("entradaId", entradaId);
        } else {
            employeeData.put("entradaId", null); // o -1 según la lógica de tu aplicación
        }

        return employeeData;
    }




    public static Set<String> buscarNombresPorAsistencia(String departamento, String tipoAsistencia, String fechaInicio, String fechaFin, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados) throws SQLException {
        // Construir la consulta usando las constantes
        String query = "SELECT e." + CAMPO_NOMBRES + ", e." + CAMPO_APELLIDOPATERNO + ", e." + CAMPO_APELLIDOMATERNO + " " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en." + CAMPO_EMPLEADO_ID + " = e.id " +
                "JOIN " + CAMPO_DEPARTAMENTOS + " d ON e." + CAMPO_DEPARTAMENTO_ID + " = d.id " +
                "JOIN dias ON en.dia_id = dias.id " + // Relacionar con la tabla de días para el rango de fechas
                "JOIN " + CAMPO_TIPOS_ASISTENCIA + " t ON en.tipo_asistencia_id = t.id " +
                "WHERE d." + CAMPO_NOMBRE + " = ? AND t." + CAMPO_NOMBRE + " = ? " +  // Filtrar por departamento y tipo de asistencia
                "AND dias." + CAMPO_FECHA + " BETWEEN ? AND ? ";

        // Añadir condiciones adicionales para supervisores o empleados
        if (incluirSupervisores || incluirEmpleados) {
            query += " AND (";
            if (incluirSupervisores) {
                query += "e." + CAMPO_JERARQUIA_ID + " = 2";
            }
            if (incluirSupervisores && incluirEmpleados) {
                query += " OR ";
            }
            if (incluirEmpleados) {
                query += "e." + CAMPO_JERARQUIA_ID + " = 3";
            }
            query += ")";
        }

        // Si hay una búsqueda, agregar el filtro al query para buscar por nombre, apellido o nombre completo
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query += " AND (e." + CAMPO_NOMBRES + " LIKE ? OR e." + CAMPO_APELLIDOPATERNO + " LIKE ? OR e." + CAMPO_APELLIDOMATERNO + " LIKE ? OR " +
                    "CONCAT(e." + CAMPO_NOMBRES + ", ' ', e." + CAMPO_APELLIDOPATERNO + ", ' ', e." + CAMPO_APELLIDOMATERNO + ") LIKE ?)";
        }

        Set<String> empleadosUnicos = new HashSet<>();

        try (Connection connectDB = getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {

            int paramIndex = 1;

            // Asignar los parámetros de la consulta
            preparedStatement.setString(paramIndex++, departamento);
            preparedStatement.setString(paramIndex++, tipoAsistencia);
            preparedStatement.setString(paramIndex++, fechaInicio);
            preparedStatement.setString(paramIndex++, fechaFin);

            // Si hay un valor de búsqueda, agregarlo como parámetro
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.trim() + "%";
                preparedStatement.setString(paramIndex++, searchPattern); // e.nombres LIKE ?
                preparedStatement.setString(paramIndex++, searchPattern); // e.apellido_paterno LIKE ?
                preparedStatement.setString(paramIndex++, searchPattern); // e.apellido_materno LIKE ?
                preparedStatement.setString(paramIndex, searchPattern); // CONCAT(...) LIKE ?
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String nombreCompleto = resultSet.getString(CAMPO_NOMBRES) + " " +
                        resultSet.getString(CAMPO_APELLIDOPATERNO) + " " +
                        resultSet.getString(CAMPO_APELLIDOMATERNO);

                // Agregar al Set, que no permitirá duplicados
                empleadosUnicos.add(nombreCompleto);
            }
        }

        return empleadosUnicos;
    }


    public static Set<String> buscarFechasPorEmpleado(String departamento, String tipoAsistencia, String nombreEmpleado, String fechaInicio, String fechaFin) throws SQLException {
        // Construir la consulta usando las constantes
        String query = "SELECT dias." + CAMPO_FECHA + " " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en." + CAMPO_EMPLEADO_ID + " = e.id " +
                "JOIN " + CAMPO_DEPARTAMENTOS + " d ON e." + CAMPO_DEPARTAMENTO_ID + " = d.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN " + CAMPO_TIPOS_ASISTENCIA + " t ON en.tipo_asistencia_id = t.id " +
                "WHERE t." + CAMPO_NOMBRE + " = ? " +  // Filtro por tipo de asistencia
                "AND (e." + CAMPO_NOMBRES + " = ? AND e." + CAMPO_APELLIDOPATERNO + " = ? AND e." + CAMPO_APELLIDOMATERNO + " = ?) " +  // Filtrar por nombre completo
                "AND dias." + CAMPO_FECHA + " BETWEEN ? AND ?";

        // Si el departamento no es "Todos los departamentos", agregar el filtro de departamento
        if (!departamento.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
            query += " AND d." + CAMPO_NOMBRE + " = ?";
        }

        Set<String> fechasUnicas = new HashSet<>();

        try (Connection connectDB = getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement(query)) {

            System.out.println("Ejecutando consulta SQL: " + query);

            // Asignar los parámetros
            preparedStatement.setString(1, tipoAsistencia);

            // Separar el nombre completo en partes: nombres, apellido paterno y materno
            String[] partesNombre = nombreEmpleado.split(" ");
            if (partesNombre.length < 3) {
                System.out.println("Nombre completo no válido: " + nombreEmpleado);
                return fechasUnicas; // Detener si el nombre no tiene al menos 3 partes
            }
            preparedStatement.setString(2, partesNombre[0]); // Nombres
            preparedStatement.setString(3, partesNombre[1]); // Apellido Paterno
            preparedStatement.setString(4, partesNombre[2]); // Apellido Materno

            preparedStatement.setString(5, fechaInicio);  // Fecha de inicio
            preparedStatement.setString(6, fechaFin);     // Fecha de fin

            // Si el departamento no es "Todos los departamentos", asignar también el valor del departamento
            if (!departamento.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
                preparedStatement.setString(7, departamento);
            }

            System.out.println("Parámetros asignados: ");
            System.out.println("Departamento: " + (departamento.equals(CAMPO_TODOSLOSDEPARTAMENTOS) ? "Todos" : departamento));
            System.out.println("Tipo de Asistencia: " + tipoAsistencia);
            System.out.println("Nombre del Empleado: " + nombreEmpleado);
            System.out.println("Fecha Inicio: " + fechaInicio);
            System.out.println("Fecha Fin: " + fechaFin);

            ResultSet resultSet = preparedStatement.executeQuery();
            int counter = 0;
            while (resultSet.next()) {
                String fecha = resultSet.getString(CAMPO_FECHA);
                fechasUnicas.add(fecha);  // Añadir al Set para evitar duplicados
                counter++;
                System.out.println("Fecha encontrada: " + fecha);
            }

            if (counter == 0) {
                System.out.println("No se encontraron fechas para el empleado: " + nombreEmpleado + " con el tipo de asistencia: " + tipoAsistencia);
            } else {
                System.out.println("Total de fechas encontradas: " + counter);
            }

        }

        return fechasUnicas;
    }


    public static ObservableList<String> buscarNombresPorConsulta(String searchQuery, String departamentoSeleccionado, boolean incluirSupervisores, boolean incluirEmpleados) throws SQLException {
        // Construir la consulta usando las constantes
        StringBuilder query = new StringBuilder("SELECT CONCAT(TRIM(e." + CAMPO_NOMBRES + "), ' ', TRIM(e." + CAMPO_APELLIDOPATERNO + "), ' ', TRIM(e." + CAMPO_APELLIDOMATERNO + ")) AS " + CAMPO_NOMBRECOMPLETO +
                ", e." + CAMPO_JERARQUIA_ID + ", d." + CAMPO_NOMBRE + " AS " + CAMPO_DEPARTAMENTO + " ");
        query.append("FROM empleados e ");
        query.append("JOIN " + CAMPO_DEPARTAMENTOS + " d ON e." + CAMPO_DEPARTAMENTO_ID + " = d.id ");
        query.append("WHERE ");

        // Filtrar por nombre completo usando CONCAT en lugar de buscar por partes
        query.append("CONCAT(LOWER(TRIM(e." + CAMPO_NOMBRES + ")), ' ', LOWER(TRIM(e." + CAMPO_APELLIDOPATERNO + ")), ' ', LOWER(TRIM(e." + CAMPO_APELLIDOMATERNO + "))) LIKE ? ");

        // Filtros para supervisores o empleados
        if (incluirSupervisores || incluirEmpleados) {
            query.append("AND (");
            if (incluirSupervisores) {
                query.append("e." + CAMPO_JERARQUIA_ID + " = 2");  // Supervisores
            }
            if (incluirSupervisores && incluirEmpleados) {
                query.append(" OR ");
            }
            if (incluirEmpleados) {
                query.append("e." + CAMPO_JERARQUIA_ID + " = 3");  // Empleados
            }
            query.append(") ");
        }

        // Filtrar por departamento, si no se seleccionó "Todos los departamentos"
        if (!departamentoSeleccionado.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
            query.append("AND d." + CAMPO_NOMBRE + " = ? ");
        }

        query.append("ORDER BY " + CAMPO_NOMBRECOMPLETO + " ASC");

        ObservableList<String> results = FXCollections.observableArrayList();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

            int paramIndex = 1;

            // Crear el patrón de búsqueda
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";

            // Asignar el patrón de búsqueda
            preparedStatement.setString(paramIndex++, searchPattern);

            // Si el departamento seleccionado no es "Todos los departamentos", agregarlo como parámetro
            if (!departamentoSeleccionado.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
                preparedStatement.setString(paramIndex++, departamentoSeleccionado);
            }

            // Ejecutar la consulta
            ResultSet resultSet = preparedStatement.executeQuery();

            // Procesar los resultados
            while (resultSet.next()) {
                String nombreCompleto = resultSet.getString(CAMPO_NOMBRECOMPLETO);
                int jerarquiaId = resultSet.getInt(CAMPO_JERARQUIA_ID);
                String departamento = resultSet.getString(CAMPO_DEPARTAMENTO);

                results.add(nombreCompleto + "," + jerarquiaId + "," + departamento);  // Añadir el nombre completo, jerarquía, y departamento a los resultados
            }
        }

        return results;
    }

    public static List<Map<String, Object>> obtenerEntradasPorDepartamento(int departamentoId) throws SQLException {
        // Construir la consulta para obtener empleados filtrados por departamento
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, " +
                "es.nombre AS estado, dias.fecha, es.id AS estado_id, " +
                "en.id AS entradaId, en.hora_entrada, en.hora_salida, " +
                "t.nombre AS tipo_asistencia, ts.nombre AS tipo_salida, " +
                "(SELECT l2.details FROM logs l2 " +
                " WHERE l2.target_employee_id = e.id " +
                " AND l2.action = 'Cambio de tipo de asistencia' " +
                " AND DATE(l2.timestamp) = dias.fecha " +
                " ORDER BY l2.timestamp DESC LIMIT 1) AS notas " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "WHERE e.departamento_id = ? " +  // Filtrar por departamento
                "ORDER BY dias.fecha DESC, en.hora_entrada DESC";

        List<Map<String, Object>> entries = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Asignar el departamentoId al parámetro de la consulta
            preparedStatement.setInt(1, departamentoId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " +
                        resultSet.getString("apellido_paterno") + " " +
                        resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha"));
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada"));
                employeeData.put("horaSalida", resultSet.getString("hora_salida"));
                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia"));
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida"));
                employeeData.put("estado", resultSet.getString("estado"));
                employeeData.put("notas", resultSet.getString("notas") != null ? resultSet.getString("notas") : ""); // Si no hay notas, mostrar vacío
                employeeData.put("entradaId", resultSet.getInt("entradaId")); // Agregar entradaId al mapa

                entries.add(employeeData);
            }
        }

        return entries;
    }


    // aqui se cierra monitoreo  ////////////////////////////////////////////////////

    //GRAFICOSCONTROLLER

    public static List<Map<String, Object>> obtenerConteoTiposAsistencia(String fechaInicio, String fechaFin, String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();

        // Construir la consulta usando las constantes
        String query = "SELECT d." + CAMPO_NOMBRE + " AS " + CAMPO_DEPARTAMENTO + ", t." + CAMPO_NOMBRE + " AS " + CAMPO_TIPOASISTENCIA + ", COUNT(*) AS cantidad " +
                "FROM entradas_salidas en " +
                "JOIN " + CAMPO_TIPOS_ASISTENCIA + " t ON en.tipo_asistencia_id = t.id " +
                "JOIN empleados e ON en." + CAMPO_EMPLEADO_ID + " = e.id " +
                "JOIN " + CAMPO_DEPARTAMENTOS + " d ON e." + CAMPO_DEPARTAMENTO_ID + " = d.id " +
                "JOIN dias di ON en.dia_id = di.id " +
                "WHERE di." + CAMPO_FECHA + " BETWEEN ? AND ? ";

        // Filtrar por departamento, si no se seleccionó "Todos los departamentos"
        if (!departamentoSeleccionado.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
            query += "AND d." + CAMPO_NOMBRE + " = ? ";
        }

        // Modificación para buscar por nombre completo o partes del nombre
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            query += "AND (LOWER(e." + CAMPO_NOMBRES + ") LIKE ? OR LOWER(e." + CAMPO_APELLIDOPATERNO + ") LIKE ? OR LOWER(e." + CAMPO_APELLIDOMATERNO + ") LIKE ? " +
                    "OR CONCAT(LOWER(e." + CAMPO_NOMBRES + "), ' ', LOWER(e." + CAMPO_APELLIDOPATERNO + "), ' ', LOWER(e." + CAMPO_APELLIDOMATERNO + ")) LIKE ?) ";
        }

        // Filtrar por jerarquía (supervisores o empleados)
        if (incluirSupervisores && !incluirEmpleados) {
            query += "AND e." + CAMPO_JERARQUIA_ID + " = 2 ";  // Solo supervisores
        } else if (incluirEmpleados && !incluirSupervisores) {
            query += "AND e." + CAMPO_JERARQUIA_ID + " = 3 ";  // Solo empleados
        }

        query += "GROUP BY d." + CAMPO_NOMBRE + ", t." + CAMPO_NOMBRE;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Asignar parámetros
            preparedStatement.setString(1, fechaInicio);
            preparedStatement.setString(2, fechaFin);

            int paramIndex = 3;
            if (!departamentoSeleccionado.equals(CAMPO_TODOSLOSDEPARTAMENTOS)) {
                preparedStatement.setString(paramIndex++, departamentoSeleccionado);
            }

            // Modificación para pasar el patrón de búsqueda
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String searchPattern = "%" + searchQuery.trim().toLowerCase() + "%";
                preparedStatement.setString(paramIndex++, searchPattern);  // Para nombres
                preparedStatement.setString(paramIndex++, searchPattern);  // Para apellido paterno
                preparedStatement.setString(paramIndex++, searchPattern);  // Para apellido materno
                preparedStatement.setString(paramIndex++, searchPattern);  // Para la concatenación del nombre completo
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> resultado = new HashMap<>();
                resultado.put(CAMPO_DEPARTAMENTO, resultSet.getString(CAMPO_DEPARTAMENTO));
                resultado.put(CAMPO_TIPOASISTENCIA, resultSet.getString(CAMPO_TIPOASISTENCIA));
                resultado.put("cantidad", resultSet.getInt("cantidad"));
                resultados.add(resultado);
            }
        }

        return resultados;
    }


    //Aqui termina graficoscontroller////////////////////////

    //InicioController

    public static List<Map<String, Object>> obtenerAsistenciaSemanal(int empleadoId, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();

        // Construir la consulta usando las constantes
        String query = "SELECT dia." + CAMPO_FECHA + ", es." + CAMPO_HORAENTRADA + ", es." + CAMPO_HORASALIDA + " " +
                "FROM entradas_salidas es " +
                "JOIN dias dia ON es.dia_id = dia.id " +
                "WHERE es." + CAMPO_EMPLEADO_ID + " = ? AND dia." + CAMPO_FECHA + " BETWEEN ? AND ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Asignar los parámetros de la consulta
            preparedStatement.setInt(1, empleadoId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(fechaInicio));
            preparedStatement.setDate(3, java.sql.Date.valueOf(fechaFin));

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> resultado = new HashMap<>();
                resultado.put(CAMPO_FECHA, resultSet.getDate(CAMPO_FECHA));
                resultado.put(CAMPO_HORAENTRADA, resultSet.getString(CAMPO_HORAENTRADA));
                resultado.put(CAMPO_HORASALIDA, resultSet.getString(CAMPO_HORASALIDA));
                resultados.add(resultado);
            }
        }

        return resultados;
    }


    public static List<InicioController.Employee> obtenerEmpleados(String filter, int departamentoId) throws SQLException {
        List<InicioController.Employee> employees = new ArrayList<>();

        // Construir la consulta para obtener solo empleados del departamento del supervisor
        String query = "SELECT id, CONCAT(e." + CAMPO_NOMBRES + ", ' ', e." + CAMPO_APELLIDOPATERNO + ", ' ', e." + CAMPO_APELLIDOMATERNO + ") AS " + CAMPO_NOMBRECOMPLETO +
                ", e." + CAMPO_PROFESION + " " +
                "FROM empleados e " +
                "WHERE e." + CAMPO_DEPARTAMENTO_ID + " = ?";  // Filtro por departamento

        // Si hay un filtro, añadir la condición a la consulta SQL
        if (!filter.isEmpty()) {
            query += " AND CONCAT(e." + CAMPO_NOMBRES + ", ' ', e." + CAMPO_APELLIDOPATERNO + ", ' ', e." + CAMPO_APELLIDOMATERNO + ") LIKE ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Asignar el `departamentoId`
            preparedStatement.setInt(1, departamentoId);

            // Asignar el filtro si está presente
            if (!filter.isEmpty()) {
                preparedStatement.setString(2, "%" + filter + "%");
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String fullName = resultSet.getString(CAMPO_NOMBRECOMPLETO);
                String profession = resultSet.getString(CAMPO_PROFESION);

                employees.add(new InicioController.Employee(id, fullName, profession));
            }
        }

        return employees;
    }

    public static List<InicioController.Employee> obtenerTodosLosEmpleados(String filter) throws SQLException {
        List<InicioController.Employee> employees = new ArrayList<>();

        // Consulta para obtener todos los empleados, sin filtrar por departamento
        String query = "SELECT id, CONCAT(e." + CAMPO_NOMBRES + ", ' ', e." + CAMPO_APELLIDOPATERNO + ", ' ', e." + CAMPO_APELLIDOMATERNO + ") AS " + CAMPO_NOMBRECOMPLETO +
                ", e." + CAMPO_PROFESION + " " +
                "FROM empleados e";

        // Si hay un filtro, añadir la condición a la consulta SQL
        if (!filter.isEmpty()) {
            query += " WHERE CONCAT(e." + CAMPO_NOMBRES + ", ' ', e." + CAMPO_APELLIDOPATERNO + ", ' ', e." + CAMPO_APELLIDOMATERNO + ") LIKE ?";
        }

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Asignar el filtro si está presente
            if (!filter.isEmpty()) {
                preparedStatement.setString(1, "%" + filter + "%");
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String fullName = resultSet.getString(CAMPO_NOMBRECOMPLETO);
                String profession = resultSet.getString(CAMPO_PROFESION);

                employees.add(new InicioController.Employee(id, fullName, profession));
            }
        }

        return employees;
    }



    //Aqui acaba inicioController/////////////////
    //AuditoriaController

    public static ObservableList<String> buscarSupervisores(String searchQuery) throws SQLException {
        // Consulta SQL para buscar supervisores en la tabla empleados basándose en su jerarquía
        String query = "SELECT DISTINCT CONCAT(e." + CAMPO_NOMBRES + ", ' ', e." + CAMPO_APELLIDOPATERNO + ", ' ', e." + CAMPO_APELLIDOMATERNO + ") AS supervisorNombre " +
                "FROM empleados e " +
                "WHERE e.jerarquia_id = 2 " +  // Suponiendo que la jerarquía 2 es para supervisores
                "AND CONCAT(LOWER(TRIM(e." + CAMPO_NOMBRES + ")), ' ', LOWER(TRIM(e." + CAMPO_APELLIDOPATERNO + ")), ' ', LOWER(TRIM(e." + CAMPO_APELLIDOMATERNO + "))) LIKE ? " +
                "ORDER BY supervisorNombre ASC";

        ObservableList<String> results = FXCollections.observableArrayList();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Transformar el parámetro de búsqueda a minúsculas y rodearlo con '%' para el LIKE
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";
            preparedStatement.setString(1, searchPattern);

            // Ejecutar la consulta
            ResultSet resultSet = preparedStatement.executeQuery();

            // Extraer resultados y agregarlos a la lista
            while (resultSet.next()) {
                String supervisorNombre = resultSet.getString("supervisorNombre");
                results.add(supervisorNombre);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar supervisores: " + e.getMessage());
            throw e;  // Volver a lanzar la excepción para que sea manejada en un nivel superior si es necesario
        }

        return results;
    }



    public static List<Auditoria> obtenerDatosAuditoria() throws SQLException {
        String query = "SELECT l.supervisor_id, l.action, l.target_employee_id, l.timestamp, l.cambios, " +
                "esuper.nombres AS supervisor_nombres, esuper.apellido_paterno AS supervisor_apellido_paterno, esuper.apellido_materno AS supervisor_apellido_materno, " +
                "etarget.nombres AS empleado_nombres, etarget.apellido_paterno AS empleado_apellido_paterno, etarget.apellido_materno AS empleado_apellido_materno, " +
                "d.nombre AS departamento_nombre " +
                "FROM logs l " +
                "JOIN empleados esuper ON l.supervisor_id = esuper.id " +
                "JOIN empleados etarget ON l.target_employee_id = etarget.id " +
                "JOIN departamentos d ON etarget.departamento_id = d.id " +
                "ORDER BY l.timestamp DESC";

        List<Auditoria> auditoriaList = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String nombreCompletoSupervisor = resultSet.getString("supervisor_nombres") + " " +
                        resultSet.getString("supervisor_apellido_paterno") + " " +
                        resultSet.getString("supervisor_apellido_materno");

                String nombreCompletoEmpleado = resultSet.getString("empleado_nombres") + " " +
                        resultSet.getString("empleado_apellido_paterno") + " " +
                        resultSet.getString("empleado_apellido_materno");

                String departamentoNombre = resultSet.getString("departamento_nombre");
                String accion = resultSet.getString("action");
                String timestamp = resultSet.getString("timestamp");
                String cambios = resultSet.getString("cambios");  // Recuperamos el valor de la columna "cambios"

                auditoriaList.add(new Auditoria(nombreCompletoEmpleado, departamentoNombre, accion, nombreCompletoSupervisor, timestamp, cambios));
            }
        }

        return auditoriaList;
    }





    //TERMINA AUDITORIA/////////////////////////
    //REGISTRO CONTROLLER/

    public static List<String> obtenerPuestos() throws SQLException {
        // Construir la consulta usando las constantes
        String query = "SELECT " + CAMPO_NOMBRE + " FROM jerarquias";
        List<String> puestos = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                puestos.add(resultSet.getString(CAMPO_NOMBRE));
            }
        }

        return puestos;
    }


    public static int obtenerIdDepartamento(String departamentoNombre) throws SQLException {
        // Construir la consulta usando las constantes
        String query = "SELECT id FROM " + CAMPO_DEPARTAMENTOS + " WHERE " + CAMPO_NOMBRE + " = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, departamentoNombre);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return 0; // Retornar 0 si no se encuentra el departamento
    }


    public static int obtenerIdPuesto(String puestoNombre) throws SQLException {
        // Construir la consulta usando las constantes
        String query = "SELECT id FROM jerarquias WHERE " + CAMPO_NOMBRE + " = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, puestoNombre);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return 0; // Retornar 0 si no se encuentra el puesto
    }

    public static boolean insertarContraseña(int empleadoId, String contraseñaHash) throws SQLException {
        String query = "INSERT INTO usuarios (empleado_id, contrasena_hash) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, empleadoId);
            statement.setString(2, contraseñaHash);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0; // Devuelve true si la inserción fue exitosa
        }
    }


    public static int insertarEmpleado(String nombre, String apellidoMaterno, String apellidoPaterno, Date fechaNacimiento, String pais, String ciudad, String email,
                                       String lada, String telefono, String rfc, String curp, String profesion, int departamentoId, int jerarquiaId) throws SQLException {
        // Construir la consulta usando las constantes
        String query = "INSERT INTO empleados (" + CAMPO_NOMBRES + ", " + CAMPO_APELLIDOMATERNO + ", " + CAMPO_APELLIDOPATERNO + ", " + CAMPO_FECHA_NACIMIENTO +
                ", pais, " + CAMPO_CIUDAD + ", " + CAMPO_CORREO + ", lada, " + CAMPO_TELEFONO + ", rfc, curp, " + CAMPO_PROFESION + ", " + CAMPO_DEPARTAMENTO_ID +
                ", " + CAMPO_JERARQUIA_ID + ", " + CAMPO_ESTATUS_ID + ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, nombre);
            statement.setString(2, apellidoMaterno);
            statement.setString(3, apellidoPaterno);
            statement.setDate(4, fechaNacimiento);
            statement.setString(5, pais);
            statement.setString(6, ciudad);
            statement.setString(7, email);
            statement.setString(8, lada);
            statement.setString(9, telefono);
            statement.setString(10, rfc);
            statement.setString(11, curp);
            statement.setString(12, profesion);
            statement.setInt(13, departamentoId);
            statement.setInt(14, jerarquiaId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1; // Retornar -1 si no se pudo insertar el empleado
    }



        private static final Logger LOGGER = Logger.getLogger(BaseDAO.class.getName());

        public static byte[] serializarTemplate(Object template) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {

                oos.writeObject(template);
                oos.flush();
                return bos.toByteArray();

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error serializing template", e);
                return new byte[0]; // Retornar array vacío si hay un error en la serialización
            }
        }


    public static int insertarHuella(int empleadoId, byte[] serializedTemplate, byte[] fingerprintImageBytes) throws SQLException {
        // Construir la consulta usando las constantes
        String query = "INSERT INTO huellas (" + CAMPO_EMPLEADO_ID + ", " + CAMPO_HUELLA + ", " + CAMPO_HUELLA_IMAGEN + ") VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, empleadoId);
            statement.setBytes(2, serializedTemplate);
            statement.setBytes(3, fingerprintImageBytes);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1; // Retornar -1 si no se pudo insertar la huella
    }

    public static int actualizarCambioHuella(int empleadoId, byte[] serializedTemplate, byte[] fingerprintImageBytes) throws SQLException {
        // Construir la consulta de actualización
        String query = "UPDATE huellas SET " + CAMPO_HUELLA + " = ?, " + CAMPO_HUELLA_IMAGEN + " = ? WHERE " + CAMPO_EMPLEADO_ID + " = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBytes(1, serializedTemplate);
            statement.setBytes(2, fingerprintImageBytes);
            statement.setInt(3, empleadoId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return empleadoId; // Retornar el ID del empleado si se pudo actualizar la huella
            }
        }
        return -1; // Retornar -1 si no se pudo actualizar la huella
    }




    public static void actualizarHuellaEmpleado(int empleadoId, int huellaId) throws SQLException {
        // Construir la consulta usando las constantes
        String query = "UPDATE empleados SET " + CAMPO_HUELLA + " = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, huellaId);
            statement.setInt(2, empleadoId);
            statement.executeUpdate();
        }
    }

    public static int obtenerEmpleadoIdPorCorreo(String correo) throws SQLException {
        String query = "SELECT id FROM empleados WHERE " + CAMPO_CORREO + " = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, correo);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
        return -1; // Retornar -1 si no se encuentra el empleado
    }

    public static boolean verificarCorreoExistente(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM empleados WHERE correo_electronico = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return true; // El correo ya existe en la base de datos
            }
        }
        return false; // El correo no está registrado
    }





    //aqui termina registroController///
    //REGISTRO SUCURSAL//////////////////////////////////7

    public static List<Map<String, Object>> obtenerEmpleados(String filtro, int limit, int offset, Integer departamentoId) throws SQLException {
        List<Map<String, Object>> empleados = new ArrayList<>();

        // Modificar la consulta para incluir el filtro del departamento si es necesario
        String sql = "SELECT id, " + CAMPO_NOMBRES + ", " + CAMPO_APELLIDOPATERNO + ", " + CAMPO_PROFESION + ", " + CAMPO_ESTATUS_ID +
                " FROM empleados " +
                "WHERE " + CAMPO_JERARQUIA_ID + " = 3 AND " + CAMPO_ESTATUS_ID + " != 4 " +
                "AND (" + CAMPO_NOMBRES + " LIKE ? OR " + CAMPO_APELLIDOPATERNO + " LIKE ?)";

        // Si el departamentoId es proporcionado, lo añadimos como filtro
        if (departamentoId != null) {
            sql += " AND departamento_id = ?";
        }

        sql += " LIMIT ? OFFSET ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String filtroSQL = "%" + filtro + "%";
            statement.setString(1, filtroSQL);
            statement.setString(2, filtroSQL);

            int index = 3;
            if (departamentoId != null) {
                statement.setInt(index++, departamentoId);  // Si se pasa el departamento, lo agregamos a la consulta
            }

            statement.setInt(index++, limit);
            statement.setInt(index, offset);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> empleado = new HashMap<>();
                empleado.put("id", resultSet.getInt("id"));
                empleado.put(CAMPO_NOMBRES, resultSet.getString(CAMPO_NOMBRES));
                empleado.put(CAMPO_APELLIDOPATERNO, resultSet.getString(CAMPO_APELLIDOPATERNO));
                empleado.put(CAMPO_PROFESION, resultSet.getString(CAMPO_PROFESION));
                empleado.put(CAMPO_ESTATUS_ID, resultSet.getInt(CAMPO_ESTATUS_ID));
                empleados.add(empleado);
            }
        }

        return empleados;
    }



    public static List<Map<String, Object>> obtenerSupervisores(String filtro, int limit, int offset) throws SQLException {
        List<Map<String, Object>> supervisores = new ArrayList<>();

        // Construir la consulta usando las constantes
        String sql = "SELECT id, " + CAMPO_NOMBRES + ", " + CAMPO_APELLIDOPATERNO + ", " + CAMPO_PROFESION + ", " + CAMPO_ESTATUS_ID +
                " FROM empleados " +
                "WHERE " + CAMPO_JERARQUIA_ID + " = 2 AND " + CAMPO_ESTATUS_ID + " != 4 " +
                "AND (" + CAMPO_NOMBRES + " LIKE ? OR " + CAMPO_APELLIDOPATERNO + " LIKE ?) " +
                "LIMIT ? OFFSET ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String filtroSQL = "%" + filtro + "%";
            statement.setString(1, filtroSQL);
            statement.setString(2, filtroSQL);
            statement.setInt(3, limit);
            statement.setInt(4, offset);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> supervisor = new HashMap<>();
                supervisor.put("id", resultSet.getInt("id"));
                supervisor.put(CAMPO_NOMBRES, resultSet.getString(CAMPO_NOMBRES));
                supervisor.put(CAMPO_APELLIDOPATERNO, resultSet.getString(CAMPO_APELLIDOPATERNO));
                supervisor.put(CAMPO_PROFESION, resultSet.getString(CAMPO_PROFESION));
                supervisor.put(CAMPO_ESTATUS_ID, resultSet.getInt(CAMPO_ESTATUS_ID));
                supervisores.add(supervisor);
            }
        }

        return supervisores;
    }


    public static void darDeBajaEmpleado(int empleadoId) throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "UPDATE empleados SET " + CAMPO_ESTATUS_ID + " = 4 WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, empleadoId);
            statement.executeUpdate();
        }
    }

    public static String obtenerNombreEmpleado(int empleadoId) throws SQLException {
        String query = "SELECT CONCAT(" + CAMPO_NOMBRES + ", ' ', " + CAMPO_APELLIDOPATERNO + ", ' ', " + CAMPO_APELLIDOMATERNO + ") AS nombre_completo FROM empleados WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, empleadoId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nombre_completo");
            }
        }
        return null; // Retorna null si el empleado no se encuentra
    }



    //termina gestion de empleados
    //EDICION CONTROLLER///////////////////
    public static Map<String, Integer> obtenerDepartamentosid() throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "SELECT id, " + CAMPO_NOMBRE + " FROM " + CAMPO_DEPARTAMENTOS;
        Map<String, Integer> departamentos = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString(CAMPO_NOMBRE);
                departamentos.put(nombre, id);
            }
        }

        return departamentos;
    }


    public static Map<String, Integer> obtenerPuestosid() throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "SELECT id, " + CAMPO_NOMBRE + " FROM jerarquias WHERE id != 1";
        Map<String, Integer> puestos = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString(CAMPO_NOMBRE);
                puestos.put(nombre, id);
            }
        }

        return puestos;
    }


    public static Map<String, Integer> obtenerEstatusEmpleados() throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "SELECT id, " + CAMPO_NOMBRE + " FROM estatus_empleado";
        Map<String, Integer> estatus = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString(CAMPO_NOMBRE);
                estatus.put(nombre, id);
            }
        }

        return estatus;
    }


    public static Map<String, Object> obtenerDatosEmpleado(int empleadoId) throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "SELECT * FROM empleados WHERE id = ?";
        Map<String, Object> empleadoData = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, empleadoId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                empleadoData.put(CAMPO_NOMBRES, resultSet.getString(CAMPO_NOMBRES));
                empleadoData.put(CAMPO_APELLIDOPATERNO, resultSet.getString(CAMPO_APELLIDOPATERNO));
                empleadoData.put(CAMPO_APELLIDOMATERNO, resultSet.getString(CAMPO_APELLIDOMATERNO));
                empleadoData.put("pais", resultSet.getString("pais"));
                empleadoData.put(CAMPO_CIUDAD, resultSet.getString(CAMPO_CIUDAD));
                empleadoData.put("lada", resultSet.getString("lada"));
                empleadoData.put(CAMPO_TELEFONO, resultSet.getString(CAMPO_TELEFONO));
                empleadoData.put(CAMPO_CORREO, resultSet.getString(CAMPO_CORREO));
                empleadoData.put("rfc", resultSet.getString("rfc"));
                empleadoData.put("curp", resultSet.getString("curp"));
                empleadoData.put(CAMPO_PROFESION, resultSet.getString(CAMPO_PROFESION));
                empleadoData.put(CAMPO_FECHA_NACIMIENTO, resultSet.getDate(CAMPO_FECHA_NACIMIENTO));
                empleadoData.put(CAMPO_DEPARTAMENTO_ID, resultSet.getInt(CAMPO_DEPARTAMENTO_ID));
                empleadoData.put(CAMPO_JERARQUIA_ID, resultSet.getInt(CAMPO_JERARQUIA_ID));
                empleadoData.put(CAMPO_ESTATUS_ID, resultSet.getInt(CAMPO_ESTATUS_ID));
            } else {
                return new HashMap<>(); // Retornar mapa vacío si no se encuentra el empleado
            }
        }

        return empleadoData;
    }


    public static byte[] obtenerHuellaImagen(int empleadoId) throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "SELECT " + CAMPO_HUELLA_IMAGEN + " FROM huellas WHERE " + CAMPO_EMPLEADO_ID + " = ?";
        byte[] imageBytes = null;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, empleadoId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                imageBytes = resultSet.getBytes(CAMPO_HUELLA_IMAGEN);
            }
        }

        return imageBytes;
    }


    public static boolean actualizarEmpleado(Map<String, Object> empleadoData) throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "UPDATE empleados SET " + CAMPO_NOMBRES + " = ?, " + CAMPO_APELLIDOPATERNO + " = ?, " + CAMPO_APELLIDOMATERNO + " = ?, " +
                "pais = ?, " + CAMPO_CIUDAD + " = ?, " +
                "lada = ?, " + CAMPO_TELEFONO + " = ?, " + CAMPO_CORREO + " = ?, " +
                "rfc = ?, curp = ?, " + CAMPO_PROFESION + " = ?, " + CAMPO_FECHA_NACIMIENTO + " = ?, " +
                CAMPO_DEPARTAMENTO_ID + " = ?, " + CAMPO_JERARQUIA_ID + " = ?, " + CAMPO_ESTATUS_ID + " = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, (String) empleadoData.get(CAMPO_NOMBRES));
            statement.setString(2, (String) empleadoData.get(CAMPO_APELLIDOPATERNO));
            statement.setString(3, (String) empleadoData.get(CAMPO_APELLIDOMATERNO));
            statement.setString(4, (String) empleadoData.get("pais"));
            statement.setString(5, (String) empleadoData.get(CAMPO_CIUDAD));
            statement.setString(6, (String) empleadoData.get("lada"));
            statement.setString(7, (String) empleadoData.get(CAMPO_TELEFONO));
            statement.setString(8, (String) empleadoData.get(CAMPO_CORREO));
            statement.setString(9, (String) empleadoData.get("rfc"));
            statement.setString(10, (String) empleadoData.get("curp"));
            statement.setString(11, (String) empleadoData.get(CAMPO_PROFESION));
            statement.setDate(12, (java.sql.Date) empleadoData.get(CAMPO_FECHA_NACIMIENTO));
            statement.setInt(13, (int) empleadoData.get(CAMPO_DEPARTAMENTO_ID));
            statement.setInt(14, (int) empleadoData.get(CAMPO_JERARQUIA_ID));
            statement.setInt(15, (int) empleadoData.get(CAMPO_ESTATUS_ID));
            statement.setInt(16, (int) empleadoData.get("id"));

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static String obtenerNombreDepartamento(int departamentoId) throws SQLException {
        String query = "SELECT nombre FROM departamentos WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, departamentoId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nombre");
            }
        }
        return null; // o un valor predeterminado si el ID no existe
    }

    public static String obtenerNombreEstatus(int estatusId) throws SQLException {
        String query = "SELECT nombre FROM estatus_empleado WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, estatusId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nombre");
            }
        }
        return null; // o un valor predeterminado si el ID no existe
    }



    //TERMINA EDICION//
    //IDENTIFICARSE CONTROLLER///////

    public static List<Map<String, Object>> obtenerHuellasEmpleadosActivos() throws SQLException {
        // Construir la consulta usando las constantes
        String sql = "SELECT h." + CAMPO_HUELLA + ", e.id, e." + CAMPO_NOMBRES + ", e." + CAMPO_APELLIDOPATERNO + " " +
                "FROM huellas h " +
                "JOIN empleados e ON h." + CAMPO_EMPLEADO_ID + " = e.id " +
                "WHERE e." + CAMPO_ESTATUS_ID + " = 1";

        List<Map<String, Object>> huellas = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, Object> huellaData = new HashMap<>();
                huellaData.put(CAMPO_HUELLA, resultSet.getBytes(CAMPO_HUELLA));
                huellaData.put("id", resultSet.getInt("id"));
                huellaData.put(CAMPO_NOMBRES, resultSet.getString(CAMPO_NOMBRES));
                huellaData.put(CAMPO_APELLIDOPATERNO, resultSet.getString(CAMPO_APELLIDOPATERNO));

                huellas.add(huellaData);
            }
        }

        return huellas;
    }


    ///termina IDENTIFICARSE///
    //Asistencias Controller//

    public static Optional<Map<String, LocalTime>> obtenerRegistrosDelDia(int empleadoId, LocalDate fecha) throws SQLException {
        String sql = "SELECT hora_entrada, hora_salida FROM entradas_salidas " +
                "JOIN dias ON entradas_salidas.dia_id = dias.id " +
                "WHERE entradas_salidas.empleado_id = ? AND di  as.fecha = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, empleadoId);
            statement.setDate(2, java.sql.Date.valueOf(fecha));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Map<String, LocalTime> recordData = new HashMap<>();
                recordData.put(CAMPO_HORAENTRADA, resultSet.getTime(CAMPO_HORAENTRADA).toLocalTime());
                recordData.put(CAMPO_HORASALIDA, resultSet.getTime(CAMPO_HORASALIDA) != null ? resultSet.getTime(CAMPO_HORASALIDA).toLocalTime() : null);

                return Optional.of(recordData);
            }
        }

        return Optional.empty();
    }

    public static void insertarEntradaAsistencia(int empleadoId, LocalDate fecha, LocalTime horaEntrada) throws SQLException {
        try (Connection connection = getConnection()) {
            int diaId = obtenerDiaId(connection, fecha);

            // Insertar entrada en la base de datos
            String sql = "INSERT INTO entradas_salidas (empleado_id, dia_id, hora_entrada, tipo_asistencia_id) " +
                    "VALUES (?, ?, ?, 1)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, empleadoId);
                statement.setInt(2, diaId);
                statement.setTime(3, java.sql.Time.valueOf(horaEntrada));
                statement.executeUpdate();
            }
        }
    }

    private static int obtenerDiaId(Connection connection, LocalDate fecha) throws SQLException {
        String sql = "SELECT id FROM dias WHERE fecha = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(fecha));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                // Insertar el día si no existe en la tabla
                String insertDiaSql = "INSERT INTO dias (fecha) VALUES (?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertDiaSql, Statement.RETURN_GENERATED_KEYS)) {
                    insertStatement.setDate(1, java.sql.Date.valueOf(fecha));
                    insertStatement.executeUpdate();
                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("No se pudo insertar el día y obtener su ID.");
                    }
                }
            }
        }
    }

    public static void actualizarSalidaAsistencia(int empleadoId, LocalDate fecha, LocalTime horaSalida) throws SQLException {
        try (Connection connection = getConnection()) {
            int diaId = obtenerDiaId(connection, fecha);

            // Actualizar la salida en la base de datos
            String sql = "UPDATE entradas_salidas SET hora_salida = ? WHERE empleado_id = ? AND dia_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTime(1, java.sql.Time.valueOf(horaSalida));
                statement.setInt(2, empleadoId);
                statement.setInt(3, diaId);
                statement.executeUpdate();
            }
        }
    }

    public static Optional<Map<String, LocalTime>> obtenerHorariosDeAsistencia(int empleadoId, LocalDate fecha) throws SQLException {
        String sql = "SELECT hora_entrada, hora_salida FROM entradas_salidas " +
                "JOIN dias ON entradas_salidas.dia_id = dias.id " +
                "WHERE entradas_salidas.empleado_id = ? AND dias.fecha = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, empleadoId);
            statement.setDate(2, java.sql.Date.valueOf(fecha));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalTime entrada = resultSet.getTime(CAMPO_HORAENTRADA) != null ? resultSet.getTime(CAMPO_HORAENTRADA).toLocalTime() : null;
                LocalTime salida = resultSet.getTime(CAMPO_HORASALIDA) != null ? resultSet.getTime(CAMPO_HORASALIDA).toLocalTime() : null;

                Map<String, LocalTime> horarios = new HashMap<>();
                horarios.put("entrada", entrada);
                horarios.put("salida", salida);

                return Optional.of(horarios);
            }
        }
        return Optional.empty();
    }

//permisos comienzo

    public static List<Map<String, Object>> obtenerEntradasPorEmpleado(int empleadoId) throws SQLException {
        List<Map<String, Object>> entradas = new ArrayList<>();

        // Base de la consulta SQL
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, " +
                "es.nombre AS estado, dias.fecha, es.id AS estado_id, " +
                "en.hora_entrada, en.hora_salida, t.nombre AS tipo_asistencia, " +
                "ts.nombre AS tipo_salida, " +
                "(SELECT l2.details FROM logs l2 " +
                " WHERE l2.target_employee_id = e.id " +
                " AND l2.action = 'Cambio de tipo de asistencia' " +
                " AND DATE(l2.timestamp) = dias.fecha " +
                " ORDER BY l2.timestamp DESC LIMIT 1) AS notas " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "WHERE e.id = ? " + // Filtrar por el ID del empleado
                "ORDER BY dias.fecha DESC, en.hora_entrada DESC"; // Ordenar por fecha y hora de entrada

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Establecer el ID del empleado en la consulta
            statement.setInt(1, empleadoId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " +
                        resultSet.getString("apellido_paterno") + " " +
                        resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha"));
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada"));
                employeeData.put("horaSalida", resultSet.getString("hora_salida"));
                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia"));
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida"));
                employeeData.put("estado", resultSet.getString("estado"));
                employeeData.put("notas", resultSet.getString("notas") != null ? resultSet.getString("notas") : ""); // Si no hay notas, mostrar vacío

                // Calcular el tiempo laborado si hay hora de entrada y salida
                String horaEntrada = resultSet.getString("hora_entrada");
                String horaSalida = resultSet.getString("hora_salida");
                if (horaEntrada != null && horaSalida != null) {
                    employeeData.put("tiempoLaborado", calculateTiempoLaborado(horaEntrada, horaSalida));
                } else {
                    employeeData.put("tiempoLaborado", "N/A");
                }

                entradas.add(employeeData);
            }
        }

        return entradas;
    }

    // Método para calcular el tiempo laborado
    private static String calculateTiempoLaborado(String horaEntrada, String horaSalida) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        try {
            // Parsear las horas de entrada y salida
            LocalTime entrada = LocalTime.parse(horaEntrada, formatter);
            LocalTime salida = LocalTime.parse(horaSalida, formatter);

            // Calcular la duración entre la hora de entrada y la hora de salida
            Duration duracion = Duration.between(entrada, salida);

            // Convertir la duración a horas y minutos
            long horas = duracion.toHours();
            long minutos = duracion.toMinutes() % 60;

            // Devolver la duración en el formato "X horas Y minutos"
            return String.format("%d horas %d minutos", horas, minutos);

        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return "Formato de hora inválido";
        }
    }







}

