package controllers;

import Usuarios.Usuario;

public class AuthResult {
    private final boolean success;
    private final String errorMessage;
    private final Usuario usuario;

    public AuthResult(boolean success, String errorMessage, Usuario usuario) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.usuario = usuario;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}