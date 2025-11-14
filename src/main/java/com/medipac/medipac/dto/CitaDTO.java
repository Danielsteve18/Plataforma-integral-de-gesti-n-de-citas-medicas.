package com.medipac.medipac.dto;

import com.medipac.medipac.model.EstadoCita;
import java.time.LocalDateTime;

/**
 * DTO para transferir información de citas
 */
public class CitaDTO {
    
    private Long id;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteApellido;
    private Long doctorId;
    private String doctorNombre;
    private String doctorApellido;
    private String especialidad;
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private String motivo;
    private EstadoCita estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String notasCancelacion;
    private boolean tieneHistoriaClinica;

    // Constructores
    public CitaDTO() {}

    public CitaDTO(Long id, Long pacienteId, String pacienteNombre, String pacienteApellido,
                   Long doctorId, String doctorNombre, String doctorApellido, String especialidad,
                   LocalDateTime fechaHora, Integer duracionMinutos, String motivo,
                   EstadoCita estado, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
                   String notasCancelacion, boolean tieneHistoriaClinica) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.pacienteNombre = pacienteNombre;
        this.pacienteApellido = pacienteApellido;
        this.doctorId = doctorId;
        this.doctorNombre = doctorNombre;
        this.doctorApellido = doctorApellido;
        this.especialidad = especialidad;
        this.fechaHora = fechaHora;
        this.duracionMinutos = duracionMinutos;
        this.motivo = motivo;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.notasCancelacion = notasCancelacion;
        this.tieneHistoriaClinica = tieneHistoriaClinica;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public String getPacienteApellido() {
        return pacienteApellido;
    }

    public void setPacienteApellido(String pacienteApellido) {
        this.pacienteApellido = pacienteApellido;
    }

    public String getPacienteNombreCompleto() {
        return pacienteNombre + " " + pacienteApellido;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorNombre() {
        return doctorNombre;
    }

    public void setDoctorNombre(String doctorNombre) {
        this.doctorNombre = doctorNombre;
    }

    public String getDoctorApellido() {
        return doctorApellido;
    }

    public void setDoctorApellido(String doctorApellido) {
        this.doctorApellido = doctorApellido;
    }

    public String getDoctorNombreCompleto() {
        return doctorNombre + " " + doctorApellido;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public LocalDateTime getFechaHoraFin() {
        if (fechaHora == null || duracionMinutos == null) {
            return fechaHora;
        }
        return fechaHora.plusMinutes(duracionMinutos);
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getNotasCancelacion() {
        return notasCancelacion;
    }

    public void setNotasCancelacion(String notasCancelacion) {
        this.notasCancelacion = notasCancelacion;
    }

    public boolean isTieneHistoriaClinica() {
        return tieneHistoriaClinica;
    }

    public void setTieneHistoriaClinica(boolean tieneHistoriaClinica) {
        this.tieneHistoriaClinica = tieneHistoriaClinica;
    }

    @Override
    public String toString() {
        return "CitaDTO{" +
                "id=" + id +
                ", pacienteNombreCompleto='" + getPacienteNombreCompleto() + '\'' +
                ", doctorNombreCompleto='" + getDoctorNombreCompleto() + '\'' +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado +
                '}';
    }
}
