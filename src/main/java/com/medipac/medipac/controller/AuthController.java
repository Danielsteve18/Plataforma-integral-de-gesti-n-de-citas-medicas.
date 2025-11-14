package com.medipac.medipac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medipac.medipac.model.Usuario;
import com.medipac.medipac.model.Paciente;
import com.medipac.medipac.model.RolUsuario;
import com.medipac.medipac.repository.UsuarioRepository;
import com.medipac.medipac.repository.PacienteRepository;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "admin", required = false) String admin,
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (admin != null) {
            model.addAttribute("info", "Acceso de administrador - Ingresa tus credenciales");
        }
        
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("success", "Has cerrado sesión exitosamente");
        }
        
        return "logins/login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "registro/registro";
    }

    @GetMapping("/admin")
    public String redirectToAdmin() {
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/register")
    @Transactional
    public String processRegister(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("🔍 Intentando registrar usuario: " + username);
        System.out.println("📧 Email: " + email);
        System.out.println("👤 Nombre: " + name);
        
        try {
            // Validar campos vacíos
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre completo es requerido");
                return "redirect:/register";
            }
            
            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El correo electrónico es requerido");
                return "redirect:/register";
            }
            
            if (username == null || username.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario es requerido");
                return "redirect:/register";
            }
            
            if (password == null || password.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La contraseña es requerida");
                return "redirect:/register";
            }

            // Validar username y email con validaciones básicas primero
            if (usuarioRepository.findByUsername(username.trim()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
                return "redirect:/register";
            }

            if (usuarioRepository.findByEmail(email.trim()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está registrado");
                return "redirect:/register";
            }

            // TODO: Activar validación avanzada después de solucionar el problema inicial
            // UsuarioValidationService.ValidationResult validation = validationService.validarUsuarioCompleto(username.trim(), email.trim());
            // if (!validation.isValid()) {
            //     redirectAttributes.addFlashAttribute("error", validation.getMessage());
            //     return "redirect:/register";
            // }

            // Crear nuevo usuario (tabla base)
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username.trim());
            nuevoUsuario.setEmail(email.trim());
            nuevoUsuario.setPassword(passwordEncoder.encode(password));
            nuevoUsuario.setRol(RolUsuario.PACIENTE.getValor()); // Por defecto es paciente

            // Guardar usuario primero para obtener el ID
            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

            // Crear registro de paciente (tabla relacionada)
            // Separar nombre completo en nombre y apellido
            String[] partesNombre = name.trim().split(" ", 2);
            String nombre = partesNombre[0];
            String apellido = partesNombre.length > 1 ? partesNombre[1] : "";

            Paciente nuevoPaciente = new Paciente();
            // Dentro de la misma transacción, podemos usar la referencia directa
            nuevoPaciente.setUsuario(usuarioGuardado);
            nuevoPaciente.setNombre(nombre);
            nuevoPaciente.setApellido(apellido);
            
            // Guardar paciente
            pacienteRepository.save(nuevoPaciente);

            redirectAttributes.addFlashAttribute("success", "Usuario registrado exitosamente. Ahora puedes iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            System.err.println("❌ Error en registro: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al registrar usuario: " + e.getMessage());
            return "redirect:/register";
        }
    }
}

