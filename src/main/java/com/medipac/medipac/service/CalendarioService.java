package com.medipac.medipac.service;

import com.medipac.medipac.dto.CalendarioDTO;
import com.medipac.medipac.dto.DiaCalendarioDTO;
import com.medipac.medipac.model.Doctor;
import com.medipac.medipac.model.Horario;
import com.medipac.medipac.repository.DoctorRepository;
import com.medipac.medipac.repository.HorarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class CalendarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(CalendarioService.class);
    
    @Autowired
    private HorarioRepository horarioRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    // Definición de jornadas con sus horarios
    private static final Map<String, LocalTime[]> JORNADAS = new HashMap<>();
    
    static {
        JORNADAS.put("MAÑANA", new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(12, 0)});
        JORNADAS.put("TARDE", new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(18, 0)});
        JORNADAS.put("NOCHE", new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(23, 0)});
    }
    
    /**
     * Obtiene el calendario semanal del doctor
     */
    @Transactional(readOnly = true)
    public CalendarioDTO obtenerCalendario(Long doctorId) {
        logger.info("Obteniendo calendario para doctor ID: {}", doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado con ID: " + doctorId));
        
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = inicioSemana.plusDays(7);
        
        List<Horario> horarios = horarioRepository.findByDoctorAndFechaBetween(doctor, inicioSemana, finSemana);
        
        // Agrupar horarios por día
        Map<DayOfWeek, List<String>> calendarioMap = new HashMap<>();
        
        for (Horario horario : horarios) {
            DayOfWeek dia = horario.getFecha().getDayOfWeek();
            String jornada = identificarJornada(horario.getHoraInicio(), horario.getHoraFin());
            
            if (jornada != null) {
                calendarioMap.computeIfAbsent(dia, k -> new ArrayList<>()).add(jornada);
            }
        }
        
        // Crear DTOs para todos los días de la semana
        List<DiaCalendarioDTO> dias = new ArrayList<>();
        for (DayOfWeek dia : DayOfWeek.values()) {
            List<String> jornadas = calendarioMap.getOrDefault(dia, Collections.emptyList());
            dias.add(new DiaCalendarioDTO(
                dia.name(),
                !jornadas.isEmpty(),
                jornadas
            ));
        }
        
        return new CalendarioDTO(doctorId, dias);
    }
    
    /**
     * Guarda el calendario semanal del doctor
     */
    @Transactional
    public void guardarCalendario(CalendarioDTO calendarioDTO) {
        logger.info("Guardando calendario para doctor ID: {}", calendarioDTO.getDoctorId());
        
        Doctor doctor = doctorRepository.findById(calendarioDTO.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado con ID: " + calendarioDTO.getDoctorId()));
        
        // Calcular rango de la semana actual
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = inicioSemana.plusDays(7);
        
        // Eliminar horarios existentes de la semana
        horarioRepository.deleteByDoctorAndFechaBetween(doctor, inicioSemana, finSemana);
        
        // Crear nuevos horarios basados en la configuración
        List<Horario> nuevosHorarios = new ArrayList<>();
        
        for (DiaCalendarioDTO diaDTO : calendarioDTO.getDias()) {
            if (!diaDTO.isActivo() || diaDTO.getJornadas() == null || diaDTO.getJornadas().isEmpty()) {
                continue;
            }
            
            try {
                DayOfWeek dia = DayOfWeek.valueOf(diaDTO.getDia());
                LocalDate fecha = inicioSemana.with(TemporalAdjusters.nextOrSame(dia));
                
                for (String jornadaNombre : diaDTO.getJornadas()) {
                    LocalTime[] horario = JORNADAS.get(jornadaNombre);
                    if (horario != null) {
                        Horario nuevoHorario = new Horario();
                        nuevoHorario.setDoctor(doctor);
                        nuevoHorario.setFecha(fecha);
                        nuevoHorario.setHoraInicio(horario[0]);
                        nuevoHorario.setHoraFin(horario[1]);
                        nuevoHorario.setDisponible(true);
                        
                        nuevosHorarios.add(nuevoHorario);
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.error("Día inválido: {}", diaDTO.getDia(), e);
            }
        }
        
        // Guardar todos los horarios
        horarioRepository.saveAll(nuevosHorarios);
        logger.info("Calendario guardado exitosamente. Total horarios creados: {}", nuevosHorarios.size());
    }
    
    /**
     * Identifica a qué jornada pertenece un horario
     */
    private String identificarJornada(LocalTime horaInicio, LocalTime horaFin) {
        for (Map.Entry<String, LocalTime[]> entry : JORNADAS.entrySet()) {
            LocalTime[] horario = entry.getValue();
            if (horario[0].equals(horaInicio) && horario[1].equals(horaFin)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Obtiene las jornadas disponibles con sus horarios
     */
    public Map<String, String> obtenerJornadasDisponibles() {
        Map<String, String> jornadas = new LinkedHashMap<>();
        jornadas.put("MAÑANA", "08:00 - 12:00");
        jornadas.put("TARDE", "14:00 - 18:00");
        jornadas.put("NOCHE", "19:00 - 23:00");
        return jornadas;
    }
}
