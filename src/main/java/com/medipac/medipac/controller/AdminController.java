package com.medipac.medipac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medipac.medipac.repository.UsuarioRepository;
import com.medipac.medipac.repository.PacienteRepository;
import com.medipac.medipac.repository.DoctorRepository;
import com.medipac.medipac.repository.EspecialidadRepository;
import com.medipac.medipac.service.AdminService;
import com.medipac.medipac.service.CitaService;
import com.medipac.medipac.model.Usuario;
import com.medipac.medipac.model.Cita;
import com.medipac.medipac.model.EstadoCita;
import com.medipac.medipac.model.Doctor;
import com.medipac.medipac.model.Especialidad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private EspecialidadRepository especialidadRepository;

    // Helper method para obtener el username del admin logueado
    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            AdminService.AdminStats stats = adminService.obtenerEstadisticas();
            List<Usuario> todosUsuarios = adminService.obtenerTodosLosUsuarios();
            
            // Añadir conteo de citas totales al stats
            try {
                List<Cita> todasCitas = citaService.obtenerTodasLasCitas();
                stats.totalCitas = (long) (todasCitas != null ? todasCitas.size() : 0);
            } catch (Exception e) {
                stats.totalCitas = 0L;
            }
            
            model.addAttribute("stats", stats);
            model.addAttribute("usuarios", todosUsuarios);
            model.addAttribute("seccion", "usuarios");
            
            return "admin/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "error/error";
        }
    }

    // Gestión de citas médicas
    @GetMapping("/citas")
    public String gestionCitas(Model model) {
        try {
            String adminActual = getAuthenticatedUsername();
            AdminService.AdminStats stats = adminService.obtenerEstadisticas();
            
            // Obtener todas las citas
            List<Cita> todasCitas = citaService.obtenerTodasLasCitas();
            
            // Calcular estadísticas de citas (manejar caso de lista vacía)
            Map<String, Long> citasStats = new HashMap<>();
            if (todasCitas != null && !todasCitas.isEmpty()) {
                citasStats.put("total", (long) todasCitas.size());
                citasStats.put("pendientes", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count());
                citasStats.put("confirmadas", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.CONFIRMADA).count());
                citasStats.put("completadas", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count());
                citasStats.put("canceladas", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count());
            } else {
                todasCitas = List.of(); // Lista vacía
                citasStats.put("total", 0L);
                citasStats.put("pendientes", 0L);
                citasStats.put("confirmadas", 0L);
                citasStats.put("completadas", 0L);
                citasStats.put("canceladas", 0L);
            }
            
            model.addAttribute("stats", citasStats);
            model.addAttribute("citas", todasCitas);
            model.addAttribute("seccion", "citas");
            
            return "admin/citas";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar las citas: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    // Gestión de usuarios
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        String adminActual = getAuthenticatedUsername();
        AdminService.AdminStats stats = adminService.obtenerEstadisticas();
        List<Usuario> todosUsuarios = adminService.obtenerTodosLosUsuarios(adminActual);
        
        model.addAttribute("stats", stats);
        model.addAttribute("usuarios", todosUsuarios);
        model.addAttribute("seccion", "usuarios");
        
        return "admin/dashboard";
    }

    @GetMapping("/doctores")
    public String doctores(Model model) {
        // Obtener todos los doctores con sus especialidades
        List<Doctor> doctores = doctorRepository.findAll();
        
        // Obtener todas las especialidades disponibles
        List<Especialidad> especialidades = especialidadRepository.findAll();
        
        model.addAttribute("doctores", doctores);
        model.addAttribute("especialidades", especialidades);
        
        return "admin/doctores";
    }

    @GetMapping("/pacientes")
    public String pacientes(Model model) {
        String adminActual = getAuthenticatedUsername();
        AdminService.AdminStats stats = adminService.obtenerEstadisticas();
        List<Usuario> pacientes = adminService.obtenerUsuariosPorRol("PACIENTE", adminActual);
        
        model.addAttribute("stats", stats);
        model.addAttribute("usuarios", pacientes);
        model.addAttribute("seccion", "pacientes");
        
        return "admin/dashboard";
    }

    @GetMapping("/administradores")
    public String administradores(Model model) {
        String adminActual = getAuthenticatedUsername();
        AdminService.AdminStats stats = adminService.obtenerEstadisticas();
        List<Usuario> admins = adminService.obtenerUsuariosPorRol("ADMIN", adminActual);
        List<Usuario> administradores = adminService.obtenerUsuariosPorRol("ADMINISTRADOR", adminActual);
        admins.addAll(administradores);
        
        model.addAttribute("stats", stats);
        model.addAttribute("usuarios", admins);
        model.addAttribute("seccion", "administradores");
        
        return "admin/dashboard";
    }

    @GetMapping("/bloqueados")
    public String bloqueados(Model model) {
        String adminActual = getAuthenticatedUsername();
        AdminService.AdminStats stats = adminService.obtenerEstadisticas();
        List<Usuario> bloqueados = adminService.obtenerUsuariosPorRol("BLOQUEADO", adminActual);
        
        model.addAttribute("stats", stats);
        model.addAttribute("usuarios", bloqueados);
        model.addAttribute("seccion", "bloqueados");
        
        return "admin/dashboard";
    }

    // API endpoints para cambiar roles y estados
    @PostMapping("/cambiar-rol")
    public String cambiarRol(@RequestParam Long usuarioId, 
                            @RequestParam String nuevoRol, 
                            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("🔄 Solicitud cambio de rol - Usuario ID: " + usuarioId + ", Nuevo rol: " + nuevoRol);
            
            if (usuarioId == null || nuevoRol == null || nuevoRol.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Parámetros inválidos");
                return "redirect:/admin/usuarios";
            }
            
            boolean exito = adminService.cambiarRolUsuario(usuarioId, nuevoRol.trim().toUpperCase());
            if (exito) {
                redirectAttributes.addFlashAttribute("success", "Rol cambiado exitosamente a " + nuevoRol);
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo cambiar el rol del usuario");
            }
        } catch (Exception e) {
            System.err.println("❌ Error en cambiarRol: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error interno del servidor");
        }
        
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/cambiar-estado")
    public String cambiarEstado(@RequestParam Long usuarioId, 
                               @RequestParam boolean activo, 
                               RedirectAttributes redirectAttributes) {
        try {
            System.out.println("🔄 Solicitud cambio de estado - Usuario ID: " + usuarioId + ", Activo: " + activo);
            
            if (usuarioId == null) {
                redirectAttributes.addFlashAttribute("error", "ID de usuario inválido");
                return "redirect:/admin/usuarios";
            }
            
            boolean exito = adminService.cambiarEstadoUsuario(usuarioId, activo);
            if (exito) {
                String mensaje = activo ? "Usuario desbloqueado exitosamente" : "Usuario bloqueado exitosamente";
                redirectAttributes.addFlashAttribute("success", mensaje);
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo cambiar el estado del usuario");
            }
        } catch (Exception e) {
            System.err.println("❌ Error en cambiarEstado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error interno del servidor");
        }
        
        return "redirect:/admin/usuarios";
    }

    // Endpoints AJAX dinámicos
    @PostMapping("/cambiar-rol-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarRolAjax(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            String nuevoRol = request.get("nuevoRol").toString().trim().toUpperCase();
            
            System.out.println("🔄 AJAX - Cambio de rol - Usuario ID: " + usuarioId + ", Nuevo rol: " + nuevoRol);
            
            boolean exito = adminService.cambiarRolUsuario(usuarioId, nuevoRol);
            
            if (exito) {
                response.put("success", true);
                response.put("message", "Rol cambiado exitosamente a " + nuevoRol);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "No se pudo cambiar el rol del usuario");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            System.err.println("❌ Error en cambiarRolAjax: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error al cambiar el rol: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cambiar-estado-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarEstadoAjax(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long usuarioId = Long.valueOf(request.get("usuarioId").toString());
            Boolean activo = Boolean.valueOf(request.get("activo").toString());
            
            System.out.println("🔄 AJAX - Cambio de estado - Usuario ID: " + usuarioId + ", Activo: " + activo);
            
            boolean exito = adminService.cambiarEstadoUsuario(usuarioId, activo);
            
            if (exito) {
                response.put("success", true);
                response.put("bloqueado", !activo);
                response.put("message", activo ? "Usuario desbloqueado exitosamente" : "Usuario bloqueado exitosamente");
                
                // Si se desbloquea, intentar obtener el rol por defecto
                if (activo) {
                    response.put("rolAnterior", "PACIENTE"); // Rol por defecto al desbloquear
                }
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "No se pudo cambiar el estado del usuario");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            System.err.println("❌ Error en cambiarEstadoAjax: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error al cambiar el estado: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ============= GESTIÓN DE ESPECIALIDADES DE DOCTORES =============
    
    @PostMapping("/actualizar-especialidad-doctor")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarEspecialidadDoctor(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long doctorId = Long.valueOf(request.get("doctorId").toString());
            Long especialidadId = Long.valueOf(request.get("especialidadId").toString());
            
            System.out.println("🔄 Actualizando especialidad - Doctor ID: " + doctorId + ", Especialidad ID: " + especialidadId);
            
            // Buscar el doctor
            Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
            
            // Buscar la especialidad
            Especialidad especialidad = especialidadRepository.findById(especialidadId)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
            
            // Limpiar especialidades anteriores y asignar la nueva
            Set<Especialidad> especialidades = doctor.getEspecialidades();
            if (especialidades == null) {
                especialidades = new java.util.HashSet<>();
                doctor.setEspecialidades(especialidades);
            }
            especialidades.clear();
            especialidades.add(especialidad);
            
            // Guardar cambios
            doctorRepository.save(doctor);
            
            response.put("success", true);
            response.put("message", "Especialidad actualizada exitosamente");
            response.put("especialidadNombre", especialidad.getNombre());
            
            System.out.println("✅ Especialidad actualizada: " + especialidad.getNombre());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar especialidad: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al actualizar especialidad: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/agregar-especialidad-doctor")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarEspecialidadDoctor(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long doctorId = Long.valueOf(request.get("doctorId").toString());
            Long especialidadId = Long.valueOf(request.get("especialidadId").toString());
            
            System.out.println("➕ Agregando especialidad - Doctor ID: " + doctorId + ", Especialidad ID: " + especialidadId);
            
            // Buscar el doctor
            Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
            
            // Buscar la especialidad
            Especialidad especialidad = especialidadRepository.findById(especialidadId)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
            
            // Agregar especialidad (sin eliminar las existentes)
            Set<Especialidad> especialidades = doctor.getEspecialidades();
            if (especialidades == null) {
                especialidades = new java.util.HashSet<>();
                doctor.setEspecialidades(especialidades);
            }
            especialidades.add(especialidad);
            
            // Guardar cambios
            doctorRepository.save(doctor);
            
            response.put("success", true);
            response.put("message", "Especialidad agregada exitosamente");
            response.put("especialidadNombre", especialidad.getNombre());
            
            System.out.println("✅ Especialidad agregada: " + especialidad.getNombre());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error al agregar especialidad: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al agregar especialidad: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
