package com.medipac.medipac.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas", indexes = {
    @Index(name = "idx_cita_fecha_hora", columnList = "fecha_hora"),
    @Index(name = "idx_cita_estado", columnList = "estado"),
    @Index(name = "idx_cita_paciente_fecha", columnList = "paciente_id, fecha_hora"),
    @Index(name = "idx_cita_doctor_fecha", columnList = "doctor_id, fecha_hora")
})
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoCita estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "notas_cancelacion", length = 500)
    private String notasCancelacion;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos = 30; // Duración por defecto: 30 minutos

    @OneToOne(mappedBy = "cita", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HistoriaClinica historiaClinica;

    // Constructor por defecto
    public Cita() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = EstadoCita.PROGRAMADA;
        this.duracionMinutos = 30;
    }

    // Constructor con parámetros
    public Cita(Paciente paciente, Doctor doctor, LocalDateTime fechaHora, String motivo) {
        this.paciente = paciente;
        this.doctor = doctor;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = EstadoCita.PROGRAMADA;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.duracionMinutos = 30;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
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

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
        this.fechaActualizacion = LocalDateTime.now();
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

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoCita.PROGRAMADA;
        }
        if (duracionMinutos == null) {
            duracionMinutos = 30;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Métodos de conveniencia para estados
    public void programar() {
        this.estado = EstadoCita.PROGRAMADA;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void confirmar() {
        this.estado = EstadoCita.CONFIRMADA;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void completar() {
        this.estado = EstadoCita.COMPLETADA;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void cancelar() {
        this.estado = EstadoCita.CANCELADA;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void cancelar(String notas) {
        this.estado = EstadoCita.CANCELADA;
        this.notasCancelacion = notas;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void marcarNoAsistio() {
        this.estado = EstadoCita.NO_ASISTIO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Métodos de verificación de estado
    public boolean estaCompletada() {
        return EstadoCita.COMPLETADA.equals(estado);
    }

    public boolean estaCancelada() {
        return EstadoCita.CANCELADA.equals(estado);
    }

    public boolean estaProgramada() {
        return EstadoCita.PROGRAMADA.equals(estado);
    }

    public boolean estaConfirmada() {
        return EstadoCita.CONFIRMADA.equals(estado);
    }

    public boolean noAsistio() {
        return EstadoCita.NO_ASISTIO.equals(estado);
    }

    public boolean estaActiva() {
        return EstadoCita.PROGRAMADA.equals(estado) || EstadoCita.CONFIRMADA.equals(estado);
    }

    // Método para verificar si la cita puede ser modificada
    public boolean puedeSerModificada() {
        return !estaCompletada() && !estaCancelada() && !noAsistio();
    }

    // Método para verificar si la cita ya pasó
    public boolean yaPaso() {
        return fechaHora != null && fechaHora.isBefore(LocalDateTime.now());
    }

    // Método para obtener la fecha de fin de la cita
    public LocalDateTime getFechaHoraFin() {
        if (fechaHora == null || duracionMinutos == null) {
            return fechaHora;
        }
        return fechaHora.plusMinutes(duracionMinutos);
    }

    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado +
                ", duracionMinutos=" + duracionMinutos +
                '}';
    }
}