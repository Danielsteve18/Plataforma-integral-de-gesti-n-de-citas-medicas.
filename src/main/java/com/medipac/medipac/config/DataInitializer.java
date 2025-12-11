package com.medipac.medipac.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.medipac.medipac.model.Usuario;
import com.medipac.medipac.model.RolUsuario;
import com.medipac.medipac.model.Especialidad;
import com.medipac.medipac.repository.UsuarioRepository;
import com.medipac.medipac.repository.EspecialidadRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EspecialidadRepository especialidadRepository;

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

        // Inicializar especialidades médicas si no existen
        inicializarEspecialidades();
    }

    private void inicializarEspecialidades() {
        String[][] especialidades = {
            {"Cardiología", "Especialidad médica que se encarga del corazón y el sistema circulatorio. Diagnóstico y tratamiento de enfermedades cardiovasculares."},
            {"Dermatología", "Especialidad médica enfocada en el cuidado de la piel, cabello y uñas. Tratamiento de enfermedades cutáneas y estéticas."},
            {"Endocrinología", "Especialidad que trata las enfermedades relacionadas con las hormonas y el metabolismo, como diabetes y trastornos tiroideos."},
            {"Gastroenterología", "Especialidad médica del aparato digestivo. Diagnóstico y tratamiento de enfermedades del estómago, intestinos, hígado y páncreas."},
            {"Ginecología", "Especialidad médica y quirúrgica que trata la salud del sistema reproductor femenino."},
            {"Medicina General", "Atención primaria integral para pacientes de todas las edades. Diagnóstico y tratamiento de enfermedades comunes."},
            {"Neurología", "Especialidad médica que trata los trastornos del sistema nervioso central y periférico."},
            {"Oftalmología", "Especialidad médica que se encarga del diagnóstico y tratamiento de las enfermedades de los ojos."},
            {"Oncología", "Especialidad médica dedicada al diagnóstico y tratamiento del cáncer."},
            {"Ortopedia", "Especialidad médica que trata las enfermedades y lesiones del sistema musculoesquelético."},
            {"Otorrinolaringología", "Especialidad médica que trata las enfermedades del oído, nariz y garganta."},
            {"Pediatría", "Especialidad médica dedicada al cuidado de la salud de bebés, niños y adolescentes."},
            {"Psiquiatría", "Especialidad médica que se ocupa del diagnóstico, prevención y tratamiento de trastornos mentales."},
            {"Psicología", "Especialidad que estudia el comportamiento humano y los procesos mentales. Terapia y apoyo psicológico."},
            {"Neumología", "Especialidad médica que se encarga del diagnóstico y tratamiento de enfermedades del sistema respiratorio."},
            {"Urología", "Especialidad médica que trata las enfermedades del tracto urinario y sistema reproductor masculino."},
            {"Traumatología", "Especialidad médica que trata las lesiones traumáticas del sistema musculoesquelético."},
            {"Radiología", "Especialidad médica que utiliza técnicas de imagen para el diagnóstico y tratamiento de enfermedades."},
            {"Anestesiología", "Especialidad médica dedicada a la atención y cuidado de los pacientes durante procedimientos quirúrgicos."},
            {"Medicina Interna", "Especialidad médica que se dedica a la atención integral del adulto enfermo, enfocándose en el diagnóstico y tratamiento no quirúrgico."}
        };

        int creadas = 0;
        int existentes = 0;

        for (String[] esp : especialidades) {
            String nombre = esp[0];
            String descripcion = esp[1];

            // Verificar si la especialidad ya existe
            if (!especialidadRepository.findByNombre(nombre).isPresent()) {
                Especialidad especialidad = new Especialidad(nombre, descripcion);
                especialidadRepository.save(especialidad);
                creadas++;
            } else {
                existentes++;
            }
        }

        if (creadas > 0) {
            System.out.println("✅ Especialidades médicas inicializadas:");
            System.out.println("   Nuevas especialidades creadas: " + creadas);
        }
        if (existentes > 0) {
            System.out.println("ℹ️ Especialidades ya existentes: " + existentes);
        }
        if (creadas == 0 && existentes == 0) {
            System.out.println("ℹ️ No se inicializaron especialidades");
        }
    }
}