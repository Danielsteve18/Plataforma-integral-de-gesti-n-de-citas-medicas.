package com.medipac.medipac.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medipac.medipac.repository.AdministradorRepository;

@RestController
public class TestController {

    private final AdministradorRepository repo;

    public TestController(AdministradorRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/test-db")
    public String testDb() {
        long count = repo.count();
        return "✅ Conexión exitosa. Total de administradores: " + count;
    }
}
