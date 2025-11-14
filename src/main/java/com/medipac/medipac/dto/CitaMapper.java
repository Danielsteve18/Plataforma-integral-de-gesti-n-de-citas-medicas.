package com.medipac.medipac.dto;

import com.medipac.medipac.model.Cita;
import com.medipac.medipac.model.Especialidad;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Cita y DTOs
 */
@Component
public class CitaMapper {

    /**
     * Convierte una entidad Cita a un CitaDTO
     */
    public CitaDTO toDTO(Cita cita) {
        if (cita == null) {
            return null;
        }

        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        
        // Información del paciente
        if (cita.getPaciente() != null) {
            dto.setPacienteId(cita.getPaciente().getUsuarioId());
            dto.setPacienteNombre(cita.getPaciente().getNombre());
            dto.setPacienteApellido(cita.getPaciente().getApellido());
        }
        
        // Información del doctor
        if (cita.getDoctor() != null) {
            dto.setDoctorId(cita.getDoctor().getUsuarioId());
            dto.setDoctorNombre(cita.getDoctor().getNombre());
            dto.setDoctorApellido(cita.getDoctor().getApellido());
            
            // Obtener la primera especialidad (si existe)
            if (cita.getDoctor().getEspecialidades() != null && !cita.getDoctor().getEspecialidades().isEmpty()) {
                dto.setEspecialidad(
                    cita.getDoctor().getEspecialidades().stream()
                        .findFirst()
                        .map(Especialidad::getNombre)
                        .orElse("General")
                );
            }
        }
        
        // Información de la cita
        dto.setFechaHora(cita.getFechaHora());
        dto.setDuracionMinutos(cita.getDuracionMinutos());
        dto.setMotivo(cita.getMotivo());
        dto.setEstado(cita.getEstado());
        dto.setFechaCreacion(cita.getFechaCreacion());
        dto.setFechaActualizacion(cita.getFechaActualizacion());
        dto.setNotasCancelacion(cita.getNotasCancelacion());
        dto.setTieneHistoriaClinica(cita.getHistoriaClinica() != null);
        
        return dto;
    }

    /**
     * Convierte una lista de Citas a una lista de CitaDTOs
     */
    public List<CitaDTO> toDTOList(List<Cita> citas) {
        if (citas == null) {
            return List.of();
        }
        return citas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
