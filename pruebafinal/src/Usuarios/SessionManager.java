package Usuarios;

public class SessionManager {
    private static Usuario currentUser;

    public static void setCurrentUser(Usuario user) {
        currentUser = user;
    }

    public static Usuario getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentSupervisorId() {
        if (currentUser != null && currentUser instanceof Supervisor) {
            return currentUser.getId();
        } else {
            throw new IllegalStateException("No hay un supervisor logueado actualmente.");
        }
    }
}
