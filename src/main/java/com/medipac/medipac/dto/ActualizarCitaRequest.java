package com.medipac.medipac.dto;

import com.medipac.medipac.model.EstadoCita;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO para actualizar una cita existente
 */
public class ActualizarCitaRequest {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long citaId;

    @Future(message = "La nueva fecha de la cita debe ser futura")
    private LocalDateTime nuevaFechaHora;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String nuevoMotivo;

    private EstadoCita nuevoEstado;

    @Size(max = 500, message = "Las notas de cancelación no pueden exceder 500 caracteres")
    private String notasCancelacion;

    @Min(value = 15, message = "La duración mínima de una cita es 15 minutos")
    @Max(value = 180, message = "La duración máxima de una cita es 180 minutos")
    private Integer nuevaDuracion;

    // Constructores
    public ActualizarCitaRequest() {}

    public ActualizarCitaRequest(Long citaId) {
        this.citaId = citaId;
    }

    // Getters y Setters
    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public LocalDateTime getNuevaFechaHora() {
        return nuevaFechaHora;
    }

    public void setNuevaFechaHora(LocalDateTime nuevaFechaHora) {
        this.nuevaFechaHora = nuevaFechaHora;
    }

    public String getNuevoMotivo() {
        return nuevoMotivo;
    }

    public void setNuevoMotivo(String nuevoMotivo) {
        this.nuevoMotivo = nuevoMotivo;
    }

    public EstadoCita getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadoCita nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getNotasCancelacion() {
        return notasCancelacion;
    }

    public void setNotasCancelacion(String notasCancelacion) {
        this.notasCancelacion = notasCancelacion;
    }

    public Integer getNuevaDuracion() {
        return nuevaDuracion;
    }

    public void setNuevaDuracion(Integer nuevaDuracion) {
        this.nuevaDuracion = nuevaDuracion;
    }

    @Override
    public String toString() {
        return "ActualizarCitaRequest{" +
                "citaId=" + citaId +
                ", nuevaFechaHora=" + nuevaFechaHora +
                ", nuevoEstado=" + nuevoEstado +
                '}';
    }
}
