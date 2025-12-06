package com.medipac.medipac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    // Helper method para verificar que el usuario actual es admin
    private boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                          a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model) {
        try {
            AdminService.AdminStats stats = adminService.obtenerEstadisticas();
            
            // Obtener estadísticas de citas
            Map<String, Long> citasStats = new HashMap<>();
            try {
                List<Cita> todasCitas = citaService.obtenerTodasLasCitas();
                stats.totalCitas = (long) (todasCitas != null ? todasCitas.size() : 0);
                
                if (todasCitas != null && !todasCitas.isEmpty()) {
                    citasStats.put("total", (long) todasCitas.size());
                    citasStats.put("pendientes", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count());
                    citasStats.put("confirmadas", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.CONFIRMADA).count());
                    citasStats.put("completadas", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count());
                    citasStats.put("canceladas", todasCitas.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count());
                    
                    // Obtener citas recientes (últimas 5)
                    List<Cita> citasRecientes = todasCitas.stream()
                        .sorted((c1, c2) -> c2.getFechaCreacion().compareTo(c1.getFechaCreacion()))
                        .limit(5)
                        .collect(java.util.stream.Collectors.toList());
                    
                    // Inicializar relaciones lazy para citas recientes
                    for (Cita cita : citasRecientes) {
                        if (cita.getPaciente() != null) {
                            org.hibernate.Hibernate.initialize(cita.getPaciente());
                            if (cita.getPaciente().getUsuario() != null) {
                                org.hibernate.Hibernate.initialize(cita.getPaciente().getUsuario());
                            }
                        }
                        if (cita.getDoctor() != null) {
                            org.hibernate.Hibernate.initialize(cita.getDoctor());
                            if (cita.getDoctor().getUsuario() != null) {
                                org.hibernate.Hibernate.initialize(cita.getDoctor().getUsuario());
                            }
                        }
                    }
                    
                    model.addAttribute("citasRecientes", citasRecientes);
                } else {
                    citasStats.put("total", 0L);
                    citasStats.put("pendientes", 0L);
                    citasStats.put("confirmadas", 0L);
                    citasStats.put("completadas", 0L);
                    citasStats.put("canceladas", 0L);
                    model.addAttribute("citasRecientes", List.of());
                }
            } catch (Exception e) {
                stats.totalCitas = 0L;
                citasStats.put("total", 0L);
                citasStats.put("pendientes", 0L);
                citasStats.put("confirmadas", 0L);
                citasStats.put("completadas", 0L);
                citasStats.put("canceladas", 0L);
                model.addAttribute("citasRecientes", List.of());
            }
            
            // Obtener usuarios recientes (últimos 5)
            List<Usuario> usuariosRecientes = adminService.obtenerTodosLosUsuarios().stream()
                .sorted((u1, u2) -> u2.getFechaCreacion().compareTo(u1.getFechaCreacion()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
            
            // Obtener conteo de especialidades
            long totalEspecialidades = especialidadRepository.count();
            
            model.addAttribute("stats", stats);
            model.addAttribute("citasStats", citasStats);
            model.addAttribute("usuariosRecientes", usuariosRecientes);
            model.addAttribute("totalEspecialidades", totalEspecialidades);
            model.addAttribute("seccion", null); // null para mostrar el dashboard
            
            return "admin/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "error/error";
        }
    }

    // Gestión de citas médicas
    @GetMapping("/citas")
    @Transactional(readOnly = true)
    public String gestionCitas(Model model) {
        try {
            String adminActual = getAuthenticatedUsername();
            AdminService.AdminStats stats = adminService.obtenerEstadisticas();
            
            // Obtener todas las citas
            List<Cita> todasCitas = citaService.obtenerTodasLasCitas();
            
            // Inicializar relaciones lazy para evitar LazyInitializationException
            if (todasCitas != null && !todasCitas.isEmpty()) {
                for (Cita cita : todasCitas) {
                    // Inicializar paciente y su usuario
                    if (cita.getPaciente() != null) {
                        org.hibernate.Hibernate.initialize(cita.getPaciente());
                        if (cita.getPaciente().getUsuario() != null) {
                            org.hibernate.Hibernate.initialize(cita.getPaciente().getUsuario());
                        }
                    }
                    // Inicializar doctor, su usuario y especialidades
                    if (cita.getDoctor() != null) {
                        org.hibernate.Hibernate.initialize(cita.getDoctor());
                        if (cita.getDoctor().getUsuario() != null) {
                            org.hibernate.Hibernate.initialize(cita.getDoctor().getUsuario());
                        }
                        if (cita.getDoctor().getEspecialidades() != null) {
                            org.hibernate.Hibernate.initialize(cita.getDoctor().getEspecialidades());
                        }
                    }
                }
            }
            
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
    @Transactional(readOnly = true)
    public String doctores(Model model) {
        // Obtener todos los doctores con sus especialidades
        List<Doctor> doctores = doctorRepository.findAll();
        
        // Inicializar las especialidades para evitar LazyInitializationException
        for (Doctor doctor : doctores) {
            org.hibernate.Hibernate.initialize(doctor.getEspecialidades());
        }
        
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
        // Verificar que solo admins pueden acceder
        if (!esAdmin()) {
            model.addAttribute("error", "Acceso denegado. Solo administradores pueden acceder a esta sección.");
            return "error/error";
        }
        
        String adminActual = getAuthenticatedUsername();
        AdminService.AdminStats stats = adminService.obtenerEstadisticas();
        List<Usuario> admins = adminService.obtenerUsuariosPorRol("ADMIN", adminActual);
        List<Usuario> administradores = adminService.obtenerUsuariosPorRol("ADMINISTRADOR", adminActual);
        admins.addAll(administradores);
        
        model.addAttribute("stats", stats);
        model.addAttribute("administradores", admins);
        model.addAttribute("seccion", "administradores");
        
        return "admin/administradores";
    }

    @GetMapping("/bloqueados")
    public String bloqueados(Model model) {
        // Verificar que solo admins pueden acceder
        if (!esAdmin()) {
            model.addAttribute("error", "Acceso denegado. Solo administradores pueden acceder a esta sección.");
            return "error/error";
        }
        
        String adminActual = getAuthenticatedUsername();
        AdminService.AdminStats stats = adminService.obtenerEstadisticas();
        
        // Obtener usuarios bloqueados
        List<Usuario> usuariosBloqueados = adminService.obtenerUsuariosPorRol("BLOQUEADO", adminActual);
        
        // Obtener usuarios activos (no bloqueados) para poder bloquearlos
        List<Usuario> todosUsuarios = adminService.obtenerTodosLosUsuarios(adminActual);
        List<Usuario> usuariosActivos = todosUsuarios.stream()
            .filter(u -> !"BLOQUEADO".equals(u.getRol()))
            .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("stats", stats);
        model.addAttribute("usuariosBloqueados", usuariosBloqueados);
        model.addAttribute("usuariosActivos", usuariosActivos);
        model.addAttribute("seccion", "bloqueados");
        
        return "admin/bloqueados";
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
        
        // Verificar que solo admins pueden bloquear/desbloquear
        if (!esAdmin()) {
            response.put("success", false);
            response.put("message", "Acceso denegado. Solo administradores pueden bloquear usuarios.");
            return ResponseEntity.status(403).body(response);
        }
        
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
    @Transactional
    public ResponseEntity<Map<String, Object>> agregarEspecialidadDoctor(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long doctorId = Long.valueOf(request.get("doctorId").toString());
            Long especialidadId = Long.valueOf(request.get("especialidadId").toString());
            
            System.out.println("➕ Agregando especialidad - Doctor ID: " + doctorId + ", Especialidad ID: " + especialidadId);
            
            // Buscar el doctor
            Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
            
            // Inicializar la colección lazy
            org.hibernate.Hibernate.initialize(doctor.getEspecialidades());
            
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
    
    // ========== GESTIÓN DE ESPECIALIDADES ==========
    
    @PostMapping("/crear-especialidad")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearEspecialidad(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String nombre = request.get("nombre");
            String descripcion = request.get("descripcion");
            
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new RuntimeException("El nombre de la especialidad es obligatorio");
            }
            
            // Verificar si ya existe
            if (especialidadRepository.findByNombre(nombre).isPresent()) {
                throw new RuntimeException("Ya existe una especialidad con ese nombre");
            }
            
            Especialidad especialidad = new Especialidad();
            especialidad.setNombre(nombre.trim());
            especialidad.setDescripcion(descripcion != null ? descripcion.trim() : "");
            
            especialidadRepository.save(especialidad);
            
            response.put("success", true);
            response.put("message", "Especialidad creada exitosamente");
            response.put("especialidad", Map.of(
                "id", especialidad.getId(),
                "nombre", especialidad.getNombre(),
                "descripcion", especialidad.getDescripcion()
            ));
            
            System.out.println("✅ Especialidad creada: " + especialidad.getNombre());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear especialidad: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/editar-especialidad")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editarEspecialidad(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long id = Long.valueOf(request.get("id").toString());
            String nombre = request.get("nombre").toString();
            String descripcion = request.get("descripcion").toString();
            
            Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
            
            // Verificar si el nuevo nombre ya existe en otra especialidad
            if (!especialidad.getNombre().equals(nombre)) {
                if (especialidadRepository.findByNombre(nombre).isPresent()) {
                    throw new RuntimeException("Ya existe una especialidad con ese nombre");
                }
            }
            
            especialidad.setNombre(nombre.trim());
            especialidad.setDescripcion(descripcion != null ? descripcion.trim() : "");
            
            especialidadRepository.save(especialidad);
            
            response.put("success", true);
            response.put("message", "Especialidad actualizada exitosamente");
            
            System.out.println("✅ Especialidad actualizada: " + especialidad.getNombre());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error al editar especialidad: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/eliminar-especialidad")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarEspecialidad(@RequestBody Map<String, Long> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long id = request.get("id");
            
            Especialidad especialidad = especialidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
            
            // Verificar si hay doctores con esta especialidad
            List<Doctor> doctores = doctorRepository.findByEspecialidadNombre(especialidad.getNombre());
            if (!doctores.isEmpty()) {
                throw new RuntimeException("No se puede eliminar. Hay " + doctores.size() + " doctor(es) con esta especialidad");
            }
            
            especialidadRepository.delete(especialidad);
            
            response.put("success", true);
            response.put("message", "Especialidad eliminada exitosamente");
            
            System.out.println("✅ Especialidad eliminada: " + especialidad.getNombre());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar especialidad: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/remover-especialidad-doctor")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> removerEspecialidadDoctor(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long doctorId = Long.valueOf(request.get("doctorId").toString());
            Long especialidadId = Long.valueOf(request.get("especialidadId").toString());
            
            Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
            
            // Inicializar la colección lazy
            org.hibernate.Hibernate.initialize(doctor.getEspecialidades());
            
            Especialidad especialidad = especialidadRepository.findById(especialidadId)
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
            
            // Verificar que el doctor tenga más de una especialidad
            if (doctor.getEspecialidades().size() <= 1) {
                throw new RuntimeException("El doctor debe tener al menos una especialidad");
            }
            
            doctor.getEspecialidades().remove(especialidad);
            doctorRepository.save(doctor);
            
            response.put("success", true);
            response.put("message", "Especialidad removida exitosamente");
            
            System.out.println("✅ Especialidad removida del doctor");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error al remover especialidad: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
