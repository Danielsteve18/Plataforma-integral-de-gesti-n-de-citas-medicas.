package com.medipac.medipac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medipac.medipac.model.Usuario;
import com.medipac.medipac.model.Doctor;
import com.medipac.medipac.model.Especialidad;
import com.medipac.medipac.repository.UsuarioRepository;
import com.medipac.medipac.repository.DoctorRepository;
import com.medipac.medipac.repository.EspecialidadRepository;

import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AdminService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private EspecialidadRepository especialidadRepository;

    // Obtener todos los usuarios activos
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Obtener todos los usuarios excluyendo al administrador actual
    public List<Usuario> obtenerTodosLosUsuarios(String adminActual) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .filter(usuario -> !usuario.getUsername().equals(adminActual))
                .collect(java.util.stream.Collectors.toList());
    }

    // Obtener usuarios por rol
    public List<Usuario> obtenerUsuariosPorRol(String rol) {
        return usuarioRepository.findByRol(rol);
    }

    // Obtener usuarios por rol excluyendo al administrador actual
    public List<Usuario> obtenerUsuariosPorRol(String rol, String adminActual) {
        List<Usuario> usuarios = usuarioRepository.findByRol(rol);
        return usuarios.stream()
                .filter(usuario -> !usuario.getUsername().equals(adminActual))
                .collect(java.util.stream.Collectors.toList());
    }

    // Cambiar rol de usuario
    public boolean cambiarRolUsuario(Long usuarioId, String nuevoRol) {
        try {
            // Validar que el rol sea válido
            if (!esRolValido(nuevoRol)) {
                System.err.println("❌ Rol inválido: " + nuevoRol);
                return false;
            }
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String rolAnterior = usuario.getRol();
                usuario.setRol(nuevoRol);
                usuarioRepository.save(usuario);
                System.out.println("✅ Rol cambiado para usuario " + usuario.getUsername() + " de " + rolAnterior + " a: " + nuevoRol);
                
                // Si el nuevo rol es DOCTOR, crear registro de doctor con especialidad por defecto
                if ("DOCTOR".equals(nuevoRol) && !"DOCTOR".equals(rolAnterior)) {
                    crearDoctorConEspecialidadPorDefecto(usuario);
                }
                
                return true;
            }
            System.err.println("❌ Usuario no encontrado con ID: " + usuarioId);
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error al cambiar rol del usuario " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Crear registro de doctor con especialidad "Medicina General" por defecto
    private void crearDoctorConEspecialidadPorDefecto(Usuario usuario) {
        try {
            // Verificar si ya existe un registro de doctor para este usuario
            Optional<Doctor> doctorExistente = doctorRepository.findByUsuarioId(usuario.getId());
            if (doctorExistente.isPresent()) {
                System.out.println("ℹ️ El usuario ya tiene registro de doctor");
                return;
            }
            
            // Buscar o crear la especialidad "Medicina General"
            Especialidad medicinaGeneral = especialidadRepository.findByNombre("Medicina General")
                .orElseGet(() -> {
                    Especialidad nueva = new Especialidad("Medicina General", "Atención médica general y consultas básicas");
                    return especialidadRepository.save(nueva);
                });
            
            // Crear el registro de doctor
            Doctor nuevoDoctor = new Doctor();
            nuevoDoctor.setUsuario(usuario);
            nuevoDoctor.setNombre(usuario.getUsername()); // Usar username como nombre temporal
            nuevoDoctor.setApellido(""); // Vacío por defecto
            nuevoDoctor.setNumeroLicencia("LIC-" + usuario.getId()); // Generar licencia temporal
            
            // Asignar especialidad
            Set<Especialidad> especialidades = new HashSet<>();
            especialidades.add(medicinaGeneral);
            nuevoDoctor.setEspecialidades(especialidades);
            
            doctorRepository.save(nuevoDoctor);
            System.out.println("✅ Registro de doctor creado con especialidad 'Medicina General' para usuario: " + usuario.getUsername());
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear registro de doctor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Validar si un rol es válido
    private boolean esRolValido(String rol) {
        return rol != null && (
            rol.equals("PACIENTE") || 
            rol.equals("DOCTOR") || 
            rol.equals("ADMIN") || 
            rol.equals("ADMINISTRADOR") ||
            rol.equals("BLOQUEADO")
        );
    }

    // Bloquear/desbloquear usuario (usando el rol)
    public boolean cambiarEstadoUsuario(Long usuarioId, boolean activo) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                if (!activo) {
                    // Bloquear usuario: cambiar rol a BLOQUEADO
                    usuario.setRol("BLOQUEADO");
                } else {
                    // Desbloquear usuario: restaurar a PACIENTE por defecto
                    // Nota: En un sistema real, deberíamos guardar el rol anterior
                    usuario.setRol("PACIENTE");
                }
                
                usuarioRepository.save(usuario);
                System.out.println("✅ Estado cambiado para usuario " + usuario.getUsername() + " a: " + (activo ? "ACTIVO (PACIENTE)" : "BLOQUEADO"));
                return true;
            }
            System.err.println("❌ Usuario no encontrado con ID: " + usuarioId);
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error al cambiar estado del usuario " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Obtener estadísticas
    public AdminStats obtenerEstadisticas() {
        long totalUsuarios = usuarioRepository.count();
        long totalAdmins = usuarioRepository.countByRol("ADMIN") + usuarioRepository.countByRol("ADMINISTRADOR");
        long totalDoctores = usuarioRepository.countByRol("DOCTOR");
        long totalPacientes = usuarioRepository.countByRol("PACIENTE");
        long totalBloqueados = usuarioRepository.countByRol("BLOQUEADO");

        return new AdminStats(totalUsuarios, totalAdmins, totalDoctores, totalPacientes, totalBloqueados);
    }

    // Clase interna para estadísticas
    public static class AdminStats {
        private long totalUsuarios;
        private long totalAdmins;
        private long totalDoctores;
        private long totalPacientes;
        private long totalBloqueados;
        public long totalCitas; // Campo público para facilitar el acceso

        public AdminStats(long totalUsuarios, long totalAdmins, long totalDoctores, long totalPacientes, long totalBloqueados) {
            this.totalUsuarios = totalUsuarios;
            this.totalAdmins = totalAdmins;
            this.totalDoctores = totalDoctores;
            this.totalPacientes = totalPacientes;
            this.totalBloqueados = totalBloqueados;
            this.totalCitas = 0; // Inicializar en 0
        }

        // Getters
        public long getTotalUsuarios() { return totalUsuarios; }
        public long getTotalAdmins() { return totalAdmins; }
        public long getTotalDoctores() { return totalDoctores; }
        public long getTotalPacientes() { return totalPacientes; }
        public long getTotalBloqueados() { return totalBloqueados; }
        public long getTotalCitas() { return totalCitas; }
    }
}