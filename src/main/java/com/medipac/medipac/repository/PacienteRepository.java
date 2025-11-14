package com.medipac.medipac.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medipac.medipac.model.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
    @Query("SELECT p FROM Paciente p WHERE p.usuario.username = :username")
    Optional<Paciente> findByUsuarioUsername(@Param("username") String username);
    
}