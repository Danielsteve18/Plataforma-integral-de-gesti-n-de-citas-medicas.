package com.medipac.medipac.model;

public enum RolUsuario {
    PACIENTE("PACIENTE"),
    DOCTOR("DOCTOR"),
    ADMINISTRADOR("ADMIN");

    private final String valor;

    RolUsuario(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return valor;
    }

    public static RolUsuario fromString(String rol) {
        for (RolUsuario r : RolUsuario.values()) {
            if (r.valor.equals(rol)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol no válido: " + rol);
    }
}