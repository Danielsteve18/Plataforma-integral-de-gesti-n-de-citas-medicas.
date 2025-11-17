package com.medipac.medipac.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_paciente_favoritos")
public class DoctorPacienteFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    // Constructor por defecto
    public DoctorPacienteFavorito() {
        this.fechaAgregado = LocalDateTime.now();
    }

    // Constructor con parámetros
    public DoctorPacienteFavorito(Doctor doctor, Paciente paciente) {
        this.doctor = doctor;
        this.paciente = paciente;
        this.fechaAgregado = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
}
