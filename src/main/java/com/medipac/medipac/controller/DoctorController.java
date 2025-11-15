package com.medipac.medipac.controller;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medipac.medipac.model.*;
import com.medipac.medipac.repository.*;
import com.medipac.medipac.service.*;
import com.medipac.medipac.dto.CalendarioDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private HistoriaClinicaService historiaClinicaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CalendarioService calendarioService;

    // Helper method para obtener el doctor logueado
    private Doctor getDoctorLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            try {
                Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(auth.getName());
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    System.out.println("🔍 Buscando doctor para usuario: " + usuario.getUsername() + " (ID: " + usuario.getId() + ")");
                    Optional<Doctor> doctorOpt = doctorRepository.findByUsuarioId(usuario.getId());
                    if (doctorOpt.isPresent()) {
                        System.out.println("✅ Doctor encontrado: " + doctorOpt.get().getNombreCompleto());
                        return doctorOpt.get();
                    } else {
                        System.err.println("❌ No se encontró registro de doctor para el usuario: " + usuario.getUsername());
                    }
                } else {
                    System.err.println("❌ Usuario no encontrado: " + auth.getName());
                }
            } catch (Exception e) {
                System.err.println("❌ Error al obtener doctor logueado: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
    
    // ============= DASHBOARD Y VISTA PRINCIPAL =============
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Doctor doctor = getDoctorLogueado();
            if (doctor == null) {
                System.err.println("❌ No se pudo obtener el doctor logueado, redirigiendo a login");
                return "redirect:/login?error=access_denied";
            }
            
            System.out.println("✅ Doctor accediendo al dashboard: " + doctor.getNombreCompleto());
            
            // Obtener estadísticas del doctor con manejo de errores
            CitaService.EstadisticasCitas estadisticasCitas = null;
            try {
                estadisticasCitas = citaService.obtenerEstadisticasDoctor(doctor.getUsuarioId());
            } catch (Exception e) {
                System.err.println("⚠️ Error al obtener estadísticas de citas: " + e.getMessage());
                estadisticasCitas = new CitaService.EstadisticasCitas(0L, 0L, 0L, 0L);
            }
            
            HistoriaClinicaService.EstadisticasHistoria estadisticasHistorias = null;
            try {
                estadisticasHistorias = historiaClinicaService.obtenerEstadisticasDoctor(doctor.getUsuarioId());
            } catch (Exception e) {
                System.err.println("⚠️ Error al obtener estadísticas de historias: " + e.getMessage());
                // Crear estadísticas vacías si hay error (totalHistorias, conDiagnostico, conPrescripcion, conNotas)
                estadisticasHistorias = new HistoriaClinicaService.EstadisticasHistoria(0, 0, 0, 0);
            }
            
            // Obtener citas de hoy
            List<Cita> citasHoy = null;
            try {
                citasHoy = citaService.obtenerCitasHoyDoctor(doctor.getUsuarioId());
                if (citasHoy == null) citasHoy = new java.util.ArrayList<>();
            } catch (Exception e) {
                System.err.println("⚠️ Error al obtener citas de hoy: " + e.getMessage());
                citasHoy = new java.util.ArrayList<>();
            }
            
            // Obtener próximas citas (próximos 7 días)
            List<Cita> proximasCitas = null;
            try {
                proximasCitas = citaService.obtenerProximasCitasDoctor(doctor.getUsuarioId());
                if (proximasCitas == null) proximasCitas = new java.util.ArrayList<>();
            } catch (Exception e) {
                System.err.println("⚠️ Error al obtener próximas citas: " + e.getMessage());
                proximasCitas = new java.util.ArrayList<>();
            }
            
            model.addAttribute("doctor", doctor);
            model.addAttribute("estadisticasCitas", estadisticasCitas);
            model.addAttribute("estadisticasHistorias", estadisticasHistorias);
            model.addAttribute("citasHoy", citasHoy);
            model.addAttribute("proximasCitas", proximasCitas.subList(0, Math.min(proximasCitas.size(), 5))); // Solo las próximas 5
            
            return "doctor/dashboard";
            
        } catch (Exception e) {
            System.err.println("❌ Error crítico en dashboard de doctor: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=system_error";
        }
    }

    // ============= GESTIÓN DE CITAS =============
    
    @GetMapping("/mis-citas")
    @Transactional
    public String verMisCitas(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener todas las citas del doctor
        List<Cita> citas = citaService.obtenerTodasCitasDoctor(doctor.getUsuarioId());
        
        // Inicializar pacientes y sus usuarios para evitar LazyInitializationException
        citas.forEach(cita -> {
            Hibernate.initialize(cita.getPaciente());
            Hibernate.initialize(cita.getPaciente().getUsuario());
        });
        
        // Calcular estadísticas
        Map<String, Long> stats = new HashMap<>();
        stats.put("pendientes", citas.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count());
        stats.put("confirmadas", citas.stream().filter(c -> c.getEstado() == EstadoCita.CONFIRMADA).count());
        stats.put("completadas", citas.stream().filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count());
        stats.put("canceladas", citas.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count());
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("citas", citas);
        model.addAttribute("stats", stats);
        
        return "doctor/mis-citas";
    }
    
    @PostMapping("/cita/{id}/confirmar")
    public String confirmarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = getDoctorLogueado();
            if (doctor == null) {
                return "redirect:/login?error=access_denied";
            }
            
            CitaServiceMejorado.CitaResult result = citaService.confirmarCita(id, doctor.getUsuarioId());
            
            if (result.isExito()) {
                redirectAttributes.addFlashAttribute("success", result.getMensaje());
            } else {
                redirectAttributes.addFlashAttribute("error", result.getMensaje());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar la cita: " + e.getMessage());
        }
        
        return "redirect:/doctor/mis-citas";
    }
    
    @PostMapping("/cita/{id}/completar")
    public String completarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = getDoctorLogueado();
            if (doctor == null) {
                return "redirect:/login?error=access_denied";
            }
            
            CitaServiceMejorado.CitaResult result = citaService.completarCita(id, doctor.getUsuarioId());
            
            if (result.isExito()) {
                redirectAttributes.addFlashAttribute("success", result.getMensaje());
            } else {
                redirectAttributes.addFlashAttribute("error", result.getMensaje());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al completar la cita: " + e.getMessage());
        }
        
        return "redirect:/doctor/mis-citas";
    }
    
    @PostMapping("/cita/{id}/cancelar")
    public String cancelarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = getDoctorLogueado();
            if (doctor == null) {
                return "redirect:/login?error=access_denied";
            }
            
            CitaServiceMejorado.CitaResult result = citaService.cancelarCita(id, doctor.getUsuarioId(), "Cancelada por el doctor");
            
            if (result.isExito()) {
                redirectAttributes.addFlashAttribute("success", result.getMensaje());
            } else {
                redirectAttributes.addFlashAttribute("error", result.getMensaje());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la cita: " + e.getMessage());
        }
        
        return "redirect:/doctor/mis-citas";
    }
    
    @GetMapping("/citas")
    public String verCitas(@RequestParam(value = "fecha", required = false) String fechaStr, Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        List<Cita> citas;
        LocalDate fecha = null;
        
        if (fechaStr != null && !fechaStr.isEmpty()) {
            try {
                fecha = LocalDate.parse(fechaStr);
                citas = citaService.obtenerCitasPorFecha(fecha).stream()
                    .filter(c -> c.getDoctor().getUsuarioId().equals(doctor.getUsuarioId()))
                    .toList();
            } catch (Exception e) {
                fecha = LocalDate.now();
                citas = citaService.obtenerCitasHoyDoctor(doctor.getUsuarioId());
            }
        } else {
            fecha = LocalDate.now();
            citas = citaService.obtenerCitasHoyDoctor(doctor.getUsuarioId());
        }
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("citas", citas);
        model.addAttribute("fechaSeleccionada", fecha);
        
        return "doctor/citas";
    }
    
    @PostMapping("/citas/confirmar/{citaId}")
    @ResponseBody
    public String confirmarCita(@PathVariable Long citaId) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "error: No autorizado";
        }
        
        CitaService.CitaResult result = citaService.confirmarCita(citaId, doctor.getUsuarioId());
        return result.isExito() ? "success: " + result.getMensaje() : "error: " + result.getMensaje();
    }
    
    @PostMapping("/citas/completar/{citaId}")
    @ResponseBody
    public String completarCita(@PathVariable Long citaId) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "error: No autorizado";
        }
        
        CitaService.CitaResult result = citaService.completarCita(citaId, doctor.getUsuarioId());
        return result.isExito() ? "success: " + result.getMensaje() : "error: " + result.getMensaje();
    }

    // ============= GESTIÓN DE PACIENTES =============
    
    @GetMapping("/pacientes")
    public String verPacientes(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener todos los pacientes que han tenido citas con este doctor
        List<Cita> todasCitas = citaService.obtenerTodasCitasDoctor(doctor.getUsuarioId());
        List<Paciente> pacientes = todasCitas.stream()
            .map(Cita::getPaciente)
            .distinct()
            .toList();
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("pacientes", pacientes);
        
        return "doctor/pacientes";
    }
    
    @GetMapping("/pacientes/{pacienteId}")
    public String verDetallesPaciente(@PathVariable Long pacienteId, Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Verificar que el doctor tenga relación con este paciente
        List<HistoriaClinica> historiasCompartidas = historiaClinicaService.obtenerHistorialPacienteConDoctor(pacienteId, doctor.getUsuarioId());
        if (historiasCompartidas.isEmpty()) {
            return "redirect:/doctor/pacientes?error=no_access";
        }
        
        // Obtener información completa del paciente
        List<HistoriaClinica> historialCompleto = historiaClinicaService.obtenerHistorialPaciente(pacienteId);
        List<Cita> citasConDoctor = citaService.obtenerHistorialPaciente(pacienteId).stream()
            .filter(c -> c.getDoctor().getUsuarioId().equals(doctor.getUsuarioId()))
            .toList();
        
        Paciente paciente = citasConDoctor.isEmpty() ? null : citasConDoctor.get(0).getPaciente();
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("paciente", paciente);
        model.addAttribute("historialCompleto", historialCompleto);
        model.addAttribute("citasConDoctor", citasConDoctor);
        model.addAttribute("historiasCompartidas", historiasCompartidas);
        
        return "doctor/detalle-paciente";
    }

    // ============= GESTIÓN DE HISTORIAS CLÍNICAS =============
    
    @GetMapping("/historias")
    public String verHistoriasClinicas(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        List<HistoriaClinica> historias = historiaClinicaService.obtenerHistoriasPorDoctor(doctor.getUsuarioId());
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("historias", historias);
        
        return "doctor/historias";
    }
    
    @GetMapping("/historias/crear/{citaId}")
    public String formularioCrearHistoria(@PathVariable Long citaId, Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Verificar que la cita pertenece al doctor y esté completada
        // Esta lógica se maneja en el servicio, pero podemos pre-validar aquí
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("citaId", citaId);
        
        return "doctor/crear-historia";
    }
    
    @PostMapping("/historias/crear")
    public String crearHistoriaClinica(
            @RequestParam Long citaId,
            @RequestParam String diagnostico,
            @RequestParam String prescripcion,
            @RequestParam String notas,
            RedirectAttributes redirectAttributes) {
        
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        HistoriaClinicaService.HistoriaResult result = historiaClinicaService.crearHistoriaClinica(
            citaId, doctor.getUsuarioId(), diagnostico, prescripcion, notas
        );
        
        if (result.isExito()) {
            redirectAttributes.addFlashAttribute("success", result.getMensaje());
            return "redirect:/doctor/historias";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMensaje());
            return "redirect:/doctor/citas";
        }
    }
    
    @GetMapping("/historias/editar/{historiaId}")
    public String formularioEditarHistoria(@PathVariable Long historiaId, Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Verificar que el doctor puede acceder a esta historia
        if (!historiaClinicaService.puedeAccederHistoria(historiaId, doctor.getUsuarioId())) {
            return "redirect:/doctor/historias?error=no_access";
        }
        
        // Aquí deberías obtener la historia clínica para pre-llenar el formulario
        model.addAttribute("doctor", doctor);
        model.addAttribute("historiaId", historiaId);
        
        return "doctor/editar-historia";
    }
    
    @PostMapping("/historias/editar")
    public String editarHistoriaClinica(
            @RequestParam Long historiaId,
            @RequestParam String diagnostico,
            @RequestParam String prescripcion,
            @RequestParam String notas,
            RedirectAttributes redirectAttributes) {
        
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        HistoriaClinicaService.HistoriaResult result = historiaClinicaService.actualizarHistoriaClinica(
            historiaId, doctor.getUsuarioId(), diagnostico, prescripcion, notas
        );
        
        if (result.isExito()) {
            redirectAttributes.addFlashAttribute("success", result.getMensaje());
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMensaje());
        }
        
        return "redirect:/doctor/historias";
    }

    // ============= PERFIL DEL DOCTOR =============
    
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        model.addAttribute("doctor", doctor);
        
        return "doctor/perfil";
    }
    
    @GetMapping("/editar-perfil")
    @Transactional(readOnly = true)
    public String editarPerfil(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Forzar la carga de especialidades para evitar LazyInitializationException
        if (doctor.getEspecialidades() != null) {
            Hibernate.initialize(doctor.getEspecialidades());
        }
        
        model.addAttribute("doctor", doctor);
        
        return "doctor/editar-perfil";
    }
    
    @PostMapping("/editar-perfil")
    public String actualizarPerfil(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(required = false) String telefono,
            RedirectAttributes redirectAttributes) {
        
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        try {
            if (nombre != null && !nombre.trim().isEmpty()) {
                doctor.setNombre(nombre.trim());
            }
            
            if (apellido != null && !apellido.trim().isEmpty()) {
                doctor.setApellido(apellido.trim());
            }
            
            if (telefono != null && !telefono.trim().isEmpty()) {
                doctor.setTelefono(telefono.trim());
            }
            
            doctorRepository.save(doctor);
            
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar perfil: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }
        
        return "redirect:/doctor/editar-perfil";
    }
    
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfilLegacy(
            @RequestParam String telefono,
            RedirectAttributes redirectAttributes) {
        
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        try {
            doctor.setTelefono(telefono);
            doctorRepository.save(doctor);
            
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
        }
        
        return "redirect:/doctor/perfil";
    }

    // ============= GESTIÓN DE CALENDARIO =============
    
    @GetMapping("/calendario")
    public String mostrarCalendario(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        CalendarioDTO calendario = calendarioService.obtenerCalendario(doctor.getUsuarioId());
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("calendario", calendario);
        model.addAttribute("jornadasInfo", calendarioService.obtenerJornadasDisponibles());
        
        return "doctor/calendario";
    }
    
    @PostMapping("/calendario/guardar")
    public String guardarCalendario(
            @ModelAttribute CalendarioDTO calendarioDTO,
            RedirectAttributes redirectAttributes) {
        
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        try {
            calendarioDTO.setDoctorId(doctor.getUsuarioId());
            calendarioService.guardarCalendario(calendarioDTO);
            
            redirectAttributes.addFlashAttribute("mensaje", "Calendario actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar calendario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        
        return "redirect:/doctor/calendario";
    }

    // ============= BÚSQUEDAS Y REPORTES =============
    
    @GetMapping("/buscar")
    public String buscarPacientes(@RequestParam(required = false) String query, Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        List<HistoriaClinica> resultados = List.of();
        
        if (query != null && !query.trim().isEmpty()) {
            // Buscar en historias clínicas por diagnóstico
            resultados = historiaClinicaService.buscarPorDiagnostico(query).stream()
                .filter(h -> h.getCita().getDoctor().getUsuarioId().equals(doctor.getUsuarioId()))
                .toList();
        }
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("query", query);
        model.addAttribute("resultados", resultados);
        
        return "doctor/buscar";
    }
}