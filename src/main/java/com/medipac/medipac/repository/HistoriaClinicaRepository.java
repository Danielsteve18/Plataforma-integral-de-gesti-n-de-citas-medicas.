package com.medipac.medipac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medipac.medipac.model.HistoriaClinica;
import com.medipac.medipac.model.Paciente;
import com.medipac.medipac.model.Doctor;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {
    
    // Buscar historias clínicas por paciente
    @Query("SELECT hc FROM HistoriaClinica hc JOIN hc.cita c WHERE c.paciente = :paciente ORDER BY hc.fechaCreacion DESC")
    List<HistoriaClinica> findByPaciente(@Param("paciente") Paciente paciente);
    
    // Buscar historias clínicas por doctor
    @Query("SELECT hc FROM HistoriaClinica hc JOIN hc.cita c WHERE c.doctor = :doctor ORDER BY hc.fechaCreacion DESC")
    List<HistoriaClinica> findByDoctor(@Param("doctor") Doctor doctor);
    
    // Buscar historias clínicas con diagnóstico específico
    @Query("SELECT hc FROM HistoriaClinica hc WHERE LOWER(hc.diagnostico) LIKE LOWER(CONCAT('%', :diagnostico, '%')) ORDER BY hc.fechaCreacion DESC")
    List<HistoriaClinica> findByDiagnosticoContaining(@Param("diagnostico") String diagnostico);
    
    // Buscar historias clínicas por paciente y doctor
    @Query("SELECT hc FROM HistoriaClinica hc JOIN hc.cita c WHERE c.paciente = :paciente AND c.doctor = :doctor ORDER BY hc.fechaCreacion DESC")
    List<HistoriaClinica> findByPacienteAndDoctor(@Param("paciente") Paciente paciente, @Param("doctor") Doctor doctor);
    
    // Buscar última historia clínica de un paciente
    @Query("SELECT hc FROM HistoriaClinica hc JOIN hc.cita c WHERE c.paciente = :paciente ORDER BY hc.fechaCreacion DESC LIMIT 1")
    HistoriaClinica findUltimaHistoriaByPaciente(@Param("paciente") Paciente paciente);
    
    // Contar historias clínicas por paciente
    @Query("SELECT COUNT(hc) FROM HistoriaClinica hc JOIN hc.cita c WHERE c.paciente = :paciente")
    long countByPaciente(@Param("paciente") Paciente paciente);
    
    // Contar historias clínicas por doctor
    @Query("SELECT COUNT(hc) FROM HistoriaClinica hc JOIN hc.cita c WHERE c.doctor = :doctor")
    long countByDoctor(@Param("doctor") Doctor doctor);
}