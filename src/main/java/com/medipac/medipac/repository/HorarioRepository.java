package com.medipac.medipac.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medipac.medipac.model.Horario;
import com.medipac.medipac.model.Doctor;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    
    // Buscar horarios por doctor
    List<Horario> findByDoctorOrderByFechaAscHoraInicioAsc(Doctor doctor);
    
    // Buscar horarios disponibles por doctor
    List<Horario> findByDoctorAndDisponibleTrueOrderByFechaAscHoraInicioAsc(Doctor doctor);
    
    // Buscar horarios por fecha
    List<Horario> findByFechaOrderByHoraInicioAsc(LocalDate fecha);
    
    // Buscar horarios disponibles por fecha
    List<Horario> findByFechaAndDisponibleTrueOrderByHoraInicioAsc(LocalDate fecha);
    
    // Buscar horarios por doctor y fecha
    List<Horario> findByDoctorAndFechaOrderByHoraInicioAsc(Doctor doctor, LocalDate fecha);
    
    // Buscar horarios disponibles por doctor y fecha
    List<Horario> findByDoctorAndFechaAndDisponibleTrueOrderByHoraInicioAsc(Doctor doctor, LocalDate fecha);
    
    // Buscar horarios en rango de fechas
    @Query("SELECT h FROM Horario h WHERE h.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY h.fecha ASC, h.horaInicio ASC")
    List<Horario> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    // Buscar horarios disponibles para la próxima semana
    @Query("SELECT h FROM Horario h WHERE h.disponible = true AND h.fecha BETWEEN CURRENT_DATE AND :fechaLimite ORDER BY h.fecha ASC, h.horaInicio ASC")
    List<Horario> findHorariosDisponiblesProximaSemana(@Param("fechaLimite") LocalDate fechaLimite);
    
    // Verificar si existe un horario en conflicto
    @Query("SELECT COUNT(h) FROM Horario h WHERE h.doctor = :doctor AND h.fecha = :fecha AND " +
           "((h.horaInicio <= :horaInicio AND h.horaFin > :horaInicio) OR " +
           "(h.horaInicio < :horaFin AND h.horaFin >= :horaFin) OR " +
           "(h.horaInicio >= :horaInicio AND h.horaFin <= :horaFin))")
    long countHorariosEnConflicto(@Param("doctor") Doctor doctor, @Param("fecha") LocalDate fecha, 
                                  @Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin);
    
    // Buscar próximos horarios disponibles por doctor
    @Query("SELECT h FROM Horario h WHERE h.doctor = :doctor AND h.disponible = true AND " +
           "(h.fecha > CURRENT_DATE OR (h.fecha = CURRENT_DATE AND h.horaInicio > CURRENT_TIME)) " +
           "ORDER BY h.fecha ASC, h.horaInicio ASC")
    List<Horario> findProximosHorariosDisponibles(@Param("doctor") Doctor doctor);
    
    // Contar horarios disponibles por doctor
    long countByDoctorAndDisponibleTrue(Doctor doctor);
    
    // Buscar horarios por rango de horas en una fecha específica
    @Query("SELECT h FROM Horario h WHERE h.fecha = :fecha AND h.horaInicio BETWEEN :horaInicio AND :horaFin ORDER BY h.horaInicio ASC")
    List<Horario> findByFechaAndHoraBetween(@Param("fecha") LocalDate fecha, 
                                           @Param("horaInicio") LocalTime horaInicio, 
                                           @Param("horaFin") LocalTime horaFin);
    
    // Métodos para gestión de calendario semanal
    @Query("SELECT h FROM Horario h WHERE h.doctor = :doctor AND h.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY h.fecha ASC, h.horaInicio ASC")
    List<Horario> findByDoctorAndFechaBetween(@Param("doctor") Doctor doctor, 
                                               @Param("fechaInicio") LocalDate fechaInicio, 
                                               @Param("fechaFin") LocalDate fechaFin);
    
    // Eliminar horarios por doctor en rango de fechas
    void deleteByDoctorAndFechaBetween(Doctor doctor, LocalDate fechaInicio, LocalDate fechaFin);
}