package com.medipac.medipac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medipac.medipac.model.*;
import com.medipac.medipac.repository.*;
import com.medipac.medipac.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/paciente")
public class PacienteController {

    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private EspecialidadRepository especialidadRepository;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private HistoriaClinicaService historiaClinicaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Helper method para obtener el paciente logueado
    private Paciente getPacienteLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(auth.getName());
            if (usuarioOpt.isPresent()) {
                return pacienteRepository.findById(usuarioOpt.get().getId()).orElse(null);
            }
        }
        return null;
    }
    
    // ============= DASHBOARD Y VISTA PRINCIPAL =============
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener estadísticas del paciente
        CitaService.EstadisticasCitas estadisticasCitas = citaService.obtenerEstadisticasPaciente(paciente.getUsuarioId());
        HistoriaClinicaService.EstadisticasHistoria estadisticasHistorias = historiaClinicaService.obtenerEstadisticasPaciente(paciente.getUsuarioId());
        
        // Obtener próximas citas
        List<Cita> proximasCitas = citaService.obtenerProximasCitasPaciente(paciente.getUsuarioId());
        
        // Obtener última historia clínica
        HistoriaClinica ultimaHistoria = historiaClinicaService.obtenerUltimaHistoriaPaciente(paciente.getUsuarioId());
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("estadisticasCitas", estadisticasCitas);
        model.addAttribute("estadisticasHistorias", estadisticasHistorias);
        model.addAttribute("proximasCitas", proximasCitas.subList(0, Math.min(proximasCitas.size(), 3))); // Solo las próximas 3
        model.addAttribute("ultimaHistoria", ultimaHistoria);
        
        return "paciente/dashboard";
    }

    // ============= AGENDAR CITAS =============
    
    @GetMapping("/agendar-cita")
    public String mostrarFormularioAgendarCita(Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        model.addAttribute("paciente", paciente);
        
        return "paciente/agendar-cita";
    }
    
    @GetMapping("/agendar")
    public String mostrarFormularioAgendar(Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener especialidades disponibles
        List<Especialidad> especialidades = especialidadRepository.findEspecialidadesConDoctoresDisponibles();
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("especialidades", especialidades);
        
        return "paciente/agendar";
    }
    
    @GetMapping("/doctores-por-especialidad/{especialidadId}")
    @ResponseBody
    public List<Doctor> obtenerDoctoresPorEspecialidad(@PathVariable Long especialidadId) {
        Optional<Especialidad> especialidadOpt = especialidadRepository.findById(especialidadId);
        if (especialidadOpt.isPresent()) {
            return doctorRepository.findByEspecialidad(especialidadOpt.get());
        }
        return List.of();
    }
    
    @PostMapping("/agendar")
    public String agendarCita(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            @RequestParam String motivo,
            RedirectAttributes redirectAttributes) {
        
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
        
        CitaService.CitaResult result = citaService.agendarCita(
            paciente.getUsuarioId(), doctorId, fechaHora, motivo
        );
        
        if (result.isExito()) {
            redirectAttributes.addFlashAttribute("success", result.getMensaje());
            return "redirect:/paciente/mis-citas";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getMensaje());
            return "redirect:/paciente/agendar";
        }
    }

    // ============= GESTIÓN DE CITAS =============
    
    @GetMapping("/mis-citas")
    public String verMisCitas(Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener todas las citas del paciente
        List<Cita> citas = citaService.obtenerHistorialPaciente(paciente.getUsuarioId());
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citas);
        
        return "paciente/mis-citas";
    }
    
    @GetMapping("/mis-citas-old")
    public String verMisCitas(@RequestParam(value = "filtro", defaultValue = "todas") String filtro, Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        List<Cita> citas;
        
        switch (filtro) {
            case "proximas":
                citas = citaService.obtenerProximasCitasPaciente(paciente.getUsuarioId());
                break;
            case "completadas":
                citas = citaService.obtenerHistorialPaciente(paciente.getUsuarioId()).stream()
                    .filter(Cita::estaCompletada)
                    .toList();
                break;
            case "canceladas":
                citas = citaService.obtenerHistorialPaciente(paciente.getUsuarioId()).stream()
                    .filter(Cita::estaCancelada)
                    .toList();
                break;
            default:
                citas = citaService.obtenerHistorialPaciente(paciente.getUsuarioId());
                break;
        }
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citas);
        model.addAttribute("filtroActual", filtro);
        
        return "paciente/mis-citas";
    }
    
    @PostMapping("/cancelar-cita/{citaId}")
    @ResponseBody
    public String cancelarCita(@PathVariable Long citaId) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "error: No autorizado";
        }
        
        CitaService.CitaResult result = citaService.cancelarCita(citaId, paciente.getUsuarioId());
        return result.isExito() ? "success: " + result.getMensaje() : "error: " + result.getMensaje();
    }

    // ============= HISTORIAL MÉDICO =============
    
    @GetMapping("/historial")
    public String verHistorialMedico(Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        List<HistoriaClinica> historias = historiaClinicaService.obtenerHistorialPaciente(paciente.getUsuarioId());
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("historias", historias);
        
        return "paciente/historial";
    }
    
    @GetMapping("/historial/{historiaId}")
    public String verDetalleHistoria(@PathVariable Long historiaId, Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Verificar que el paciente puede acceder a esta historia
        if (!historiaClinicaService.pacientePuedeAccederHistoria(historiaId, paciente.getUsuarioId())) {
            return "redirect:/paciente/historial?error=no_access";
        }
        
        // Aquí deberías obtener la historia clínica completa
        model.addAttribute("paciente", paciente);
        model.addAttribute("historiaId", historiaId);
        
        return "paciente/detalle-historia";
    }

    // ============= PERFIL DEL PACIENTE =============
    
    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        model.addAttribute("paciente", paciente);
        
        return "paciente/perfil";
    }
    
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String telefono,
            RedirectAttributes redirectAttributes) {
        
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        try {
            if (fechaNacimiento != null) {
                paciente.setFechaNacimiento(fechaNacimiento);
            }
            if (genero != null && !genero.trim().isEmpty()) {
                paciente.setGenero(genero.trim());
            }
            if (telefono != null && !telefono.trim().isEmpty()) {
                paciente.setTelefono(telefono.trim());
            }
            
            pacienteRepository.save(paciente);
            
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
        }
        
        return "redirect:/paciente/perfil";
    }

    // ============= BUSCAR DOCTORES Y ESPECIALIDADES =============
    
    @GetMapping("/buscar-doctores")
    public String buscarDoctores(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long especialidadId,
            Model model) {
        
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        List<Doctor> doctores = List.of();
        List<Especialidad> especialidades = especialidadRepository.findAll();
        
        if (query != null && !query.trim().isEmpty()) {
            doctores = doctorRepository.findByNombreCompletoContaining(query);
        } else if (especialidadId != null) {
            Optional<Especialidad> especialidadOpt = especialidadRepository.findById(especialidadId);
            if (especialidadOpt.isPresent()) {
                doctores = doctorRepository.findByEspecialidad(especialidadOpt.get());
            }
        } else {
            doctores = doctorRepository.findDoctoresDisponibles();
        }
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("doctores", doctores);
        model.addAttribute("especialidades", especialidades);
        model.addAttribute("query", query);
        model.addAttribute("especialidadSeleccionada", especialidadId);
        
        return "paciente/buscar-doctores";
    }
    
    @GetMapping("/doctor/{doctorId}")
    public String verPerfilDoctor(@PathVariable Long doctorId, Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return "redirect:/paciente/buscar-doctores?error=doctor_not_found";
        }
        
        Doctor doctor = doctorOpt.get();
        
        // Obtener estadísticas del doctor (si es público)
        CitaService.EstadisticasCitas estadisticasDoctor = citaService.obtenerEstadisticasDoctor(doctor.getUsuarioId());
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("doctor", doctor);
        model.addAttribute("estadisticasDoctor", estadisticasDoctor);
        
        return "paciente/perfil-doctor";
    }

    // ============= NOTIFICACIONES Y RECORDATORIOS =============
    
    @GetMapping("/notificaciones")
    public String verNotificaciones(Model model) {
        Paciente paciente = getPacienteLogueado();
        if (paciente == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener citas próximas como notificaciones
        List<Cita> citasProximas = citaService.obtenerProximasCitasPaciente(paciente.getUsuarioId());
        
        // Filtrar citas para los próximos 3 días
        LocalDateTime limite = LocalDateTime.now().plusDays(3);
        List<Cita> citasUrgentes = citasProximas.stream()
            .filter(c -> c.getFechaHora().isBefore(limite))
            .toList();
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("citasUrgentes", citasUrgentes);
        model.addAttribute("todasProximas", citasProximas);
        
        return "paciente/notificaciones";
    }

    // ============= API ENDPOINTS PARA AJAX =============
    
    @GetMapping("/api/horarios-disponibles/{doctorId}")
    @ResponseBody
    public List<String> obtenerHorariosDisponibles(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        // Esta implementación sería más compleja en un sistema real
        // Por ahora, devolvemos horarios fijos de ejemplo
        return List.of("09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00");
    }
    
    @GetMapping("/api/disponibilidad/{doctorId}")
    @ResponseBody
    public boolean verificarDisponibilidad(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora) {
        
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return false;
        }
        
        // Verificar si hay conflictos
        // Esta lógica ya está en CitaService.agendarCita()
        return true; // Simplificado para el ejemplo
    }
}