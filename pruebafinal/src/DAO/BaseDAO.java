package DAO;

import controllers.AuditoriaController;
import controllers.DatabaseConnection;
import controllers.InicioController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class BaseDAO {

    //Estos metodos controla monitoreo

    public static int obtenerIdTipoAsistencia(String tipoAsistencia) throws SQLException {
        String query = "SELECT id FROM tipos_asistencia WHERE nombre = ?";
        try (Connection connection = DatabaseConnection.getConnection();
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
        String updateQuery = "UPDATE entradas_salidas SET tipo_asistencia_id = ? WHERE empleado_id = ? AND dia_id = (SELECT id FROM dias WHERE fecha = ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            preparedStatement.setInt(1, tipoAsistenciaId);
            preparedStatement.setInt(2, empleadoId);
            preparedStatement.setString(3, fechaEntrada);

            preparedStatement.executeUpdate();
        }
    }

    public static void registrarCambioLog(int supervisorId, String accion, int empleadoId, String detalles) throws SQLException {
        String insertLogQuery = "INSERT INTO logs (supervisor_id, action, target_employee_id, details) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertLogQuery)) {

            preparedStatement.setInt(1, supervisorId);
            preparedStatement.setString(2, accion);
            preparedStatement.setInt(3, empleadoId);
            preparedStatement.setString(4, detalles);

            preparedStatement.executeUpdate();
        }
    }

    public static List<String> obtenerDepartamentos() throws SQLException {
        String query = "SELECT nombre FROM departamentos";
        List<String> departamentos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                departamentos.add(resultSet.getString("nombre"));
            }
        }

        return departamentos;
    }

    public static List<Map<String, Object>> obtenerTodasLasEntradas() throws SQLException {
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida, " +
                "l.details as notas " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "LEFT JOIN logs l ON l.target_employee_id = e.id " +
                "AND l.action = 'Cambio de tipo de asistencia' " +
                "ORDER BY dias.fecha DESC, en.hora_entrada DESC";

        List<Map<String, Object>> entries = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
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

    public static List<Map<String, Object>> buscarPorFechaYDepartamento(String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados, String fechaInicio, String fechaFin) throws SQLException {
        String query = "SELECT e.id, e.nombres, e.apellido_paterno, e.apellido_materno, es.nombre as estado, " +
                "dias.fecha, es.id as estado_id, en.hora_entrada, en.hora_salida, t.nombre as tipo_asistencia, ts.nombre as tipo_salida, " +
                "l.details as notas " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN estatus_empleado es ON e.estatus_id = es.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN tipos_salida ts ON en.tipo_salida_id = ts.id " +
                "LEFT JOIN logs l ON l.target_employee_id = e.id AND l.action = 'Cambio de tipo de asistencia' " +
                "WHERE dias.fecha BETWEEN ? AND ? ";

        // Filtro opcional por departamento (solo si no es "Todos los departamentos")
        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            query += "AND e.departamento_id IN (SELECT id FROM departamentos WHERE nombre = ?) ";
        }

        // Filtros para supervisores o empleados
        if (incluirSupervisores || incluirEmpleados) {
            query += "AND (";
            if (incluirSupervisores) {
                query += "e.jerarquia_id = 2 ";  // Supervisores
            }
            if (incluirSupervisores && incluirEmpleados) {
                query += "OR ";
            }
            if (incluirEmpleados) {
                query += "e.jerarquia_id = 3 ";  // Empleados
            }
            query += ") ";
        }

        // Filtro por búsqueda en nombre completo (concatenando nombres y apellidos)
        if (!searchQuery.isEmpty()) {
            query += "AND CONCAT(LOWER(TRIM(e.nombres)), ' ', LOWER(TRIM(e.apellido_paterno)), ' ', LOWER(TRIM(e.apellido_materno))) LIKE ? ";
        }

        query += "ORDER BY dias.fecha ASC";  // Ordenar por fecha

        List<Map<String, Object>> searchResults = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            int paramIndex = 1;

            // Siempre asignamos los parámetros de fecha
            preparedStatement.setString(paramIndex++, fechaInicio);
            preparedStatement.setString(paramIndex++, fechaFin);

            // Si se filtra por departamento
            if (!departamentoSeleccionado.equals("Todos los departamentos")) {
                preparedStatement.setString(paramIndex++, departamentoSeleccionado);
            }

            // Si hay un valor de búsqueda, asignamos el patrón de búsqueda
            if (!searchQuery.isEmpty()) {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                preparedStatement.setString(paramIndex++, searchPattern);
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", String.valueOf(resultSet.getInt("id")));
                employeeData.put("nombreCompleto", resultSet.getString("nombres") + " " + resultSet.getString("apellido_paterno") + " " + resultSet.getString("apellido_materno"));
                employeeData.put("fechaEntrada", resultSet.getString("fecha"));
                employeeData.put("horaEntrada", resultSet.getString("hora_entrada"));
                employeeData.put("horaSalida", resultSet.getString("hora_salida"));
                employeeData.put("tipoAsistencia", resultSet.getString("tipo_asistencia"));
                employeeData.put("tipoSalida", resultSet.getString("tipo_salida"));
                employeeData.put("estado", resultSet.getString("estado"));
                employeeData.put("notas", resultSet.getString("notas") != null ? resultSet.getString("notas") : ""); // Si no hay notas, mostrar vacío

                searchResults.add(employeeData);
            }
        }

        return searchResults;
    }

    public static Set<String> buscarNombresPorAsistencia(String departamento, String tipoAsistencia, String fechaInicio, String fechaFin, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados) throws SQLException {
        String query = "SELECT e.nombres, e.apellido_paterno, e.apellido_materno " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "JOIN dias ON en.dia_id = dias.id " + // Relacionar con la tabla de días para el rango de fechas
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "WHERE d.nombre = ? AND t.nombre = ? " +  // Filtrar por departamento y tipo de asistencia
                "AND dias.fecha BETWEEN ? AND ? ";

        // Añadir condiciones adicionales para supervisores o empleados
        if (incluirSupervisores || incluirEmpleados) {
            query += " AND (";
            if (incluirSupervisores) {
                query += "e.jerarquia_id = 2";
            }
            if (incluirSupervisores && incluirEmpleados) {
                query += " OR ";
            }
            if (incluirEmpleados) {
                query += "e.jerarquia_id = 3";
            }
            query += ")";
        }

        // Si hay una búsqueda, agregar el filtro al query para buscar por nombre, apellido o nombre completo
        if (searchQuery != null && !searchQuery.isEmpty()) {
            query += " AND (e.nombres LIKE ? OR e.apellido_paterno LIKE ? OR e.apellido_materno LIKE ? OR CONCAT(e.nombres, ' ', e.apellido_paterno, ' ', e.apellido_materno) LIKE ?)";
        }

        Set<String> empleadosUnicos = new HashSet<>();

        try (Connection connectDB = DatabaseConnection.getConnection();
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
                preparedStatement.setString(paramIndex++, searchPattern); // CONCAT(...) LIKE ?
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String nombreCompleto = resultSet.getString("nombres") + " " +
                        resultSet.getString("apellido_paterno") + " " +
                        resultSet.getString("apellido_materno");

                // Agregar al Set, que no permitirá duplicados
                empleadosUnicos.add(nombreCompleto);
            }
        }

        return empleadosUnicos;
    }

    public static Set<String> buscarFechasPorEmpleado(String departamento, String tipoAsistencia, String nombreEmpleado, String fechaInicio, String fechaFin) throws SQLException {
        String query = "SELECT dias.fecha " +
                "FROM entradas_salidas en " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "JOIN dias ON en.dia_id = dias.id " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "WHERE t.nombre = ? " + // Filtro por tipo de asistencia
                "AND (e.nombres = ? AND e.apellido_paterno = ? AND e.apellido_materno = ?) " + // Filtrar por nombre completo
                "AND dias.fecha BETWEEN ? AND ?";

        // Si el departamento no es "Todos los departamentos", agregar el filtro de departamento
        if (!departamento.equals("Todos los departamentos")) {
            query += " AND d.nombre = ?";
        }

        Set<String> fechasUnicas = new HashSet<>();

        try (Connection connectDB = DatabaseConnection.getConnection();
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
            if (!departamento.equals("Todos los departamentos")) {
                preparedStatement.setString(7, departamento);
            }

            System.out.println("Parámetros asignados: ");
            System.out.println("Departamento: " + (departamento.equals("Todos los departamentos") ? "Todos" : departamento));
            System.out.println("Tipo de Asistencia: " + tipoAsistencia);
            System.out.println("Nombre del Empleado: " + nombreEmpleado);
            System.out.println("Fecha Inicio: " + fechaInicio);
            System.out.println("Fecha Fin: " + fechaFin);

            ResultSet resultSet = preparedStatement.executeQuery();
            int counter = 0;
            while (resultSet.next()) {
                String fecha = resultSet.getString("fecha");
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
        StringBuilder query = new StringBuilder("SELECT CONCAT(TRIM(nombres), ' ', TRIM(apellido_paterno), ' ', TRIM(apellido_materno)) AS nombreCompleto, jerarquia_id, d.nombre as departamento ");
        query.append("FROM empleados e ");
        query.append("JOIN departamentos d ON e.departamento_id = d.id ");
        query.append("WHERE ");

        // Filtrar por nombre completo usando CONCAT en lugar de buscar por partes
        query.append("CONCAT(LOWER(TRIM(nombres)), ' ', LOWER(TRIM(apellido_paterno)), ' ', LOWER(TRIM(apellido_materno))) LIKE ? ");

        // Filtros para supervisores o empleados
        if (incluirSupervisores || incluirEmpleados) {
            query.append("AND (");
            if (incluirSupervisores) {
                query.append("jerarquia_id = 2");  // Supervisores
            }
            if (incluirSupervisores && incluirEmpleados) {
                query.append(" OR ");
            }
            if (incluirEmpleados) {
                query.append("jerarquia_id = 3");  // Empleados
            }
            query.append(") ");
        }

        // Filtrar por departamento, si no se seleccionó "Todos los departamentos"
        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            query.append("AND d.nombre = ? ");
        }

        query.append("ORDER BY nombreCompleto ASC");

        ObservableList<String> results = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

            int paramIndex = 1;

            // Crear el patrón de búsqueda
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";

            // Asignar el patrón de búsqueda
            preparedStatement.setString(paramIndex++, searchPattern);

            // Si el departamento seleccionado no es "Todos los departamentos", agregarlo como parámetro
            if (!departamentoSeleccionado.equals("Todos los departamentos")) {
                preparedStatement.setString(paramIndex++, departamentoSeleccionado);
            }

            // Ejecutar la consulta
            ResultSet resultSet = preparedStatement.executeQuery();

            // Procesar los resultados
            while (resultSet.next()) {
                String nombreCompleto = resultSet.getString("nombreCompleto");
                int jerarquiaId = resultSet.getInt("jerarquia_id");
                String departamento = resultSet.getString("departamento");

                results.add(nombreCompleto + "," + jerarquiaId + "," + departamento);  // Añadir el nombre completo, jerarquía, y departamento a los resultados
            }
        }

        return results;
    }
    // aqui se cierra monitoreo  ////////////////////////////////////////////////////

    //GRAFICOSCONTROLLER

    public static List<Map<String, Object>> obtenerConteoTiposAsistencia(String fechaInicio, String fechaFin, String departamentoSeleccionado, String searchQuery, boolean incluirSupervisores, boolean incluirEmpleados) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();

        String query = "SELECT d.nombre AS departamento, t.nombre AS tipo_asistencia, COUNT(*) AS cantidad " +
                "FROM entradas_salidas en " +
                "JOIN tipos_asistencia t ON en.tipo_asistencia_id = t.id " +
                "JOIN empleados e ON en.empleado_id = e.id " +
                "JOIN departamentos d ON e.departamento_id = d.id " +
                "JOIN dias di ON en.dia_id = di.id " +
                "WHERE di.fecha BETWEEN ? AND ? ";

        if (!departamentoSeleccionado.equals("Todos los departamentos")) {
            query += "AND d.nombre = ? ";
        }

        // Modificación para buscar por nombre completo o partes del nombre
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            query += "AND (LOWER(e.nombres) LIKE ? OR LOWER(e.apellido_paterno) LIKE ? OR LOWER(e.apellido_materno) LIKE ? " +
                    "OR CONCAT(LOWER(e.nombres), ' ', LOWER(e.apellido_paterno), ' ', LOWER(e.apellido_materno)) LIKE ?) ";
        }

        if (incluirSupervisores && !incluirEmpleados) {
            query += "AND e.jerarquia_id = 2 ";  // Solo supervisores
        } else if (incluirEmpleados && !incluirSupervisores) {
            query += "AND e.jerarquia_id = 3 ";  // Solo empleados
        }

        query += "GROUP BY d.nombre, t.nombre";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Asignar parámetros
            preparedStatement.setString(1, fechaInicio);
            preparedStatement.setString(2, fechaFin);

            int paramIndex = 3;
            if (!departamentoSeleccionado.equals("Todos los departamentos")) {
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
                resultado.put("departamento", resultSet.getString("departamento"));
                resultado.put("tipo_asistencia", resultSet.getString("tipo_asistencia"));
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

        String query = "SELECT dia.fecha, es.hora_entrada, es.hora_salida " +
                "FROM entradas_salidas es " +
                "JOIN dias dia ON es.dia_id = dia.id " +
                "WHERE es.empleado_id = ? AND dia.fecha BETWEEN ? AND ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, empleadoId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(fechaInicio));
            preparedStatement.setDate(3, java.sql.Date.valueOf(fechaFin));

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("fecha", resultSet.getDate("fecha"));
                resultado.put("hora_entrada", resultSet.getString("hora_entrada"));
                resultado.put("hora_salida", resultSet.getString("hora_salida"));
                resultados.add(resultado);
            }
        }

        return resultados;
    }

    public static List<InicioController.Employee> obtenerEmpleados(String filter) throws SQLException {
        List<InicioController.Employee> employees = new ArrayList<>();
        String query = "SELECT id, CONCAT(nombres, ' ', apellido_paterno, ' ', apellido_materno) AS full_name, profesion FROM empleados";

        // Si hay un filtro, añadir la condición a la consulta SQL
        if (!filter.isEmpty()) {
            query += " WHERE CONCAT(nombres, ' ', apellido_paterno, ' ', apellido_materno) LIKE ?";
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (!filter.isEmpty()) {
                preparedStatement.setString(1, "%" + filter + "%");
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String fullName = resultSet.getString("full_name");
                String profession = resultSet.getString("profesion");

                employees.add(new InicioController.Employee(id, fullName, profession));
            }
        }

        return employees;
    }

    //Aqui acaba inicioController/////////////////
    //AuditoriaController

    public static ObservableList<String> buscarSupervisores(String searchQuery) throws SQLException {
        String query = "SELECT DISTINCT CONCAT(esuper.nombres, ' ', esuper.apellido_paterno, ' ', esuper.apellido_materno) AS supervisorNombre " +
                "FROM logs l " +
                "JOIN empleados esuper ON l.supervisor_id = esuper.id " +
                "WHERE CONCAT(LOWER(TRIM(esuper.nombres)), ' ', LOWER(TRIM(esuper.apellido_paterno)), ' ', LOWER(TRIM(esuper.apellido_materno))) LIKE ? " +
                "ORDER BY supervisorNombre ASC";

        ObservableList<String> results = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            String searchPattern = "%" + searchQuery.toLowerCase() + "%";
            preparedStatement.setString(1, searchPattern);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String supervisorNombre = resultSet.getString("supervisorNombre");
                results.add(supervisorNombre);
            }
        }

        return results;
    }

    public static List<AuditoriaController.Auditoria> obtenerDatosAuditoria() throws SQLException {
        String query = "SELECT l.supervisor_id, l.action, l.target_employee_id, l.timestamp, l.details, " +
                "esuper.nombres AS supervisor_nombres, esuper.apellido_paterno AS supervisor_apellido_paterno, esuper.apellido_materno AS supervisor_apellido_materno, " +
                "etarget.nombres AS empleado_nombres, etarget.apellido_paterno AS empleado_apellido_paterno, etarget.apellido_materno AS empleado_apellido_materno, " +
                "d.nombre AS departamento_nombre " +
                "FROM logs l " +
                "JOIN empleados esuper ON l.supervisor_id = esuper.id " +
                "JOIN empleados etarget ON l.target_employee_id = etarget.id " +
                "JOIN departamentos d ON etarget.departamento_id = d.id " +
                "ORDER BY l.timestamp DESC";

        List<AuditoriaController.Auditoria> auditoriaList = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
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
                String detalles = resultSet.getString("details");

                auditoriaList.add(new AuditoriaController.Auditoria(nombreCompletoEmpleado, departamentoNombre, accion, nombreCompletoSupervisor, timestamp, detalles));
            }
        }

        return auditoriaList;
    }







}
