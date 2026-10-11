package com.vidasalud.citas.dto;

import com.vidasalud.citas.model.Cita;

import java.time.LocalDateTime;

public record CitaResponse(
        Long id,
        Long pacienteId,
        String especialidad,
        LocalDateTime fechaHora,
        String profesionalCodigo,
        String motivo,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {

    public static CitaResponse from(Cita c) {
        return new CitaResponse(c.getId(), c.getPacienteId(), c.getEspecialidad(), c.getFechaHora(),
                c.getProfesionalCodigo(), c.getMotivo(), c.getEstado(), c.getFechaCreacion(),
                c.getFechaActualizacion());
    }
}
