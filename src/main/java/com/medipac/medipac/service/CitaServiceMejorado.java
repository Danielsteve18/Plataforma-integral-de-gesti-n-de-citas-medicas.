package com.medipac.medipac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medipac.medipac.dto.*;
import com.medipac.medipac.model.*;
import com.medipac.medipac.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio mejorado para gestión de citas médicas
 * Incluye validaciones robustas y manejo eficiente de conflictos
 */
@Service
@Transactional
public class CitaServiceMejorado {

    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private HorarioRepository horarioRepository;
    
    @Autowired
    private CitaMapper citaMapper;

    // ============= CONSTANTES DE NEGOCIO =============
    
    private static final int DURACION_MINIMA_MINUTOS = 15;
    private static final int DURACION_MAXIMA_MINUTOS = 180;
    private static final int DURACION_DEFECTO_MINUTOS = 30;
    private static final int MINUTOS_ANTICIPACION_MINIMA = 60; // 1 hora de anticipación
    private static final int DIAS_ANTICIPACION_MAXIMA = 90; // 3 meses de anticipación máxima

    // ============= CREAR CITA =============
    
    /**
     * Crea una nueva cita con validaciones completas
     */
    public CitaResult crearCita(CrearCitaRequest request) {
        try {
            // 1. Validar que existan el paciente y doctor
            Optional<Paciente> pacienteOpt = pacienteRepository.findById(request.getPacienteId());
            if (!pacienteOpt.isPresent()) {
                return CitaResult.error("Paciente no encontrado");
            }
            
            Optional<Doctor> doctorOpt = doctorRepository.findById(request.getDoctorId());
            if (!doctorOpt.isPresent()) {
                return CitaResult.error("Doctor no encontrado");
            }
            
            Paciente paciente = pacienteOpt.get();
            Doctor doctor = doctorOpt.get();
            
            // 2. Validar la fecha y hora
            ValidationResult validacion = validarFechaHora(request.getFechaHora());
            if (!validacion.isValido()) {
                return CitaResult.error(validacion.getMensaje());
            }
            
            // 3. Validar duración
            Integer duracion = request.getDuracionMinutos();
            if (duracion == null) {
                duracion = DURACION_DEFECTO_MINUTOS;
            }
            if (duracion < DURACION_MINIMA_MINUTOS || duracion > DURACION_MAXIMA_MINUTOS) {
                return CitaResult.error(String.format(
                    "La duración debe estar entre %d y %d minutos",
                    DURACION_MINIMA_MINUTOS, DURACION_MAXIMA_MINUTOS
                ));
            }
            
            // 4. Verificar disponibilidad del doctor
            LocalDateTime fechaHoraFin = request.getFechaHora().plusMinutes(duracion);
            boolean hayConflicto = citaRepository.existeConflictoHorario(
                doctor, request.getFechaHora(), fechaHoraFin
            );
            
            if (hayConflicto) {
                return CitaResult.error("El doctor no está disponible en ese horario");
            }
            
            // 5. Verificar que el paciente no tenga citas duplicadas en el mismo horario
            boolean pacienteTieneCita = verificarCitaPacienteEnHorario(
                paciente, request.getFechaHora(), fechaHoraFin
            );
            
            if (pacienteTieneCita) {
                return CitaResult.error("Ya tiene una cita programada en ese horario");
            }
            
            // 6. Crear y guardar la cita
            Cita nuevaCita = new Cita(paciente, doctor, request.getFechaHora(), request.getMotivo());
            nuevaCita.setDuracionMinutos(duracion);
            Cita citaGuardada = citaRepository.save(nuevaCita);
            
            System.out.println("✅ Cita creada exitosamente: ID=" + citaGuardada.getId() + 
                             " - Fecha=" + citaGuardada.getFechaHora());
            
            return CitaResult.success("Cita agendada exitosamente", citaGuardada);
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear cita: " + e.getMessage());
            e.printStackTrace();
            return CitaResult.error("Error interno al crear la cita: " + e.getMessage());
        }
    }

    // ============= ACTUALIZAR CITA =============
    
    /**
     * Actualiza una cita existente
     */
    public CitaResult actualizarCita(ActualizarCitaRequest request) {
        try {
            Optional<Cita> citaOpt = citaRepository.findById(request.getCitaId());
            if (!citaOpt.isPresent()) {
                return CitaResult.error("Cita no encontrada");
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita puede ser modificada
            if (!cita.puedeSerModificada()) {
                return CitaResult.error("La cita no puede ser modificada en su estado actual");
            }
            
            boolean huboChangios = false;
            
            // Actualizar fecha y hora si se proporciona
            if (request.getNuevaFechaHora() != null) {
                ValidationResult validacion = validarFechaHora(request.getNuevaFechaHora());
                if (!validacion.isValido()) {
                    return CitaResult.error(validacion.getMensaje());
                }
                
                // Verificar disponibilidad en la nueva fecha
                LocalDateTime fechaHoraFin = request.getNuevaFechaHora().plusMinutes(
                    request.getNuevaDuracion() != null ? request.getNuevaDuracion() : cita.getDuracionMinutos()
                );
                
                boolean hayConflicto = verificarConflictoExcluyendoCita(
                    cita.getDoctor(), request.getNuevaFechaHora(), fechaHoraFin, cita.getId()
                );
                
                if (hayConflicto) {
                    return CitaResult.error("El doctor no está disponible en el nuevo horario");
                }
                
                cita.setFechaHora(request.getNuevaFechaHora());
                huboChangios = true;
            }
            
            // Actualizar motivo si se proporciona
            if (request.getNuevoMotivo() != null && !request.getNuevoMotivo().trim().isEmpty()) {
                cita.setMotivo(request.getNuevoMotivo().trim());
                huboChangios = true;
            }
            
            // Actualizar duración si se proporciona
            if (request.getNuevaDuracion() != null) {
                if (request.getNuevaDuracion() < DURACION_MINIMA_MINUTOS || 
                    request.getNuevaDuracion() > DURACION_MAXIMA_MINUTOS) {
                    return CitaResult.error("Duración inválida");
                }
                cita.setDuracionMinutos(request.getNuevaDuracion());
                huboChangios = true;
            }
            
            // Actualizar estado si se proporciona
            if (request.getNuevoEstado() != null) {
                cita.setEstado(request.getNuevoEstado());
                if (request.getNuevoEstado() == EstadoCita.CANCELADA && 
                    request.getNotasCancelacion() != null) {
                    cita.setNotasCancelacion(request.getNotasCancelacion());
                }
                huboChangios = true;
            }
            
            if (!huboChangios) {
                return CitaResult.error("No se especificaron cambios para actualizar");
            }
            
            Cita citaActualizada = citaRepository.save(cita);
            return CitaResult.success("Cita actualizada exitosamente", citaActualizada);
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar cita: " + e.getMessage());
            return CitaResult.error("Error interno al actualizar la cita");
        }
    }

    // ============= CANCELAR CITA =============
    
    /**
     * Cancela una cita con notas
     */
    public CitaResult cancelarCita(Long citaId, Long usuarioId, String notas) {
        try {
            Optional<Cita> citaOpt = citaRepository.findByIdWithPacienteAndDoctor(citaId);
            if (!citaOpt.isPresent()) {
                return CitaResult.error("Cita no encontrada");
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar permisos (paciente o doctor pueden cancelar)
            if (!cita.getPaciente().getUsuarioId().equals(usuarioId) && 
                !cita.getDoctor().getUsuarioId().equals(usuarioId)) {
                return CitaResult.error("No tiene permisos para cancelar esta cita");
            }
            
            // Verificar que la cita no esté ya cancelada o completada
            if (cita.estaCancelada()) {
                return CitaResult.error("La cita ya está cancelada");
            }
            if (cita.estaCompletada()) {
                return CitaResult.error("No se puede cancelar una cita completada");
            }
            
            cita.cancelar(notas);
            citaRepository.save(cita);
            
            return CitaResult.success("Cita cancelada exitosamente", cita);
            
        } catch (Exception e) {
            System.err.println("❌ Error al cancelar cita: " + e.getMessage());
            return CitaResult.error("Error interno al cancelar la cita");
        }
    }

    // ============= CONFIRMAR CITA =============
    
    /**
     * Confirma una cita programada
     */
    public CitaResult confirmarCita(Long citaId, Long doctorId) {
        try {
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (!citaOpt.isPresent()) {
                return CitaResult.error("Cita no encontrada");
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenece al doctor
            if (!cita.getDoctor().getUsuarioId().equals(doctorId)) {
                return CitaResult.error("No tiene permisos para confirmar esta cita");
            }
            
            if (!cita.estaProgramada()) {
                return CitaResult.error("Solo se pueden confirmar citas programadas");
            }
            
            cita.confirmar();
            citaRepository.save(cita);
            
            return CitaResult.success("Cita confirmada exitosamente", cita);
            
        } catch (Exception e) {
            System.err.println("❌ Error al confirmar cita: " + e.getMessage());
            return CitaResult.error("Error interno al confirmar la cita");
        }
    }

    // ============= COMPLETAR CITA =============
    
    /**
     * Marca una cita como completada
     */
    public CitaResult completarCita(Long citaId, Long doctorId) {
        try {
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (!citaOpt.isPresent()) {
                return CitaResult.error("Cita no encontrada");
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenece al doctor
            if (!cita.getDoctor().getUsuarioId().equals(doctorId)) {
                return CitaResult.error("No tiene permisos para completar esta cita");
            }
            
            if (!cita.estaActiva()) {
                return CitaResult.error("Solo se pueden completar citas activas");
            }
            
            cita.completar();
            citaRepository.save(cita);
            
            return CitaResult.success("Cita completada exitosamente", cita);
            
        } catch (Exception e) {
            System.err.println("❌ Error al completar cita: " + e.getMessage());
            return CitaResult.error("Error interno al completar la cita");
        }
    }

    // ============= MARCAR NO ASISTIÓ =============
    
    /**
     * Marca una cita como no asistida
     */
    public CitaResult marcarNoAsistio(Long citaId, Long doctorId) {
        try {
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (!citaOpt.isPresent()) {
                return CitaResult.error("Cita no encontrada");
            }
            
            Cita cita = citaOpt.get();
            
            if (!cita.getDoctor().getUsuarioId().equals(doctorId)) {
                return CitaResult.error("No tiene permisos para marcar esta cita");
            }
            
            if (!cita.yaPaso()) {
                return CitaResult.error("Solo se pueden marcar como no asistidas las citas pasadas");
            }
            
            cita.marcarNoAsistio();
            citaRepository.save(cita);
            
            return CitaResult.success("Cita marcada como no asistida", cita);
            
        } catch (Exception e) {
            System.err.println("❌ Error al marcar no asistió: " + e.getMessage());
            return CitaResult.error("Error interno");
        }
    }

    // ============= CONSULTAS =============
    
    /**
     * Obtiene próximas citas de un paciente
     */
    public List<CitaDTO> obtenerProximasCitasPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (!pacienteOpt.isPresent()) {
            return List.of();
        }
        List<Cita> citas = citaRepository.findProximasCitasByPaciente(
            pacienteOpt.get(), LocalDateTime.now()
        );
        return citaMapper.toDTOList(citas);
    }
    
    /**
     * Obtiene historial completo de citas de un paciente
     */
    public List<CitaDTO> obtenerHistorialPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (!pacienteOpt.isPresent()) {
            return List.of();
        }
        List<Cita> citas = citaRepository.findByPacienteOrderByFechaHoraDesc(pacienteOpt.get());
        return citaMapper.toDTOList(citas);
    }
    
    /**
     * Obtiene citas de hoy de un doctor
     */
    public List<CitaDTO> obtenerCitasHoyDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return List.of();
        }
        List<Cita> citas = citaRepository.findCitasHoyByDoctor(doctorOpt.get());
        return citaMapper.toDTOList(citas);
    }
    
    /**
     * Obtiene próximas citas de un doctor
     */
    public List<CitaDTO> obtenerProximasCitasDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (!doctorOpt.isPresent()) {
            return List.of();
        }
        List<Cita> citas = citaRepository.findProximasCitasByDoctor(
            doctorOpt.get(), LocalDateTime.now()
        );
        return citaMapper.toDTOList(citas);
    }
    
    /**
     * Obtiene citas por fecha
     */
    public List<CitaDTO> obtenerCitasPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        List<Cita> citas = citaRepository.findByFechaHoraBetween(inicio, fin);
        return citaMapper.toDTOList(citas);
    }
    
    /**
     * Obtiene una cita por ID
     */
    public Optional<CitaDTO> obtenerCitaPorId(Long citaId) {
        Optional<Cita> citaOpt = citaRepository.findByIdWithPacienteAndDoctor(citaId);
        return citaOpt.map(citaMapper::toDTO);
    }

    // ============= VALIDACIONES PRIVADAS =============
    
    /**
     * Valida que la fecha y hora sean válidas para agendar una cita
     */
    private ValidationResult validarFechaHora(LocalDateTime fechaHora) {
        LocalDateTime ahora = LocalDateTime.now();
        
        // No puede ser en el pasado
        if (fechaHora.isBefore(ahora)) {
            return ValidationResult.invalid("No se puede agendar una cita en el pasado");
        }
        
        // Debe tener al menos anticipación mínima
        LocalDateTime minimaAnticipacion = ahora.plusMinutes(MINUTOS_ANTICIPACION_MINIMA);
        if (fechaHora.isBefore(minimaAnticipacion)) {
            return ValidationResult.invalid(
                String.format("Se requiere al menos %d minutos de anticipación", MINUTOS_ANTICIPACION_MINIMA)
            );
        }
        
        // No puede ser muy lejana en el futuro
        LocalDateTime maximaAnticipacion = ahora.plusDays(DIAS_ANTICIPACION_MAXIMA);
        if (fechaHora.isAfter(maximaAnticipacion)) {
            return ValidationResult.invalid(
                String.format("No se pueden agendar citas con más de %d días de anticipación", DIAS_ANTICIPACION_MAXIMA)
            );
        }
        
        // Validar horario laboral (8:00 AM - 8:00 PM)
        LocalTime hora = fechaHora.toLocalTime();
        if (hora.isBefore(LocalTime.of(8, 0)) || hora.isAfter(LocalTime.of(20, 0))) {
            return ValidationResult.invalid("Las citas solo pueden agendarse entre 8:00 AM y 8:00 PM");
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Verifica si un paciente tiene una cita en el horario especificado
     */
    private boolean verificarCitaPacienteEnHorario(Paciente paciente, 
                                                     LocalDateTime fechaInicio, 
                                                     LocalDateTime fechaFin) {
        List<Cita> citasPaciente = citaRepository.findProximasCitasByPaciente(paciente, fechaInicio);
        return citasPaciente.stream()
            .anyMatch(c -> {
                LocalDateTime citaInicio = c.getFechaHora();
                LocalDateTime citaFin = c.getFechaHoraFin();
                return (citaInicio.isBefore(fechaFin) && citaFin.isAfter(fechaInicio));
            });
    }
    
    /**
     * Verifica conflictos excluyendo una cita específica (para actualizaciones)
     */
    private boolean verificarConflictoExcluyendoCita(Doctor doctor, 
                                                       LocalDateTime fechaInicio,
                                                       LocalDateTime fechaFin,
                                                       Long citaIdExcluir) {
        List<Cita> citasDoctor = citaRepository.findProximasCitasByDoctor(doctor, fechaInicio.minusHours(3));
        return citasDoctor.stream()
            .filter(c -> !c.getId().equals(citaIdExcluir))
            .anyMatch(c -> {
                LocalDateTime citaInicio = c.getFechaHora();
                LocalDateTime citaFin = c.getFechaHoraFin();
                return (citaInicio.isBefore(fechaFin) && citaFin.isAfter(fechaInicio));
            });
    }

    // ============= CLASES AUXILIARES =============
    
    public static class CitaResult {
        private boolean exito;
        private String mensaje;
        private Cita cita;
        
        private CitaResult(boolean exito, String mensaje, Cita cita) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.cita = cita;
        }
        
        public static CitaResult success(String mensaje, Cita cita) {
            return new CitaResult(true, mensaje, cita);
        }
        
        public static CitaResult error(String mensaje) {
            return new CitaResult(false, mensaje, null);
        }
        
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public Cita getCita() { return cita; }
    }
    
    private static class ValidationResult {
        private boolean valido;
        private String mensaje;
        
        private ValidationResult(boolean valido, String mensaje) {
            this.valido = valido;
            this.mensaje = mensaje;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String mensaje) {
            return new ValidationResult(false, mensaje);
        }
        
        public boolean isValido() { return valido; }
        public String getMensaje() { return mensaje; }
    }
}
