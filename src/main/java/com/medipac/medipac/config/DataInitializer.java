package com.medipac.medipac.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.medipac.medipac.model.Usuario;
import com.medipac.medipac.model.RolUsuario;
import com.medipac.medipac.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuario administrador por defecto si no existe
        if (!usuarioRepository.findByUsername("admin").isPresent()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@medipac.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(RolUsuario.ADMINISTRADOR.getValor());
            
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario administrador creado:");
            System.out.println("   Usuario: admin");
            System.out.println("   Contraseña: admin123");
            System.out.println("   Email: admin@medipac.com");
        } else {
            System.out.println("ℹ️ Usuario admin ya existe en la base de datos");
        }

        // Crear usuario doctor de prueba si no existe
        if (!usuarioRepository.findByUsername("doctor1").isPresent()) {
            Usuario doctor = new Usuario();
            doctor.setUsername("doctor1");
            doctor.setEmail("doctor@medipac.com");
            doctor.setPassword(passwordEncoder.encode("doctor123"));
            doctor.setRol(RolUsuario.DOCTOR.getValor());
            
            usuarioRepository.save(doctor);
            System.out.println("✅ Usuario doctor creado:");
            System.out.println("   Usuario: doctor1");
            System.out.println("   Contraseña: doctor123");
            System.out.println("   Email: doctor@medipac.com");
        }
    }
}