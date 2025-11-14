package com.medipac.medipac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.medipac.medipac.model.Usuario;
import com.medipac.medipac.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioValidationService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Valida si un username es válido (no existe uno igual o similar)
     */
    public ValidationResult validarUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "El nombre de usuario no puede estar vacío");
        }

        // Verificar si existe exactamente igual (case-insensitive)
        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return new ValidationResult(false, "Ya existe un usuario con este nombre");
        }

        // Verificar usuarios similares
        List<Usuario> similares = usuarioRepository.findByUsernameSimilar(username);
        if (!similares.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("Usuarios similares encontrados: ");
            for (int i = 0; i < Math.min(similares.size(), 3); i++) {
                mensaje.append(similares.get(i).getUsername());
                if (i < Math.min(similares.size(), 3) - 1) {
                    mensaje.append(", ");
                }
            }
            return new ValidationResult(false, mensaje.toString());
        }

        return new ValidationResult(true, "Nombre de usuario válido");
    }

    /**
     * Valida si un email es válido (no existe uno igual o similar)
     */
    public ValidationResult validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "El email no puede estar vacío");
        }

        // Validación básica de formato email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return new ValidationResult(false, "El formato del email no es válido");
        }

        // Verificar si existe exactamente igual (case-insensitive)
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            return new ValidationResult(false, "Ya existe un usuario con este email");
        }

        // Verificar emails similares
        String dominio = email.substring(email.indexOf("@"));
        String nombreLocal = email.substring(0, email.indexOf("@"));
        
        List<Usuario> similares = usuarioRepository.findByEmailSimilar(nombreLocal);
        if (!similares.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("Emails similares encontrados: ");
            for (int i = 0; i < Math.min(similares.size(), 3); i++) {
                mensaje.append(similares.get(i).getEmail());
                if (i < Math.min(similares.size(), 3) - 1) {
                    mensaje.append(", ");
                }
            }
            return new ValidationResult(false, mensaje.toString());
        }

        return new ValidationResult(true, "Email válido");
    }

    /**
     * Valida tanto username como email
     */
    public ValidationResult validarUsuarioCompleto(String username, String email) {
        ValidationResult usernameResult = validarUsername(username);
        if (!usernameResult.isValid()) {
            return usernameResult;
        }

        ValidationResult emailResult = validarEmail(email);
        if (!emailResult.isValid()) {
            return emailResult;
        }

        return new ValidationResult(true, "Usuario válido");
    }

    /**
     * Clase para encapsular el resultado de la validación
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}