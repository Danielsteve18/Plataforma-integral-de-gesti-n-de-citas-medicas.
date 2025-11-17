package com.medipac.medipac.repository;

import com.medipac.medipac.model.Prescripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescripcionRepository extends JpaRepository<Prescripcion, Long> {
    
    @Query("SELECT p FROM Prescripcion p WHERE p.cita.doctor.usuarioId = :doctorId ORDER BY p.fechaCreacion DESC")
    List<Prescripcion> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT p FROM Prescripcion p WHERE p.cita.id = :citaId")
    Optional<Prescripcion> findByCitaId(@Param("citaId") Long citaId);
    
    @Query("SELECT p FROM Prescripcion p WHERE p.cita.paciente.usuarioId = :pacienteId ORDER BY p.fechaCreacion DESC")
    List<Prescripcion> findByPacienteId(@Param("pacienteId") Long pacienteId);
}
