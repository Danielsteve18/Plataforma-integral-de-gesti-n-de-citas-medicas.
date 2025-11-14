package com.medipac.medipac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.medipac.medipac.dto.*;
import com.medipac.medipac.model.*;
import com.medipac.medipac.repository.*;
import com.medipac.medipac.service.CitaServiceMejorado;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestión eficiente de citas médicas
 * Proporciona endpoints JSON para aplicaciones frontend modernas
 */
@RestController
@RequestMapping("/api/citas")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class CitaRestController {

    @Autowired
    private CitaServiceMejorado citaService;
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CitaMapper citaMapper;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private EspecialidadRepository especialidadRepository;

    // Helper method para obtener el usuario logueado
    private Usuario getUsuarioLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return usuarioRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    // ============= CREAR CITA =============
    
    /**
     * GET /api/doctores/especialidad/{nombre}
     * Obtiene doctores por especialidad
     */
    @GetMapping("/doctores/especialidad/{nombre}")
    public ResponseEntity<?> obtenerDoctoresPorEspecialidad(@PathVariable String nombre) {
        try {
            List<Doctor> doctores = doctorRepository.findByEspecialidadNombre(nombre);
            
            List<Map<String, Object>> doctoresInfo = doctores.stream()
                .map(doctor -> {
                    Map<String, Object> info = new java.util.HashMap<>();
                    info.put("id", doctor.getUsuarioId());
                    info.put("nombre", doctor.getNombre());
                    info.put("apellido", doctor.getApellido());
                    info.put("nombreCompleto", doctor.getNombreCompleto());
                    info.put("numeroLicencia", doctor.getNumeroLicencia());
                    info.put("telefono", doctor.getTelefono() != null ? doctor.getTelefono() : "");
                    info.put("especialidad", nombre);
                    return info;
                })
                .toList();
            
            return ResponseEntity.ok(doctoresInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener doctores: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/citas/disponibles
     * Obtiene horarios disponibles de un doctor en una fecha
     */
    @GetMapping("/disponibles")
    public ResponseEntity<?> obtenerHorariosDisponibles(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            // Horarios estándar (de 8:00 AM a 6:00 PM, cada hora)
            List<String> horariosEstandar = List.of(
                "08:00", "09:00", "10:00", "11:00", "12:00",
                "14:00", "15:00", "16:00", "17:00", "18:00"
            );
            
            // Obtener las citas ya agendadas para ese doctor en esa fecha
            java.time.LocalDateTime inicioDia = fecha.atStartOfDay();
            java.time.LocalDateTime finDia = fecha.atTime(23, 59);
            
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "mensaje", "Doctor no encontrado"
                ));
            }
            
            // Obtener citas ocupadas
            List<Cita> citasOcupadas = citaRepository.findByDoctorAndFechaHoraBetween(
                doctorOpt.get(), inicioDia, finDia
            );
            
            // Extraer horas ocupadas (formato HH:mm)
            List<String> horasOcupadas = citasOcupadas.stream()
                .map(cita -> String.format("%02d:%02d", 
                    cita.getFechaHora().getHour(), 
                    cita.getFechaHora().getMinute()))
                .toList();
            
            // Filtrar horarios disponibles
            List<String> horariosDisponibles = horariosEstandar.stream()
                .filter(horario -> !horasOcupadas.contains(horario))
                .toList();
            
            return ResponseEntity.ok(horariosDisponibles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener horarios: " + e.getMessage()
            ));
        }
    }
    
    /**
     * POST /api/citas
     * Crea una nueva cita médica
     */
    @PostMapping
    public ResponseEntity<?> crearCita(@Valid @RequestBody CrearCitaRequest request) {
        try {
            CitaServiceMejorado.CitaResult result = citaService.crearCita(request);
            
            if (result.isExito()) {
                CitaDTO citaDTO = citaMapper.toDTO(result.getCita());
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "mensaje", result.getMensaje(),
                    "cita", citaDTO
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "mensaje", result.getMensaje()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    // ============= OBTENER CITAS =============
    
    /**
     * GET /api/citas/{id}
     * Obtiene una cita por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCita(@PathVariable Long id) {
        Optional<CitaDTO> citaDTO = citaService.obtenerCitaPorId(id);
        
        if (citaDTO.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cita", citaDTO.get()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "mensaje", "Cita no encontrada"
            ));
        }
    }
    
    /**
     * GET /api/citas/paciente/{pacienteId}/proximas
     * Obtiene las próximas citas de un paciente
     */
    @GetMapping("/paciente/{pacienteId}/proximas")
    public ResponseEntity<?> obtenerProximasCitasPaciente(@PathVariable Long pacienteId) {
        try {
            List<CitaDTO> citas = citaService.obtenerProximasCitasPaciente(pacienteId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cantidad", citas.size(),
                "citas", citas
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener citas: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/citas/paciente/{pacienteId}/historial
     * Obtiene el historial completo de citas de un paciente
     */
    @GetMapping("/paciente/{pacienteId}/historial")
    public ResponseEntity<?> obtenerHistorialPaciente(@PathVariable Long pacienteId) {
        try {
            List<CitaDTO> citas = citaService.obtenerHistorialPaciente(pacienteId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cantidad", citas.size(),
                "citas", citas
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener historial: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/citas/doctor/{doctorId}/hoy
     * Obtiene las citas de hoy de un doctor
     */
    @GetMapping("/doctor/{doctorId}/hoy")
    public ResponseEntity<?> obtenerCitasHoyDoctor(@PathVariable Long doctorId) {
        try {
            List<CitaDTO> citas = citaService.obtenerCitasHoyDoctor(doctorId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cantidad", citas.size(),
                "citas", citas
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener citas: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/citas/doctor/{doctorId}/proximas
     * Obtiene las próximas citas de un doctor
     */
    @GetMapping("/doctor/{doctorId}/proximas")
    public ResponseEntity<?> obtenerProximasCitasDoctor(@PathVariable Long doctorId) {
        try {
            List<CitaDTO> citas = citaService.obtenerProximasCitasDoctor(doctorId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "cantidad", citas.size(),
                "citas", citas
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener citas: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/citas/fecha/{fecha}
     * Obtiene citas por fecha específica
     */
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<?> obtenerCitasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            List<CitaDTO> citas = citaService.obtenerCitasPorFecha(fecha);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "fecha", fecha,
                "cantidad", citas.size(),
                "citas", citas
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener citas: " + e.getMessage()
            ));
        }
    }

    // ============= ACTUALIZAR CITA =============
    
    /**
     * PUT /api/citas/{id}
     * Actualiza una cita existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCita(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarCitaRequest request) {
        try {
            request.setCitaId(id);
            CitaServiceMejorado.CitaResult result = citaService.actualizarCita(request);
            
            if (result.isExito()) {
                CitaDTO citaDTO = citaMapper.toDTO(result.getCita());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", result.getMensaje(),
                    "cita", citaDTO
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "mensaje", result.getMensaje()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    // ============= ACCIONES DE ESTADO =============
    
    /**
     * PATCH /api/citas/{id}/confirmar
     * Confirma una cita
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarCita(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogueado();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "mensaje", "No autenticado"
                ));
            }
            
            CitaServiceMejorado.CitaResult result = citaService.confirmarCita(id, usuario.getId());
            
            if (result.isExito()) {
                CitaDTO citaDTO = citaMapper.toDTO(result.getCita());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", result.getMensaje(),
                    "cita", citaDTO
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "mensaje", result.getMensaje()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * PATCH /api/citas/{id}/completar
     * Marca una cita como completada
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<?> completarCita(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogueado();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "mensaje", "No autenticado"
                ));
            }
            
            CitaServiceMejorado.CitaResult result = citaService.completarCita(id, usuario.getId());
            
            if (result.isExito()) {
                CitaDTO citaDTO = citaMapper.toDTO(result.getCita());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", result.getMensaje(),
                    "cita", citaDTO
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "mensaje", result.getMensaje()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * PATCH /api/citas/{id}/cancelar
     * Cancela una cita
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            Usuario usuario = getUsuarioLogueado();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "mensaje", "No autenticado"
                ));
            }
            
            String notas = body != null ? body.get("notas") : null;
            CitaServiceMejorado.CitaResult result = citaService.cancelarCita(id, usuario.getId(), notas);
            
            if (result.isExito()) {
                CitaDTO citaDTO = citaMapper.toDTO(result.getCita());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", result.getMensaje(),
                    "cita", citaDTO
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "mensaje", result.getMensaje()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * PATCH /api/citas/{id}/no-asistio
     * Marca una cita como no asistida
     */
    @PatchMapping("/{id}/no-asistio")
    public ResponseEntity<?> marcarNoAsistio(@PathVariable Long id) {
        try {
            Usuario usuario = getUsuarioLogueado();
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "mensaje", "No autenticado"
                ));
            }
            
            CitaServiceMejorado.CitaResult result = citaService.marcarNoAsistio(id, usuario.getId());
            
            if (result.isExito()) {
                CitaDTO citaDTO = citaMapper.toDTO(result.getCita());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", result.getMensaje(),
                    "cita", citaDTO
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "mensaje", result.getMensaje()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    // ============= ESTADÍSTICAS =============
    
    /**
     * GET /api/citas/estadisticas/paciente/{pacienteId}
     * Obtiene estadísticas de citas de un paciente
     */
    @GetMapping("/estadisticas/paciente/{pacienteId}")
    public ResponseEntity<?> obtenerEstadisticasPaciente(@PathVariable Long pacienteId) {
        try {
            // Esta funcionalidad necesitaría implementarse en CitaServiceMejorado
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mensaje", "Funcionalidad en desarrollo"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener estadísticas: " + e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/citas/estadisticas/doctor/{doctorId}
     * Obtiene estadísticas de citas de un doctor
     */
    @GetMapping("/estadisticas/doctor/{doctorId}")
    public ResponseEntity<?> obtenerEstadisticasDoctor(@PathVariable Long doctorId) {
        try {
            // Esta funcionalidad necesitaría implementarse en CitaServiceMejorado
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mensaje", "Funcionalidad en desarrollo"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "mensaje", "Error al obtener estadísticas: " + e.getMessage()
            ));
        }
    }

    // ============= MANEJO DE ERRORES =============
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "mensaje", "Error interno del servidor",
            "detalle", e.getMessage()
        ));
    }
}
