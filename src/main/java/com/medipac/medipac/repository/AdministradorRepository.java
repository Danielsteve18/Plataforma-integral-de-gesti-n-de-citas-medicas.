package com.medipac.medipac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medipac.medipac.model.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
}

