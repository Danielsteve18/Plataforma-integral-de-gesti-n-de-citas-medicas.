package com.medipac.medipac.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinica {

    @Id
    @Column(name = "cita_id")
    private Long citaId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String prescripcion;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Constructor por defecto
    public HistoriaClinica() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros
    public HistoriaClinica(Cita cita, String diagnostico, String prescripcion, String notas) {
        this.cita = cita;
        this.diagnostico = diagnostico;
        this.prescripcion = prescripcion;
        this.notas = notas;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getPrescripcion() {
        return prescripcion;
    }

    public void setPrescripcion(String prescripcion) {
        this.prescripcion = prescripcion;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    // Métodos de conveniencia
    public boolean tieneDiagnostico() {
        return diagnostico != null && !diagnostico.trim().isEmpty();
    }

    public boolean tienePrescripcion() {
        return prescripcion != null && !prescripcion.trim().isEmpty();
    }

    public boolean tieneNotas() {
        return notas != null && !notas.trim().isEmpty();
    }
}