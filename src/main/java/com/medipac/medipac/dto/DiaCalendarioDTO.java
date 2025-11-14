package com.medipac.medipac.dto;

import java.util.ArrayList;
import java.util.List;

public class DiaCalendarioDTO {
    
    private String dia;
    private boolean activo;
    private List<String> jornadas;
    
    public DiaCalendarioDTO() {
        this.jornadas = new ArrayList<>();
    }
    
    public DiaCalendarioDTO(String dia, boolean activo, List<String> jornadas) {
        this.dia = dia;
        this.activo = activo;
        this.jornadas = jornadas != null ? jornadas : new ArrayList<>();
    }
    
    public String getDia() {
        return dia;
    }
    
    public void setDia(String dia) {
        this.dia = dia;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public List<String> getJornadas() {
        return jornadas;
    }
    
    public void setJornadas(List<String> jornadas) {
        this.jornadas = jornadas;
    }
}
