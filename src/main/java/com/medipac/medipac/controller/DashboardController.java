package com.medipac.medipac.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String redirectDashboard() {
        // Esta ruta se manejará con Spring Security para redirigir según el rol
        return "redirect:/login";
    }
}