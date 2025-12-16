package com.medipac.medipac.service;

import com.medipac.medipac.config.JasperReportsConfig;
import com.medipac.medipac.model.*;
import com.medipac.medipac.repository.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para generación de reportes PDF usando JasperReports
 */
@Service
public class ReportService {

    @Autowired
    private JasperReportsConfig jasperConfig;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Genera reporte PDF de historia clínica de un paciente
     * 
     * @param pacienteId ID del paciente
     * @return Array de bytes del PDF generado
     * @throws Exception Si hay error en la generación
     */
    @Transactional(readOnly = true)
    public byte[] generarHistoriaClinicaPDF(Long pacienteId) throws Exception {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        // Obtener todas las citas completadas del paciente
        List<Cita> citasCompletadas = citaRepository.findByPacienteOrderByFechaHoraDesc(paciente).stream()
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
                .collect(Collectors.toList());

        List<HistoriaClinica> historias = new ArrayList<>();

        for (Cita cita : citasCompletadas) {
            // Buscar historia clínica por cita
            Optional<HistoriaClinica> historiaOpt = historiaClinicaRepository.findAll().stream()
                    .filter(h -> h.getCita() != null && h.getCita().getId().equals(cita.getId()))
                    .findFirst();
            historiaOpt.ifPresent(historias::add);
        }

        // Preparar datos para el reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nombrePaciente", paciente.getNombre() + " " + paciente.getApellido());
        parameters.put("fechaNacimiento",
                paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().toString() : "N/A");
        parameters.put("genero", paciente.getGenero() != null ? paciente.getGenero() : "N/A");
        parameters.put("telefono", paciente.getTelefono() != null ? paciente.getTelefono() : "N/A");
        parameters.put("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("totalHistorias", historias.size());

        // Crear lista de DTOs para el reporte
        List<HistoriaClinicaDTO> historiasDTO = historias.stream()
                .map(h -> new HistoriaClinicaDTO(
                        h.getCita().getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        h.getCita().getDoctor().getNombre() + " " + h.getCita().getDoctor().getApellido(),
                        h.getDiagnostico(),
                        h.getPrescripcion() != null ? h.getPrescripcion() : "Sin prescripción",
                        h.getNotas() != null ? h.getNotas() : ""))
                .collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(historiasDTO);

        // Compilar y llenar el reporte
        JasperReport jasperReport = jasperConfig.getCompiledReport("historia_clinica");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Exportar a PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    /**
     * Genera reporte PDF de agenda del doctor
     * 
     * @param doctorId    ID del doctor
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin    Fecha de fin del rango
     * @return Array de bytes del PDF generado
     * @throws Exception Si hay error en la generación
     */
    @Transactional(readOnly = true)
    public byte[] generarAgendaDoctorPDF(Long doctorId, LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        // Obtener citas en el rango de fechas
        List<Cita> citas = citaRepository.findByDoctorAndFechaHoraBetween(
                doctor,
                fechaInicio.atStartOfDay(),
                fechaFin.plusDays(1).atStartOfDay());

        // Preparar parámetros
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nombreDoctor", "Dr. " + doctor.getNombre() + " " + doctor.getApellido());
        parameters.put("numeroLicencia", doctor.getNumeroLicencia());
        parameters.put("fechaInicio", fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("fechaFin", fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("totalCitas", citas.size());

        // Contar citas por estado
        long programadas = citas.stream().filter(c -> c.getEstado() == EstadoCita.PROGRAMADA).count();
        long confirmadas = citas.stream().filter(c -> c.getEstado() == EstadoCita.CONFIRMADA).count();
        long completadas = citas.stream().filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count();
        long canceladas = citas.stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();

        parameters.put("citasProgramadas", programadas);
        parameters.put("citasConfirmadas", confirmadas);
        parameters.put("citasCompletadas", completadas);
        parameters.put("citasCanceladas", canceladas);

        // Crear DTOs
        List<CitaDTO> citasDTO = citas.stream()
                .map(c -> new CitaDTO(
                        c.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        c.getPaciente().getNombre() + " " + c.getPaciente().getApellido(),
                        c.getMotivo(),
                        c.getEstado().toString(),
                        c.getDuracionMinutos() + " min"))
                .collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(citasDTO);

        JasperReport jasperReport = jasperConfig.getCompiledReport("agenda_doctor");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    /**
     * Genera reporte PDF de lista de pacientes del doctor
     * 
     * @param doctorId ID del doctor
     * @return Array de bytes del PDF generado
     * @throws Exception Si hay error en la generación
     */
    @Transactional(readOnly = true)
    public byte[] generarListaPacientesPDF(Long doctorId) throws Exception {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        // Obtener pacientes únicos atendidos por el doctor
        List<Cita> todasCitas = citaRepository.findByDoctorOrderByFechaHoraAsc(doctor);
        Map<Long, PacienteReporteDTO> pacientesMap = new HashMap<>();

        for (Cita cita : todasCitas) {
            Paciente p = cita.getPaciente();
            if (!pacientesMap.containsKey(p.getUsuarioId())) {
                pacientesMap.put(p.getUsuarioId(), new PacienteReporteDTO(
                        p.getNombre() + " " + p.getApellido(),
                        p.getFechaNacimiento() != null
                                ? String.valueOf(LocalDate.now().getYear() - p.getFechaNacimiento().getYear())
                                : "N/A",
                        "",
                        0));
            }

            PacienteReporteDTO dto = pacientesMap.get(p.getUsuarioId());
            dto.setTotalCitas(dto.getTotalCitas() + 1);

            if (cita.getEstado() == EstadoCita.COMPLETADA &&
                    (dto.getUltimaConsulta().isEmpty() ||
                            cita.getFechaHora().isAfter(LocalDate.parse(dto.getUltimaConsulta(),
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay()))) {
                dto.setUltimaConsulta(cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        List<PacienteReporteDTO> pacientesList = new ArrayList<>(pacientesMap.values());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nombreDoctor", "Dr. " + doctor.getNombre() + " " + doctor.getApellido());
        parameters.put("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("totalPacientes", pacientesList.size());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pacientesList);

        JasperReport jasperReport = jasperConfig.getCompiledReport("lista_pacientes");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    // The user provided a duplicate method for generarAgendaDoctorPDF,
    // but the instruction was to insert "admin methods".
    // The provided code block also contains `generarReportePacientesAdmin` and
    // `generarReporteDoctoresAdmin`.
    // I will insert the admin methods and assume the `generarAgendaDoctorPDF` was a
    // mistake or a different version.
    // I will insert the admin methods before the first inner class.

    /**
     * Genera reporte PDF de TODOS los pacientes para el administrador
     */
    @Transactional(readOnly = true)
    public byte[] generarReportePacientesAdmin() throws Exception {
        List<Paciente> pacientes = pacienteRepository.findAll();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nombreDoctor", "ADMINISTRADOR"); // Reutilizamos parametro
        parameters.put("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("totalPacientes", pacientes.size());

        List<PacienteReporteDTO> pacientesDTO = pacientes.stream()
                .map(p -> new PacienteReporteDTO(
                        p.getNombre() + " " + p.getApellido(),
                        (p.getFechaNacimiento() != null)
                                ? String.valueOf(Period.between(p.getFechaNacimiento(), LocalDate.now()).getYears())
                                        + " años"
                                : "N/A",
                        // Usamos el campo ultimaConsulta para mostrar email/telefono o info relevante
                        p.getUsuario().getEmail(),
                        0))
                .sorted(Comparator.comparing(PacienteReporteDTO::getNombre))
                .collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pacientesDTO);
        // Usamos admin_pacientes.jrxml
        JasperReport jasperReport = jasperConfig.getCompiledReport("admin_pacientes");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    /**
     * Genera reporte PDF de TODOS los doctores para el administrador
     */
    /**
     * Genera reporte PDF de TODOS los doctores para el administrador
     */
    @Transactional(readOnly = true)
    public byte[] generarReporteDoctoresAdmin() throws Exception {
        List<Doctor> doctores = doctorRepository.findAll();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tituloReporte", "Reporte General de Doctores");
        parameters.put("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("totalDoctores", doctores.size());

        List<DoctorReporteDTO> doctoresDTO = doctores.stream()
                .map(d -> {
                    String nombre = "Dr. " + d.getNombre() + " " + d.getApellido();
                    String especialidades = d.getEspecialidades().stream()
                            .map(Especialidad::getNombre)
                            .collect(Collectors.joining(", "));
                    String especialidad = especialidades.isEmpty() ? "General" : especialidades;

                    return new DoctorReporteDTO(
                            nombre,
                            especialidad,
                            d.getNumeroLicencia(),
                            d.getTelefono(),
                            d.getUsuario().getEmail());
                })
                .sorted(Comparator.comparing(DoctorReporteDTO::getNombre))
                .collect(Collectors.toList());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(doctoresDTO);
        JasperReport jasperReport = jasperConfig.getCompiledReport("admin_doctores");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    // DTOs internos para los reportes
    public static class HistoriaClinicaDTO {
        private String fecha;
        private String doctor;
        private String diagnostico;
        private String prescripcion;
        private String notas;

        public HistoriaClinicaDTO(String fecha, String doctor, String diagnostico, String prescripcion, String notas) {
            this.fecha = fecha;
            this.doctor = doctor;
            this.diagnostico = diagnostico;
            this.prescripcion = prescripcion;
            this.notas = notas;
        }

        // Getters
        public String getFecha() {
            return fecha;
        }

        public String getDoctor() {
            return doctor;
        }

        public String getDiagnostico() {
            return diagnostico;
        }

        public String getPrescripcion() {
            return prescripcion;
        }

        public String getNotas() {
            return notas;
        }
    }

    public static class CitaDTO {
        private String fechaHora;
        private String paciente;
        private String motivo;
        private String estado;
        private String duracion;

        public CitaDTO(String fechaHora, String paciente, String motivo, String estado, String duracion) {
            this.fechaHora = fechaHora;
            this.paciente = paciente;
            this.motivo = motivo;
            this.estado = estado;
            this.duracion = duracion;
        }

        // Getters
        public String getFechaHora() {
            return fechaHora;
        }

        public String getPaciente() {
            return paciente;
        }

        public String getMotivo() {
            return motivo;
        }

        public String getEstado() {
            return estado;
        }

        public String getDuracion() {
            return duracion;
        }
    }

    public static class PacienteReporteDTO {
        private String nombre;
        private String edad;
        private String ultimaConsulta;
        private int totalCitas;

        public PacienteReporteDTO(String nombre, String edad, String ultimaConsulta, int totalCitas) {
            this.nombre = nombre;
            this.edad = edad;
            this.ultimaConsulta = ultimaConsulta;
            this.totalCitas = totalCitas;
        }

        // Getters y Setters
        public String getNombre() {
            return nombre;
        }

        public String getEdad() {
            return edad;
        }

        public String getUltimaConsulta() {
            return ultimaConsulta;
        }

        public int getTotalCitas() {
            return totalCitas;
        }

        public void setUltimaConsulta(String ultimaConsulta) {
            this.ultimaConsulta = ultimaConsulta;
        }

        public void setTotalCitas(int totalCitas) {
            this.totalCitas = totalCitas;
        }
    }

    public static class DoctorReporteDTO {
        private String nombre;
        private String especialidad;
        private String licencia;
        private String telefono;
        private String email;

        public DoctorReporteDTO(String nombre, String especialidad, String licencia, String telefono, String email) {
            this.nombre = nombre;
            this.especialidad = especialidad;
            this.licencia = licencia;
            this.telefono = telefono;
            this.email = email;
        }

        public String getNombre() {
            return nombre;
        }

        public String getEspecialidad() {
            return especialidad;
        }

        public String getLicencia() {
            return licencia;
        }

        public String getTelefono() {
            return telefono;
        }

        public String getEmail() {
            return email;
        }
    }
}
