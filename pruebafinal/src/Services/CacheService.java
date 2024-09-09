package Services;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CacheService {

    private static Cache<String, List<Map<String, Object>>> employeeCache;

    // Inicializa el caché de empleados con un tiempo de expiración
    static {
        employeeCache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES) // Configura el caché para que expire después de 60 minutos
                .maximumSize(100)  // Limita el tamaño máximo de registros en el caché
                .build();
    }

    // Método para obtener los empleados desde el caché
    public static List<Map<String, Object>> getEmployeesFromCache() {
        return employeeCache.getIfPresent("employees");
    }

    // Método para guardar la lista de empleados en el caché
    public static void loadEmployeesIntoCache(List<Map<String, Object>> employees) {
        employeeCache.put("employees", employees);
    }

    // Método para verificar si el caché está vacío
    public static boolean isCacheEmpty() {
        return employeeCache.getIfPresent("employees") == null;
    }

    // Método para invalidar el caché si es necesario actualizarlo manualmente
    public static void invalidateCache() {
        employeeCache.invalidateAll();
    }
}
