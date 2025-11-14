package com.medipac.medipac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.medipac.medipac.model.Usuario;
import com.medipac.medipac.repository.UsuarioRepository;

import java.util.Collections;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Intentando cargar usuario: " + username);
        
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario no encontrado: " + username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        System.out.println("✅ Usuario encontrado: " + usuario.getUsername());
        System.out.println("📧 Email: " + usuario.getEmail());
        System.out.println("🔐 Password hash: " + usuario.getPassword().substring(0, 10) + "...");
        System.out.println("👤 Rol: " + usuario.getRol());
        System.out.println("🔑 Autoridad: ROLE_" + usuario.getRol());

        String authority = "ROLE_" + usuario.getRol();
        UserDetails userDetails = User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                .build();
        
        System.out.println("🎯 UserDetails creado exitosamente para: " + username);
        System.out.println("🔐 Autoridad asignada: " + authority);
        System.out.println("📋 Authorities del UserDetails: " + userDetails.getAuthorities());
        return userDetails;
    }

    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}