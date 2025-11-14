package com.medipac.medipac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medipac.medipac.model.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    
    // Buscar especialidad por nombre
    Optional<Especialidad> findByNombre(String nombre);
    
    // Buscar especialidades por nombre similar
    @Query("SELECT e FROM Especialidad e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Especialidad> findByNombreContaining(@Param("nombre") String nombre);
    
    // Buscar especialidades que tengan doctores disponibles
    @Query("SELECT DISTINCT e FROM Especialidad e JOIN e.doctores d JOIN d.horarios h WHERE h.disponible = true AND h.fecha >= CURRENT_DATE")
    List<Especialidad> findEspecialidadesConDoctoresDisponibles();
    
    // Contar doctores por especialidad
    @Query("SELECT e.nombre, COUNT(d) FROM Especialidad e LEFT JOIN e.doctores d GROUP BY e.id, e.nombre")
    List<Object[]> countDoctoresByEspecialidad();
    
    // Buscar especialidades más solicitadas (por número de citas)
    @Query("SELECT e, COUNT(c) as citaCount FROM Especialidad e " +
           "JOIN e.doctores d JOIN d.citas c " +
           "GROUP BY e.id ORDER BY citaCount DESC")
    List<Object[]> findEspecialidadesMasSolicitadas();
}