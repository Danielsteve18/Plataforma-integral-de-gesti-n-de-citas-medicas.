package com.medipac.medipac.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        System.out.println("🔑 Roles detectados: " + roles);
        
        if (roles.contains("ROLE_ADMINISTRADOR") || roles.contains("ROLE_ADMIN")) {
            System.out.println("✅ Redirigiendo administrador a /admin");
            response.sendRedirect("/admin");
        } else if (roles.contains("ROLE_DOCTOR")) {
            System.out.println("✅ Redirigiendo doctor a /doctor/dashboard");
            response.sendRedirect("/doctor/dashboard");
        } else if (roles.contains("ROLE_PACIENTE")) {
            System.out.println("✅ Redirigiendo paciente a /paciente/dashboard");
            response.sendRedirect("/paciente/dashboard");
        } else {
            System.out.println("❌ Rol no reconocido, redirigiendo a /");
            response.sendRedirect("/");
        }
    }
}