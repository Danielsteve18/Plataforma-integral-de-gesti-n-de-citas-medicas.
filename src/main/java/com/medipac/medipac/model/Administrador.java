package com.medipac.medipac.model;

import jakarta.persistence.*;

@Entity
@Table(name = "administradores")
public class Administrador {
    
    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Constructor por defecto
    public Administrador() {}

    // Constructor con parámetros
    public Administrador(Usuario usuario) {
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
