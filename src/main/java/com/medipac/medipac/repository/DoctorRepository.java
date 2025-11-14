package com.medipac.medipac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medipac.medipac.model.Doctor;
import com.medipac.medipac.model.Especialidad;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    // Buscar todos los doctores con sus especialidades cargadas
    @Override
    @EntityGraph(attributePaths = {"especialidades"})
    List<Doctor> findAll();
    
    // Buscar doctor por número de licencia
    Optional<Doctor> findByNumeroLicencia(String numeroLicencia);
    
    // Buscar doctor por nombre completo
    @Query("SELECT d FROM Doctor d WHERE LOWER(CONCAT(d.nombre, ' ', d.apellido)) LIKE LOWER(CONCAT('%', :nombreCompleto, '%'))")
    List<Doctor> findByNombreCompletoContaining(@Param("nombreCompleto") String nombreCompleto);
    
    // Buscar doctores por especialidad
    @Query("SELECT d FROM Doctor d JOIN d.especialidades e WHERE e = :especialidad")
    List<Doctor> findByEspecialidad(@Param("especialidad") Especialidad especialidad);
    
    // Buscar doctores por nombre de especialidad
    @EntityGraph(attributePaths = {"especialidades"})
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.especialidades e WHERE LOWER(e.nombre) = LOWER(:nombreEspecialidad)")
    List<Doctor> findByEspecialidadNombre(@Param("nombreEspecialidad") String nombreEspecialidad);
    
    // Buscar doctores disponibles (que tengan horarios disponibles)
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.horarios h WHERE h.disponible = true AND h.fecha >= CURRENT_DATE")
    List<Doctor> findDoctoresDisponibles();
    
    // Buscar doctor por usuario ID
    Optional<Doctor> findByUsuarioId(Long usuarioId);
    
    // Contar doctores por especialidad
    @Query("SELECT COUNT(d) FROM Doctor d JOIN d.especialidades e WHERE e.id = :especialidadId")
    long countByEspecialidadId(@Param("especialidadId") Long especialidadId);
}