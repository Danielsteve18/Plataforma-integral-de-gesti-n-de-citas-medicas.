package com.medipac.medipac.repository;

import com.medipac.medipac.model.DoctorPacienteFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorPacienteFavoritoRepository extends JpaRepository<DoctorPacienteFavorito, Long> {
    
    @Query("SELECT f FROM DoctorPacienteFavorito f WHERE f.doctor.usuarioId = :doctorId AND f.paciente.usuarioId = :pacienteId")
    Optional<DoctorPacienteFavorito> findByDoctorIdAndPacienteId(@Param("doctorId") Long doctorId, @Param("pacienteId") Long pacienteId);
    
    @Query("SELECT f.paciente.usuarioId FROM DoctorPacienteFavorito f WHERE f.doctor.usuarioId = :doctorId")
    List<Long> findPacienteIdsByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT f FROM DoctorPacienteFavorito f WHERE f.doctor.usuarioId = :doctorId")
    List<DoctorPacienteFavorito> findByDoctorId(@Param("doctorId") Long doctorId);
    
    boolean existsByDoctorUsuarioIdAndPacienteUsuarioId(Long doctorId, Long pacienteId);
}
