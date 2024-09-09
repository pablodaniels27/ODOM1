package Services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import controllers.DatabaseConnection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class CacheService {

    private static Cache<String, List<Map<String, Object>>> employeeCache;

    static {
        employeeCache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    // Método existente para obtener todos los empleados con nombre completo
    public static List<Map<String, Object>> getEmployeesFromDatabaseOrCache() {
        if (isCacheEmpty()) {
            List<Map<String, Object>> employees = new ArrayList<>();
            String query = "SELECT id, nombres, apellido_paterno, apellido_materno, profesion, jerarquia_id FROM empleados";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("id", resultSet.getInt("id"));
                    employeeData.put("nombres", resultSet.getString("nombres"));
                    employeeData.put("apellido_paterno", resultSet.getString("apellido_paterno"));
                    employeeData.put("apellido_materno", resultSet.getString("apellido_materno"));
                    employeeData.put("profession", resultSet.getString("profesion"));
                    employeeData.put("jerarquia_id", resultSet.getInt("jerarquia_id"));
                    employees.add(employeeData);
                }

                // Cargar los datos en el caché
                loadEmployeesIntoCache(employees);

                System.out.println("Empleados cargados en el caché desde la base de datos.");
                return employees;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Empleados obtenidos del caché.");
        return getEmployeesFromCache();
    }

    // Método para obtener empleados solo con nombre y apellido paterno
    public static List<Map<String, Object>> getEmployeesWithPaternalSurname() {
        List<Map<String, Object>> employeesWithPaternalSurname = new ArrayList<>();
        List<Map<String, Object>> employees = getEmployeesFromDatabaseOrCache();

        if (employees != null) {
            for (Map<String, Object> employee : employees) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", employee.get("id"));
                employeeData.put("nombres", employee.get("nombres"));
                employeeData.put("apellido_paterno", employee.get("apellido_paterno"));
                employeeData.put("profession", employee.get("profession"));
                employeeData.put("jerarquia_id", employee.get("jerarquia_id")); // Asegurarse de incluir jerarquia_id
                employeesWithPaternalSurname.add(employeeData);
            }
        } else {
            System.out.println("Error: No se pudo obtener la lista de empleados.");
        }

        return employeesWithPaternalSurname;
    }

    public static boolean isCacheEmpty() {
        return employeeCache.getIfPresent("employees") == null;
    }

    public static void loadEmployeesIntoCache(List<Map<String, Object>> employees) {
        employeeCache.put("employees", employees);
    }

    public static List<Map<String, Object>> getEmployeesFromCache() {
        return employeeCache.getIfPresent("employees");
    }

    public static void invalidateCache() {
        employeeCache.invalidateAll();
    }
}

