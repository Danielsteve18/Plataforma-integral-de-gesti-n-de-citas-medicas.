package com.medipac.medipac.dto;

import java.util.List;

public class CalendarioDTO {
    
    private Long doctorId;
    private List<DiaCalendarioDTO> dias;
    
    public CalendarioDTO() {}
    
    public CalendarioDTO(Long doctorId, List<DiaCalendarioDTO> dias) {
        this.doctorId = doctorId;
        this.dias = dias;
    }
    
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public List<DiaCalendarioDTO> getDias() {
        return dias;
    }
    
    public void setDias(List<DiaCalendarioDTO> dias) {
        this.dias = dias;
    }
}
