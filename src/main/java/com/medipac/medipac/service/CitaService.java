package com.medipac.medipac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medipac.medipac.model.*;
import com.medipac.medipac.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private HorarioRepository horarioRepository;

    // ============= MÉTODOS PARA PACIENTES =============
    
    /**
     * Agendar una nueva cita
     */
    public CitaResult agendarCita(Long pacienteId, Long doctorId, LocalDateTime fechaHora, String motivo) {
        try {
            // Validar que existan el paciente y doctor
            Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            
            if (!pacienteOpt.isPresent()) {
                return new CitaResult(false, "Paciente no encontrado", null);
            }
            if (!doctorOpt.isPresent()) {
                return new CitaResult(false, "Doctor no encontrado", null);
            }
            
            Paciente paciente = pacienteOpt.get();
            Doctor doctor = doctorOpt.get();
            
            // Validar que la fecha no sea en el pasado
            if (fechaHora.isBefore(LocalDateTime.now())) {
                return new CitaResult(false, "No se puede agendar una cita en el pasado", null);
            }
            
            // Verificar disponibilidad del doctor
            long citasEnHorario = citaRepository.countCitasEnHorario(doctor, fechaHora);
            if (citasEnHorario > 0) {
                return new CitaResult(false, "El doctor no está disponible en ese horario", null);
            }
            
            // Crear y guardar la cita
            Cita nuevaCita = new Cita(paciente, doctor, fechaHora, motivo);
            Cita citaGuardada = citaRepository.save(nuevaCita);
            
            System.out.println("✅ Cita agendada: " + citaGuardada.getId() + " - " + fechaHora);
            return new CitaResult(true, "Cita agendada exitosamente", citaGuardada);
            
        } catch (Exception e) {
            System.err.println("❌ Error al agendar cita: " + e.getMessage());
            return new CitaResult(false, "Error interno al agendar la cita", null);
        }
    }
    
    /**
     * Obtener próximas citas de un paciente
     */
    public List<Cita> obtenerProximasCitasPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (pacienteOpt.isPresent()) {
            return citaRepository.findProximasCitasByPaciente(pacienteOpt.get(), LocalDateTime.now());
        }
        return List.of();
    }
    
    /**
     * Obtener historial completo de citas de un paciente
     */
    public List<Cita> obtenerHistorialPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (pacienteOpt.isPresent()) {
            return citaRepository.findByPacienteOrderByFechaHoraDesc(pacienteOpt.get());
        }
        return List.of();
    }
    
    /**
     * Cancelar una cita (solo si es futura)
     */
    public CitaResult cancelarCita(Long citaId, Long pacienteId) {
        try {
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (!citaOpt.isPresent()) {
                return new CitaResult(false, "Cita no encontrada", null);
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenece al paciente
            if (!cita.getPaciente().getUsuarioId().equals(pacienteId)) {
                return new CitaResult(false, "No tiene permisos para cancelar esta cita", null);
            }
            
            // Verificar que la cita no sea en el pasado
            if (cita.getFechaHora().isBefore(LocalDateTime.now())) {
                return new CitaResult(false, "No se puede cancelar una cita pasada", null);
            }
            
            // Verificar que no esté ya cancelada o completada
            if (cita.estaCancelada() || cita.estaCompletada()) {
                return new CitaResult(false, "La cita ya está cancelada o completada", null);
            }
            
            cita.cancelar();
            citaRepository.save(cita);
            
            return new CitaResult(true, "Cita cancelada exitosamente", cita);
            
        } catch (Exception e) {
            System.err.println("❌ Error al cancelar cita: " + e.getMessage());
            return new CitaResult(false, "Error interno al cancelar la cita", null);
        }
    }

    // ============= MÉTODOS PARA DOCTORES =============
    
    /**
     * Obtener citas de hoy para un doctor
     */
    public List<Cita> obtenerCitasHoyDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            return citaRepository.findCitasHoyByDoctor(doctorOpt.get());
        }
        return List.of();
    }
    
    /**
     * Obtener próximas citas de un doctor
     */
    public List<Cita> obtenerProximasCitasDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            return citaRepository.findProximasCitasByDoctor(doctorOpt.get(), LocalDateTime.now());
        }
        return List.of();
    }
    
    /**
     * Obtener todas las citas de un doctor
     */
    public List<Cita> obtenerTodasCitasDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            return citaRepository.findByDoctorOrderByFechaHoraAsc(doctorOpt.get());
        }
        return List.of();
    }
    
    /**
     * Obtener todas las citas del sistema (para administradores)
     */
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAll();
    }
    
    /**
     * Confirmar una cita
     */
    public CitaResult confirmarCita(Long citaId, Long doctorId) {
        try {
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (!citaOpt.isPresent()) {
                return new CitaResult(false, "Cita no encontrada", null);
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenece al doctor
            if (!cita.getDoctor().getUsuarioId().equals(doctorId)) {
                return new CitaResult(false, "No tiene permisos para confirmar esta cita", null);
            }
            
            cita.confirmar();
            citaRepository.save(cita);
            
            return new CitaResult(true, "Cita confirmada exitosamente", cita);
            
        } catch (Exception e) {
            System.err.println("❌ Error al confirmar cita: " + e.getMessage());
            return new CitaResult(false, "Error interno al confirmar la cita", null);
        }
    }
    
    /**
     * Completar una cita
     */
    public CitaResult completarCita(Long citaId, Long doctorId) {
        try {
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (!citaOpt.isPresent()) {
                return new CitaResult(false, "Cita no encontrada", null);
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenece al doctor
            if (!cita.getDoctor().getUsuarioId().equals(doctorId)) {
                return new CitaResult(false, "No tiene permisos para completar esta cita", null);
            }
            
            cita.completar();
            citaRepository.save(cita);
            
            return new CitaResult(true, "Cita completada exitosamente", cita);
            
        } catch (Exception e) {
            System.err.println("❌ Error al completar cita: " + e.getMessage());
            return new CitaResult(false, "Error interno al completar la cita", null);
        }
    }

    // ============= MÉTODOS GENERALES =============
    
    /**
     * Obtener citas por fecha
     */
    public List<Cita> obtenerCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
    
    /**
     * Obtener estadísticas de citas por doctor
     */
    public EstadisticasCitas obtenerEstadisticasDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            
            long programadas = citaRepository.countByDoctorAndEstado(doctor, EstadoCita.PROGRAMADA);
            long confirmadas = citaRepository.countByDoctorAndEstado(doctor, EstadoCita.CONFIRMADA);
            long completadas = citaRepository.countByDoctorAndEstado(doctor, EstadoCita.COMPLETADA);
            long canceladas = citaRepository.countByDoctorAndEstado(doctor, EstadoCita.CANCELADA);
            
            return new EstadisticasCitas(programadas, confirmadas, completadas, canceladas);
        }
        return new EstadisticasCitas(0, 0, 0, 0);
    }
    
    /**
     * Obtener estadísticas de citas por paciente
     */
    public EstadisticasCitas obtenerEstadisticasPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (pacienteOpt.isPresent()) {
            Paciente paciente = pacienteOpt.get();
            
            long programadas = citaRepository.countByPacienteAndEstado(paciente, EstadoCita.PROGRAMADA);
            long confirmadas = citaRepository.countByPacienteAndEstado(paciente, EstadoCita.CONFIRMADA);
            long completadas = citaRepository.countByPacienteAndEstado(paciente, EstadoCita.COMPLETADA);
            long canceladas = citaRepository.countByPacienteAndEstado(paciente, EstadoCita.CANCELADA);
            
            return new EstadisticasCitas(programadas, confirmadas, completadas, canceladas);
        }
        return new EstadisticasCitas(0, 0, 0, 0);
    }

    // ============= CLASES AUXILIARES =============
    
    public static class CitaResult {
        private boolean exito;
        private String mensaje;
        private Cita cita;
        
        public CitaResult(boolean exito, String mensaje, Cita cita) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.cita = cita;
        }
        
        // Getters
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public Cita getCita() { return cita; }
    }
    
    public static class EstadisticasCitas {
        private long programadas;
        private long confirmadas;
        private long completadas;
        private long canceladas;
        
        public EstadisticasCitas(long programadas, long confirmadas, long completadas, long canceladas) {
            this.programadas = programadas;
            this.confirmadas = confirmadas;
            this.completadas = completadas;
            this.canceladas = canceladas;
        }
        
        // Getters
        public long getProgramadas() { return programadas; }
        public long getConfirmadas() { return confirmadas; }
        public long getCompletadas() { return completadas; }
        public long getCanceladas() { return canceladas; }
        public long getTotal() { return programadas + confirmadas + completadas + canceladas; }
    }
}