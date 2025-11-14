package com.medipac.medipac.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medipac.medipac.model.Cita;
import com.medipac.medipac.model.Doctor;
import com.medipac.medipac.model.EstadoCita;
import com.medipac.medipac.model.Paciente;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    
    // Buscar citas por paciente
    List<Cita> findByPacienteOrderByFechaHoraDesc(Paciente paciente);
    
    // Buscar citas por doctor
    List<Cita> findByDoctorOrderByFechaHoraAsc(Doctor doctor);
    
    // Buscar citas por estado
    List<Cita> findByEstadoOrderByFechaHoraAsc(EstadoCita estado);
    
    // Buscar citas por fecha
    @Query("SELECT c FROM Cita c WHERE DATE(c.fechaHora) = :fecha ORDER BY c.fechaHora ASC")
    List<Cita> findByFecha(@Param("fecha") LocalDate fecha);
    
    // Buscar citas de hoy por doctor
    @Query("SELECT c FROM Cita c WHERE c.doctor = :doctor AND DATE(c.fechaHora) = CURRENT_DATE ORDER BY c.fechaHora ASC")
    List<Cita> findCitasHoyByDoctor(@Param("doctor") Doctor doctor);
    
    // Buscar próximas citas por paciente
    @Query("SELECT c FROM Cita c WHERE c.paciente = :paciente AND c.fechaHora >= :fechaHora AND c.estado IN ('PROGRAMADA', 'CONFIRMADA') ORDER BY c.fechaHora ASC")
    List<Cita> findProximasCitasByPaciente(@Param("paciente") Paciente paciente, @Param("fechaHora") LocalDateTime fechaHora);
    
    // Buscar próximas citas por doctor
    @Query("SELECT c FROM Cita c WHERE c.doctor = :doctor AND c.fechaHora >= :fechaHora AND c.estado IN ('PROGRAMADA', 'CONFIRMADA') ORDER BY c.fechaHora ASC")
    List<Cita> findProximasCitasByDoctor(@Param("doctor") Doctor doctor, @Param("fechaHora") LocalDateTime fechaHora);
    
    // Verificar disponibilidad de horario
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.doctor = :doctor AND c.fechaHora = :fechaHora AND c.estado IN ('PROGRAMADA', 'CONFIRMADA')")
    long countCitasEnHorario(@Param("doctor") Doctor doctor, @Param("fechaHora") LocalDateTime fechaHora);
    
    // Buscar citas de un doctor en un rango de fechas
    List<Cita> findByDoctorAndFechaHoraBetween(Doctor doctor, LocalDateTime inicio, LocalDateTime fin);
    
    // Verificar conflictos de horario considerando duración
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.doctor = :doctor " +
           "AND c.estado IN ('PROGRAMADA', 'CONFIRMADA') " +
           "AND (c.fechaHora < :fechaHoraFin AND c.fechaHora >= :fechaHora)")
    boolean existeConflictoHorario(@Param("doctor") Doctor doctor, 
                                   @Param("fechaHora") LocalDateTime fechaHora,
                                   @Param("fechaHoraFin") LocalDateTime fechaHoraFin);
    
    // Buscar citas en rango de fechas
    @Query("SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fechaHora ASC")
    List<Cita> findByFechaHoraBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    // Contar citas por estado y doctor
    long countByDoctorAndEstado(Doctor doctor, EstadoCita estado);
    
    // Contar citas por estado y paciente
    long countByPacienteAndEstado(Paciente paciente, EstadoCita estado);
    
    // Buscar historial de citas completadas por paciente
    @Query("SELECT c FROM Cita c WHERE c.paciente = :paciente AND c.estado = 'COMPLETADA' ORDER BY c.fechaHora DESC")
    List<Cita> findHistorialCompletoByPaciente(@Param("paciente") Paciente paciente);
    
    // Buscar citas pendientes por doctor para hoy y mañana
    @Query("SELECT c FROM Cita c WHERE c.doctor = :doctor AND DATE(c.fechaHora) BETWEEN CURRENT_DATE AND :fechaLimite AND c.estado IN ('PROGRAMADA', 'CONFIRMADA') ORDER BY c.fechaHora ASC")
    List<Cita> findCitasPendientesByDoctor(@Param("doctor") Doctor doctor, @Param("fechaLimite") LocalDate fechaLimite);
    
    // Buscar citas por paciente y doctor
    List<Cita> findByPacienteAndDoctorOrderByFechaHoraDesc(Paciente paciente, Doctor doctor);
    
    // Buscar cita por ID con paciente y doctor cargados
    @Query("SELECT c FROM Cita c JOIN FETCH c.paciente JOIN FETCH c.doctor WHERE c.id = :id")
    Optional<Cita> findByIdWithPacienteAndDoctor(@Param("id") Long id);
    
    // Obtener citas del día actual con estado específico
    @Query("SELECT c FROM Cita c WHERE DATE(c.fechaHora) = CURRENT_DATE AND c.estado = :estado ORDER BY c.fechaHora ASC")
    List<Cita> findCitasHoyByEstado(@Param("estado") EstadoCita estado);
    
    // Contar citas de un paciente con un doctor
    long countByPacienteAndDoctor(Paciente paciente, Doctor doctor);
    
    // Buscar citas que no asistieron en los últimos 30 días
    @Query("SELECT c FROM Cita c WHERE c.estado = 'NO_ASISTIO' AND c.fechaHora >= :fechaLimite")
    List<Cita> findCitasNoAsistioRecientes(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    // Obtener próximas citas de un doctor en un día específico
    @Query("SELECT c FROM Cita c WHERE c.doctor = :doctor AND DATE(c.fechaHora) = :fecha " +
           "AND c.estado IN ('PROGRAMADA', 'CONFIRMADA') ORDER BY c.fechaHora ASC")
    List<Cita> findCitasByDoctorAndFecha(@Param("doctor") Doctor doctor, @Param("fecha") LocalDate fecha);
    
    // Buscar citas canceladas con notas
    @Query("SELECT c FROM Cita c WHERE c.estado = 'CANCELADA' AND c.notasCancelacion IS NOT NULL ORDER BY c.fechaActualizacion DESC")
    List<Cita> findCitasCanceladasConNotas();
}