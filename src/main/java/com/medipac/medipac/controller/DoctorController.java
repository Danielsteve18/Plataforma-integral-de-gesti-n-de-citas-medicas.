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
import java.util.ArrayList;
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
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private HistoriaClinicaService historiaClinicaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PrescripcionRepository prescripcionRepository;
    
    @Autowired
    private CalendarioService calendarioService;
    
    @Autowired
    private DoctorPacienteFavoritoRepository favoritoRepository;

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
            
            CitaService.CitaResult result = citaService.confirmarCita(id, doctor.getUsuarioId());
            
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
            
            CitaService.CitaResult result = citaService.completarCita(id, doctor.getUsuarioId());
            
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
            
            CitaService.CitaResult result = citaService.cancelarCita(id, doctor.getUsuarioId());
            
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
    @Transactional
    public String verHistoriasClinicas(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener todas las citas del doctor (aceptadas y rechazadas)
        List<Cita> todasCitas = citaService.obtenerTodasCitasDoctor(doctor.getUsuarioId());
        
        // Inicializar pacientes
        todasCitas.forEach(cita -> {
            Hibernate.initialize(cita.getPaciente());
            Hibernate.initialize(cita.getPaciente().getUsuario());
        });
        
        // Obtener IDs de pacientes favoritos
        List<Long> pacientesFavoritosIds = favoritoRepository.findPacienteIdsByDoctorId(doctor.getUsuarioId());
        
        // Agrupar pacientes únicos con sus citas
        Map<Paciente, Map<String, Object>> pacientesConCitas = new HashMap<>();
        
        for (Cita cita : todasCitas) {
            Paciente paciente = cita.getPaciente();
            
            if (!pacientesConCitas.containsKey(paciente)) {
                Map<String, Object> info = new HashMap<>();
                info.put("citasAceptadas", new ArrayList<Cita>());
                info.put("citasRechazadas", new ArrayList<Cita>());
                info.put("totalCitas", 0);
                info.put("esFavorito", pacientesFavoritosIds.contains(paciente.getUsuarioId()));
                pacientesConCitas.put(paciente, info);
            }
            
            Map<String, Object> info = pacientesConCitas.get(paciente);
            @SuppressWarnings("unchecked")
            List<Cita> citasAceptadas = (List<Cita>) info.get("citasAceptadas");
            @SuppressWarnings("unchecked")
            List<Cita> citasRechazadas = (List<Cita>) info.get("citasRechazadas");
            
            if (cita.getEstado() == EstadoCita.CONFIRMADA || cita.getEstado() == EstadoCita.COMPLETADA) {
                citasAceptadas.add(cita);
            } else if (cita.getEstado() == EstadoCita.CANCELADA) {
                citasRechazadas.add(cita);
            }
            
            info.put("totalCitas", citasAceptadas.size() + citasRechazadas.size());
        }
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("pacientesConCitas", pacientesConCitas);
        
        return "doctor/historias";
    }
    
    @GetMapping("/historias/paciente/{pacienteId}")
    @Transactional
    public String verHistoriasPaciente(@PathVariable Long pacienteId, Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener el paciente por usuarioId
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (!pacienteOpt.isPresent()) {
            return "redirect:/doctor/historias?error=paciente_no_encontrado";
        }
        
        Paciente paciente = pacienteOpt.get();
        Hibernate.initialize(paciente.getUsuario());
        
        // Obtener todas las citas de este paciente con el doctor
        List<Cita> todasCitas = citaService.obtenerTodasCitasDoctor(doctor.getUsuarioId()).stream()
            .filter(c -> c.getPaciente().getUsuarioId().equals(pacienteId))
            .collect(java.util.stream.Collectors.toList());
        
        // Separar citas aceptadas y rechazadas
        List<Cita> citasAceptadas = todasCitas.stream()
            .filter(c -> c.getEstado() == EstadoCita.CONFIRMADA || c.getEstado() == EstadoCita.COMPLETADA)
            .collect(java.util.stream.Collectors.toList());
            
        List<Cita> citasRechazadas = todasCitas.stream()
            .filter(c -> c.getEstado() == EstadoCita.CANCELADA)
            .collect(java.util.stream.Collectors.toList());
        
        // Obtener historias clínicas del paciente con este doctor
        List<HistoriaClinica> historias = historiaClinicaService.obtenerHistorialPacienteConDoctor(pacienteId, doctor.getUsuarioId());
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("paciente", paciente);
        model.addAttribute("historias", historias);
        model.addAttribute("citasAceptadas", citasAceptadas);
        model.addAttribute("citasRechazadas", citasRechazadas);
        
        System.out.println("📊 Datos enviados a la vista:");
        System.out.println("  - Historias: " + historias.size());
        System.out.println("  - Citas Aceptadas: " + citasAceptadas.size());
        System.out.println("  - Citas Rechazadas: " + citasRechazadas.size());
        
        return "doctor/historias-paciente";
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

    // ============= GESTIÓN DE PRESCRIPCIONES =============
    
    @GetMapping("/prescripciones")
    @Transactional
    public String verPrescripciones(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener todas las prescripciones del doctor
        List<Prescripcion> prescripciones = prescripcionRepository.findByDoctorId(doctor.getUsuarioId());
        
        // Inicializar relaciones de prescripciones
        prescripciones.forEach(p -> {
            Hibernate.initialize(p.getCita());
            Hibernate.initialize(p.getCita().getPaciente());
            Hibernate.initialize(p.getCita().getPaciente().getUsuario());
        });
        
        // Obtener citas CONFIRMADAS (aceptadas) sin prescripción para completar
        List<Cita> citasAceptadas = citaService.obtenerTodasCitasDoctor(doctor.getUsuarioId()).stream()
            .filter(c -> c.getEstado() == EstadoCita.CONFIRMADA)
            .filter(c -> prescripcionRepository.findByCitaId(c.getId()).isEmpty())
            .collect(java.util.stream.Collectors.toList());
        
        // Inicializar relaciones de citas aceptadas
        citasAceptadas.forEach(c -> {
            Hibernate.initialize(c.getPaciente());
            Hibernate.initialize(c.getPaciente().getUsuario());
        });
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("prescripciones", prescripciones);
        model.addAttribute("citasAceptadas", citasAceptadas);
        
        return "doctor/prescripciones";
    }
    
    @PostMapping("/prescripciones/crear")
    @Transactional
    public String crearPrescripcion(
            @RequestParam Long citaId,
            @RequestParam String medicamentos,
            @RequestParam String indicaciones,
            @RequestParam String dosis,
            @RequestParam String duracion,
            @RequestParam(required = false) String notasPrescripcion,
            RedirectAttributes redirectAttributes) {
        
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        try {
            // Verificar que la cita existe y pertenece al doctor
            Optional<Cita> citaOpt = citaService.obtenerTodasCitasDoctor(doctor.getUsuarioId()).stream()
                .filter(c -> c.getId().equals(citaId))
                .findFirst();
            
            if (!citaOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
                return "redirect:/doctor/prescripciones";
            }
            
            Cita cita = citaOpt.get();
            
            // Validar que la cita esté confirmada (aceptada)
            if (cita.getEstado() != EstadoCita.CONFIRMADA) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden crear prescripciones para citas confirmadas");
                return "redirect:/doctor/prescripciones";
            }
            
            // Verificar que no exista ya una prescripción para esta cita
            if (prescripcionRepository.findByCitaId(citaId).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una prescripción para esta cita");
                return "redirect:/doctor/prescripciones";
            }
            
            // Crear la prescripción
            Prescripcion prescripcion = new Prescripcion();
            prescripcion.setCita(cita);
            prescripcion.setMedicamentos(medicamentos);
            prescripcion.setIndicaciones(indicaciones);
            prescripcion.setDosis(dosis);
            prescripcion.setDuracion(duracion);
            prescripcion.setNotas(notasPrescripcion);
            
            prescripcionRepository.save(prescripcion);
            
            redirectAttributes.addFlashAttribute("success", "Prescripción creada exitosamente");
            redirectAttributes.addFlashAttribute("citaId", citaId);
            redirectAttributes.addFlashAttribute("prescripcionId", prescripcion.getId());
            redirectAttributes.addFlashAttribute("mostrarModalHistoria", true);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la prescripción: " + e.getMessage());
        }
        
        return "redirect:/doctor/prescripciones";
    }
    
    @PostMapping("/prescripciones/completar")
    @Transactional
    public String completarCitaConHistoria(
            @RequestParam Long citaId,
            @RequestParam Long prescripcionId,
            @RequestParam String diagnostico,
            @RequestParam(required = false) String prescripcionAdicional,
            @RequestParam(required = false) String notasHistoria,
            RedirectAttributes redirectAttributes) {
        
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        try {
            // Obtener la prescripción
            Optional<Prescripcion> prescripcionOpt = prescripcionRepository.findById(prescripcionId);
            if (!prescripcionOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Prescripción no encontrada");
                return "redirect:/doctor/prescripciones";
            }
            
            Prescripcion prescripcion = prescripcionOpt.get();
            
            // Construir la prescripción completa
            String prescripcionCompleta = "MEDICAMENTOS: " + prescripcion.getMedicamentos() + "\n\n" +
                                         "DOSIS: " + prescripcion.getDosis() + "\n\n" +
                                         "DURACIÓN: " + prescripcion.getDuracion() + "\n\n" +
                                         "INDICACIONES: " + prescripcion.getIndicaciones();
            
            if (prescripcionAdicional != null && !prescripcionAdicional.trim().isEmpty()) {
                prescripcionCompleta += "\n\nINFORMACIÓN ADICIONAL: " + prescripcionAdicional;
            }
            
            // PRIMERO: Completar la cita (cambiar estado a COMPLETADA)
            CitaService.CitaResult citaResult = citaService.completarCita(citaId, doctor.getUsuarioId());
            if (!citaResult.isExito()) {
                redirectAttributes.addFlashAttribute("error", "Error al completar la cita: " + citaResult.getMensaje());
                return "redirect:/doctor/prescripciones";
            }
            
            // SEGUNDO: Crear la historia clínica (ahora la cita ya está COMPLETADA)
            HistoriaClinicaService.HistoriaResult result = historiaClinicaService.crearHistoriaClinica(
                citaId, 
                doctor.getUsuarioId(), 
                diagnostico, 
                prescripcionCompleta, 
                notasHistoria != null ? notasHistoria : ""
            );
            
            if (result.isExito()) {
                redirectAttributes.addFlashAttribute("success", "Cita completada con historia clínica creada");
            } else {
                redirectAttributes.addFlashAttribute("error", result.getMensaje());
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al completar la cita: " + e.getMessage());
        }
        
        return "redirect:/doctor/prescripciones";
    }
    
    // ========== ENDPOINTS PARA FAVORITOS ==========
    
    @PostMapping("/favoritos/toggle/{pacienteId}")
    @ResponseBody
    public Map<String, Object> toggleFavorito(@PathVariable Long pacienteId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Doctor doctor = getDoctorLogueado();
            if (doctor == null) {
                response.put("success", false);
                response.put("message", "Doctor no autenticado");
                return response;
            }
            
            Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
            if (!pacienteOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Paciente no encontrado");
                return response;
            }
            
            Paciente paciente = pacienteOpt.get();
            
            // Verificar si ya existe el favorito
            Optional<DoctorPacienteFavorito> favoritoExistente = 
                favoritoRepository.findByDoctorIdAndPacienteId(doctor.getUsuarioId(), pacienteId);
            
            if (favoritoExistente.isPresent()) {
                // Eliminar de favoritos
                favoritoRepository.delete(favoritoExistente.get());
                response.put("success", true);
                response.put("esFavorito", false);
                response.put("message", "Paciente eliminado de favoritos");
            } else {
                // Agregar a favoritos
                DoctorPacienteFavorito nuevoFavorito = new DoctorPacienteFavorito(doctor, paciente);
                favoritoRepository.save(nuevoFavorito);
                response.put("success", true);
                response.put("esFavorito", true);
                response.put("message", "Paciente agregado a favoritos");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/mis-pacientes")
    @Transactional
    public String verMisPacientes(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener pacientes favoritos
        List<DoctorPacienteFavorito> favoritos = favoritoRepository.findByDoctorId(doctor.getUsuarioId());
        
        // Inicializar y obtener información completa de cada paciente favorito
        Map<Paciente, Map<String, Object>> pacientesFavoritos = new HashMap<>();
        
        for (DoctorPacienteFavorito favorito : favoritos) {
            Paciente paciente = favorito.getPaciente();
            Hibernate.initialize(paciente);
            Hibernate.initialize(paciente.getUsuario());
            
            // Obtener citas del paciente con este doctor
            List<Cita> citasDelPaciente = citaService.obtenerCitasPorPacienteYDoctor(
                paciente.getUsuarioId(), 
                doctor.getUsuarioId()
            );
            
            // Inicializar relaciones lazy de las citas
            citasDelPaciente.forEach(cita -> {
                Hibernate.initialize(cita.getPaciente());
                Hibernate.initialize(cita.getPaciente().getUsuario());
                Hibernate.initialize(cita.getDoctor());
            });
            
            Map<String, Object> info = new HashMap<>();
            List<Cita> citasAceptadas = new ArrayList<>();
            List<Cita> citasRechazadas = new ArrayList<>();
            
            for (Cita cita : citasDelPaciente) {
                if (cita.getEstado() == EstadoCita.CONFIRMADA || cita.getEstado() == EstadoCita.COMPLETADA) {
                    citasAceptadas.add(cita);
                } else if (cita.getEstado() == EstadoCita.CANCELADA) {
                    citasRechazadas.add(cita);
                }
            }
            
            info.put("citasAceptadas", citasAceptadas);
            info.put("citasRechazadas", citasRechazadas);
            info.put("totalCitas", citasAceptadas.size() + citasRechazadas.size());
            info.put("esFavorito", true);
            
            pacientesFavoritos.put(paciente, info);
        }
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("pacientesConCitas", pacientesFavoritos);
        
        return "doctor/mis-pacientes";
    }
    
    // ============= NOTIFICACIONES Y RECORDATORIOS =============
    
    @GetMapping("/notificaciones")
    @Transactional
    public String verNotificaciones(Model model) {
        Doctor doctor = getDoctorLogueado();
        if (doctor == null) {
            return "redirect:/login?error=access_denied";
        }
        
        // Obtener citas próximas como notificaciones
        List<Cita> citasProximas = citaService.obtenerProximasCitasDoctor(doctor.getUsuarioId());
        
        // Inicializar relaciones lazy
        citasProximas.forEach(cita -> {
            if (cita.getPaciente() != null) {
                Hibernate.initialize(cita.getPaciente());
                Hibernate.initialize(cita.getPaciente().getUsuario());
            }
        });
        
        // Filtrar citas para los próximos 3 días
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusDays(3);
        List<Cita> citasUrgentes = citasProximas.stream()
            .filter(c -> c.getFechaHora().isAfter(ahora) && c.getFechaHora().isBefore(limite))
            .toList();
        
        // Obtener las demás citas (excluyendo las urgentes, solo las que están después de 3 días)
        List<Cita> otrasCitasProximas = citasProximas.stream()
            .filter(c -> c.getFechaHora().isAfter(ahora) && !c.getFechaHora().isBefore(limite))
            .toList();
        
        model.addAttribute("doctor", doctor);
        model.addAttribute("citasUrgentes", citasUrgentes);
        model.addAttribute("citasProximas", otrasCitasProximas);
        
        return "doctor/notificaciones";
    }
}
