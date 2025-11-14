package com.medipac.medipac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medipac.medipac.model.*;
import com.medipac.medipac.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HistoriaClinicaService {

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;
    
    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;

    // ============= MÉTODOS PARA DOCTORES =============
    
    /**
     * Crear historia clínica para una cita completada
     */
    public HistoriaResult crearHistoriaClinica(Long citaId, Long doctorId, String diagnostico, String prescripcion, String notas) {
        try {
            // Validar que la cita existe
            Optional<Cita> citaOpt = citaRepository.findById(citaId);
            if (!citaOpt.isPresent()) {
                return new HistoriaResult(false, "Cita no encontrada", null);
            }
            
            Cita cita = citaOpt.get();
            
            // Verificar que la cita pertenece al doctor
            if (!cita.getDoctor().getUsuarioId().equals(doctorId)) {
                return new HistoriaResult(false, "No tiene permisos para crear historia clínica de esta cita", null);
            }
            
            // Verificar que la cita esté completada
            if (!cita.estaCompletada()) {
                return new HistoriaResult(false, "Solo se puede crear historia clínica para citas completadas", null);
            }
            
            // Verificar que no exista ya una historia clínica para esta cita
            if (cita.getHistoriaClinica() != null) {
                return new HistoriaResult(false, "Ya existe una historia clínica para esta cita", null);
            }
            
            // Crear y guardar historia clínica
            HistoriaClinica nuevaHistoria = new HistoriaClinica(cita, diagnostico, prescripcion, notas);
            HistoriaClinica historiaGuardada = historiaClinicaRepository.save(nuevaHistoria);
            
            System.out.println("✅ Historia clínica creada para cita: " + citaId);
            return new HistoriaResult(true, "Historia clínica creada exitosamente", historiaGuardada);
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear historia clínica: " + e.getMessage());
            return new HistoriaResult(false, "Error interno al crear la historia clínica", null);
        }
    }
    
    /**
     * Actualizar historia clínica existente
     */
    public HistoriaResult actualizarHistoriaClinica(Long historiaId, Long doctorId, String diagnostico, String prescripcion, String notas) {
        try {
            Optional<HistoriaClinica> historiaOpt = historiaClinicaRepository.findById(historiaId);
            if (!historiaOpt.isPresent()) {
                return new HistoriaResult(false, "Historia clínica no encontrada", null);
            }
            
            HistoriaClinica historia = historiaOpt.get();
            
            // Verificar que la historia pertenece al doctor
            if (!historia.getCita().getDoctor().getUsuarioId().equals(doctorId)) {
                return new HistoriaResult(false, "No tiene permisos para modificar esta historia clínica", null);
            }
            
            // Actualizar campos
            historia.setDiagnostico(diagnostico);
            historia.setPrescripcion(prescripcion);
            historia.setNotas(notas);
            
            HistoriaClinica historiaActualizada = historiaClinicaRepository.save(historia);
            
            System.out.println("✅ Historia clínica actualizada: " + historiaId);
            return new HistoriaResult(true, "Historia clínica actualizada exitosamente", historiaActualizada);
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar historia clínica: " + e.getMessage());
            return new HistoriaResult(false, "Error interno al actualizar la historia clínica", null);
        }
    }
    
    /**
     * Obtener todas las historias clínicas creadas por un doctor
     */
    public List<HistoriaClinica> obtenerHistoriasPorDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            return historiaClinicaRepository.findByDoctor(doctorOpt.get());
        }
        return List.of();
    }

    // ============= MÉTODOS PARA PACIENTES =============
    
    /**
     * Obtener historial médico completo de un paciente
     */
    public List<HistoriaClinica> obtenerHistorialPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (pacienteOpt.isPresent()) {
            return historiaClinicaRepository.findByPaciente(pacienteOpt.get());
        }
        return List.of();
    }
    
    /**
     * Obtener última historia clínica de un paciente
     */
    public HistoriaClinica obtenerUltimaHistoriaPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (pacienteOpt.isPresent()) {
            return historiaClinicaRepository.findUltimaHistoriaByPaciente(pacienteOpt.get());
        }
        return null;
    }
    
    /**
     * Obtener historias clínicas entre un paciente y doctor específico
     */
    public List<HistoriaClinica> obtenerHistorialPacienteConDoctor(Long pacienteId, Long doctorId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        
        if (pacienteOpt.isPresent() && doctorOpt.isPresent()) {
            return historiaClinicaRepository.findByPacienteAndDoctor(pacienteOpt.get(), doctorOpt.get());
        }
        return List.of();
    }

    // ============= MÉTODOS GENERALES =============
    
    /**
     * Buscar historias clínicas por diagnóstico
     */
    public List<HistoriaClinica> buscarPorDiagnostico(String diagnostico) {
        return historiaClinicaRepository.findByDiagnosticoContaining(diagnostico);
    }
    
    /**
     * Obtener estadísticas de historias clínicas por doctor
     */
    public EstadisticasHistoria obtenerEstadisticasDoctor(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            
            long totalHistorias = historiaClinicaRepository.countByDoctor(doctor);
            List<HistoriaClinica> historias = historiaClinicaRepository.findByDoctor(doctor);
            
            long conDiagnostico = historias.stream().mapToLong(h -> h.tieneDiagnostico() ? 1 : 0).sum();
            long conPrescripcion = historias.stream().mapToLong(h -> h.tienePrescripcion() ? 1 : 0).sum();
            long conNotas = historias.stream().mapToLong(h -> h.tieneNotas() ? 1 : 0).sum();
            
            return new EstadisticasHistoria(totalHistorias, conDiagnostico, conPrescripcion, conNotas);
        }
        return new EstadisticasHistoria(0, 0, 0, 0);
    }
    
    /**
     * Obtener estadísticas de historias clínicas por paciente
     */
    public EstadisticasHistoria obtenerEstadisticasPaciente(Long pacienteId) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);
        if (pacienteOpt.isPresent()) {
            Paciente paciente = pacienteOpt.get();
            
            long totalHistorias = historiaClinicaRepository.countByPaciente(paciente);
            List<HistoriaClinica> historias = historiaClinicaRepository.findByPaciente(paciente);
            
            long conDiagnostico = historias.stream().mapToLong(h -> h.tieneDiagnostico() ? 1 : 0).sum();
            long conPrescripcion = historias.stream().mapToLong(h -> h.tienePrescripcion() ? 1 : 0).sum();
            long conNotas = historias.stream().mapToLong(h -> h.tieneNotas() ? 1 : 0).sum();
            
            return new EstadisticasHistoria(totalHistorias, conDiagnostico, conPrescripcion, conNotas);
        }
        return new EstadisticasHistoria(0, 0, 0, 0);
    }
    
    /**
     * Verificar si un doctor puede acceder a una historia clínica
     */
    public boolean puedeAccederHistoria(Long historiaId, Long doctorId) {
        Optional<HistoriaClinica> historiaOpt = historiaClinicaRepository.findById(historiaId);
        if (historiaOpt.isPresent()) {
            return historiaOpt.get().getCita().getDoctor().getUsuarioId().equals(doctorId);
        }
        return false;
    }
    
    /**
     * Verificar si un paciente puede acceder a una historia clínica
     */
    public boolean pacientePuedeAccederHistoria(Long historiaId, Long pacienteId) {
        Optional<HistoriaClinica> historiaOpt = historiaClinicaRepository.findById(historiaId);
        if (historiaOpt.isPresent()) {
            return historiaOpt.get().getCita().getPaciente().getUsuarioId().equals(pacienteId);
        }
        return false;
    }

    // ============= CLASES AUXILIARES =============
    
    public static class HistoriaResult {
        private boolean exito;
        private String mensaje;
        private HistoriaClinica historia;
        
        public HistoriaResult(boolean exito, String mensaje, HistoriaClinica historia) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.historia = historia;
        }
        
        // Getters
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public HistoriaClinica getHistoria() { return historia; }
    }
    
    public static class EstadisticasHistoria {
        private long totalHistorias;
        private long conDiagnostico;
        private long conPrescripcion;
        private long conNotas;
        
        public EstadisticasHistoria(long totalHistorias, long conDiagnostico, long conPrescripcion, long conNotas) {
            this.totalHistorias = totalHistorias;
            this.conDiagnostico = conDiagnostico;
            this.conPrescripcion = conPrescripcion;
            this.conNotas = conNotas;
        }
        
        // Getters
        public long getTotalHistorias() { return totalHistorias; }
        public long getConDiagnostico() { return conDiagnostico; }
        public long getConPrescripcion() { return conPrescripcion; }
        public long getConNotas() { return conNotas; }
    }
}