package com.medipac.medipac.controller;

import com.medipac.medipac.model.Doctor;
import com.medipac.medipac.repository.DoctorRepository;
import com.medipac.medipac.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador REST para generación de reportes PDF
 * Solo accesible para usuarios con rol DOCTOR o ADMINISTRADOR
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private com.medipac.medipac.repository.UsuarioRepository usuarioRepository;

    /**
     * Helper method to get doctor by username
     */
    private Doctor getDoctorByUsername(String username) {
        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return doctorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
    }

    /**
     * Genera reporte PDF de historia clínica de un paciente
     * 
     * @param pacienteId ID del paciente
     * @return Array de bytes del PDF generado
     * @throws Exception Si hay error en la generación
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReportController.class);

    @GetMapping("/doctor/historia-clinica/{pacienteId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> generarHistoriaClinicaPDF(
            @PathVariable Long pacienteId,
            Authentication authentication) {

        try {
            logger.info("Iniciando generación de PDF para paciente ID: {}", pacienteId);
            byte[] pdfBytes = reportService.generarHistoriaClinicaPDF(pacienteId);
            logger.info("PDF generado exitosamente, tamaño: {} bytes", pdfBytes.length);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "historia_clinica_paciente_" + pacienteId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            logger.error("Error: Paciente no encontrado con ID: {}", pacienteId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error interno generando PDF para paciente ID: {}", pacienteId, e);
            e.printStackTrace();
            String errorMessage = "Error generando reporte: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Genera reporte PDF de agenda del doctor
     * Solo accesible para doctores
     */
    @GetMapping("/doctor/agenda")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> generarAgendaDoctorPDF(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Authentication authentication) {

        try {
            // Obtener el doctor logueado
            String username = authentication.getName();
            Doctor doctor = getDoctorByUsername(username);

            // Fechas por defecto: última semana
            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio, DateTimeFormatter.ISO_DATE)
                    : LocalDate.now().minusWeeks(1);

            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin, DateTimeFormatter.ISO_DATE) : LocalDate.now();

            byte[] pdfBytes = reportService.generarAgendaDoctorPDF(
                    doctor.getUsuarioId(), inicio, fin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "agenda_doctor_" + inicio + "_" + fin + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error generando agenda doctor", e);
            String errorMessage = "Error generando reporte: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Genera reporte PDF de lista de pacientes del doctor
     * Solo accesible para doctores
     */
    @GetMapping("/doctor/pacientes")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> generarListaPacientesPDF(Authentication authentication) {

        try {
            // Obtener el doctor logueado
            String username = authentication.getName();
            Doctor doctor = getDoctorByUsername(username);

            byte[] pdfBytes = reportService.generarListaPacientesPDF(doctor.getUsuarioId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "lista_pacientes_" + LocalDate.now() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error generando lista pacientes", e);
            String errorMessage = "Error generando reporte: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Genera reporte de citas del día para el doctor
     * Solo accesible para doctores
     */
    @GetMapping("/doctor/citas-hoy")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> generarCitasHoyPDF(Authentication authentication) {

        try {
            String username = authentication.getName();
            Doctor doctor = getDoctorByUsername(username);

            LocalDate hoy = LocalDate.now();
            byte[] pdfBytes = reportService.generarAgendaDoctorPDF(
                    doctor.getUsuarioId(), hoy, hoy);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "citas_hoy_" + hoy + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error generando citas hoy", e);
            String errorMessage = "Error generando reporte: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Genera reporte mensual de citas para el doctor
     * Solo accesible para doctores
     */
    @GetMapping("/doctor/reporte-mensual")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> generarReporteMensualPDF(Authentication authentication) {

        try {
            String username = authentication.getName();
            Doctor doctor = getDoctorByUsername(username);

            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

            byte[] pdfBytes = reportService.generarAgendaDoctorPDF(
                    doctor.getUsuarioId(), inicioMes, finMes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "reporte_mensual_" + hoy.getMonthValue() + "_" + hoy.getYear() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error generando reporte mensual", e);
            String errorMessage = "Error generando reporte: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    // --- REPORTES ADMINISTRADOR ---

    @GetMapping("/admin/pacientes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> generarReportePacientesAdmin() {
        try {
            logger.info("Iniciando generación de reporte global de pacientes para admin");
            byte[] pdfBytes = reportService.generarReportePacientesAdmin();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "reporte_pacientes_global_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"))
                            + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generando reporte de pacientes admin", e);
            String errorMessage = "Error generando reporte: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/admin/doctores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> generarReporteDoctoresAdmin() {
        try {
            logger.info("Iniciando generación de reporte global de doctores para admin");
            byte[] pdfBytes = reportService.generarReporteDoctoresAdmin();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "reporte_doctores_global_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"))
                            + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generando reporte de doctores admin", e);
            String errorMessage = "Error generando reporte: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
