package com.medipac.medipac.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO para crear una nueva cita médica
 */
public class CrearCitaRequest {

    // Opcional: se puede obtener del usuario autenticado
    private Long pacienteId;

    @NotNull(message = "El ID del doctor es obligatorio")
    private Long doctorId;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La fecha de la cita debe ser futura")
    private LocalDateTime fechaHora;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    @Min(value = 15, message = "La duración mínima de una cita es 15 minutos")
    @Max(value = 180, message = "La duración máxima de una cita es 180 minutos")
    private Integer duracionMinutos = 30;

    // Constructores
    public CrearCitaRequest() {}

    public CrearCitaRequest(Long pacienteId, Long doctorId, LocalDateTime fechaHora, String motivo) {
        this.pacienteId = pacienteId;
        this.doctorId = doctorId;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.duracionMinutos = 30;
    }

    // Getters y Setters
    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    @Override
    public String toString() {
        return "CrearCitaRequest{" +
                "pacienteId=" + pacienteId +
                ", doctorId=" + doctorId +
                ", fechaHora=" + fechaHora +
                ", duracionMinutos=" + duracionMinutos +
                '}';
    }
}
